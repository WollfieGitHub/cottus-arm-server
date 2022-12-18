package fr.wollfie.cottus.models.animation.pathing;

import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.smallrye.mutiny.tuples.Tuple3;

public class WaitAnimation extends EndEffectorAnimation {
    
    private final double timeSec;

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
