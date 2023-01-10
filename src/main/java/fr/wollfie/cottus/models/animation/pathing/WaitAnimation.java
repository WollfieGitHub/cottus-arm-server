package fr.wollfie.cottus.models.animation.pathing;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.smallrye.mutiny.tuples.Tuple3;

/**
 * An animation that makes the end effector of the arm stay at its current position
 * for the specified duration in seconds
 */
public class WaitAnimation extends EndEffectorAnimation implements AnimationPrimitive {
    
    @JsonProperty("timeSec") private final double timeSec;
    @JsonGetter("timeSec") public double getTimeSec() { return this.timeSec; };

    /**
     * An animation that makes the end effector of the arm stay at its current position
     * for the specified duration in seconds
     * @param timeSec The duration in seconds
     */
    public WaitAnimation(
           @JsonProperty("timeSec") double timeSec
    ) {
        super(true);
        this.timeSec = timeSec;
    }

    @Override
    public double getDurationSecs() { return timeSec; }

    @Override
    protected Tuple3<Vector3D, Rotation, Double> relativeEvaluateAt(double secFromStart) {
        return Tuple3.of(Vector3D.Zero, Rotation.Identity, 0.0);
    }
}
