package net.fhirfactory.dricats.mimic.tasking.cli.subcommands;

import ca.uhn.fhir.context.FhirContext;
import net.fhirfactory.dricats.mimic.tasking.cli.datatypes.HL7TransactionReport;
import net.fhirfactory.dricats.mimic.tasking.cli.datatypes.HL7UsefulMetadata;
import net.fhirfactory.dricats.mimic.tasking.cli.helpers.MLLPLogFileStreamParser;
import net.fhirfactory.dricats.mimic.tasking.cli.helpers.common.ReportContentWriters;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.AuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        name="parseLogs",
        description="Compares 2 Directories of Logs for ingres/egress traffic reports"
)
public class ParseLogsOnly implements Runnable{
    private static final Logger LOG = LoggerFactory.getLogger(ParseLogsOnly.class);

    private HashMap<String, ArrayList<String>> ingresMsgMap;

    private ArrayList<AuditEvent> egressEvents;
    private HashMap<String, String> egressEventMsg;
    private HashMap<String, ArrayList<String>> egressMsgMap;

    private ArrayList<String> output;
    private final FhirContext fhirContext = FhirContext.forR4();



    @CommandLine.Option(names = {"--ingresSystemLogDirectory"})
    private String ingresLogDirectory;

    @CommandLine.Option(names = {"--egressSystemLogDirectory"})
    private String egressLogDirectory;

    @CommandLine.Option(names = {"--outputDirectory"})
    private String outputDirectory;

    String messageString;

