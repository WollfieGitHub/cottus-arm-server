package fr.wollfie.cottus.utils.maths.intervals.trigonometric;

import fr.wollfie.cottus.utils.maths.MathUtils;
import fr.wollfie.cottus.utils.maths.intervals.ConvexInterval;

import static java.lang.Math.*;

/** A trigonometric interval  */
public abstract class TrigonometricInterval extends ConvexInterval {
    
    /** Lower bound of the trigonometric interval */
    protected final double lowerBound;
    /** Upper bound of the trigonometric interval */ 
    protected final double upperBound;

    private TrigonometricInterval(double lowerBound, double upperBound) {
        super(min(lowerBound, upperBound), max(lowerBound, upperBound));
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override public boolean contains(double v) { return containsNormalized(MathUtils.normalizeAngle(v)); }

    protected abstract boolean containsNormalized(double normalizeAngle);

    /**
     * Creates a trigonometric interval from the specified lower and upper bounds
     * @param lowerBoundRad The real lower bound
     * @param upperBoundRad The real upper bound
     * @return A new trigonometric interval
     */
    public static TrigonometricInterval with(double lowerBoundRad, double upperBoundRad) {
        double l2 = MathUtils.normalizeAngle(lowerBoundRad);
        double u2 = MathUtils.normalizeAngle(upperBoundRad);

        if (l2 <= u2) { return new Inner(l2, u2); }
        else { return new Outer(l2, u2); }
    }

    /**
     * Creates a trigonometric interval from the specified lower and upper bounds in degrees
     * @param lowerBoundDeg The real lower bound in degree
     * @param upperBoundDeg The real upper bound in degree
     * @return A new trigonometric interval
     */
    public static TrigonometricInterval withDeg(double lowerBoundDeg, double upperBoundDeg) {
        return TrigonometricInterval.with(Math.toRadians(lowerBoundDeg), Math.toRadians(upperBoundDeg));
    }
    
    private static String intervalToString(double v1, double v2) {
        return String.format("[%.2fpi, %.2fpi]", v1/PI, v2/PI);
    }

    /** Outer trigonometric interval : The interval is of the form : ]-pi, upper, lower, pi] */
    private static class Outer extends TrigonometricInterval {

        private Outer(double lowerBound, double upperBound) { super(lowerBound, upperBound); }

        @Override public boolean containsNormalized(double v) { 
            return -PI <= v && v <= upperBound
                    || lowerBound <= v && v <= PI; 
        }

        @Override
        public String toString() {
            return TrigonometricInterval.intervalToString(-PI, upperBound)
                    + " U "
                    + TrigonometricInterval.intervalToString(lowerBound, PI);
        }
    }
    
    /** Inner trigonometric interval : The interval is of the form : ]-pi, lower, upper, pi] */
    private static class Inner extends TrigonometricInterval {

        private Inner(double lowerBound, double upperBound) { super(lowerBound, upperBound); }

        @Override
        public boolean containsNormalized(double v) {
            return lowerBound <= v && v <= upperBound;
        }

        @Override
        public String toString() {
            return TrigonometricInterval.intervalToString(lowerBound, upperBound);
        }
    }
    
    
}
