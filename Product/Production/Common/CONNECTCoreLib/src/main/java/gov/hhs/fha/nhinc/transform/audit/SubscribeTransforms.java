/*
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services. 
 * All rights reserved. 
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met: 
 *     * Redistributions of source code must retain the above 
 *       copyright notice, this list of conditions and the following disclaimer. 
 *     * Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the documentation 
 *       and/or other materials provided with the distribution. 
 *     * Neither the name of the United States Government nor the 
 *       names of its contributors may be used to endorse or promote products 
 *       derived from this software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY 
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package gov.hhs.fha.nhinc.transform.audit;

import com.services.nhinc.schema.auditmessage.AuditMessageType;
import com.services.nhinc.schema.auditmessage.AuditSourceIdentificationType;
import com.services.nhinc.schema.auditmessage.CodedValueType;
import com.services.nhinc.schema.auditmessage.ParticipantObjectIdentificationType;
import com.services.nhinc.schema.auditmessage.AuditMessageType.ActiveParticipant;

import gov.hhs.fha.nhinc.common.auditlog.LogEventRequestType;
import gov.hhs.fha.nhinc.common.hiemauditlog.LogNhinSubscribeRequestType;
import gov.hhs.fha.nhinc.common.hiemauditlog.LogSubscribeResponseType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.UserType;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.transform.marshallers.JAXBContextHandler;
import gov.hhs.fha.nhinc.util.format.PatientIdFormatUtil;

import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

import oasis.names.tc.ebxml_regrep.xsd.rim._3.AdhocQueryType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.SlotType1;

import org.apache.log4j.Logger;
import org.oasis_open.docs.wsn.b_2.Subscribe;

/**
 * Transforms for subscribe messages.
 *
 * @author webbn
 *
 * @author rhaslam - Updated to match PD, DQ, DR, and XDR changes for 2013 Specification
 * This implementation (AdminDistTransforms) was not matched to any specificaion
 */
public class SubscribeTransforms {

    private static final Logger LOG = Logger.getLogger(SubscribeTransforms.class);
    private static final String SLOT_NAME_PATIENT_ID = "$XDSDocumentEntryPatientId";

