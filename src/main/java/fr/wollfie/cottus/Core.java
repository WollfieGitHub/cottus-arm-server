package fr.wollfie.cottus;

import com.fazecast.jSerialComm.SerialPort;
import fr.wollfie.cottus.resources.serial.SerialCommunication;
import fr.wollfie.cottus.resources.websockets.ArmStateSocket;
import fr.wollfie.cottus.services.ArmCommunicationService;
import fr.wollfie.cottus.services.ArmStateService;
import fr.wollfie.cottus.services.arm_controller.ArmAnimatorControllerService;
import fr.wollfie.cottus.services.arm_controller.ArmManualControllerService;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** For lack of a better name */
@Startup
@ApplicationScoped
public class Core {

    // Take a look at /resources/application.yml to see properties
    @ConfigProperty(name = "cottus.config.startup.connect-serial") boolean connectSerial; 
    @ConfigProperty(name = "cottus.config.startup.on-pi") boolean onPi;
    
    private static final long UPDATE_DELAY = 33;
    private ScheduledExecutorService timer;
    
    @Inject ArmManualControllerService armManualControllerService;
    @Inject ArmAnimatorControllerService armAnimatorControllerService;
    @Inject ArmStateSocket armStateSocket;
    
    @Inject SerialCommunication communication;
    @Inject ArmCommunicationService armCommunicationService;
    
    @Inject ArmStateService armStateService;
    
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

        if (this.connectSerial) {

            List<SerialPort> ports = communication.getAllPorts();
            StringBuilder sb = new StringBuilder();
            sb.append("\nUsing jSerialComm Library Version v")
                    .append(SerialPort.getVersion());
            sb.append("\nAvailable Ports:\n");
            for (int i = 0; i < ports.size(); ++i) {
                sb.append("   [").append(i).append("] ")
                        .append(ports.get(i).getSystemPortName())
                        .append(": ").append(ports.get(i).getDescriptivePortName())
                        .append(" - ").append(ports.get(i).getPortDescription())
                        .append("\n");
            }
            Log.info(sb.toString());
            if (!ports.isEmpty()) { communication.connectTo(ports.get(0)); }
            else { Log.warnf("No available serial ports to connect to..."); }
        }
        
        final boolean defaultReady = true;
        this.armStateService.setReady(defaultReady);
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
