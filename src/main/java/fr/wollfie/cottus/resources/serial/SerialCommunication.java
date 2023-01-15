package fr.wollfie.cottus.resources.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import fr.wollfie.cottus.resources.serial.msg.SerialMessage;
import fr.wollfie.cottus.services.ArmCommunicationService;
import io.quarkus.logging.Log;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/** 
 * Serial Connection with the Arduino
 * */
@ApplicationScoped
public class SerialCommunication {

    private static long lastPacketMs = System.currentTimeMillis();
    private static final long PACKET_FREQUENCY_MS = 50;
    SerialPort activePort;
    SerialPort[] ports = SerialPort.getCommPorts();
    
    private SerialMessage.Builder messageBuilder = SerialMessage.buildNew();
    
    @Inject ArmCommunicationService armCommunicationService;
    private static final int BAUD_RATE = 9600;

    /** @return All the serial ports available for this device */
    public List<SerialPort> getAllPorts() {
        return Arrays.asList(this.ports);
    }

    private void tryReconnect() {
        Log.info("Disconnected !");
    }
    
    /**
     * Start the connection with the specified Serial Port
     * @param port The serial port to connect to
     */
    public void connectTo(SerialPort port) {
        this.activePort = port;
        
        if (activePort.isOpen()) {
            Log.errorf("Failed to connect to port %s because it is already opened by another device!",
                    activePort.getPortDescription());
            return;
        } else if (!activePort.openPort()) {
            Log.errorf("Failed to connect to port %s for unknown reasons...",
                    activePort.getPortDescription());
            return;
        } else {
            Log.infof("%s port opened.", activePort.getPortDescription());

        }

        SerialCommunication communication = this;
        activePort.setBaudRate(BAUD_RATE);
        
        activePort.addDataListener(new SerialPortDataListener() {

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                    
                    byte[] buffer = new byte[activePort.bytesAvailable()];
                    activePort.readBytes(buffer, activePort.bytesAvailable());
                    String msg = new String(buffer);

                    messageBuilder.append(msg);
                    
                    if (messageBuilder.isComplete()) {
                        armCommunicationService.onMsgReceived(communication.messageBuilder.toMsg());
                        messageBuilder = SerialMessage.buildNew();
                    }
                } else if (
                        event.getEventType() == SerialPort.LISTENING_EVENT_PORT_DISCONNECTED
                        || event.getEventType() == SerialPort.LISTENING_EVENT_TIMED_OUT
                ) { communication.tryReconnect(); }
            }

            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE
                        | SerialPort.LISTENING_EVENT_PORT_DISCONNECTED
                        | SerialPort.LISTENING_EVENT_TIMED_OUT;
            }
        });
    }
    
    @PreDestroy
    void cleanup() {
        if (this.activePort != null && this.activePort.isOpen()) {
            if (!this.activePort.closePort()) {
                Log.errorf("Failed to close %s on cleanup", activePort);
            }
        }
    }
    
    /**
     * Write data to the arduino. This must be redundant data as it may be dropped 
     * if a packet was just sent 
     * @param data The data to send to the Arduino
     */
    public void writeUnreliably(String data) {
        long nowMs = System.currentTimeMillis();
        if (nowMs - lastPacketMs > PACKET_FREQUENCY_MS && this.activePort != null) {
            byte[] toWrite = data.getBytes(StandardCharsets.UTF_8);
            activePort.writeBytes(toWrite, toWrite.length);
            lastPacketMs = nowMs;
        }
    }
}
