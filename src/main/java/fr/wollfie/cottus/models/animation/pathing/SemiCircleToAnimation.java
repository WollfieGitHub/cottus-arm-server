package fr.wollfie.cottus.models.animation.pathing;

import fr.wollfie.cottus.utils.Preconditions;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.smallrye.mutiny.tuples.Tuple3;

import static java.lang.Math.*;

/**
 * Creates an animation that animates the end effector in a semi circle, of the specified 
 * angle in total, which center is situated somewhere in the middle of the (relative or absolute) 
 * origin and the desired end position.
 */
public class SemiCircleToAnimation extends EndEffectorAnimation {

    /** Radius of the circle */
    private final double radius;
    /** Total angle of the circle */
    private final double angleRad;
    
    /** The center point of the circle */
    private final Vector3D centerPoint;
    private final Vector3D u;
    private final Vector3D v;
    
    private final double timeSec;

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
    public SemiCircleToAnimation(boolean relative, Vector3D endPosition, double timeSec, 
                                 double angleDeg, Vector3D circleDirection) {
        super(relative);
        Preconditions.checkArgument(!circleDirection.isZero());
        // The {circleDirection} vector is the height of the isosceles triangle which
        // goes through the summit which is the center of the circle. This isosceles triangle has
        // its sides of equal lengths equal to the radius of the circle
        // This triangle's third side is the chord length of the remaining arc
        // https://www.omnicalculator.com/math/arc-length
        this.angleRad = toRadians(angleDeg);
        double arcAngle = PI/2.0 - this.angleRad;
        
        Vector3D chord = endPosition.minus(Vector3D.Zero);
        double chordLength = chord.norm();
        
        double a = sin(arcAngle/2.0);
        double radius = chordLength / (2*a);
        
        // Half of the base of the triangle and its other side with the Pythagorean Theorem
        double height = sqrt(radius*radius - (chordLength*chordLength/4));
        // TODO REMOVE ONCE CHECKED
        if (Double.isNaN(height)) { throw new IllegalStateException("Wrong sign under the root"); }
        
        this.centerPoint = circleDirection
                .normalized()
                .scaled(height)
                .plus(chord.scaled(0.5));
        // Vectors orthogonal to the normal, base of the plan of the circle
        this.u = circleDirection.normalized();
        this.v = chord.normalized();
        
        this.radius = radius;
        this.timeSec = timeSec;
    }

    @Override
    public double getDurationSecs() { return timeSec; }

    @Override
    protected Tuple3<Vector3D, Rotation, Double> relativeEvaluateAt(double secFromStart) {
        double theta = this.angleRad/timeSec;
        // https://www.quora.com/A-problem-in-3D-geometry-what-is-the-equation-of-the-circle-see-details
        return Tuple3.of(
                this.centerPoint
                        .plus( u.scaled(radius * cos(theta)) )
                        .plus( v.scaled(radius * sin(theta)) ),
                Rotation.Identity,
                0.0
        );
    }
}
