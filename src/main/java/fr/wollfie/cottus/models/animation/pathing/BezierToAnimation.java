package fr.wollfie.cottus.models.animation.pathing;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class BezierToAnimation extends EndEffectorAnimation implements AnimationPrimitive {

    /** The duration of the animation in seconds */
    @JsonProperty("timeSec") private final double timeSec;
    @JsonGetter("timeSec") public double getTimeSec() { return timeSec; }

    /** The curve, parametrized from 0 to 1 (start to finish) */
    private final Function<Double, Vector3D> bezierCurve;
    
    @JsonProperty("anchorPoints") private final Vector3D[] anchorPoints;
    @JsonGetter("anchorPoints") public Vector3D[] getAnchorPoints() { return anchorPoints; }

    
    @JsonProperty("endPosition") private final Vector3D endPosition;
    @JsonGetter("endPosition") public Vector3D getEndPosition() { return endPosition; }

    public BezierToAnimation(
            @JsonProperty("relative") boolean relative, 
            @JsonProperty("endPosition") Vector3D endPosition, 
            @JsonProperty("timeSec") double timeSec, 
            @JsonProperty("anchorPoints") Vector3D... anchorPoints
    ) {
        super(relative);
        this.timeSec = timeSec;
        this.endPosition = endPosition;
        this.anchorPoints = anchorPoints;

        // First and last coefficient of the curve is end and begin points
        List<Vector3D> points = new ArrayList<>();
        points.add(Vector3D.Zero);
        points.addAll(List.of(this.anchorPoints));
        points.add(this.endPosition);
        
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
