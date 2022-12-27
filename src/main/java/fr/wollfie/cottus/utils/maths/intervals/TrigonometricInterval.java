package fr.wollfie.cottus.utils.maths.intervals;

/** A trigonometric interval  */
public class TrigonometricInterval extends Interval {
    
    
    
    @Override
    public boolean contains(double v) {
        return false;
    }

    @Override
    public double clamped(double v) {
        return 0;
    }
}
