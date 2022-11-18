package net.fhirfactory.dricats.mimic.tasking.cli.helpers;

import ca.uhn.fhir.context.FhirContext;
import net.fhirfactory.dricats.mimic.tasking.cli.datatypes.HL7TransactionReport;
import net.fhirfactory.dricats.mimic.tasking.cli.datatypes.HL7UsefulMetadata;
import net.fhirfactory.dricats.mimic.tasking.cli.datatypes.MessageFlowDirectionEnum;
import net.fhirfactory.dricats.mimic.tasking.cli.helpers.common.DRICaTSLogFileParserCommon;
import net.fhirfactory.dricats.mimic.tasking.cli.helpers.common.ReportContentWriters;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class MLLPLogFileStreamParser extends DRICaTSLogFileParserCommon {
    private static final Logger LOG = LoggerFactory.getLogger(MLLPLogFileStreamParser.class);

    //
    // Constructor(s)
    //
    public MLLPLogFileStreamParser(FhirContext fhirContext){
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

    //
    // Metadata Only Parsing Functions
    //

    public HashSet<HL7UsefulMetadata> extractEgressTransactionMetadataFromFile(String systemName, String logDirectoryName, String logFileName){
        LOG.debug(".extractEgressTransactionMetadataFromFile(): Entry, systemName->{}, logDirectoryName->{}, logFileName->{}", systemName, logDirectoryName, logFileName);
        HashSet<HL7UsefulMetadata> transactionMetadataSet = null;
        try {
            LOG.info(".extractEgressTransactionMetadataFromFile(): [Create InputStream] Start");
            LOG.info(".extractEgressTransactionMetadataFromFile(): [Create InputStream] Attempting to Access File->{}", logFileName);
            File logFile = new File(logDirectoryName, logFileName);
            LOG.info(".extractEgressTransactionMetadataFromFile(): [Create InputStream] Created File Handle...");
            InputStream eventStream = new FileInputStream(logFile);
            LOG.info(".extractEgressTransactionMetadataFromFile(): [Create InputStream] Finish");
            LOG.info(".extractEgressTransactionMetadataFromFile(): [Map InputStream to Metadata Set] Start");
            transactionMetadataSet = parseEgressLogFileForMetadata(systemName, eventStream);
            LOG.info(".extractEgressTransactionMetadataFromFile(): [Map InputStream to Metadata Set] Finish");
        }  catch( Exception ex){
            LOG.info(".extractEgressTransactionMetadataFromFile(): Problem loading file, error->{}", ExceptionUtils.getMessage(ex));
            ex.printStackTrace();
        }
        LOG.debug(".extractEgressTransactionMetadataFromFile(): Exit");
        return(transactionMetadataSet);
    }

    public HashSet<HL7UsefulMetadata> parseEgressLogFileForMetadata(String systemName, InputStream eventStream){
        LOG.debug(".parseLogFileForMetadata(): Entry");
        Scanner sc = null;
        HashSet<HL7UsefulMetadata> transactionList = new HashSet<>();
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
                    LOG.debug(".parseLogFileForMetadata(): Found an Ingres MLLP Message Log Entry");
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
                    LOG.debug(".parseLogFileForMetadata(): Processing Entry->{}",currentMLLPMessage);
                    HL7UsefulMetadata metadata = extractEgressMetadata(currentMLLPMessage);
                    metadata.setSubsystem(systemName);
                    if(metadata != null) {
                        getLogger().debug(".parseLogFileForMetadata(): Adding metadata element->{}", metadata);
                        transactionList.add(metadata);
                    }
                }
            }
        } catch(Exception ex){
            LOG.error(".extractIndividualMLLPEntries(): Problem -> ", ex);
        }
        LOG.debug(".extractIndividualMLLPEntries(): Exit, mllpLogList.size()->{}", transactionList.size());
        return(transactionList);
    }

    //
    // Ingres Log File Processing
    //

    public HashSet<HL7TransactionReport> processIngresLogFile(String systemName, String logDirectoryName, String logFileName, String outputDirectory, Set<HL7UsefulMetadata> egressMessageMetadataSet){
        LOG.debug(".processIngresLogFile(): Entry, systemName->{}, logDirectoryName->{}, logFileName->{}", systemName, logDirectoryName, logFileName);
        HashSet<HL7TransactionReport> transactionMetadataSet = null;
        try {
            LOG.info(".processIngresLogFile(): [Create InputStream] Start");
            LOG.info(".processIngresLogFile(): [Create InputStream] Attempting to Access File->{}", logFileName);
            File logFile = new File(logDirectoryName, logFileName);
            LOG.info(".processIngresLogFile(): [Create InputStream] Created File Handle...");
            InputStream eventStream = new FileInputStream(logFile);
            LOG.info(".processIngresLogFile(): [Create InputStream] Finish");
            LOG.info(".processIngresLogFile(): [Map InputStream to Metadata Set] Start");
            transactionMetadataSet = processIngresLogFileStream(systemName, eventStream, outputDirectory, egressMessageMetadataSet);
            LOG.info(".processIngresLogFile(): [Map InputStream to Metadata Set] Finish");
        }  catch( Exception ex){
            LOG.info(".processIngresLogFile(): Problem loading file, error->{}", ExceptionUtils.getMessage(ex));
            ex.printStackTrace();
        }
        LOG.debug(".processIngresLogFile(): Exit");
        return(transactionMetadataSet);
    }

    public HashSet<HL7TransactionReport> processIngresLogFileStream(String systemName, InputStream eventStream, String outputDirectory, Set<HL7UsefulMetadata> egressMessageMetadataSet){
        LOG.debug(".processIngresLogFileStream(): Entry, systemName->{}, outputDirectory->{}", systemName, outputDirectory);
        HashSet<HL7TransactionReport> transactionReports = new HashSet<>();

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
                    LOG.debug(".processIngresLogFileStream(): Found an Ingres MLLP Message Log Entry");
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
                    LOG.debug(".processIngresLogFileStream(): Adding Entry->{}",currentMLLPMessage);
                    HL7TransactionReport hl7TransactionReport = processLogEntry(systemName, currentMLLPMessage, outputDirectory, egressMessageMetadataSet);
                    if(hl7TransactionReport != null){
                        transactionReports.add(hl7TransactionReport);
                    }
                }
            }
        } catch(Exception ex){
            LOG.error(".processIngresLogFileStream(): Problem -> ", ex);
        }
        LOG.info(".processIngresLogFileStream(): Exit, transactionReports.size()->{}", transactionReports.size());
        return(transactionReports);
    }

    public HL7TransactionReport processLogEntry(String subsystemName, String logEntry, String outputDirectory, Set<HL7UsefulMetadata> egressMessageMetadataSet){
        if(StringUtils.isEmpty(logEntry)){
            return(null);
        }
        if(StringUtils.isEmpty(outputDirectory)){
            return(null);
        }
        if(StringUtils.isEmpty(subsystemName)){
            subsystemName = "Unknown";
        }
        ReportContentWriters contentWriters = new ReportContentWriters();
        boolean isIngresMLLP = isIngresMLLPTraffic(logEntry);
        if (isIngresMLLP) {
            String portNumber = extractIngresMLLPPortDetails(logEntry);
            String message = extractActualMLLPMessage(logEntry);
            if (message != null) {
                HL7UsefulMetadata metadata = extractUsefulHL7Data(message);
                if (metadata != null) {
                    metadata.setPortNumber(portNumber);
                    metadata.setFlowDirection(MessageFlowDirectionEnum.INGRES_TO_SUBSYSTEM);
                    if (StringUtils.isNotEmpty(subsystemName)) {
                        metadata.setSubsystem(subsystemName);
                    }
                    LOG.debug(".mapLogEntryArrayToMessageMap(): metadata->{}", metadata);
                    HL7TransactionReport report = buildTransactionReport(metadata, egressMessageMetadataSet);
                    LOG.debug(".mapLogEntryArrayToMessageMap(): report->{}", report);
                    return(report);
                }
            }
        }
        LOG.debug(".mapLogEntryArrayToMessageMap(): Exit, no report :( ");
        return(null);
    }


}
