package fr.wollfie.cottus.utils.maths.intervals;

import fr.wollfie.cottus.utils.Preconditions;

import java.util.List;

import static java.lang.Math.*;

/** A continuous interval */
public class ConvexInterval extends UnionOfIntervals {

    public final double lowerBound;
    public double getLowerBound() { return lowerBound; }

    public final double upperBound;
    public double getUpperBound() { return upperBound; }

    /** The interval of all real numbers */
    public static ConvexInterval REAL = new ConvexInterval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY) {
        @Override public boolean contains(double v) { return true; }
    };

    /** The empty interval (The empty set is trivially convex) */
    public static ConvexInterval EMPTY = new ConvexInterval(Double.NaN, Double.NaN) {
        @Override public boolean contains(double v) { return true; }
    };

    public ConvexInterval(double lowerBound, double upperBound) {
        super();
        Preconditions.checkArgument(lowerBound <= upperBound || (Double.isNaN(lowerBound) && Double.isNaN(upperBound)));

        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    /**
     * Creates a continuous interval between the specified lower bound and upper bound (included)
     * @param lowerBound The lower bound
     * @param upperBound The upper bound
     * @return A continuous interval between lower bound and upper bound (included)
     */
    public static ConvexInterval from(double lowerBound, double upperBound) {
        return new ConvexInterval(lowerBound, upperBound);
    }
    
    @Override
    public boolean contains(double v) {
        return lowerBound <= v && v <= upperBound;
    }

    @Override
    public String toString() { return String.format("[%5.3f, %5.3f]", lowerBound, upperBound); }

    /** @return True if {@code that} is a subset of {@code this}, false otherwise */
    public boolean isSubsetOf(ConvexInterval that) {
        return that.lowerBound <= this.lowerBound && that.upperBound >= this.upperBound;
    }
    
    @Override
    public UnionOfIntervals complement() {
        if (isRealSet()) { return EMPTY; }
        
        return new UnionOfIntervals(List.of(
                ConvexInterval.from(Double.NEGATIVE_INFINITY, lowerBound),
                ConvexInterval.from(upperBound, Double.POSITIVE_INFINITY)
        ));
    }
    
    /** @return True if the interval is the set of all real numbers, false otherwise */
    public boolean isRealSet() {
        return Double.isInfinite(this.lowerBound) && Double.isInfinite(this.upperBound)
                && this.lowerBound < 0 && this.upperBound > 0;
    }

    public UnionOfIntervals minus(ConvexInterval that) {
        if (this.isEmpty()) { return EMPTY; }
        if (that.isEmpty()) { return this; }
        
        if (that.isSubsetOf(this)) {
            return new UnionOfIntervals(List.of(
                    ConvexInterval.from(this.lowerBound, that.lowerBound),
                    ConvexInterval.from(that.upperBound, this.upperBound)
            ));
        } else if (this.isSubsetOf(that)) {
            return ConvexInterval.EMPTY;
        // <that> interval is partly on the left of <this> interval
        } else if (that.lowerBound < this.lowerBound && that.upperBound > this.lowerBound) {
            return ConvexInterval.from(that.upperBound, this.upperBound);
        // <that> interval is partly on the right of <this> interval
        } else if (that.upperBound > this.upperBound && that.lowerBound < this.lowerBound) {
            return ConvexInterval.from(this.lowerBound, that.lowerBound);
        // <that> interval has no elements in <this> interval
        } else { return this; }
    }
    
    public ConvexInterval inter(ConvexInterval that) {
        if (this.isEmpty() || that.isEmpty()) { return EMPTY; }
        
        // The intervals don't overlap
        if (that.upperBound < this.lowerBound || that.lowerBound > this.upperBound) { return EMPTY; }

        return ConvexInterval.from(max(this.lowerBound, that.lowerBound), min(this.upperBound, that.upperBound));
    }

    public UnionOfIntervals union(ConvexInterval that) {
        if (this.isEmpty()) { return that; }
        if (that.isEmpty()) { return this; }
        
        if (this.isSubsetOf(that)) { return that; }
        else if (that.isSubsetOf(this)) { return this; }
        
        // The interval is on the left
        if (that.upperBound < this.lowerBound) { 
            return new UnionOfIntervals(that, this);
        // The interval is on the right
        } else if (this.lowerBound < that.lowerBound) {
            return new UnionOfIntervals(this, that);
        // The intervals overlap
        } else {
            return ConvexInterval.from(min(this.lowerBound, that.lowerBound), max(this.upperBound, that.upperBound));
        }
    }
    
    /** @return True if the interval is the empty set, false otherwise */
    public boolean isEmpty() {
        return Double.isNaN(this.lowerBound) || Double.isNaN(this.upperBound);
    }
}
