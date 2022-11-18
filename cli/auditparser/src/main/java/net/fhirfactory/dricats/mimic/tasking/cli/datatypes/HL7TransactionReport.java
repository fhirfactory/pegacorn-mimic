package net.fhirfactory.dricats.mimic.tasking.cli.datatypes;

import java.util.Objects;

public class HL7TransactionReport {
    private HL7UsefulMetadata ingresMetadata;
    private HL7UsefulMetadata egressMetadata;
    private boolean successfulEgressActivity;


    //
    // Constructor(s)
    //

    public HL7TransactionReport(HL7UsefulMetadata ingresMetadata){
        this.ingresMetadata = ingresMetadata;
        this.egressMetadata = null;
        this.successfulEgressActivity = false;
    }

    //
    // Getters and Setters
    //

    public HL7UsefulMetadata getIngresMetadata() {
        return ingresMetadata;
    }

    public void setIngresMetadata(HL7UsefulMetadata ingresMetadata) {
        this.ingresMetadata = ingresMetadata;
    }

    public HL7UsefulMetadata getEgressMetadata() {
        return egressMetadata;
    }

    public void setEgressMetadata(HL7UsefulMetadata egressMetadata) {
        this.egressMetadata = egressMetadata;
    }

    public boolean isSuccessfulEgressActivity() {
        return successfulEgressActivity;
    }

    public void setSuccessfulEgressActivity(boolean successfulEgressActivity) {
        this.successfulEgressActivity = successfulEgressActivity;
    }

    //
    // toString
    //

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("HL7TransactionReport{");
        sb.append("ingresMetadata=").append(ingresMetadata);
        sb.append(", egressMetadata=").append(egressMetadata);
        sb.append(", successfulEgressActivity=").append(successfulEgressActivity);
        sb.append('}');
        return sb.toString();
    }

    //
    // hash and equals
    //


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HL7TransactionReport that = (HL7TransactionReport) o;
        return isSuccessfulEgressActivity() == that.isSuccessfulEgressActivity() && Objects.equals(getIngresMetadata(), that.getIngresMetadata()) && Objects.equals(getEgressMetadata(), that.getEgressMetadata());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIngresMetadata());
    }
}
