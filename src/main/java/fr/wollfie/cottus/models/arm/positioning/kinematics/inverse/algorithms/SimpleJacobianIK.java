package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.algorithms;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.dto.JointBounds;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.IKSolver;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.KinematicsModule;
import fr.wollfie.cottus.models.arm.positioning.specification.AbsoluteEndEffectorSpecification;
import fr.wollfie.cottus.utils.maths.Axis3D;
import fr.wollfie.cottus.utils.maths.Vector;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.matrices.MatrixUtil;
import io.quarkus.logging.Log;
import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class SimpleJacobianIK implements IKSolver {

    private List<JointBounds> bounds;
    private DHTable table;
    private int n;
    
    private Vector xFinal, xT, dX, qT, dQT;
    private Vector[] cols;
    private SimpleMatrix jacobianInverse, jacobian;
    private CottusArm arm;
    
    private static final int MAX_OUTER_ITER = 500;
    private static final double STEP_SIZE = 0.5*1000;
    private static final double THETA_MAX_STEP = 0.2;

    public SimpleJacobianIK() { }

    @Override
    public List<Double> startIKSolve(
            CottusArm arm, AbsoluteEndEffectorSpecification specification,
            double maxPosError, double maxRotError
    ) throws NoSolutionException {
        this.arm = arm;
        long first, last, current;
        first = System.nanoTime();
        
        // Extract the bounds for the angle of the joints
        bounds = arm.joints().stream().map(Joint::getBounds).toList();
        // Copy the table so that it doesn't actually change the state of the arm
        table = arm.dhTable().copy();
        // We don't care about the end effector's end angle
        n = arm.getNbOfJoints();
        
        // Desired configuration of the end effector
        Vector3D rot = specification.getEndEffectorOrientation().getEulerAngles();
        // Rotate the Z axis of the base frame
        Vector3D zAxis = Axis3D.Z.rotatedAtOriginUsing(rot);
        Vector3D position = specification.getEndEffectorPosition();
        xFinal = new Vector(position.x, position.y, position.z, zAxis.x, zAxis.y, zAxis.z);
        
        // Current joint configuration (angles) of the arm
        qT = new Vector(IntStream.range(0, n).mapToDouble(table::getVarTheta).toArray());
        // Current configuration (position and rotation) of the end effector
        xT = updateCurrentPosition();
        
        Log.infof("\nxT = %s\nxF = %s", xT, xFinal);
        
        // Keep track of the number of operations
        int outerIter = 0;

        last = System.nanoTime();
        
        // Iterate while the error is too big or until limit reached
        while (outerIter <= MAX_OUTER_ITER) {
            
            // Test if the error is small enough
            if (IKSolver.errorIsUnderThreshold(xT, xFinal, maxPosError, maxRotError)) 
            {
                current = System.nanoTime();
                Log.infof("Total : %.3fms, Average %.3fms",
                        (double)(current-first)/1e6,
                        (double)((current-first)/(1e6*outerIter)));
                // Once error is small enough, return the angles
                return getAngles(table, qT);
            }
            
            jacobian = IKSolver.computeJacobianWithCross(n, table, xT);
            // Find the pseudo inverse of the jacobian 
            jacobianInverse = jacobian.pseudoInverse();
            
            // Compute difference in position and rotation of the end effector
            dX = xFinal.minus(xT).normalized().scaled(IKSolver.getFitness(xT, xFinal)*STEP_SIZE);
            // Compute the difference in angle to get closer to result
            dQT = MatrixUtil.mult(jacobianInverse, dX).clamped(-THETA_MAX_STEP, +THETA_MAX_STEP);
            // Update the angles according to our guess on the diff
            qT = IKSolver.getBoundedAngles( qT.plus(dQT), this.bounds );
            // And update the position in cartesian space
            xT = updateCurrentPosition();
            
            current = System.nanoTime();
            Log.infof("Iteration %3d : %5.3fms, delta : %5.3e - Error : %5.3f, dist : %5.3f",
                    outerIter,
                    (double)((current-last)/1e6),
                    dQT.norm(), xFinal.minus(xT).norm(), dX.norm()
            );
            // Update info and iteration index
            last = current;
            outerIter++;
        }
        
        // The algorithm couldn't converge
        throw new NoSolutionException();
    }

    /** Converts the vector of angles into a list of angles */
    private List<Double> getAngles(DHTable table, Vector qT) {
        List<Double> angles = new ArrayList<>();
        for (int i = 0; i < qT.dim; i++) { if (!table.isVirtual(i)) { angles.add(qT.get(i)); } }
        return angles;
    }

    /** Returns the position of the end effector given the current table and angles */
    private Vector updateCurrentPosition() { return KinematicsModule.forward(table, qT); }

    
}
