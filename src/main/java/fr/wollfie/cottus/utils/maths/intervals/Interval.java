package fr.wollfie.cottus.utils.maths.intervals;

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
}
