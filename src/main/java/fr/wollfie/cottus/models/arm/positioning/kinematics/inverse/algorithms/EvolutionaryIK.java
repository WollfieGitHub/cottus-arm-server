package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.algorithms;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.dto.JointBounds;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.IKFuture;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.IKSolver;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.KinematicsModule;
import fr.wollfie.cottus.utils.maths.Axis3D;
import fr.wollfie.cottus.utils.maths.Vector;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.matrices.MatrixUtil;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.quarkus.logging.Log;
import org.ejml.simple.SimpleMatrix;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Kinematic Analysis :
 * The specification of each joint of the arm relative to the previous one (Excluding the end effector's)
 * 
 * R1 Orthog R2 Orthog R3 Orthog R4 Orthog Orthog R5 Orthog R6
 */
public class EvolutionaryIK implements IKSolver {

    private SimpleMatrix jacobian;
    private SimpleMatrix[][] jacobians;
    private Vector[][] qTs;
    private Vector[][] xLs;
    private Vector xFinal;
    private Random random;
    private DHTable table;
    private int n;
    private Vector qT;
    private Vector xT;
    private double alphaT;
    private double standardDeviation;
    private List<JointBounds> bounds;
    private final IKFuture ikFuture;

// //======================================================================================\\
// ||                                                                                      ||
// ||                                       INVERSE KINEMATICS                             ||
// ||                                                                                      ||
// \\======================================================================================//
    
    private final KinematicsModule kinematics;

    public EvolutionaryIK(KinematicsModule kinematicsModule, IKFuture future) {
        this.kinematics = kinematicsModule;
        this.ikFuture = future;
    }

    /** 
     * Explanation of algorithm (from the paper) :
     * For m = 2, the Jacobians J1t and J2t are created, leading to two possible solutions Q1t and Q2t. Through
     * forward kinematics, each Qit determines a new pose Xit . In this simple example, X2t is selected since
     * it is closer to the desired/final point Xfinal. In the next iteration t + 1, the process will continue 
     * from this position, Q2t.
     * */
    @Override
    public void startIKSolve(
            CottusArm arm, Vector3D pos, Rotation rotation,
            double maxPosError, double maxRotError
    ) {
        final int nbIndividuals = 12;
        final int nbThreads = 24;
        
        final int maxIter = 200;

        long first, last, current;
        first = System.nanoTime();
        
        random = new Random();
        bounds = arm.joints().stream().map(Joint::getBounds).toList();
        
        table = arm.dhTable().copy();
        // We don't care about the end effector's end angle
        n = arm.getNbOfJoints();
        // Desired configuration of the end effector
        Vector3D rot = rotation.getEulerAngles();
        Vector3D zAxis = Axis3D.Z.rotatedAtOriginUsing(rot);
        xFinal = new Vector(pos.x, pos.y, pos.z, zAxis.x, zAxis.y, zAxis.z);
        
        // Forward kinematic function

        // Current joint configuration (angles) of the arm
        qT = new Vector(IntStream.range(0, n).mapToDouble(table::getVarTheta).toArray());
        // Current configuration (position and rotation) of the end effector
        xT = kinematics.forward(table, qT);
        
        // Attenuation factor that governs convergence speed of the algorithm
        standardDeviation = (maxPosError+maxRotError)/2.0;
        final double alpha0 = 1.0;
        int spread = nbThreads / nbIndividuals;
        
        jacobians = new SimpleMatrix[nbIndividuals][spread];
        qTs = new Vector[nbIndividuals][spread];
        xLs = new Vector[nbIndividuals][spread];

        Vector[] xL = new Vector[nbIndividuals];
        Vector[] qL = new Vector[nbIndividuals];
        
        int iter = 0;

        ExecutorService executorService = Executors.newFixedThreadPool(nbThreads);
        
        last = System.nanoTime();
        double error;
        
        while (iter <= maxIter ) {
            iter++;
            error = Math.sqrt(IKSolver.getFitness(xT, xFinal));
            alphaT = alpha0 * 2 * (error/(1000.0));

            if (IKSolver.errorIsUnderThreshold(xT, xFinal, maxPosError, maxRotError))
            {
                current = System.nanoTime();
                Log.infof("Total : %.3fms, Average %.3fms",
                        (double)(current-first)/1e6,
                        (double)((current-first)/(1e6*iter)));

                // Once error is small enough, return the angles
                this.ikFuture.updateWith(getAngles(table, qT));
                executorService.shutdown();
                return;
            }
            
            // Fork with multiple threads and randomly generate jacobians with "White Noise"
            // distribution to converge faster

            jacobian = IKSolver.computeJacobianWithCross(n, table, xT);
            
            for (int i = 0; i < nbThreads; i++) {
                final int l = i / spread;
                final int kl = i % spread;
                try {
                    executorService.submit(() -> iterate(l, kl)).get();
                } catch (InterruptedException | ExecutionException e) { throw new RuntimeException(e); }
            }

            for (int l = 0; l < nbIndividuals; l++) {
                int minIndex = getMinIndex(spread, xFinal, xLs, l);
                xL[l] = xLs[l][minIndex];
                qL[l] = qTs[l][minIndex];
            }

            int minIndex = getMinIndexInBreeds(nbIndividuals, xFinal, xL);
            xT = xL[minIndex];
            qT = qL[minIndex];
            
            ikFuture.updateWith(getAngles(table, qT));

            current = System.nanoTime();
            Log.infof("Iteration %3d : %5.3fms - Error : %5.3f", iter, (double)((current-last)/1e6), error);
            last = current;
        }
        executorService.shutdown();
        ikFuture.fail();
    }

    @NotNull
    private List<Double> getAngles(DHTable table, Vector qT) {
        // Once error is small enough, return the angles
        List<Double> angles = new ArrayList<>();
        for (int i = 0; i < qT.dim; i++) { if (!table.isVirtual(i)) { angles.add(qT.get(i)); } }
        return angles;
    }

    /** Generate the jacobians */
    private void iterate(int l, int kl) {
        jacobians[l][kl] = MatrixUtil.apply(jacobian, d -> d+ random.nextGaussian(0, standardDeviation));

        Vector deltaQT = MatrixUtil.mult(jacobians[l][kl].pseudoInverse(), xFinal.minus(xT).scaled(alphaT));
        // Clamp the values so that they respect the bounds 
        double[] values = qT.plus(deltaQT).getValues();
        for (int i = 0; i < n; i++) { values[i] = this.bounds.get(i).clamp(values[i]); }
        
        qTs[l][kl] = new Vector(values);
        xLs[l][kl] = kinematics.forward(table, qTs[l][kl]);

    }

    /** @return The index of the bred item with minimum fitness */
    private int getMinIndexInBreeds(int breedSize, Vector xF, Vector[] xL) {
        double min = Double.MAX_VALUE;
        
        int minIndex = 0;
        double fitness;
        for (int l = 0; l < breedSize; l++) {
            Vector xI = xL[l];
            fitness = IKSolver.getFitness(xI, xF);
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
                double error = xFinal.minus(xTs[l][kl]).normSquared();
                if (error < min) {
                    minIndex = kl;
                    min = error;
                }
            }
        }
        return minIndex;
    }

    
}
