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
package gov.hhs.fha.nhinc.auditrepository.nhinc;

import gov.hhs.fha.nhinc.common.auditlog.LogEventSecureRequestType;
import gov.hhs.fha.nhinc.common.nhinccommon.AcknowledgementType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.FindCommunitiesAndAuditEventsResponseType;
import gov.hhs.fha.nhinc.hibernate.AdvancedAuditRecord;
import gov.hhs.fha.nhinc.hibernate.AuditRepositoryDAO;
import gov.hhs.fha.nhinc.hibernate.AuditRepositoryRecord;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.transform.audit.AuditDataTransformConstants;
import gov.hhs.fha.nhinc.transform.marshallers.JAXBContextHandler;
import gov.hhs.fha.nhinc.util.Base64Coder;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.Blob;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import com.services.nhinc.schema.auditmessage.AuditMessageType;
import com.services.nhinc.schema.auditmessage.AuditMessageType.ActiveParticipant;
import com.services.nhinc.schema.auditmessage.AuditSourceIdentificationType;
import com.services.nhinc.schema.auditmessage.CodedValueType;
import com.services.nhinc.schema.auditmessage.EventIdentificationType;
import com.services.nhinc.schema.auditmessage.FindAuditEventsResponseType;
import com.services.nhinc.schema.auditmessage.FindAuditEventsType;
import com.services.nhinc.schema.auditmessage.ObjectFactory;
import com.services.nhinc.schema.auditmessage.ParticipantObjectIdentificationType;

/**
 * 
 * @author mflynn02
 * @author rhaslam - Adjusted for the Auditing Specification update (2013)
 */
public class AuditRepositoryOrchImpl {
    private static final Logger LOG = Logger.getLogger(AuditRepositoryOrchImpl.class);
    private  AuditRepositoryDAO auditDAO = null; 
    private static String logStatus = "";

    /**
     * constructor.
     */
    public AuditRepositoryOrchImpl() {
        LOG.debug("AuditRepositoryOrchImpl Initialized");
    }

    protected AuditRepositoryDAO getAuditRepositoryDAO() {
    	auditDAO = (auditDAO != null ? auditDAO : new AuditRepositoryDAO());
    	return auditDAO;
    }
    
