package fr.wollfie.cottus.models.animation.pathing;

import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.smallrye.mutiny.tuples.Tuple3;

/**
 * An animation that makes the end effector of the arm stay at its current position
 * for the specified duration in seconds
 */
public class WaitAnimation extends EndEffectorAnimation {
    
    private final double timeSec;

    /**
     * An animation that makes the end effector of the arm stay at its current position
     * for the specified duration in seconds
     * @param timeSec The duration in seconds
     */
    public WaitAnimation(double timeSec) {
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
