package fr.wollfie.cottus.services;

import fr.wollfie.cottus.dto.ArmSpecification;

/** Handles animation of the arm, i.e., recording and replay of chained {@link ArmSpecification } */
public interface ArmAnimatorService {

    /** Update the internal state of the animator */
    void update();

    /** Resets the reference {@link ArmSpecification} object which the relative animations are relative too
     * or sets the animation mode to absolute if the given {@code relative} boolean is set to false */
    void resetReferenceFrame(boolean relative);
}
