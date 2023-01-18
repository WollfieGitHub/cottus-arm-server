package fr.wollfie.cottus.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Bounds for the joint, sending an angle which {@link JointBounds#isOutOfBounds(double)} might damage
 * the arm.
 */
public interface JointBounds {
    
    /** @return True if the given value {@code v} is in the bounds of the joint */
    @JsonIgnore boolean isOutOfBounds(double v);
    
    /** @return The value if it is within the bounds of the joint, or the min value if
     * it is less than the minimum bounds, or the maximum bounds value otherwise */
    @JsonIgnore double clamped(double v);
    
    /** @return The lower bound of the joint's bounds */
    double getLowerBound();
    
    /** @return The upper bound of the joint's bounds */
    double getUpperBound();
}