    /**
     * This method is the actual implementation method for AuditLogMgr Service to Log the AuditEvents and responses the
     * status of logging.
     * 
     * @param mess the message
     * @param assertion the assertion
     * @return gov.hhs.fha.nhinc.common.nhinccommon.AcknowledgementType
     * 
     * The content of mess is:
     *  * AuditMessage (see below)
	 *
     * The contents of mess.getAuditMessage() include:
     * 	*List<ActiveParticipant>
     *  *List<ParticipantObjectIdentification>
     *  *List<AuditSourceIdedntification>
     *  *EventIdentification
     */
    public AcknowledgementType logAudit(LogEventSecureRequestType mess, AssertionType assertion) {
        LOG.debug("Entering...." + this.getClass().getName() +".logAudit()");
        auditDAO = getAuditRepositoryDAO();
        
        AuditRepositoryRecord baseAuditRecord = new AuditRepositoryRecord();
        AdvancedAuditRecord advancedAuditRecord = new AdvancedAuditRecord();
        AuditMessageType auditMessage = mess.getAuditMessage();
        boolean crossLinkRecords = false;
        
        // Insert direction and interface
        String direction = mess.getDirection();
        String _interface = mess.getInterface();
        if ((direction != null) && (! direction.isEmpty())) {
        	crossLinkRecords = true;
        }

        if ((_interface != null) && (! _interface.isEmpty())) {
        	crossLinkRecords = true;
        }
        
        /*
         *  In the current implementation of the Audit Repository the two strings
         *  _interface and direction share a single column in the table (Yuck!)
         */
        baseAuditRecord.setMessageType(_interface + " " + direction);
        
        String userId = null;
        boolean userIdFound = false;
        String userName = null;
        boolean userNameFound = false;
        String altUserId = null;
        String savedAltUserId = null;
        String sourceSystem = null;
        boolean sourceSystemFound = false;
        @SuppressWarnings("unused") // Only used to say of the source system is denoted by IP address on DNS address, both are strings so we don't care.
		Short  networkId = null;
        StringBuffer roleBuffer = new StringBuffer();
        boolean userRolesFound = false;
        boolean communityNameFound = false;
        boolean serviceNameFound = false;
        boolean messageIdFound = false;
        
        // There's lots of Good Stuff here that formerly was not persisted
        List<ActiveParticipant> activeParticipantList = auditMessage.getActiveParticipant();
        // Pull apart ActiveParticipantRecords looking for user information
        if (activeParticipantList != null) { 
	       	for (ActiveParticipant activePart : activeParticipantList) {
        		if (activePart != null) {
        			if (activePart.getNetworkAccessPointID() != null || activePart.getNetworkAccessPointTypeCode() != null){
        				continue; //The should be a source or destination record that does note include user information 
        			}
        			
        			userId = activePart.getUserID();
    				userName = activePart.getUserName();
                	List<CodedValueType> roleValueList = activePart.getRoleIDCode();
                	
	        		if (!userIdFound && userId != null && userId.length() > 0) {
	                    baseAuditRecord.setUserId(userId);
	                    userIdFound = true;
	        		}
	        		if (savedAltUserId != null && altUserId != null && altUserId.length() > 0) {
	        			savedAltUserId = altUserId;
	        		}
	        		if (!userNameFound && userName != null && userName.length() > 0) {
	        			advancedAuditRecord.setUserName(userName);
	        			userNameFound = true;
	        		}
	        		if (!sourceSystemFound && sourceSystem != null && sourceSystem.length() > 0){
	        			advancedAuditRecord.setSourceSystem(sourceSystem);
	        			sourceSystemFound = true;
	        		}
	                if ((! userRolesFound) && roleValueList != null && roleValueList.size() > 0) {
	                	Iterator<CodedValueType> it = roleValueList.iterator();
	                	while (it.hasNext()) {
	                		CodedValueType current = it.next();
	                		if (current != null){
		                		if ((current.getCode().equals(AuditDataTransformConstants.AP_SOURCE_TYPE_CODE) || (current.getCode().equals(AuditDataTransformConstants.AP_DESTINATION_TYPE_CODE)))) {
		                			// These don't contain user roles skip them
		                			continue;
		                		}
		                		if ((current.getDisplayName() != null) && (current.getDisplayName().length() > 0)) {
		                			roleBuffer.append(current.getDisplayName());
		                			roleBuffer.append(" ");
		                			userRolesFound = true;
		                		}
			                	// Remove the trailing " "
			                	if (roleBuffer.length() > 0) {
			                		roleBuffer.setLength(roleBuffer.length()-1);
			                	}
	                		}
	                	}
	                }
        		}
        	}
			if (userName == null) {
				LOG.warn("AuditRepositoryOrchImpl.logAudit(): There were no ActiveParticipant Human Requestor record was identified.");
			}
        }

        // Pull apart ActiveParticipantRecords looking for All informnation. We gave preference above to what should be the
        // Human Requestor ActiveParticipant record
        if (activeParticipantList != null) { 
	       	for (ActiveParticipant activePart : activeParticipantList) {
        		if (activePart != null) {
        			userId = activePart.getUserID();
    				userName = activePart.getUserName();
                	List<CodedValueType> roleValueList = activePart.getRoleIDCode();
                	sourceSystem = activePart.getNetworkAccessPointID();
                	altUserId = activePart.getAlternativeUserID();
                	
	        		if (!userIdFound && userId != null && userId.length() > 0) {
	                    baseAuditRecord.setUserId(userId);
	                    userIdFound = true;
	        		}
	        		if (savedAltUserId != null && altUserId != null && altUserId.length() > 0) {
	        			savedAltUserId = altUserId;
	        		}
	        		if (!userNameFound && userName != null && userName.length() > 0) {
	        			advancedAuditRecord.setUserName(userName);
	        			userNameFound = true;
	        		}
	        		if (!sourceSystemFound && sourceSystem != null && sourceSystem.length() > 0){
	        			advancedAuditRecord.setSourceSystem(sourceSystem);
	        			sourceSystemFound = true;
	        		}
	                if ((! userRolesFound) && roleValueList != null && roleValueList.size() > 0) {
	                	Iterator<CodedValueType> it = roleValueList.iterator();
	                	while (it.hasNext()) {
	                		CodedValueType current = it.next();
	                		if (current != null){
		                		if ((current.getCode().equals(AuditDataTransformConstants.AP_SOURCE_TYPE_CODE) || (current.getCode().equals(AuditDataTransformConstants.AP_DESTINATION_TYPE_CODE)))) {
		                			// These don't contain user roles skip them
		                			continue;
		                		}
		                		if ((current.getDisplayName() != null) && (current.getDisplayName().length() > 0)) {
		                			roleBuffer.append(current.getDisplayName());
		                			roleBuffer.append(" ");
		                			userRolesFound = true;
		                		}
			                	// Remove the trailing " "
			                	if (roleBuffer.length() > 0) {
			                		roleBuffer.setLength(roleBuffer.length()-1);
			                	}
	                		}
	                	}
	                }
        		}
        	}
        }
		else {
			LOG.error("AuditRepositoryOrchImpl.logAudit(): There were no ActiveParticipant records to process.");
		}

        // Finish Populating acquired fields in the advanced audit record
        advancedAuditRecord.setUserRoles(roleBuffer.toString());
        advancedAuditRecord.setMessageDirection(direction);
        advancedAuditRecord.setSubsystem(_interface);
        
        // Populate the auditRecord with userId
        if (userId != null && savedAltUserId != null) {
            baseAuditRecord.setUserId(userId);
        }
        else if (savedAltUserId != null && savedAltUserId.length() > 0){
        	baseAuditRecord.setUserId(savedAltUserId);
        }
    
        // Process the AuditSourceIdentification record
        /*
         * Note: as of the 2013 Specification for PD, DQ, DR and XDR there is no specification the content of this record
         * Thus a SPEC compliant message need not contain any content here. Any content that is here can be treated as 
         * data that has piggy-backed itself into undefined fields in the message. One day those fields might be defined
         * 
         * It is clear based on former use, that the home community id has been passed here as the AuditSourceId.
         */
        String senderCommunityId = null;        
        String senderCommunityName = null;
        List<AuditSourceIdentificationType> auditSourceIdentificationList = null;
        auditSourceIdentificationList = auditMessage.getAuditSourceIdentification();
        if (auditSourceIdentificationList != null && auditSourceIdentificationList.size() > 0) {
            Iterator<AuditSourceIdentificationType> it = auditSourceIdentificationList.iterator();
            while (it.hasNext()){
            	AuditSourceIdentificationType ast = it.next();
            	senderCommunityId = ast.getAuditSourceID();
            	senderCommunityName = ast.getAuditEnterpriseSiteID();
            	if (senderCommunityId != null){
            		break;
            	}
            }
        }
        
        // Process the ParticipantObjectIdentification record
        /*
         * Formerly only 1 record was expected in this message, but now, in some cases, there will be multiple records
         */
        short partObjectTypeCode = 0;
        short partObjectTypeCodeRole = 0;
        
        List<ParticipantObjectIdentificationType> participantObjectIdentificationList = auditMessage.getParticipantObjectIdentification();
        
        ParticipantObjectIdentificationType patient = null, job = null, query = null, document = null, data = null, community = null;
        
        if (participantObjectIdentificationList != null && participantObjectIdentificationList.size() > 0) {
			for (ParticipantObjectIdentificationType participantObjectEntry: participantObjectIdentificationList ){
	            if (participantObjectEntry != null) {
	            	/*
	            	 *  recognize the type of record from the ObjectTypeCode / ObjectTypeRoleCode
	            	 *  patient 	= 1, 1
	            	 *  document 	= 2, 3
	            	 *  data 		= 2,64	This record type is not in the Specifications
	            	 *  job 		= 2,20
	            	 *  query 		= 2,24
	            	 *  community	= 2,96  This record type is not in the Specifications
	            	 */
	                partObjectTypeCode = participantObjectEntry.getParticipantObjectTypeCode();
	                partObjectTypeCodeRole = participantObjectEntry.getParticipantObjectTypeCodeRole();
	                
	                if ((partObjectTypeCode == AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_PERSON) && 
	                		(partObjectTypeCodeRole == AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_PATIENT)){
	                	patient = captureParticipantRecord(participantObjectEntry);
	                }
	                else if ((partObjectTypeCode == AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM) && 
	                		(partObjectTypeCodeRole == AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_JOB)) {
	                	job = captureParticipantRecord(participantObjectEntry);
	                }
	                else if ((partObjectTypeCode == AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM) && 
	                		(partObjectTypeCodeRole == AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_QUERY)) {
	                	query = captureParticipantRecord(participantObjectEntry);
	                }
	                else if ((partObjectTypeCode == AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM) && 
	                		(partObjectTypeCodeRole == AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_REPORT)) {
	            		document = captureParticipantRecord(participantObjectEntry);
	                }
	                else if ((partObjectTypeCode == AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM) &&
	                		(partObjectTypeCodeRole == AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_DATA_TRANSPORT)) {
	            		data = captureParticipantRecord(participantObjectEntry);
	                }
	                else if ((partObjectTypeCode == AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM) &&
	                		(partObjectTypeCodeRole == AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_COMMUNITY)) {
	            		community = captureParticipantRecord(participantObjectEntry);
	                }
	            }
			}
        }
		// The preference order for selecting the ParticipantObjectIdentification that will be persisted is:
    		
        boolean participantObjectSelected = false;
        ParticipantObjectIdentificationType selected = null;
  
	    if ((document != null) && (! participantObjectSelected)) { 
	    	baseAuditRecord.setParticipationTypeCode(document.getParticipantObjectTypeCode());
	    	baseAuditRecord.setParticipationTypeCodeRole(document.getParticipantObjectTypeCodeRole());
	    	baseAuditRecord.setReceiverPatientId(document.getParticipantObjectID());
	    	baseAuditRecord.setParticipationIDTypeCode(document.getParticipantObjectIDTypeCode().getCode());
	    	LOG.debug("Document information found in the Audit Message");
	    	participantObjectSelected = true;
	    	selected = document;
        }

        if ((job != null) && (! participantObjectSelected)) {
	    	baseAuditRecord.setParticipationTypeCode(job.getParticipantObjectTypeCode());
	    	baseAuditRecord.setParticipationTypeCodeRole(job.getParticipantObjectTypeCodeRole());
	    	baseAuditRecord.setReceiverPatientId(job.getParticipantObjectID());
	    	baseAuditRecord.setParticipationIDTypeCode(job.getParticipantObjectIDTypeCode().getCode());
            LOG.debug("Job information found in the Audit Message");
            participantObjectSelected = true;
            selected = job;
        }
        
        if ((patient != null) && (!participantObjectSelected)){
	    	baseAuditRecord.setParticipationTypeCode(patient.getParticipantObjectTypeCode());
	    	baseAuditRecord.setParticipationTypeCodeRole(patient.getParticipantObjectTypeCodeRole());
	    	baseAuditRecord.setReceiverPatientId(patient.getParticipantObjectID());
	    	baseAuditRecord.setParticipationIDTypeCode(patient.getParticipantObjectIDTypeCode().getCode());
            LOG.debug("Patient Information found in the Audit Message");
            participantObjectSelected = true;
            selected = patient;
        }
        
        if ((query != null) && (! participantObjectSelected)) {
	    	baseAuditRecord.setParticipationTypeCode(query.getParticipantObjectTypeCode());
	    	baseAuditRecord.setParticipationTypeCodeRole(query.getParticipantObjectTypeCodeRole());
	    	baseAuditRecord.setReceiverPatientId(query.getParticipantObjectID());
	    	baseAuditRecord.setParticipationIDTypeCode(query.getParticipantObjectIDTypeCode().getCode());
            LOG.debug("Query Params found in the Audit Message");
            participantObjectSelected = true;
            selected = query;
        }

        if ((community != null) && (! participantObjectSelected)) {
	    	baseAuditRecord.setParticipationTypeCode(community.getParticipantObjectTypeCode());
	    	baseAuditRecord.setParticipationTypeCodeRole(community.getParticipantObjectTypeCodeRole());
	    	baseAuditRecord.setReceiverPatientId(community.getParticipantObjectID());
	    	baseAuditRecord.setParticipationIDTypeCode(community.getParticipantObjectIDTypeCode().getCode());
            LOG.debug("Query Params found in the Audit Message");
            participantObjectSelected = true;
            selected = community;
        }
        /*
         *  If the community Id was passed in the community ParticipantObjectIdentification record use it
         *  otherwise, use the value that came through the auditSourceRecords above
         */
        
        if (community != null) {
        	if (community.getParticipantObjectID() != null) {
        		baseAuditRecord.setCommunityId(community.getParticipantObjectID());
        	}
        	if ((community.getParticipantObjectName() != null) && (community.getParticipantObjectName().length() > 0)) {
        		advancedAuditRecord.setSourceCommunity(community.getParticipantObjectName());
        		communityNameFound = true;
        	}
        }

        // If we did not get the senderCommunityId from the Community ParticipantObjectIdentification record
        // we look at the auditSource record (legacy location) for it.
        if ((baseAuditRecord.getCommunityId() == null) && (senderCommunityId != null) && (senderCommunityId.length() > 0)) {
        	baseAuditRecord.setCommunityId(senderCommunityId);
        }

        // If we did not get the senderCommunityName Information from the Community ParticipantObjectIdentification record
        // we look at the auditSource record (legacy location) for it.
        if ((advancedAuditRecord.getSourceCommunity() == null) && (senderCommunityName != null) && (senderCommunityName.length() > 0)) {
        	advancedAuditRecord.setSourceCommunity(senderCommunityName);
        	communityNameFound = true;
        }

    	// Grab the messageID if available
        if(data != null && data.getParticipantObjectID() != null && data.getParticipantObjectID().length() > 0) {
    		String messageId = data.getParticipantObjectID();
    		if (messageId.startsWith("urn:uuid:")) {
    			messageId = messageId.substring(9);
    		}
    		advancedAuditRecord.setMessageId(messageId);
    		messageIdFound = true;
    	}
    	// If query params were passed, then we fill in the query_params field in the advanved_audit table
        if (query != null && query.getParticipantObjectQuery() != null && query.getParticipantObjectQuery().length > 0) {
        	char[] encodedQuery = Base64Coder.encode(query.getParticipantObjectQuery());
        	String sEncodedQuery = new String(encodedQuery);
        	advancedAuditRecord.setQueryParams(Hibernate.createClob(sEncodedQuery));
        }
        // Now we deal with the message being audited itself
        // The way this is handled is largely for the Message Auditing in Aurion 4.1 and earlier
        byte[] messageBeingAudited = null;
        
        //This represents the content of the message being audited - not the Audit Message itself
        if (data != null  && data.getParticipantObjectQuery() != null && data.getParticipantObjectQuery().length > 0) {
        	messageBeingAudited = data.getParticipantObjectQuery();
        }
        
        // When we have a data object we always take the Message Being Audited from it.
		// For Aurion 4.1 compatability we have to do special processing.
        if (messageBeingAudited!= null && participantObjectSelected) {
        	// This is for compatibility with the old world.
        	// Use fields from everything from the selected record above except for ObjectQuery 
        	// which must be the AuditMessage to be compatible with Aurion 4.1
       		ParticipantObjectIdentificationType reconstituteParticipantObject = new ParticipantObjectIdentificationType();
        	reconstituteParticipantObject.setParticipantObjectTypeCode(selected.getParticipantObjectTypeCode());
        	reconstituteParticipantObject.setParticipantObjectTypeCodeRole(selected.getParticipantObjectTypeCodeRole());
        	reconstituteParticipantObject.setParticipantObjectID(selected.getParticipantObjectID());
        	reconstituteParticipantObject.setParticipantObjectName(selected.getParticipantObjectName());
        	reconstituteParticipantObject.setParticipantObjectIDTypeCode(selected.getParticipantObjectIDTypeCode());
        	reconstituteParticipantObject.setParticipantObjectQuery(messageBeingAudited);
        		
        	//Remove any existing participant objects and add this one - backwarads compatability audit_repository table
        	auditMessage.getParticipantObjectIdentification().clear();
        	auditMessage.getParticipantObjectIdentification().add(reconstituteParticipantObject);
        }
        
        if (! participantObjectSelected) {
        	LOG.error(this.getClass().getName() +".logAudit(): No base ParticipantObjectRecord found - some columns in the auditrepo.auditrepository table will be empty.");
        }    
        
		if (baseAuditRecord.getMessage() == null) {
        	baseAuditRecord.setMessage(getBlobFromAuditMessage(mess.getAuditMessage()));
		}
		// A copy of just the message being audited is placed in the advanced audit table
		// Hopefully one day the audit_repository table can be abandoned all-together.
 		if ((advancedAuditRecord.getMessageAudited() == null) && (messageBeingAudited != null)) {
 			char[] encodedMessage = Base64Coder.encode(messageBeingAudited);
 			String sEncodedMessage = new String(encodedMessage);
			advancedAuditRecord.setMessageAudited(Hibernate.createClob(sEncodedMessage));
		}
		
        EventIdentificationType eventIdentification = auditMessage.getEventIdentification();
     
        if (eventIdentification.getEventTypeCode() != null) {
        	List<CodedValueType> eventList = eventIdentification.getEventTypeCode();
        	Iterator<CodedValueType> it = eventList.iterator();
        	while(it.hasNext()) {
        		CodedValueType eventType = it.next();
        		if (eventType.getDisplayName() != null && eventType.getDisplayName().length() > 0){
        			advancedAuditRecord.setServiceName(eventType.getDisplayName());
        			serviceNameFound = true;
        			break;
        		}
        	}
        }
        // Use the GMT Date as computed and stored in the eventIdentification record as the audit timestamp
        XMLGregorianCalendar xMLCalDate = eventIdentification.getEventDateTime();
        if (xMLCalDate != null) {
            Date eventTimeStamp = null;
            eventTimeStamp = convertXMLGregorianCalendarToDate(xMLCalDate);
            baseAuditRecord.setTimeStamp(eventTimeStamp);
        }
        // Should the above code not work, we have a backup-strategy
        if (baseAuditRecord.getTimeStamp() == null) {
        	TimeZone tz = TimeZone.getTimeZone("UTC");
            Calendar cal =Calendar.getInstance(tz);
        	baseAuditRecord.setTimeStamp(cal.getTime());
        }
        
        // The result indicator is always ZERO.
        if (eventIdentification.getEventOutcomeIndicator() != null) {
        	BigInteger iStatus = eventIdentification.getEventOutcomeIndicator();
        	long lStatus = iStatus.longValue();
        	baseAuditRecord.setEventId(lStatus);
        }
        else {
        	baseAuditRecord.setEventId(0L);
        }
       
        // Grab the PurposeOfUse from the ASSERTION passed to the AuditRepositoryAdapter and
        // insert it into the table
        String eventPurposeOfUse = null;
        // gets the purposeOfUse from the message and adds to the audit record
        // maintained two different if statements in case someone adds the uniquePatientId logic later
        if (assertion != null) {
            if (assertion.getPurposeOfDisclosureCoded() != null &&
                    NullChecker.isNotNullish(assertion.getPurposeOfDisclosureCoded().getCode())) {
                eventPurposeOfUse = assertion.getPurposeOfDisclosureCoded().getCode();
                baseAuditRecord.setPurposeOfUse(eventPurposeOfUse);
            }
        }
        
        /*
         * If there is data in the advancedAuditRecord cross link it with the baseAuditRecord
         * 
         * The link will cause the data of advancedAuditRecord to stored in its own table with the same key value as 
         * the record in the base table. If the link is not performed, nothing will be stored in the advanced table. 
         */
        if (crossLinkRecords || userNameFound || userRolesFound || serviceNameFound || sourceSystemFound || communityNameFound || messageIdFound) {
        	baseAuditRecord.setAdvancedAuditRecord(advancedAuditRecord);
        	advancedAuditRecord.setAuditRepositoryRecord(baseAuditRecord);
        }

        Long recordId = auditDAO.insertAuditRepositoryRecord(baseAuditRecord);

        AcknowledgementType response = new AcknowledgementType();
        if (recordId != null && recordId.longValue() > 0) {
        	response.setMessage("Created Message Audit Record in Database. RecordId = " + recordId.longValue());
        } 
        else {
            response.setMessage("Unable to create Message Audit Record in Database...");
            LOG.error(this.getClass().getName() + ".AuditRepositoryOrchImpl.logAudit() failed to save the Message Audit Record in the database.");
        }
        LOG.debug("Existing..." + this.getClass().getName() + ".AuditRepositoryOrchImpl.logAudit()");
        return response;
    }
    
