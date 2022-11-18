package net.fhirfactory.dricats.mimic.tasking.cli.datatypes;

import java.io.Serializable;
import java.util.Objects;

public class HL7SimpleMetadata implements Serializable {
    private String patientId;
    private String messageTimestamp;
    private String messageType;
    private String messageId;
    private String ackCode;
    private boolean isAck;

    //
    // Constructors
    //

    public HL7SimpleMetadata(){
        this.patientId = null;
        this.messageId = null;
        this.messageTimestamp = null;
        this.ackCode = null;
        this.messageType = null;
        this.isAck = false;
    }

    public HL7SimpleMetadata(String patientId, String messageTimestamp, String messageId){
        this.patientId = patientId;
        this.messageTimestamp = messageTimestamp;
        this.messageId = messageId;
        this.ackCode = null;
        this.messageType = null;
        this.isAck = false;
    }

    public HL7SimpleMetadata(String messageTimestamp, String messageId){
        this.patientId = null;
        this.messageTimestamp = messageTimestamp;
        this.messageId = messageId;
        this.ackCode = null;
        this.messageType = null;
        this.isAck = false;
    }

    //
    // Getters and Setters
    //

    public String getAckCode() {
        return ackCode;
    }

    public void setAckCode(String ackCode) {
        this.ackCode = ackCode;
    }

    public boolean isAck() {
        return isAck;
    }

    public void setAck(boolean ack) {
        isAck = ack;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getMessageTimestamp() {
        return messageTimestamp;
    }

    public void setMessageTimestamp(String messageTimestamp) {
        this.messageTimestamp = messageTimestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    //
    // hashcode and equals
    //

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HL7SimpleMetadata that = (HL7SimpleMetadata) o;
        return Objects.equals(getPatientId(), that.getPatientId()) && Objects.equals(getMessageTimestamp(), that.getMessageTimestamp()) && Objects.equals(getMessageId(), that.getMessageId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMessageTimestamp(), getMessageId());
    }

    //
    // toString
    //

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("HL7SimpleMetadata{");
        sb.append("patientId='").append(patientId).append('\'');
        sb.append(", messageTimestamp='").append(messageTimestamp).append('\'');
        sb.append(", messageId='").append(messageId).append('\'');
        sb.append(", ackCode='").append(ackCode).append('\'');
        sb.append(", isAck=").append(isAck);
        sb.append(", ack=").append(isAck());
        sb.append(", messageType=").append(messageType);
        sb.append('}');
        return sb.toString();
    }
}
