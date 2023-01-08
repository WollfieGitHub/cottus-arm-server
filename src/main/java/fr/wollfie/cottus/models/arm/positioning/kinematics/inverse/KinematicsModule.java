package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.specification.EndEffectorSpecification;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.algorithms.Analytical7DOFsIK;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.algorithms.EvolutionaryIK;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.algorithms.IKFuture;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.algorithms.SimpleJacobianIK;
import fr.wollfie.cottus.models.arm.positioning.specification.AbsoluteEndEffectorSpecification;
import fr.wollfie.cottus.models.arm.positioning.specification.RelativeEndEffectorSpecification;
import fr.wollfie.cottus.utils.maths.Axis3D;
import fr.wollfie.cottus.utils.maths.Vector;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.matrices.MatrixUtil;
import io.quarkus.logging.Log;
import org.ejml.simple.SimpleMatrix;
import org.jetbrains.annotations.NotNull;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@ApplicationScoped
public class KinematicsModule {
    
    private enum IKAlgorithm {
        SIMPLE_PSEUDO_INVERSE_JACOBIAN, // Doesn't work, very chaotic
        PARALLEL_EVOLUTIONARY_IK,       // Work but only for position
        ANALYTICAL_IK,                  // 
    }

    public KinematicsModule() {  }
    
    private static final IKAlgorithm IK_ALGORITHM = IKAlgorithm.ANALYTICAL_IK;
    private static IKFuture currentIKSolve;
    
    /**
     * Provided with the angles of the joints of the arm, returns the position and rotation of 
     * the end effector
     * @param angles The angles of each joint of the arm
     * @return The position and rotation of the arm in the form {@code Vector[pos.x, pos.y, pos.z, rot.x, rot.y, rot.z]}
     * where {@code pos} is the position and {@code rot} is the euler angles of the rotation
     */
    @NotNull 
    public static Vector forward(DHTable table, Vector angles) {
        table.setThetas(angles);
        int n = table.size()-1;
        SimpleMatrix t0n = table.getTransformMatrix(0, n);
        Vector3D translation = MatrixUtil.extractTranslation(t0n);
        Vector3D rotation = MatrixUtil.multHt(t0n, Axis3D.Z.unitVector).minus(translation);
        return new Vector(translation.x, translation.y, translation.z, rotation.x, rotation.y, rotation.z);
    }

    /**
     * @param arm The state of the arm. Info on actuators max/min angles and length will be used
     * @param endEffectorSpecification The position and rotation of the end effector in world space,
     *                                with specific arm angle
     * @return The set of angles that allow the arm to position itself as desired
     */
    public static IKFuture inverseSolve(
            CottusArm arm, EndEffectorSpecification endEffectorSpecification
    ) {
        AbsoluteEndEffectorSpecification absoluteEndEffectorSpecification;
        if (endEffectorSpecification instanceof RelativeEndEffectorSpecification relativeSpecification) {
            absoluteEndEffectorSpecification = relativeSpecification.toAbsolute(arm);
        // Otherwise the specification is absolute
        } else { absoluteEndEffectorSpecification = (AbsoluteEndEffectorSpecification) endEffectorSpecification; }
        
        if (currentIKSolve != null && !currentIKSolve.isDone()) { currentIKSolve.cancel(true); }
        
        // Need this weird stuff because of lambda's difficulty to deal with checked expression
        currentIKSolve = new IKFuture();
        CompletableFuture.runAsync(() -> {
            IKSolver solver = switch (IK_ALGORITHM) {
                case PARALLEL_EVOLUTIONARY_IK -> new EvolutionaryIK();
                case SIMPLE_PSEUDO_INVERSE_JACOBIAN -> new SimpleJacobianIK();
                // TODO CHANGE ARM ANGLE TO VARIABLE
                case ANALYTICAL_IK -> new Analytical7DOFsIK();
            };
            // Then start solving ik
            try { 
                List<Double> solution = solver.startIKSolve(arm, absoluteEndEffectorSpecification, 10.0, Math.toRadians(5));
                currentIKSolve.complete(solution);
            } catch (Exception e) { currentIKSolve.completeExceptionally(e);  }
        });
        return currentIKSolve;
    }
    
}
