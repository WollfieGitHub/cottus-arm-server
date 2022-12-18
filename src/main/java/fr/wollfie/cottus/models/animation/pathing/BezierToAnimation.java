package fr.wollfie.cottus.models.animation.pathing;

import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.smallrye.mutiny.tuples.Tuple;
import io.smallrye.mutiny.tuples.Tuple3;

import java.util.stream.Stream;

public class BezierToAnimation extends EndEffectorAnimation{
    
    private final Vector3D endPosition;
    private final double timeSec;
    private final Vector3D[] anchorPoints;

    public BezierToAnimation(boolean relative, Vector3D endPosition, double timeSec, Vector3D... anchorPoints) {
        super(relative);
        this.endPosition = endPosition;
        this.timeSec = timeSec;
        this.anchorPoints = anchorPoints;
    }

    @Override
    public double getDurationSecs() { return timeSec; }

    @Override
    protected Tuple3<Vector3D, Rotation, Double> relativeEvaluateAt(double secFromStart) {
        double[] bezierCoefficient = new double[anchorPoints.length];
        // TODO BEZIER COEFFICIENTS
        // https://mathcurve.com/courbes3d.gb/bezier3d/bezier3d.shtml
        
        Vector3D result = Vector3D.Zero;
        for (int i = 0; i < anchorPoints.length; i++) {
            result = result.add( anchorPoints[i].scaledBy(bezierCoefficient[i]) );
        }
        return Tuple3.of( result, Rotation.Identity, 0.0 );
    }
}
