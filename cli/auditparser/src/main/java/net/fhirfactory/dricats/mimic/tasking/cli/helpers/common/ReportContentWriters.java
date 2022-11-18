package net.fhirfactory.dricats.mimic.tasking.cli.helpers.common;

import net.fhirfactory.dricats.mimic.tasking.cli.datatypes.HL7TransactionReport;
import net.fhirfactory.dricats.mimic.tasking.cli.datatypes.HL7UsefulMetadata;
import net.fhirfactory.dricats.mimic.tasking.cli.datatypes.MessageFlowDirectionEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

public class ReportContentWriters {
    private static final Logger LOG = LoggerFactory.getLogger(ReportContentWriters.class);

    protected Logger getLogger(){
        return(LOG);
    }


    public void writeAllHL7MessagesAsIndividualFileByMetadata(File basePath, Map<HL7UsefulMetadata, String> msgMap){
        getLogger().debug(".writeAllHL7MessagesAsIndividualFileByPatientId(): Entry");
        if(basePath == null){
            getLogger().info(".writeAllHL7MessagesAsIndividualFileByPatientId(): Exit, outputFileNamePrefix is empty");
            return;
        }
        try {
            getLogger().info(".writeAllHL7MessagesAsIndividualFileByPatientId(): [Create Output Directory (FileByPatientId)] Start");
            String byMsgIdDirectoryName = "ByPatientId";
            File byMsgIdDirectory = new File(basePath, byMsgIdDirectoryName);
            if(byMsgIdDirectory.exists()){
                getLogger().info(".writeAllHL7MessagesAsIndividualFileByPatientId(): [Create Output Directory (FileByPatientId)] Directory {} already exists", byMsgIdDirectoryName);
            } else {
                byMsgIdDirectory.mkdir();
            }
            getLogger().info(".writeAllHL7MessagesAsIndividualFileByPatientId(): [Create Output Directory (FileByPatientId)] Finish");
            getLogger().info(".writeAllHL7MessagesAsIndividualFileByPatientId(): [Write Individual Files (FileByPatientId)] Start");
            for(HL7UsefulMetadata  currentMsgId: msgMap.keySet()){
                getLogger().debug(".writeAllHL7MessagesAsIndividualFileByPatientId(): [Create OutputStream] Start");
                StringBuilder fileNameBuilder = new StringBuilder();
                String subsystemName = currentMsgId.getSubsystem();
                if(StringUtils.isNotEmpty(subsystemName)){
                    fileNameBuilder.append(subsystemName).append("-");
                } else {
                    fileNameBuilder.append("UnknownSystem").append("-");
                }
                if(currentMsgId.getFlowDirection() != null){
                    if(currentMsgId.getFlowDirection().equals(MessageFlowDirectionEnum.INGRES_TO_SUBSYSTEM)){
                        fileNameBuilder.append("Ingres").append("-");
                    } else {
                        fileNameBuilder.append("Egress").append("-");
                    }
                } else {
                    fileNameBuilder.append("UnknownDirection").append("-");
                }
                String portNumber = currentMsgId.getPortNumber();
                if(StringUtils.isNotEmpty(portNumber)){
                    fileNameBuilder.append(portNumber).append("-");
                } else {
                    fileNameBuilder.append("UnknownEndpoint").append("-");
                }
                if(currentMsgId.isAck()){
                    if(StringUtils.isNotEmpty(currentMsgId.getAckCode())){
                        fileNameBuilder.append("AckCode").append("-");
                        fileNameBuilder.append(currentMsgId.getAckCode()).append("-");
                    } else {
                        fileNameBuilder.append("Ack").append("-");
                    }
                }
                if(StringUtils.isNotEmpty(currentMsgId.getPatientId())) {
                    fileNameBuilder.append(currentMsgId.getPatientId()).append("-");
                } else {
                    fileNameBuilder.append("NoPatientId").append("-");
                }
                fileNameBuilder.append(currentMsgId.getMessageTimestamp()).append("-");
                fileNameBuilder.append(currentMsgId.getMessageId()).append(".hl7");
                String currentFileName = fileNameBuilder.toString();
                Path fileName = Paths.get(byMsgIdDirectory.getPath(), currentFileName);
                getLogger().debug(".writeAllHL7MessagesAsIndividualFileByPatientId(): [Create OutputStream] Start, File->{}", fileName);
                FileOutputStream eventStream = new FileOutputStream(fileName.toFile());
                getLogger().debug(".writeAllHL7MessagesAsIndividualFileByPatientId(): [Create OutputStream] Finish");
                getLogger().debug(".writeAllHL7MessagesAsIndividualFileByPatientId(): [Write HL7 Message List] Start");
                String currentMsg = msgMap.get(currentMsgId);
                if(StringUtils.isEmpty(currentMsg)){
                    getLogger().warn(".writeAllHL7MessagesAsIndividualFileByPatientId(): [Write HL7 Message List] Something odd, the message itself is empty");
                    currentMsg = "Unknown";
                }
                eventStream.write(currentMsg.getBytes(StandardCharsets.UTF_8));
                getLogger().debug(".writeAllHL7MessagesAsIndividualFileByPatientId(): [Write HL7 Message List] Finish");
                getLogger().debug(".writeAllHL7MessagesAsIndividualFileByPatientId(): [Close OutputStream] Start");
                eventStream.close();
                getLogger().debug(".writeAllHL7MessagesAsIndividualFileByPatientId(): [Close OutputStream] Finish");
            }
            getLogger().info(".writeAllHL7MessagesAsIndividualFileByPatientId(): [Write Individual Files (FileByPatientId)] Finish");

            getLogger().info(".writeAllHL7MessagesAsIndividualFileByPatientId(): [Write HL7 Message List] Finish");
        } catch(Exception ex){
            getLogger().warn(".writeAllHL7MessagesAsIndividualFileByPatientId(): Error...", ex);
        }
        getLogger().debug(".writeAllHL7MessagesAsIndividualFileByPatientId(): Exit");
    }

