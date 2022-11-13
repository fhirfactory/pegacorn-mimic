package net.fhirfactory.dricats.mimic.tasking.cli.subcommands;

import ca.uhn.fhir.context.FhirContext;
import net.fhirfactory.dricats.mimic.tasking.cli.datatypes.HL7UsefulMetadata;
import net.fhirfactory.dricats.mimic.tasking.cli.helpers.AuditEventParser;
import net.fhirfactory.dricats.mimic.tasking.cli.helpers.LogFileParser;
import net.fhirfactory.dricats.mimic.tasking.cli.helpers.MessageSetMergerUtilities;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

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
        name="identifyUnsentMessages",
        description="Compares 2 Files and ascertains the Unsent (Missing) Ones"
)
public class IdentifyUnsentMessages implements Runnable{
    private static final Logger LOG = LoggerFactory.getLogger(IdentifyUnsentMessages.class);

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
    private String outputFileName;

    String messageString;

    @Override
    public void run() {
        LOG.info(".run(): Entry, ingresFileName->{}, egressFileName->{}, outputFileName->{}, logLevel->{}", ingresAuditFileName, egressAuditFileName, outputFileName);

        LOG.info(".run(): [Load AuditEvent Details] Start");
        LOG.info(".run(): [Load AuditEvent Details] Loading Content From Ingres Audit File");
        ArrayList<AuditEvent> ingresEventsFromAuditFile = null;
        HashMap<String, String> ingresEventMsg = null;
        HashMap<HL7UsefulMetadata, String> ingresEventsByPatientMap = null;

        AuditEventParser auditEventParser = new AuditEventParser(fhirContext);
        ingresEventsFromAuditFile = auditEventParser.loadEventsFromFile(ingresAuditFileName);
        if(ingresEventsFromAuditFile != null) {
            ingresEventMsg = auditEventParser.mapAuditEventArrayToMessageMap(ingresEventsFromAuditFile);
            ingresEventsByPatientMap = auditEventParser.mapAuditEventArrayToMessageByPatientIdMap(ingresEventsFromAuditFile);
            if(ingresEventMsg != null){
                auditEventParser.writeAllHL7MessagesToSingleFile(outputFileName, ingresEventMsg);
                auditEventParser.writeAllHL7MessagesAsIndividualFile(outputFileName, ingresEventMsg);
                auditEventParser.writeAllHL7MessagesAsIndividualFileByPatientId(outputFileName, ingresEventsByPatientMap);
            }
        }
        LOG.info(".run(): [Load AuditEvent Details] Loading Content From Ingres Log File");
        ArrayList<String> ingresEventsFromLogFile = null;
        HashMap<HL7UsefulMetadata, String> logFileEntryMap = null;
        LogFileParser logFileParser = new LogFileParser(fhirContext);
        ingresEventsFromLogFile = logFileParser.loadLogEntriesFromFile(ingresLogFileName);
        if(ingresEventsFromLogFile != null) {
            logFileEntryMap = logFileParser.mapLogEntryArrayToMessageMap(ingresEventsFromLogFile);
            if(logFileEntryMap != null) {
                auditEventParser.writeAllHL7MessagesAsIndividualFileByPatientId(outputFileName + "FromLogs", logFileEntryMap);
            }
        }
        if(ingresEventsByPatientMap != null || logFileEntryMap != null){
            MessageSetMergerUtilities messageSetMerger = new MessageSetMergerUtilities();
            HashMap<HL7UsefulMetadata, String> mergedTest = messageSetMerger.mergeMaps(ingresEventsByPatientMap, logFileEntryMap);
        }

        LOG.info(".run(): [Load AuditEvent Details] Loading Content From Egress Audit File");
        egressEvents = auditEventParser.loadEventsFromFile(egressAuditFileName);

        LOG.info(".run(): [Load AuditEvent Details] Finish");
        System.exit(0);
    }


}
