package fr.wollfie.utils.maths.interval;

import fr.wollfie.cottus.utils.maths.intervals.trigonometric.TrigonometricInterval;
import org.junit.jupiter.api.Test;

import static java.lang.Math.PI;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class TrigonometricIntervalTest {
    
    @Test void innerTrigonometricIntervalContainsReturnsTrueOnContainedValue() {
        TrigonometricInterval interval = TrigonometricInterval.with(-PI/4.0, PI/2.0);
        assertThat(interval.contains(-PI/8.0), is(true));
    }

    @Test void outerTrigonometricIntervalContainsReturnsTrueOnContainedValue() {
        TrigonometricInterval interval = TrigonometricInterval.with(PI/2.0, -PI/2.0);
        assertThat(interval.contains(-PI), is(true));
    }

    @Test void innerTrigonometricIntervalContainsReturnsFalseOnNotContainedValue() {
        TrigonometricInterval interval = TrigonometricInterval.with(-PI/4.0, PI/2.0);
        assertThat(interval.contains(-PI), is(false));
    }

    @Test void outerTrigonometricIntervalContainsReturnsFalseOnNotContainedValue() {
        TrigonometricInterval interval = TrigonometricInterval.with(PI/2.0, -PI/2.0);
        assertThat(interval.contains(0), is(false));
    }
}
