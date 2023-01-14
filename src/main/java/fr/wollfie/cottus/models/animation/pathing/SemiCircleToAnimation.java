package fr.wollfie.cottus.models.animation.pathing;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.wollfie.cottus.utils.Preconditions;
import fr.wollfie.cottus.utils.Utils;
import fr.wollfie.cottus.utils.maths.MathUtils;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.smallrye.mutiny.tuples.Tuple3;

import static java.lang.Math.*;

/**
 * Creates an animation that animates the end effector in a semi circle, of the specified 
 * angle in total, which center is situated somewhere in the middle of the (relative or absolute) 
 * origin and the desired end position.
 */
public class SemiCircleToAnimation extends EndEffectorAnimation implements AnimationPrimitive {

    /** Radius of the circle */
    private final double radius;
    /** Total angle of the circle */
    private final double theta;
    
    /** The center point of the circle */
    private final Vector3D centerPoint;
    private final Vector3D u;
    private final Vector3D v;
    
    @JsonProperty("timeSec") private final double timeSec;
    @JsonGetter("timeSec") public double getTimeSec() { return timeSec; }
    
    @JsonProperty("circleDirection") private final Vector3D circleDirection;
    @JsonGetter("circleDirection") public Vector3D getCircleDirection() { return circleDirection; }

    @JsonProperty("endPosition") private final Vector3D endPosition;
    @JsonGetter("endPosition") public Vector3D getEndPosition() { return endPosition; }

    @JsonProperty("angleDeg") private final double angleDeg;
    @JsonGetter("angleDeg") public double getAngleDeg() { return angleDeg; }
    
    /**
     * <p>
     *     Creates an animation that animates the end effector in a semicircle, of the specified 
     *     angle in total, which center is situated somewhere in the middle of the (relative or absolute) 
     *     origin and the desired end position. The {@param circleDirection} parameters is the height
     *     isosceles triangle which goes through the summit which is the center of the circle
     * </p>
     * <img src="../../../../../../../resources/documentation/SemiCircleToAnimationIllustration.png" width=600 height=600>
     * @param relative Whether the animation should be relative to the current position of the end effector
     * @param endPosition The position of the end effector at the end of the animation
     * @param timeSec The total duration of the animation
     * @param angleDeg The angle in degrees of the semicircle
     * @param circleDirection The direction of circle
     */
    public SemiCircleToAnimation(
            @JsonProperty("relative") boolean relative,
            @JsonProperty("endPosition") Vector3D endPosition,
            @JsonProperty("timeSec") double timeSec, 
            @JsonProperty("angleDeg") double angleDeg, 
            @JsonProperty("circleDirection") Vector3D circleDirection
    ) {
        super(relative);
        Preconditions.checkArgument(!circleDirection.isZero());
        this.circleDirection = circleDirection;
        this.endPosition = endPosition;
        this.angleDeg = angleDeg;
        this.timeSec = timeSec;
        // The {circleDirection} vector is the height of the isosceles triangle which
        // goes through the summit which is the center of the circle. This isosceles triangle has
        // its sides of equal lengths equal to the radius of the circle
        // This triangle's third side is the chord length of the remaining arc
        // https://www.omnicalculator.com/math/arc-length
        this.theta = toRadians(this.angleDeg);

        Vector3D chord = this.endPosition.minus(Vector3D.Zero);
        double chordLength = chord.norm();
        
        double radius = chordLength / (2*sin( this.theta/2.0 ));
        
        // TODO REMOVE ONCE CHECKED
        double height = sqrt(radius*radius - (chordLength*chordLength/4.0));
        if (Double.isNaN(height)) { throw new IllegalStateException("Wrong sign under the root"); }
        
        this.centerPoint = Vector3D.Zero
                .plus(chord.scaled(1/2.0))
                .plus(circleDirection.normalized().scaled(height));
        // Vectors orthogonal to the normal, base of the plan of the circle
        this.u = chord.normalized().scaled(-1);
        this.v = circleDirection.normalized();
        
        this.radius = radius;
    }

    @Override
    public double getDurationSecs() { return timeSec; }

    @Override
    protected Tuple3<Vector3D, Rotation, Double> relativeEvaluateAt(double secFromStart) {
        double theta = (secFromStart / this.getDurationSecs()) * this.theta
                - (this.theta - PI) / 2.0;
        // https://www.quora.com/A-problem-in-3D-geometry-what-is-the-equation-of-the-circle-see-details
        return Tuple3.of(
                this.centerPoint
                        .plus( u.scaled(radius * cos(theta)) )
                        .plus( v.scaled(radius * sin(theta)) ),
                Rotation.Identity, 0.0
        );
    }
}