    /*
     * Populates a new ParticipantObjectIdentificationType record based on what records are found in the list
     */
    private ParticipantObjectIdentificationType captureParticipantRecord (final ParticipantObjectIdentificationType rec) {
    	ParticipantObjectIdentificationType participantObject = new ParticipantObjectIdentificationType();
    	participantObject.setParticipantObjectTypeCode(rec.getParticipantObjectTypeCode());
    	participantObject.setParticipantObjectTypeCodeRole(rec.getParticipantObjectTypeCodeRole());
    	participantObject.setParticipantObjectID(rec.getParticipantObjectID());
    	participantObject.setParticipantObjectIDTypeCode(rec.getParticipantObjectIDTypeCode());
    	participantObject.setParticipantObjectName(rec.getParticipantObjectName());
    	participantObject.setParticipantObjectQuery(rec.getParticipantObjectQuery());
		
    	// These fields are generally not now used..
    	participantObject.setParticipantObjectIDTypeCode(rec.getParticipantObjectIDTypeCode());
    	participantObject.setParticipantObjectDataLifeCycle(rec.getParticipantObjectDataLifeCycle());
    	
    	return participantObject;
    }
		
    /*
     * Ouch!  This is the Connect/Aurion method that creates a BLOB for storage in the Audit Record
     * 
     * But Why would you want to store the audit message itself, would it not be better to store the message being audited?
     * YES it would but the original CONNECT audit code stored the auditMessage with the message being audited inside it,
     * so for backwards compatability we do the same.
     */

