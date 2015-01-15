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

import gov.hhs.fha.nhinc.common.auditlog.LogAdhocQueryRequestType;
import gov.hhs.fha.nhinc.common.auditlog.LogAdhocQueryResultRequestType;
import gov.hhs.fha.nhinc.common.auditlog.LogEventRequestType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.UserType;
import gov.hhs.fha.nhinc.transform.marshallers.JAXBContextHandler;
import gov.hhs.healthit.nhin.DocQueryAcknowledgementType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryRequest;
import oasis.names.tc.ebxml_regrep.xsd.query._3.AdhocQueryResponse;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ExtrinsicObjectType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.IdentifiableType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ObjectRefType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.SlotType1;

import org.apache.log4j.Logger;

import com.services.nhinc.schema.auditmessage.AuditMessageType;
import com.services.nhinc.schema.auditmessage.AuditMessageType.ActiveParticipant;
import com.services.nhinc.schema.auditmessage.CodedValueType;
import com.services.nhinc.schema.auditmessage.EventIdentificationType;
import com.services.nhinc.schema.auditmessage.ParticipantObjectIdentificationType;

/**
 *
 * @author MFLYNN02
 * @author rhaslam (2013 SPEC Update)
 * 
 * The specifcation update used here:
 * 	IT Infrastructure Technical Framework Volume 2a (ITI TF-2a) Transactions Part A – Sections 3.1 – 3.28 
 *	Revision 10.0 – Final Text September 27, 2013 - SECTION 3.18.5.1
 */
public class DocumentQueryTransforms {

    private static final Logger LOG = Logger.getLogger(DocumentQueryTransforms.class);
    private static final String PATIENT_ID_SLOT_REQUEST = "$XDSDocumentEntryPatientId";
    private static final String PATIENT_ID_SLOT_RESPONSE = "sourcePatientId";
    
    public DocumentQueryTransforms() {
    }
    
    private CodedValueType getEventTypeCode() {
    	CodedValueType value = AuditDataTransformHelper.createCodedValue(
            AuditDataTransformConstants.DQ_EVENT_TYPE_CODE,
            AuditDataTransformConstants.DQ_EVENT_TYPE_CODE_SYSTEM,
            AuditDataTransformConstants.DQ_EVENT_TYPE_CODE_SYSTEM_NAME,
            AuditDataTransformConstants.DQ_EVENT_TYPE_CODE_SYSTEM_DISPLAY_NAME,
            null);
    	return value;
    }
    

    /**
     *
     * @param message LogAdhocQueryRequestType
     * @return <code>LogEventRequestType</code>
     */
    public LogEventRequestType transformDocQueryReq2AuditMsg(LogAdhocQueryRequestType message) {
        return transformDocQueryReq2AuditMsg(message, null);
    }

