package fr.wollfie.cottus.utils.maths.intervals;

import fr.wollfie.cottus.utils.Preconditions;

import static java.lang.Math.abs;

/** A continuous interval */
public abstract class ContinuousInterval extends Interval {

    /**
     * Creates a continuous interval between the specified lower bound and upper bound (included)
     * @param lowerBound The lower bound
     * @param upperBound The upper bound
     * @return A continuous interval between lower bound and upper bound (included)
     */
    public static ContinuousInterval from(double lowerBound, double upperBound) {
        Preconditions.checkArgument(lowerBound < upperBound);
        
        return new ContinuousInterval() {
            @Override public boolean contains(double v) { return lowerBound <= v && v <= upperBound; }

            @Override
            public double clamped(double v) {
                if (this.contains(v)) { return v; }
                
                double d1 = abs(lowerBound-v);
                double d2 = abs(upperBound-v);
                if (d1 < d2) { return lowerBound; }
                else { return upperBound; }
            }
        };
    }
    
}
