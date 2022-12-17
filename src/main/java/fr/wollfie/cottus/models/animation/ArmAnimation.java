package fr.wollfie.cottus.models.animation;

import fr.wollfie.cottus.dto.ArmSpecification;

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
    
    /** @return The number of seconds since the animation has started playing */
    double getSecondsElapsedFromStart();
    
    /** @return True if the animation is currently playing */
    boolean isPlaying();
    
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
     *      = myIncredibleAnimation.andThenPlay(myBeautifulAnimation);
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

            @Override
            public double getSecondsElapsedFromStart() {
                return first.getSecondsElapsedFromStart() + second.getSecondsElapsedFromStart();
            }

            @Override
            public boolean isPlaying() {
                return first.isPlaying() || second.isPlaying();
            }
        };
    }
}
