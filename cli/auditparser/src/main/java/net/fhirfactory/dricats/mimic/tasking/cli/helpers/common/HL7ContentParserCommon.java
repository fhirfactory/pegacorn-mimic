package net.fhirfactory.dricats.mimic.tasking.cli.helpers.common;

import ca.uhn.fhir.context.FhirContext;
import net.fhirfactory.dricats.mimic.tasking.cli.datatypes.HL7UsefulMetadata;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.AuditEvent;
import org.slf4j.Logger;

import java.util.List;

public abstract class HL7ContentParserCommon {
    private FhirContext fhirContext;

    //
    // Constructor(s)
    //
    public HL7ContentParserCommon(FhirContext fhirContext){
        this.fhirContext = fhirContext;
    }

    //
    // Getters and Setters
    //

    protected FhirContext getFHIRContext(){
        return(fhirContext);
    }

    abstract protected Logger getLogger();

    public boolean isHL7Message(String message){
        if(StringUtils.isEmpty(message)){
            return(false);
        }
        if(message.startsWith("MSH")){
            return(true);
        }
        return(false);
    }

    public HL7UsefulMetadata extractUsefulHL7Data(String hl7Message){
        getLogger().debug(".extractUsefulHL7Data(): [Extract HL7 Msg Metadata] Start");
        if(StringUtils.isEmpty(hl7Message)){
            getLogger().debug(".extractUsefulHL7Data(): [Extract HL7 Msg Metadata] Is Not HL7 Message");
        } else {
            String msgId = extractMessageId(hl7Message);
            getLogger().debug(".extractUsefulHL7Data(): [Extract HL7 Msg Metadata] msgId->{}", msgId);
            String msgTimestamp = extractMessageTimestamp(hl7Message);
            getLogger().debug(".extractUsefulHL7Data(): [Extract HL7 Msg Metadata] msgTimestamp->{}", msgTimestamp);
            String patientId = extractPatientId(hl7Message);
            getLogger().debug(".extractUsefulHL7Data(): [Extract HL7 Msg Metadata] patientId->{}", patientId);
            String msgType = extractMessageType(hl7Message);
            if(StringUtils.isEmpty(msgId) || StringUtils.isEmpty(msgTimestamp)){
                getLogger().debug(".extractUsefulHL7Data(): [Extract HL7 Msg Metadata] Either msgId or msgTimestamp are missing");
            } else {
                HL7UsefulMetadata metadata = null;
                if(StringUtils.isNotEmpty(patientId)) {
                    metadata = new HL7UsefulMetadata(patientId, msgTimestamp, msgId);
                } else {
                    metadata = new HL7UsefulMetadata(msgTimestamp, msgId);
                }
                if(StringUtils.isNotEmpty(msgType)){
                    metadata.setMessageType(msgType);
                }
                getLogger().debug(".extractUsefulHL7Data(): [Extract HL7 Msg Metadata] Finish");
                return(metadata);
            }
        }
        getLogger().debug(".extractUsefulHL7Data(): [Extract HL7 Msg Metadata] Finish (empty)");
        return(null);
    }

    public HL7UsefulMetadata extractUsefulHL7DataFromAck(String hl7Message){
        getLogger().debug(".extractUsefulHL7DataFromAck(): [Extract HL7 Msg Metadata] Start");
        if(StringUtils.isEmpty(hl7Message)){
            getLogger().trace(".extractUsefulHL7DataFromAck(): [Extract HL7 Msg Payload] Is Not HL7 Message");
        } else {
            String msgId = extractMessageIdFromAck(hl7Message);
            getLogger().trace(".extractUsefulHL7DataFromAck(): [Extract HL7 Msg Payload] msgId->{}", msgId);
            String msgTimestamp = extractMessageTimestamp(hl7Message);
            getLogger().trace(".extractUsefulHL7DataFromAck(): [Extract HL7 Msg Payload] msgTimestamp->{}", msgTimestamp);
            if(StringUtils.isEmpty(msgId) || StringUtils.isEmpty(msgTimestamp)){
                getLogger().trace(".extractUsefulHL7DataFromAck(): [Extract HL7 Msg Payload] Either msgId or msgTimestamp are missing");
            } else {
                HL7UsefulMetadata metadata = new HL7UsefulMetadata(msgTimestamp, msgId);
                getLogger().debug(".extractUsefulHL7DataFromAck(): [Extract HL7 Msg Metadata] Finish");
                return(metadata);
            }
        }
        getLogger().debug(".extractUsefulHL7DataFromAck(): [Extract HL7 Msg Metadata] Finish (empty)");
        return(null);
    }

    protected String[] splitMLLPMessageIntoSegments(String message){
        getLogger().trace(".splitMLLPMessageIntoSegments(): Entry, message->{}", message);
        String segmentList[] = message.split("\r");
        if(segmentList.length < 2){
            segmentList = message.split("\\r");
            if(segmentList.length < 2) {
                segmentList = message.split("\\\r");
                if(segmentList.length < 2) {
                    segmentList = message.split("\\\\r");
                    if(segmentList.length < 2) {
                        getLogger().debug(".splitMLLPMessageIntoSegments(): Could Not Split Message");
                        return (null);
                    }
                }
            }
        }
        return(segmentList);
    }

