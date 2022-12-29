package fr.wollfie.cottus.utils.maths.intervals.trigonometric;

import fr.wollfie.cottus.utils.Utils;
import fr.wollfie.cottus.utils.maths.intervals.ContinuousInterval;

import static java.lang.Math.*;

/** A trigonometric interval  */
public abstract class TrigonometricInterval extends ContinuousInterval {
    
    /** Lower bound of the trigonometric interval */
    protected final double lowerBound;
    /** Upper bound of the trigonometric interval */ 
    protected final double upperBound;

    public TrigonometricInterval(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override public boolean contains(double v) { return containsNormalized(Utils.normalizeAngle(v)); }
    @Override public double clamped(double v) { return clampedNormalized(Utils.normalizeAngle(v)); }

    protected abstract boolean containsNormalized(double normalizeAngle);
    protected abstract double clampedNormalized(double normalizeAngle);

    /**
     * Creates a trigonometric interval from the specified lower and upper bounds
     * @param lowerBound The real lower bound
     * @param upperBound The real upper bound
     * @return A new trigonometric interval
     */
    public static TrigonometricInterval with(double lowerBound, double upperBound) {
        double l2 = Utils.normalizeAngle(lowerBound);
        double u2 = Utils.normalizeAngle(upperBound);
        if (l2 <= u2) { return new Inner(lowerBound, upperBound); }
        else { return new Outer(lowerBound, upperBound); }
    }

    /** Outer trigonometric interval : The interval is of the form : ]-pi, upper, lower, pi] */
    private static class Outer extends TrigonometricInterval {

        private Outer(double lowerBound, double upperBound) { super(lowerBound, upperBound); }

        @Override public boolean containsNormalized(double v) { 
            return -PI <= v && v <= upperBound
                    || lowerBound <= v && v <= PI; 
        }
        
        @Override public double clampedNormalized(double v) { 
            if (this.containsNormalized(v)) { return v; }

            double distanceToHi = v - upperBound;
            double distanceToLo = lowerBound - v;
            
            if (distanceToLo < distanceToHi) { return lowerBound; }
            else { return upperBound; }
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
        public double clampedNormalized(double v) {
            if (this.contains(v)) { return v; }

            double distanceToHi, distanceToLo;

            if (v <= lowerBound) {
                distanceToHi = (v + 2 * PI) - upperBound;
                distanceToLo = lowerBound - v;
            } else {
                distanceToLo = lowerBound - (v - 2 * PI);
                distanceToHi = v - upperBound;
            }

            if (distanceToLo < distanceToHi) { return lowerBound; } 
            else { return upperBound; }
        }
    }
}
