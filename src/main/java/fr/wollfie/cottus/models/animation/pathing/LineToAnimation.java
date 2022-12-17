package fr.wollfie.cottus.models.animation.pathing;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.wollfie.cottus.dto.ArmSpecification;
import fr.wollfie.cottus.dto.ArmAnimation;
import fr.wollfie.cottus.models.arm.positioning.specification.RelativeEndEffectorSpecification;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;

public final class LineToAnimation extends RelativeArmAnimation {
    
    private final Vector3D position;
    private final double timeSec;
    
    public LineToAnimation(
            @JsonProperty("position") Vector3D position,
            @JsonProperty("timeSec") double timeSec
    ) {
        this.position = position;
        this.timeSec = timeSec;
    }
    
    @Override
    protected RelativeEndEffectorSpecification relativeEvaluateAt(double secFromStart) {
        return new RelativeEndEffectorSpecification(
                position.interpolate(Vector3D.Zero, secFromStart/this.timeSec),
                Rotation.Identity, 0
        );
    }

    @Override
    public double getDurationSecs() { return timeSec; }

}
