package fr.wollfie.cottus.services;

import fr.wollfie.cottus.dto.CottusArm;

/** 
 * The {@link PhysicalArmStateUpdaterService} is responsible for
 * actually moving the physical arm from the {@link fr.wollfie.cottus.dto.CottusArm} state
 * */
public interface PhysicalArmStateUpdaterService {

    /**
     * Update the joints of the physical arm given the virtual state
     * of the {@link CottusArm} object
     * @param armState The state of the arm
     */
    void updateArmGiven(CottusArm armState);
}
