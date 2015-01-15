package gov.hhs.fha.nhinc.auditrepository;

import gov.hhs.fha.nhinc.common.nhinccommon.CeType;
import gov.hhs.fha.nhinc.hibernate.AdvancedAuditRecord;
import gov.hhs.fha.nhinc.hibernate.AuditRepositoryRecord;
import gov.hhs.fha.nhinc.transform.audit.AuditDataTransformConstants;
import gov.hhs.fha.nhinc.transform.audit.AuditDataTransformHelper;
import gov.hhs.fha.nhinc.util.Base64Coder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.hibernate.Hibernate;

import com.services.nhinc.schema.auditmessage.EventIdentificationType;

public class AuditTestHelper {
    public static List<AuditRepositoryRecord> populateDummyRecords() {
    	List<AuditRepositoryRecord> recordList = new ArrayList<AuditRepositoryRecord>();
    	AuditRepositoryRecord record1 = new AuditRepositoryRecord();
    	AuditRepositoryRecord record2 = new AuditRepositoryRecord();
    	AuditRepositoryRecord record3 = new AuditRepositoryRecord();
    	
    	record1.setCommunityId("1.1");
    	record1.setEventId(24);
    	record1.setMessageType("entity outbound");
    	record1.setParticipationIDTypeCode(AuditDataTransformHelper.formatCodedValue(AuditDataTransformHelper.getPatientParticipantRoleIdCodedValue()));
    	record1.setParticipationTypeCode(AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_PERSON);
    	record1.setParticipationTypeCodeRole(AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_PATIENT);
    	record1.setPurposeOfUse("TREATMENT");
    	record1.setReceiverPatientId("77777^^^&1.1&ISO");
    	record1.setSenderPatientId(null);
    	record1.setUserId("kscatterberg-zz-test-aa");
    	// Simulate a message
    	record1.setMessage(Hibernate.createBlob(generateRandomByteArray(412)));
    	recordList.add(record1);

    	record2.setCommunityId("2.2");
    	record2.setEventId(132);
    	record2.setMessageType("nhin outbound");
    	record2.setParticipationIDTypeCode(AuditDataTransformHelper.formatCodedValue(AuditDataTransformHelper.getDocumentParticipantRoleIdCodedValue()));
    	record2.setParticipationTypeCode(AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM);
    	record2.setParticipationTypeCodeRole(AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_REPORT);
    	record2.setPurposeOfUse("PUBLIC HEALTH");
    	record2.setReceiverPatientId("77777^^^&1.1&ISO");
    	record2.setSenderPatientId(null);
    	record2.setUserId("ssampion-zz-test-aa");
    	// Simulate a message
    	record2.setMessage(Hibernate.createBlob(generateRandomByteArray(978)));
    	recordList.add(record2);
    	    	
    	record3.setCommunityId("1.2.3.4.5.6.7");
    	record3.setEventId(19);
    	record3.setMessageType("adapter inbound");
    	record3.setParticipationIDTypeCode(AuditDataTransformHelper.formatCodedValue(AuditDataTransformHelper.getDataTransportParticipantRoleIdCodedValue()));
    	record3.setParticipationTypeCode(AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM);
    	record3.setParticipationTypeCodeRole(AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_DATA_TRANSPORT);
    	record3.setPurposeOfUse("PAYMENT");
    	record3.setReceiverPatientId("77777^^^&1.1&ISO");
    	record3.setSenderPatientId(null);
    	record3.setUserId("tgrandolff-zz-test-aa");
    	// Simulate a message
    	record3.setMessage(Hibernate.createBlob(generateRandomByteArray(1412)));
    	recordList.add(record3);
    	
    	return recordList;
    }
    
