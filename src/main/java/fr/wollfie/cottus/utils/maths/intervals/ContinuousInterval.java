package fr.wollfie.cottus.utils.maths.intervals;

import fr.wollfie.cottus.utils.Preconditions;

/** A continuous interval */
public class ContinuousInterval implements Interval {

    public final double lowerBound;
    public final double upperBound;
    
    /** The interval of all real numbers */
    public static ContinuousInterval REAL = new ContinuousInterval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY) {
        @Override public boolean contains(double v) { return true; }
    };

    public ContinuousInterval(double lowerBound, double upperBound) {
        Preconditions.checkArgument(lowerBound < upperBound);

        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    /**
     * Creates a continuous interval between the specified lower bound and upper bound (included)
     * @param lowerBound The lower bound
     * @param upperBound The upper bound
     * @return A continuous interval between lower bound and upper bound (included)
     */
    public static ContinuousInterval from(double lowerBound, double upperBound) {
        return new ContinuousInterval(lowerBound, upperBound);
    }

    
    @Override
    public boolean contains(double v) {
        return lowerBound <= v && v <= upperBound;
    }

    @Override
    public String toString() { return String.format("[%5.3f, %5.3f]", lowerBound, upperBound); }
}
