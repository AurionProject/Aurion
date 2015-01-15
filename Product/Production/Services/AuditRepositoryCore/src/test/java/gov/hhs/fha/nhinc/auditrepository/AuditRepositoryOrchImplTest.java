package gov.hhs.fha.nhinc.auditrepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gov.hhs.fha.nhinc.auditrepository.nhinc.AuditRepositoryOrchImpl;
import gov.hhs.fha.nhinc.common.auditlog.LogEventSecureRequestType;
import gov.hhs.fha.nhinc.common.nhinccommon.AcknowledgementType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.CeType;
import gov.hhs.fha.nhinc.common.nhinccommon.PersonNameType;
import gov.hhs.fha.nhinc.common.nhinccommon.SamlAuthnStatementType;
import gov.hhs.fha.nhinc.common.nhinccommon.UserType;
import gov.hhs.fha.nhinc.hibernate.AdvancedAuditRecord;
import gov.hhs.fha.nhinc.hibernate.AuditRepositoryDAO;
import gov.hhs.fha.nhinc.hibernate.AuditRepositoryRecord;
import gov.hhs.fha.nhinc.transform.audit.AuditDataTransformConstants;
import gov.hhs.fha.nhinc.transform.audit.AuditDataTransformHelper;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import com.services.nhinc.schema.auditmessage.AuditMessageType;
import com.services.nhinc.schema.auditmessage.AuditMessageType.ActiveParticipant;
import com.services.nhinc.schema.auditmessage.AuditSourceIdentificationType;
import com.services.nhinc.schema.auditmessage.CodedValueType;
import com.services.nhinc.schema.auditmessage.EventIdentificationType;
import com.services.nhinc.schema.auditmessage.ParticipantObjectIdentificationType;


public class AuditRepositoryOrchImplTest {

	private static final String patientId = "77777^^^&1.1&ISO";
	private static final String documentId = "9178.68247129";
	private static final String submissionSetId = "324123974.309821";
	private static byte [] nullBytes = null;
	private static final String communityId = "7.6.5.4.3.2.1";
	private static final String ipAddress = "10.242.6.129";
	private static final String dnsName = "health1.mayo.org";
	private static final String communityName = "Mayo Clinic";
	private static String messageId = null;
	private AuditRepositoryOrchImpl orchestrator = null;
	private AuditRepositoryDAO auditDAO = null;
	
	@Before
	public void getAuditRepositoryOrchImpl() {
		orchestrator = new AuditRepositoryOrchImpl();
		messageId = AuditTestHelper.getMessageId();
		 auditDAO = new AuditRepositoryDAO();
	}
	
	/*
	 * Options for ActiveParticipant Records
	 */
	
	public ActiveParticipant createActiveParticipantFromUser(UserType user) {
		AuditMessageType.ActiveParticipant ap = AuditDataTransformHelper.createActiveParticipantForHuman(user);
		return ap;
	}

	public ActiveParticipant createActiveParticipantDestination(AssertionType assertion) {
		AuditMessageType.ActiveParticipant ap = AuditDataTransformHelper.createActiveParticipantDestination(assertion.getSamlAuthnStatement(), false);
		return ap;
	}
	
	public ActiveParticipant createActiveParticipantSource(boolean isSender, String ipAddress) {
		AuditMessageType.ActiveParticipant ap = AuditDataTransformHelper.createActiveParticipantSource(isSender, ipAddress);
		return ap;		
	}
	
	/*
	 * Options for AuditSourceRecords
	 * 
	 * The SPEC says nothing is defined for the Audit Source Record - We only use it for legacy messages that have not been updated to the 2013 
	 * Implementation.
	 */
	
	public AuditSourceIdentificationType createAuditSourceRecord(String communityId, String communityName, CodedValueType auditSourceType)  
	{
		AuditSourceIdentificationType auditSource = AuditDataTransformHelper.createAuditSourceIdentification(communityId, communityName, auditSourceType);
		return auditSource;
		
	}
	/*
	 * Options for EventIdentification Records
	 */
	public EventIdentificationType createEventIdentification(String actionCode, CodedValueType eventId, CodedValueType eventType) {
		Integer eventStatus = new Integer(0);
		EventIdentificationType event = AuditDataTransformHelper.createEventIdentification(actionCode, eventStatus, eventId, eventType);
		return event;
	}
	
