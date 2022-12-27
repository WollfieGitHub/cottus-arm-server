package fr.wollfie.cottus.models.arm.positioning.kinematics.inverse;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.exception.NoSolutionException;
import fr.wollfie.cottus.models.arm.positioning.kinematics.DHTable;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.algorithms.EvolutionaryIK;
import fr.wollfie.cottus.models.arm.positioning.kinematics.inverse.algorithms.SimpleJacobianIK;
import fr.wollfie.cottus.utils.maths.Vector;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.matrices.MatrixUtil;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import org.ejml.simple.SimpleMatrix;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class KinematicsModule {
    
    /** Max error on position */
    private static final double ALPHA = 1.0;
    /** Max error on rotation */
    private static final double BETA = 0.5;

    public KinematicsModule() {  }

    /**
     * Provided with the angles of the joints of the arm, returns the position and rotation of 
     * the end effector
     * @param angles The angles of each joint of the arm
     * @return The position and rotation of the arm in the form {@code Vector[pos.x, pos.y, pos.z, rot.x, rot.y, rot.z]}
     * where {@code pos} is the position and {@code rot} is the euler angles of the rotation
     */
    @NotNull public Vector forward(DHTable table, Vector angles) {
        table.setThetas(angles);
        SimpleMatrix transform = table.getTransformMatrix(0,table.size() -1);
        Vector3D translation = MatrixUtil.extractTranslation(transform);
        Vector3D rotation = MatrixUtil.extractRotation(transform) ;
        return new Vector(translation.x, translation.y, translation.z, rotation.x, rotation.y, rotation.z);
    }

    /**
     * @param arm The state of the arm. Info on actuators max/min angles and length will be used
     * @param endEffectorPosition The position of the end effector in world space
     * @param endEffectorRotation The orientation of the end effector in world space
     * @return The set of angles that allow the arm to position itself as desired
     * @throws NoSolutionException If there is no solution to set the angles to obtain the desired arm configuration
     */
    public List<Double> inverseSolve(
            CottusArm arm,
            Vector3D endEffectorPosition,
            Rotation endEffectorRotation
    ) throws NoSolutionException {
        IKSolver module = new SimpleJacobianIK(this);
        return module.ikSolve(
                arm, endEffectorPosition, endEffectorRotation,
                1.0, KinematicsModule::defaultFitness
        );
        // Default 24, 12, 32
    }


    /** @return The index of the bred item with minimum fitness */
    private static double defaultFitness(Vector xCurr, Vector xGoal) {
        return  (
                (xGoal.get(0)-xCurr.get(0)) * (xGoal.get(0)-xCurr.get(0))
                        + (xGoal.get(1)-xCurr.get(1)) * (xGoal.get(1)-xCurr.get(1))
                        + (xGoal.get(2)-xCurr.get(2)) * (xGoal.get(2)-xCurr.get(2))
        ) * ALPHA + ( // IMPORTANCE OF TRANSLATION
                (xGoal.get(3)-xCurr.get(3)) * (xGoal.get(3)-xCurr.get(3))
                        + (xGoal.get(4)-xCurr.get(4)) * (xGoal.get(4)-xCurr.get(4))
                        + (xGoal.get(5)-xCurr.get(5)) * (xGoal.get(5)-xCurr.get(5))
        ) * BETA; // IMPORTANCE OF ROTATION;
    }
}
