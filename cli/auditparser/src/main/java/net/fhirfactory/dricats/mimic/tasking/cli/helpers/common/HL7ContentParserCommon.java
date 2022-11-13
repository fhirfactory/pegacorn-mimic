package net.fhirfactory.dricats.mimic.tasking.cli.helpers.common;

import ca.uhn.fhir.context.FhirContext;
import net.fhirfactory.dricats.mimic.tasking.cli.datatypes.HL7UsefulMetadata;
import net.fhirfactory.dricats.mimic.tasking.cli.datatypes.MessageFlowDirectionEnum;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.AuditEvent;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

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

        if(StringUtils.isEmpty(hl7Message)){
            getLogger().info(".mapAuditEventArrayToMessageByPatientIdMap(): [Extract HL7 Msg Payload] Is Not HL7 Message");
        } else {
            String msgId = extractMessageId(hl7Message);
            getLogger().debug(".mapAuditEventArrayToMessageByPatientIdMap(): [Extract HL7 Msg Payload] msgId->{}", msgId);
            String msgTimestamp = extractMessageTimestamp(hl7Message);
            getLogger().debug(".mapAuditEventArrayToMessageByPatientIdMap(): [Extract HL7 Msg Payload] msgTimestamp->{}", msgTimestamp);
            String patientId = extractPatientId(hl7Message);
            getLogger().debug(".mapAuditEventArrayToMessageByPatientIdMap(): [Extract HL7 Msg Payload] patientId->{}", patientId);
            if(StringUtils.isEmpty(msgId) || StringUtils.isEmpty(msgTimestamp) || StringUtils.isEmpty(patientId)){
                getLogger().info(".mapAuditEventArrayToMessageByPatientIdMap(): [Extract HL7 Msg Payload] Either msgId, msgTimestamp or patientId are empty");
            } else {
                HL7UsefulMetadata metadata = new HL7UsefulMetadata(patientId, msgTimestamp, msgId);
                return(metadata);
            }
        }
        return(null);
    }

    public String extractMessageId(String message){
        if(StringUtils.isEmpty(message)){
            return(null);
        }
        String segmentList[] = message.split("\r");
        if(segmentList.length < 2){
            getLogger().debug(".extractMessageId(): Could Not Split Message");
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
        if(StringUtils.isEmpty(message)){
            return(null);
        }
        String segmentList[] = message.split("\r");
        if(segmentList.length < 2){
            getLogger().debug(".extractMessageId(): Could Not Split Message");
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
        if(StringUtils.isEmpty(message)){
            return(null);
        }
        String segmentList[] = message.split("\r");
        if(segmentList.length < 2){
            getLogger().debug(".extractMessageId(): Could Not Split Message");
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
        boolean isADTMessage = messageType.contains("ADT");
        if(!isADTMessage){
            getLogger().debug(".extractPatientId(): Is Not An ADT Message");
            return(null);
        }
        String pidSegment = extractPIDSegment(message);
        if(pidSegment == null){
            getLogger().debug(".extractPatientId(): Could Not Extract PID Segment");
            return(null);
        }
        String fieldList[] = pidSegment.split("\\|");
        if(fieldList.length < 3){
            getLogger().debug(".extractPatientId(): There is no Identifier Field");
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
        String segmentList[] = message.split("\r");
        if(segmentList.length < 2){
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

    public void writeAllHL7MessagesAsIndividualFileByPatientId(String outputFileNamePrefix, Map<HL7UsefulMetadata, String> msgMap){
        getLogger().info(".writeAllHL7Messages(): Entry");
        if(StringUtils.isEmpty(outputFileNamePrefix)){
            getLogger().info(".writeAllHL7Messages(): Exit, outputFileNamePrefix is empty");
            return;
        }
        try {
            getLogger().info(".writeAllHL7Messages(): [Create Output Directory (FileByPatientId)] Start");
            String directoryName = outputFileNamePrefix;
            File directory = new File(directoryName);
            String byMsgIdDirectoryName = "ByPatientId";
            File byMsgIdDirectory = new File(directoryName, byMsgIdDirectoryName);
            if(directory.exists()){
                getLogger().info(".writeAllHL7Messages(): [Create Output Directory (FileByPatientId)] Directory {} already exists", directory);
            } else {
                directory.mkdir();
            }
            if(byMsgIdDirectory.exists()){
                getLogger().info(".writeAllHL7Messages(): [Create Output Directory (FileByPatientId)] Directory {} already exists", byMsgIdDirectoryName);
            } else {
                byMsgIdDirectory.mkdir();
            }
            getLogger().info(".writeAllHL7Messages(): [Create Output Directory (FileByPatientId)] Finish");
            for(HL7UsefulMetadata  currentMsgId: msgMap.keySet()){
                getLogger().info(".writeAllHL7Messages(): [Create OutputStream] Start");
                StringBuilder fileNameBuilder = new StringBuilder();
                String subsystemName = currentMsgId.getSubsystem();
                if(StringUtils.isNotEmpty(subsystemName)){
                    fileNameBuilder.append(subsystemName).append("-");
                }
                if(currentMsgId.getFlowDirection() != null){
                    if(currentMsgId.getFlowDirection().equals(MessageFlowDirectionEnum.INGRES_TO_SUBSYSTEM)){
                        fileNameBuilder.append("Ingres").append("-");
                    } else {
                        fileNameBuilder.append("Egress").append("-");
                    }
                }
                String portNumber = currentMsgId.getPortNumber();
                if(StringUtils.isNotEmpty(portNumber)){
                    fileNameBuilder.append(portNumber).append("-");
                }
                fileNameBuilder.append(currentMsgId.getPatientId()).append("-");
                fileNameBuilder.append(currentMsgId.getMessageTimestamp()).append("-");
                fileNameBuilder.append(currentMsgId.getMessageId()).append(".hl7");
                String currentFileName = fileNameBuilder.toString();
                Path fileName = Paths.get(byMsgIdDirectory.getPath(), currentFileName);
                getLogger().info(".writeAllHL7Messages(): [Create OutputStream] Attempting to Create File->{}", fileName);
                FileOutputStream eventStream = new FileOutputStream(fileName.toFile());
                getLogger().info(".writeAllHL7Messages(): [Create OutputStream] Finish");
                getLogger().info(".writeAllHL7Messages(): [Write HL7 Message List] Entry");
                String currentMsg = msgMap.get(currentMsgId);
                eventStream.write(currentMsg.getBytes(StandardCharsets.UTF_8));
                eventStream.write("\n \n \n".getBytes(StandardCharsets.UTF_8));
                eventStream.close();
            }

            getLogger().info(".writeAllHL7Messages(): [Write HL7 Message List] Finish");
        } catch(Exception ex){
            getLogger().info(".writeAllHL7Messages(): Error...");
        }
    }
}
