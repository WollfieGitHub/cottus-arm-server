package fr.wollfie.cottus.utils.maths.intervals;

import fr.wollfie.cottus.utils.maths.MathUtils;

import static java.lang.Math.abs;

/** An interval of real numbers */
@FunctionalInterface
public interface Interval {

    /** An empty interval */
    Interval EMPTY = v -> false;
    
    /** @return True if the real {@code v} is contained in this interval */
    boolean contains(double v);

    /** @return An interval consisting only of the specified value */
    static Interval unique(double value) {
        return v -> MathUtils.isZero(abs(value-v));
    }
    
    /** @return The interval "R \ {value}" */
    static Interval realExcept(double value) {
        return ContinuousInterval.REAL.minus(unique(value));
    }
    
    default Interval complement() {
        Interval interval = this;
        return v -> !interval.contains(v);
    }
    
    /** @return The interval which is the relative complement of {@param that} in {@code this} */
    default Interval minus(Interval that) {
        return that.and(this.complement());
    }
    
    /** @return The intersection between this interval and the other interval i2 */
    default Interval and(Interval i2) {
        Interval i1 = this;
        return v -> i1.contains(v) && i2.contains(v);
    }

    /** @return The union between this interval and the other interval i2 */
    default Interval or(Interval i2) {
        Interval i1 = this;
        return v -> i1.contains(v) || i2.contains(v);
    }
    
    /** @return Intersection of all the specified intervals */
    static Interval and(Interval... intervals) {
        Interval result = ContinuousInterval.REAL;
        for (Interval i : intervals) {
            result = result.and(i);
        }
        return result;
    }

    /** @return Union of all the specified intervals */
    static Interval or(Interval... intervals) {
        Interval result = ContinuousInterval.EMPTY;
        for (Interval i : intervals) {
            result = result.or(i);
        }
        return result;
    }
}
