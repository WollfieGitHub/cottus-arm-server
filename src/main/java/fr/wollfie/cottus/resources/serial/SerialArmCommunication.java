package fr.wollfie.cottus.resources.serial;

import fr.wollfie.cottus.Core;
import fr.wollfie.cottus.dto.CottusArm;
import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.models.arm.positioning.specification.AngleSpecification;
import fr.wollfie.cottus.resources.serial.msg.AnglesMessage;
import fr.wollfie.cottus.resources.serial.msg.EspMessage;
import fr.wollfie.cottus.resources.serial.msg.MotorSpeedMessage;
import fr.wollfie.cottus.services.ArmCommunicationService;
import fr.wollfie.cottus.services.ArmManipulatorService;
import fr.wollfie.cottus.services.arm_controller.ArmManualControllerService;
import org.jboss.resteasy.reactive.common.NotImplementedYet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Predicate;

@ApplicationScoped
public class SerialArmCommunication implements ArmCommunicationService {

    @Inject ArmManipulatorService armManipulatorService;
    
    private double motorRadPerSec = 0;
    @Override public double getMotorSpeed() { return this.motorRadPerSec; }
    

    private List<Double> angles = List.of(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    @Override public List<Double> getAngles() { return this.angles; }
    
    @Inject SerialCommunication serialArduino;
    
    @Override
    public void updateArmState() {
        List<Double> jointAngles = armManipulatorService.getArmState()
                .joints().stream()
                .filter(Predicate.not(Joint::isVirtual))
                .map(Joint::getAngleRad).toList();
        
        // Send the joint angles to the arduino
        serialArduino.writeUnreliably(this.toEspData(jointAngles));
    }

    @Override
    public void onMsgReceived(String msg) {
        EspMessage message = EspMessage.getPacket(msg);
        
        if (message instanceof AnglesMessage anglesMessage) { this.onAnglesReceived(anglesMessage); }
        else if (message instanceof MotorSpeedMessage motorSpeedMessage) { this.onMotorSpeedReceived(motorSpeedMessage); }
        else { throw new IllegalArgumentException("No message is fitting the pattern of \"" + msg + "\""); }
    }
    
    private void onAnglesReceived(AnglesMessage distances) {
        this.angles = distances.getAngles();
        
        this.armManipulatorService.moveDrivenGiven(new AngleSpecification(this.angles));
    }

    private void onMotorSpeedReceived(MotorSpeedMessage motorSpeed) {
        this.motorRadPerSec = motorSpeed.getRadPerSec();
        // When the arm sends the motor speed, it signals the arm is ready to take commands 
        this.armManipulatorService.setReady();
    }
    
    /** @return The arm angles formatted as a string */
    private String toEspData(List<Double> armAngles) {
        return new AnglesMessage(armAngles).getMessage();
    }

    
}
