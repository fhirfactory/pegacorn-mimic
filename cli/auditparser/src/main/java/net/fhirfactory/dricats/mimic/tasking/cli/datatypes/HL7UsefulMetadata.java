package net.fhirfactory.dricats.mimic.tasking.cli.datatypes;

import java.io.Serializable;

public class HL7UsefulMetadata extends HL7SimpleMetadata implements Serializable {
    private String subsystem;
    private String portNumber;
    private MessageFlowDirectionEnum flowDirection;

    //
    // Constructor(s)
    //

    public HL7UsefulMetadata(){
        super();
        this.subsystem = null;
        this.portNumber = null;
        this.flowDirection = null;
    }

    public HL7UsefulMetadata (String patientId, String messageTimestamp, String messageId, String subsystem){
        super(patientId, messageTimestamp, messageId);
        this.subsystem = subsystem;
    }

    public HL7UsefulMetadata (String patientId, String messageTimestamp, String messageId){
        super(patientId, messageTimestamp, messageId);
        this.subsystem = subsystem;
    }

    //
    // Getters and Setters
    //

    public String getSubsystem() {
        return subsystem;
    }

    public void setSubsystem(String subsystem) {
        this.subsystem = subsystem;
    }

    public String getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(String portNumber) {
        this.portNumber = portNumber;
    }

    public MessageFlowDirectionEnum getFlowDirection() {
        return flowDirection;
    }

    public void setFlowDirection(MessageFlowDirectionEnum flowDirection) {
        this.flowDirection = flowDirection;
    }

    //
    // toString()
    //


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("HL7UsefulMetadata{");
        sb.append("subsystem='").append(subsystem).append('\'');
        sb.append(", portNumber='").append(portNumber).append('\'');
        sb.append(", flowDirection=").append(flowDirection);
        sb.append(", patientId='").append(getPatientId()).append('\'');
        sb.append(", messageTimestamp='").append(getMessageTimestamp()).append('\'');
        sb.append(", messageId='").append(getMessageId()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