    private Blob getBlobFromAuditMessage(com.services.nhinc.schema.auditmessage.AuditMessageType mess) {
        Blob auditMessageBlob = null; // Not Implemented
        try {
            JAXBContextHandler oHandler = new JAXBContextHandler();
            JAXBContext jc = oHandler.getJAXBContext("com.services.nhinc.schema.auditmessage");
            Marshaller marshaller = jc.createMarshaller();
            ByteArrayOutputStream baOutStrm = new ByteArrayOutputStream();
            baOutStrm.reset();
            com.services.nhinc.schema.auditmessage.ObjectFactory factory = 
            		new com.services.nhinc.schema.auditmessage.ObjectFactory();
            JAXBElement<AuditMessageType> oJaxbElement = factory.createAuditMessage(mess);
            marshaller.marshal(oJaxbElement, baOutStrm);
            byte[] buffer = baOutStrm.toByteArray();
            //baOutStrm.close(); this call is documented to do nothing so we won't call it.
            auditMessageBlob = Hibernate.createBlob(buffer);
        } catch (Exception e) {
            LOG.error("Exception during Blob conversion for the audit message :" + e.getMessage());
            e.printStackTrace();
        }
        return auditMessageBlob;
    }

    /**
     * This is the actual implementation for AuditLogMgr Service for AuditQuery returns the AuditEventsReponse.
     * 
     * @param query the query
     * @param assertion the assertion
     * @return the found FindAuditEventsResponseType 
     */
    public FindCommunitiesAndAuditEventsResponseType findAudit(FindAuditEventsType query, AssertionType assertion) {
        LOG.debug("AuditRepositoryOrchImpl.findAudit() -- Begin");

        if (logStatus.equals("")) {
            logStatus = "on";
        }

        if (logStatus.equalsIgnoreCase("off")) {
            LOG.info("Enable Audit Logging Before Making Query by changing the "
                    + "value in 'auditlogchoice' properties file");
            return null;
        }
        FindCommunitiesAndAuditEventsResponseType auditEvents = new FindCommunitiesAndAuditEventsResponseType();
        String patientId = query.getPatientId();
        String userId = query.getUserId();
        Date beginDate = null;
        Date endDate = null;
        XMLGregorianCalendar xmlBeginDate = query.getBeginDateTime();
        XMLGregorianCalendar xmlEndDate = query.getEndDateTime();

        if (xmlBeginDate != null) {
            beginDate = convertXMLGregorianCalendarToDate(xmlBeginDate);
        }
        if (xmlEndDate != null) {
            endDate = convertXMLGregorianCalendarToDate(xmlEndDate);
        }
		AuditRepositoryDAO auditDAO = getAuditRepositoryDAO();
        List<AuditRepositoryRecord> responseList = auditDAO.queryAuditRepositoryOnCriteria(userId, patientId, beginDate, endDate);
        LOG.debug("after query call to logDAO.");
        /* if (responseList != null && responseList.size() > 0) { */
        LOG.debug("responseList is not NULL ");
        auditEvents = buildAuditReponseType(responseList);
        /* } */

        LOG.debug("AuditRepositoryOrchImpl.findAudit() -- End");
        return auditEvents;
    }

