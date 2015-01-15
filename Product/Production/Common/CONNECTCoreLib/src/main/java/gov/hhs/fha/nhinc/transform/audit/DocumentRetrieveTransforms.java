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

import gov.hhs.fha.nhinc.common.auditlog.DocRetrieveMessageType;
import gov.hhs.fha.nhinc.common.auditlog.LogDocRetrieveRequestType;
import gov.hhs.fha.nhinc.common.auditlog.LogDocRetrieveResultRequestType;
import gov.hhs.fha.nhinc.common.auditlog.LogEventRequestType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.UserType;
import gov.hhs.fha.nhinc.properties.PropertyAccessException;
import gov.hhs.fha.nhinc.properties.PropertyAccessor;
import gov.hhs.fha.nhinc.transform.marshallers.JAXBContextHandler;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType.DocumentRequest;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType.DocumentResponse;
import gov.hhs.fha.nhinc.largefile.LargeFileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import com.services.nhinc.schema.auditmessage.AuditMessageType;
import com.services.nhinc.schema.auditmessage.AuditMessageType.ActiveParticipant;
import com.services.nhinc.schema.auditmessage.CodedValueType;
import com.services.nhinc.schema.auditmessage.EventIdentificationType;
import com.services.nhinc.schema.auditmessage.ParticipantObjectIdentificationType;

/**
 * 
 * @author MFLYNN02
 * @author rhaslam (2013 Spec Update)
 *
 * The specification used here:
 *	IT Infrastructure Technical Framework Volume 2b (ITI TF-2b) Transactions Part B – Sections 3.29 – 3.64 Revision 
 *	Final Text - September 27, 2013 - Section 3.43.6.1 Audit Record Considerations
 */
public class DocumentRetrieveTransforms {
    private static final Logger LOG = Logger.getLogger(DocumentRetrieveTransforms.class);
    private static final String DOCUMENT_RETRIEVE_RESPONSE_MESSAGE_CONTENT_REDACTED_MESSAGE = "Document retrieve response message content redacted.";
    private static final String PROPERTY_KEY_AUDIT_DOC_RETRIEVE_RESPONSE_REDACTION = "audit.doc.retrieve.response.redaction";
    private static final String GATEWAY_PROPERTY_FILE = "gateway";
    
    private static Boolean redactionStatus = null;
    /**
     * 
     * @param message
     * @return <code>LogEventRequestType</code>
     */
    public static LogEventRequestType transformDocRetrieveReq2AuditMsg(LogDocRetrieveRequestType message) {
        return transformDocRetrieveReq2AuditMsg(message, null);
    }
    
