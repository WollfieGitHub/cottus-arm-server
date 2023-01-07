package fr.wollfie.cottus.models.animation.pathing;

import fr.wollfie.cottus.utils.maths.MathUtils;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.smallrye.mutiny.tuples.Tuple3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static fr.wollfie.cottus.utils.maths.MathUtils.binomial;
import static java.lang.Math.pow;

/**
 * An animation following a Bézier curve with specified start position, end position
 * and anchor points
 */
public class BezierToAnimation extends EndEffectorAnimation {

    /** The duration of the animation in seconds */
    private final double timeSec;
    /** The curve, parametrized from 0 to 1 (start to finish) */
    private final Function<Double, Vector3D> bezierCurve;

    public BezierToAnimation(boolean relative, Vector3D endPosition, double timeSec, Vector3D... anchorPoints) {
        super(relative);
        this.timeSec = timeSec;
        
        // First and last coefficient of the curve is end and begin points
        List<Vector3D> points = new ArrayList<>();
        points.add(Vector3D.Zero);
        points.addAll(List.of(anchorPoints));
        points.add(endPosition);
        
        // Compute the "equation" of the curve
        this.bezierCurve = MathUtils.bezierCurve(points);
    }

    @Override
    public double getDurationSecs() {
        return timeSec;
    }

    /**
     * @param secFromStart Seconds from start of the animation
     * @return The specification for the end effector
     */
    @Override
    protected Tuple3<Vector3D, Rotation, Double> relativeEvaluateAt(double secFromStart) {

        double t = secFromStart / timeSec;

        return Tuple3.of( this.bezierCurve.apply(t), Rotation.Identity, 0.0 );
    }
}
