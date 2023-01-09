package fr.wollfie.cottus.services;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.resources.serial.SerialCommunication;

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

    /**
     * Called by {@link SerialCommunication} when the arduino sends the 
     * distances in radians that each stepper motors have relative to their
     * target rotation
     * @param angleDiffsRad The list of angle difference in radians relative to 
     *                      the stepper motors' target rotation
     */
    void onDistanceReceived(String angleDiffsRad);
}
