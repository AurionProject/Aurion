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

/**
 *
 * @author mflynn02
 *
 * @author rhaslam - Updated to match PD, DQ, DR, and XDR changes for 2013 Specification
 * This implementation (AdminDistTransforms) was not matched to any specificaion
 */
import com.services.nhinc.schema.auditmessage.AuditMessageType;
import com.services.nhinc.schema.auditmessage.AuditSourceIdentificationType;
import com.services.nhinc.schema.auditmessage.CodedValueType;
import com.services.nhinc.schema.auditmessage.ParticipantObjectIdentificationType;
import com.services.nhinc.schema.auditmessage.AuditMessageType.ActiveParticipant;

import gov.hhs.fha.nhinc.common.auditlog.LogEventRequestType;
import gov.hhs.fha.nhinc.common.auditlog.LogFindAuditEventsRequestType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.UserType;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.transform.marshallers.JAXBContextHandler;

import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

public class FindAuditEventsTransforms {

    private static final Logger LOG = Logger.getLogger(FindAuditEventsTransforms.class);

    public static LogEventRequestType transformFindAuditEventsReq2AuditMsg(LogFindAuditEventsRequestType message) {
        AuditMessageType auditMsg = new AuditMessageType();
        LogEventRequestType response = new LogEventRequestType();

        if (message == null || message.getDirection() == null || message.getInterface() == null) {
        	LOG.error("DocumentRetrieveTransforms.transformDocRetrieveResp2AuditMsg() has insufficient data to log a message audfit");
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

        int eventOutcomeID = 0;
        String userID = "";
        CodedValueType eventID = AuditDataTransformHelper.createCodedValue("ADQ", "AuditQuery", "ADQ", "AuditQuery", null);

        // EventIdentification
        auditMsg.setEventIdentification(AuditDataTransformHelper.createEventIdentification(
            AuditDataTransformConstants.EVENT_ACTION_CODE_EXECUTE, eventOutcomeID, eventID, null));
        LOG.info("set EventIdentification");

        // ActiveParticipant
        // NOTE: This is [1..*] in schema but only one item to map to from FindAuditEventsType

        if (userInfo != null && NullChecker.isNotNullish(userInfo.getUserName())) {
            userID = userInfo.getUserName();
            LOG.info("userID " + userID);
        }
        String altUserID = "";
        String userName = "";
        if ((userInfo != null) && (userInfo.getPersonName() != null) && NullChecker.isNotNullish(userInfo.getPersonName().getGivenName())
            && NullChecker.isNotNullish(userInfo.getPersonName().getFamilyName())) {
            userName = userInfo.getPersonName().getGivenName() + " " + userInfo.getPersonName().getFamilyName();
        }
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

        // AuditSourceIdentification
        // NOTE: This is [1..*] in the schema but only one item to map to from FindAuditEventsType
        String auditSourceID = "";
        String enterpriseSiteID = "";
        if (userInfo != null && userInfo.getOrg() != null) {
            if (userInfo.getOrg().getName() != null && userInfo.getOrg().getName().length() > 0) {
                enterpriseSiteID = userInfo.getOrg().getName();
            }
            if (userInfo.getOrg().getHomeCommunityId() != null
                && userInfo.getOrg().getHomeCommunityId().length() > 0) {

                auditSourceID = userInfo.getOrg().getHomeCommunityId();
                LOG.info("auditSourceID " + auditSourceID);
            }
        }
        AuditSourceIdentificationType auditSource = AuditDataTransformHelper.createAuditSourceIdentification(auditSourceID, enterpriseSiteID, null);
        auditMsg.getAuditSourceIdentification().add(auditSource);
        LOG.info("set AuditSourceIdentification");

        // ParticipationObjectIdentification
        // NOTE: This is [0..*] in the schema but only one item to map to from FindAuditEventsType
        String patientID = "";

        if (message.getMessage().getFindAuditEvents().getPatientId() != null
            && message.getMessage().getFindAuditEvents().getPatientId().length() > 0) {
            patientID = message.getMessage().getFindAuditEvents().getPatientId();
            LOG.info("patientID " + patientID);
        }
        /**
         * Create a ParticipantObjectIdentification entry for Patient
         */
        ParticipantObjectIdentificationType partObjId = null;
        if ((patientID != null) && (! patientID.isEmpty())) {
 	        CodedValueType partObjectIdType = AuditDataTransformHelper.getPatientParticipantRoleIdCodedValue();
 	
 	        // Participant Object Identification Entry $1 Patient
 	        partObjId = AuditDataTransformHelper.createParticipantObjectIdentification(
 	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_PERSON,
 	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_PATIENT,
 	        		partObjectIdType,
 	        		patientID,
 	        		null);
 	
 	        auditMsg.getParticipantObjectIdentification().add(partObjId);
        }

        boolean addDone = true;
        if (partObjId == null) {
        	partObjId = new ParticipantObjectIdentificationType();
        	addDone = false;
        }
        
        // Fill in the message field with the contents of the event message
        try {
            JAXBContextHandler oHandler = new JAXBContextHandler();
            JAXBContext jc = oHandler.getJAXBContext("com.services.nhinc.schema.auditmessage");
            Marshaller marshaller = jc.createMarshaller();
            ByteArrayOutputStream baOutStrm = new ByteArrayOutputStream();
            baOutStrm.reset();

            com.services.nhinc.schema.auditmessage.ObjectFactory factory = new com.services.nhinc.schema.auditmessage.ObjectFactory();
            JAXBElement oJaxbElement = factory.createFindAuditEvents(message.getMessage().getFindAuditEvents());
            baOutStrm.close();
            marshaller.marshal(oJaxbElement, baOutStrm);
            LOG.debug("Done marshalling the message.");

            partObjId.setParticipantObjectQuery(baOutStrm.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        if (!addDone) {
        	auditMsg.getParticipantObjectIdentification().add(partObjId);
        }
        response.setAuditMessage(auditMsg);
        return response;
    }
}
