package fr.wollfie.cottus.resources.serial;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.services.PhysicalArmStateUpdaterService;
import org.jboss.resteasy.reactive.common.NotImplementedYet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Predicate;

@ApplicationScoped
public class SerialPhysicalArmStateUpdater implements PhysicalArmStateUpdaterService {
    
    @Inject SerialCommunication serialArduino;
    
    @Override
    public void updateArmGiven(CottusArm armState) {
        List<Double> jointAngles = armState.joints().stream()
                .filter(Predicate.not(Joint::isVirtual))
                .map(Joint::getAngleRad).toList();
        
        // Send the joint angles to the arduino
        serialArduino.writeUnreliably(this.toArduinoData(jointAngles));
    }

    @Override
    public void onDistanceReceived(String diffAnglesRad) {
        throw new NotImplementedYet();
    }

    // TODO
    /** @return The arm angles formatted as a string */
    private String toArduinoData(List<Double> armAngles) {
        return "TODO";
    }
    
    /** @return The arm angles difference relative to target position
     * converted from the formatted string */
    private List<Double> fromArduinoData(String diffAnglesRad) {
        throw new NotImplementedYet();
    }

    
}
