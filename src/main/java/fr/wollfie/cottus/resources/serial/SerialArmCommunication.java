package fr.wollfie.cottus.resources.serial;

import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.resources.serial.msg.AnglesMessage;
import fr.wollfie.cottus.resources.serial.msg.ArduinoMessage;
import fr.wollfie.cottus.resources.serial.msg.MotorSpeedMessage;
import fr.wollfie.cottus.services.ArmCommunicationService;
import org.jboss.resteasy.reactive.common.NotImplementedYet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Predicate;

@ApplicationScoped
public class SerialArmCommunication implements ArmCommunicationService {
    
    @Inject SerialCommunication serialArduino;
    
    private double motorRadPerSec = 0;
    
    @Override
    public void updateArmGiven(CottusArm armState) {
        List<Double> jointAngles = armState.joints().stream()
                .filter(Predicate.not(Joint::isVirtual))
                .map(Joint::getAngleRad).toList();
        
        // Send the joint angles to the arduino
        serialArduino.writeUnreliably(this.toArduinoData(jointAngles));
    }

    @Override
    public void onMsgReceived(String msg) {
        ArduinoMessage message = ArduinoMessage.getPacket(msg);
        
        if (message instanceof AnglesMessage anglesMessage) { this.onDistancesReceived(anglesMessage); }
        else if (message instanceof MotorSpeedMessage motorSpeedMessage) { this.onMotorSpeedReceived(motorSpeedMessage); }
        else { throw new IllegalArgumentException("No message is fitting the pattern of \"" + msg + "\""); }
    }

    private void onDistancesReceived(AnglesMessage distances) {
        // TODO
    }

    private void onMotorSpeedReceived(MotorSpeedMessage motorSpeed) {
        this.motorRadPerSec = motorSpeed.getRadPerSec();
    }

    @Override public double getMotorSpeed() { return this.motorRadPerSec; }

    /** @return The arm angles formatted as a string */
    private String toArduinoData(List<Double> armAngles) {
        return new AnglesMessage(armAngles).getMessage();
    }
    
    /** @return The arm angles difference relative to target position
     * converted from the formatted string */
    private List<Double> fromArduinoData(String diffAnglesRad) {
        throw new NotImplementedYet();
    }

    
}