    public void writeAllHL7MessagesToSingleFile(File basePath, String contentSource, Map<HL7UsefulMetadata, String> msgMap){
        getLogger().debug(".writeAllHL7MessagesToSingleFile(): Entry");
        if(basePath == null){
            getLogger().warn(".writeAllHL7MessagesToSingleFile(): Exit, outputFileNamePrefix is empty");
            return;
        }
        try {
            getLogger().info(".writeAllHL7MessagesToSingleFile(): [Create OutputStream] Start");
            String fileName = "AllMessages-" + contentSource + ".hl7";
            Path fullFileName = Paths.get(basePath.getPath(), fileName);
            getLogger().debug(".writeAllHL7MessagesToSingleFile(): [Create OutputStream] Attempting to Create File->{}", fullFileName);
            FileOutputStream eventStream = new FileOutputStream(fullFileName.toFile());
            getLogger().debug(".writeAllHL7MessagesToSingleFile(): [Create OutputStream] Finish");
            getLogger().debug(".writeAllHL7MessagesToSingleFile(): [Write HL7 Message List] Entry");
            for(String currentMsg: msgMap.values()){
                eventStream.write(currentMsg.getBytes(StandardCharsets.UTF_8));
                eventStream.write("\n".getBytes(StandardCharsets.UTF_8));
            }
            eventStream.close();
            getLogger().info(".writeAllHL7MessagesToSingleFile(): [Write HL7 Message List] Finish");
        } catch(Exception ex){
            getLogger().warn(".writeAllHL7MessagesToSingleFile(): Error...", ex);
        }
    }

    public void writeAllHL7MessagesAsIndividualFile(File basePath, String contentSource, Map<HL7UsefulMetadata, String> msgMap){
        getLogger().debug(".writeAllHL7MessagesAsIndividualFile(): Entry");
        if(basePath == null){
            getLogger().info(".writeAllHL7MessagesAsIndividualFile(): Exit, outputFileNamePrefix is empty");
            return;
        }
        try {
            getLogger().debug(".writeAllHL7MessagesAsIndividualFile(): [Create Output Directory] Start");
            String byMsgIdDirectoryName = "ByMsgId-" + contentSource;
            File byMsgIdDirectory = new File(basePath, byMsgIdDirectoryName);
            if(byMsgIdDirectory.exists()){
                getLogger().debug(".writeAllHL7MessagesAsIndividualFile(): [Create Output Directory] Directory {} already exists", byMsgIdDirectoryName);
            } else {
                byMsgIdDirectory.mkdir();
            }
            getLogger().debug(".writeAllHL7MessagesAsIndividualFile(): [Create Output Directory] Finish");
            for(HL7UsefulMetadata currentMsgId: msgMap.keySet()){
                getLogger().debug(".writeAllHL7MessagesAsIndividualFile(): [Create OutputStream] Start");
                Path fileName = Paths.get(byMsgIdDirectory.getPath(), currentMsgId.getMessageId() + ".hl7");
                getLogger().debug(".writeAllHL7MessagesAsIndividualFile(): [Create OutputStream] Attempting to Create File->{}", fileName);
                FileOutputStream eventStream = new FileOutputStream(fileName.toFile());
                getLogger().debug(".writeAllHL7MessagesAsIndividualFile(): [Create OutputStream] Finish");
                getLogger().debug(".writeAllHL7MessagesAsIndividualFile(): [Write HL7 Message List] Entry");
                String currentMsg = msgMap.get(currentMsgId);
                eventStream.write(currentMsg.getBytes(StandardCharsets.UTF_8));
                eventStream.close();
                getLogger().debug(".writeAllHL7MessagesAsIndividualFile(): [Write HL7 Message List] Finish");
            }

        } catch(Exception ex){
            getLogger().info(".writeAllHL7Messages(): Error...", ex);
        }
    }