	/*
	 * Options for ParticipantObjectIdentification Records
	 */
	public static ParticipantObjectIdentificationType createPatientParticipantObjectIdentification() {
		ParticipantObjectIdentificationType patientParticipant = AuditDataTransformHelper.createParticipantObjectIdentification(
				AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_PERSON, 
				AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_PATIENT, 
				AuditDataTransformHelper.getPatientParticipantRoleIdCodedValue(),
				patientId, 
				nullBytes);
		return patientParticipant;
	}

	public ParticipantObjectIdentificationType createDocumentParticipantObjectIdentification() {
		ParticipantObjectIdentificationType documentParticipant = AuditDataTransformHelper.createParticipantObjectIdentification(
				AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM, 
				AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_REPORT, 
				AuditDataTransformHelper.getDocumentParticipantRoleIdCodedValue(),
				documentId, 
				nullBytes);
		return documentParticipant;
	}
	
	public ParticipantObjectIdentificationType createQueryParametersParticipantObjectIdentification() {
		ParticipantObjectIdentificationType queryParticipant = AuditDataTransformHelper.createParticipantObjectIdentification(
				AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM, 
				AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_QUERY, 
				AuditDataTransformHelper.getQueryParamsParticipantRoleIdCodedValue(),
				documentId, 
				AuditTestHelper.generateRandomByteArray(256));
		return queryParticipant;
	}
	
	public ParticipantObjectIdentificationType createDataTransportParticipantObjectIdentification() {
		ParticipantObjectIdentificationType dataParticipant = AuditDataTransformHelper.createParticipantObjectIdentification(
				AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM, 
				AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_DATA_TRANSPORT, 
				AuditDataTransformHelper.getDataTransportParticipantRoleIdCodedValue(),
				AuditTestHelper.getMessageId(), 
				AuditTestHelper.generateRandomByteArray(1042));
		return dataParticipant;
	}
	
	public ParticipantObjectIdentificationType createCommunityParticipantObjectIdentification() {
		ParticipantObjectIdentificationType communityParticipant = AuditDataTransformHelper.createParticipantObjectIdentification(
				AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM, 
				AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_COMMUNITY, 
				AuditDataTransformHelper.getCommunityParticipantRoleIdCode(),
				communityId, 
				nullBytes);
				communityParticipant.setParticipantObjectName(communityName);
		return communityParticipant;
	}
	
	public ParticipantObjectIdentificationType createDocumentSubmissionParticipantObjectIdentification() {
		ParticipantObjectIdentificationType docSubmissionParticipant = AuditDataTransformHelper.createParticipantObjectIdentification(
				AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM, 
				AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_JOB, 
				AuditDataTransformHelper.getSubmissionSetParticipantRoleIdCodedValue(),
				submissionSetId, 
				nullBytes);
		return docSubmissionParticipant;
	}
	
