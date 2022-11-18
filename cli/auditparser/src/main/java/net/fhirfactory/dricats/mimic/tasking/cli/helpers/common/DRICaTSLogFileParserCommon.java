package net.fhirfactory.dricats.mimic.tasking.cli.helpers.common;

import ca.uhn.fhir.context.FhirContext;
import net.fhirfactory.dricats.mimic.tasking.cli.datatypes.HL7TransactionReport;
import net.fhirfactory.dricats.mimic.tasking.cli.datatypes.HL7UsefulMetadata;
import net.fhirfactory.dricats.mimic.tasking.cli.datatypes.MessageFlowDirectionEnum;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Scanner;
import java.util.Set;

public abstract class DRICaTSLogFileParserCommon extends HL7ContentParserCommon {

    //
    // Constructor(s)
    //
    public DRICaTSLogFileParserCommon(FhirContext fhirContext){
        super(fhirContext);
    }

    //
    // Getters and Setters
    //

    //
    // Business Methods
    //

    public String scanForSubsystemName(String logFileName){
        getLogger().info(".scanForSubsystemName(): Entry");
        String name = null;
        try {
            getLogger().info(".scanForSubsystemName(): [Create InputStream] Start");
            getLogger().info(".scanForSubsystemName(): [Create InputStream] Attempting to Access File->{}", logFileName);
            File initialFile = new File(logFileName);
            getLogger().info(".scanForSubsystemName(): [Create InputStream] Created File Handle...");
            InputStream eventStream = new FileInputStream(initialFile);
            getLogger().info(".scanForSubsystemName(): [Create InputStream] Finish");
            getLogger().info(".scanForSubsystemName(): [Scan For Name] Start");
            name = parseForSystemName(eventStream);
            getLogger().info(".scanForSubsystemName(): [Scan For Name] Finish");
            eventStream.close();
        } catch (Exception ex){
            getLogger().info(".scanForSubsystemName(): Exception ", ex);
        }
        return(null);
    }

    public String scanForSubsystemName(String logFileDirectory, String logFileName){
        getLogger().info(".scanForSubsystemName(): Entry");
        String name = null;
        try {
            getLogger().info(".scanForSubsystemName(): [Create InputStream] Start");
            getLogger().info(".scanForSubsystemName(): [Create InputStream] Attempting to Access File->{}", logFileName);
            File initialFile = new File(logFileDirectory, logFileName);
            getLogger().info(".scanForSubsystemName(): [Create InputStream] Created File Handle...");
            InputStream eventStream = new FileInputStream(initialFile);
            getLogger().info(".scanForSubsystemName(): [Create InputStream] Finish");
            getLogger().info(".scanForSubsystemName(): [Scan For Name] Start");
            name = parseForSystemName(eventStream);
            getLogger().info(".scanForSubsystemName(): [Scan For Name] Finish");
            eventStream.close();
        } catch (Exception ex){
            getLogger().info(".scanForSubsystemName(): Exception ", ex);
        }
        return(name);
    }

