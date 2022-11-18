package net.fhirfactory.dricats.mimic.tasking.cli.helpers;

import ca.uhn.fhir.context.FhirContext;
import net.fhirfactory.dricats.mimic.tasking.cli.datatypes.HL7UsefulMetadata;
import net.fhirfactory.dricats.mimic.tasking.cli.datatypes.MessageFlowDirectionEnum;
import net.fhirfactory.dricats.mimic.tasking.cli.helpers.common.DRICaTSLogFileParserCommon;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class MLLPLogFileParser extends DRICaTSLogFileParserCommon {
    private static final Logger LOG = LoggerFactory.getLogger(MLLPLogFileParser.class);

    private FhirContext fhirContext;

    //
    // Constructor(s)
    //
    public MLLPLogFileParser(FhirContext fhirContext){
        super(fhirContext);
    }

    //
    // Getters and Setters
    //

    @Override
    protected Logger getLogger(){
        return(LOG);
    }

    //
    // Business Methods
    //

    public String parseForSystemName(InputStream logFileStream) {
        LOG.debug(".parseForSystemName(): Entry");
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
                    if(locationOfDot > 0){
                        String name = nameSubstring.substring(0, locationOfDot);
                        if(StringUtils.isNotEmpty(name)){
                            String fullName = "MITaF."+name;
                            return(fullName);
                        }
                    }
                }
                int fhirbreakLocation = currentLine.indexOf("FHIRBreak.");
                if(fhirbreakLocation > 0){
                    int probableSubsystemNameLocation = fhirbreakLocation + "FHIRBreak.".length();
                    String nameSubstring = currentLine.substring(probableSubsystemNameLocation);
                    int locationOfDot = nameSubstring.indexOf(".");
                    if(locationOfDot > 0){
                        String name = nameSubstring.substring(0, locationOfDot);
                        if(StringUtils.isNotEmpty(name)){
                            String fullName = "FHIRBreak."+name;
                            return(fullName);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LOG.info(".parseForSystemName(): Exception ", ex);
        }
        return(null);
    }



    //
    // Transaction + Metadata Parsing Functions
    //

    public ArrayList<String> loadLogEntriesFromFile(String fileName){
        LOG.info(".extractMLLPMessages(): Entry");
        ArrayList<String> eventList = null;
        try {
            LOG.info(".extractMLLPMessages(): [Create InputStream] Start");
            LOG.info(".extractMLLPMessages(): [Create InputStream] Attempting to Access File->{}", fileName);
            File initialFile = new File(fileName);
            LOG.info(".extractMLLPMessages(): [Create InputStream] Created File Handle...");
            InputStream eventStream = new FileInputStream(initialFile);
            LOG.info(".extractMLLPMessages(): [Create InputStream] Finish");
            LOG.info(".extractMLLPMessages(): [Map InputStream to EventList] Start");
            eventList = extractIndividualMLLPEntries(eventStream);
            LOG.info(".extractMLLPMessages(): [Map InputStream to EventList] Finish");
            eventStream.close();
        } catch( Exception ex){
            LOG.info(".extractMLLPMessages(): Problem loading file, error->{}", ExceptionUtils.getMessage(ex));
            ex.printStackTrace();
        }
        LOG.info(".loadMessageFromFile(): Exit");
        return(eventList);
    }

    public ArrayList<String> extractIndividualMLLPEntries(InputStream eventStream){
        LOG.debug(".extractIndividualMLLPEntries(): Entry");
        Scanner sc = null;
        ArrayList<String> mllpLogList = new ArrayList<>();
        try {
            sc = new Scanner(eventStream, "UTF-8");
            String currentLine = null;
            String previousLine = null;
            while(sc.hasNextLine()){
                previousLine = currentLine;
                currentLine = sc.nextLine();
                boolean isFirstLine = currentLine.contains("MLLPMessageIngresProcessor") || currentLine.contains("MLLPActivityAnswerCollector");
                boolean previousIsFirstLine = false;
                if(StringUtils.isNotEmpty(previousLine)) {
                    previousIsFirstLine = previousLine.contains("MLLPMessageIngresProcessor") || previousLine.contains("MLLPActivityAnswerCollector");
                }
                if(isFirstLine || previousIsFirstLine){
                    LOG.debug(".extractIndividualMLLPEntries(): Found an Ingres MLLP Message Log Entry");
                    StringBuilder currentMessageBuilder = new StringBuilder();
                    if(previousIsFirstLine ){
                        currentMessageBuilder.append(previousLine).append("\r");
                    }
                    currentMessageBuilder.append(currentLine).append("\r");
                    while(sc.hasNextLine()){
                        previousLine = currentLine;
                        currentLine = sc.nextLine();
                        if(StringUtils.isEmpty(currentLine)){
                            break;
                        }
                        boolean isLineNewLogEntry = Character.isDigit(currentLine.charAt(0));
                        if(isLineNewLogEntry){
                            break;
                        }
                        currentMessageBuilder.append(currentLine).append("\r");
                        if(currentLine.startsWith("MSA")) {
                            break;
                        }
                    }
                    String currentMLLPMessage = currentMessageBuilder.toString();
                    LOG.debug(".extractIndividualMLLPEntries(): Adding Entry->{}",currentMLLPMessage);
                    mllpLogList.add(currentMLLPMessage);
                }
            }
        } catch(Exception ex){
            LOG.error(".extractIndividualMLLPEntries(): Problem -> ", ex);
        }
        LOG.debug(".extractIndividualMLLPEntries(): Exit, mllpLogList.size()->{}", mllpLogList.size());
        return(mllpLogList);
    }

    public HashMap<HL7UsefulMetadata, String> mapLogEntryArrayToMessageMap(String subsystemName, ArrayList<String> logEntries) {
        if (logEntries == null) {
            LOG.debug(".mapLogEntryArrayToMessageMap(): logEntries is null");
            return (null);
        }
        LOG.debug(".mapLogEntryArrayToMessageMap(): Entry, logEntries.size()->{}", logEntries.size());
        HashMap<HL7UsefulMetadata, String> messageSet = new HashMap<>();
        for (String currentLogEntry : logEntries) {
            String portNumber = null;
            String message = null;
            boolean isIngresMLLP = isIngresMLLPTraffic(currentLogEntry);
            if (isIngresMLLP) {
                portNumber = extractIngresMLLPPortDetails(currentLogEntry);
                message = extractActualMLLPMessage(currentLogEntry);
                if (message != null) {
                    HL7UsefulMetadata metadata = extractUsefulHL7Data(message);
                    if (metadata != null) {
                        metadata.setPortNumber(portNumber);
                        metadata.setFlowDirection(MessageFlowDirectionEnum.INGRES_TO_SUBSYSTEM);
                        if (StringUtils.isNotEmpty(subsystemName)) {
                            metadata.setSubsystem(subsystemName);
                        }
                        LOG.debug(".mapLogEntryArrayToMessageMap(): [Processing Ingres Message] Adding Message to Set, metadata->{}", metadata);
                        messageSet.put(metadata, message);
                    }
                }
            } else {
                LOG.info(".mapLogEntryArrayToMessageMap(): [Processing Egress Message] Start");
                boolean isEgressMLLP = isEgressMLLPTraffic(currentLogEntry);
                boolean isAnAck = isAnAck(currentLogEntry);
                if (isEgressMLLP) {
                    portNumber = extractEgressMLLPProcessorDetails(currentLogEntry);
                    message = extractActualMLLPMessage(currentLogEntry);
                    if (message != null) {
                        HL7UsefulMetadata metadata = extractUsefulHL7DataFromAck(message);
                        if (metadata != null) {
                            metadata.setPortNumber(portNumber);
                            metadata.setFlowDirection(MessageFlowDirectionEnum.EGRESS_FROM_SUBSYSTEM);
                            metadata.setAck(true);
                            String ackCode = extractAckCodeFromAck(message);
                            if (StringUtils.isNotEmpty(ackCode)) {
                                metadata.setAckCode(ackCode);
                            }
                            if (StringUtils.isNotEmpty(subsystemName)) {
                                metadata.setSubsystem(subsystemName);
                            }
                            LOG.debug(".mapLogEntryArrayToMessageMap(): [Processing Egress Message] Adding Message to Set, metadata->{}s", metadata);
                            messageSet.put(metadata, message);
                        }
                    }
                }
                LOG.info(".mapLogEntryArrayToMessageMap(): [Processing Egress Message] Finish");
            }
        }
        LOG.debug(".mapLogEntryArrayToMessageMap(): Exit, messageSet.size()->{}", messageSet.size());
        return (messageSet);
    }

}
