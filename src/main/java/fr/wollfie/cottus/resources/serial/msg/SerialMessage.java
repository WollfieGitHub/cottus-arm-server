package fr.wollfie.cottus.resources.serial.msg;

import fr.wollfie.cottus.utils.Preconditions;

public abstract class SerialMessage {
    
    protected static final int HEADER_LENGTH = 2;
    protected final String header;

    public SerialMessage(String header) {
        Preconditions.checkArgument(this instanceof SerialLogMessage || header.length() == HEADER_LENGTH);
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
    
    public static SerialMessage getPacket(String message) {
        SerialMessage msg;
        
        boolean log = true;
        
        if (message.length() < HEADER_LENGTH) { 
            msg = new SerialLogMessage();
        } else {
            String header = message.substring(0, HEADER_LENGTH);
            
            msg = switch (header) {
                case AnglesMessage.HEADER -> new AnglesMessage();
                case MotorSpeedMessage.HEADER -> new MotorSpeedMessage();
                default -> new SerialLogMessage();
            };
            
            log = msg instanceof SerialLogMessage;
        }
        
        if (!log) { message = message.substring(HEADER_LENGTH); }
        
        msg.parse(message);
        return msg;
    }
    
    /** @return A new {@link SerialMessage.Builder} instance */
    public static Builder buildNew() { return new Builder(); }
    
    public static class Builder {
        private StringBuilder msg = new StringBuilder();
        private boolean complete;

        private Builder() { }
        
        /** Append the data to the packet, and update the state {@link Builder#complete} if the 
         * data contains a line break, which means the packet is complete */
        public void append(String data) {
            this.msg.append(data);
            this.complete = data.contains("\n");
        }
        
        /** @return If the packet is ready to be converted to a {@link SerialMessage} instance */
        public boolean isComplete() { return complete; }

        public SerialMessage toMsg() {
            if (!complete) { throw new IllegalStateException("The message isn't complete"); }
            
            return SerialMessage.getPacket(msg.toString());
        }
    }
}
