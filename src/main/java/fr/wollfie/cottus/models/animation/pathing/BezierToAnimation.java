package fr.wollfie.cottus.models.animation.pathing;

import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.smallrye.mutiny.tuples.Tuple3;

import java.util.ArrayList;
import java.util.List;

import static fr.wollfie.cottus.utils.Utils.binom;
import static java.lang.Math.pow;

public class BezierToAnimation extends EndEffectorAnimation {

    private final double timeSec;
    private final List<Vector3D> anchorPoints;

    public BezierToAnimation(boolean relative, Vector3D endPosition, double timeSec, Vector3D... anchorPoints) {
        super(relative);
        this.timeSec = timeSec;
        this.anchorPoints = new ArrayList<>();

        // First and last coefficient of the curve is end and begin points
        this.anchorPoints.add(Vector3D.Zero);
        this.anchorPoints.addAll(List.of(anchorPoints));
        this.anchorPoints.add(endPosition);
    }

    @Override
    public double getDurationSecs() {
        return timeSec;
    }

    /**
     * @param secFromStart Seconds from start of the animation
     * @return The specification for the end effector
     * @implNote <a href="https://mathcurve.com/courbes3d.gb/bezier3d/bezier3d.shtml">Source</a>
     */
    @Override
    protected Tuple3<Vector3D, Rotation, Double> relativeEvaluateAt(double secFromStart) {

        double t = secFromStart / timeSec;
        int n = anchorPoints.size();
        Vector3D result = Vector3D.Zero;

        for (int k = 0; k < n; k++) {
            double bernsteinCoefficient = binom(k, n) * pow(t, k) * pow(1 - t, n - k);
            result = result.add(anchorPoints.get(k).scaledBy(bernsteinCoefficient));
        }

        return Tuple3.of(result, Rotation.Identity, 0.0);
    }
}
