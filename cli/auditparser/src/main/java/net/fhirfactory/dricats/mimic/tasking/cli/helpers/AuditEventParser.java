package net.fhirfactory.dricats.mimic.tasking.cli.helpers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import net.fhirfactory.dricats.mimic.tasking.cli.datatypes.HL7UsefulMetadata;
import net.fhirfactory.dricats.mimic.tasking.cli.helpers.common.HL7ContentParserCommon;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AuditEventParser extends HL7ContentParserCommon {
    private static final Logger LOG = LoggerFactory.getLogger(AuditEventParser.class);

    //
    // Constructor(s)
    //

    public AuditEventParser(FhirContext fhirContext){
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

    public ArrayList<AuditEvent> loadEventsFromFile(String fileName){
        LOG.info(".loadMessageFromFile(): Entry");
        ArrayList<AuditEvent> eventList = null;
        try {
            LOG.info(".loadMessageFromFile(): [Create InputStream] Start");
            LOG.info(".loadMessageFromFile(): [Create InputStream] Attempting to Access File->{}", fileName);
            File initialFile = new File(fileName);
            LOG.info(".loadMessageFromFile(): [Create InputStream] Created File Handle...");
            InputStream eventStream = new FileInputStream(initialFile);
            LOG.info(".loadMessageFromFile(): [Create InputStream] Finish");
            LOG.info(".loadMessageFromFile(): [Map InputStream to EventList] Start");
            eventList = mapFileToAuditEventArray(eventStream);
            LOG.info(".loadMessageFromFile(): [Map InputStream to EventList] Finish");
        } catch( Exception ex){
            LOG.info(".loadMessageFromFile(): Problem loading file, error->{}", ExceptionUtils.getMessage(ex));
            ex.printStackTrace();
        }
        LOG.info(".loadMessageFromFile(): Exit");
        return(eventList);
    }

    public ArrayList<AuditEvent> mapFileToAuditEventArray(InputStream fileStream){
        LOG.info(".mapFileToAuditEventArray(): Entry");
        IParser jsonParser = getFHIRContext().newJsonParser();
        Bundle bundle = null;
        ArrayList<AuditEvent> eventList = new ArrayList<>();
        LOG.info(".mapFileToAuditEventArray(): [Convert File To FHIR::Bundle] Start");
        try {
            bundle = jsonParser.parseResource(Bundle.class,fileStream);
        } catch (Exception e) {
            LOG.info(".mapFileToAuditEventArray(): [Convert File To FHIR::Bundle] Finish, Exception");
            LOG.error(".mapDataParcelManifestToString(): Error -->{}", ExceptionUtils.getStackTrace(e));
            return(null);
        }
        if(bundle == null){
            LOG.info(".mapFileToAuditEventArray(): [Convert File To FHIR::Bundle] Finish, returned Bundle is NULL");
            LOG.info(".mapFileToAuditEventArray(): Exit");
            return(null);
        }
        LOG.info(".mapFileToAuditEventArray(): [Convert File To FHIR::Bundle] Finish");
        LOG.info(".mapFileToAuditEventArray(): [Extract FHIR::AuditEvent Entries] Start");
        if(bundle.getTotal() > 0){
            LOG.info(".mapFileToAuditEventArray(): [Extract FHIR::AuditEvent Entries] Number of Entries > 0");
            for(Bundle.BundleEntryComponent currentEntry: bundle.getEntry()){
                Resource currentResource = currentEntry.getResource();
                if(currentResource != null){
                    if(currentResource.getResourceType().equals(ResourceType.AuditEvent)){
                        AuditEvent currentEvent = (AuditEvent)currentResource;
                        eventList.add(currentEvent);
                    }
                }
            }
        }
        LOG.info(".mapFileToAuditEventArray(): [Extract FHIR::AuditEvent Entries] Finish, Number of Entries = {}", eventList.size());
        LOG.info(".mapFileToAuditEventArray(): Exit");
        return(eventList);
    }

    public HashMap<String, String> mapAuditEventArrayToMessageMap(ArrayList<AuditEvent> eventList){
        LOG.info(".mapAuditEventArrayToMessageMap(): Entry");
        if(eventList.isEmpty()){
            LOG.info(".mapAuditEventArrayToMessageMap(): Exit, eventList is empty");
            return(null);
        }
        HashMap<String, String> eventMsgMap = new HashMap<>();
        LOG.info(".mapFileToAuditEventArray(): [Extract HL7 Msg Payload] Start");
        for(AuditEvent currentAuditEvent: eventList){
            String hl7Message = extractHL7MsgIfPresent(currentAuditEvent);
            if(StringUtils.isEmpty(hl7Message)){
                LOG.info(".mapFileToAuditEventArray(): [Extract HL7 Msg Payload] Is Not HL7 Message");
            } else {
                String msgId = extractMessageId(hl7Message);
                if(StringUtils.isEmpty(msgId)){
                    LOG.info(".mapFileToAuditEventArray(): [Extract HL7 Msg Payload] Could not extract Message Control Id");
                } else {
                    eventMsgMap.put(msgId, hl7Message);
                }
            }
        }
        LOG.info(".mapFileToAuditEventArray(): [Extract HL7 Msg Payload] Finish, eventMsgMap.size()->{}", eventMsgMap.size());
        LOG.info(".mapAuditEventArrayToMessageMap(): Exit");
        return(eventMsgMap);
    }

    public HashMap<HL7UsefulMetadata, String> mapAuditEventArrayToMessageByPatientIdMap(ArrayList<AuditEvent> eventList){
        LOG.info(".mapAuditEventArrayToMessageByPatientIdMap(): Entry");
        if(eventList.isEmpty()){
            LOG.info(".mapAuditEventArrayToMessageByPatientIdMap(): Exit, eventList is empty");
            return(null);
        }
        HashMap<HL7UsefulMetadata, String> eventMsgMap = new HashMap<>();
        LOG.info(".mapAuditEventArrayToMessageByPatientIdMap(): [Extract HL7 Msg Payload] Start");
        for(AuditEvent currentAuditEvent: eventList){
            String hl7Message = extractHL7MsgIfPresent(currentAuditEvent);
            if(StringUtils.isEmpty(hl7Message)){
                LOG.info(".mapAuditEventArrayToMessageByPatientIdMap(): [Extract HL7 Msg Payload] Is Not HL7 Message");
            } else {
                HL7UsefulMetadata metadata = extractUsefulHL7Data(hl7Message);
                if(metadata != null){
                    String subsystemName = extractSystem(currentAuditEvent);
                    LOG.debug(".mapAuditEventArrayToMessageByPatientIdMap(): [Extract HL7 Msg Payload] subsystemName->{}", subsystemName);
                    metadata.setSubsystem(subsystemName);
                    eventMsgMap.put(metadata, hl7Message);
                }
            }
        }
        LOG.info(".mapAuditEventArrayToMessageByPatientIdMap(): [Extract HL7 Msg Payload] Finish, eventMsgMap.size()->{}", eventMsgMap.size());
        LOG.info(".mapAuditEventArrayToMessageByPatientIdMap(): Exit");
        return(eventMsgMap);
    }

    public String extractHL7MsgIfPresent(AuditEvent currentAuditEvent){
        if(currentAuditEvent.hasEntity()){
            for(AuditEvent.AuditEventEntityComponent currentEntity: currentAuditEvent.getEntity()){
                if(currentEntity.hasDetail()){
                    for(AuditEvent.AuditEventEntityDetailComponent currentDetail: currentEntity.getDetail()){
                        if(currentDetail.hasValueStringType()){
                            if(currentDetail.getValueStringType().hasValue()){
                                String message = currentDetail.getValueStringType().getValue();
                                if(isHL7Message(message)){
                                    return(message);
                                }
                            }
                        }
                    }
                }
            }
        }
        return(null);
    }




    public void writeAllHL7MessagesToSingleFile(String outputFileNamePrefix, Map<String, String> msgMap){
        LOG.info(".writeAllHL7Messages(): Entry");
        if(StringUtils.isEmpty(outputFileNamePrefix)){
            LOG.info(".writeAllHL7Messages(): Exit, outputFileNamePrefix is empty");
            return;
        }
        try {
            LOG.info(".writeAllHL7Messages(): [Create OutputStream] Start");
            String fileName = outputFileNamePrefix + "-AllMessages.hl7";
            LOG.info(".writeAllHL7Messages(): [Create OutputStream] Attempting to Create File->{}", fileName);
            FileOutputStream eventStream = new FileOutputStream(fileName);
            LOG.info(".writeAllHL7Messages(): [Create OutputStream] Finish");
            LOG.info(".writeAllHL7Messages(): [Write HL7 Message List] Entry");
            for(String currentMsg: msgMap.values()){
                eventStream.write(currentMsg.getBytes(StandardCharsets.UTF_8));
                eventStream.write("\n \n \n".getBytes(StandardCharsets.UTF_8));
            }
            eventStream.close();
            LOG.info(".writeAllHL7Messages(): [Write HL7 Message List] Finish");
        } catch(Exception ex){
            LOG.info(".writeAllHL7Messages(): Error...");
        }
    }

    public void writeAllHL7MessagesAsIndividualFile(String outputFileNamePrefix, Map<String, String> msgMap){
        LOG.info(".writeAllHL7Messages(): Entry");
        if(StringUtils.isEmpty(outputFileNamePrefix)){
            LOG.info(".writeAllHL7Messages(): Exit, outputFileNamePrefix is empty");
            return;
        }
        try {
            LOG.info(".writeAllHL7Messages(): [Create Output Directory] Start");
            String directoryName = outputFileNamePrefix;
            File directory = new File(directoryName);
            String byMsgIdDirectoryName = "ByMsgId";
            File byMsgIdDirectory = new File(directoryName, byMsgIdDirectoryName);
            if(directory.exists()){
                LOG.info(".writeAllHL7Messages(): [Create Output Directory] Directory {} already exists", directory);
            } else {
                directory.mkdir();
            }
            if(byMsgIdDirectory.exists()){
                LOG.info(".writeAllHL7Messages(): [Create Output Directory] Directory {} already exists", byMsgIdDirectoryName);
            } else {
                byMsgIdDirectory.mkdir();
            }
            LOG.info(".writeAllHL7Messages(): [Create Output Directory] Finish");
            for(String currentMsgId: msgMap.keySet()){
                LOG.info(".writeAllHL7Messages(): [Create OutputStream] Start");
                Path fileName = Paths.get(byMsgIdDirectory.getPath(), currentMsgId + ".hl7");
                LOG.info(".writeAllHL7Messages(): [Create OutputStream] Attempting to Create File->{}", fileName);
                FileOutputStream eventStream = new FileOutputStream(fileName.toFile());
                LOG.info(".writeAllHL7Messages(): [Create OutputStream] Finish");
                LOG.info(".writeAllHL7Messages(): [Write HL7 Message List] Entry");
                String currentMsg = msgMap.get(currentMsgId);
                eventStream.write(currentMsg.getBytes(StandardCharsets.UTF_8));
                eventStream.write("\n \n \n".getBytes(StandardCharsets.UTF_8));
                eventStream.close();
            }

            LOG.info(".writeAllHL7Messages(): [Write HL7 Message List] Finish");
        } catch(Exception ex){
            LOG.info(".writeAllHL7Messages(): Error...");
        }
    }


}
