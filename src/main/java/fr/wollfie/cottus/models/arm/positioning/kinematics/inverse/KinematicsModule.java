package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.algorithms.Analytical7DOFsIK;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.algorithms.EvolutionaryIK;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.algorithms.SimpleJacobianIK;
import fr.wollfie.cottus.utils.Constants;
import fr.wollfie.cottus.utils.maths.Axis3D;
import fr.wollfie.cottus.utils.maths.Vector;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.matrices.MatrixUtil;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import org.ejml.simple.SimpleMatrix;
import org.jetbrains.annotations.NotNull;

public class KinematicsModule {
    
    private enum IKAlgorithm {
        SIMPLE_PSEUDO_INVERSE_JACOBIAN, // Doesn't work, very chaotic
        PARALLEL_EVOLUTIONARY_IK,       // Work but only for position
        ANALYTICAL_IK,                  // 
    }

    public KinematicsModule() {  }
    
    private Thread ikThread;
    private static final IKAlgorithm IK_ALGORITHM = IKAlgorithm.ANALYTICAL_IK;

    /**
     * Provided with the angles of the joints of the arm, returns the position and rotation of 
     * the end effector
     * @param angles The angles of each joint of the arm
     * @return The position and rotation of the arm in the form {@code Vector[pos.x, pos.y, pos.z, rot.x, rot.y, rot.z]}
     * where {@code pos} is the position and {@code rot} is the euler angles of the rotation
     */
    @NotNull public Vector forward(DHTable table, Vector angles) {
        table.setThetas(angles);
        int n = table.size()-1;
        SimpleMatrix t0n = table.getTransformMatrix(0, n);
        Vector3D translation = MatrixUtil.extractTranslation(t0n);
        Vector3D rotation = MatrixUtil.multHt(t0n, Axis3D.Z.unitVector).minus(translation);
        return new Vector(translation.x, translation.y, translation.z, rotation.x, rotation.y, rotation.z);
    }

    /**
     * @param arm The state of the arm. Info on actuators max/min angles and length will be used
     * @param endEffectorPosition The position of the end effector in world space
     * @param endEffectorRotation The orientation of the end effector in world space
     * @return The set of angles that allow the arm to position itself as desired
     */
    public IKFuture inverseSolve(
            CottusArm arm,
            Vector3D endEffectorPosition,
            Rotation endEffectorRotation
    ) {
        IKFuture future = IKFuture.createNew();
        if (this.ikThread != null) { this.ikThread.interrupt(); }
        
        this.ikThread = new Thread(() -> {
            // Choose which algorithm to use
            IKSolver solver = switch (IK_ALGORITHM) {
                case PARALLEL_EVOLUTIONARY_IK -> new EvolutionaryIK(this, future);
                case SIMPLE_PSEUDO_INVERSE_JACOBIAN -> new SimpleJacobianIK(this, future);
                // TODO CHANGE ARM ANGLE TO VARIABLE
                case ANALYTICAL_IK -> new Analytical7DOFsIK(this, future, 0);
            };
            // Then start solving ik
            solver.startIKSolve(
                    arm, endEffectorPosition, endEffectorRotation,
                    10.0, Math.toRadians(5)
            );
        });
        this.ikThread.start();
        return future;
        // Default 24, 12, 32
    }
    
}
