package fr.wollfie.cottus.services;

import fr.wollfie.cottus.dto.ArmSpecification;

/** Handles animation of the arm, i.e., recording and replay of chained {@link ArmSpecification } */
public interface ArmAnimatorService extends ArmControllerService {

    /** @return True if the animator is playing an animation, false otherwise */
    boolean isPlayingAnimation();
}
