package net.fhirfactory.dricats.mimic.tasking.cli.helpers;

import ca.uhn.fhir.context.FhirContext;
import net.fhirfactory.dricats.mimic.tasking.cli.datatypes.HL7UsefulMetadata;
import net.fhirfactory.dricats.mimic.tasking.cli.datatypes.MessageFlowDirectionEnum;
import net.fhirfactory.dricats.mimic.tasking.cli.helpers.common.HL7ContentParserCommon;
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

public class LogFileParser extends HL7ContentParserCommon {
    private static final Logger LOG = LoggerFactory.getLogger(LogFileParser.class);

    private FhirContext fhirContext;

    //
    // Constructor(s)
    //
    public LogFileParser(FhirContext fhirContext){
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
                boolean isFirstLine = currentLine.contains("MLLPMessageIngresProcessor");
                boolean previousIsFirstLine = false;
                if(StringUtils.isNotEmpty(previousLine)) {
                    previousIsFirstLine = previousLine.contains("MLLPMessageIngresProcessor");
                }
                if(isFirstLine || previousIsFirstLine){
                    LOG.debug(".extractIndividualMLLPEntries(): Found Log Entry");
                    StringBuilder currentMessageBuilder = new StringBuilder();
                    if(previousIsFirstLine ){
                        currentMessageBuilder.append(previousLine).append("\r");
                    }
                    currentMessageBuilder.append(currentLine).append("\r");
                    while(sc.hasNextLine()){
                        previousLine = currentLine;
                        currentLine = sc.nextLine();
                        boolean isLineNewLogEntry = Character.isDigit(currentLine.charAt(0));
                        if(isLineNewLogEntry){
                            break;
                        }
                        currentMessageBuilder.append(currentLine).append("\r");
                    }
                    String currentMLLPMessage = currentMessageBuilder.toString();
                    mllpLogList.add(currentMLLPMessage);
                }
            }
        } catch(Exception ex){
            LOG.error(".extractIndividualMLLPEntries(): Problem -> ", ex);
        }
        LOG.debug(".extractIndividualMLLPEntries(): Exit, mllpLogList.size()->{}", mllpLogList.size());
        return(mllpLogList);
    }

    public HashMap<HL7UsefulMetadata, String> mapLogEntryArrayToMessageMap(ArrayList<String> logEntries){
        if(logEntries == null){
            LOG.debug(".mapLogEntryArrayToMessageMap(): logEntries is null");
            return(null);
        }
        LOG.debug(".mapLogEntryArrayToMessageMap(): Entry, logEntries.size()->{}", logEntries.size());
        HashMap<HL7UsefulMetadata, String> messageSet = new HashMap<>();
        for(String currentLogEntry: logEntries){
            String portNumber = null;
            String message =null;
            boolean isIngresMLLP = isIngresMLLPTraffic(currentLogEntry);
            if(isIngresMLLP){
                portNumber = extractIngresMLLPPortDetails(currentLogEntry);
                LOG.error(".mapLogEntryArrayToMessageMap(): portNumber -> {}", portNumber);
                message = extractMLLPMessage(currentLogEntry);
                if(message != null){
                    HL7UsefulMetadata metadata = extractUsefulHL7Data(message);
                    if(metadata != null){
                        LOG.error(".mapLogEntryArrayToMessageMap(): metadata -> {}", metadata);
                        metadata.setPortNumber(portNumber);
                        metadata.setFlowDirection(MessageFlowDirectionEnum.INGRES_TO_SUBSYSTEM);
                        messageSet.put(metadata, message);
                        LOG.error(".mapLogEntryArrayToMessageMap(): metadata -> {}", metadata);
                    }
                }
            }
        }
        LOG.debug(".mapLogEntryArrayToMessageMap(): Exit, messageSet.size()->{}", messageSet.size());
        return(messageSet);
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

    public String extractMLLPMessage(String logEntry){
        if(StringUtils.isEmpty(logEntry)){
            return(null);
        }
        int mllpMessageStart = StringUtils.indexOf(logEntry, "MSH" );
        if(mllpMessageStart <= 0){
            return(null);
        }
        String actualHL7Message = logEntry.substring(mllpMessageStart);
        return(actualHL7Message);
    }
}
