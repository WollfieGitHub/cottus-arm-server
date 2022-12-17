package fr.wollfie.cottus.dto;

import fr.wollfie.cottus.models.arm.positioning.specification.EndEffectorSpecification;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

import java.util.List;

/**
 * A set of parameters that fully specify the state of the arm at a given
 * point in time. It should remove all degrees of freedom
 */
public interface ArmSpecification {

    /**
     * @return The *7* angles, from root to end effector,
     */
    List<Double> getAngles();

    /**
     * Creates a new Arm Specification by specifying only the position of
     * the end effector
     * @param endPosition The position in 3D space of the end effector
     * @param endRotation The proper rotation in 3D space of the end effector
     * @param endAngle The end effector's angle
     * @return The arm specification
     */
    static ArmSpecification fromEnd(
            Vector3D endPosition,
            Rotation endRotation,
            double endAngle,
            boolean relativePosition
    ) {
        return new EndEffectorSpecification( endPosition, endRotation, endAngle );
    }
}
