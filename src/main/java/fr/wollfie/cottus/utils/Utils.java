package fr.wollfie.cottus.utils;

public class Utils {
    private Utils() {}

    /** Puts the thread to sleep for specified duration in milliseconds */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
}
