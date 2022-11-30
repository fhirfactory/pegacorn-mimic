package net.fhirfactory.dricats.mimic.tasking.cli.helpers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import net.fhirfactory.dricats.mimic.tasking.cli.datatypes.HL7UsefulMetadata;
import net.fhirfactory.dricats.mimic.tasking.cli.helpers.common.HL7ContentParserCommon;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DRICaTSAuditEventParser extends HL7ContentParserCommon {
    private static final Logger LOG = LoggerFactory.getLogger(DRICaTSAuditEventParser.class);

    //
    // Constructor(s)
    //

    public DRICaTSAuditEventParser(FhirContext fhirContext){
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

    public Set<String> getFilenamesFromDirectory(String directoryName) throws IOException {
        try (Stream<Path> stream = Files.walk(Paths.get(directoryName), 3)) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toSet());
        }
    }

    public String findMessageInIngresAuditEventSet(String ingresAuditEventDirectory, String messageId, String timestamp){
        LOG.info(".findMessageInIngresAuditEventSet(): Entry, ingresAuditEventDirectory->{}, messageId->{}, timestamp->{}", ingresAuditEventDirectory, messageId, timestamp);
        String message = null;
        try {
            LOG.info(".findMessageInIngresAuditEventSet(): [Get Ingres AuditEvent File List] Start");
            Set<String> ingresLogFileList = null;
            try {
                ingresLogFileList = getFilenamesFromDirectory(ingresAuditEventDirectory);
            } catch(Exception ex){
                LOG.error(".findMessageInIngresAuditEventSet[Get Ingres AuditEvent File List] Problem ->", ex);
                System.exit(0);
            }
            LOG.info(".findMessageInIngresAuditEventSet(): [Get Ingres AuditEvent File List] Finish");
            LOG.info(".findMessageInIngresAuditEventSet(): [Trawl Through AuditEvent List] Start");
            for(String currentAuditEventFile: ingresLogFileList){
                LOG.info(".findMessageInIngresAuditEventSet(): [Create InputStream] Start");
                LOG.info(".findMessageInIngresAuditEventSet(): [Create InputStream] Attempting to Access File->{}", currentAuditEventFile);
                File currentFile = new File(ingresAuditEventDirectory, currentAuditEventFile);
                LOG.debug(".findMessageInIngresAuditEventSet(): [Create InputStream] Created File Handle...");
                InputStream eventStream = new FileInputStream(currentFile);
                LOG.info(".findMessageInIngresAuditEventSet(): [Create InputStream] Finish");
                LOG.info(".findMessageInIngresAuditEventSet(): [Parse File For AuditEvent] Start");
                message = parseFileForMessage(eventStream, messageId, timestamp);
                if(StringUtils.isNotEmpty(message)){
                    break;
                }
                LOG.info(".findMessageInIngresAuditEventSet(): [Parse File For AuditEvent] Finish");
            }
        } catch( Exception ex){
            LOG.warn(".findMessageInIngresAuditEventSet(): Problem loading file, error...", ex);
            ex.printStackTrace();
        }
        LOG.debug(".findMessageInIngresAuditEventSet(): Exit");
        return(message);
    }

    public String parseFileForMessage(InputStream fileStream, String messageId, String timestamp){
        LOG.debug(".mapFileToAuditEventArray(): Entry, messageId->{}, timestamp->{}", messageId, timestamp);
        IParser jsonParser = getFHIRContext().newJsonParser();
        Bundle bundle = null;
        ArrayList<AuditEvent> eventList = new ArrayList<>();
        LOG.debug(".mapFileToAuditEventArray(): [Convert File To FHIR::Bundle] Start");
        try {
            bundle = jsonParser.parseResource(Bundle.class,fileStream);
        } catch (Exception e) {
            LOG.debug(".mapFileToAuditEventArray(): [Convert File To FHIR::Bundle] Finish, Exception");
            LOG.error(".mapDataParcelManifestToString(): Error...", e);
            return(null);
        }
        if(bundle == null){
            LOG.debug(".mapFileToAuditEventArray(): [Convert File To FHIR::Bundle] Finish, returned Bundle is NULL");
            LOG.info(".mapFileToAuditEventArray(): Exit");
            return(null);
        }
        LOG.debug(".mapFileToAuditEventArray(): [Convert File To FHIR::Bundle] Finish");
        LOG.debug(".mapFileToAuditEventArray(): [Extract FHIR::AuditEvent Entries] Start");
        if(bundle.getTotal() > 0){
            LOG.debug(".mapFileToAuditEventArray(): [Extract FHIR::AuditEvent Entries] Number of Entries > 0");
            for(Bundle.BundleEntryComponent currentEntry: bundle.getEntry()){
                Resource currentResource = currentEntry.getResource();
                if(currentResource != null){
                    if(currentResource.getResourceType().equals(ResourceType.AuditEvent)){
                        AuditEvent currentEvent = (AuditEvent)currentResource;
                        String requiredMessage = checkPayload(currentEvent, messageId, timestamp);
                        if(StringUtils.isNotEmpty(requiredMessage)){
                            return(requiredMessage);
                        }
                    }
                }
            }
        }
        LOG.debug(".mapFileToAuditEventArray(): Exit");
        return(null);
    }

    public String checkPayload(AuditEvent auditEvent, String messageId, String timeStamp){
        LOG.debug(".checkPayload(): Entry");
        if(auditEvent == null){
            LOG.info(".checkPayload(): Exit, eventList is empty");
            return(null);
        }
        LOG.debug(".checkPayload(): [Extract HL7 Msg Payload] Start");
        String hl7Message = extractHL7MsgIfPresent(auditEvent);
        if(StringUtils.isEmpty(hl7Message)){
            LOG.debug(".checkPayload(): [Extract HL7 Msg Payload] Is Not HL7 Message");
        } else {
            String auditEventMessageId = extractMessageId(hl7Message);
            String auditEventMessageTimestamp = extractMessageTimestamp(hl7Message);
            LOG.info(".checkPayload(): [Extract HL7 Msg Payload] auditEventMessageId->{}, auditEventMessageTimestamp->{}", auditEventMessageId, auditEventMessageTimestamp);
            if(StringUtils.isEmpty(auditEventMessageId) || StringUtils.isEmpty(auditEventMessageTimestamp)){
                LOG.debug(".checkPayload(): [Extract HL7 Msg Payload] Could not extract Message Control Id");
                return(null);
            } else {
                if(messageId.contentEquals(auditEventMessageId) && timeStamp.contentEquals(auditEventMessageTimestamp)){
                    return(hl7Message);
                }
            }
        }
        LOG.debug(".checkPayload(): Exit");
        return(null);
    }

    public ArrayList<AuditEvent> loadEventsFromFile(String fileName){
        LOG.debug(".loadMessageFromFile(): Entry");
        ArrayList<AuditEvent> eventList = null;
        try {
            LOG.info(".loadMessageFromFile(): [Create InputStream] Start");
            LOG.info(".loadMessageFromFile(): [Create InputStream] Attempting to Access File->{}", fileName);
            File initialFile = new File(fileName);
            LOG.debug(".loadMessageFromFile(): [Create InputStream] Created File Handle...");
            InputStream eventStream = new FileInputStream(initialFile);
            LOG.info(".loadMessageFromFile(): [Create InputStream] Finish");
            LOG.info(".loadMessageFromFile(): [Map InputStream to EventList] Start");
            eventList = mapFileToAuditEventArray(eventStream);
            LOG.info(".loadMessageFromFile(): [Map InputStream to EventList] Finish");
        } catch( Exception ex){
            LOG.warn(".loadMessageFromFile(): Problem loading file, error...", ex);
            ex.printStackTrace();
        }
        LOG.debug(".loadMessageFromFile(): Exit");
        return(eventList);
    }

    public ArrayList<AuditEvent> mapFileToAuditEventArray(InputStream fileStream){
        LOG.debug(".mapFileToAuditEventArray(): Entry");
        IParser jsonParser = getFHIRContext().newJsonParser();
        Bundle bundle = null;
        ArrayList<AuditEvent> eventList = new ArrayList<>();
        LOG.info(".mapFileToAuditEventArray(): [Convert File To FHIR::Bundle] Start");
        try {
            bundle = jsonParser.parseResource(Bundle.class,fileStream);
        } catch (Exception e) {
            LOG.debug(".mapFileToAuditEventArray(): [Convert File To FHIR::Bundle] Finish, Exception");
            LOG.error(".mapDataParcelManifestToString(): Error...", e);
            return(null);
        }
        if(bundle == null){
            LOG.debug(".mapFileToAuditEventArray(): [Convert File To FHIR::Bundle] Finish, returned Bundle is NULL");
            LOG.info(".mapFileToAuditEventArray(): Exit");
            return(null);
        }
        LOG.info(".mapFileToAuditEventArray(): [Convert File To FHIR::Bundle] Finish");
        LOG.info(".mapFileToAuditEventArray(): [Extract FHIR::AuditEvent Entries] Start");
        if(bundle.getTotal() > 0){
            LOG.debug(".mapFileToAuditEventArray(): [Extract FHIR::AuditEvent Entries] Number of Entries > 0");
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
        LOG.debug(".mapFileToAuditEventArray(): Exit");
        return(eventList);
    }

    public HashMap<String, String> mapAuditEventArrayToMessageMap(ArrayList<AuditEvent> eventList){
        LOG.info(".mapAuditEventArrayToMessageMap(): Entry");
        if(eventList.isEmpty()){
            LOG.info(".mapAuditEventArrayToMessageMap(): Exit, eventList is empty");
            return(null);
        }
        HashMap<String, String> eventMsgMap = new HashMap<>();
        LOG.info(".mapAuditEventArrayToMessageMap(): [Extract HL7 Msg Payload] Start");
        for(AuditEvent currentAuditEvent: eventList){
            String hl7Message = extractHL7MsgIfPresent(currentAuditEvent);
            if(StringUtils.isEmpty(hl7Message)){
                LOG.info(".mapAuditEventArrayToMessageMap(): [Extract HL7 Msg Payload] Is Not HL7 Message");
            } else {
                String msgId = extractMessageId(hl7Message);
                if(StringUtils.isEmpty(msgId)){
                    LOG.info(".mapAuditEventArrayToMessageMap(): [Extract HL7 Msg Payload] Could not extract Message Control Id");
                } else {
                    eventMsgMap.put(msgId, hl7Message);
                }
            }
        }
        LOG.info(".mapAuditEventArrayToMessageMap(): [Extract HL7 Msg Payload] Finish, eventMsgMap.size()->{}", eventMsgMap.size());
        LOG.info(".mapAuditEventArrayToMessageMap(): Exit");
        return(eventMsgMap);
    }

    public HashMap<HL7UsefulMetadata, String> mapAuditEventArrayToMetadataBasedMap(ArrayList<AuditEvent> eventList){
        LOG.debug(".mapAuditEventArrayToMessageByPatientIdMap(): Entry");
        if(eventList.isEmpty()){
            LOG.debug(".mapAuditEventArrayToMessageByPatientIdMap(): Exit, eventList is empty");
            return(null);
        }
        HashMap<HL7UsefulMetadata, String> eventMsgMap = new HashMap<>();
        LOG.info(".mapAuditEventArrayToMessageByPatientIdMap(): [Extract HL7 Msg Payload] Start");
        for(AuditEvent currentAuditEvent: eventList){
            String hl7Message = extractHL7MsgIfPresent(currentAuditEvent);
            if(StringUtils.isEmpty(hl7Message)){
                LOG.debug(".mapAuditEventArrayToMessageByPatientIdMap(): [Extract HL7 Msg Payload] Is Not HL7 Message");
            } else {
                HL7UsefulMetadata metadata = extractUsefulHL7Data(hl7Message);
                if(metadata != null){
                    String subsystemName = extractSystem(currentAuditEvent);
                    metadata.setSubsystem(subsystemName);
                    LOG.info(".mapAuditEventArrayToMessageByPatientIdMap(): [Extract HL7 Msg Payload] Adding Message to Set, metadata->{}", metadata);
                    eventMsgMap.put(metadata, hl7Message);
                }
            }
        }
        LOG.info(".mapAuditEventArrayToMessageByPatientIdMap(): [Extract HL7 Msg Payload] Finish, eventMsgMap.size()->{}", eventMsgMap.size());
        LOG.debug(".mapAuditEventArrayToMessageByPatientIdMap(): Exit");
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





}