    public LogEventRequestType transformNhinSubscribeRequestToAuditMessage(LogNhinSubscribeRequestType message) {
        LogEventRequestType response = new LogEventRequestType();
        AuditMessageType auditMsg = new AuditMessageType();


        LOG.info("******************************************************************");
        LOG.info("Entering transformNhinSubscribeRequestToAuditMessage() method.");
        LOG.info("******************************************************************");
        
        if (message == null || message.getDirection() == null || message.getInterface() == null) {
        	LOG.error("SubscribeTransforms.transformNhinSubscribeRequestToAuditMessage() has insufficient data to log a message audfit");
        	return null;
        }
        String _interface = message.getInterface();
        String direction = message.getDirection();
        response.setDirection(direction);
        response.setInterface(_interface);

        // Extract UserInfo and Assertion from the message
        UserType userInfo = null;
        AssertionType assertion = null;
        if (message.getMessage() != null && message.getMessage().getAssertion() != null) {
        	assertion = message.getMessage().getAssertion();
        	if (assertion.getUserInfo() != null) {
        		userInfo = assertion.getUserInfo();
        	}
        }

        // Create EventIdentification
        CodedValueType eventID = null;
        eventID = AuditDataTransformHelper.createCodedValue(
        	AuditDataTransformConstants.EVENT_ID_CODE_SYS_NAME_SUB,
            AuditDataTransformConstants.EVENT_ID_DISPLAY_NAME_SUBSCRIBE,
            AuditDataTransformConstants.EVENT_ID_CODE_SYS_NAME_SUB,
            AuditDataTransformConstants.EVENT_ID_DISPLAY_NAME_SUBSCRIBE,
            null);
        
        auditMsg.setEventIdentification(AuditDataTransformHelper.createEventIdentification(
            AuditDataTransformConstants.EVENT_ACTION_CODE_CREATE,
            AuditDataTransformConstants.EVENT_OUTCOME_INDICATOR_SUCCESS, eventID, null));

        // Create Active Participant Section
        boolean isSender = AuditDataTransformHelper.isSender(_interface, direction);
        boolean isRecipient = isSender ? false : true;
       
        if (userInfo != null) {
            ActiveParticipant participant = AuditDataTransformHelper.createActiveParticipantForHuman(userInfo);
            auditMsg.getActiveParticipant().add(participant);
        }

        ActiveParticipant source = AuditDataTransformHelper.createActiveParticipantSource(isSender, null);
        auditMsg.getActiveParticipant().add(source);
        
        ActiveParticipant destination = AuditDataTransformHelper.createActiveParticipantDestination(assertion.getSamlAuthnStatement(), isRecipient);
        auditMsg.getActiveParticipant().add(destination);

        /* Assign AuditSourceIdentification */
        String communityId = "";
        String communityName = "";
        String patientId = "";

        if ((message != null) && (message.getMessage() != null) && (message.getMessage().getSubscribe() != null)) {
            PatientInfo patientInfo = extractPatientInfo(message.getMessage().getSubscribe());

            if (patientInfo != null) {
                communityId = patientInfo.getCommunityId();
                communityName = patientInfo.getCommunityName();
                patientId = patientInfo.getPatientId();
            }
        }

        if (userInfo != null && userInfo.getOrg() != null) {
            if (userInfo.getOrg().getHomeCommunityId() != null) {
                communityId = userInfo.getOrg().getHomeCommunityId();
            }
            if (userInfo.getOrg().getName() != null) {
                communityName = userInfo.getOrg().getName();
            }
        }

        AuditSourceIdentificationType auditSource = AuditDataTransformHelper.createAuditSourceIdentification(communityId, communityName, null);
        auditMsg.getAuditSourceIdentification().add(auditSource);

        /* Assign ParticipationObjectIdentification */
        /**
         * Create a ParticipantObjectIdentification entry for Patient
         */
        ParticipantObjectIdentificationType partObjId = null;
        if ((patientId != null) && (! patientId.isEmpty())) {
 	        CodedValueType partObjectIdType = AuditDataTransformHelper.getPatientParticipantRoleIdCodedValue();
 	
 	        // Participant Object Identification Entry $1 Patient
 	        partObjId = AuditDataTransformHelper.createParticipantObjectIdentification(
 	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_PERSON,
 	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_PATIENT,
 	        		partObjectIdType,
 	        		patientId,
 	        		null);
 	
 	        auditMsg.getParticipantObjectIdentification().add(partObjId);
        }

        // TODO: Illegal piggybacking of message in ParticipantObjectIdentification.ObjectQuery
        if (partObjId == null) {
        	partObjId = new ParticipantObjectIdentificationType();
        }

        // Fill in the message field with the contents of the event message
        try {
            JAXBContextHandler oHandler = new JAXBContextHandler();
            JAXBContext jc = oHandler.getJAXBContext(org.oasis_open.docs.wsn.b_2.ObjectFactory.class,
                oasis.names.tc.ebxml_regrep.xsd.rim._3.ObjectFactory.class);
            Marshaller marshaller = jc.createMarshaller();
            ByteArrayOutputStream baOutStrm = new ByteArrayOutputStream();
            baOutStrm.reset();
            if (message != null) {
                marshaller.marshal(message.getMessage().getSubscribe(), baOutStrm);
            }
            LOG.debug("Done marshalling the message.");

            partObjId.setParticipantObjectQuery(baOutStrm.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("EXCEPTION when marshalling subscribe request: " + e);
            throw new RuntimeException();
        }

        response.setAuditMessage(auditMsg);

        LOG.info("******************************************************************");
        LOG.info("Exiting transformNhinSubscribeRequestToAuditMessage() method.");
        LOG.info("******************************************************************");

        return response;
    }

    public LogEventRequestType transformSubscribeResponseToAuditMessage(LogSubscribeResponseType message) {
        LogEventRequestType response = new LogEventRequestType();
        AuditMessageType auditMsg = new AuditMessageType();

        LOG.info("******************************************************************");
        LOG.info("Entering transformSubscribeResponseToAuditMessage() method.");
        LOG.info("******************************************************************");
        
        if (message == null || message.getDirection() == null || message.getInterface() == null) {
        	LOG.error("SubscribeTransforms.transformSubscribeResponseToAuditMessage() has insufficient data to log a message audfit");
        	return null;
        }
        String _interface = message.getInterface();
        String direction = message.getDirection();
        response.setDirection(direction);
        response.setInterface(_interface);

        // Extract UserInfo and Assertion from the message
        UserType userInfo = null;
        AssertionType assertion = null;
        if (message.getMessage() != null && message.getMessage().getAssertion() != null) {
        	assertion = message.getMessage().getAssertion();
        	if (assertion.getUserInfo() != null) {
        		userInfo = assertion.getUserInfo();
        	}
        }

        // Create EventIdentification
        CodedValueType eventID = null;
        eventID = AuditDataTransformHelper.createCodedValue(
        	AuditDataTransformConstants.EVENT_ID_CODE_SYS_NAME_SUB,
            AuditDataTransformConstants.EVENT_ID_DISPLAY_NAME_SUBSCRIBE,
            AuditDataTransformConstants.EVENT_ID_CODE_SYS_NAME_SUB,
            AuditDataTransformConstants.EVENT_ID_DISPLAY_NAME_SUBSCRIBE,
            null);
        
        auditMsg.setEventIdentification(AuditDataTransformHelper.createEventIdentification(
            AuditDataTransformConstants.EVENT_ACTION_CODE_CREATE,
            AuditDataTransformConstants.EVENT_OUTCOME_INDICATOR_SUCCESS, eventID, null));

        // Create Active Participant Section
        boolean isSender = AuditDataTransformHelper.isSender(_interface, direction);
        boolean isRecipient = isSender ? false : true;
       
        if (userInfo != null) {
            ActiveParticipant participant = AuditDataTransformHelper.createActiveParticipantForHuman(userInfo);
            auditMsg.getActiveParticipant().add(participant);
        }

        ActiveParticipant source = AuditDataTransformHelper.createActiveParticipantSource(isSender, null);
        auditMsg.getActiveParticipant().add(source);
        
        ActiveParticipant destination = AuditDataTransformHelper.createActiveParticipantDestination(assertion.getSamlAuthnStatement(), isRecipient);
        auditMsg.getActiveParticipant().add(destination);

        String communityId = "";
        String communityName = "";
        if (userInfo != null && userInfo.getOrg() != null) {
            if (userInfo.getOrg().getHomeCommunityId() != null) {
                communityId = userInfo.getOrg().getHomeCommunityId();
            }
            if (userInfo.getOrg().getName() != null) {
                communityName = userInfo.getOrg().getName();
            }
        }

        AuditSourceIdentificationType auditSource = AuditDataTransformHelper.createAuditSourceIdentification(communityId, communityName, null);
        auditMsg.getAuditSourceIdentification().add(auditSource);

        String patientId = "unknown";
        /* Assign ParticipationObjectIdentification */
        /**
         * Create a ParticipantObjectIdentification entry for Patient
         */
        ParticipantObjectIdentificationType partObjId = null;
        if ((patientId != null) && (! patientId.isEmpty())) {
 	        CodedValueType partObjectIdType = AuditDataTransformHelper.getPatientParticipantRoleIdCodedValue();
 	
 	        // Participant Object Identification Entry $1 Patient
 	        partObjId = AuditDataTransformHelper.createParticipantObjectIdentification(
 	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_PERSON,
 	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_PATIENT,
 	        		partObjectIdType,
 	        		patientId,
 	        		null);
 	
 	        auditMsg.getParticipantObjectIdentification().add(partObjId);
        }

        // TODO: Illegal piggybacking of message in ParticipantObjectIdentification.ObjectQuery
        boolean addDone = true;
        if (partObjId == null) {
        	partObjId = new ParticipantObjectIdentificationType();
        	addDone = false;
        }

        // Fill in the message field with the contents of the event message
        try {
            JAXBContextHandler oHandler = new JAXBContextHandler();
            JAXBContext jc = oHandler.getJAXBContext("org.oasis_open.docs.wsn.b_2");
            Marshaller marshaller = jc.createMarshaller();
            ByteArrayOutputStream baOutStrm = new ByteArrayOutputStream();
            baOutStrm.reset();
            if (message != null) {
                marshaller.marshal(message.getMessage().getSubscribeResponse(), baOutStrm);
            }
            LOG.debug("Done marshalling the message.");

            partObjId.setParticipantObjectQuery(baOutStrm.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("EXCEPTION when marshalling subscribe response: " + e);
            throw new RuntimeException();
        }
        if (! addDone){
 	        auditMsg.getParticipantObjectIdentification().add(partObjId);
        }
        response.setAuditMessage(auditMsg);

        LOG.info("******************************************************************");
        LOG.info("Exiting transformSubscribeResponseToAuditMessage() method.");
        LOG.info("******************************************************************");

        return response;
    }

    private PatientInfo extractPatientInfo(Subscribe subscribe) {
        PatientInfo patientInfo = new PatientInfo();
        if (subscribe != null) {
            AdhocQueryType adhocQuery = getAdhocQuery(subscribe);
            if (adhocQuery != null) {
                String formattedPatientId = getFormattedPatientId(adhocQuery);
                if (NullChecker.isNotNullish(formattedPatientId)) {
                    patientInfo.setPatientId(PatientIdFormatUtil.parsePatientId(formattedPatientId));
                    patientInfo.setCommunityId(PatientIdFormatUtil.parseCommunityId(formattedPatientId));
                }
            }
        }
        return patientInfo;
    }

    private AdhocQueryType getAdhocQuery(Subscribe nhinSubscribe) {
        AdhocQueryType adhocQuery = null;
        LOG.info("begin getAdhocQuery");
        List<Object> any = nhinSubscribe.getAny();
        LOG.info("found " + any.size() + " any item(s)");

        for (Object anyItem : any) {
            LOG.info("anyItem=" + anyItem);
            if (anyItem instanceof oasis.names.tc.ebxml_regrep.xsd.rim._3.AdhocQueryType) {
                adhocQuery = (AdhocQueryType) anyItem;
            }
            if (anyItem instanceof JAXBElement) {
                LOG.info("jaxbelement.getValue=" + ((JAXBElement) anyItem).getValue());
                if (((JAXBElement) anyItem).getValue() instanceof AdhocQueryType) {
                    adhocQuery = (AdhocQueryType) ((JAXBElement) anyItem).getValue();
                } else {
                    LOG.warn("unhandled anyitem jaxbelement value " + ((JAXBElement) anyItem).getValue());
                }
            } else {
                LOG.warn("unhandled anyitem " + anyItem);
            }
        }
        LOG.info("end getAdhocQuery");
        return adhocQuery;
    }

    private String getFormattedPatientId(AdhocQueryType adhocQuery) {
        String formattedPatientId = null;
        List<SlotType1> slots = adhocQuery.getSlot();
        if ((slots != null) && !(slots.isEmpty())) {
            for (SlotType1 slot : slots) {
                if (SLOT_NAME_PATIENT_ID.equals(slot.getName())) {
                    if (slot.getValueList() != null) {
                        List<String> slotValues = slot.getValueList().getValue();
                        for (String value : slotValues) {
                            if (NullChecker.isNotNullish(value)) {
                                formattedPatientId = value;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return formattedPatientId;
    }

    private static class PatientInfo {

        private String communityId;
        private String communityName;
        private String patientId;

        public String getCommunityId() {
            return communityId;
        }

        public void setCommunityId(String communityId) {
            this.communityId = communityId;
        }

        public void setCommunityName(String communityName) {
            this.communityName = communityName;
        }

        public String getCommunityName() {
            return communityName;
        }

        public void setPatientId(String patientId) {
            this.patientId = patientId;
        }

        public String getPatientId() {
            return patientId;
        }
    }
}