    public static AuditRepositoryRecord populateDummyRecord() {
    	AuditRepositoryRecord record1 = new AuditRepositoryRecord();
    	
    	record1.setCommunityId("1.1");
    	record1.setEventId(24);
    	record1.setMessageType("entity outbound");
    	record1.setParticipationIDTypeCode(AuditDataTransformHelper.formatCodedValue(AuditDataTransformHelper.getPatientParticipantRoleIdCodedValue()));
    	record1.setParticipationTypeCode(AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_PERSON);
    	record1.setParticipationTypeCodeRole(AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_PATIENT);
    	record1.setPurposeOfUse("TREATMENT");
    	record1.setReceiverPatientId("77777^^^&1.1&ISO");
    	record1.setSenderPatientId(null);
    	record1.setUserId("kscatterberg-zz-test-aa");
    	// Simulate a message
    	record1.setMessage(Hibernate.createBlob(generateRandomByteArray(312)));
    	return record1;
    }
    
    public static AuditRepositoryRecord populateDummyBaseRecord() {
    	AuditRepositoryRecord record1 = new AuditRepositoryRecord();
    	
    	record1.setCommunityId("1.1");
    	record1.setEventId(24);
    	record1.setMessageType("entity outbound");
    	record1.setParticipationIDTypeCode(AuditDataTransformHelper.formatCodedValue(AuditDataTransformHelper.getPatientParticipantRoleIdCodedValue()));
    	record1.setParticipationTypeCode(AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_PERSON);
    	record1.setParticipationTypeCodeRole(AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_PATIENT);
    	record1.setPurposeOfUse("TREATMENT");
    	record1.setReceiverPatientId("77777^^^&1.1&ISO");
    	record1.setSenderPatientId(null);
    	record1.setUserId("sullivanes-test-aa");
    	// Simulate a message
    	record1.setMessage(Hibernate.createBlob(generateRandomByteArray(312)));
    	return record1;
    }
    public static AdvancedAuditRecord populateDummyAdvancedRecord() {
    	AdvancedAuditRecord record1 = new AdvancedAuditRecord();
    	
    	record1.setMessageDirection("outbound");
    	record1.setMessageId(AuditTestHelper.getMessageId());
    	record1.setServiceName("Document Query");
    	record1.setSourceCommunity("Intermountain Medical Center");
    	record1.setSourceSystem("209.31.242.12");
    	record1.setSubsystem("entity");
    	record1.setUserName("Edward S. Sullivan");
    	record1.setUserRoles("NURSE PRACTIONER");
    	// Simulate queryParams
    	char [] encodedQuery = Base64Coder.encode(generateRandomByteArray(98));
    	String  sEncodedQuery = new String(encodedQuery);
    	record1.setQueryParams(Hibernate.createClob(sEncodedQuery));
    	// Simulate a message
    	char [] encodedMessage = Base64Coder.encode((generateRandomByteArray(292)));
    	String sEncodedMessage = new String(encodedMessage);
    	record1.setMessageAudited(Hibernate.createClob((sEncodedMessage)));
    	return record1;
    }

    // The following two method fake a string of bytes representing a message.
    public static byte[] generateRandomByteArray(int size) {
    	return getRandomByteArray(size);
    }
    
    private static byte[] getRandomByteArray(int size){
	    byte[] result= new byte[size];
	    Random random= new Random();
	    random.nextBytes(result);
	    return result;
	}
    
    // Create Message Components
    public static EventIdentificationType createEventIdentification() {
    	EventIdentificationType eventId = new EventIdentificationType();
    	return eventId;
    	
    }
    
    public static String getMessageId() { 
    	String messageId = UUID.randomUUID().toString();
    	return messageId;
    }
    
    public static CeType getPhysicialRoleCoded() {
    	CeType role = new CeType();
    	role.setCode("19842"); // Actual number unknown
    	role.setCodeSystem("2.16.840.1.113883.6.96");
    	role.setCodeSystemName("SNOmed CT");
    	role.setDisplayName("Physician");
    	return role;
    }
    
    public static CeType getNurseRoleCoded() {
    	CeType role = new CeType();
    	role.setCode("20901873"); // Actual number unknown
    	role.setCodeSystem("2.16.840.1.113883.6.96");
    	role.setCodeSystemName("SNOmed CT");
    	role.setDisplayName("Registered Nurse");
    	return role;    	
    }
    
}
