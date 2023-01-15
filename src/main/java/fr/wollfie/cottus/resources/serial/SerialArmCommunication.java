package fr.wollfie.cottus.resources.serial;

import fr.wollfie.cottus.dto.Joint;
import fr.wollfie.cottus.models.arm.positioning.specification.AngleSpecification;
import fr.wollfie.cottus.resources.serial.msg.AnglesMessage;
import fr.wollfie.cottus.resources.serial.msg.SerialLogMessage;
import fr.wollfie.cottus.resources.serial.msg.SerialMessage;
import fr.wollfie.cottus.resources.serial.msg.MotorSpeedMessage;
import fr.wollfie.cottus.services.ArmCommunicationService;
import fr.wollfie.cottus.services.ArmStateService;
import io.quarkus.logging.Log;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Predicate;

@ApplicationScoped
public class SerialArmCommunication implements ArmCommunicationService {

    @Inject
    ArmStateService armStateService;
    
    private double motorRadPerSec = 0;
    @Override public double getMotorSpeed() { return this.motorRadPerSec; }
    

    private List<Double> angles = List.of(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    @Override public List<Double> getAngles() { return this.angles; }
    
    @Inject SerialCommunication serial;
    
    @Override
    public void updateArmState() {
        List<Double> jointAngles = armStateService.getArmState()
                .joints().stream()
                .filter(Predicate.not(Joint::isVirtual))
                .map(Joint::getAngleRad).toList();
        
        // Send the joint angles to the arduino
        serial.writeUnreliably(this.toEspData(jointAngles));
    }

    @Override
    public void onMsgReceived(SerialMessage message) {
        
        if (message instanceof AnglesMessage anglesMessage) { this.onAnglesReceived(anglesMessage); }
        else if (message instanceof MotorSpeedMessage motorSpeedMessage) { this.onMotorSpeedReceived(motorSpeedMessage); }
        else if (message instanceof SerialLogMessage espLog) { Log.infof("from Serial : %s", espLog.getMessage()); }
        else { throw new IllegalArgumentException("No message fitting this format"); }
    }
    
    private void onAnglesReceived(AnglesMessage distances) {
        this.angles = distances.getAngles();
        
        this.armStateService.moveDrivenGiven(new AngleSpecification(this.angles));
    }

    private void onMotorSpeedReceived(MotorSpeedMessage motorSpeed) {
        this.motorRadPerSec = motorSpeed.getRadPerSec();
        
        Log.infof("Motor speed received : %5.3f", motorRadPerSec);
        // When the arm sends the motor speed, it signals the arm is ready to take commands 
        this.armStateService.setReady(true);
    }
    
    /** @return The arm angles formatted as a string */
    private String toEspData(List<Double> armAngles) {
        return new AnglesMessage(armAngles).getMessage();
    }

    
}
