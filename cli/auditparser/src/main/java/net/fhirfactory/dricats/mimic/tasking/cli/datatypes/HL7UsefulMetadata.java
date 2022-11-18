package net.fhirfactory.dricats.mimic.tasking.cli.datatypes;

import java.io.Serializable;
import java.util.Objects;

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
        this.portNumber = null;
        this.flowDirection = null;
    }

    public HL7UsefulMetadata (String patientId, String messageTimestamp, String messageId){
        super(patientId, messageTimestamp, messageId);
        this.subsystem = subsystem;
        this.portNumber = null;
        this.flowDirection = null;
    }

    public HL7UsefulMetadata (String messageTimestamp, String messageId){
        super(messageTimestamp, messageId);
        this.subsystem = subsystem;
        this.portNumber = null;
        this.flowDirection = null;
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
        sb.append("subsystem=").append(subsystem);
        sb.append(", portNumber=").append(portNumber);
        sb.append(", flowDirection=").append(flowDirection);
        sb.append(", ackCode=").append(getAckCode());
        sb.append(", ack=").append(isAck());
        sb.append(", patientId=").append(getPatientId());
        sb.append(", messageTimestamp=").append(getMessageTimestamp());
        sb.append(", messageId=").append(getMessageId());
        sb.append(", messageType=").append(getMessageType());
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        HL7UsefulMetadata that = (HL7UsefulMetadata) o;
        return Objects.equals(getSubsystem(), that.getSubsystem()) && Objects.equals(getPortNumber(), that.getPortNumber()) && getFlowDirection() == that.getFlowDirection();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getPortNumber());
    }
}
