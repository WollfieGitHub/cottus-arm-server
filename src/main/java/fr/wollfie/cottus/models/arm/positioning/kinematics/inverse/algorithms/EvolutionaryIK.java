package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.algorithms;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.dto.JointBounds;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.IKSolver;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.KinematicsModule;
import fr.wollfie.cottus.utils.maths.Vector;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.matrices.MatrixUtil;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.quarkus.logging.Log;
import org.ejml.data.SingularMatrixException;
import org.ejml.simple.SimpleMatrix;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

/**
 * Kinematic Analysis :
 * The specification of each joint of the arm relative to the previous one (Excluding the end effector's)
 * 
 * R1 Orthog R2 Orthog R3 Orthog R4 Orthog Orthog R5 Orthog R6
 */
public class EvolutionaryIK implements IKSolver {

    private static final double ALPHA = 1;
    private static final double BETA = 0.5;
    private SimpleMatrix jacobian;
    private SimpleMatrix[][] jacobians;
    private Vector[][] qTs;
    private Vector[][] xLs;
    private Vector[] xL;
    private Vector[] qL;
    private Vector xFinal;
    private Random random;
    private DHTable table;
    private int n;
    private Vector qT;
    private Vector xT;
    private int spread;
    private double alphaT;
    private double standardDeviation;
    private int nbIndividuals;
    private ExecutorService executorService;
    private List<JointBounds> bounds;

// //======================================================================================\\
// ||                                                                                      ||
// ||                                       INVERSE KINEMATICS                             ||
// ||                                                                                      ||
// \\======================================================================================//
    
    private static final double Delta = 0.000001;
    
    private final KinematicsModule kinematics;

    public EvolutionaryIK(KinematicsModule kinematicsModule) {
        this.kinematics = kinematicsModule;
    }

    /** 
     * Explanation of algorithm (from the paper) :
     * For m = 2, the Jacobians J1t and J2t are created, leading to two possible solutions Q1t and Q2t. Through
     * forward kinematics, each Qit determines a new pose Xit . In this simple example, X2t is selected since
     * it is closer to the desired/final point Xfinal. In the next iteration t + 1, the process will continue 
     * from this position, Q2t.
     * */
    private List<Double> ikSolve(
            CottusArm arm,
            Vector3D pos, Vector3D rot,
            double maxErrorMm,
            int nbThreads, int nbIndividuals, int maxIter
    ) throws NoSolutionException, ExecutionException, InterruptedException {
        this.nbIndividuals = nbIndividuals;
        random = new Random();
        bounds = arm.joints().stream().map(Joint::getBounds).toList();
        
        table = arm.getDHTable().copy();
        // We don't care about the end effector's end angle
        n = arm.getNbOfJoints();
        // Desired configuration of the end effector
        xFinal = new Vector(pos.x, pos.y, pos.z, rot.x, rot.y, rot.z);
        
        // Forward kinematic function

        // Current joint configuration (angles) of the arm
        qT = new Vector(IntStream.range(0, n).mapToDouble(table::getTheta).toArray());
        // Current configuration (position and rotation) of the end effector
        xT = kinematics.forward(table, qT);
        
        // Attenuation factor that governs convergence speed of the algorithm
        standardDeviation = maxErrorMm/2.0;
        final double alpha0 = 1.0;
        spread = nbThreads/ nbIndividuals;
        
        jacobians = new SimpleMatrix[nbIndividuals][spread];
        qTs = new Vector[nbIndividuals][spread];
        xLs = new Vector[nbIndividuals][spread];

        xL = new Vector[nbIndividuals];
        qL = new Vector[nbIndividuals];
        
        int iter = 0;

        executorService = Executors.newFixedThreadPool(nbThreads);
        
        while (iter <= maxIter && xT.subtract(xFinal).normSquared() >= maxErrorMm*maxErrorMm) {
            iter++;
            alphaT = alpha0/(iter*iter*iter);
            
            // Fork with multiple threads and randomly generate jacobians with "White Noise"
            // distribution to converge faster


            Vector[] cols = new Vector[n];
            Vector delta;
            for (int c = 0; c < n; c++) {
                if (table.isVirtual(c)) { delta = Vector.Zero(n); }
                else { delta = Vector.unit(c, n).scaled(Delta); }

                cols[c] = xT.subtract(kinematics.forward(table, qT.add(delta)));
            }

            jacobian = MatrixUtil.from(cols);
            
            for (int i = 0; i < nbThreads; i++) {
                final int l = i / spread;
                final int kl = i % spread;
                executorService.submit(() -> iterate(l, kl)).get();
            }

            for (int l = 0; l < nbIndividuals; l++) {
                int minIndex = getMinIndex(spread, xFinal, xLs, l);
                xL[l] = xLs[l][minIndex];
                qL[l] = qTs[l][minIndex];
            }

            int minIndex = getMinIndexInBreeds(nbIndividuals, xFinal, xL, qL);
            xT = xL[minIndex];
            qT = qL[minIndex];
        }

        // Once error is small enough, return the angles
        List<Double> angles = new ArrayList<>();
        for (int i = 0; i < qT.dim; i++) { if (!table.isVirtual(i)) { angles.add(qT.get(i)); } }
        return angles;
    }

    /** Generate the jacobians */
    private void iterate(int l, int kl) {


        jacobians[l][kl] = MatrixUtil.apply(jacobian, d -> d+ random.nextGaussian(0, standardDeviation));

        Vector deltaQT;
        try {
            if (n == 3 + 3) { deltaQT = MatrixUtil.mult(jacobians[l][kl].invert(), xFinal.subtract(xT).scaled(alphaT)); }
            else { deltaQT = MatrixUtil.mult(jacobians[l][kl].pseudoInverse(), xFinal.subtract(xT).scaled(alphaT)); }

        } catch (SingularMatrixException | IllegalArgumentException e) { deltaQT = Vector.Zero(n); }

        // Clamp the values so that they respect the bounds 
        double[] values = qT.add(deltaQT).getValues();
        for (int i = 0; i < n; i++) { values[i] = this.bounds.get(i).clamp(values[i]); }
        
        qTs[l][kl] = new Vector(values);
        xLs[l][kl] = kinematics.forward(table, qTs[l][kl]);

    }

    /** @return The index of the bred item with minimum fitness */
    private int getMinIndexInBreeds(int breedSize, Vector xF, Vector[] xL, Vector[] qL) {
        double min = Double.MAX_VALUE;
        
        int minIndex = 0;
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
        int minIndex = 0;
        for (int kl = 0; kl < spread; kl++) {
            if (xTs[l][kl] != null) {
                double error = xFinal.subtract(xTs[l][kl]).normSquared();
                if (error < min) {
                    minIndex = kl;
                    min = error;
                }
            }
        }
        return minIndex;
    }

    @Override
    public List<Double> ikSolve(CottusArm arm, Vector3D position, Rotation rotation, double maxError, BiFunction<Vector, Vector, Double> computeError) {
        return null;
    }
}
