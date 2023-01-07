package fr.wollfie.cottus.dto.specification;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

/** 
 * <p>
 *     A specification of the position, orientation of the end effector paired with
 *     a <span style="font-weight: bold;">desired</span> arm angle.
 * </p> 
 * <p>
 *     The Arm is the angle formed between the plane_0 and plane_phi where 
 *     <ul>
 *         <li>plane_0 is the plane formed by The base, The Shoulder, The Wrist</li>
 *         <li>plane_0 is the plane formed by The base, The Elbow, The Wrist</li>
 *     </ul>
 * </p>
 * 
 * */
public abstract class EndEffectorSpecification implements ArmSpecification {

    /** Preferred angle for the arm */
    @JsonProperty("armAngle") private final double preferredArmAngle;
    /** Position in 3D space of the end effector */
    @JsonProperty("endEffectorPosition") private final Vector3D endEffectorPosition;
    /** Orientation in 3D space of the end effector */
    @JsonProperty("endEffectorRotation") private final Rotation endEffectorOrientation;

    public EndEffectorSpecification(
            @JsonProperty("endEffectorPosition") Vector3D endEffectorPosition,
            @JsonProperty("endEffectorRotation") Rotation endEffectorOrientation,
            @JsonProperty("armAngle") double preferredArmAngle
    ) {
        this.preferredArmAngle = preferredArmAngle;
        this.endEffectorPosition = endEffectorPosition;
        this.endEffectorOrientation = endEffectorOrientation; 
    }

    @JsonGetter("armAngle") public double getPreferredArmAngle() { return preferredArmAngle; }
    @JsonGetter("endEffectorPosition") public Vector3D getEndEffectorPosition() { return endEffectorPosition; }
    @JsonGetter("endEffectorRotation") public Rotation getEndEffectorOrientation() { return endEffectorOrientation; }

    @Override
    public String toString() {
        return String.format("EndEffectorSpecification{%s, %s, %s}",
                preferredArmAngle,
                endEffectorPosition,
                endEffectorOrientation
        );
    }
}
