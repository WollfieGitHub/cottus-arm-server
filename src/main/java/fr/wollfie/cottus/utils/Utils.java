package fr.wollfie.cottus.utils;

import static fr.wollfie.cottus.utils.Constants.EPSILON;

public class Utils {
    private Utils() {}
    
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
}