    protected static Boolean getRedactionStatus() {
    	if (redactionStatus == null){
    		redactionStatus = isDocRetrieveResponseBodyRedactionEnabled();
    	}
    	return redactionStatus;
	}
    /**
     * 
     * @param message
     * @param responseCommunityID
     * @return <code>LogEventRequestType</code>
     */
    public static LogEventRequestType transformDocRetrieveReq2AuditMsg(LogDocRetrieveRequestType message, String responseCommunityID) {
        AuditMessageType auditMsg = new AuditMessageType();
        LogEventRequestType response = new LogEventRequestType();

        LOG.info("******************************************************************");
        LOG.info("Entering transformDocRetrieveReq2AuditMsg() method.");
        LOG.info("******************************************************************");
        
        if (message == null || message.getDirection() == null || message.getInterface() == null) {
        	LOG.error("DocumentRetrieveTransforms.transformDocRetrieveReq2AuditMsg() has insufficient data to log a message audfit");
        	return null;
        }
        
        RetrieveDocumentSetRequestType retrieveDocumentSet = message.getMessage().getRetrieveDocumentSetRequest();
        List<DocumentRequest> documentRequestList = retrieveDocumentSet. getDocumentRequest();
        
        
        String _interface = message.getInterface(); 
        String direction = message.getDirection();
        response.setDirection(direction);
        response.setInterface(_interface);

        
        UserType userInfo = null;
        AssertionType assertion = null;
        if (message.getMessage() != null && message.getMessage().getAssertion() != null) {
        	assertion = message.getMessage().getAssertion();
	        if (assertion.getUserInfo() != null) {
	            userInfo = assertion.getUserInfo();
	        }
        }

        // Create Event Identification Section
        CodedValueType eventId = AuditDataTransformHelper.createCodedValue(
                AuditDataTransformConstants.DR_RESPONSE_EVENT_ID_CODE,
                AuditDataTransformConstants.DR_RESPONSE_EVENT_ID_CODE_SYSTEM,
                AuditDataTransformConstants.DR_RESPONSE_EVENT_ID_CODE_SYSTEM_NAME,
                AuditDataTransformConstants.DR_RESPONSE_EVENT_ID_CODE_SYSTEM_DISPLAY_NAME,
                null);
        CodedValueType eventTypeCode = AuditDataTransformHelper.createCodedValue(
                AuditDataTransformConstants.DR_EVENT_TYPE_CODE,
                AuditDataTransformConstants.DR_EVENT_TYPE_CODE_SYSTEM,
                AuditDataTransformConstants.DR_EVENT_TYPE_CODE_SYSTEM_NAME,
                AuditDataTransformConstants.DR_EVENT_TYPE_CODE_SYSTEM_DISPLAY_NAME,
                null);

        EventIdentificationType eventIdentification = AuditDataTransformHelper.createEventIdentification(
                AuditDataTransformConstants.DR_REQUEST_EVENT_ACTION_CODE,
                AuditDataTransformConstants.EVENT_OUTCOME_INDICATOR_SUCCESS, eventId, null);
        
        auditMsg.setEventIdentification(eventIdentification);

        eventIdentification.getEventTypeCode().add(eventTypeCode);

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

        String uniquePatientId = "";
        if (message != null && message.getMessage() != null && message.getMessage().getAssertion() != null
                && message.getMessage().getAssertion().getUniquePatientId() != null
                && message.getMessage().getAssertion().getUniquePatientId().size() > 0) {
            uniquePatientId = message.getMessage().getAssertion().getUniquePatientId().get(0);
            LOG.debug("=====>>>>> Create Audit Source Identification Section --> Assertion Unique Patient Id is ["
                    + uniquePatientId + "]");
        }
        
        // Create Document ParticipationObjectIdentification Section
        String documentIds = "";
        if (message != null && message.getMessage() != null && message.getMessage().getRetrieveDocumentSetRequest() != null
            && message.getMessage().getRetrieveDocumentSetRequest().getDocumentRequest() != null
            && message.getMessage().getRetrieveDocumentSetRequest().getDocumentRequest().size() > 0) {
        	
			List<DocumentRequest> docRequest = message.getMessage().getRetrieveDocumentSetRequest().getDocumentRequest();
			Iterator<DocumentRequest> it = docRequest.iterator();
			while (it.hasNext()) {
				DocumentRequest doc = it.next();
				if (! documentIds.isEmpty())
				{
					documentIds += ", ";
				}
				documentIds += doc.getDocumentUniqueId();
			}
        }
        
        
        if ((documentIds != null) && (! documentIds.isEmpty())) {
            
        	CodedValueType documentObjectType = AuditDataTransformHelper.getDocumentParticipantRoleIdCodedValue();
            
            ParticipantObjectIdentificationType docObjId = AuditDataTransformHelper.createParticipantObjectIdentification(
            		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM,
            		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_REPORT,
            		documentObjectType, 
            		documentIds,
            		null);
            auditMsg.getParticipantObjectIdentification().add(docObjId);
        }
        
        /*
         * Create the Community ParticipantObjectIdentification record 
         */
        if (userInfo != null && userInfo.getOrg()!= null) {
	        ParticipantObjectIdentificationType communityRecord = AuditDataTransformHelper.createParticipantCommunityRecordFromUser(userInfo);
	        auditMsg.getParticipantObjectIdentification().add(communityRecord);
        }
        
        // Create PATIENT ParticipationObjectIdentification Section
        // PatientObjectIdentification for Patient IFF Available
        if ((uniquePatientId != null) && !uniquePatientId.isEmpty()) {
	        CodedValueType patientObjectType = AuditDataTransformHelper.getPatientParticipantRoleIdCodedValue();
	
	        // Participant Object Identification Entry Patient
	        ParticipantObjectIdentificationType partObjId = AuditDataTransformHelper.createParticipantObjectIdentification(
	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_PERSON,
	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_PATIENT,
	        		patientObjectType,
	        		uniquePatientId,
	        		null);
	
	        auditMsg.getParticipantObjectIdentification().add(partObjId);
        }  
        /* 
         * Put the contents of the actual message into the Audit Log Message
         * This is carried by a ParticipantObjectIdentification record of type DATA
         */
        byte[] messageBytes = null;
        String messageId = null;
        RetrieveDocumentSetRequestType actualRequest = message.getMessage().getRetrieveDocumentSetRequest();
        DocRetrieveMessageType m = message.getMessage();
        messageBytes = marshallDocumentRetrieveRequestMessage(actualRequest);
        messageId = assertion.getMessageId();
        
        if (messageBytes != null && messageBytes.length > 0) {
        	CodedValueType transportType = AuditDataTransformHelper.getDataTransportParticipantRoleIdCodedValue();
        	
	        ParticipantObjectIdentificationType  dataObject = AuditDataTransformHelper.createParticipantObjectIdentification(
	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM,
	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_DATA_TRANSPORT,
	        		transportType,
	        		messageId,
	        		messageBytes);
	        auditMsg.getParticipantObjectIdentification().add(dataObject);
        }
        
        if (auditMsg.getParticipantObjectIdentification().size() == 0) {
        	LOG.error("DocumentRetrieveTransforms.transformDocRetrieveReq2AuditMsg(): No ParticipantObjectIdentification record was generated.");
        }

        LOG.info("******************************************************************");
        LOG.info("Exiting transformDocRetrieveReq2AuditMsg() method.");
        LOG.info("******************************************************************");

        response.setAuditMessage(auditMsg);
        return response;
    }

