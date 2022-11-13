package net.fhirfactory.dricats.mimic.tasking.cli.datatypes;

import java.io.Serializable;
import java.util.Objects;

public class HL7SimpleMetadata implements Serializable {
    private String patientId;
    private String messageTimestamp;
    private String messageId;

    //
    // Constructors
    //

    public HL7SimpleMetadata(){
        this.patientId = null;
        this.messageId = null;
        this.messageTimestamp = null;
    }

    public HL7SimpleMetadata(String patientId, String messageTimestamp, String messageId){
        this.patientId = patientId;
        this.messageTimestamp = messageTimestamp;
        this.messageId = messageId;
    }

    //
    // Getters and Setters
    //

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
        return Objects.hash(getPatientId(), getMessageTimestamp(), getMessageId());
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
        sb.append('}');
        return sb.toString();
    }
}