    public String extractMessageIdFromAck(String message){
        getLogger().debug(".extractMessageIdFromAck(): Entry, message->{}", message);
        if(StringUtils.isEmpty(message)){
            return(null);
        }
        String segmentList[] = splitMLLPMessageIntoSegments(message);
        if(segmentList == null){
            return(null);
        }
        String msa = segmentList[1];
        String fieldList[] = msa.split("\\|");
        if(fieldList.length < 3){
            return(null);
        }
        String msgId = fieldList[2];
        return(msgId);
    }

    public String extractAckCodeFromAck(String message){
        getLogger().debug(".extractAckCodeFromAck(): Entry, message->{}", message);
        if(StringUtils.isEmpty(message)){
            return(null);
        }
        String segmentList[] = splitMLLPMessageIntoSegments(message);
        if(segmentList == null){
            return(null);
        }
        String msa = segmentList[1];
        String fieldList[] = msa.split("\\|");
        if(fieldList.length < 3){
            return(null);
        }
        String ackCode = fieldList[1];
        return(ackCode);
    }

    public String extractMessageId(String message){
        getLogger().debug(".extractMessageId(): Entry, message->{}", message);
        if(StringUtils.isEmpty(message)){
            return(null);
        }
        String segmentList[] = splitMLLPMessageIntoSegments(message);
        if(segmentList == null){
            return(null);
        }
        String msh = segmentList[0];
        String fieldList[] = msh.split("\\|");
        if(fieldList.length < 10){
            return(null);
        }
        String msgId = fieldList[9];
        return(msgId);
    }

    public String extractMessageTimestamp(String message){
        getLogger().debug(".extractMessageTimestamp(): Entry, message->{}", message);
        if(StringUtils.isEmpty(message)){
            return(null);
        }
        String segmentList[] = splitMLLPMessageIntoSegments(message);
        if(segmentList == null){
            return(null);
        }
        String msh = segmentList[0];
        String fieldList[] = msh.split("\\|");
        if(fieldList.length < 10){
            return(null);
        }
        String msgTimestamp = fieldList[6];
        return(msgTimestamp);
    }

    public String extractMessageType(String message){
        getLogger().debug(".extractMessageType(): Entry, message->{}", message);
        if(StringUtils.isEmpty(message)){
            return(null);
        }
        String segmentList[] = splitMLLPMessageIntoSegments(message);
        if(segmentList == null){
            return(null);
        }
        String msh = segmentList[0];
        String fieldList[] = msh.split("\\|");
        if(fieldList.length < 10){
            return(null);
        }
        String msgTimestamp = fieldList[8];
        return(msgTimestamp);
    }

    public String extractPatientId(String message){
        if(StringUtils.isEmpty(message)){
            return(null);
        }
        String messageType = extractMessageType(message);
        if(messageType == null){
            getLogger().debug(".extractPatientId(): Could Not Extract Message Type");
            return(null);
        }
//        boolean isADTMessage = messageType.contains("ADT");
//        if(!isADTMessage){
//            getLogger().debug(".extractPatientId(): Is Not An ADT Message");
//            return(null);
//        }
        String pidSegment = extractPIDSegment(message);
        if(pidSegment == null){
            getLogger().debug(".extractPatientId(): Could Not Extract PID Segment");
            return(null);
        }
        String fieldList[] = pidSegment.split("\\|");
        if(fieldList.length < 3){
            if(fieldList.length == 3) {
                if (StringUtils.isNotEmpty(fieldList[2]))
                {
                    String identifier = fieldList[2];
                    return(identifier);
                }
            }
            return(null);
        }
        String identifierField = fieldList[3];
        if(StringUtils.isEmpty(identifierField)){
            getLogger().debug(".extractPatientId(): identifierField is empty");
            return(null);
        }
        String pidSegmentFields[] = identifierField.split("\\^");
        String patientIdentifier = pidSegmentFields[0];
        return(patientIdentifier);
    }

    public String extractPIDSegment(String message){
        getLogger().debug(".extractPIDSegment(): Entry, message->{}", message);
        String segmentList[] = splitMLLPMessageIntoSegments(message);
        if(segmentList == null){
            return(null);
        }
        for(int counter = 0; counter < segmentList.length; counter++){
            if(segmentList[counter].startsWith("PID")){
                return(segmentList[counter]);
            }
        }
        return(null);
    }

    public String extractSystem(AuditEvent currentAuditEvent){
        if(currentAuditEvent == null){
            return(null);
        }
        if(currentAuditEvent.hasAgent()){
            List<AuditEvent.AuditEventAgentComponent> agentList = currentAuditEvent.getAgent();
            if(agentList.size() > 0) {
                AuditEvent.AuditEventAgentComponent agent = agentList.get(0);
                if(agent.hasName()){
                    String name = agent.getName();
                    if(StringUtils.isNotEmpty(name)){
                        String nameElements[] = name.split("\\(");
                        String nameWithoutInstanceId = nameElements[0];
                        return(nameWithoutInstanceId);
                    }
                }
            }
        }
        return(null);
    }


}
