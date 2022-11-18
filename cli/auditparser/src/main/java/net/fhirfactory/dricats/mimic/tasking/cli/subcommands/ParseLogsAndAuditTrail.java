package net.fhirfactory.dricats.mimic.tasking.cli.subcommands;

import ca.uhn.fhir.context.FhirContext;
import net.fhirfactory.dricats.mimic.tasking.cli.datatypes.HL7UsefulMetadata;
import net.fhirfactory.dricats.mimic.tasking.cli.helpers.DRICaTSAuditEventParser;
import net.fhirfactory.dricats.mimic.tasking.cli.helpers.MLLPLogFileParser;
import net.fhirfactory.dricats.mimic.tasking.cli.helpers.MessageSetMergerUtilities;
import net.fhirfactory.dricats.mimic.tasking.cli.helpers.common.ReportContentWriters;
import org.hl7.fhir.r4.model.AuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * Copyright (c) 2022 Mark Hunter
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

@CommandLine.Command(
        name="parseLogsAndAuditTrail",
        description="Compares 2 Files and ascertains the Unsent (Missing) Ones"
)
public class ParseLogsAndAuditTrail implements Runnable{
    private static final Logger LOG = LoggerFactory.getLogger(ParseLogsAndAuditTrail.class);

    private HashMap<String, ArrayList<String>> ingresMsgMap;

    private ArrayList<AuditEvent> egressEvents;
    private HashMap<String, String> egressEventMsg;
    private HashMap<String, ArrayList<String>> egressMsgMap;

    private ArrayList<String> output;
    private final FhirContext fhirContext = FhirContext.forR4();



    @CommandLine.Option(names = {"--ingresAuditFile"})
    private String ingresAuditFileName;

    @CommandLine.Option(names = {"--ingresLogFile"})
    private String ingresLogFileName;

    @CommandLine.Option(names = {"--egressAuditFile"})
    private String egressAuditFileName;

    @CommandLine.Option(names = {"--egressLogFile"})
    private String egressLogFileName;

    @CommandLine.Option(names = {"-o", "--output"})
    private String outputName;

    String messageString;