	public LogEventSecureRequestType assembleMessage(
			String direction,
			String _interface,
			EventIdentificationType event, 
			List<AuditSourceIdentificationType> auditSourceList,
			List<ActiveParticipant> activeList,
			List<ParticipantObjectIdentificationType> participantList)
	{
		LogEventSecureRequestType message = new LogEventSecureRequestType();
		message.setDirection(direction);
		message.setInterface(_interface);
		AuditMessageType auditMsg = new AuditMessageType();
		message.setAuditMessage(auditMsg);
		auditMsg.setEventIdentification(event);

		Iterator<AuditSourceIdentificationType> sourceIt = auditSourceList.iterator();
		while (sourceIt.hasNext()) {
			AuditSourceIdentificationType record = sourceIt.next();
			auditMsg.getAuditSourceIdentification().add(record);
		}
		
		Iterator<ActiveParticipant> activeIt = activeList.iterator();
		while (activeIt.hasNext()) {
			ActiveParticipant record = activeIt.next();
			auditMsg.getActiveParticipant().add(record);
		}

		Iterator<ParticipantObjectIdentificationType> partIt = participantList.iterator();
		while (partIt.hasNext()) {
			ParticipantObjectIdentificationType record = partIt.next();
			auditMsg.getParticipantObjectIdentification().add(record);
		}
		
		return message;
	}
	
	
	public void validateResult(LogEventSecureRequestType messageWrapper, String result, String expectedPurposeOfUse) {
		
		AuditMessageType auditMessage = messageWrapper.getAuditMessage();

		// read back the audit message from the DB
		// the result message contains the recordId
		int eqLoc = result.indexOf('=');
		String recordIdString = result.substring(eqLoc+1);
		recordIdString = recordIdString.trim();
		System.out.println("recordIdString=" + recordIdString);
		Long recordId = null;
		try {
			recordId = Long.valueOf(recordIdString);
		}
		catch(NumberFormatException e){
			System.out.println("Number format exception converting string to Long (index)");
		}
		
		AuditRepositoryRecord baseAuditRecord = auditDAO.findById(recordId);		
		// Test fields of the base audit record
		assertEquals(recordId.longValue(), baseAuditRecord.getId());
		assertNotNull(baseAuditRecord.getTimeStamp());
		BigInteger resultCode = auditMessage.getEventIdentification().getEventOutcomeIndicator();
		assertEquals(resultCode.longValue(), 0L);
		
		ActiveParticipant userRecord = AuditDataTransformHelper.findUserAuditSourceRecord(auditMessage.getActiveParticipant());
		if (userRecord != null) {
			String userId = userRecord.getUserID();
			assertEquals(userId, baseAuditRecord.getUserId());
		}
		
		ParticipantObjectIdentificationType baseParticipantRecord = AuditDataTransformHelper.findBaseParticipantRecord(auditMessage.getParticipantObjectIdentification());
		short typeCode = baseParticipantRecord.getParticipantObjectTypeCode();
		assertEquals(typeCode, baseAuditRecord.getParticipationTypeCode());
		
		short typeCodeRole = baseParticipantRecord.getParticipantObjectTypeCodeRole();
		assertEquals(typeCodeRole, baseAuditRecord.getParticipationTypeCodeRole());
		
		CodedValueType idTypeCode = AuditDataTransformHelper.findBaseParticipantRecord((auditMessage.getParticipantObjectIdentification())).getParticipantObjectIDTypeCode();
		assertEquals(idTypeCode.getCode(), baseAuditRecord.getParticipationIDTypeCode());
		String objectId = baseParticipantRecord.getParticipantObjectID();
		assertEquals(objectId, baseAuditRecord.getReceiverPatientId());
		assertEquals(expectedPurposeOfUse, baseAuditRecord.getPurposeOfUse());
		assertEquals(messageWrapper.getInterface() + " " + messageWrapper.getDirection(), baseAuditRecord.getMessageType());
		
		// Test Fields of the Advanced Audit Record
		assertNotNull(baseAuditRecord.getAdvancedAuditRecord());
		AdvancedAuditRecord advRecord = baseAuditRecord.getAdvancedAuditRecord();

		assertEquals(auditMessage.getEventIdentification().getEventTypeCode().get(0).getDisplayName(), advRecord.getServiceName());
		assertEquals(messageWrapper.getInterface(), advRecord.getSubsystem());
		assertEquals(messageWrapper.getDirection(), advRecord.getMessageDirection());
		ParticipantObjectIdentificationType transportRecord = AuditDataTransformHelper.findParticipantRecordByRoleIdCode(
				auditMessage.getParticipantObjectIdentification(),
				AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_DATA_TRANSPORT);
		try {
			if (transportRecord != null) {
				assertEquals(transportRecord.getParticipantObjectID(), advRecord.getMessageId());
				long messageSize = transportRecord.getParticipantObjectQuery().clone().length;
				long blobSize = baseAuditRecord.getMessage().length();
				assertEquals(messageSize, blobSize);
			}
		}
		catch(Exception e){
			System.out.println("ERROR: Exception " + e.getMessage() + "prevented a test of the proper length of the message record.");
		}

		ParticipantObjectIdentificationType communityRecord = AuditDataTransformHelper.findParticipantRecordByRoleIdCode(
				auditMessage.getParticipantObjectIdentification(),
				AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_COMMUNITY);
		if(communityRecord != null){
			assertEquals(communityRecord.getParticipantObjectID(), baseAuditRecord.getCommunityId());
			assertEquals(communityRecord.getParticipantObjectName(), advRecord.getSourceCommunity());
		}
		else {
			//Do the match with the Audit Source Record
		}
		if (userRecord != null) {
			assertEquals(userRecord.getUserName(),advRecord.getUserName());
			List<CodedValueType> userRoleList = userRecord.getRoleIDCode();
			StringBuffer roles = new StringBuffer();
			for (CodedValueType role : userRoleList) {
				if (role != null && role.getCodeSystemName() != null) {
					roles.append(role.getDisplayName());
					roles.append(" ");
				}
			}
			//remove the last " " 
			roles.setLength(roles.length() -1);
			assertEquals(roles.toString(), advRecord.getUserRoles());			
		}
		ActiveParticipant sourceSystem = AuditDataTransformHelper.findAuditSourceSourceSystemRecord(auditMessage.getActiveParticipant());
		assertEquals(sourceSystem.getNetworkAccessPointID(), advRecord.getSourceSystem());
	}
	
