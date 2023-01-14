package fr.wollfie.cottus;

import com.fazecast.jSerialComm.SerialPort;
import fr.wollfie.cottus.resources.serial.SerialCommunication;
import fr.wollfie.cottus.resources.websockets.ArmStateSocket;
import fr.wollfie.cottus.services.ArmCommunicationService;
import fr.wollfie.cottus.services.ArmManipulatorService;
import fr.wollfie.cottus.services.arm_controller.ArmAnimatorControllerService;
import fr.wollfie.cottus.services.arm_controller.ArmManualControllerService;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.UnknownFormatConversionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** For lack of a better name */
@Startup
@ApplicationScoped
public class Core {

    private static final long UPDATE_DELAY = 33;
    private ScheduledExecutorService timer;
    
    @Inject ArmManualControllerService armManualControllerService;
    @Inject ArmAnimatorControllerService armAnimatorControllerService;
    @Inject ArmStateSocket armStateSocket;
    
    @Inject SerialCommunication communication;
    @Inject ArmCommunicationService armCommunicationService;
    
    @Inject ArmManipulatorService armManipulatorService;
    
// //======================================================================================\\
// ||                                                                                      ||
// ||                                       LIFECYCLE                                      ||
// ||                                                                                      ||
// \\======================================================================================//
    
    @PostConstruct
    void start() {
        this.armManualControllerService.setActive(true);
        this.timer = Executors.newSingleThreadScheduledExecutor(Executors.defaultThreadFactory());
        this.timer.scheduleAtFixedRate(this::update, 0, UPDATE_DELAY, TimeUnit.MILLISECONDS);
        Log.info("The update loop started...");

        List<SerialPort> ports = communication.getAllPorts();
        Log.infof("Available ports : %s", ports);
        if (!ports.isEmpty()) { communication.connectTo(ports.get(0)); }
        else { Log.warnf("No available serial ports to connect to..."); }
        
        final boolean defaultReady = true;
        this.armManipulatorService.setReady(defaultReady);
        Log.infof("Default ready state set to %s%s", defaultReady, 
                (!defaultReady 
                        ? "Waiting for arm to connect and finish its homing sequence..." 
                        : ", please" +
                " set it to false once the arduino connects correctly. Set it back to true" +
                " for development purposes."));
    }
    
    /** Update the application's state */
    private void update() {
        try {
            if (armAnimatorControllerService.isPlayingAnimation()) {
                this.armManualControllerService.setActive(false);
                this.armAnimatorControllerService.update();
            } else {
                this.armManualControllerService.setActive(true);
                this.armManualControllerService.update();
            }
            
            armStateSocket.broadCastArmState();
            armCommunicationService.updateArmState();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @PreDestroy
    void onDestroy() {
        if (this.timer != null && !this.timer.isShutdown()) {
            this.timer.shutdown();
            this.timer = null;
        }
        Log.info("Update loop has been shutdown.");
    }

}
