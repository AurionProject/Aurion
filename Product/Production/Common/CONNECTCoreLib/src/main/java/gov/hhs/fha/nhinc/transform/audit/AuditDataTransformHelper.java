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

import gov.hhs.fha.nhinc.common.auditlog.LogEventSecureRequestType;
import gov.hhs.fha.nhinc.common.nhinccommon.CeType;
import gov.hhs.fha.nhinc.common.nhinccommon.HomeCommunityType;
import gov.hhs.fha.nhinc.common.nhinccommon.SamlAuthnStatementType;
import gov.hhs.fha.nhinc.common.nhinccommon.UserType;
import gov.hhs.fha.nhinc.properties.PropertyAccessException;
import gov.hhs.fha.nhinc.properties.PropertyAccessor;
import gov.hhs.fha.nhinc.util.Base64Coder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;

import oasis.names.tc.ebxml_regrep.xsd.rim._3.ExternalIdentifierType;

import org.apache.log4j.Logger;

import com.services.nhinc.schema.auditmessage.AuditMessageType;
import com.services.nhinc.schema.auditmessage.AuditMessageType.ActiveParticipant;
import com.services.nhinc.schema.auditmessage.AuditSourceIdentificationType;
import com.services.nhinc.schema.auditmessage.CodedValueType;
import com.services.nhinc.schema.auditmessage.EventIdentificationType;
import com.services.nhinc.schema.auditmessage.ParticipantObjectIdentificationType;

/**
 * 
 * @author MFLYNN02
 * @author rhaslam - Updates for the 2013 Message Audit Specifications
 */
public class AuditDataTransformHelper {

    private static final Logger LOG = Logger.getLogger(AuditDataTransformHelper.class);

    // Properties file keys
    private static final String PROPERTY_FILE_NAME_GATEWAY = "gateway";
    private static final String PROPERTY_FILE_KEY_HOME_COMMUNITY = "localHomeCommunityId";
    private static final String PROPERTY_FILE_KEY_HOME_COMMUNITY_DESCRIPTION = "localHomeCommunityDescription";


