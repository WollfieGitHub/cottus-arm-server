package fr.wollfie.cottus.resources.serial.msg;

public class SerialLogMessage extends SerialMessage {

    private String logMsg;
    
    public SerialLogMessage() {
        super("");
    }

    @Override
    protected void parse(String message) {
        this.logMsg = message;
    }

    @Override
    protected String getDataMsg() {
        return this.logMsg;
    }

    @Override
    public boolean isEmpty() {
        return this.logMsg == null;
    }
}
