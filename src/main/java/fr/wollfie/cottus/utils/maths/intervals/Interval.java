package fr.wollfie.cottus.utils.maths.intervals;

import fr.wollfie.cottus.utils.maths.MathUtils;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapter;

import static java.lang.Math.abs;

/** An interval of real numbers */
public abstract class Interval {
    
    /** @return True if the real {@code v} is contained in this interval */
    public abstract boolean contains(double v);
    
    /** The interval of all real numbers */
    public static Interval REAL = new Interval() {
        @Override public boolean contains(double v) { return true; }
    };
    
    /** An empty interval */
    public static Interval EMPTY = new Interval() {
        @Override public boolean contains(double v) { return false; }
    };
    
    /** @return An interval consisting only of the specified value */
    public static Interval unique(double value) {
        return new Interval() {
            @Override public boolean contains(double v) { return MathUtils.isZero(abs(value-v)); }
        };
    }
    
    /** @return The interval "R \ {value}" */
    public static Interval realExcept(double value) {
        return REAL.minus(unique(value));
    }
    
    public Interval complement() {
        Interval interval = this;
        return new Interval() {
            @Override public boolean contains(double v) { return !interval.contains(v); }
        };
    }
    
    /** @return The interval which is the relative complement of {@param that} in {@code this} */
    public Interval minus(Interval that) {
        return that.and(this.complement());
    }
    
    /** @return The intersection between this interval and the other interval i2 */
    public Interval and(Interval i2) {
        Interval i1 = this;
        return new Interval() {
            @Override public boolean contains(double v) { return i1.contains(v) && i2.contains(v); }
        };
    }

    /** @return The union between this interval and the other interval i2 */
    public Interval or(Interval i2) {
        Interval i1 = this;
        return new Interval() {
            @Override public boolean contains(double v) { return i1.contains(v) || i2.contains(v); }
        };
    }
    
    /** @return Intersection of all the specified intervals */
    public static Interval and(Interval... intervals) {
        Interval result = REAL;
        for (Interval i : intervals) {
            result = result.and(i);
        }
        return result;
    }

    /** @return Union of all the specified intervals */
    public static Interval or(Interval... intervals) {
        Interval result = EMPTY;
        for (Interval i : intervals) {
            result = result.or(i);
        }
        return result;
    }
}
