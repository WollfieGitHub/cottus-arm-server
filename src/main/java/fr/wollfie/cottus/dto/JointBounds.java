package fr.wollfie.cottus.dto;

import com.fasterxml.jackson.annotation.JsonGetter;

public interface JointBounds {
    
    /** @return The lower bound for the joint angle in radians */
    @JsonGetter("min")
    double getLowerBound();

    /** @return The upper bound for the joint angle in radians */
    @JsonGetter("max")
    double getUpperBound();

    /** @return True if the given value {@code v} is in the bounds of the joint */
    boolean isInBounds(double v);
    
}
