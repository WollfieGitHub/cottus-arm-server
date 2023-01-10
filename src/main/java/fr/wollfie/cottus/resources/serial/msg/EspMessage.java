package fr.wollfie.cottus.resources.serial.msg;

import fr.wollfie.cottus.utils.Preconditions;

public abstract class EspMessage {
    
    protected static final int HEADER_LENGTH = 2;
    protected final String header;

    public EspMessage(String header) {
        Preconditions.checkArgument(header.length() == HEADER_LENGTH);
        this.header = header;
    }
    
    protected abstract void parse(String message);

    /** @return The message as a string to send to the arduino */
    public String getMessage() {
        return this.getDataMsg() + "\n"; // Do not forge end of line marker to signal the arduino the bounds of the msg
    }
    
    protected abstract String getDataMsg();
    
    /** @return True if the message does not contain any data */
    public abstract boolean isEmpty();
    
    public static EspMessage getPacket(String message) {
        String header = message.substring(0, HEADER_LENGTH);
        
        EspMessage msg = switch (header) {
            case AnglesMessage.HEADER -> new AnglesMessage();
            case MotorSpeedMessage.HEADER -> new MotorSpeedMessage();
            default -> null;
        };
        if (msg == null) { return null; }
        
        msg.parse(message.substring(HEADER_LENGTH));
        return msg;
    }
}
