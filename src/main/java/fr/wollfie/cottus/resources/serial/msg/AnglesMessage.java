package fr.wollfie.cottus.resources.serial.msg;

import fr.wollfie.cottus.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

/** Message = "AN[value]D[value]D[value]D...D[value]D" */
public class AnglesMessage extends SerialMessage {
    
    public static final String HEADER = "AN";
    private static final String END_VALUE_MARKER = "D";

    private List<Double> anglesRad;
    public List<Double> getAngles() { return anglesRad; }

    public AnglesMessage(List<Double> anglesRad) {
        super(HEADER);
        this.anglesRad = anglesRad;
    }

    public AnglesMessage() { super(HEADER); }

    @Override
    protected void parse(String message) {
        // Ensure termination of while loop
        if (message.lastIndexOf(END_VALUE_MARKER) != message.length()-1) {
            throw new IllegalArgumentException("The message is ill formatted !"); 
        }
        anglesRad = new ArrayList<>();
        
        String value;
        int index;
        do {
            index = message.indexOf(END_VALUE_MARKER);
            value = message.substring(0, index);
            anglesRad.add(Math.toRadians(Double.parseDouble(value)));
            // Remove the current value + the end marker
            message = message.substring(index+1);
            
        } while (index != message.length()-1);
    }

    @Override
    public String getDataMsg() {
        Preconditions.checkArgument(!this.isEmpty());
        
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(HEADER);

        for (Double angle : this.anglesRad) {
            stringBuilder.append(Math.toDegrees(angle)).append(END_VALUE_MARKER);
        }
        return stringBuilder.toString();
    }

    @Override public boolean isEmpty() { return angles == null; }


}
