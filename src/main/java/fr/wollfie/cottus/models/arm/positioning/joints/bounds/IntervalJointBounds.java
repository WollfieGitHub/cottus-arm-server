package fr.wollfie.cottus.models.arm.positioning.joints.bounds;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.wollfie.cottus.dto.JointBounds;
import fr.wollfie.cottus.utils.maths.intervals.Interval;

/** Joint's bounds based around an interval object  */
public class IntervalJointBounds implements JointBounds {
    
    /** These bounds do not allow any rotation for the joint */
    public static IntervalJointBounds EMPTY = IntervalJointBounds.of(Interval.EMPTY);
    /** Any angle is allowed by this type of bounds */
    public static IntervalJointBounds ANY = IntervalJointBounds.of(Interval.REAL);
    
    @JsonIgnore private final Interval interval;
    private IntervalJointBounds(Interval interval) { this.interval = interval; }

    public static IntervalJointBounds of(Interval interval) { return new IntervalJointBounds(interval); }
    
    // TODO EDIT BACK
    @Override public boolean isOutOfBounds(double v) { return false; /*return !this.interval.contains(v);*/ }
    @Override public double clamped(double v) {
        double result = this.interval.clamped(v);
        return Double.isNaN(result) ? v : result;
    }

    @Override
    public String toString() {
        return "IntervalJointBounds{" + interval + '}';
    }
}