    /**
     *
     * @param message LogAdhocQueryRequestType
     * @param responseCommunityID
     * @return <code>LogEventRequestType</code>
     */
    public LogEventRequestType transformDocQueryReq2AuditMsg(LogAdhocQueryRequestType message, String responseCommunityID) {

        AuditMessageType auditMsg = new AuditMessageType();
        LogEventRequestType response = new LogEventRequestType();
        
        LOG.trace("******************************************************************");
        LOG.trace("Entering transformDocQueryReq2AuditMsg() method.");
        LOG.trace("******************************************************************");
        
        if (message == null || message.getDirection() == null || message.getInterface() == null) {
        	LOG.error("DocumentQueryTransforms.transformDocQueryReq2AuditMsg() has insufficient data to log a message audfit");
        	return null;
        }
        
        String _interface = message.getInterface();
        String direction = message.getDirection();
        response.setDirection(direction);
        response.setInterface(_interface);

        AssertionType assertion = null;
        UserType userInfo = null;
        // Extract Assertion and UserInfo from Message.Assertion
        if (message != null && message.getMessage() != null && message.getMessage().getAssertion() != null) {
        	assertion = message.getMessage().getAssertion();
	        if (assertion.getUserInfo() != null) {
	            userInfo = message.getMessage().getAssertion().getUserInfo();
	        }
        }

        // Create Event Identification Section
        // AdhocQueryRequest
        // Create EventIdentification
        CodedValueType eventId = AuditDataTransformHelper.createCodedValue(
            AuditDataTransformConstants.DQ_REQUEST_EVENT_ID_CODE,
            AuditDataTransformConstants.DQ_REQUEST_EVENT_ID_CODE_SYSTEM,
            AuditDataTransformConstants.DQ_REQUEST_EVENT_ID_CODE_SYSTEM_NAME,
            AuditDataTransformConstants.DQ_REQUEST_EVENT_ID_CODE_SYSTEM_DISPLAY_NAME,
            null);
        
        CodedValueType eventTypeCode = getEventTypeCode();

        EventIdentificationType eventIdentification = AuditDataTransformHelper.createEventIdentification(
            AuditDataTransformConstants.DQ_REQUEST_EVENT_ACTION_CODE,
            AuditDataTransformConstants.EVENT_OUTCOME_INDICATOR_SUCCESS, 
            eventId, 
            eventTypeCode);
        
        auditMsg.setEventIdentification(eventIdentification);
        
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

        // Create Audit Source Identification Section

        // Create Participant Object Identification Section
        // Patient ParticipantObjectIdentification
        String patientId = "";
        if (message != null && message.getMessage() != null && message.getMessage().getAdhocQueryRequest() != null
            && message.getMessage().getAdhocQueryRequest().getAdhocQuery() != null
            && message.getMessage().getAdhocQueryRequest().getAdhocQuery().getSlot() != null
            && message.getMessage().getAdhocQueryRequest().getAdhocQuery().getSlot().size() > 0) {
            List<SlotType1> slotItemsList = message.getMessage().getAdhocQueryRequest().getAdhocQuery().getSlot();
            for (SlotType1 slotItem : slotItemsList) {
                if (slotItem != null) {
                    if (PATIENT_ID_SLOT_REQUEST.equals(slotItem.getName())) {
                        if (slotItem.getValueList() != null && slotItem.getValueList().getValue() != null
                            && slotItem.getValueList().getValue().size() > 0) {
                            patientId = slotItem.getValueList().getValue().get(0);
                            break;
                        }
                    }
                }
            }
        }
        /**
         * DQ Request has 2 defined Participant Objects on Query
         * 	** Patient
         *  ** QueryParameters
         */
        /**
         * Create a ParticipantObjectIdentification entry for Patient
         */
        if ((patientId != null) && (! patientId.isEmpty())) {
	        CodedValueType patientType = AuditDataTransformHelper.getPatientParticipantRoleIdCodedValue();
	
	        // Participant Object Identification Entry $1 Patient
	        ParticipantObjectIdentificationType partObjId = AuditDataTransformHelper.createParticipantObjectIdentification(
	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_PERSON,
	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_PATIENT,
	        		patientType,
	        		patientId,
	        		null);
	
	        auditMsg.getParticipantObjectIdentification().add(partObjId);
        }
        // Participant Object Identification #2 Query Parameters
        // Encode the Query Parameters
        byte[] queryParams = null;
        try {
            JAXBContextHandler oHandler = new JAXBContextHandler();
            JAXBContext jc = oHandler.getJAXBContext("oasis.names.tc.ebxml_regrep.xsd.query._3");
            Marshaller marshaller = jc.createMarshaller();
            ByteArrayOutputStream baOutStrm = new ByteArrayOutputStream();
            baOutStrm.reset();
            if (message != null) {
                marshaller.marshal(message.getMessage().getAdhocQueryRequest(), baOutStrm);
            }
            LOG.debug("Done marshalling the message.");

            queryParams = baOutStrm.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        
        // Add the QueryParams Participant Object Identification Type 
        if (queryParams != null) {

	        CodedValueType queryParamsType = AuditDataTransformHelper.getQueryParamsParticipantRoleIdCodedValue();
	        
	        ParticipantObjectIdentificationType queryObjID = AuditDataTransformHelper.createParticipantObjectIdentification(
	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM,
	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_QUERY,
	        		queryParamsType, 
	        		assertion.getMessageId(),
	        		queryParams);
	
	        auditMsg.getParticipantObjectIdentification().add(queryObjID);
        }

        /*
         * Create the Community ParticipantObjectIdentification record 
         */
        if (userInfo != null && userInfo.getOrg()!= null) {
	        ParticipantObjectIdentificationType communityRecord = AuditDataTransformHelper.createParticipantCommunityRecordFromUser(userInfo);
	        auditMsg.getParticipantObjectIdentification().add(communityRecord);
        }
        /* 
         * Put the contents of the actual message into the Audit Log Message
         * This is carried by a ParticipantObjectIdentification record of type DATA
         */
        byte[] messageBytes = null;
        String messageId = null;
        AdhocQueryRequest actualRequest = message.getMessage().getAdhocQueryRequest();
        messageBytes = marshallAdhocQueryRequest(actualRequest);
        
        messageId = assertion.getMessageId();
        
        if (messageBytes != null && messageBytes.length > 0){
        	CodedValueType dataTransport = AuditDataTransformHelper.getDataTransportParticipantRoleIdCodedValue();
	        ParticipantObjectIdentificationType  dataObject = AuditDataTransformHelper.createParticipantObjectIdentification(
	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM,
	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_DATA_TRANSPORT,
	        		dataTransport,
	        		messageId,
	        		messageBytes);
	        auditMsg.getParticipantObjectIdentification().add(dataObject);
        }
        
        response.setAuditMessage(auditMsg);

        LOG.trace("******************************************************************");
        LOG.trace("Exiting transformDocQueryReq2AuditMsg() method.");
        LOG.trace("******************************************************************");

        return response;
    }

    /**
     *
     * @param message
     * @return <code>LogEventRequestType</code>
     */
    public LogEventRequestType transformDocQueryResp2AuditMsg(LogAdhocQueryResultRequestType message) {
        return transformDocQueryResp2AuditMsg(message, null);
    }

    /**
     *
     * @param message
     * @param requestCommunityID
     * @return <code>LogEventRequestType</code>
     */
    public LogEventRequestType transformDocQueryResp2AuditMsg(LogAdhocQueryResultRequestType message,
        String requestCommunityID) {
        AuditMessageType auditMsg = new AuditMessageType();
        LogEventRequestType response = new LogEventRequestType();
        
        if (message == null || message.getDirection() == null || message.getInterface() == null) {
        	LOG.error("DocumentQueryTransforms.transformDocQueryReq2AuditMsg().transformDocQueryResp2AuditMsg() has insufficient data to log a message audfit");
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
        else {
            LOG.error("***** ASSERTION IS NULL *****");
        }
        
        LOG.trace("******************************************************************");
        LOG.trace("Entering transformDocQueryResp2AuditMsg() method.");
        LOG.trace("******************************************************************");

        // Create Event Identification Section
        // Create EventIdentification
        CodedValueType eventId = AuditDataTransformHelper.createCodedValue(
            AuditDataTransformConstants.DQ_RESPONSE_EVENT_ID_CODE,
            AuditDataTransformConstants.DQ_RESPONSE_EVENT_ID_CODE_SYSTEM,
            AuditDataTransformConstants.DQ_RESPONSE_EVENT_ID_CODE_SYSTEM_NAME,
            AuditDataTransformConstants.DQ_RESPONSE_EVENT_ID_CODE_SYSTEM_DISPLAY_NAME,
            null);
        
        CodedValueType eventTypeCode = getEventTypeCode();

        EventIdentificationType eventIdentification = AuditDataTransformHelper.createEventIdentification(
            AuditDataTransformConstants.DQ_RESPONE_EVENT_ACTION_CODE,
            AuditDataTransformConstants.EVENT_OUTCOME_INDICATOR_SUCCESS, 
            eventId, 
            eventTypeCode);
        
        auditMsg.setEventIdentification(eventIdentification);

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

        // Create Participant Object Identification Section - Get PatientID
        String patientId = "";
        if (message != null && message.getMessage() != null && message.getMessage().getAdhocQueryResponse() != null 
            && message.getMessage().getAdhocQueryResponse().getResponseSlotList() != null
            && message.getMessage().getAdhocQueryResponse().getResponseSlotList().getSlot() != null
            && message.getMessage().getAdhocQueryResponse().getResponseSlotList().getSlot().size() > 0) {
            List<SlotType1> slotItemsList = message.getMessage().getAdhocQueryResponse().getResponseSlotList().getSlot();
            for (SlotType1 slotItem : slotItemsList) {
                if (slotItem != null) {
                    if (PATIENT_ID_SLOT_RESPONSE.equals(slotItem.getName())) {
                        if (slotItem.getValueList() != null && slotItem.getValueList().getValue() != null
                            && slotItem.getValueList().getValue().size() > 0) {
                            patientId = slotItem.getValueList().getValue().get(0);
                            break;
                        }
                    }
                }
            }
        }
        /**
         * Create a ParticipantObjectIdentification entry for Patient
         */
        if ((patientId != null) && (! patientId.isEmpty())) {
	        CodedValueType patientType = AuditDataTransformHelper.getPatientParticipantRoleIdCodedValue();
	
	        // Participant Object Identification Entry $1 Patient
	        ParticipantObjectIdentificationType partObjId = AuditDataTransformHelper.createParticipantObjectIdentification(
	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_PERSON,
	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_PATIENT,
	        		patientType,
	        		patientId,
	        		null);
	
	        auditMsg.getParticipantObjectIdentification().add(partObjId);
        }
        // Get list of returned DOCUMENT Ids
        if ((message != null) && (message.getMessage() != null)
            && (message.getMessage().getAdhocQueryResponse() != null)
            && (message.getMessage().getAdhocQueryResponse().getRegistryObjectList() != null)
            && (message.getMessage().getAdhocQueryResponse().getRegistryObjectList().getIdentifiable() != null)
            && (message.getMessage().getAdhocQueryResponse().getRegistryObjectList().getIdentifiable().size() > 0)) {
            // Create Audit Source Identification Section
            List<JAXBElement<? extends IdentifiableType>> objList = message.getMessage().getAdhocQueryResponse()
                .getRegistryObjectList().getIdentifiable();
            ExtrinsicObjectType oExtObj = null;
            ObjectRefType oObjRef = null;

            // Look for the first ExtrinsicObject type.. We will use that one to extract the data.
            // -------------------------------------------------------------------------------------
            for (int i = 0; i < objList.size(); i++) {
                JAXBElement<? extends IdentifiableType> oJAXBObj = objList.get(i);

                if ((oJAXBObj != null)
                    && (oJAXBObj.getDeclaredType() != null)
                    && (oJAXBObj.getDeclaredType().getCanonicalName() != null)
                    && (oJAXBObj.getDeclaredType().getCanonicalName()
                    .equals("oasis.names.tc.ebxml_regrep.xsd.rim._3.ExtrinsicObjectType"))
                    && (oJAXBObj.getValue() != null)) {
                    oExtObj = (ExtrinsicObjectType) oJAXBObj.getValue();
                    break; // We have what we want let's get out of here...
                } else if ((oJAXBObj != null)
                    && (oJAXBObj.getDeclaredType() != null)
                    && (oJAXBObj.getDeclaredType().getCanonicalName() != null)
                    && (oJAXBObj.getDeclaredType().getCanonicalName()
                    .equals("oasis.names.tc.ebxml_regrep.xsd.rim._3.ObjectRefType"))
                    && (oJAXBObj.getValue() != null)) {
                    oObjRef = (ObjectRefType) oJAXBObj.getValue();
                    break; // We have what we want let's get out of here...
                }

            } 
            
            /*
             * Create the Community ParticipantObjectIdentification record 
             */
            if (userInfo != null && userInfo.getOrg()!= null) {
    	        ParticipantObjectIdentificationType communityRecord = AuditDataTransformHelper.createParticipantCommunityRecordFromUser(userInfo);
    	        auditMsg.getParticipantObjectIdentification().add(communityRecord);
            }

            // Document Entry for ParticipantObjectIdentification
            // The specification does not call for this entry - but the result of a DQ is 0 or more 
            // documentIds and the document version of ParticipantObjectIdentification fits perfectly.
            // --------------------------------------------------
            ParticipantObjectIdentificationType partObjId = new ParticipantObjectIdentificationType();
            String documentIds = "";
            if (oExtObj != null && oExtObj.getExternalIdentifier() != null
                && oExtObj.getExternalIdentifier().size() > 0) {
        		int numberOfDocs = oExtObj.getExternalIdentifier().size();
        		for (int i=0; i < numberOfDocs; i++) {
        			if (i > 0) {
        				documentIds += ", ";
        			}
        			documentIds += oExtObj.getExternalIdentifier().get(i).getValue();
        		}
            }
            
            if ((documentIds != null) && (! documentIds.isEmpty())) {
	            CodedValueType participantObjectType = AuditDataTransformHelper.getDocumentParticipantRoleIdCodedValue();
	            
	            partObjId = AuditDataTransformHelper.createParticipantObjectIdentification(
	            		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM,
	            		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_REPORT,
	            		participantObjectType,           		
	            		documentIds,
	            		null);
	            
	            auditMsg.getParticipantObjectIdentification().add(partObjId);
            }
            
            // QUERY Entry for ParticipantObjectIdentification
            byte[] queryParams = null;
            try {
                JAXBContextHandler oHandler = new JAXBContextHandler();
                JAXBContext jc = oHandler.getJAXBContext("oasis.names.tc.ebxml_regrep.xsd.query._3");
                Marshaller marshaller = jc.createMarshaller();
                ByteArrayOutputStream baOutStrm = new ByteArrayOutputStream();
                baOutStrm.reset();
                marshaller.marshal(message.getMessage().getAdhocQueryResponse(), baOutStrm);
                LOG.debug("Done marshalling the message.");
                queryParams = baOutStrm.toByteArray();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
            
            if ((queryParams != null) && (queryParams.length > 0)) {
	            CodedValueType objectIdTypeCode = AuditDataTransformHelper.getQueryParamsParticipantRoleIdCodedValue();
	            
	            ParticipantObjectIdentificationType queryObjID = AuditDataTransformHelper.createParticipantObjectIdentification(
	            		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM,
	            		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_QUERY,
	            		objectIdTypeCode, 
	            		assertion.getMessageId(),
	            		queryParams);
	            auditMsg.getParticipantObjectIdentification().add(queryObjID);
            }
        }
        /*
         * Create the Community ParticipantObjectIdentification record 
         */
        if (userInfo != null && userInfo.getOrg()!= null) {
	        ParticipantObjectIdentificationType communityRecord = AuditDataTransformHelper.createParticipantCommunityRecordFromUser(userInfo);
	        auditMsg.getParticipantObjectIdentification().add(communityRecord);
        }
        
        /* 
         * Put the contents of the actual message into the Audit Log Message
         * This is carried by a ParticipantObjectIdentification record of type DATA
         */
        byte[] messageBytes = null;
        String messageId = null;
        
        AdhocQueryResponse actualResponse = message.getMessage().getAdhocQueryResponse();     
        messageBytes = marshallAdhocQueryResponse(actualResponse); 
        
        messageId = assertion.getMessageId();
        
        if (messageBytes != null && messageBytes.length > 0){
        	CodedValueType dataTypeCode = AuditDataTransformHelper.getDataTransportParticipantRoleIdCodedValue();
	        ParticipantObjectIdentificationType  dataObject = AuditDataTransformHelper.createParticipantObjectIdentification(
	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM,
	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_DATA_TRANSPORT,
	        		dataTypeCode,
	        		messageId,
	        		messageBytes);
	        auditMsg.getParticipantObjectIdentification().add(dataObject);
        }
        
        response.setAuditMessage(auditMsg);

        LOG.trace("******************************************************************");
        LOG.trace("Exiting transformDocQueryResp2AuditMsg() method.");
        LOG.trace("******************************************************************");
        return response;
    }

    /**
     *
     * @param acknowledgement
     * @param assertion
     * @param direction
     * @param _interface
     * @return <code>LogEventRequestType</code>
     */
    public LogEventRequestType transformAcknowledgementToAuditMsg(DocQueryAcknowledgementType acknowledgement,
        AssertionType assertion, String direction, String _interface) {
        return transformAcknowledgementToAuditMsg(acknowledgement, assertion, direction, _interface, null);
    }

    /**
     *
     * @param acknowledgement
     * @param assertion
     * @param direction
     * @param _interface
     * @param action
     * @return <code>LogEventRequestType</code>
     */
    public LogEventRequestType transformAcknowledgementToAuditMsg(DocQueryAcknowledgementType acknowledgement,
        AssertionType assertion, String direction, String _interface, String requestCommunityID) {
        LogEventRequestType result = null;
        AuditMessageType auditMsg = null;

        if ((acknowledgement == null) ||  (assertion == null)) {
            LOG.error("DocumentQueryTransforms.transformAcknowledgementToAuditMsg lacks data from which to create a message audit entry. No audit entered.");
            return null;
        }
        
        result = new LogEventRequestType();

        auditMsg = new AuditMessageType();
        // Create EventIdentification
        CodedValueType eventId = AuditDataTransformHelper.createCodedValue(
            AuditDataTransformConstants.DQ_RESPONSE_EVENT_ID_CODE,
            AuditDataTransformConstants.DQ_RESPONSE_EVENT_ID_CODE_SYSTEM,
            AuditDataTransformConstants.DQ_RESPONSE_EVENT_ID_CODE_SYSTEM_NAME,
            AuditDataTransformConstants.DQ_RESPONSE_EVENT_ID_CODE_SYSTEM_DISPLAY_NAME,
            null);
        
        CodedValueType eventTypeCode = getEventTypeCode();
        
        EventIdentificationType eventIdentification = AuditDataTransformHelper.createEventIdentification(
            AuditDataTransformConstants.DQ_RESPONE_EVENT_ACTION_CODE,
            AuditDataTransformConstants.EVENT_OUTCOME_INDICATOR_SUCCESS, 
            eventId, 
            eventTypeCode);
        
        auditMsg.setEventIdentification(eventIdentification);

        // Create Active Participant Section
        UserType userInfo = assertion.getUserInfo();
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

        String patientId = "";
        /* Assign ParticipationObjectIdentification */
        // Create Participation Object Identification Section
        ParticipantObjectIdentificationType partObjId = new ParticipantObjectIdentificationType();
        if (assertion.getUniquePatientId() != null && assertion.getUniquePatientId().size() > 0
            && assertion.getUniquePatientId().get(0) != null) {
        	patientId = assertion.getUniquePatientId().get(0);
        }
        
        // Patient Entry for ParticipantObjectIdentification
        if (patientId != null) {
	        CodedValueType patientParticipantIdType = AuditDataTransformHelper.getPatientParticipantRoleIdCodedValue();
	        partObjId = AuditDataTransformHelper.createParticipantObjectIdentification(
	        	AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_PERSON,
	        	AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_PATIENT,
	        	patientParticipantIdType,
	        	patientId,
	        	null);
        }
        
        result.setAuditMessage(auditMsg);
        result.setDirection(direction);
        result.setInterface(_interface);

        return result;
    }

    /**
     *
     * @param baOutStrm
     * @param acknowledgement
     * @throws RuntimeException
     */
    private void marshalAcknowledgement(ByteArrayOutputStream baOutStrm, DocQueryAcknowledgementType acknowledgement)
        throws RuntimeException {
        // Put the contents of the actual message into the Audit Log Message
        try {
            JAXBContextHandler oHandler = new JAXBContextHandler();
            JAXBContext jc = oHandler.getJAXBContext("gov.hhs.healthit.nhin");
            Marshaller marshaller = jc.createMarshaller();
            baOutStrm.reset();

            javax.xml.namespace.QName xmlqname = new javax.xml.namespace.QName("http://www.hhs.gov/healthit/nhin",
                "DocQueryAcknowledgementType");
            JAXBElement<DocQueryAcknowledgementType> element;

            element = new JAXBElement<DocQueryAcknowledgementType>(xmlqname, DocQueryAcknowledgementType.class,
                acknowledgement);

            marshaller.marshal(element, baOutStrm);
            LOG.debug("Done marshalling the message.");
        } catch (Exception e) {
            LOG.error("Exception while marshalling Acknowledgement", e);
            throw new RuntimeException();
        }
    }
    
    /*
     * Marshall the RetrieveDocumentSetResponse into a byte array. Normally this is easy, but for DocQuery and DocRetrieve the schema file
     * XSD for these messages does not declare an @XMLRootElement. This requires that the code wrap the object with a root element.
     * 
     * One day an update to the XSD might provide a RootELement declaration in which case the wrapper code can be removed
     */
    protected static byte[] marshallAdhocQueryRequest(AdhocQueryRequest message) throws RuntimeException {
        // Put the contents of the actual message into the Audit Log Message
        ByteArrayOutputStream baOutStrm = new ByteArrayOutputStream();
        byte[] messageBytes = null;
    	try {
            JAXBContextHandler oHandler = new JAXBContextHandler();
            JAXBContext jc = oHandler.getJAXBContext("oasis.names.tc.ebxml_regrep.xsd.query._3");
            Marshaller marshaller = jc.createMarshaller();
            baOutStrm.reset();
            marshaller.marshal(message, baOutStrm);
            baOutStrm.close();
            messageBytes = baOutStrm.toByteArray();
        } 
    	catch (JAXBException e) {
        	// THe audit entry should not fail just because the JAXB marshalling failed here
            e.printStackTrace();
            LOG.error("Marshalling the AdhocQueryRequest message generated a run-time exception. Message not logged.");
        }
    	catch (IOException e) {
        	// THe audit entry should not fail just because the JAXB marshalling failed here
            e.printStackTrace();
            LOG.warn("Marshalling the AdhocQueryRequest message generated an I/O Exception closing the stream.");
        }
    	return messageBytes;
    }
    
    
    /*
     * Marshall the RetrieveDocumentSetResponse into a byte array. Normally this is easy, but for DocQuery and DocRetrieve the schema file
     * XSD for these messages does not declare an @XMLRootElement. This requires that the code wrap the object with a root element.
     * 
     * One day an update to the XSD might provice a RootELement declaration in which case the wrapper code can be removed
     */
    protected static byte[] marshallAdhocQueryResponse(AdhocQueryResponse message) throws RuntimeException {
        // Put the contents of the actual message into the Audit Log Message
        ByteArrayOutputStream baOutStrm = new ByteArrayOutputStream();
        byte[] messageBytes = null;
    	try {
            JAXBContextHandler oHandler = new JAXBContextHandler();
            JAXBContext jc = oHandler.getJAXBContext("oasis.names.tc.ebxml_regrep.xsd.query._3");
            Marshaller marshaller = jc.createMarshaller();
            marshaller.marshal(message, baOutStrm);
            baOutStrm.close();
            messageBytes = baOutStrm.toByteArray();
        } 
    	catch (JAXBException e) {
        	// THe audit entry should not fail just because the JAXB marshalling failed here
    		e.printStackTrace();
            LOG.error("Marshalling the AdhocQueryResponse message generated a run-time exception. Message not logged.");
        }
    	catch (IOException e) {
        	// THe audit entry should not fail just because the JAXB marshalling failed here
            e.printStackTrace();
            LOG.warn("Marshalling the AdhocQueryResponse message generated an I/O Exception closing the stream.");
        }
    	return messageBytes;
    }
}
