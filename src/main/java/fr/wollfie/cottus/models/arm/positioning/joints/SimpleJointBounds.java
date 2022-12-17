package fr.wollfie.cottus.models.arm.positioning.joints;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.wollfie.cottus.dto.JointBounds;

public record SimpleJointBounds(
        @JsonIgnore double minRad,
        @JsonIgnore double maxRad
) implements JointBounds {

    /**
     * Create new bounds from values in degrees in interval [-180, 180]
     * @param minDeg Min angle in degrees that the joint should reach
     * @param maxDeg Max angle in degrees that the joint should reach
     * @return The bounds for the joint
     */
    public static SimpleJointBounds fromDeg(double minDeg, double maxDeg) {
        return new SimpleJointBounds(Math.toRadians(minDeg), Math.toRadians(maxDeg));
    }

    @Override public double getLowerBound() { return minRad; }
    @Override public double getUpperBound() { return maxRad; }
    @Override public boolean isInBounds(double v) { 
        double v2 = normalized(v);
        return minRad <= v2 && v2 <= maxRad; 
    }
    
    /** Normalize the angle between [-pi, pi] */
    private double normalized(double v) {
        double b = Math.PI*2;
        return  (v % b + b) % b - Math.PI;
    }
}
