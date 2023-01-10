package fr.wollfie.cottus.resources.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import fr.wollfie.cottus.services.ArmCommunicationService;

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
    
    @Inject
    ArmCommunicationService armCommunicationService;
    
    /** @return All the serial ports available for this device */
    public List<SerialPort> getAllPorts() {
        return Arrays.asList(this.ports);
    }

    /**
     * Start the connection with the specified Serial Port
     * @param port The serial port to connect to
     */
    public void startOn(SerialPort port) {
        activePort = SerialPort.getCommPort(port.getPortDescription());

        if (activePort.openPort())
            System.out.println(activePort.getPortDescription() + " port opened.");
        System.out.println(activePort.getPortDescription() + " Connected");

        activePort.addDataListener(new SerialPortDataListener() {

            @Override
            public void serialEvent(SerialPortEvent event) {
                int size = event.getSerialPort().bytesAvailable();
                
                byte[] buffer = new byte[size];
                event.getSerialPort().readBytes(buffer, size);
                
                String msg = new String(buffer);
                
                armCommunicationService.onMsgReceived(msg);
            }

            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }
        });
    }

    /**
     * Write data to the arduino. This must be redundant data as it may be dropped 
     * if a packet was just sent 
     * @param data The data to send to the Arduino
     */
    public void writeUnreliably(String data) {
        long nowMs = System.currentTimeMillis();
        if (nowMs - lastPacketMs > PACKET_FREQUENCY_MS) {
            byte[] toWrite = data.getBytes(StandardCharsets.UTF_8);
            activePort.writeBytes(toWrite, toWrite.length);
            lastPacketMs = nowMs;
        }
    }
}