    /**
     * 
     * @param message
     * @return <code>LogEventRequestType</code>
     */
    public static LogEventRequestType transformDocRetrieveResp2AuditMsg(LogDocRetrieveResultRequestType message) {
        return transformDocRetrieveResp2AuditMsg(message, null);
    }

    /**
     * 
     * @param message
     * @param requestCommunityID
     * @return <code>LogEventRequestType</code>
     */
    public static LogEventRequestType transformDocRetrieveResp2AuditMsg(
    		LogDocRetrieveResultRequestType message,
            Boolean forceRedaction) 
    {
        AuditMessageType auditMsg = new AuditMessageType();
        LogEventRequestType response = new LogEventRequestType();
        
        LOG.info("******************************************************************");
        LOG.info("Entering transformDocRetrieveResp2AuditMsg() method.");
        LOG.info("******************************************************************");
        
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
      
        // Create Event Identification Section
        CodedValueType eventId = AuditDataTransformHelper.createCodedValue(
                AuditDataTransformConstants.DR_RESPONSE_EVENT_ID_CODE,
                AuditDataTransformConstants.DR_RESPONSE_EVENT_ID_CODE_SYSTEM,
                AuditDataTransformConstants.DR_RESPONSE_EVENT_ID_CODE_SYSTEM_NAME,
                AuditDataTransformConstants.DR_RESPONSE_EVENT_ID_CODE_SYSTEM_DISPLAY_NAME,
                null);
        
        CodedValueType eventTypeCode = AuditDataTransformHelper.createCodedValue(
                AuditDataTransformConstants.DR_EVENT_TYPE_CODE,
                AuditDataTransformConstants.DR_EVENT_TYPE_CODE_SYSTEM,
                AuditDataTransformConstants.DR_EVENT_TYPE_CODE_SYSTEM_NAME,
                AuditDataTransformConstants.DR_EVENT_TYPE_CODE_SYSTEM_DISPLAY_NAME,
                null);

        EventIdentificationType eventIdentification = AuditDataTransformHelper.createEventIdentification(
                AuditDataTransformConstants.DR_RESPONSE_EVENT_ACTION_CODE,
                AuditDataTransformConstants.EVENT_OUTCOME_INDICATOR_SUCCESS, 
                eventId, 
                null);
        
        auditMsg.setEventIdentification(eventIdentification);

        eventIdentification.getEventTypeCode().add(eventTypeCode);

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

        String uniquePatientId = "";
        if (assertion != null &&   assertion.getUniquePatientId() != null && assertion.getUniquePatientId().size() > 0) {
            uniquePatientId = message.getMessage().getAssertion().getUniquePatientId().get(0);
            LOG.debug("=====>>>>> Create Audit Source Identification Section --> Assertion Unique Patient Id is ["
                    + uniquePatientId + "]");
        }

        // Create Community ParticipantObjectIdentification record
        if (userInfo != null && userInfo.getOrg()!= null) {
	        ParticipantObjectIdentificationType communityRecord = AuditDataTransformHelper.createParticipantCommunityRecordFromUser(userInfo);
	        auditMsg.getParticipantObjectIdentification().add(communityRecord);
        }

        // Create the ParticipantObjectIdentification Section

        /* There is no provision here for a patient based ParticipationObjectIdentification Document Retrieve Response
         * 
         * Create PATIENT ParticipationObjectIdentification Section
        if ((uniquePatientId != null) && !uniquePatientId.isEmpty()) {
	        CodedValueType patientType = AuditDataTransformHelper.getParticipantPatientType();
	
	        // Participant Object Identification Entry Patient
	        ParticipantObjectIdentificationType partObjPatient = AuditDataTransformHelper.createParticipantObjectIdentification(
	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_PERSON,
	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_PATIENT,
	        		patientType,
	        		uniquePatientId,
	        		null);
	
	        auditMsg.getParticipantObjectIdentification().add(partObjPatient);
        }  
        */
        
       // Create Document ParticipationObjectIdentification Section
        
        String documentIds = "";
        if (message != null && message.getMessage() != null && message.getMessage().getRetrieveDocumentSetResponse() != null
            && message.getMessage().getRetrieveDocumentSetResponse().getDocumentResponse() != null
            && message.getMessage().getRetrieveDocumentSetResponse().getDocumentResponse().size() > 0) {
        	
			List<DocumentResponse> docs = message.getMessage().getRetrieveDocumentSetResponse().getDocumentResponse();
			Iterator<DocumentResponse> it = docs.iterator();
			while (it.hasNext()) {
				DocumentResponse doc = it.next();
				if (! documentIds.isEmpty())
				{
					documentIds += ", ";
				}
				documentIds += doc.getDocumentUniqueId();
			}
        }
        if ((documentIds != null) && (! documentIds.isEmpty())) {
                
            CodedValueType documentType = AuditDataTransformHelper.getDocumentParticipantRoleIdCodedValue();
                
            ParticipantObjectIdentificationType docObjId = AuditDataTransformHelper.createParticipantObjectIdentification(
            		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM,
            		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_REPORT,
            		documentType, 
            		documentIds,
            		null);
            
            auditMsg.getParticipantObjectIdentification().add(docObjId);
        }
        

        /* 
         * Put the contents of the actual message into the Audit Log Message
         * This is carried by a ParticipantObjectIdentification record of type DATA
         */
        byte[] messageBytes = null;
        String messageId = null;

        RetrieveDocumentSetResponseType actualResponse = message.getMessage().getRetrieveDocumentSetResponse();
        // NOTE: DO NOT UNCOMMENT the marshalling code below. It is a destructive operation that deletes the 
        //       document so that it is not returned to the requesting gateway on the document retrieve action.
        //------------------------------------------------------------------------------------------------------
        messageBytes = null;  // marshallDocumentRetrieveResponseMessage(actualResponse, forceRedaction);
        
        messageId = assertion.getMessageId();
        
        if (messageBytes != null && messageBytes.length > 0){
        	CodedValueType dataTransportType = AuditDataTransformHelper.getDataTransportParticipantRoleIdCodedValue();
	        ParticipantObjectIdentificationType  dataObject = AuditDataTransformHelper.createParticipantObjectIdentification(
	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM,
	        		AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_DATA_TRANSPORT,
	        		dataTransportType,
	        		messageId,
	        		messageBytes);
	        auditMsg.getParticipantObjectIdentification().add(dataObject);
        }
        
        if (auditMsg.getParticipantObjectIdentification().size() == 0){
        	LOG.error("DocumentRetrieveTransforms.transformDocRetrieveResp2AuditMsg: No ParticipantObjectIdentification record was generated.");
        }

        LOG.info("******************************************************************");
        LOG.info("Exiting transformDocRetrieveResp2AuditMsg() method.");
        LOG.info("******************************************************************");

        response.setAuditMessage(auditMsg);
        return response;
    }