    /**
     * This method builds the Actual Response from each of the EventLogList coming from Database.
     * 
     * @param eventsList
     * @return CommunitiesAndFindAdutiEventResponse
     */
    private FindCommunitiesAndAuditEventsResponseType buildAuditReponseType(List<AuditRepositoryRecord> auditRecList) {
        LOG.debug("AuditRepositoryOrchImpl.buildAuditResponseType -- Begin");
        FindCommunitiesAndAuditEventsResponseType auditResType = new FindCommunitiesAndAuditEventsResponseType();
        FindAuditEventsResponseType response = new FindAuditEventsResponseType();
        AuditMessageType auditMessageType = null;
        Blob blobMessage = null;

        int size = auditRecList.size();
        AuditRepositoryRecord eachRecord = null;
        for (int i = 0; i < size; i++) {
            eachRecord = auditRecList.get(i);
            auditMessageType = new AuditMessageType();
            blobMessage = eachRecord.getMessage();
            if (blobMessage != null) {
                auditMessageType = unMarshallBlobToAuditMess(blobMessage);
                //ActiveParticipant act = (ActiveParticipant) auditMessageType.getActiveParticipant().get(0);
                response.getFindAuditEventsReturn().add(auditMessageType);

                if (auditMessageType.getAuditSourceIdentification().size() > 0
                        && auditMessageType.getAuditSourceIdentification().get(0) != null
                        && auditMessageType.getAuditSourceIdentification().get(0).getAuditSourceID() != null
                        && auditMessageType.getAuditSourceIdentification().get(0).getAuditSourceID().length() > 0) {
                    String tempCommunity = auditMessageType.getAuditSourceIdentification().get(0).getAuditSourceID();
                    if (!auditResType.getCommunities().contains(tempCommunity)) {
                        auditResType.getCommunities().add(tempCommunity);
                        LOG.debug("Adding community " + tempCommunity);
                    }
                }
            }
        }

        auditResType.setFindAuditEventResponse(response);
        LOG.debug("AuditRepositoryOrchImpl.buildAuditResponseType -- End");
        return auditResType;
    }

