package net.fhirfactory.dricats.mimic.tasking.cli.helpers;

import net.fhirfactory.dricats.mimic.tasking.cli.datatypes.HL7UsefulMetadata;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MessageSetMergerUtilities {
    private static final Logger LOG = LoggerFactory.getLogger(MessageSetMergerUtilities.class);

    public HashMap<HL7UsefulMetadata, String> mergeIngresMaps(Map<HL7UsefulMetadata, String> incomingAuditBasedMsgSet, Map<HL7UsefulMetadata, String> incomingLogFileMsgSet){
        if(incomingAuditBasedMsgSet == null){
            LOG.debug(".mergeIngresMaps(): incomingAuditBasedMsgSet is null");
            return(null);
        }
        if(incomingLogFileMsgSet == null ){
            LOG.debug(".mergeIngresMaps(): incomingLogFileMsgSet is null");
            return(null);
        }

        HashMap<HL7UsefulMetadata, String> inOnlyAuditBasedMsgSet = new HashMap<>();
        HashMap<HL7UsefulMetadata, String> inOnlyLogFileMsgSet = new HashMap<>();
        HashMap<HL7UsefulMetadata, String> mergedSet = new HashMap<>();

        LOG.info(".mergeIngresMaps(): [Create Merged List] Start");
        for(HL7UsefulMetadata currentAuditBasedMsg: incomingAuditBasedMsgSet.keySet()){
            boolean isInOtherSet = false;
            HL7UsefulMetadata hl7Metadata = SerializationUtils.clone(currentAuditBasedMsg);
            String message = SerializationUtils.clone(incomingAuditBasedMsgSet.get(currentAuditBasedMsg));
            HL7UsefulMetadata sameMessage = null;
            for(HL7UsefulMetadata currentLogBasedMsg: incomingLogFileMsgSet.keySet()){
                boolean sameMsgId = currentAuditBasedMsg.getMessageId().contentEquals(currentLogBasedMsg.getMessageId());
                boolean sameTimestamp = currentAuditBasedMsg.getMessageTimestamp().contentEquals(currentLogBasedMsg.getMessageTimestamp());
                if(sameMsgId && sameTimestamp){
                    sameMessage = currentLogBasedMsg;
                    break;
                }
            }
            if(sameMessage != null) {
                if (sameMessage.getFlowDirection() != null) {
                    hl7Metadata.setFlowDirection(sameMessage.getFlowDirection());
                }
                if (StringUtils.isNotEmpty(sameMessage.getPortNumber())) {
                    hl7Metadata.setPortNumber(sameMessage.getPortNumber());
                }
            }
            mergedSet.put(hl7Metadata, message);
        }
        for(HL7UsefulMetadata currentLogBasedMsg: incomingLogFileMsgSet.keySet()){
            boolean alreadyInList = false;
            for(HL7UsefulMetadata currentAuditBasedMsg: incomingAuditBasedMsgSet.keySet()) {
                boolean sameMsgId = currentAuditBasedMsg.getMessageId().contentEquals(currentLogBasedMsg.getMessageId());
                boolean sameTimestamp = currentAuditBasedMsg.getMessageTimestamp().contentEquals(currentLogBasedMsg.getMessageTimestamp());
                if (sameMsgId && sameTimestamp) {
                    alreadyInList = true;
                    break;
                }
            }
            if(!alreadyInList){
                mergedSet.put(SerializationUtils.clone(currentLogBasedMsg), SerializationUtils.clone(incomingAuditBasedMsgSet.get(currentLogBasedMsg)));
            }
        }
        LOG.info(".mergeIngresMaps(): [Create Merged List] Finish");

        for(HL7UsefulMetadata currentAuditBasedMsg: incomingAuditBasedMsgSet.keySet()){
            boolean isInOtherSet = false;
            for(HL7UsefulMetadata currentLogBasedMsg: incomingLogFileMsgSet.keySet()){
                boolean sameMsgId = currentAuditBasedMsg.getMessageId().contentEquals(currentLogBasedMsg.getMessageId());
                boolean samePatientId = currentAuditBasedMsg.getPatientId().contentEquals(currentLogBasedMsg.getPatientId());
                boolean sameTimestamp = currentAuditBasedMsg.getMessageTimestamp().contentEquals(currentLogBasedMsg.getMessageTimestamp());
                if(sameMsgId && samePatientId && sameTimestamp){
                    isInOtherSet = true;
                    break;
                }
            }
            if(!isInOtherSet){
                inOnlyAuditBasedMsgSet.put(currentAuditBasedMsg, incomingAuditBasedMsgSet.get(currentAuditBasedMsg));
            }
        }
        for(HL7UsefulMetadata currentLogBasedMsg: incomingLogFileMsgSet.keySet()){
            boolean isInOtherSet = false;
            for(HL7UsefulMetadata currentAuditBasedMsg: incomingAuditBasedMsgSet.keySet()){
                boolean sameMsgId = currentLogBasedMsg.getMessageId().contentEquals(currentAuditBasedMsg.getMessageId());
                boolean samePatientId = currentLogBasedMsg.getPatientId().contentEquals(currentAuditBasedMsg.getPatientId());
                boolean sameTimestamp = currentLogBasedMsg.getMessageTimestamp().contentEquals(currentAuditBasedMsg.getMessageTimestamp());
                if(sameMsgId && samePatientId && sameTimestamp){
                    isInOtherSet = true;
                    break;
                }
            }
            if(!isInOtherSet){
                inOnlyLogFileMsgSet.put(currentLogBasedMsg, incomingAuditBasedMsgSet.get(currentLogBasedMsg));
            }
        }
        LOG.info(".mergeIngresMaps(): inOnlyAuditBasedMsgSet.size()->{}", inOnlyAuditBasedMsgSet.size());
        LOG.info(".mergeIngresMaps(): inOnlyLogFileMsgSet.size()->{}", inOnlyLogFileMsgSet.size());
        LOG.info(".mergeIngresMaps(): mergedSet.size()->{}", mergedSet.size());
        return(mergedSet);
    }
    public HashMap<HL7UsefulMetadata, String> mergeEgressMaps(Map<HL7UsefulMetadata, String> incomingAuditBasedMsgSet, Map<HL7UsefulMetadata, String> incomingLogFileMsgSet){
        if(incomingAuditBasedMsgSet == null){
            LOG.debug(".mergeEgressMaps(): incomingAuditBasedMsgSet is null");
            return(null);
        }
        if(incomingLogFileMsgSet == null ){
            LOG.debug(".mergeEgressMaps(): incomingLogFileMsgSet is null");
            return(null);
        }

        HashMap<HL7UsefulMetadata, String> inOnlyAuditBasedMsgSet = new HashMap<>();
        HashMap<HL7UsefulMetadata, String> inOnlyLogFileMsgSet = new HashMap<>();
        HashMap<HL7UsefulMetadata, String> mergedSet = new HashMap<>();

        // merge same first
        LOG.info(".mergeEgressMaps(): [Create Merged List] Start");
        for(HL7UsefulMetadata currentAuditBasedMsg: incomingAuditBasedMsgSet.keySet()){
            boolean isInOtherSet = false;
            HL7UsefulMetadata hl7Metadata = SerializationUtils.clone(currentAuditBasedMsg);
            String message = SerializationUtils.clone(incomingAuditBasedMsgSet.get(currentAuditBasedMsg));
            HL7UsefulMetadata sameMessage = null;
            for(HL7UsefulMetadata currentLogBasedMsg: incomingLogFileMsgSet.keySet()){
                boolean sameMsgId = currentAuditBasedMsg.getMessageId().contentEquals(currentLogBasedMsg.getMessageId());
                boolean sameTimestamp = currentAuditBasedMsg.getMessageTimestamp().contentEquals(currentLogBasedMsg.getMessageTimestamp());
                if(sameMsgId && sameTimestamp){
                    sameMessage = currentAuditBasedMsg;
                    break;
                }
            }
            if(sameMessage != null) {
                if (sameMessage.getFlowDirection() != null) {
                    hl7Metadata.setFlowDirection(sameMessage.getFlowDirection());
                }
                if (StringUtils.isNotEmpty(sameMessage.getPortNumber())) {
                    hl7Metadata.setPortNumber(sameMessage.getPortNumber());
                }
                if (StringUtils.isNotEmpty(sameMessage.getSubsystem())) {
                    hl7Metadata.setSubsystem(sameMessage.getSubsystem());
                }
                if (StringUtils.isNotEmpty(sameMessage.getAckCode())) {
                    hl7Metadata.setAckCode(sameMessage.getAckCode());
                }
            }
            mergedSet.put(hl7Metadata, message);
        }
        for(HL7UsefulMetadata currentLogBasedMsg: incomingLogFileMsgSet.keySet()){
            boolean alreadyInList = false;
            for(HL7UsefulMetadata currentAuditBasedMsg: incomingAuditBasedMsgSet.keySet()) {
                boolean sameMsgId = currentAuditBasedMsg.getMessageId().contentEquals(currentLogBasedMsg.getMessageId());
                boolean sameTimestamp = currentAuditBasedMsg.getMessageTimestamp().contentEquals(currentLogBasedMsg.getMessageTimestamp());
                if (sameMsgId && sameTimestamp) {
                    alreadyInList = true;
                    break;
                }
            }
            if(!alreadyInList){
                mergedSet.put(SerializationUtils.clone(currentLogBasedMsg), SerializationUtils.clone(incomingAuditBasedMsgSet.get(currentLogBasedMsg)));
            }
        }
        LOG.info(".mergeEgressMaps(): [Create Merged List] Finish");

        for(HL7UsefulMetadata currentAuditBasedMsg: incomingAuditBasedMsgSet.keySet()){
            boolean isInOtherSet = false;
            for(HL7UsefulMetadata currentLogBasedMsg: incomingLogFileMsgSet.keySet()){
                boolean sameMsgId = currentAuditBasedMsg.getMessageId().contentEquals(currentLogBasedMsg.getMessageId());
                boolean sameTimestamp = currentAuditBasedMsg.getMessageTimestamp().contentEquals(currentLogBasedMsg.getMessageTimestamp());
                if(sameMsgId && sameTimestamp){
                    isInOtherSet = true;
                    break;
                }
            }
            if(!isInOtherSet){
                inOnlyAuditBasedMsgSet.put(currentAuditBasedMsg, incomingAuditBasedMsgSet.get(currentAuditBasedMsg));
            }
        }
        for(HL7UsefulMetadata currentLogBasedMsg: incomingLogFileMsgSet.keySet()){
            boolean isInOtherSet = false;
            for(HL7UsefulMetadata currentAuditBasedMsg: incomingAuditBasedMsgSet.keySet()){
                boolean sameMsgId = currentLogBasedMsg.getMessageId().contentEquals(currentAuditBasedMsg.getMessageId());
                boolean sameTimestamp = currentLogBasedMsg.getMessageTimestamp().contentEquals(currentAuditBasedMsg.getMessageTimestamp());
                if(sameMsgId && sameTimestamp){
                    isInOtherSet = true;
                    break;
                }
            }
            if(!isInOtherSet){
                inOnlyLogFileMsgSet.put(currentLogBasedMsg, incomingAuditBasedMsgSet.get(currentLogBasedMsg));
            }
        }
        LOG.info(".mergeEgressMaps(): inOnlyAuditBasedMsgSet.size()->{}", inOnlyAuditBasedMsgSet.size());
        LOG.info(".mergeEgressMaps(): inOnlyLogFileMsgSet.size()->{}", inOnlyLogFileMsgSet.size());
        LOG.info(".mergeEgressMaps(): mergedSet.size()->{}", mergedSet.size());
        return(mergedSet);
    }

}
