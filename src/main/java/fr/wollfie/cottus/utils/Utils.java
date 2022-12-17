package fr.wollfie.cottus.utils;

import static fr.wollfie.cottus.utils.Constants.EPSILON;

public class Utils {
    private Utils() {}
    
    public static boolean isZero(double v) {
        return Math.abs(v) < EPSILON;
    }
}
