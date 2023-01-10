package fr.wollfie.cottus.resources.serial.msg;

import fr.wollfie.cottus.utils.Preconditions;

public abstract class ArduinoMessage {
    
    protected static final int HEADER_LENGTH = 2;
    protected final String header;

    public ArduinoMessage(String header) {
        Preconditions.checkArgument(header.length() == HEADER_LENGTH);
        this.header = header;
    }
    
    protected abstract void parse(String message);
    
    /** @return The message as a string to send to the arduino */
    public abstract String getMessage();
    
    /** @return True if the message does not contain any data */
    public abstract boolean isEmpty();
    
    public static ArduinoMessage getPacket(String message) {
        String header = message.substring(0, HEADER_LENGTH);
        
        ArduinoMessage msg = switch (header) {
            case AnglesMessage.HEADER -> new AnglesMessage();
            case MotorSpeedMessage.HEADER -> new MotorSpeedMessage();
            default -> null;
        };
        if (msg == null) { return null; }
        
        msg.parse(message.substring(HEADER_LENGTH));
        return msg;
    }
}