    public String parseForSystemName(InputStream logFileStream) {
        getLogger().debug(".parseForSystemName(): Entry");
        Scanner sc = null;
        try {
            sc = new Scanner(logFileStream, "UTF-8");
            while(sc.hasNextLine()){
                String currentLine = sc.nextLine();
                int mitafLocation = currentLine.indexOf("MITaF.");
                if(mitafLocation > 0){
                    int probableSubsystemNameLocation = mitafLocation + "MITaF.".length();
                    String nameSubstring = currentLine.substring(probableSubsystemNameLocation);
                    int locationOfDot = nameSubstring.indexOf(".");
                    int locationOfColon = nameSubstring.indexOf(":");
                    if(locationOfColon > 0 || locationOfDot > 0) {
                        int assumedEndpoint = 0;
                        if(locationOfColon < locationOfDot){
                            assumedEndpoint = locationOfColon;
                        } else {
                            assumedEndpoint = locationOfDot;
                        }
                        String name = nameSubstring.substring(0, assumedEndpoint);
                        if(name.contentEquals("PRL")){
                            int mitafPRLLocation = currentLine.indexOf("MITaF.PRL");
                            int probablePRLNameLocation = mitafPRLLocation + "MITaF.PRL.".length();
                            String prlNameSubstring = currentLine.substring(probablePRLNameLocation);
                            int locationOfDotPostPRL = prlNameSubstring.indexOf(".");
                            int locationOfColonPostPRL = prlNameSubstring.indexOf(":");
                            if(locationOfDotPostPRL > 0 || locationOfColonPostPRL > 0) {
                                int assumedEndpointAfterPRL = 0;
                                if (locationOfColonPostPRL < locationOfDotPostPRL) {
                                    assumedEndpointAfterPRL = locationOfColonPostPRL;
                                } else {
                                    assumedEndpointAfterPRL = locationOfDotPostPRL;
                                }
                                name = "PRL." + prlNameSubstring.substring(0, assumedEndpointAfterPRL);
                            }
                        }
                        if (StringUtils.isNotEmpty(name)) {
                            String fullName = "MITaF." + name;
                            return (fullName);
                        }
                    }
                }
                int fhirbreakLocation = currentLine.indexOf("FHIRBreak.");
                if(fhirbreakLocation > 0){
                    int probableSubsystemNameLocation = fhirbreakLocation + "FHIRBreak.".length();
                    String nameSubstring = currentLine.substring(probableSubsystemNameLocation);
                    int locationOfDot = nameSubstring.indexOf(".");
                    int locationOfColon = nameSubstring.indexOf(":");
                    if(locationOfColon > 0 || locationOfDot > 0) {
                        int assumedEndpoint = 0;
                        if(locationOfColon < locationOfDot){
                            assumedEndpoint = locationOfColon;
                        } else {
                            assumedEndpoint = locationOfDot;
                        }
                        String name = nameSubstring.substring(0, assumedEndpoint);
                        if(StringUtils.isNotEmpty(name)){
                            String fullName = "FHIRBreak."+name;
                            return(fullName);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            getLogger().info(".parseForSystemName(): Exception ", ex);
        }
        return(null);
    }

    public boolean isAnAck(String message){
        if(StringUtils.isEmpty(message)){
            return(false);
        }
        String messageType = extractMessageType(message);
        if(StringUtils.isNotEmpty(messageType) && messageType.contains("ACK")){
            return(true);
        }
        return(false);
    }

    public boolean isIngresMLLPTraffic(String logEntry){
        if(StringUtils.isEmpty(logEntry)){
            return(false);
        }
        if(logEntry.contains("mllp://0.0.0.0:")){
            return(true);
        }
        return(false);
    }

    public boolean isEgressMLLPTraffic(String logEntry){
        if(StringUtils.isEmpty(logEntry)){
            return(false);
        }
        if(logEntry.contains("MLLPActivityAnswerCollector") || logEntry.contains("MSA")){
            return(true);
        }
        return(false);
    }

    public String extractEgressMLLPProcessorDetails(String logEntry){
        if(StringUtils.isEmpty(logEntry)){
            return(null);
        }
        int workshopIdLocation = StringUtils.indexOf(logEntry, "ExternalIPC");
        if(workshopIdLocation <= 0){
            getLogger().debug(".extractEgressMLLPProcessorDetails(): Exit, cannot find -ExternalIPC-");
            return(null);
        }
        String workingSubstring = logEntry.substring(workshopIdLocation+"ExternalIPC".length()+1);
        if(StringUtils.isEmpty(workingSubstring)){
            getLogger().debug(".extractEgressMLLPProcessorDetails(): Exit, workingSubstring is empty");
            return(null);
        }
        String splitWorkingSubstring[] = workingSubstring.split("\\.");
        if(splitWorkingSubstring.length < 3){
            getLogger().debug(".extractEgressMLLPProcessorDetails(): Exit, Can't resolve function");
            return(null);
        }
        String portNumber = splitWorkingSubstring[0];
        if(StringUtils.isEmpty(portNumber)){
            if(splitWorkingSubstring.length > 1){
                portNumber = splitWorkingSubstring[1];
            }
        }
        getLogger().debug(".extractEgressMLLPProcessorDetails(): portNumber->{}", portNumber);
        return(portNumber);
    }

    public String extractIngresMLLPPortDetails(String logEntry){
        if(StringUtils.isEmpty(logEntry)){
            return(null);
        }
        int mllpDefinitionPoint = StringUtils.indexOf( logEntry, "mllp://0.0.0.0:");
        if(mllpDefinitionPoint <= 0){
            return(null);
        }
        int portStringStart = mllpDefinitionPoint + "mllp://0.0.0.0:".length();
        String portNumber = logEntry.substring(portStringStart, portStringStart+5);
        getLogger().debug(".extractIngresMLLPPortDetails(): portNumber->{}", portNumber);
        return(portNumber);
    }

    public String extractActualMLLPMessage(String logEntry){
        if(StringUtils.isEmpty(logEntry)){
            return(null);
        }
        int mllpMessageStart = StringUtils.indexOf(logEntry, "MSH" );
        if(mllpMessageStart < 0){
            return(null);
        }
        String actualHL7Message = logEntry.substring(mllpMessageStart);
        return(actualHL7Message);
    }

    public HL7UsefulMetadata extractEgressMetadata(String logEntry){
        if(StringUtils.isEmpty(logEntry)){
            return(null);
        }
        if(isEgressMLLPTraffic(logEntry)){
            String actualMLLPMessage = extractActualMLLPMessage(logEntry);
            getLogger().debug(".extractEgressMetadata: actualMLLPMessage->{}", actualMLLPMessage);
            if(isAnAck(actualMLLPMessage)){
                getLogger().debug(".extractEgressMetadata: It's an ACK");
                HL7UsefulMetadata metadata = new HL7UsefulMetadata();
                String portNumber = extractEgressMLLPProcessorDetails(logEntry);
                String messageId = extractMessageIdFromAck(actualMLLPMessage);
                String messageTimestamp = extractMessageTimestamp(actualMLLPMessage);
                String messageType = extractMessageType(actualMLLPMessage);
                String messageAckType = extractAckCodeFromAck(actualMLLPMessage);
                if(StringUtils.isNotEmpty(messageId) && StringUtils.isNotEmpty(messageTimestamp)){
                    metadata.setMessageId(messageId);
                    metadata.setMessageTimestamp(messageTimestamp);
                    if(StringUtils.isNotEmpty(portNumber)){
                        metadata.setPortNumber(portNumber);
                    }
                    if(StringUtils.isNotEmpty(messageType)){
                        metadata.setMessageType(messageAckType);
                    }
                    if(StringUtils.isNotEmpty(messageAckType)){
                        metadata.setAckCode(messageAckType);
                    }
                    metadata.setAck(true);
                    metadata.setFlowDirection(MessageFlowDirectionEnum.EGRESS_FROM_SUBSYSTEM);
                    return(metadata);
                }
            }
        }
        return(null);
    }

    public HL7TransactionReport buildTransactionReport(HL7UsefulMetadata ingresMessageMetadata, Set<HL7UsefulMetadata> egressMessageMetadataSet){
        if(ingresMessageMetadata == null){
            return(null);
        }
        HL7UsefulMetadata egressMatch = null;
        HL7TransactionReport report = new HL7TransactionReport(ingresMessageMetadata);;
        if(egressMessageMetadataSet == null || egressMessageMetadataSet.isEmpty()) {
            report.setSuccessfulEgressActivity(false);
        } else {
            for(HL7UsefulMetadata currentEgressMetadata: egressMessageMetadataSet){
                if(currentEgressMetadata.getMessageId().contentEquals(ingresMessageMetadata.getMessageId())){
                    egressMatch = currentEgressMetadata;
                    break;
                }
            }
            if(egressMatch != null){
                report.setEgressMetadata(egressMatch);
                report.setSuccessfulEgressActivity(true);
            } else {
                report.setSuccessfulEgressActivity(false);
            }
        }
        return(report);
    }

}
