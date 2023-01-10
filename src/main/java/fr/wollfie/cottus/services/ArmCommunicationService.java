package fr.wollfie.cottus.services;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.resources.serial.SerialCommunication;

import java.util.List;

/** 
 * The {@link ArmCommunicationService} is responsible for
 * actually moving the physical arm from the {@link fr.wollfie.cottus.dto.CottusArm} state
 * */
public interface ArmCommunicationService {

    /**
     * Update the joints of the physical arm given the virtual state
     * of the {@link CottusArm} object
     */
    void updateArmState();

    /**
     * Called by {@link SerialCommunication} when the arduino sends the 
     * distances in radians that each stepper motors have relative to their
     * target rotation
     * @param angleDiffsRad The list of angle difference in radians relative to 
     *                      the stepper motors' target rotation
     */
    void onMsgReceived(String angleDiffsRad);

    /**
     * @return The number of radians per seconds of the motors (For now they all have the same,
     * taking into account their reduction ratio and micro stepping)
     */
    double getMotorSpeed();
    
    /** @return The angles in radians of each motor */
    List<Double> getAngles();
}