    public void writeHL7MessagesAsIndividualFileByMetadata(String basePath, HL7UsefulMetadata metadata, String message){
        getLogger().debug(".writeHL7MessagesAsIndividualFileByMetadata(): Entry");
        if(basePath == null){
            getLogger().debug(".writeHL7MessagesAsIndividualFileByMetadata(): Exit, outputFileNamePrefix is empty");
            return;
        }
        try {
            getLogger().debug(".writeHL7MessagesAsIndividualFileByMetadata(): [Write Individual Files (metadata)] Start");
            getLogger().debug(".writeHL7MessagesAsIndividualFileByMetadata(): [Write Individual Files (metadata)] [Create OutputStream] Start");
            StringBuilder fileNameBuilder = new StringBuilder();
            String subsystemName = metadata.getSubsystem();
            if(StringUtils.isNotEmpty(subsystemName)){
                fileNameBuilder.append(subsystemName).append("-");
            } else {
                fileNameBuilder.append("UnknownSystem").append("-");
            }
            if(metadata.getFlowDirection() != null){
                if(metadata.getFlowDirection().equals(MessageFlowDirectionEnum.INGRES_TO_SUBSYSTEM)){
                    fileNameBuilder.append("Ingres").append("-");
                } else {
                    fileNameBuilder.append("Egress").append("-");
                }
            } else {
                fileNameBuilder.append("UnknownDirection").append("-");
            }
            String portNumber = metadata.getPortNumber();
            if(StringUtils.isNotEmpty(portNumber)){
                fileNameBuilder.append(portNumber).append("-");
            } else {
                fileNameBuilder.append("UnknownEndpoint").append("-");
            }
            if(metadata.isAck()){
                if(StringUtils.isNotEmpty(metadata.getAckCode())){
                    fileNameBuilder.append("AckCode").append("-");
                    fileNameBuilder.append(metadata.getAckCode()).append("-");
                } else {
                    fileNameBuilder.append("Ack").append("-");
                }
            }
            if(StringUtils.isNotEmpty(metadata.getPatientId())) {
                fileNameBuilder.append(metadata.getPatientId()).append("-");
            } else {
                fileNameBuilder.append("NoPatientId").append("-");
            }
            fileNameBuilder.append(metadata.getMessageTimestamp()).append("-");
            fileNameBuilder.append(metadata.getMessageId()).append(".hl7");
            String currentFileName = fileNameBuilder.toString();
            Path fileName = Paths.get(basePath, currentFileName);
            getLogger().debug(".writeHL7MessagesAsIndividualFileByMetadata(): [Write Individual Files (metadata)] [Create OutputStream] File->{}", fileName);
            FileOutputStream eventStream = new FileOutputStream(fileName.toFile());
            getLogger().debug(".writeHL7MessagesAsIndividualFileByMetadata(): [Write Individual Files (metadata)] [Create OutputStream] Finish");
            getLogger().debug(".writeHL7MessagesAsIndividualFileByMetadata(): [Write Individual Files (metadata)] [Write HL7 Message] Start");
            if(StringUtils.isEmpty(message)){
                getLogger().warn(".writeHL7MessagesAsIndividualFileByMetadata(): [Write Individual Files (metadata)] [Write HL7 Message] Something odd, the message itself is empty");
                message = "Unknown";
            }
            eventStream.write(message.getBytes(StandardCharsets.UTF_8));
            getLogger().debug(".writeHL7MessagesAsIndividualFileByMetadata(): [Write Individual Files (metadata)] [Write HL7 Message List] Finish");
            getLogger().debug(".writeHL7MessagesAsIndividualFileByMetadata(): [Write Individual Files (metadata)] [Close OutputStream] Start");
            eventStream.close();
            getLogger().debug(".writeHL7MessagesAsIndividualFileByMetadata(): [Write Individual Files (metadata)] [Close OutputStream] Finish");
            getLogger().debug(".writeHL7MessagesAsIndividualFileByMetadata(): [Write Individual Files (metadata)] Finish");
        } catch(Exception ex){
            getLogger().warn(".writeHL7MessagesAsIndividualFileByMetadata(): Error...", ex);
        }
        getLogger().debug(".writeHL7MessagesAsIndividualFileByMetadata(): Exit");
    }
    public void writeTransactionReport(String systemName, String outputDirectory, Set<HL7TransactionReport> reportSet){
        if(reportSet == null){
            return;
        }
        try {
            String reportName = "TransactionReport-IngresSubsystem-" + systemName + ".csv";
            Path fileName = Paths.get(outputDirectory, reportName);
            getLogger().debug(".writeTransactionReport(): [Create OutputStream] Start, File->{}", fileName);
            FileOutputStream reportStream = new FileOutputStream(fileName.toFile());
            getLogger().debug(".writeTransactionReport(): [Create OutputStream] Finish");
            String reportHeader = "Ingres-SubsystemName, Ingres-Port, Traffic-Direction, Ingres-MessageType, Ingres-MessageId, Ingres-MessageTimestamp, Ingres-PatientId" +
                    ", Egress-SubsystemName, Egress-Port, Traffic-Direction, Egress-MessageType, Egress-MessageId, Egress-MessageTimestamp, Egress-PatientId, Egress-AckCode, Successful-Send\n";
            reportStream.write(reportHeader.getBytes());
            for(HL7TransactionReport currentReport: reportSet) {
                StringBuilder reportLineBuilder = new StringBuilder();
                String ingresMetadataString = createCSVSeparatedIngresHL7MetadataString(currentReport.getIngresMetadata());
                reportLineBuilder.append(ingresMetadataString);
                reportLineBuilder.append(",");
                String egressMetadataString = createCSVSeparatedEgressHL7MetadataString(currentReport.getEgressMetadata());
                reportLineBuilder.append(egressMetadataString);
                reportLineBuilder.append(",");
                if (currentReport.isSuccessfulEgressActivity()) {
                    reportLineBuilder.append("Success");
                } else {
                    reportLineBuilder.append("Fail");
                }
                reportLineBuilder.append("\n");
                String csvReportLine = reportLineBuilder.toString();
                reportStream.write(csvReportLine.getBytes());
            }
            reportStream.close();
        } catch(Exception ex){
            getLogger().warn("Couldn't write report, error->", ex);
        }
    }

