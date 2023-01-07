package fr.wollfie.cottus.utils.maths.intervals;

import fr.wollfie.cottus.utils.maths.MathUtils;

import static java.lang.Math.abs;

/** An interval of real numbers */
public abstract class Interval {
    
    /** @return True if the real {@code v} is contained in this interval */
    public abstract boolean contains(double v);
    
    /** @return The closest integer from {@code v} which is in the interval
     * or {@link Double#NaN} if the real number cannot be clamped because the interval is empty */
    public abstract double clamped(double v);
    
    /** The interval of all real numbers */
    public static Interval REAL = new Interval() {
        @Override public boolean contains(double v) { return true; }
        @Override public double clamped(double v) { return v; }
    };
    
    /** An empty interval */
    public static Interval EMPTY = new Interval() {
        @Override public boolean contains(double v) { return false; }
        @Override public double clamped(double v) { return Double.NaN; }
    };
    
    /** @return An interval consisting only of the specified value */
    public static Interval unique(double value) {
        return new Interval() {
            @Override public boolean contains(double v) { return MathUtils.isZero(abs(value-v)); }
            @Override public double clamped(double v) { return v; }
        };
    }
    
    /** @return The intersection between this interval and the other interval i2 */
    public Interval and(Interval i2) {
        Interval i1 = this;
        return new Interval() {
            @Override public boolean contains(double v) { return i1.contains(v) && i2.contains(v); }
            @Override public double clamped(double v) { 
                double v1  = i1.clamped(v);
                double v2 = i2.clamped(v);
                
                // TODO DOUBLE CHECK THIS
                if (!i2.contains(v1) &&  i1.contains(v2)) { return v2; }
                if ( i2.contains(v1) && !i1.contains(v2)) { return v1; }

                double d1 = abs(v1-v);
                double d2 = abs(v2-v);
                
                if ( i2.contains(v1) && i1.contains(v2)) {
                    if (d1 < d2) { return v1; }
                    else { return v2; }
                }
                // The interval is the empty set
                return Double.NaN;
            }
        };
    }

    /** @return The union between this interval and the other interval i2 */
    public Interval or(Interval i2) {
        Interval i1 = this;
        return new Interval() {
            @Override public boolean contains(double v) { return i1.contains(v) || i2.contains(v); }
            @Override public double clamped(double v) {
                double v1  = i1.clamped(v);
                double v2 = i2.clamped(v);

                double d1 = abs(v1-v);
                double d2 = abs(v2-v);

                // Just return whichever bound is closest
                if (d1 < d2) { return v1; }
                else { return v2; }
            }
        };
    }
    
    /** @return Intersection of all the specified intervals */
    public static Interval and(Interval... intervals) {
        Interval result = REAL;
        for (Interval i : intervals) {
            result = result.and(i);
        }
        return result;
    }

    /** @return Union of all the specified intervals */
    public static Interval or(Interval... intervals) {
        Interval result = EMPTY;
        for (Interval i : intervals) {
            result = result.or(i);
        }
        return result;
    }
}
