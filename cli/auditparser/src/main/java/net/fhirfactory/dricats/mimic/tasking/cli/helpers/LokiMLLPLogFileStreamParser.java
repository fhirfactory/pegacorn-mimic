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

public class LokiMLLPLogFileStreamParser extends DRICaTSLogFileParserCommon {
    private static final Logger LOG = LoggerFactory.getLogger(LokiMLLPLogFileStreamParser.class);

    ReportContentWriters contentWriters;

    //
    // Constructor(s)
    //
    public LokiMLLPLogFileStreamParser(FhirContext fhirContext){
        super(fhirContext);
        contentWriters = new ReportContentWriters();
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
            LOG.trace(".extractEgressTransactionMetadataFromFile(): [Create InputStream] Start");
            LOG.trace(".extractEgressTransactionMetadataFromFile(): [Create InputStream] Attempting to Access File->{}", logFileName);
            File logFile = new File(logDirectoryName, logFileName);
            LOG.trace(".extractEgressTransactionMetadataFromFile(): [Create InputStream] Created File Handle...");
            InputStream eventStream = new FileInputStream(logFile);
            LOG.trace(".extractEgressTransactionMetadataFromFile(): [Create InputStream] Finish");
            LOG.trace(".extractEgressTransactionMetadataFromFile(): [Map InputStream to Metadata Set] Start");
            transactionMetadataSet = parseEgressJSONLogFileForMetadata(systemName, eventStream);
            LOG.trace(".extractEgressTransactionMetadataFromFile(): [Map InputStream to Metadata Set] Finish");
        }  catch( Exception ex){
            LOG.trace(".extractEgressTransactionMetadataFromFile(): Problem loading file, error->{}", ExceptionUtils.getMessage(ex));
            ex.printStackTrace();
        }
        LOG.debug(".extractEgressTransactionMetadataFromFile(): Exit");
        return(transactionMetadataSet);
    }

    public HashSet<HL7UsefulMetadata> parseEgressJSONLogFileForMetadata(String systemName, InputStream eventStream){
        LOG.debug(".parseLogFileForMetadata(): Entry");
        Scanner sc = null;
        HashSet<HL7UsefulMetadata> transactionList = new HashSet<>();
        try {
            sc = new Scanner(eventStream, "UTF-8");
            String currentLine = null;
            while(sc.hasNextLine()){
                currentLine = sc.nextLine();
                boolean isAckMessage = currentLine.contains("Acknowledgement Message") || currentLine.contains("MLLPActivityAnswerCollector");
                if(isAckMessage){
                    LOG.debug(".parseLogFileForMetadata(): Found an Ingres MLLP Ack Message Log Entry");

                    int messageLeadInLocation = StringUtils.indexOf(currentLine, "Acknowledgement Message-\\u003e");
                    int messageStartLocation = messageLeadInLocation + "Acknowledgement Message-\\u003e".length();
                    LOG.trace(".parseLogFileForMetadata(): messageLeadInLocation->{}, messageStartLocation->{}", messageLeadInLocation, messageStartLocation);
                    if(messageLeadInLocation > 0){
                        String messageStart = currentLine.substring(messageStartLocation);
                        LOG.trace("Content from start of Message->{}", messageStart);
                        int messageLeadOutLocation = StringUtils.indexOf(messageStart, "\\n\",\"stream\":\"");
                        LOG.trace(".parseLogFileForMetadata(): messageLeadOutLocation->{}", messageLeadOutLocation);

                        if(messageLeadOutLocation > 0){
                            String currentMLLPMessage = messageStart.substring(0, messageLeadOutLocation);
                            getLogger().trace(".parseLogFileForMetadata(): Processing Entry->{}",currentMLLPMessage);
                            HL7UsefulMetadata metadata = extractEgressMetadata(currentMLLPMessage);
                            getLogger().debug(".parseLogFileForMetadata(): metadata>{}",metadata);
                            if(StringUtils.isNotEmpty(systemName)) {
                                metadata.setSubsystem(systemName);
                            }
                            if(metadata != null) {
                                getLogger().trace(".parseLogFileForMetadata(): Adding metadata element->{}", metadata);
                                transactionList.add(metadata);
                            }
                        }
                    }
                }
            }
        } catch(Exception ex){
            LOG.error(".parseEgressJSONLogFileForMetadata(): Problem -> ", ex);
        }
        LOG.debug(".parseEgressJSONLogFileForMetadata(): Exit, mllpLogList.size()->{}", transactionList.size());
        return(transactionList);
    }



    //
    // Ingres Log File Processing
    //

    public HashSet<HL7TransactionReport> processIngresJSONLogFile(String systemName, String ingresPort, String logDirectoryName, String logFileName, Set<HL7UsefulMetadata> egressMessageMetadataSet){
        LOG.debug(".processIngresLogFile(): Entry, systemName->{}, ingresPort->{}, logDirectoryName->{}, logFileName->{}", systemName, ingresPort, logDirectoryName, logFileName);
        HashSet<HL7TransactionReport> transactionMetadataSet = null;
        try {
            LOG.trace(".processIngresLogFile(): [Create InputStream] Start");
            LOG.trace(".processIngresLogFile(): [Create InputStream] Attempting to Access File->{}", logFileName);
            File logFile = new File(logDirectoryName, logFileName);
            LOG.trace(".processIngresLogFile(): [Create InputStream] Created File Handle...");
            InputStream eventStream = new FileInputStream(logFile);
            LOG.trace(".processIngresLogFile(): [Create InputStream] Finish");
            LOG.trace(".processIngresLogFile(): [Map InputStream to Metadata Set] Start");
            transactionMetadataSet = processIngresLokiLogFileStream(systemName, ingresPort, eventStream,egressMessageMetadataSet);
            LOG.trace(".processIngresLogFile(): [Map InputStream to Metadata Set] Finish");
        }  catch( Exception ex){
            LOG.trace(".processIngresLogFile(): Problem loading file, error->{}", ExceptionUtils.getMessage(ex));
            ex.printStackTrace();
        }
        LOG.debug(".processIngresLogFile(): Exit");
        return(transactionMetadataSet);
    }

    public class LogEntry{
        
    }

    public HashSet<HL7TransactionReport> processIngresLokiLogFileStream(String systemName, String ingresPort, InputStream eventStream, Set<HL7UsefulMetadata> egressMessageMetadataSet){
        LOG.debug(".processIngresLokiLogFileStream(): Entry, systemName->{}", systemName);
        HashSet<HL7TransactionReport> transactionReports = new HashSet<>();

        Scanner sc = null;
        ArrayList<String> mllpLogList = new ArrayList<>();
        try {
            sc = new Scanner(eventStream, "UTF-8");
            String currentLine = null;
            while(sc.hasNextLine()){
                currentLine = sc.nextLine();
                boolean isFirstLine = currentLine.contains("MLLPMessageIngresProcessor") || currentLine.contains("MLLPActivityAnswerCollector");
                if(isFirstLine ){
                    LOG.trace(".processIngresLokiLogFileStream(): Found an Ingres MLLP Message Log Entry");
                    int messageLeadInLocation = StringUtils.indexOf(currentLine, "Incoming Message-\\u003e");
                    int messageStartLocation = messageLeadInLocation + "Incoming Message-\\u003e".length();
                    LOG.trace(".processIngresLokiLogFileStream(): messageLeadInLocation->{}, messageStartLocation->{}", messageLeadInLocation, messageStartLocation);
                    if(messageLeadInLocation > 0) {
                        String messageStart = currentLine.substring(messageStartLocation);
                        LOG.trace("Content from start of Message->{}", messageStart);
                        int messageLeadOutLocation = StringUtils.indexOf(messageStart, "\",\"stream\":\"");
                        LOG.trace(".processIngresLokiLogFileStream(): messageLeadOutLocation->{}", messageLeadOutLocation);
                        if (messageLeadOutLocation > 0) {
                            LOG.trace(".processIngresLokiLogFileStream(): [Do Substring] Start");
                            String currentMLLPMessage = messageStart.substring(0, messageLeadOutLocation);
                            LOG.trace(".processIngresLokiLogFileStream(): [Do Substring] Finish");
                            LOG.trace(".processIngresLokiLogFileStream(): Adding Entry->{}", currentMLLPMessage);
                            HL7TransactionReport hl7TransactionReport = processIngresSystemLogEntry(systemName, ingresPort, currentLine, currentMLLPMessage, egressMessageMetadataSet);
                            if (hl7TransactionReport != null) {
                                transactionReports.add(hl7TransactionReport);
                            }
                        }
                    }
                }
            }
        } catch(Exception ex){
            LOG.error(".processIngresLokiLogFileStream(): Problem -> ", ex);
        }
        LOG.debug(".processIngresLokiLogFileStream(): Exit, transactionReports.size()->{}", transactionReports.size());
        return(transactionReports);
    }

    public HL7TransactionReport processIngresSystemLogEntry(String subsystemName, String ingresPort, String logEntry, String mllpMessage, Set<HL7UsefulMetadata> egressMessageMetadataSet){

        getLogger().debug(".processIngresSystemLogEntry(): Entry, subsystemName->{}",subsystemName);
        if(StringUtils.isEmpty(logEntry)){
            return(null);
        }
        if(StringUtils.isEmpty(subsystemName)){
            subsystemName = "Unknown";
        }
        LOG.trace(".processIngresSystemLogEntry(): Have Key Details, Will Now Process!");
        boolean isIngresMLLP = isIngresMLLPTraffic(logEntry);
        if (isIngresMLLP || logEntry.startsWith("MSH")) {
            LOG.trace(".processIngresSystemLogEntry(): Is an Ingres Message!");
            String portNumber = extractIngresMLLPPortDetails(logEntry);
            if (mllpMessage != null) {
                HL7UsefulMetadata metadata = extractUsefulHL7Data(mllpMessage);
                if (metadata != null) {
                    metadata.setPortNumber(portNumber);
                    metadata.setFlowDirection(MessageFlowDirectionEnum.INGRES_TO_SUBSYSTEM);
                     if (StringUtils.isNotEmpty(subsystemName)) {
                        metadata.setSubsystem(subsystemName);
                    }
                    LOG.trace(".processIngresSystemLogEntry(): metadata->{}", metadata);
                    HL7TransactionReport report = null;
                     if(StringUtils.isNotEmpty(ingresPort)) {
                         if(StringUtils.isNotEmpty(portNumber) && portNumber.contentEquals(ingresPort)) {
                             report = buildTransactionReport(metadata, egressMessageMetadataSet);
                         }
                     } else {
                         report = buildTransactionReport(metadata, egressMessageMetadataSet);
                     }
                    LOG.trace(".processIngresSystemLogEntry(): report->{}", report);
                    return(report);
                }
            }
        }
        LOG.debug(".processIngresSystemLogEntry(): Exit, no report :( ");
        return(null);
    }




}
