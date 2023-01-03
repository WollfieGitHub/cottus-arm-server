package fr.wollfie.cottus.dto.specification;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.wollfie.cottus.dto.specification.ArmSpecification;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

public abstract class EndEffectorSpecification implements ArmSpecification {

    @JsonProperty("engEffectorAngle") private final double endEffectorAngleRad;
    @JsonProperty("endEffectorPosition") private final Vector3D endEffectorPosition;
    @JsonProperty("endEffectorRotation") private final Rotation endEffectorOrientation;

    public EndEffectorSpecification(
            @JsonProperty("endEffectorPosition") Vector3D endEffectorPosition,
            @JsonProperty("endEffectorRotation") Rotation endEffectorOrientation,
            @JsonProperty("endEffectorAngle") double endEffectorAngleRad
    ) {
        this.endEffectorAngleRad = endEffectorAngleRad;
        this.endEffectorPosition = endEffectorPosition;
        this.endEffectorOrientation = endEffectorOrientation; 
    }

    @JsonGetter("endEffectorAngle") public double getEndEffectorAngleRad() { return endEffectorAngleRad; }
    @JsonGetter("endEffectorPosition") public Vector3D getEndEffectorPosition() { return endEffectorPosition; }
    @JsonGetter("endEffectorRotation") public Rotation getEndEffectorOrientation() { return endEffectorOrientation; }
}