    public String createCSVSeparatedIngresHL7MetadataString(HL7UsefulMetadata metadata){
        if(metadata == null){
            HL7UsefulMetadata badMetadata = new HL7UsefulMetadata();
            metadata = badMetadata;
        }
        StringBuilder csvLineBuilder = new StringBuilder();
        if(StringUtils.isNotEmpty(metadata.getSubsystem())){
            csvLineBuilder.append(metadata.getSubsystem());
        }
        csvLineBuilder.append(",");
        if(StringUtils.isNotEmpty(metadata.getPortNumber())){
            csvLineBuilder.append(metadata.getPortNumber());
        }
        csvLineBuilder.append(",");
        if(metadata.getFlowDirection() != null) {
            if (metadata.getFlowDirection().equals(MessageFlowDirectionEnum.INGRES_TO_SUBSYSTEM)) {
                csvLineBuilder.append("Inbound");
            }
        }
        csvLineBuilder.append(",");
        if(StringUtils.isNotEmpty(metadata.getMessageType())){
            csvLineBuilder.append(metadata.getMessageType());
        }
        csvLineBuilder.append(",");
        if(StringUtils.isNotEmpty(metadata.getMessageId())){
            csvLineBuilder.append(metadata.getMessageId());
        }
        csvLineBuilder.append(",");
        if(StringUtils.isNotEmpty(metadata.getMessageTimestamp())){
            csvLineBuilder.append(metadata.getMessageTimestamp());
        }
        csvLineBuilder.append(",");
        if(StringUtils.isNotEmpty(metadata.getPatientId())){
            csvLineBuilder.append(metadata.getPatientId());
        }
        return(csvLineBuilder.toString());
    }

    public String createCSVSeparatedEgressHL7MetadataString(HL7UsefulMetadata metadata) {
        if(metadata == null){
            metadata = new HL7UsefulMetadata();
        }
        StringBuilder egressCSVLineBuilder = new StringBuilder();
        String baseCSVLine = createCSVSeparatedIngresHL7MetadataString(metadata);
        egressCSVLineBuilder.append(baseCSVLine);
        egressCSVLineBuilder.append(",");
        if(StringUtils.isNotEmpty(metadata.getAckCode())){
            egressCSVLineBuilder.append(metadata.getAckCode());
        }
        return(egressCSVLineBuilder.toString());
    }
}
