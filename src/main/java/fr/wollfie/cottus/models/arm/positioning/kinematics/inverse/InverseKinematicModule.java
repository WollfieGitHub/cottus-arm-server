package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.utils.maths.Vector;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.matrices.MatrixUtil;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import org.ejml.data.SingularMatrixException;
import org.ejml.simple.SimpleMatrix;

import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Kinematic Analysis :
 * The specification of each joint of the arm relative to the previous one (Excluding the end effector's)
 * 
 * R1 Orthog R2 Orthog R3 Orthog R4 Orthog Orthog R5 Orthog R6
 */
public class InverseKinematicModule {

    private static final double ALPHA = 1;
    private static final double BETA = 0.5;
    
    // Static class, cannot be instantiated
    private InverseKinematicModule() {}

// //======================================================================================\\
// ||                                                                                      ||
// ||                                       INVERSE KINEMATICS                             ||
// ||                                                                                      ||
// \\======================================================================================//
    
    /**
     * TODO: 12/8/2022 DOCUMENT THIS FUNCTION 
     * @param arm The state of the arm. Info on actuators max/min angles and length will be used
     * @param endEffectorPosition The position of the end effector in world space
     * @param endEffectorRotation The orientation of the end effector in world space
     * @return The set of angles that allow the arm to position itself as desired
     * @throws NoSolutionException If there is no solution to set the angles to obtain the desired arm configuration
     *
     * @implNote From "Solving Kinematics Problems of a 6-DOF Robot Manipulator", Alireza Khatamian: 
     * The computation of the inverse kinematics given in these papers were adapted to the robot arm's configuration
     */
    public static Vector inverseSolve(
            CottusArm arm,
            Vector3D endEffectorPosition,
            Rotation endEffectorRotation
    ) throws NoSolutionException {
        InverseKinematicModule module = new InverseKinematicModule();
        return module.ikSolve(
                arm, endEffectorPosition, endEffectorRotation.getEulerAngles(),
                1.0, 0.5, 24, 12
        );
    }
    
    private static final double Delta = 0.01;
    /** 
     * Explanation of algorithm (from the paper) :
     * For m = 2, the Jacobians J1t and J2t are created, leading to two possible solutions Q1t and Q2t. Through
     * forward kinematics, each Qit determines a new pose Xit . In this simple example, X2t is selected since
     * it is closer to the desired/final point Xfinal. In the next iteration t + 1, the process will continue 
     * from this position, Q2t.
     * */
    private Vector ikSolve(
            CottusArm arm,
            Vector3D pos,
            Vector3D rot,
            double maxErrorMm, double standardDeviation,
            int nbThreads, int nbIndividuals
    ) throws NoSolutionException {
        Random random = new Random();
        DHTable table = arm.getDHTable().copy();
        // We don't care about the end effector's end angle
        int n = arm.getNbOfJoints()-1;
        // Desired configuration of the end effector
        Vector xFinal = new Vector(pos.x, pos.y, pos.z, rot.x, rot.y, rot.z);

        // Forward kinematic function
        Function<Vector, Vector> f = q -> {
            SimpleMatrix transform = table.getTransformMatrix(0, n);
            Vector3D translation = MatrixUtil.mult(transform, Vector3D.Zero);
            Vector3D rotation = MatrixUtil.extractRotation(transform) ;
            return new Vector(translation.x, translation.y, translation.z, rotation.x, rotation.y, rotation.z);
        };

        // Current joint configuration (angles) of the arm
        Vector qT = new Vector(IntStream.range(0, n).mapToDouble(table::getTheta).toArray());
        // Current configuration (position and rotation) of the end effector
        Vector xT = f.apply(qT);
        
        // Attenuation factor that governs convergence speed of the algorithm
        final double alphaT = 0.1;
        final int spread = nbThreads/nbIndividuals;
        
        // Save a sqrt
        while (xT.subtract(xFinal).normSquared() >= maxErrorMm*maxErrorMm) {
            
            // Fork with multiple threads and randomly generate jacobians with "White Noise"
            // distribution to converge faster
            SimpleMatrix[][] jacobians = new SimpleMatrix[nbIndividuals][spread];
            Vector[][] qTs = new Vector[nbIndividuals][spread];
            Vector[][] xTs = new Vector[nbIndividuals][spread];

            Vector[] xL = new Vector[spread];
            Vector[] qL = new Vector[spread];
            
            Vector finalQT = qT, finalXT = xT;
            IntStream.range(0, nbThreads).parallel().forEach(k -> {

                int l = k / (spread);
                int kl = k % (spread);

                Vector[] cols = new Vector[n];
                for (int c = 0; c < n; c++) {
                    cols[c] = finalXT.subtract(f.apply(finalQT.add(Vector.unit(c++, n).scaled(Delta))));
                }

                SimpleMatrix jacobian = MatrixUtil.from(cols);
                
                jacobians[l][kl] = MatrixUtil.apply(jacobian, d -> d+random.nextGaussian(0, standardDeviation));
                
                try {
                    Vector deltaQT;
                    
                    if (n == 3 + 3) { deltaQT = MatrixUtil.mult(jacobians[l][kl].invert(), xFinal.subtract(finalXT).scaled(alphaT)); } 
                    else { deltaQT = MatrixUtil.mult(jacobians[l][kl].pseudoInverse(), xFinal.subtract(finalXT).scaled(alphaT)); }
                    
                    qTs[l][kl] = finalQT.add(deltaQT);
                    xTs[l][kl] = f.apply(qTs[l][kl]);
                } catch (SingularMatrixException e) { qTs[l][kl] = null; xTs[l][kl] = null; }
            });
            
            // Join all threads by finding the angles that most minimize the error 
            // relative to distance and rotation
            for (int l = 0; l < nbIndividuals; l++) {
                int minIndex = getMinIndex(spread, xFinal, xTs, l);
                xL[l] = xTs[l][minIndex];
                qL[l] = qTs[l][minIndex];
            }

            int minIndex = getMinIndexInBreeds(nbIndividuals, xFinal, xL, qL);
            xT = xL[minIndex];
            qT = qL[minIndex];
        }
        // Once error is small enough, return the angles
        return qT;
    }

    /** @return The index of the bred item with minimum fitness */
    private int getMinIndexInBreeds(int breedSize, Vector xF, Vector[] xL, Vector[] qL) {
        double min = Double.MAX_VALUE;
        
        int minIndex = -1;
        double fitness;
        for (int l = 0; l < breedSize; l++) {
            Vector xI = xL[l];
            fitness = ( 
                    (xF.get(0)-xI.get(0)) * (xF.get(0)-xI.get(0))
                    + (xF.get(1)-xI.get(1)) * (xF.get(1)-xI.get(1))
                    + (xF.get(2)-xI.get(2)) * (xF.get(2)-xI.get(2))
            ) * ALPHA + ( // IMPORTANCE OF TRANSLATION
                    (xF.get(3)-xI.get(3)) * (xF.get(3)-xI.get(3))
                            + (xF.get(4)-xI.get(4)) * (xF.get(4)-xI.get(4))
                            + (xF.get(5)-xI.get(5)) * (xF.get(5)-xI.get(5))
            ) * BETA; // IMPORTANCE OF ROTATION
            if (fitness < min) { minIndex = l; min = fitness; }
        }
        return minIndex;
    }

    /** Returns the index of the change with minimal error */
    private static int getMinIndex(int spread, Vector xFinal, Vector[][] xTs, int l) {
        double min = Double.MAX_VALUE;
        int minIndex = -1;
        for (int kl = 0; kl < spread; kl++) {
            double error = xFinal.subtract(xTs[l][kl]).normSquared();
            if (xTs[l][kl] != null && error < min) { 
                minIndex = kl;
                min = error;
            }
        }
        return minIndex;
    }

}
