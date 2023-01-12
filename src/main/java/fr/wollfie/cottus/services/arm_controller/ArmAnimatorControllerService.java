package fr.wollfie.cottus.services.arm_controller;

import fr.wollfie.cottus.dto.animation.ArmAnimation;
import fr.wollfie.cottus.dto.specification.ArmSpecification;

/** Handles animation of the arm, i.e., recording and replay of chained {@link ArmSpecification } */
public interface ArmAnimatorControllerService extends ArmControllerService {

    /** @return True if the animator is playing an animation, false otherwise */
    boolean isPlayingAnimation();

    /**
     * Plays an animation for the arm
     * @param animation The animation to play
     * @return True if the animation could be played, false if another animation was already playing
     */
    boolean playAnimation(ArmAnimation animation);
}