    @Override
    public void run() {
        LOG.info(".run(): Entry, ingresFileName->{}, egressFileName->{}, outputFileName->{}, logLevel->{}", ingresAuditFileName, egressAuditFileName, outputName);

        LOG.info("ingresAuditFileName = {}", ingresAuditFileName);
        LOG.info("ingresLogFileName = {}", ingresLogFileName);
        LOG.info("egressAuditFileName = {}", egressAuditFileName);
        LOG.info("egressLogFileName = {}", egressLogFileName);
        LOG.info("outputDirectoryName = {}", outputName);

        ReportContentWriters contentWriter = new ReportContentWriters();

        LOG.info(".run(): [Build Base Directory] Start");
        File baseDirectory = createBaseDirectory(outputName);
        LOG.info(".run(): [Build Base Directory] Finish");

        LOG.info(".run(): [Build Base Ingres Content Directory] Start");
        String ingresDirectoryName = "IngresSubsystem";
        File ingresBaseDirectory = createSubDirectories(outputName, ingresDirectoryName);
        Path ingresBasePath = ingresBaseDirectory.toPath();
        LOG.info(".run(): [Build Base Ingres Content Directory] Finish");

        LOG.info(".run(): [Load Ingres Subsystem AuditEvent Details] Start");
        ArrayList<AuditEvent> ingresEventsFromAuditFile = null;
        HashMap<String, String> ingresEventMsg = null;
        HashMap<HL7UsefulMetadata, String> ingresMetadataMapFromAuditEvents = null;

        DRICaTSAuditEventParser auditEventParser = new DRICaTSAuditEventParser(fhirContext);
        ingresEventsFromAuditFile = auditEventParser.loadEventsFromFile(ingresAuditFileName);
        if(ingresEventsFromAuditFile != null) {
            ingresMetadataMapFromAuditEvents = auditEventParser.mapAuditEventArrayToMetadataBasedMap(ingresEventsFromAuditFile);
            if(ingresEventMsg != null){
                contentWriter.writeAllHL7MessagesToSingleFile(ingresBaseDirectory, "AuditEvents", ingresMetadataMapFromAuditEvents);
                contentWriter.writeAllHL7MessagesAsIndividualFile(ingresBaseDirectory, "AuditEvents", ingresMetadataMapFromAuditEvents);
            }
        }
        LOG.info(".run(): [Load Ingres Subsystem AuditEvent Details] Finish");


        LOG.info(".run(): [Load Ingres Subsystem LogEvent Details] Start");
        ArrayList<String> ingresEventsFromLogFile = null;
        HashMap<String, String> ingresEventMapFromLogFile = null;
        HashMap<HL7UsefulMetadata, String> ingresMetadataMapFromLogFile = null;
        MLLPLogFileParser logFileParser = new MLLPLogFileParser(fhirContext);
        String ingresSystemName = logFileParser.scanForSubsystemName(ingresLogFileName);
        ingresEventsFromLogFile = logFileParser.loadLogEntriesFromFile(ingresLogFileName);
        if(ingresEventsFromLogFile != null){
            ingresMetadataMapFromLogFile = logFileParser.mapLogEntryArrayToMessageMap(ingresSystemName, ingresEventsFromLogFile);
            if(ingresMetadataMapFromLogFile != null) {
                contentWriter.writeAllHL7MessagesToSingleFile(ingresBaseDirectory, "LogEvents", ingresMetadataMapFromLogFile);
                contentWriter.writeAllHL7MessagesAsIndividualFile(ingresBaseDirectory, "LogEvents", ingresMetadataMapFromLogFile);
            }
        }
        LOG.info(".run(): [Load Ingres Subsystem LogEvent Details] Finish");


        LOG.info(".run(): [Merge Ingres Subsystem AuditEvent and LogEvent Details] Start");
        if(ingresMetadataMapFromAuditEvents != null || ingresMetadataMapFromLogFile != null){
            MessageSetMergerUtilities messageSetMerger = new MessageSetMergerUtilities();
            HashMap<HL7UsefulMetadata, String> mergedContent = messageSetMerger.mergeIngresMaps(ingresMetadataMapFromAuditEvents, ingresMetadataMapFromLogFile);
            contentWriter.writeAllHL7MessagesAsIndividualFileByMetadata(ingresBaseDirectory, mergedContent);
        }
        LOG.info(".run(): [Merge Ingres Subsystem AuditEvent and LogEvent Details] Finish");


        LOG.info(".run(): [Build Base Ingres Content Directory] Start");
        String egressDirectoryName = "EgressSubsystem";
        File egressBaseDirectory = createSubDirectories(outputName, egressDirectoryName);
        Path egressBasePath = egressBaseDirectory.toPath();
        LOG.info(".run(): [Build Base Ingres Content Directory] Finish");


        LOG.info(".run(): [Load Egress Subsystem AuditEvent Details] Start");
        ArrayList<AuditEvent> egressEventsFromAuditFile = null;
        HashMap<HL7UsefulMetadata, String> egressMetadataMapFromAuditEvents = null;

        egressEventsFromAuditFile = auditEventParser.loadEventsFromFile(egressAuditFileName);
        if(egressEventsFromAuditFile != null) {
            egressMetadataMapFromAuditEvents = auditEventParser.mapAuditEventArrayToMetadataBasedMap(egressEventsFromAuditFile);
            if(egressEventMsg != null){
                contentWriter.writeAllHL7MessagesToSingleFile(egressBaseDirectory, "AuditEvents", egressMetadataMapFromAuditEvents);
                contentWriter.writeAllHL7MessagesAsIndividualFile(egressBaseDirectory, "AuditEvents", egressMetadataMapFromAuditEvents);
            }
        }
        LOG.info(".run(): [Load Egress Subsystem AuditEvent Details] Finish");


        LOG.info(".run(): [Load Egress Subsystem LogEvent Details] Start");
        ArrayList<String> egressEventsFromLogFile = null;
        HashMap<HL7UsefulMetadata, String> egressMetadataMapFromLogFile = null;
        String egressSystemName = logFileParser.scanForSubsystemName(egressLogFileName);
        egressEventsFromLogFile = logFileParser.loadLogEntriesFromFile(egressLogFileName);
        if(egressEventsFromLogFile != null) {
            egressMetadataMapFromLogFile = logFileParser.mapLogEntryArrayToMessageMap(egressSystemName,egressEventsFromLogFile);
            if(egressMetadataMapFromLogFile != null) {
                contentWriter.writeAllHL7MessagesToSingleFile(egressBaseDirectory, "LogEvents", egressMetadataMapFromLogFile);
                contentWriter.writeAllHL7MessagesAsIndividualFile(egressBaseDirectory, "LogEvents", egressMetadataMapFromLogFile);
            }
        }
        LOG.info(".run(): [Load Egress Subsystem LogEvent Details] Finish");

        LOG.info(".run(): [Merge Egress Subsystem AuditEvent and LogEvent Details] Start");
        if(egressMetadataMapFromAuditEvents != null || egressMetadataMapFromLogFile != null){
            LOG.info(".run(): [Merge Egress Subsystem AuditEvent and LogEvent Details] Processing");
            MessageSetMergerUtilities messageSetMerger = new MessageSetMergerUtilities();
            HashMap<HL7UsefulMetadata, String> mergedContent = messageSetMerger.mergeEgressMaps(egressMetadataMapFromAuditEvents, egressMetadataMapFromLogFile);
            contentWriter.writeAllHL7MessagesAsIndividualFileByMetadata(egressBaseDirectory, mergedContent);
        }
        LOG.info(".run(): [Merge Egress Subsystem AuditEvent and LogEvent Details] Finish");

        System.exit(0);
    }

    protected File createBaseDirectory(String directoryName){
        File directory = new File(directoryName);
        if(directory.exists()){
            LOG.warn(".createBaseDirectory(): [Create Output Directory] Directory {} already exists", directory);
        } else {
            directory.mkdir();
        }
        return(directory);
    }

    protected File createSubDirectories(String basePath, String subdirectoryName){
        File subdirectory = new File(basePath, subdirectoryName);
        if(subdirectory.exists()){
            LOG.warn(".createSubDirectories(): [Create Output Directory] Directory {} already exists", subdirectory);
        } else {
            subdirectory.mkdir();
        }
        return(subdirectory);
    }

}