    /*
     * Marshall the RetrieveDocumentSetResponse into a byte array. Normally this is easy, but for DocQuery and DocRetrieve the schema file
     * XSD for these messages does not declare an @XMLRootElement. This requires that the code wrap the object with a root element.
     * 
     * One day an update to the XSD might provide a RootELement declaration in which case the wrapper code can be removed
     */
    protected static byte[] marshallDocumentRetrieveRequestMessage(RetrieveDocumentSetRequestType message) throws RuntimeException {
        // Put the contents of the actual message into the Audit Log Message
        ByteArrayOutputStream baOutStrm = new ByteArrayOutputStream();
        byte[] messageBytes = null;
            try {
            JAXBContextHandler oHandler = new JAXBContextHandler();
            JAXBContext jc = oHandler.getJAXBContext("ihe.iti.xds_b._2007");
            Marshaller marshaller = jc.createMarshaller();
            baOutStrm.reset();
            ihe.iti.xds_b._2007.ObjectFactory factory = new ihe.iti.xds_b._2007.ObjectFactory();
            JAXBElement oJaxbElement = factory.createRetrieveDocumentSetRequest(message);
            marshaller.marshal(oJaxbElement, baOutStrm);
            // baOutStrm.close(); This call does nothing - thus removed.
            messageBytes = baOutStrm.toByteArray();
        } catch (JAXBException e) {
        	// The audit entry should not fail just because the JAXB marshalling failed here
            e.printStackTrace();
            LOG.error("Marshalling the RetrieveDocumentSetRequestType message generated a run-time exception. Message not logged.");
        }
    	return messageBytes;
    }
    
    
    /*
     * Marshall the RetrieveDocumentSetResponse into a byte array. Normally this is easy, but for DocQuery and DocRetrieve the schema file
     * XSD for these messages does not declare an @XMLRootElement. This requires that the code wrap the object with a root element.
     * 
     * One day an update to the XSD might provide a RootELement declaration in which case the wrapper code can be removed
     */
    protected static byte[] marshallDocumentRetrieveResponseMessage(RetrieveDocumentSetResponseType message, Boolean forceRedaction) throws RuntimeException {
        // Put the contents of the actual message into the Audit Log Message
        ByteArrayOutputStream baOutStrm = new ByteArrayOutputStream();
        byte[] messageBytes = null;
        boolean redactionEnabled = false;
        
        if (forceRedaction == null) {
        	redactionEnabled = getRedactionStatus().booleanValue();
        }
        else {
        	redactionEnabled = forceRedaction.booleanValue();
        }
        if (redactionEnabled){
            LOG.debug("DocumentRetrieveTransforms.marshallDOcumentRetrieveResponseMessage(..) redaction enabled - returning redacted content.");
            messageBytes = DOCUMENT_RETRIEVE_RESPONSE_MESSAGE_CONTENT_REDACTED_MESSAGE.getBytes();
        } 
        else {
			try {
	            JAXBContextHandler oHandler = new JAXBContextHandler();
	            JAXBContext jc = oHandler.getJAXBContext("ihe.iti.xds_b._2007");
	            Marshaller marshaller = jc.createMarshaller();
	            baOutStrm.reset();
	            ihe.iti.xds_b._2007.ObjectFactory factory = new ihe.iti.xds_b._2007.ObjectFactory();

	            // Take the DocumentResponses from the message and add them to a new RetrieveDocumentSetResponseType
	            RetrieveDocumentSetResponseType retrieveResponse =  new RetrieveDocumentSetResponseType();
	            List<DocumentResponse> responseList = message.getDocumentResponse();
	            Iterator<DocumentResponse> docIterator = responseList.iterator();
	            while (docIterator.hasNext()) {
	            	DocumentResponse docResponse = (DocumentResponse) docIterator.next();
	            	retrieveResponse.getDocumentResponse().add(docResponse);
	            }

				// Attempt to get MTOM attachements for logging - not successful -- yet	            
	            //getRawDataFromDocuments(retrieveResponse);           
	            	            
	            // Serialize the object
	            @SuppressWarnings("rawtypes")
	            JAXBElement oJaxbElement = factory.createRetrieveDocumentSetResponse(retrieveResponse);  

	            marshaller.marshal(oJaxbElement, baOutStrm);
	            baOutStrm.close();
	            messageBytes = baOutStrm.toByteArray();
	        } 
			catch (JAXBException e) {
	        	// THe audit entry should not fail just because the JAXB marshalling failed here
	            e.printStackTrace();
	            LOG.error("Marshalling the marshallDocumentRetrieveResponse message generated a run-time exception. Message not logged.");
	        }
	        catch (IOException e) {
	        	// The audit entry should not fail just because the JAXB marshalling failed here
	            e.printStackTrace();
	            LOG.warn("Marshalling the marshallDocumentRetrieveResponse message generated an I/O Exception. Document retrieve message not logged.");
	        }
        }
		return messageBytes;
    }
    
