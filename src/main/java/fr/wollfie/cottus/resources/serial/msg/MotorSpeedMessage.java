package fr.wollfie.cottus.resources.serial.msg;

import org.jboss.resteasy.reactive.common.NotImplementedYet;

/** Message = "SP[value]" */
public class MotorSpeedMessage extends EspMessage {
    
    public static final String HEADER = "SP";

    private double radPerSec = -1;
    public double getRadPerSec() { return radPerSec; }

    public MotorSpeedMessage() { super("SP"); }

    @Override
    protected void parse(String message) {
        this.radPerSec = Math.toRadians(Double.parseDouble(message));
    }

    // For now, no need to send speed to the arduino
    @Override public String getDataMsg() { throw new NotImplementedYet(); }

    @Override public boolean isEmpty() { return radPerSec < 0; }
}
