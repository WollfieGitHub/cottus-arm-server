package fr.wollfie.cottus.services.arm_controller;

import fr.wollfie.cottus.dto.specification.ArmSpecification;

/** Handles animation of the arm, i.e., recording and replay of chained {@link ArmSpecification } */
public interface ArmAnimatorControllerService extends ArmControllerService {

    /** @return True if the animator is playing an animation, false otherwise */
    boolean isPlayingAnimation();
}
