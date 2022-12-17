package fr.wollfie.cottus.services;

import fr.wollfie.cottus.dto.ArmSpecification;

/** Handles animation of the arm, i.e., recording and replay of chained {@link ArmSpecification } */
public interface ArmAnimatorService {

    /** Update the internal state of the animator */
    void update();
}
