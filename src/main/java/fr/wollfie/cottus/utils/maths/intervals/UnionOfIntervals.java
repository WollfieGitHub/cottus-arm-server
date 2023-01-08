package fr.wollfie.cottus.utils.maths.intervals;

import io.quarkus.logging.Log;
import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UnionOfIntervals implements Interval {
    
    private List<ConvexInterval> intervals;

    public List<ConvexInterval> getIntervals() { return new ArrayList<>(intervals); }
    // TODO ENSURE THAT NO INTERVALS OVERLAP => FLATTEN
    
    /** Builds a new interval, union of all the convex interval given */
    public UnionOfIntervals(List<ConvexInterval> intervals) { this.intervals = sorted(intervals); }

    /** Builds a new interval, union of all the convex interval given */
    public UnionOfIntervals(ConvexInterval i1, ConvexInterval... intervals) { this(appended(i1, intervals)); }
    
    /** Help for the second constructor(i1, i...) */
    private static List<ConvexInterval> appended(ConvexInterval i1, ConvexInterval... intervals) {
        List<ConvexInterval> intervalList = new ArrayList<>(List.of(intervals));
        intervalList.add(i1);
        return intervalList;
    }
    
    /** Used by the {@link ConvexInterval} constructor */
    protected UnionOfIntervals() {
        if (this instanceof ConvexInterval ConvexInterval) { this.intervals = sorted(List.of(ConvexInterval)); }
    }

    /**
     * Compute the intersection ⋂<sub>i</sub>A<sub>i</sub>
     * @param intervals The intervals in the intersection
     * @return The itnersection of all given intervals
     */
    public static UnionOfIntervals inter(UnionOfIntervals... intervals) {
        UnionOfIntervals result = ConvexInterval.REAL;
        for(UnionOfIntervals interval : intervals) { result = result.inter(interval); }
        return result;
    }

    private List<ConvexInterval> sorted(List<ConvexInterval> intervals) {
        List<ConvexInterval> modifiableResult = new ArrayList<>(intervals);
        modifiableResult.sort(Comparator.comparing(ConvexInterval::getLowerBound));
        return modifiableResult;
    }

    @Override public boolean contains(double v) { return intervals.stream().anyMatch(i -> i.contains(v)); }

    @Override
    public UnionOfIntervals complement() {
        // Use DeMorgan's law
        List<UnionOfIntervals> complements = this.intervals.stream().map(ConvexInterval::complement).toList();
        UnionOfIntervals result = ConvexInterval.REAL;
        for (UnionOfIntervals complement : complements) { result = result.inter(complement); }
        return result;
    }

    /** @return The result of {@code this} ⋂ {@code that} */
    public UnionOfIntervals minus(UnionOfIntervals that) { return this.inter( that.complement() ); }

    /** @return The result of {@code this} inter {@code that} */
    public UnionOfIntervals inter(UnionOfIntervals that) {
        // Complexity : O(n^2)
        if (this instanceof ConvexInterval && !(that instanceof ConvexInterval)) {
            return that.inter( this ); 
        } else if (this instanceof ConvexInterval c1) {
            ConvexInterval c2 = (ConvexInterval) that;
            return c1.inter(c2);
        }
        
        UnionOfIntervals result = ConvexInterval.REAL;
        for(ConvexInterval interval : this.intervals) {
            // Distributive property
            result = result.union( interval.inter(that) );
        }
        return result;
    }

    /** @return The result of {@code this} U {@code that} */
    public UnionOfIntervals union(UnionOfIntervals that) {
        // Complexity : O(n^2)
        if (this instanceof ConvexInterval && !(that instanceof ConvexInterval)) {
            return that.union( this );
        } else if (this instanceof ConvexInterval c1) {
            ConvexInterval c2 = (ConvexInterval) that;
            return c1.union(c2);
        }

        UnionOfIntervals result = ConvexInterval.REAL;
        for(ConvexInterval interval : this.intervals) {
            // Distributive property
            result = result.union( interval.union(that) );
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < intervals.size(); i++) {
            stringBuilder.append(intervals.get(i));
            if (i < intervals.size()-1) { stringBuilder.append("U"); }
        }
        return stringBuilder.toString();
    }
}