	@Test
	@Ignore
	public void testFullData() {
		CodedValueType eventId = AuditDataTransformHelper.createCodedValue(
				AuditDataTransformConstants.PD_REQUEST_EVENT_ID_CODE, 
				AuditDataTransformConstants.PD_REQUEST_EVENT_ID_CODE_SYSTEM,
				AuditDataTransformConstants.PD_REQUEST_EVENT_ID_CODE_SYSTEM_NAME,
				AuditDataTransformConstants.PD_REQUEST_EVENT_ID_CODE_SYSTEM_DISPLAY_NAME,
				null);
		
		CodedValueType eventType = AuditDataTransformHelper.createCodedValue(
				AuditDataTransformConstants.PD_EVENT_TYPE_CODE,
				AuditDataTransformConstants.PD_EVENT_TYPE_CODE_SYSTEM,
				AuditDataTransformConstants.PD_EVENT_TYPE_CODE_SYSTEM_NAME, 
				AuditDataTransformConstants.PD_EVENT_TYPE_CODE_SYSTEM_DISPLAY_NAME, 
				null);

				
		EventIdentificationType event = createEventIdentification("C", eventId, eventType);
		
		List<AuditSourceIdentificationType> auditSourceList = new ArrayList<AuditSourceIdentificationType>();
		AuditSourceIdentificationType auditSource = createAuditSourceRecord("1.1", "SSP Requesting Gateway", null);
		auditSourceList.add(auditSource);
		
		List<ActiveParticipant> activeParticipantList = new ArrayList<ActiveParticipant>();
		UserType userInfo = new UserType();
		userInfo.setPersonName(new PersonNameType());
		userInfo.getPersonName().setGivenName("Jullian");
		userInfo.getPersonName().setFamilyName("Sasusage");
		userInfo.getPersonName().setFullName("Jullian Sasuage");
		// this is the userId
		userInfo.setUserName("jsasuage-zz-test-aa");
		CeType physician = AuditTestHelper.getPhysicialRoleCoded();
		userInfo.setRoleCoded(physician);
		ActiveParticipant activePart = createActiveParticipantFromUser(userInfo);
		activeParticipantList.add(activePart);
		
		AssertionType assertion = new AssertionType();
		SamlAuthnStatementType samlAuthnStatement = new SamlAuthnStatementType();
		samlAuthnStatement.setSubjectLocalityAddress(ipAddress);
		assertion.setSamlAuthnStatement(samlAuthnStatement);
		CeType purposeOfUse = new CeType();
		purposeOfUse.setCode("TREATMENT"); // Not actual value
		purposeOfUse.setCodeSystem("2.16.840.1.113883.3.18.7.1");
		purposeOfUse.setCodeSystemName("nhin-purpose");
		purposeOfUse.setDisplayName("Patient Treatment");
		assertion.setPurposeOfDisclosureCoded(purposeOfUse);
		activePart = createActiveParticipantDestination(assertion);
		activeParticipantList.add(activePart);
		activePart = createActiveParticipantSource(false, ipAddress);
		activeParticipantList.add(activePart);
		
		List<ParticipantObjectIdentificationType> participantList = new ArrayList<ParticipantObjectIdentificationType>();
		ParticipantObjectIdentificationType part = createPatientParticipantObjectIdentification();
		participantList.add(part);
		part = createDocumentParticipantObjectIdentification();
		participantList.add(part);
		part = createDataTransportParticipantObjectIdentification();
		participantList.add(part);
		part = createCommunityParticipantObjectIdentification();
		participantList.add(part);		
		
		LogEventSecureRequestType auditMessageWrapper = assembleMessage("outbound", "nhin", event, auditSourceList, activeParticipantList, participantList);
		AcknowledgementType result = orchestrator.logAudit(auditMessageWrapper, assertion);
		assertTrue (result.getMessage().contains("="));
		validateResult(auditMessageWrapper, result.getMessage(), "TREATMENT");
	}
	/*
	 * Test to see if the audit message itself is stored in the baseAUdit table in place of
	 * the message being audited when such is not available in the 
	 */
	@Test
	@Ignore
	public void testNoDataTransportRecord() {
		CodedValueType eventId = AuditDataTransformHelper.createCodedValue(
				AuditDataTransformConstants.PD_REQUEST_EVENT_ID_CODE, 
				AuditDataTransformConstants.PD_REQUEST_EVENT_ID_CODE_SYSTEM,
				AuditDataTransformConstants.PD_REQUEST_EVENT_ID_CODE_SYSTEM_NAME,
				AuditDataTransformConstants.PD_REQUEST_EVENT_ID_CODE_SYSTEM_DISPLAY_NAME,
				null);
		
		CodedValueType eventType = AuditDataTransformHelper.createCodedValue(
				AuditDataTransformConstants.PD_EVENT_TYPE_CODE,
				AuditDataTransformConstants.PD_EVENT_TYPE_CODE_SYSTEM,
				AuditDataTransformConstants.PD_EVENT_TYPE_CODE_SYSTEM_NAME, 
				AuditDataTransformConstants.PD_EVENT_TYPE_CODE_SYSTEM_DISPLAY_NAME, 
				null);

				
		EventIdentificationType event = createEventIdentification("C", eventId, eventType);
		
		List<AuditSourceIdentificationType> auditSourceList = new ArrayList<AuditSourceIdentificationType>();
		AuditSourceIdentificationType auditSource = createAuditSourceRecord("99.99", "SSP Requesting Gateway", null);
		auditSourceList.add(auditSource);
		
		List<ActiveParticipant> activeParticipantList = new ArrayList<ActiveParticipant>();
		UserType userInfo = new UserType();
		userInfo.setPersonName(new PersonNameType());
		userInfo.getPersonName().setGivenName("Jullian");
		userInfo.getPersonName().setFamilyName("Sasusage");
		userInfo.getPersonName().setFullName("Jullian Sasuage");
		// this is the userId
		userInfo.setUserName("jsasuage-zz-test-aa");
		CeType physician = AuditTestHelper.getPhysicialRoleCoded();
		userInfo.setRoleCoded(physician);
		ActiveParticipant activePart = createActiveParticipantFromUser(userInfo);
		activeParticipantList.add(activePart);
		
		AssertionType assertion = new AssertionType();
		SamlAuthnStatementType samlAuthnStatement = new SamlAuthnStatementType();
		samlAuthnStatement.setSubjectLocalityAddress(ipAddress);
		assertion.setSamlAuthnStatement(samlAuthnStatement);
		CeType purposeOfUse = new CeType();
		purposeOfUse.setCode("OPERATIONS"); // Not actual value
		purposeOfUse.setCodeSystem("2.16.840.1.113883.3.18.7.1");
		purposeOfUse.setCodeSystemName("nhin-purpose");
		purposeOfUse.setDisplayName("Healthcare Operations");
		assertion.setPurposeOfDisclosureCoded(purposeOfUse);
		activePart = createActiveParticipantDestination(assertion);
		activeParticipantList.add(activePart);
		activePart = createActiveParticipantSource(false, ipAddress);
		activeParticipantList.add(activePart);
		
		List<ParticipantObjectIdentificationType> participantList = new ArrayList<ParticipantObjectIdentificationType>();
		ParticipantObjectIdentificationType part = createPatientParticipantObjectIdentification();
		participantList.add(part);
		part = createCommunityParticipantObjectIdentification();
		participantList.add(part);		
		
		LogEventSecureRequestType auditMessageWrapper = assembleMessage("outbound", "nhin", event, auditSourceList, activeParticipantList, participantList);
		AcknowledgementType result = orchestrator.logAudit(auditMessageWrapper, assertion);
		assertTrue (result.getMessage().contains("="));
		validateResult(auditMessageWrapper, result.getMessage(), "OPERATIONS");
	}
	/*
	 * Test to see if the the community information to comes through the AuditSource record
	 * in legacy implementations still gets used in the base audit record
	 */
	@Test
	@Ignore
	public void testNoCommunityRecord() {
		CodedValueType eventId = AuditDataTransformHelper.createCodedValue(
				AuditDataTransformConstants.PD_REQUEST_EVENT_ID_CODE, 
				AuditDataTransformConstants.PD_REQUEST_EVENT_ID_CODE_SYSTEM,
				AuditDataTransformConstants.PD_REQUEST_EVENT_ID_CODE_SYSTEM_NAME,
				AuditDataTransformConstants.PD_REQUEST_EVENT_ID_CODE_SYSTEM_DISPLAY_NAME,
				null);
		
		CodedValueType eventType = AuditDataTransformHelper.createCodedValue(
				AuditDataTransformConstants.PD_EVENT_TYPE_CODE,
				AuditDataTransformConstants.PD_EVENT_TYPE_CODE_SYSTEM,
				AuditDataTransformConstants.PD_EVENT_TYPE_CODE_SYSTEM_NAME, 
				AuditDataTransformConstants.PD_EVENT_TYPE_CODE_SYSTEM_DISPLAY_NAME, 
				null);

				
		EventIdentificationType event = createEventIdentification("C", eventId, eventType);
		
		List<AuditSourceIdentificationType> auditSourceList = new ArrayList<AuditSourceIdentificationType>();
		AuditSourceIdentificationType auditSource = createAuditSourceRecord("1.1", "SSP Requesting Gateway", null);
		auditSourceList.add(auditSource);
		
		List<ActiveParticipant> activeParticipantList = new ArrayList<ActiveParticipant>();
		UserType userInfo = new UserType();
		userInfo.setPersonName(new PersonNameType());
		userInfo.getPersonName().setGivenName("Jullian");
		userInfo.getPersonName().setFamilyName("Sasuage");
		userInfo.getPersonName().setFullName("Jullian Sasuage");
		// this is the userId
		userInfo.setUserName("jsasuage-zz-test-aa");
		CeType physician = AuditTestHelper.getPhysicialRoleCoded();
		userInfo.setRoleCoded(physician);
		ActiveParticipant activePart = createActiveParticipantFromUser(userInfo);
		activeParticipantList.add(activePart);
		
		AssertionType assertion = new AssertionType();
		SamlAuthnStatementType samlAuthnStatement = new SamlAuthnStatementType();
		samlAuthnStatement.setSubjectLocalityAddress(ipAddress);
		assertion.setSamlAuthnStatement(samlAuthnStatement);
		CeType purposeOfUse = new CeType();
		purposeOfUse.setCode("TREATMENT"); // Not actual value
		purposeOfUse.setCodeSystem("2.16.840.1.113883.3.18.7.1");
		purposeOfUse.setCodeSystemName("nhin-purpose");
		purposeOfUse.setDisplayName("Patient Treatment");
		assertion.setPurposeOfDisclosureCoded(purposeOfUse);
		activePart = createActiveParticipantDestination(assertion);
		activeParticipantList.add(activePart);
		activePart = createActiveParticipantSource(false, ipAddress);
		activeParticipantList.add(activePart);
		
		List<ParticipantObjectIdentificationType> participantList = new ArrayList<ParticipantObjectIdentificationType>();
		ParticipantObjectIdentificationType part = createPatientParticipantObjectIdentification();
		participantList.add(part);
		part = createDocumentParticipantObjectIdentification();
		participantList.add(part);
		part = createDataTransportParticipantObjectIdentification();
		participantList.add(part);
		
		LogEventSecureRequestType auditMessageWrapper = assembleMessage("outbound", "nhin", event, auditSourceList, activeParticipantList, participantList);
		AcknowledgementType result = orchestrator.logAudit(auditMessageWrapper, assertion);
		assertTrue (result.getMessage().contains("="));
		validateResult(auditMessageWrapper, result.getMessage(), "TREATMENT");
	}
}