    /**
     * This method unmarshalls XML Blob to AuditMessage
     * 
     * @param auditBlob
     * @return AuditMessageType
     */
    private AuditMessageType unMarshallBlobToAuditMess(Blob auditBlob) {
        LOG.debug("AuditRepositoryOrchImpl.unMarshallBlobToAuditMess -- Begin");
        AuditMessageType auditMessageType = null;
        try {
            if (auditBlob != null && ((int) auditBlob.length()) > 0) {
                InputStream in = auditBlob.getBinaryStream();
                JAXBContextHandler oHandler = new JAXBContextHandler();
                JAXBContext jc = oHandler.getJAXBContext("com.services.nhinc.schema.auditmessage");
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                JAXBElement jaxEle = (JAXBElement) unmarshaller.unmarshal(in);
                auditMessageType = (AuditMessageType) jaxEle.getValue();
            }
        } catch (Exception e) {
            LOG.error("Blob to Audit Message Conversion Error : " + e.getMessage());
            e.printStackTrace();
        }
        LOG.debug("AuditRepositoryOrchImpl.unMarshallBlobToAuditMess -- End");
        return auditMessageType;
    }

    /**
     * This method converts an XMLGregorianCalendar date to java.util.Date
     * 
     * @param xmlCalDate
     * @return java.util.Date
     */
    private Date convertXMLGregorianCalendarToDate(XMLGregorianCalendar xmlCalDate) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        LOG.info("cal.getTime() -> " + cal.getTime());
        cal.setTime(xmlCalDate.toGregorianCalendar().getTime());
        Date eventDate = cal.getTime();
        LOG.info("eventDate -> " + eventDate);
        return eventDate;
    }

    /**
     * This method converts an XMLGregorianCalendar date to java.util.Date
     * 
     * @param xmlCalDate
     * @return String
     */
    private String convertXMLGregorianCalendarToString(XMLGregorianCalendar xmlCalDate) {
        GregorianCalendar calDate = xmlCalDate.toGregorianCalendar();
        Date eventDate = calDate.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = sdf.format(eventDate);
        return strDate;
    }

}
