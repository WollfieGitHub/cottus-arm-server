package fr.wollfie.cottus.exception;

/**
 * Used when tried to set an angle for a joint which is out of its bounds.
 * Not handling this exception correctly might damage the arm
 */
public class AngleOutOfBoundsException extends Exception { }
