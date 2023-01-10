package fr.wollfie.cottus.resources.serial.msg;

import fr.wollfie.cottus.utils.Preconditions;

import javax.ws.rs.HEAD;
import java.util.ArrayList;
import java.util.List;

/** Message = "AN[value]D[value]D[value]D...D[value]D" */
public class AnglesMessage extends ArduinoMessage {
    
    public static final String HEADER = "AN";
    private static final String END_VALUE_MARKER = "D";

    private List<Double> angles;
    public List<Double> getAngles() { return angles; }

    public AnglesMessage(List<Double> angles) {
        super(HEADER);
        this.angles = angles;
    }

    public AnglesMessage() { super(HEADER); }

    @Override
    protected void parse(String message) {
        // Ensure termination of while loop
        if (message.lastIndexOf(END_VALUE_MARKER) != message.length()-1) {
            throw new IllegalArgumentException("The message is ill formatted !"); 
        }
        angles = new ArrayList<>();
        
        String value;
        int index;
        do {
            index = message.indexOf(END_VALUE_MARKER);
            value = message.substring(0, index);
            angles.add(Double.parseDouble(value));
            // Remove the current value + the end marker
            message = message.substring(index+1);
            
        } while (index != message.length()-1);
    }

    @Override
    public String getMessage() {
        Preconditions.checkArgument(!this.isEmpty());
        
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(HEADER);

        for (Double angle : this.angles) {
            stringBuilder.append(angle).append(END_VALUE_MARKER);
        }
        return stringBuilder.toString();
    }

    @Override public boolean isEmpty() { return angles == null; }


}