    private static Boolean isDocRetrieveResponseBodyRedactionEnabled() {
        LOG.debug("Entering... DocumentRetrieveTransforms.isDocRetrieveResponseBodyRedactionEnabled()");
        boolean redactionEnabled = false;
        try {
            LOG.debug("Obtaining doc retrieve response audit redaction flag (" + PROPERTY_KEY_AUDIT_DOC_RETRIEVE_RESPONSE_REDACTION + 
                    ") from " + GATEWAY_PROPERTY_FILE + ".properties");
            PropertyAccessor pa = PropertyAccessor.getInstance();
            redactionEnabled = pa.getPropertyBoolean(GATEWAY_PROPERTY_FILE, PROPERTY_KEY_AUDIT_DOC_RETRIEVE_RESPONSE_REDACTION);
        } catch (PropertyAccessException e) {
            // Defaults to false - ignore exception
            LOG.error("Error reading properties file for doc retrieve response audit bypass: " + e.getMessage(), e);
        }
        LOG.debug("Exiting... DocumentRetrieveTransforms.isDocRetrieveResponseBodyRedactionEnabled returning: " + redactionEnabled);
        return new Boolean(redactionEnabled);
    }
    
    /**
     * Parses the payload as a file URI and converts it into data handlers pointing to the actual documents.
     * 
     * @param msg
     * @throws IOException
     * @throws URISyntaxException
     * 

    private static void getRawDataFromDocuments(RetrieveDocumentSetResponseType msg) 
    		throws IOException {
        LargeFileUtils lfHandle = LargeFileUtils.getInstance();
    	List<DocumentResponse> docResponseList = msg.getDocumentResponse();
        for (DocumentResponse docResponse : docResponseList) {
        	byte [] rawData = new byte[0];
        	DataHandler dh = docResponse.getDocument();
			rawData = lfHandle.convertToBytes(dh);
       }
    }     */
}