    @Override
    public void run() {
        LOG.info(".run(): Entry, ingresLogDirectory->{}, egressLogDirectory->{}, outputDirectory->{}", ingresLogDirectory, egressLogDirectory, outputDirectory);

        LOG.info(".run(): [Build Base Directory] Start");
        File baseDirectory = createBaseDirectory(outputDirectory);
        LOG.info(".run(): [Build Base Directory] Finish");

        ReportContentWriters contentWriter = new ReportContentWriters();

        LOG.info(".run(): [Build Egress Subsystem Transaction Metadata Set] Start");

        LOG.info(".run(): [Build Egress Subsystem Transaction Metadata Set] [Getting Egress Log File List] Start");
        Set<String> egressLogFileNamesFromDirectory = null;
        try {
            egressLogFileNamesFromDirectory = getLogFilenamesFromDirectory(egressLogDirectory);
        } catch(Exception ex){
            LOG.error("Problem with searching Egress Log directory ->", ex);
            System.exit(0);
        }
        LOG.info(".run(): [Build Egress Subsystem Transaction Metadata Set] [Getting Egress Log File List] Finish");

        LOG.info(".run(): [Build Egress Subsystem Transaction Metadata Set] [Create Log File Stream Parser] Start");
        MLLPLogFileStreamParser logFileParser = new MLLPLogFileStreamParser(fhirContext);
        LOG.info(".run(): [Build Egress Subsystem Transaction Metadata Set] [Create Log File Stream Parser] Finish");

        LOG.info(".run(): [Build Egress Subsystem Transaction Metadata Set] [Scan for SystemName] Start");
        String egressSystemName = null;
        for(String currentLogFilename: egressLogFileNamesFromDirectory){
            LOG.info(".run(): [Build Egress Subsystem Transaction Metadata Set] [Iterate Through Log Files] Processing->{}", currentLogFilename);
            egressSystemName = logFileParser.scanForSubsystemName(egressLogDirectory, currentLogFilename);
            if(StringUtils.isNotEmpty(egressSystemName)){
                break;
            }
        }
        if(StringUtils.isEmpty(egressSystemName)){
            egressSystemName = "Unknown";
        }
        LOG.info(".run(): [Build Egress Subsystem Transaction Metadata Set] [Scan for SystemName] Finish");

        LOG.info(".run(): [Build Egress Subsystem Transaction Metadata Set] [Iterate Through Log Files] Start");
        HashSet<HL7UsefulMetadata> egressMessageMetadataSet = new HashSet<>();

        for(String currentLogFilename: egressLogFileNamesFromDirectory){
            LOG.info(".run(): [Build Egress Subsystem Transaction Metadata Set] [Iterate Through Log Files] Processing->{}", currentLogFilename);
            HashSet<HL7UsefulMetadata> currentMetadataSet = logFileParser.extractEgressTransactionMetadataFromFile(egressSystemName, egressLogDirectory, currentLogFilename);
            egressMessageMetadataSet.addAll(currentMetadataSet);
        }
        LOG.info(".run(): [Build Egress Subsystem Transaction Metadata Set] [Iterate Through Log Files] Finish, total entries->{}", egressMessageMetadataSet.size());

        LOG.info(".run(): [Build Egress Subsystem Transaction Metadata Set] Finish");


        LOG.info(".run(): [Build Ingres Subsystem Transaction Metadata Set] [Getting Ingres Log File List] Start");
        Set<String> ingresLogFileList = null;
        try {
            ingresLogFileList = getLogFilenamesFromDirectory(ingresLogDirectory);
        } catch(Exception ex){
            LOG.error("Problem with searching Egress Log directory ->", ex);
            System.exit(0);
        }
        LOG.info(".run(): [Build Ingres Subsystem Transaction Metadata Set] [Getting Egress Log File List] Finish");

        LOG.info(".run(): [Build Base Ingres Content Directory] Start");
        String ingresDirectoryName = "IngresSubsystem";
        File ingresBaseDirectory = createSubDirectories(outputDirectory, ingresDirectoryName);
        Path ingresBasePath = ingresBaseDirectory.toPath();
        LOG.info(".run(): [Build Base Ingres Content Directory] Finish");

        LOG.info(".run(): [Build Ingres Subsystem Transaction Metadata Set] [Scan for SystemName] Start");
        String ingresSystemName = null;
        for(String currentLogFilename: ingresLogFileList){
            LOG.info(".run(): [Build Ingres Subsystem Transaction Metadata Set] [Iterate Through Log Files] Processing->{}", currentLogFilename);
            ingresSystemName = logFileParser.scanForSubsystemName(ingresLogDirectory, currentLogFilename);
            if(StringUtils.isNotEmpty(ingresSystemName)){
                break;
            }
        }
        if(StringUtils.isEmpty(ingresSystemName)){
            ingresSystemName = "Unknown";
        }
        LOG.info(".run(): [Build Ingres Subsystem Transaction Metadata Set] [Scan for SystemName] Finish");

        LOG.info(".run(): [Build Ingres Subsystem Transaction Metadata Set] [Iterate Through Log Files] Start");
        HashSet<HL7TransactionReport> transactionReportSet = new HashSet<>();

        for(String currentLogFilename: ingresLogFileList){
            LOG.info(".run(): [Build Ingres Subsystem Transaction Metadata Set] [Iterate Through Log Files] Processing->{}", currentLogFilename);
            HashSet<HL7TransactionReport> currentMetadataSet = logFileParser.processIngresLogFile(ingresSystemName, ingresLogDirectory, currentLogFilename, ingresBaseDirectory.getAbsolutePath(), egressMessageMetadataSet);
            transactionReportSet.addAll(currentMetadataSet);
        }
        LOG.info(".run(): [Build Ingres Subsystem Transaction Metadata Set] [Iterate Through Log Files] Finish, total entries->{}", transactionReportSet.size());
        int unfinishedCount = 0;
        int finishedCount = 0;
        for(HL7TransactionReport currentReport: transactionReportSet){
            if(currentReport.getIngresMetadata() != null) {
                if (currentReport.isSuccessfulEgressActivity()) {
                    LOG.debug("Finished Transaction->{}", currentReport);
                    finishedCount++;
                } else {
                    LOG.debug("Unfinished Transaction->{}", currentReport);
                    unfinishedCount++;
                }
            }
        }
        LOG.info(".run(): Total->{}, Finished->{}, Unfinished->{}", transactionReportSet.size(), finishedCount, unfinishedCount);

        contentWriter.writeTransactionReport(ingresSystemName,outputDirectory, transactionReportSet);
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

    public Set<String> getLogFilenamesFromDirectory(String directoryName) throws IOException {
        try (Stream<Path> stream = Files.list(Paths.get(directoryName))) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toSet());
        }
    }

}
