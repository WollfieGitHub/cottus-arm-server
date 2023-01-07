package fr.wollfie.cottus.utils.maths;

import fr.wollfie.cottus.utils.Constants;
import fr.wollfie.cottus.utils.Preconditions;

import java.util.List;
import java.util.function.Function;

import static fr.wollfie.cottus.utils.Constants.EPSILON;
import static java.lang.Math.pow;

public class MathUtils {
    private MathUtils() {}
    
    /** @return True if the double {@code v} is smaller than {@link Constants#EPSILON} */
    public static boolean isZero(double v) {
        return Math.abs(v) < EPSILON;
    }

    /** @return The binomial coefficient for parameters (k, n)
     * @implNote <a href="https://rosettacode.org/wiki/Evaluate_binomial_coefficients#Java">Source</a> */
    public static double binomial(int k, int n) {
        if (k>n-k) { k=n-k; }

        long b=1;
        for (int i=1, m=n; i<=k; i++, m--) { b=b*m/i; }
        return b;
    }

    /**
     * Normalize a real angle in the interval ]-pi, pi]
     * @param v The angle to normalize
     * @return The normalized angle
     */
    public static double normalizeAngle(double v) {
        double b = Math.PI * 2;
        return ((((v + Math.PI) % b) + b) % b) - Math.PI;
    }

    /**
     * Creates a Bézier curve with the specified list of anchor points.
     * The first anchor points will be used as the starting point and the last as the ending point
     * @param points The anchor points of the curve
     * @return A Bézier curve parametrized from 0 to 1 
     * @implNote <a href="https://mathcurve.com/courbes3d.gb/bezier3d/bezier3d.shtml">Source</a>
     */
    public static Function<Double, Vector3D> bezierCurve(List<Vector3D> points) {
        return t -> {
            Preconditions.checkInInterval(0, 1, t);
            int n = points.size();
            Vector3D result = Vector3D.Zero;

            for (int k = 0; k < n; k++) {
                double bernsteinCoefficient = binomial(k, n) * pow(t, k) * pow(1 - t, n - k);
                result = result.plus(points.get(k).scaled(bernsteinCoefficient));
            }
            
            return result;
        };
    }
}
