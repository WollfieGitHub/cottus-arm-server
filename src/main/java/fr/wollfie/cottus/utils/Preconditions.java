package fr.wollfie.cottus.utils;

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
}
