package fr.wollfie.cottus.dto;

import fr.wollfie.cottus.models.animation.pathing.LineToAnimation;
import fr.wollfie.cottus.utils.maths.Vector3D;
import org.jboss.resteasy.reactive.common.NotImplementedYet;

public interface ArmAnimation {

    /**
     * Given a time elapsed from when the animation started, returns a specification
     * for the arm's state
     * @param secFromStart The second elapsed from when the animation started playing
     * @return A configuration of all angles of the arm or arm
     * if the animation stopped
     */
    ArmSpecification evaluateAt(double secFromStart);

    /** @return The duration in seconds of the animation */
    double getDurationSecs();
    
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                                       ANIMATION COMPOSITION                          ||
// ||                                                                                      ||
// \\======================================================================================//

    /**
     * A way to compose animations :
     * <pre>{@code
     *     ArmAnimation myIncredibleAnimation = ...
     *     ArmAnimation myBeautifulAnimation = ...
     *     ArmAnimation myIncrediblyBeautifulAnimation
     *      = myIncredibleAnimation.followedBy(myBeautifulAnimation);
     * }</pre>
     * @param second The animation to chain to {@code this} animation
     * @return An animation that will play {@code second} animation when {@code this} is over
     */
    default ArmAnimation followedBy(ArmAnimation second) {
        ArmAnimation first = this;
        return new ArmAnimation() {
            @Override
            public ArmSpecification evaluateAt(double secFromStart) {
                return this.getDurationSecs() >= secFromStart
                        ? first.evaluateAt(secFromStart)
                        : second.evaluateAt(first.getDurationSecs()+secFromStart);
            }

            @Override
            public double getDurationSecs() {
                return first.getDurationSecs()+second.getDurationSecs();
            }
        };
    }

    /**
     * Saves the animation to ...
     */
    default void save() {
        // TODO
    }
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                                       TODO DEFAULT PATH                              ||
// ||                                                                                      ||
// \\======================================================================================//

    /**
     * Interpolate as a line between the current position and the given position in 3D space,
     * within the given time frame
     * @param position The end position in 3D space
     * @param timeSec The time to take between the position at the beginning of the animation
     *                and the end
     * @param relative True if the position should be taken relative to the current position
     *                 of the end effector, or false if it should refer to global 3D space
     * @return The lineTo animation
     */
    static ArmAnimation lineTo(Vector3D position, double timeSec, boolean relative) {
        return new LineToAnimation(position, timeSec, relative);
    }

    /**
     * Interpolate as a Bézier curve between the current position and the given position in 3D space,
     * with the given point as anchors for the Bézier curve within the given time frame
     * @param endPosition The end position in 3D space
     * @param timeSec The time to take between the position at the beginning of the animation
     *                and the end
     * @param anchorPoints The anchor points for the curve
     * @return The lineTo animation
     */
    static ArmAnimation bezierTo(Vector3D endPosition, double timeSec, Vector3D... anchorPoints) {
        throw new NotImplementedYet();
    }

    /**
     * <p>Interpolate as a semicircle between the current position and the given position in 3D space,
     * within the given time frame in seconds.
     * <br><br>The center of the point will be chosen such that the begin and end position are the begin and
     * end of the {@code angleDeg} rotation of the circle. For example, the midpoint between the beginning 
     * position and end position will be designated as the center of the circle if the parameter {@code angleDeg} is set to pi/2.
     * <br><br>The tangent at the beginning point of the circle will point towards the specified {@code circleDirection}
     *   parameter. For example, if the direction is up, the circle's normal will be pointing towards the right or down</p>
     * @param endPosition The end position in 3D space
     * @param timeSec The time to take between the position at the beginning of the animation
     *                and the end
     * @return The lineTo animation
     */
    static ArmAnimation semiCircleTo(Vector3D endPosition, double timeSec, double angleDeg, Vector3D circleDirection) {
        throw new NotImplementedYet();
    }

    /**
     * The arm will wait at its current location during the time frame specified in seconds
     * @param timeSec The time for which the arm will wait
     * @return The wait animation
     */
    static ArmAnimation waitDuring(double timeSec) {
        throw new NotImplementedYet();
    }
}
