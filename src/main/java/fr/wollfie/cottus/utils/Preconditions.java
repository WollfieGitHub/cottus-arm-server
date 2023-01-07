package fr.wollfie.cottus.utils;

import fr.wollfie.cottus.utils.maths.intervals.ContinuousInterval;

public class Preconditions {
    private Preconditions() {}

    /**
     * Check that the given argument is true and throws an {@link  IllegalArgumentException} otherwise
     * @param shouldBeTrue An argument that should be true
     */
    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) { throw new IllegalArgumentException(); }
    }

    /**
     * Check that the given argument is not null and throws an {@link  IllegalArgumentException} otherwise
     * @param notNullObj An argument that should be not null
     */
    public static void checkNotNull(Object notNullObj) {
        if (notNullObj == null) { throw new IllegalArgumentException(); }
    }

    /**
     * Check that the argument is in the interval [lower, upper] or throws an {@link IllegalArgumentException}
     * otherwise
     * @param lower Lower bound
     * @param upper Upper bound
     * @param value Actual Value
     */
    public static void checkInInterval(double lower, double upper, double value) {
        if (!ContinuousInterval.from(lower, upper).contains(value)) { throw new IllegalArgumentException(); }
    }
}
