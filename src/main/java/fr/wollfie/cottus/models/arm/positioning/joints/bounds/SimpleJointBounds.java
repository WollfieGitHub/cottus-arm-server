package fr.wollfie.cottus.models.arm.positioning.joints.bounds;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.wollfie.cottus.dto.JointBounds;
import io.quarkus.logging.Log;

public final class SimpleJointBounds implements JointBounds {
    @JsonIgnore private final double minRad;
    @JsonIgnore private final double maxRad;
    @JsonIgnore private final boolean innerInterval;

    public SimpleJointBounds(
            double minRad,
            double maxRad
    ) {
        this.minRad = normalized(minRad);
        this.maxRad = normalized(maxRad);
        // The valid values should be in the interval between 
        // min and max if the normalized min is less than the normalized max
        this.innerInterval = this.minRad < this.maxRad;
    }

    /**
     * Create new bounds from values in degrees in interval [-180, 180]
     *
     * @param minDeg Min angle in degrees that the joint should reach
     * @param maxDeg Max angle in degrees that the joint should reach
     * @return The bounds for the joint
     */
    public static SimpleJointBounds fromDeg(double minDeg, double maxDeg) {
        return new SimpleJointBounds(
                normalized(Math.toRadians(minDeg)),
                normalized(Math.toRadians(maxDeg))
        );
    }

    @Override
    public boolean isOutOfBounds(double v) {
        double v2 = normalized(v);
        Log.info(v2);
        return (innerInterval && v2 >= minRad && v2 <= maxRad)
                || (!innerInterval  && (v2 >= minRad || v2 <= maxRad));
    }

    @Override
    public double clamp(double v) {
        double v2 = normalized(v);
        
        if (innerInterval && v2 >= minRad && v2 <= maxRad) { return v2; }   /* ]-pi, min, v2, max, pi] */
        if (!innerInterval  && (v2 >= minRad || v2 <= maxRad)) { return v2; } /* ]-pi, max, min, v2, pi] or ]-pi, v2, max, min, pi] */
        
        
        if (innerInterval && v2 <= minRad) { return minRad + 0.00001; }     /* ]-pi, v2, min, max, pi] */
        if (innerInterval && v2 >= maxRad) { return maxRad - 0.00001; }     /* ]-pi, min, max, v2, pi] */
        if (!innerInterval && (v2 <= minRad && v2 >= maxRad)) { return minRad + 0.00001; } /* ]-pi, max, v2, min, pi] */
        return v2;
    }

    /** Normalize the angle between [-pi, pi] */
    private static double normalized(double v) {
        double b = Math.PI * 2;
        return ((((v + Math.PI) % b) + b) % b) - Math.PI;
    }

}