    /**
     * Create the <code>EventIdentificationType</code> for an audit log record.
     * 
     * @param actionCode
     * @param eventOutcome
     * @param eventId
	 * @param eventType
     * @return <code>EventIdentificationType</code>
     */
    public static EventIdentificationType createEventIdentification(
    		String actionCode, 
    		Integer eventOutcome,
            CodedValueType eventId,
            CodedValueType eventType) {
        EventIdentificationType eventIdentification = new EventIdentificationType();

        // Set the Event Action Code
        eventIdentification.setEventActionCode(actionCode);

        // Set the Event Action Time
        try {
            java.util.GregorianCalendar today =
                    new java.util.GregorianCalendar(TimeZone.getTimeZone("GMT"));
            javax.xml.datatype.DatatypeFactory factory =
                    javax.xml.datatype.DatatypeFactory.newInstance();
            javax.xml.datatype.XMLGregorianCalendar calendar =
                    factory.newXMLGregorianCalendar(
                    today.get(java.util.GregorianCalendar.YEAR),
                    today.get(java.util.GregorianCalendar.MONTH) + 1,
                    today.get(java.util.GregorianCalendar.DAY_OF_MONTH),
                    today.get(java.util.GregorianCalendar.HOUR_OF_DAY),
                    today.get(java.util.GregorianCalendar.MINUTE),
                    today.get(java.util.GregorianCalendar.SECOND),
                    today.get(java.util.GregorianCalendar.MILLISECOND),
                    0);
            eventIdentification.setEventDateTime(calendar);
        } catch (DatatypeConfigurationException e) {
            LOG.error("DatatypeConfigurationException when createing XMLGregorian Date");
            LOG.error(" message: " + e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            LOG.error("ArrayIndexOutOfBoundsException when createing XMLGregorian Date");
            LOG.error(" message: " + e.getMessage());
        }
        
        // Set the Event Outcome Indicator
        BigInteger eventOutcomeBig = BigInteger.ZERO;
        if (eventOutcome != null) {
            eventOutcomeBig = new BigInteger(eventOutcome.toString());
        }
        eventIdentification.setEventOutcomeIndicator(eventOutcomeBig);

        // Set the Event Id
        if (eventId != null) {
        	eventIdentification.setEventID(eventId);
        }

        // Set the EventTypeCode
        if (eventType != null) {
            eventIdentification.getEventTypeCode().add(eventType);  
        }
       
        return eventIdentification;
    }

    /**
     * Create the event id <code>CodedValueType</code> for an audit log record.
     * 
     * @param eventCode
     * @param eventCodeSys
     * @param eventCodeSysName
     * @param dispName
     * @return <code>CodedValueType</code>
     */
    @Deprecated
    public static CodedValueType createEventId(String eventCode, String eventCodeSys, String eventCodeSysName,
            String dispName) {
        CodedValueType eventId = new CodedValueType();

        // Set the Event Id Code
        eventId.setCode(eventCode);

        // Set the Event Id Codesystem
        eventId.setCodeSystem(eventCodeSys);

        // Set the Event Id Codesystem Name
        eventId.setCodeSystemName(eventCodeSysName);

        // Set the Event Id Display Name
        eventId.setDisplayName(dispName);

        return eventId;
    }
    /**
     * Create a coded value <code>CodedValueType</code> for an audit log record.
     * 
     * @param eventCode
     * @param eventCodeSys
     * @param eventCodeSysName
     * @param dispName
     * @return <code>CodedValueType</code>
     */
    public static CodedValueType createCodedValue(String eventCode, String eventCodeSys, String eventCodeSysName, String dispName, String originalText) {
        CodedValueType cvt = new CodedValueType();

        // Set the Coded Value Code
        cvt.setCode(eventCode);

        // Set the Coded Value CodeSystem
        cvt.setCodeSystem(eventCodeSys);

        // Set the Coded Value CodeSystemName
        cvt.setCodeSystemName(eventCodeSysName);

        // Set the Coded Value CodeSystemDisplayName
        cvt.setDisplayName(dispName);
        
        // Set the Coded Value originalText
        cvt.setOriginalText(originalText);

        return cvt;
    }
    
    public static CodedValueType getPatientParticipantRoleIdCodedValue() {
    	CodedValueType value = createCodedValue(
    		AuditDataTransformConstants.PARTICIPANT_PATIENT_ID_CODE,
    	    AuditDataTransformConstants.PARTICIPANT_PATIENT_ID_SYSTEM,
    	    AuditDataTransformConstants.PARTICIPANT_PATIENT_ID_SYSTEM_NAME,
    	    AuditDataTransformConstants.PARTICIPANT_PATIENT_ID_SYSTEM_DISPLAY_NAME,
    	    null);
    	return value;
    }
    
    public static CodedValueType getDocumentParticipantRoleIdCodedValue() {
    	CodedValueType value = createCodedValue(
       		AuditDataTransformConstants.PARTICIPANT_DOCUMENT_ID_CODE,
    		AuditDataTransformConstants.PARTICIPANT_DOCUMENT_ID_SYSTEM,
    		AuditDataTransformConstants.PARTICIPANT_DOCUMENT_ID_SYSTEM_NAME,
    		AuditDataTransformConstants.PARTICIPANT_DOCUMENT_ID_SYSTEM_DISPLAY_NAME,  		
    		null);
    	return value;
    }
    
    public static CodedValueType getSubmissionSetParticipantRoleIdCodedValue() {
    	CodedValueType value = createCodedValue(
			AuditDataTransformConstants.PARTICIPANT_SUBMISSION_SET_ID_CODE,
			AuditDataTransformConstants.PARTICIPANT_SUBMISSION_SET_ID_SYSTEM,
			AuditDataTransformConstants.PARTICIPANT_SUBMISSION_SET_ID_SYSTEM_NAME,
			AuditDataTransformConstants.PARTICIPANT_SUBMISSION_SET_ID_SYSTEM_DISPLAY_NAME,
    		null);
    	return value;
    }

    public static CodedValueType getQueryParamsParticipantRoleIdCodedValue() {
    	CodedValueType value = AuditDataTransformHelper.createCodedValue(
    	AuditDataTransformConstants.DQ_PARTICIPANT_ID_CODE_QUERY_PARAMS,
    	AuditDataTransformConstants.DQ_PARTICIPANT_ID_SYSTEM_QUERY_PARAMS,
    	AuditDataTransformConstants.DQ_PARTICIPANT_ID_SYSTEM_NAME_QUERY_PARAMS,
    	AuditDataTransformConstants.DQ_PARTICIPANT_ID_SYSTEM_DISPLAY_NAME_QUERY_PARAMS,
    	null);
    	return value;
    }
    
    public static CodedValueType getDataTransportParticipantRoleIdCodedValue() {
    	CodedValueType value = createCodedValue(
    		AuditDataTransformConstants.PARTICIPANT_DATA_OBJECT_ID_CODE,
    		AuditDataTransformConstants.PARTICIPANT_DATA_OBJECT_ID_CODE_SYSTEM,
    		AuditDataTransformConstants.PARTICIPANT_DATA_OBJECT_ID_CODE_SYSTEM_NAME,
    		AuditDataTransformConstants.PARTICIPANT_DATA_OBJECT_ID_SYSTEM_DISPLAY_NAME,
    		null);
    	return value;
    }

    public static CodedValueType getCommunityParticipantRoleIdCode() {
    	CodedValueType value = createCodedValue(
    		AuditDataTransformConstants.PARTICIPANT_DATA_OBJECT_ID_CODE,
    		AuditDataTransformConstants.PARTICIPANT_DATA_OBJECT_ID_CODE_SYSTEM,
    		AuditDataTransformConstants.PARTICIPANT_DATA_OBJECT_ID_CODE_SYSTEM_NAME,
    		AuditDataTransformConstants.PARTICIPANT_DATA_OBJECT_ID_SYSTEM_DISPLAY_NAME,
    		null);
    	return value;
    }
    
 static CodedValueType getActiveParticipantSourceRoleIdCode() {
    	CodedValueType value = createCodedValue(
    		AuditDataTransformConstants.AP_SOURCE_TYPE_CODE,
            AuditDataTransformConstants.AP_SOURCE_TYPE_CODE_SYSTEM,
            AuditDataTransformConstants.AP_SOURCE_TYPE_CODE_SYSTEM_NAME,
            AuditDataTransformConstants.AP_SOURCE_TYPE_CODE_SYSTEM_DISPLAY_NAME,
            null);
    	return value;
    }

    public static CodedValueType getActiveParticipantDestinationRoleIdCode() {
    	CodedValueType value = createCodedValue(
    		AuditDataTransformConstants.AP_DESTINATION_TYPE_CODE,
            AuditDataTransformConstants.AP_DESTINATION_TYPE_CODE_SYSTEM,
            AuditDataTransformConstants.AP_DESTINATION_TYPE_CODE_SYSTEM_NAME,
            AuditDataTransformConstants.AP_DESTINATION_TYPE_CODE_SYSTEM_DISPLAY_NAME,
            null);
    	return value;
    }
    

    /*
     * formatCodedValue
     * 
     * take an object of type CodedValueType and create a comma, separated string value from it
     */
	public static String formatCodedValue(CodedValueType cv){
		StringBuffer stringValue = new StringBuffer();
		
		stringValue.append("EV(");
		stringValue.append(cv.getCode() + ",");
		stringValue.append(cv.getCodeSystem() + ",");
		stringValue.append(cv.getCodeSystemName()+ ",");
		stringValue.append(cv.getDisplayName());
		stringValue.append(")");
		
		// Limit to size of column 
		if(stringValue.length() > 100) {
			stringValue.setLength(100);
		}
			
		
		return stringValue.toString();
	}
    
    public static String getProcessId() {
    	String pid = ManagementFactory.getRuntimeMXBean().getName();
    	return pid;
    }
    
    /**
     * Compute if the message is being sent or received based upon "entity" and "direction"
     * This would be simpler IFF direction were modified to mean
     * 	"the direction with respect to the named interface" rather than the current definition 
     *   which is "direction with respect to nhin
     *   
     * @param _interface
     * @param direction
     * @return
     */
    public static boolean isSender(String _interface, String direction){
        if (_interface.equalsIgnoreCase("entity")) {
        	if (direction.equalsIgnoreCase("inbound")) {
        		return false;
        	}
        	else {
        		return true;
        	}
        }
        else if (_interface.equalsIgnoreCase("nhin")) {
        	if (direction.equalsIgnoreCase("inbound")) {
        		return false;
        	}
        	else {
        		return true;
        	}
        }
        else if (_interface.equalsIgnoreCase("adapter")) {
        	if (direction.equalsIgnoreCase("inbound")) {
        		return true;
        	}
        	else {
        		return false;
        	}
        }
        // it's bad if we get here but we return true to keep the compiler happy
        return true;
    }
    
    /**
     * Find the base ParticipantObjectIdentificationRecord from which items in the base record are populated
     * 
     */
    public static ParticipantObjectIdentificationType findBaseParticipantRecord(List<ParticipantObjectIdentificationType> participantRecordList) {
    	if ((participantRecordList == null) || (participantRecordList.size() == 0)) {
    		return null;
    	}
    	ParticipantObjectIdentificationType patientRecord = null;
    	ParticipantObjectIdentificationType jobRecord = null;
    	ParticipantObjectIdentificationType reportRecord = null;
    	ParticipantObjectIdentificationType	queryRecord = null;
    	for (ParticipantObjectIdentificationType record : participantRecordList){
    		if (record.getParticipantObjectTypeCodeRole() == AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_PATIENT){
    			patientRecord = record;
    		}
    		if (record.getParticipantObjectTypeCodeRole() == AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_JOB){
    			jobRecord = record;
    		}
    		if (record.getParticipantObjectTypeCodeRole() == AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_REPORT){
    			reportRecord = record;
    		}
    		if (record.getParticipantObjectTypeCodeRole() == AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_QUERY){
    			queryRecord = record;
    		}
    	}
    	
    	if (reportRecord != null) {
    		return reportRecord;
    	}
    	if (jobRecord != null) {
    		return jobRecord;
    	}
    	if (patientRecord != null){
    		return patientRecord;
    	}
    	if (queryRecord != null) {
    		return jobRecord;
    	}
    	return null;
    }
    /*
     * find the Data Transport Participant record among the list of ALL Participant records
     */
    public static ParticipantObjectIdentificationType findParticipantRecordByRoleIdCode(List<ParticipantObjectIdentificationType> participantRecordList, short idCode) {
    	if ((participantRecordList == null) || (participantRecordList.size() == 0)) {
    		return null;
    	}
    	ParticipantObjectIdentificationType selectedRecord = null;
    	for (ParticipantObjectIdentificationType record : participantRecordList){
    		if (record.getParticipantObjectTypeCodeRole() == idCode){
    			selectedRecord = record;
    			break;
    		}
    	}
    	return selectedRecord;
    }

    /**
     * Create the <code>AuditMessageType.ActiveParticipant</code> for an audit log record.
     * 
     * @param userInfo
     * @param userIsReq
     * @return <code>AuditMessageType.ActiveParticipant</code>
     */
    @Deprecated
    public static AuditMessageType.ActiveParticipant createActiveParticipantFromUser(UserType userInfo,
            Boolean userIsReq) {
        AuditMessageType.ActiveParticipant participant = new AuditMessageType.ActiveParticipant();

        String ipAddr = null;
        if (ipAddr == null) {
            try {
                ipAddr = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException ex) {
                LOG.error("UnknownHostException thrown getting local host address.", ex);
                throw new RuntimeException();
            }
        }

        // Set the User Id
        String userId = null;
        if (userInfo != null && userInfo.getUserName() != null && userInfo.getUserName().length() > 0) {
            userId = userInfo.getUserName();
        }

        if (userId != null) {
            participant.setUserID(userId);
        }

        // If specified, set the User Name
        String userName = null;
        if (userInfo != null && userInfo.getPersonName() != null) {
            if (userInfo.getPersonName().getGivenName() != null && userInfo.getPersonName().getGivenName().length() > 0) {
                userName = userInfo.getPersonName().getGivenName();
            }

            if (userInfo.getPersonName().getFamilyName() != null
                    && userInfo.getPersonName().getFamilyName().length() > 0) {
                if (userName != null) {
                    userName += (" " + userInfo.getPersonName().getFamilyName());
                } else {
                    userName = userInfo.getPersonName().getFamilyName();
                }
            }
        }
        if (userName != null) {
            participant.setUserName(userName);
        }

        // Set the UserIsRequester Flag
        participant.setUserIsRequestor(userIsReq);

        // Set the Network Access Point Id to the IP Address of the machine
        participant.setNetworkAccessPointID(ipAddr);

        // Set the Network Access Point Typecode
        participant.setNetworkAccessPointTypeCode(AuditDataTransformConstants.NETWORK_ACCESS_POINT_TYPE_CODE_IP);

        return participant;
    }
    /**
     * Create the <code>AuditMessageType.ActiveParticipant</code> for an audit log record.
     * 
     * @param userInfo
     * @param userIsReq
	 * @param CodedValueType roleCodedValue
     * @return <code>AuditMessageType.ActiveParticipant</code>
     */
    public static ActiveParticipant createActiveParticipantForHuman(UserType userInfo)
    {
        ActiveParticipant human = new ActiveParticipant();

        // Set the User Id from user info
        String userId = null;
        if (userInfo != null && userInfo.getUserName() != null && userInfo.getUserName().length() > 0) {
            userId = userInfo.getUserName();
        }

		// strip  "UID=" if present
        if (userId != null) {
			if ((userId.indexOf("UID=") == 0) || (userId.indexOf("uid=") == 0)) {
				userId = userId.substring(4);
			} 
            human.setUserID(userId);
        }

        // Add the user's ROLES - Convert from CeType to CodedValueType
        if (userInfo.getRoleCoded() != null) {
        	CeType ceRoles = userInfo.getRoleCoded();
        	CodedValueType roles = createCodedValue(
        			ceRoles.getCode(),
        			ceRoles.getCodeSystem(),
        			ceRoles.getCodeSystemName(),
        			ceRoles.getDisplayName(),
        			ceRoles.getOriginalText());
        	human.getRoleIDCode().add(roles);
        }

        // If specified, set the User Name
        String userName = null;
        if (userInfo != null && userInfo.getPersonName() != null) {
            if (userInfo.getPersonName().getGivenName() != null && userInfo.getPersonName().getGivenName().length() > 0) {
                userName = userInfo.getPersonName().getGivenName();
            }

            if (userInfo.getPersonName().getFamilyName() != null
                    && userInfo.getPersonName().getFamilyName().length() > 0) {
                if (userName != null) {
                    userName += (" " + userInfo.getPersonName().getFamilyName());
                } else {
                    userName = userInfo.getPersonName().getFamilyName();
                }
            }
        }
        if (userName != null) {
            human.setUserName(userName);
        }

        // Set the UserIsRequester Flag
        human.setUserIsRequestor(true);

        return human;
    }

    /**
     * Create the <code>AuditMessageType.ActiveParticipant</code> for an audit log record.
     * 
     * @param userInfo
     * @param userIsReq
     * @return <code>AuditMessageType.ActiveParticipant</code>
     * 
     * Note: This method came from work done by Harris Corporation between Aurion 4.1 and Aurion 5
     */
    public static ActiveParticipant createActiveParticipantSource(boolean isSender, String ipAddress) {

        // Create Source Active Participant Section
        ActiveParticipant source = new AuditMessageType.ActiveParticipant();

        if (isSender) {
        	source.setAlternativeUserID(getProcessId());
        }
        source.setUserIsRequestor(true);

        source.getRoleIDCode().add(getActiveParticipantSourceRoleIdCode());

        // "1" for machine (DNS) name, "2" for IP address
        source.setNetworkAccessPointTypeCode(AuditDataTransformConstants.NETWORK_ACCESS_POINT_TYPE_CODE_IP);

        //get IP address
        if (ipAddress == null){
	        try {
	        	ipAddress = InetAddress.getLocalHost().getHostAddress();
	        } catch (Exception ex) {
	            ex.printStackTrace();
	            throw new RuntimeException();
	        }
        }
        source.setNetworkAccessPointID(ipAddress);

        return source;
    }
    
    
    /**
     * Create the <code>AuditMessageType.ActiveParticipant</code> for an audit log record. Destination
     * 
     * @param authnStatement
     * @return
     */
    public static ActiveParticipant createActiveParticipantDestination(SamlAuthnStatementType authnStatement, boolean isRecipient) {

        ActiveParticipant destination = new AuditMessageType.ActiveParticipant();

        if (isRecipient) {
        	destination.setAlternativeUserID(getProcessId());
        }
        destination.setUserIsRequestor(false);

        destination.getRoleIDCode().add(getActiveParticipantDestinationRoleIdCode());

        // "1" for machine (DNS) name, "2" for IP address
        destination.setNetworkAccessPointTypeCode(AuditDataTransformConstants.NETWORK_ACCESS_POINT_TYPE_CODE_IP);

        if (authnStatement != null)
            destination.setNetworkAccessPointID(authnStatement.getSubjectLocalityAddress());

        return destination;
    }
    
    public static ActiveParticipant findUserAuditSourceRecord(List<ActiveParticipant> activeList){
    	for (ActiveParticipant record : activeList) {
    		if (record.getNetworkAccessPointTypeCode() == null && record.getNetworkAccessPointID() == null){
    			return record;
    		}
    	}
    	return null;
    }
    
    public static ActiveParticipant findAuditSourceSourceSystemRecord(List<ActiveParticipant> activeList){
    	for (ActiveParticipant record : activeList) {
    		if (record.getRoleIDCode().get(0).getCode().equals(AuditDataTransformConstants.AP_SOURCE_TYPE_CODE)) {
    			return record;
    		}
    	}
    	return null;
    }
    
    public static ActiveParticipant findAuditSourceDestinationSystemRecord(List<ActiveParticipant> activeList){
    	for (ActiveParticipant record : activeList) {
    		if (record.getRoleIDCode().get(0).getCode().equals(AuditDataTransformConstants.AP_DESTINATION_TYPE_CODE)) {
    			return record;
    		}
    	}
    	return null;
    }
    /*
    public static ActiveParticipant createActiveParticipant(String userId, String altUserId, String userName, Boolean userIsReq)
    {
        ActiveParticipant participant = new ActiveParticipant();
        String ipAddr = null;

        try {
            ipAddr = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException();
        }

        // Set the User Id
        if (userId != null) {
            participant.setUserID(userId);
        }

        // If specified, set the Alternative User Id
        if (altUserId != null) {
            participant.setAlternativeUserID(altUserId);
        }

        // If specified, set the User Name
        if (userName != null) {
            participant.setUserName(userName);
        }

        // Set the UserIsRequester Flag
        participant.setUserIsRequestor(userIsReq);

        // Set the Network Access Point Id to the IP Address of the machine
        participant.setNetworkAccessPointID(ipAddr);

        // Set the Network Access Point Typecode
        participant.setNetworkAccessPointTypeCode(AuditDataTransformConstants.NETWORK_ACCESS_POINT_TYPE_CODE_IP);

        return participant;
    }
	*/
    /**
     * Create an <code>AuditSourceIdentificationType</code> based on the user info for an audit log record.
     * 
     * @param userInfo
     * @param auditSourceType
     * @return <code>AuditSourceIdentificationType</code>
     * 
     * This method is deprecated, because the new method to pass Community Information is through a Community Instance
     * of the ParticipantObjectIdentificaation record
     */
    @Deprecated
    public static AuditSourceIdentificationType createAuditSourceIdentificationFromUser(UserType userInfo, CodedValueType auditSourceType) {
        AuditSourceIdentificationType auditSrcIdentification = null;

        // Home Community ID and name                   
        String communityId = null;
        String communityName = null;

        if (userInfo != null &&
                userInfo.getOrg() != null) {
            if (userInfo.getOrg().getHomeCommunityId() != null &&
                    userInfo.getOrg().getHomeCommunityId().length() > 0) {
                communityId = userInfo.getOrg().getHomeCommunityId();
            }

            if (userInfo.getOrg().getName() != null &&
                    userInfo.getOrg().getName().length() > 0) {
                communityName = userInfo.getOrg().getName();
            }
        }
        
        auditSrcIdentification = createAuditSourceIdentification(communityId, communityName, auditSourceType);

        return auditSrcIdentification;
    }

    public static ParticipantObjectIdentificationType createParticipantCommunityRecordFromUser(UserType userInfo) {

        // Home Community ID and name                   
        String communityId = null;
        String communityName = null;

        if ((userInfo != null) && (userInfo.getOrg() != null)) {
        	HomeCommunityType homeCommunity = userInfo.getOrg();
        	if ((homeCommunity.getHomeCommunityId() != null) && (homeCommunity.getHomeCommunityId().length() > 0)) {
                communityId = homeCommunity.getHomeCommunityId();
            }
        	
            if ((homeCommunity.getName() != null) && (homeCommunity.getName().length() > 0)) {
                communityName = homeCommunity.getName();
            }
        }
        
        CodedValueType communityObjectIdType = getCommunityParticipantRoleIdCode();
        ParticipantObjectIdentificationType communityRecord = createParticipantObjectIdentification(
    			AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM,
    			AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_COMMUNITY,
    			communityObjectIdType,
    			communityId,
    			null);
    	communityRecord.setParticipantObjectName(communityName);
        return communityRecord;
    }
    /**
     * Create an <code>AuditSourceIdentificationType</code> based on the the gateway properties file
     * 
     * @param userInfo
     * @param auditSourceType
     * @return <code>AuditSourceIdentificationType</code>
     */
    public static AuditSourceIdentificationType createAuditSourceIdentificationFromProperties(CodedValueType auditSourceType) {
        AuditSourceIdentificationType auditSrcIdentification = null;

        // Home Community ID and name
        String homeCommunityId = null;
        String homeCommunityName = null;
        String currentProperty = null;

        try
        {
        	PropertyAccessor props = PropertyAccessor.getInstance();
            homeCommunityId = props.getProperty(PROPERTY_FILE_NAME_GATEWAY, PROPERTY_FILE_KEY_HOME_COMMUNITY);
            homeCommunityName = props.getProperty(PROPERTY_FILE_NAME_GATEWAY, PROPERTY_FILE_KEY_HOME_COMMUNITY_DESCRIPTION);
        }
        catch (PropertyAccessException e)
        {
            LOG.error("Exception in audit logging when trying to retrieve the gateway Property" + currentProperty);
        }

        auditSrcIdentification = createAuditSourceIdentification(homeCommunityId, homeCommunityName, auditSourceType);
        
        return auditSrcIdentification;
    }
    
    /**
     * Create an <code>AuditSourceIdentificationType</code> based on the community id and community name.
     * 
     * @param communityId
     * @param communityName
     * @return <code>AuditSourceIdentificationType</code>
     */
    public static AuditSourceIdentificationType createAuditSourceIdentification(String communityId, String communityName, CodedValueType auditSourceType) {
        AuditSourceIdentificationType auditSrcIdentification = new AuditSourceIdentificationType();

        // Set the Audit Source Id (community id)
        if (communityId != null) {
            if (communityId.startsWith("urn:oid:")) {
                auditSrcIdentification.setAuditSourceID(communityId.substring(8));
            } else {
                auditSrcIdentification.setAuditSourceID(communityId);
            }
        }

        // If specified, set the Audit Enterprise Site Id (community name)
        if (communityName != null) {
            auditSrcIdentification.setAuditEnterpriseSiteID(communityName);
        }
        
        if (auditSourceType != null) {
        	auditSrcIdentification.getAuditSourceTypeCode().add(auditSourceType);
        }

        return auditSrcIdentification;
    }

    /**
     * Create the <code>ParticipantObjectIdentificationType</code> based on the patient id for an audit log record.
     * 
     * @param patientId
     * @return <code>ParticipantObjectIdentificationType</code>
     * 
     * Replaced by createParticipantObjectIdentification()
     */
    @Deprecated
    public static ParticipantObjectIdentificationType createDocumentParticipantObjectIdentification(String documentId) {
        ParticipantObjectIdentificationType partObjId = new ParticipantObjectIdentificationType();

        // Set the Partipation Object Id (documentId)
        if (documentId != null) {
            partObjId.setParticipantObjectID(documentId);
        }

        // Set the Participation Object Typecode
        partObjId.setParticipantObjectTypeCode(AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM);

        // Set the Participation Object Typecode Role
        partObjId
                .setParticipantObjectTypeCodeRole(AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_REPORT);

        // Set the Participation Object Id Type code
        CodedValueType partObjIdTypeCode = new CodedValueType();
        partObjIdTypeCode.setCode(AuditDataTransformConstants.PARTICIPANT_DOCUMENT_ID_CODE);
        partObjId.setParticipantObjectIDTypeCode(partObjIdTypeCode);

        return partObjId;
    }

    /**
	 * Create the <code>ParticipantObjectIdentificationType</code> record - acting on all parameters\ * 
	 * @param patientId
	 * @return <code>ParticipantObjectIdentificationType</code>
	 */
	public static ParticipantObjectIdentificationType createParticipantObjectIdentification(
			Short objectTypeCode,
			Short objectTypeCodeRole,
			CodedValueType objectIdTypeCode,
			String objectId,
			byte[] data)
	{
		
		ParticipantObjectIdentificationType partObjId = new ParticipantObjectIdentificationType();
	    
	    // Set the ParticipantObjectTypeCode (SYSTEM)
	    partObjId.setParticipantObjectTypeCode(objectTypeCode);
	
	    // Set the ParticipantObjectTypeCodeRole (Different depending on the participating object - patient, document, document set etc).
	    partObjId.setParticipantObjectTypeCodeRole(objectTypeCodeRole);
		
	    // Set the Participation Object Id Type code
	    partObjId.setParticipantObjectIDTypeCode(objectIdTypeCode);
	
	    // Set the Partipant Object Id (documentId) and the ParticipantQuery object
	    partObjId.setParticipantObjectID(objectId);
	    partObjId.setParticipantObjectQuery(data);
	    
	    return partObjId;
	}

	/**
     * Write out debug logging statements based on the given <code>AuditMessageType</code> message.
     * 
     * @param message
     */
    public static void logAuditMessage(AuditMessageType message) {
        LOG.debug("********** Audit Log Message ***********");
        LOG.debug("EventIdCode: " + message.getEventIdentification().getEventID().getCode());
        LOG.debug("EventIdCodeSystem: " + message.getEventIdentification().getEventID().getCodeSystem());

        if (message.getAuditSourceIdentification() != null && message.getAuditSourceIdentification().size() > 0
                && message.getAuditSourceIdentification().get(0).getAuditSourceID() != null) {
            LOG.debug("Home Community Id: " + message.getAuditSourceIdentification().get(0).getAuditSourceID());
        } else {
            LOG.debug("Home Community Id: There was no AuditSourceID in the message");
        }

        if (message.getActiveParticipant() != null && message.getActiveParticipant().size() > 0) {
            if (message.getActiveParticipant().get(0).getUserID() != null) {
                LOG.debug("UserId: " + message.getActiveParticipant().get(0).getUserID());
            } else {
                LOG.debug("UserId: There was no User Id in the message");
            }

            if (message.getActiveParticipant().get(0).getUserName() != null) {
                LOG.debug("UserName: " + message.getActiveParticipant().get(0).getUserName());
            }
        }

        if (message.getParticipantObjectIdentification() != null
                && message.getParticipantObjectIdentification().size() > 0
                && message.getParticipantObjectIdentification().get(0).getParticipantObjectID() != null) {
            LOG.debug("PatientId: " + message.getParticipantObjectIdentification().get(0).getParticipantObjectID());
        } else {
            LOG.debug("PatientId: There was no Patient Id in the message");
        }
    }

    /**
     * This method locates the single ExternalIdentifer object based on the given Identification Scheme.
     * 
     * @param olExtId The list of external identifier objects to be searched.
     * @param sIdentScheme The identification scheme for the item being looked for.
     * @return The <code>ExternalIdentifierType</code> matching the search criteria.
     */
    public static ExternalIdentifierType findSingleExternalIdentifier(List<ExternalIdentifierType> olExtId,
            String sIdentScheme) {
        ExternalIdentifierType oExtId = null;

        if ((olExtId != null) && (olExtId.size() > 0)) {
            for (int i = 0; i < olExtId.size(); i++) {
                if (olExtId.get(i).getIdentificationScheme().equals(sIdentScheme)) {
                    oExtId = olExtId.get(i);
                    break; // We found what we were looking for - get out of here...
                }
            }
        }
        return oExtId;
    }

    /**
     * This method locates a single External Identifier by its Identification scheme and then extracts the 
     * value from it.
     * 
     * @param olExtId The List of external identifiers to be searched.
     * @param sIdentScheme The identification scheme to look for.
     * @return The value from the specified identification scheme.
     */
    public static String findSingleExternalIdentifierAndExtractValue(List<ExternalIdentifierType> olExtId,
            String sIdentScheme) {
        String sValue = null;
        ExternalIdentifierType oExtId = findSingleExternalIdentifier(olExtId, sIdentScheme);
        if ((oExtId != null) && (oExtId.getValue() != null)) {
            sValue = oExtId.getValue();
        }
        return sValue;
    }

    /**
     * This method creates a patient id formatted as 'patientid^^^^&assigningAuthId&ISO'
     * 
     * @param assigningAuthId the assigningAuthId for the patient identifier
     * @param patientId the patientId for the patient
     * @return the properly formatted patientId.
     */
    public static String createCompositePatientId(String assigningAuthId, String patientId) {
        String sValue = null;
        sValue = patientId + "^^^&" + assigningAuthId + "&ISO";
        return sValue;
    }

    /**
     * This method creates a patient id formatted as 'patientid^^^^&communityId&ISO'
     * 
     * @param assertion. Fields of interest are UserInfo.org.homeCommunityId an uniquePatientId
     * @return the properly formatted patientId.
     */
    public static String createCompositePatientIdFromAssertion(UserType userInfo, String patientId) {
        String communityId = null;
        String compPatientId = null;

        if (userInfo != null && userInfo.getOrg() != null && userInfo.getOrg().getHomeCommunityId() != null) {
            communityId = userInfo.getOrg().getHomeCommunityId();
        }
        compPatientId = AuditDataTransformHelper.createCompositePatientId(communityId, patientId);


        return compPatientId;
    }

    /*
     * This method is for DEVELOPMENT ONLY.
     * 
     * It logs the details of the content of a AuditLog Message for analysis versus the specifications for such messages
     * 
     */
    // FOR DEVELOPMENT ONLY
    public static void captureAuditMessage(LogEventSecureRequestType message) {
    	
    	//Get the sub-objects
    	AuditMessageType auditMessage = message.getAuditMessage();
    	List<ActiveParticipant> activeParticipantList = auditMessage.getActiveParticipant();
    	List<AuditSourceIdentificationType> auditSourceIdList = auditMessage.getAuditSourceIdentification();
    	EventIdentificationType eventIdentification = auditMessage.getEventIdentification();
    	List<ParticipantObjectIdentificationType> participantIdentificationList = auditMessage.getParticipantObjectIdentification();
    	
    	String direction = message.getDirection();
    	String _interface = message.getInterface();
    	
    	BufferedWriter out = null;
    	try {
	    	File auditData = new File("/tmp/AuditData.csv");
	    	out = new BufferedWriter((new FileWriter(auditData, true)), 128);
	    	// General
	    	out.write("****************************************************************************************************************************************************************************\n");
	    	out.write("Begin Audit Message  =====  Begin Audit Message   =====  Begin Audit Message   =====  Begin Audit Message   =====  Begin Audit Message   ====  =====  Begin Audit Message   \n");
	    	out.write("Direction, " + direction + "\n");
	    	out.write("Interface, " + _interface + "\n");
	
	    	//Active Participant
	    	int recordCount = activeParticipantList.size();
	    	int currentRecord = 0;
	    	out.write("**************************************************************   Begin ActiveParticipant  Section **************************************************************************\n");
	    	out.write("ActiveParticipant record count, " + recordCount + "\n");
	    	while (currentRecord < recordCount) {
		    	ActiveParticipant activeParticipant = activeParticipantList.get(currentRecord);
	    		out.write("====Begin ActiveParticipant Record " + ++currentRecord + "====\n");
		    	out.write(",UserId, " + activeParticipant.getUserID()+ "\n");
		    	out.write(",UserName, " + activeParticipant.getUserName() + "\n");
		    	if ((activeParticipant.getRoleIDCode() != null) && (activeParticipant.getRoleIDCode().size() > 0)) {
		    		out.write(",RoleIdCode Code," + activeParticipant.getRoleIDCode().get(0).getCode() + "\n");
		    		out.write(",RoleIdCode CodeSystem," + activeParticipant.getRoleIDCode().get(0).getCodeSystem() +"\n");
		    		out.write(",RoleIdCode CodeSystemName," + activeParticipant.getRoleIDCode().get(0).getCodeSystemName()+"\n");
		    		out.write(",RoleIdCode CodeSystemDisplayName," + activeParticipant.getRoleIDCode().get(0).getDisplayName()+"\n");
		    		if (activeParticipant.getRoleIDCode().get(0).getOriginalText() != null) {
		    			out.write(",RoleIdCode OriginalText," + activeParticipant.getRoleIDCode().get(0).getOriginalText() + "\n");
			    	}
			    	else {
			    		out.write(",RoleIdCode OriginalText,null\n");
			    	}
	    		}
		    	else {
		    		out.write(",RoleIdCodeObject,null or empty\n");
		    	}
		    	out.write(",AlternativeUserId, " + activeParticipant.getAlternativeUserID() + "\n");
		    	out.write(",NetworkAccessPoint, " + activeParticipant.getNetworkAccessPointID() + "\n");
		    	out.write(",NetworkAccessPointId, " + activeParticipant.getNetworkAccessPointID() + "\n");
		    	out.write(",UserIsRequestor, " + activeParticipant.isUserIsRequestor() + "\n");
	    	}

	    	// Audit Source Identification
	    	out.write("**********************************************************   Begin Audit Source Identification  Section  ********************************************************************\n");
	    	recordCount = auditSourceIdList.size();
	    	currentRecord = 0;
	    	out.write("AuditSourceIdentitication record count, " + recordCount + "\n");
	    	for (AuditSourceIdentificationType auditSourceId : auditSourceIdList) {	
	    		out.write("====Begin AuditSourceIdentitication Record====" + ++currentRecord + "\n");	    	
		    	String enterpriseId = auditSourceId.getAuditEnterpriseSiteID();
		    	if (enterpriseId == null) {
		    		out.write(",EnterpriseSiteId, null ");
		    	}
		    	else {
		    		out.write(",EnterpriseSiteId, " + auditSourceId.getAuditEnterpriseSiteID()+ "\n");
		    	}
		    	
		    	String auditSourceID = auditSourceId.getAuditSourceID();
		    	if (auditSourceID == null){
		    		out.write(", AuditSourceId, null");
		    	}
		    	else {
			    	out.write(",AuditSourceId, " + auditSourceId.getAuditSourceID()+ "\n");		    		
		    	}

	    		List<CodedValueType> auditSourceTypeList = auditSourceId.getAuditSourceTypeCode();
		    	if ((auditSourceTypeList == null) ||  
		    			(auditSourceTypeList.size() < 1) ||
		    			(auditSourceTypeList.get(0) == null)) {
		    		out.write(",AuditSourceTypeCodeList, null\n");
		    	}
		    	else {
		    		for (CodedValueType auditSourceType : auditSourceTypeList){
			    		if (auditSourceType.getCode() == null){
		    				out.write(", AuditSourceTypeCode, null");
		    			}
		    			else {
				    		out.write(",AuditSourceTypeCode, " + auditSourceType.getCode()+ "\n");   				
		    			}
		    			if (auditSourceType.getCodeSystem() == null) {
		    				out.write(", AuditSourceTypeCodeSystem, null");	    				
		    			}
		    			else {
					    	out.write(",AuditSourceTypeCodeSystem, " + auditSourceType.getCodeSystem()+ "\n");	    				
		    			}
		    			if (auditSourceType.getCodeSystemName() == null) {
		    				out.write(", AudurceTypeCodeSystemName, null");
		    			}
		    			else {
					    	out.write(",AudurceTypeCodeSystemName, " + auditSourceType.getCodeSystemName()+ "\n");    				
		    			}
		    			if (auditSourceType.getDisplayName() == null) {
		    				out.write(", AudurceTypeCodeSystemDisplayName, null");
		    			}
		    			else {
					    	out.write(",AudurceTypeCodeSystemDisplayName, " + auditSourceType.getDisplayName()+ "\n");	    				
		    			}
				    	if (auditSourceType.getOriginalText() == null) {
				    		out.write(",AudurceTypeCode OriginalText, null\n");
				    	}
				    	else {
					    	out.write(",AudurceTypeCode OriginalText, " + auditSourceType.getOriginalText()+ "\n");	    			
				    	}
		    		}
		    	}
	    	}
	    	
	    	// Event Identification
	    	out.write("************************************************************    Begin Event Identitifation Section   ************************************************************************\n");
	    	out.write(",ActionCode,   " + eventIdentification.getEventActionCode()+ "\n");
	    	out.write(",DateTime,     " + eventIdentification.getEventDateTime()+ "\n");
	    	out.write(",EventOutcome, " + eventIdentification.getEventOutcomeIndicator()+ "\n");
	    	CodedValueType eventId = eventIdentification.getEventID();
	    	if (eventId == null){
	    		out.write("EventId, null\n");
	    	}
	    	else {
	    		if (eventId.getCode() == null){
    				out.write(", EventIdCode, null");
    			}
    			else {
    		    	out.write(",EventIdCode        , " + eventId.getCode() + "\n");	    				
    			}
    			if (eventId.getCodeSystem() == null) {
    				out.write(", EventIdCodeSystem, null");	    				
    			}
    			else {
    		    	out.write(",EventIdCodeSystem, " + eventId.getCodeSystem()+ "\n");	    				
    			}
    			if (eventId.getCodeSystemName() == null) {
    				out.write(", EventIdCodeSystemName, null");
    			}
    			else {
    		    	out.write(",EventIdCodeSystemName, " + eventId.getCodeSystemName()+ "\n");	    				
    			}
    			if (eventId.getDisplayName() == null) {
    				out.write(", EventIdCodeDisplayName, null");
    			}
    			else {
    		    	out.write(",EventIdCodeDisplayName, " + eventId.getDisplayName()+ "\n");	    				
    			}
		    	if (eventIdentification.getEventID().getOriginalText() == null) {
		    		out.write(",TypeCode OriginalText, null\n");
		    	}
		    	else {
			    	out.write(",EventID TypeCode OriginalText, " + eventIdentification.getEventID().getOriginalText()+ "\n");	    			
		    	}
	    	}
	    	
	    	List<CodedValueType> typeCodeList = eventIdentification.getEventTypeCode();
	    	if ((typeCodeList == null) || (typeCodeList.size() == 0)) {
	    		out.write("EventIdentification.EventTypeCode, null\n");
	    	}
	    	else if (typeCodeList.get(0) == null){
	    		out.write("EventIdentification.EventTypeCode, null\n");    		
	    	}
	    	else {
	    		for (CodedValueType eventType : typeCodeList){
	    			if (eventType.getCode() == null){
	    				out.write(", EventTypeCode, null");
	    			}
	    			else {
	    		    	out.write(",EventTypeCode        , " + eventType.getCode() + "\n");	    				
	    			}
	    			if (eventType.getCodeSystem() == null) {
	    				out.write(", EventTypeCodeSystem, null");	    				
	    			}
	    			else {
	    		    	out.write(",EventTypeCodeSystem, " + eventType.getCodeSystem()+ "\n");	    				
	    			}
	    			if (eventType.getCodeSystemName() == null) {
	    				out.write(", EventTypeCodeSystemName, null");
	    			}
	    			else {
	    		    	out.write(",EventTypeCodeSystemName, " + eventType.getCodeSystemName()+ "\n");	    				
	    			}
	    			if (eventType.getDisplayName() == null) {
	    				out.write(", EventTypeCodeDisplayName, null");
	    			}
	    			else {
	    		    	out.write(",EventTypeCodeDisplayName, " + eventType.getDisplayName()+ "\n");	    				
	    			}
	    		}		
	    	}

	    	if (eventIdentification.getEventID().getOriginalText() == null) {
	    		out.write(",TypeCode OriginalText, null\n");
	    	}
	    	else {
		    	out.write(",TypeCode OriginalText, " + eventIdentification.getEventID().getOriginalText()+ "\n");	    			
	    	}
    	
	    	// Participant Object Identification
	    	out.write("******************************************************  Begin Participant Object Identification Section  *******************************************************************\n");
	    	if (participantIdentificationList == null || participantIdentificationList.size() == 0) {
	    		out.write("ParticipantObjectIdentificationList, null\n");
	    	}
	    	else {
		    	recordCount = participantIdentificationList.size();
		    	currentRecord = 0;
		    	out.write("ParticipantObjectIdentitication record count, " + recordCount + "\n");
	    	
	    		while (currentRecord < recordCount) {	
			    	if (participantIdentificationList.get(currentRecord) == null) {
			    		System.out.println("Found null ParticipantObjectIdentificationRecord. recordsOutput = " + currentRecord);
			    		currentRecord++;
			    		continue;
			    	}
		    		ParticipantObjectIdentificationType participantObjectId =  participantIdentificationList.get(currentRecord);
			    	out.write("====,Begin ParticipantObjectIdentification record " + currentRecord + "====\n");
			    	if (participantObjectId == null) {
			    		out.write(",Participant Object Id, null\n");
			    	}
			    	else {
				    	if (participantObjectId.getParticipantObjectID() != null) {
				    		out.write(",Participant Object Id, " + participantObjectId.getParticipantObjectID()+ "\n");
				    	}
				    	if (participantObjectId.getParticipantObjectIDTypeCode() == null) {
				    		out.write("ParticipantObjectIDTypeCode, null\n");
				    	}
				    	else {
					    	out.write(",ObjectId TypeCode Code        , " + participantObjectId.getParticipantObjectIDTypeCode().getCode() + "\n");
					    	out.write(",ObjectId TypeCode CodeSystem, " + participantObjectId.getParticipantObjectIDTypeCode().getCodeSystem()+ "\n");
					    	out.write(",ObjectId TypeCode CodeSystemName, " + participantObjectId.getParticipantObjectIDTypeCode().getCodeSystemName()+ "\n");
					    	out.write(",ObjectId TypeCode CodeDisplayName, " + participantObjectId.getParticipantObjectIDTypeCode().getDisplayName()+ "\n");
					    	if (eventIdentification.getEventID().getOriginalText() == null) {
					    		out.write(",TypeCode OriginalText, null\n");
					    	}
					    	else {
						    	out.write(",TypeCode OriginalText, " + participantObjectId.getParticipantObjectIDTypeCode().getOriginalText()+ "\n");	    			
					    	}
				    	}
				    	
				    	if (participantObjectId.getParticipantObjectName() != null) {
				    		out.write(",Participant Object Name, " +participantObjectId.getParticipantObjectName()+ "\n");
				    	}
				    	else {
				    		out.write(",Participant Object Name, null");
				    	}
				    	
				    	if (participantObjectId.getParticipantObjectTypeCode() != null) {
					    	out.write(",Participant Object Type Code, " + participantObjectId.getParticipantObjectTypeCode()+ "\n");
				    	}
				    	else {
				    		out.write(", Participant Object Type Code, null");
				    	}
				    	
				    	if (participantObjectId.getParticipantObjectTypeCodeRole() != null){
				    		out.write(",Participant Object Type Code Role, " + participantObjectId.getParticipantObjectTypeCodeRole()+ "\n");
				    	}
				    	else {
				    		out.write(", Participant Object Type Code Role, null");		    		
				    	}
				    		
			
				    	if (participantObjectId.getParticipantObjectDataLifeCycle() == null) {
				    		out.write(",ParticipantObjectDataLifeCycle, null\n");
				    	}
				    	else {
				    		out.write(",ParticipantObjectDataLifeCycle =, " + participantObjectId.getParticipantObjectDataLifeCycle()+ "\n");
				    	}
			
				    	if (participantObjectId.getParticipantObjectDetail() == null) {
				    		out.write(",ParticipantObjectDetail, null\n");
				    	}
				    	else {
				    		out.write(",ParticipantObjectDetail-Record Count = ," + participantObjectId.getParticipantObjectDetail().size()+ "\n");
				    	}
			
				    	if (participantObjectId.getParticipantObjectQuery() == null) {
				    		out.write(",ParticipantObjectQuery, null\n");
				    	}
				    	else {
				    		out.write(",ParticipantObjectQuery = ," + participantObjectId.getParticipantObjectQuery().toString() + "\n");
				    	}
			
				    	if (participantObjectId.getParticipantObjectSensitivity() == null) {
				    		out.write(",ParticipantObjectSensitivity, null\n");
				    	}
				    	else {
				    		out.write(",ParticipantObjectSensitivity = ," + participantObjectId.getParticipantObjectSensitivity()+ "\n");
				    	}
			    	}
			    	currentRecord++;
	    		}
	    	}
	    	out.close();
    	} catch (FileNotFoundException e) {
    		e.printStackTrace();
    	} catch (IOException f) {
    		f.printStackTrace();
    	}
    }
    //*/
}
