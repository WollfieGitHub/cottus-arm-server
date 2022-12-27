package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.algorithms;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.dto.JointBounds;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.IKSolver;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.KinematicsModule;
import fr.wollfie.cottus.utils.maths.Vector;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.matrices.MatrixUtil;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.quarkus.logging.Log;
import org.ejml.simple.SimpleMatrix;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

public class SimpleJacobianIK implements IKSolver {

    private Vector xFinal;
    private List<JointBounds> bounds;
    private DHTable table;
    private int n;
    private Vector qT;
    
    private final KinematicsModule kinematics;
    private Vector xT, dX;
    private Vector[] cols;
    private SimpleMatrix jacobianInverse;

    public SimpleJacobianIK(KinematicsModule kinematics) {
        this.kinematics = kinematics;
    }

    @Override
    public List<Double> ikSolve(
            CottusArm arm, Vector3D position, Rotation rotation,
            double maxError, BiFunction<Vector, Vector, Double> computeError
    ){
        final int maxInnerIter = 20;
        final int maxOuterIter = 200;
        
        // Extract the bounds for the angle of the joints
        bounds = arm.joints().stream().map(Joint::getBounds).toList();
        // Copy the table so that it doesn't actually change the state of the arm
        table = arm.getDHTable().copy();
        // We don't care about the end effector's end angle
        n = arm.getNbOfJoints();
        
        // Desired configuration of the end effector
        Vector3D rot = rotation.getEulerAngles();
        xFinal = new Vector(position.x, position.y, position.z, rot.x, rot.y, rot.z);
        
        // Current joint configuration (angles) of the arm
        qT = new Vector(IntStream.range(0, n).mapToDouble(table::getTheta).toArray());
        // Current configuration (position and rotation) of the end effector
        xT = updateCurrentPosition();

        // Keep track of the number of operations
        int outerIter = 0, innerIter;
        double error, inverseError;
        long first, last, current;
        
        last = System.nanoTime();
        first = System.nanoTime();

        SimpleMatrix jacobian;
        // Iterate while the error is too big or until limit reached
        while (outerIter <= maxOuterIter
                && (error = computeError.apply(xT, xFinal)) >= maxError) {
            outerIter++;
            
            jacobian = computeJacobian();

            innerIter = 0;
            // Divide by two such that dX = 1*dX at first iteration
            dX = xFinal.subtract(xT).scaled(2);
            do {
                innerIter++;
                // Divide by two each time we enter the loop
                dX = dX.scaled(1/2.0);

                // Find the pseudo inverse of the jacobian 
                jacobianInverse = jacobian.pseudoInverse();

                // Compute error on the jacobian
                inverseError = MatrixUtil.mult(
                        SimpleMatrix.identity(6).minus(jacobian.mult(jacobianInverse)), dX
                ).normSquared();

            } while (innerIter <= maxInnerIter && inverseError >= maxError*maxError);

            // Update the angles according to our guess on the diff
            qT = getUpdatedAngles(jacobianInverse, dX);
            table.setThetas(qT);
            // And update the position in cartesian space
            xT = updateCurrentPosition();
            
            current = System.nanoTime();
            Log.infof("Iteration %d : %.3fms - Error : %.3f",
                    outerIter,
                    (double)((current-last)/1e6),
                    Math.sqrt(error));
            last = current;
        }

        current = System.nanoTime();
        Log.infof("Total : %.3fms, Average %.3fms",
                (double)(current-first)/1e6,
                (double)((current-first)/(1e6*outerIter)));

        // Once error is small enough, return the angles
        List<Double> angles = new ArrayList<>();
        for (int i = 0; i < qT.dim; i++) { if (!table.isVirtual(i)) { angles.add(qT.get(i)); } }
        return angles;
    }

    private Vector getUpdatedAngles(SimpleMatrix jacobianInverse, Vector dX) {
        double[] clampedValues = qT.add(MatrixUtil.mult(jacobianInverse, dX)).getValues();
        for (int i = 0; i < n; i++) { clampedValues[i] = this.bounds.get(i).clamp(clampedValues[i]); }
        return new Vector(clampedValues);
    }

    @NotNull
    private Vector updateCurrentPosition() { return kinematics.forward(table, qT); }

    /** Compute the columns of the jacobian matrix */
    private SimpleMatrix computeJacobian() {
        Vector delta;
        Vector[] cols = new Vector[n];
        for (int c = 0; c < n; c++) {
            if (table.isVirtual(c)) { delta = Vector.Zero(n); }
            else { delta = Vector.unit(c, n).scaled(Delta); }

            cols[c] = xT.subtract(kinematics.forward(table, qT.add(delta)));
        }
        return MatrixUtil.from(cols);
    }
}
