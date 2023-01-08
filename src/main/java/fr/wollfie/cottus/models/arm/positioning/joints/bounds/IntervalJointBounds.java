package fr.wollfie.cottus.models.arm.positioning.joints.bounds;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.wollfie.cottus.dto.JointBounds;
import fr.wollfie.cottus.utils.maths.MathUtils;
import fr.wollfie.cottus.utils.maths.intervals.ConvexInterval;

import static java.lang.Math.*;

/** Joint's bounds based around an interval object  */
public class IntervalJointBounds implements JointBounds {
    
    /** These bounds do not allow any rotation for the joint */
    public static IntervalJointBounds EMPTY = IntervalJointBounds.of(null);
    /** Any angle is allowed by this type of bounds */
    public static IntervalJointBounds ANY = IntervalJointBounds.of(ConvexInterval.REAL);
    
    @JsonIgnore private final ConvexInterval interval;
    private IntervalJointBounds(ConvexInterval interval) { this.interval = interval; }

    public static IntervalJointBounds of(ConvexInterval interval) { return new IntervalJointBounds(interval); }
    
    // TODO EDIT BACK
    @Override public boolean isOutOfBounds(double v) { return false; /*return this.interval == null ? true : !this.interval.contains(v);*/ }
    @Override public double clamped(double v) {
        if (this.interval == null) { return Double.NaN; }
        
        double result = MathUtils.clamped(this.interval.lowerBound, this.interval.upperBound, v);
        return Double.isNaN(result) ? v : result;
    }

    @Override
    public double getLowerBound() { return this.clamped(-PI); }
    @Override
    public double getUpperBound() { return this.clamped(PI); }

    @Override
    public String toString() {
        return "IntervalJointBounds{" + interval + '}';
    }
}
