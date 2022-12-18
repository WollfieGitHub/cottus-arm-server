package fr.wollfie.cottus.models.animation.pathing;

import fr.wollfie.cottus.utils.Preconditions;
import fr.wollfie.cottus.utils.maths.Vector3D;
import fr.wollfie.cottus.utils.maths.rotation.Rotation;
import io.smallrye.mutiny.tuples.Tuple3;

import static java.lang.Math.*;

public class SemiCircleToAnimation extends EndEffectorAnimation {

    private final double radius;
    private final double angleRad;
    
    private final Vector3D centerPoint;
    private final Vector3D u;
    private final Vector3D v;
    
    private final double timeSec;
    
    public SemiCircleToAnimation(boolean relative, Vector3D endPosition, double timeSec, 
                                 double angleDeg, Vector3D circleDirection) {
        super(relative);
        Preconditions.checkArgument(!circleDirection.isZero());
        // The {circleDirection} vector is the height of the isosceles triangle which's 
        // goes through the summit which is the center of the circle. This isosceles triangle has
        // its sides of equal lengths equal to the radius of the circle
        // This triangle's third side is the chord length of the remaining arc
        // https://www.omnicalculator.com/math/arc-length
        this.angleRad = toRadians(angleDeg);
        double arcAngle = PI/2.0 - this.angleRad;
        
        Vector3D chord = endPosition.subtract(Vector3D.Zero);
        double chordLength = chord.norm();
        
        double a = sin(arcAngle/2.0);
        double radius = chordLength / (2*a);
        
        // Half of the base of the triangle and its other side with the Pythagorean Theorem
        double height = sqrt(radius*radius - (chordLength*chordLength/4));
        // TODO REMOVE ONCE CHECK
        if (Double.isNaN(height)) { throw new IllegalStateException("Wrong sign under the root"); }
        
        this.centerPoint = circleDirection
                .normalized()
                .scaledBy(height)
                .add(chord.scaledBy(0.5));
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
                        .add( u.scaledBy(radius * cos(theta)) )
                        .add( v.scaledBy(radius * sin(theta)) ),
                Rotation.Identity,
                0.0
        );
    }
}
