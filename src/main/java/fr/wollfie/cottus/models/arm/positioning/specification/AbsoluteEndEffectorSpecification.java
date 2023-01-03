package fr.wollfie.cottus.models.arm.positioning.specification;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.wollfie.cottus.dto.specification.EndEffectorSpecification;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

import java.util.List;

public class AbsoluteEndEffectorSpecification extends EndEffectorSpecification {
    
    public AbsoluteEndEffectorSpecification(
            @JsonProperty("endEffectorPosition") Vector3D endEffectorPosition,
            @JsonProperty("endEffectorRotation") Rotation endEffectorOrientation,
            @JsonProperty("endEffectorAngle") double endEffectorAngleRad
    ) {
        super(endEffectorPosition, endEffectorOrientation, endEffectorAngleRad);
    }

    @Override
    public List<Double> getAngles() {
        return null;
    }

    @Override
    public String toString() {
        return String.format("AbsoluteEndEffectorSpecification{%s, %s, %5.3f}",
                this.getEndEffectorPosition(),
                this.getEndEffectorOrientation(),
                this.getEndEffectorAngleRad());
    }
}
