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

    public HashMap<HL7UsefulMetadata, String> mergeMaps(Map<HL7UsefulMetadata, String> incomingAuditBasedMsgSet, Map<HL7UsefulMetadata, String> incomingLogFileMsgSet){
        if(incomingAuditBasedMsgSet == null){
            LOG.debug(".mergeMaps(): incomingAuditBasedMsgSet is null");
            return(null);
        }
        if(incomingLogFileMsgSet == null ){
            LOG.debug(".mergeMaps(): incomingLogFileMsgSet is null");
            return(null);
        }

        HashMap<HL7UsefulMetadata, String> inOnlyAuditBasedMsgSet = new HashMap<>();
        HashMap<HL7UsefulMetadata, String> inOnlyLogFileMsgSet = new HashMap<>();
        HashMap<HL7UsefulMetadata, String> mergedSet = new HashMap<>();

        // merge same first
        for(HL7UsefulMetadata currentAuditBasedMsg: incomingAuditBasedMsgSet.keySet()){
            boolean isInOtherSet = false;
            for(HL7UsefulMetadata currentLogBasedMsg: incomingLogFileMsgSet.keySet()){
                boolean sameMsgId = currentAuditBasedMsg.getMessageId().contentEquals(currentLogBasedMsg.getMessageId());
                boolean samePatientId = currentAuditBasedMsg.getPatientId().contentEquals(currentLogBasedMsg.getPatientId());
                boolean sameTimestamp = currentAuditBasedMsg.getMessageTimestamp().contentEquals(currentLogBasedMsg.getMessageTimestamp());
                if(sameMsgId && samePatientId && sameTimestamp){
                    HL7UsefulMetadata hl7Metadata = SerializationUtils.clone(currentAuditBasedMsg);
                    if(currentLogBasedMsg.getFlowDirection() != null){
                        hl7Metadata.setFlowDirection(currentLogBasedMsg.getFlowDirection());
                    }
                    if(StringUtils.isNotEmpty(currentLogBasedMsg.getPortNumber())){
                        hl7Metadata.setPortNumber(currentLogBasedMsg.getPortNumber());
                    }
                    String clonedMessage = SerializationUtils.clone(incomingAuditBasedMsgSet.get(currentAuditBasedMsg));
                    mergedSet.put(hl7Metadata, clonedMessage);
                    break;
                }
            }
        }
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
        LOG.debug(".mergeMaps(): inOnlyAuditBasedMsgSet.size()->{}", inOnlyAuditBasedMsgSet.size());
        LOG.debug(".mergeMaps(): inOnlyLogFileMsgSet.size()->{}", inOnlyLogFileMsgSet.size());
        LOG.debug(".mergeMaps(): mergedSet.size()->{}", mergedSet.size());
        return(mergedSet);
    }
}
