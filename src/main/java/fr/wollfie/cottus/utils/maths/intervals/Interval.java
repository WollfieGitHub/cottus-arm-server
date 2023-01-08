package fr.wollfie.cottus.utils.maths.intervals;

import fr.wollfie.cottus.utils.maths.MathUtils;

import static java.lang.Math.abs;

/** An interval of real numbers */
@FunctionalInterface
public interface Interval {

    /** @return True if the real {@code v} is contained in this interval */
    boolean contains(double v);

    /** @return An interval consisting only of the specified value */
    static Interval unique(double value) {
        return v -> MathUtils.isZero(abs(value-v));
    }
    
    /** @return The interval "R \ {value}" */
    static Interval realExcept(double value) {
        return ConvexInterval.REAL.minus(unique(value));
    }
    
    default Interval complement() {
        Interval interval = this;
        return v -> !interval.contains(v);
    }
    
    /** @return The interval which is the relative complement of {@param that} in {@code this} to the set of Real numbers */
    default Interval minus(Interval that) {
        return that.inter(this.complement());
    }
    
    /** @return The intersection between this interval and the other interval i2 */
    default Interval inter(Interval i2) {
        Interval i1 = this;
        return v -> i1.contains(v) && i2.contains(v);
    }

    /** @return The union between this interval and the other interval i2 */
    default Interval union(Interval i2) {
        Interval i1 = this;
        return v -> i1.contains(v) || i2.contains(v);
    }
    
    /** @return Intersection of all the specified intervals */
    static Interval inter(Interval... intervals) {
        Interval result = ConvexInterval.REAL;
        for (Interval i : intervals) {
            result = result.inter(i);
        }
        return result;
    }

    /** @return Union of all the specified intervals */
    static Interval union(Interval... intervals) {
        Interval result = ConvexInterval.EMPTY;
        for (Interval i : intervals) {
            result = result.union(i);
        }
        return result;
    }
}
