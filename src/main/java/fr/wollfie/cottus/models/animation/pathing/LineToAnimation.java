package fr.wollfie.cottus.models.animation.pathing;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.wollfie.cottus.models.arm.positioning.specification.RelativeEndEffectorSpecification;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.smallrye.mutiny.tuples.Tuple3;

/**
 * An animation that animates linearly from the current position 
 * to the specified end position (either relative or absolute) in the given amount of seconds.
 */
public final class LineToAnimation extends EndEffectorAnimation {
    
    private final Vector3D position;
    private final double timeSec;
    
    public LineToAnimation(
            @JsonProperty("relative") boolean relative,
            @JsonProperty("position") Vector3D position,
            @JsonProperty("time") double timeSec
    ) {
        super(relative);
        this.position = position;
        this.timeSec = timeSec;
    }
    
    @Override
    protected Tuple3<Vector3D, Rotation, Double>  relativeEvaluateAt(double secFromStart) {
        return Tuple3.of(
                position.interpolate(Vector3D.Zero, secFromStart/this.timeSec),
                Rotation.Identity,
                0.0
        );
    }

    @Override
    public double getDurationSecs() { return timeSec; }

}
