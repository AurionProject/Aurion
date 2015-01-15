package gov.hhs.fha.nhinc.compliance;

import org.hl7.v3.CD;
import org.hl7.v3.CE;
import org.hl7.v3.COCTMT090003UV01AssignedEntity;
import org.hl7.v3.CS;
import org.hl7.v3.II;
import org.hl7.v3.MFMIMT700711UV01Custodian;
import org.hl7.v3.PRPAIN201306UV02;
import org.hl7.v3.PRPAIN201306UV02MFMIMT700711UV01ControlActProcess;
import org.hl7.v3.PRPAIN201306UV02MFMIMT700711UV01RegistrationEvent;
import org.hl7.v3.PRPAIN201306UV02MFMIMT700711UV01Subject1;
import org.hl7.v3.PRPAIN201306UV02MFMIMT700711UV01Subject2;
import org.hl7.v3.PRPAMT201310UV02Patient;
import org.hl7.v3.PRPAMT201310UV02QueryMatchObservation;
import org.hl7.v3.PRPAMT201310UV02Subject;
import org.hl7.v3.ParticipationTargetSubject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the PatientDiscoveryResponseComplianceChecker class.
 * 
 * @author Neil Webb
 */
public class PatientDiscoveryResponseComplianceCheckerTest {

	private static final String PATIENT_STATUS_CODE_INCORRECT = "SD";
	private static final String PATIENT_SUBJECT_OF_QUERY_MATCH_OBS_CLASS_CODE_INCORRECT = "CLS";
	private static final String CUSTODIAN_ASSIGNED_ENTITY_CODE_CODE_INCORRECT = "SOME_CODE";
	private static final String CUSTODIAN_ASSIGNED_ENTITY_CODE_CODE_SYSTEM_INCORRECT = "1.2.3";
	public static final String CONTROL_ACT_PROCESS_CODE_CODE_INCORRECT = "PRPA_TE201306UV";
	public static final String CONTROL_ACT_PROCESS_CODE_CODE_SYSTEM_INCORRECT = "9.5.2";

	private PRPAIN201306UV02 response = null;
	private PatientDiscoveryResponseComplianceChecker testSubject = null;
	
	@Before
	public void setUp() {
		response = buildResponseMessage();
		testSubject = new PatientDiscoveryResponseComplianceChecker(response) {
			protected boolean isComplianceCheckEnabled() {
				return true;
			}
		};
		verifyBaseObjectsNotNull(response);
	}
	
	// ########## COMPLIANCE CHECK DISABLED SWITCH ##################
	
	
	// ########## PATIENT STATUS CODE ##################

	@Test
	public void testPatientStatusCodeMissing() {
		
		assertNull("Patient status was not null before compliance check", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getStatusCode().getCode());
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Patient status was null after compliance check", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getStatusCode().getCode());
		assertEquals("Patient status was incorrect after compliance check", PatientDiscoveryResponseComplianceChecker.PATIENT_STATUS_CODE, response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getStatusCode().getCode());
	}
	
	@Test
	public void testPatientStatusCodeEmptyString() {
		
		response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getStatusCode().setCode("");
		assertEquals("Patient status was not an empty string value before compliance check", "", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getStatusCode().getCode());
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Patient status was null after compliance check", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getStatusCode().getCode());
		assertEquals("Patient status was incorrect after compliance check", PatientDiscoveryResponseComplianceChecker.PATIENT_STATUS_CODE, response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getStatusCode().getCode());
	}
	
	@Test
	public void testPatientStatusCodeIncorrect() {
		
		response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getStatusCode().setCode(PATIENT_STATUS_CODE_INCORRECT);
		assertEquals("Patient status was not the incorrect value before compliance check", PATIENT_STATUS_CODE_INCORRECT, response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getStatusCode().getCode());
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Patient status was null after compliance check", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getStatusCode().getCode());
		assertEquals("Patient status was incorrect after compliance check", PatientDiscoveryResponseComplianceChecker.PATIENT_STATUS_CODE, response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getStatusCode().getCode());
	}
	
	// ########## PATIENT SUBJECT OF TYPE CODE AND QUERY MATCH OBSERVATION CLASS CODE ##################

	@Test
	public void testPatientSubjectOfCodesMissing() {
		
		// Patient subject of type code defaults to "SBJ" in getter - no missing test performed
		assertTrue("Patient subject of query match observation class code list was not empty before compliance check", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0).getQueryMatchObservation().getClassCode().isEmpty());
		
		testSubject.update2011SpecCompliance();
		assertFalse("Patient subject of query match observation class code list was empty after compliance check", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0).getQueryMatchObservation().getClassCode().isEmpty());
		assertEquals("Patient subject of query match observation class code was incorrect after compliance check", PatientDiscoveryResponseComplianceChecker.PATIENT_SUBJECT_OF_QUERY_MATCH_OBS_CLASS_CODE, response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0).getQueryMatchObservation().getClassCode().get(0));
	}
	
	@Test
	public void testPatientSubjectOfCodesEmptyString() {
		
		// Patient subject of type code is an enumeration - no empty test performed
		response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0).getQueryMatchObservation().getClassCode().add("");
		assertEquals("Patient subject of query match observation class code was not an empty string value before compliance check", "", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0).getQueryMatchObservation().getClassCode().get(0));
		
		testSubject.update2011SpecCompliance();
		assertFalse("Patient subject of query match observation class code list was empty after compliance check", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0).getQueryMatchObservation().getClassCode().isEmpty());
		assertEquals("Patient subject of query match observation class code was incorrect after compliance check", PatientDiscoveryResponseComplianceChecker.PATIENT_SUBJECT_OF_QUERY_MATCH_OBS_CLASS_CODE, response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0).getQueryMatchObservation().getClassCode().get(0));
	}
	
	@Test
	public void testPatientSubjectOfCodesIncorrect() {
		
		response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0).setTypeCode(ParticipationTargetSubject.SPC);
		response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0).getQueryMatchObservation().getClassCode().add(PATIENT_SUBJECT_OF_QUERY_MATCH_OBS_CLASS_CODE_INCORRECT);
		assertEquals("Patient subject of type code value not incorrect before compliance check", ParticipationTargetSubject.SPC.value(), response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0).getTypeCode().value());
		assertEquals("Patient subject of query match observation class code was not incorrect value before compliance check", PATIENT_SUBJECT_OF_QUERY_MATCH_OBS_CLASS_CODE_INCORRECT, response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0).getQueryMatchObservation().getClassCode().get(0));
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Patient subject of type code was null after compliance check", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0).getTypeCode());
		assertEquals("Patient subject of type code value incorrect after compliance check", ParticipationTargetSubject.SBJ.value(), response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0).getTypeCode().value());
		assertFalse("Patient subject of query match observation class code list was empty after compliance check", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0).getQueryMatchObservation().getClassCode().isEmpty());
		assertEquals("Patient subject of query match observation class code was incorrect after compliance check", PatientDiscoveryResponseComplianceChecker.PATIENT_SUBJECT_OF_QUERY_MATCH_OBS_CLASS_CODE, response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0).getQueryMatchObservation().getClassCode().get(0));
	}
	
	// ########## CUSTODIAN ASSIGNED ENTITY CODE ##################

	@Test
	public void testCustodianAssignedEntityCodeMissing() {
		
		assertNull("Custodian assigned entity code was not null before compliance check", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian().getAssignedEntity().getCode());
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Custodian assigned entity code was null after compliance check", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian().getAssignedEntity().getCode());
		assertEquals("Custodian assigned entity code-code was incorrect after compliance check", PatientDiscoveryResponseComplianceChecker.CUSTODIAN_ASSIGNED_ENTITY_CODE_CODE, response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian().getAssignedEntity().getCode().getCode());
		assertEquals("Custodian assigned entity code-code system was incorrect after compliance check", PatientDiscoveryResponseComplianceChecker.CUSTODIAN_ASSIGNED_ENTITY_CODE_CODE_SYSTEM, response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian().getAssignedEntity().getCode().getCodeSystem());
	}
	
	@Test
	public void testCustodianAssignedEntityCodeEmptyStrings() {
		
		response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian().getAssignedEntity().setCode(new CE());
		response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian().getAssignedEntity().getCode().setCode("");
		response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian().getAssignedEntity().getCode().setCodeSystem("");
		assertNotNull("Custodian assigned entity code was null before compliance check", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian().getAssignedEntity().getCode());
		assertEquals("Custodian assigned entity code-code was not an empty string before compliance check", "", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian().getAssignedEntity().getCode().getCode());
		assertEquals("Custodian assigned entity code-code system was not an empty string before compliance check", "", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian().getAssignedEntity().getCode().getCodeSystem());
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Custodian assigned entity code was null after compliance check", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian().getAssignedEntity().getCode());
		assertEquals("Custodian assigned entity code-code was incorrect after compliance check", PatientDiscoveryResponseComplianceChecker.CUSTODIAN_ASSIGNED_ENTITY_CODE_CODE, response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian().getAssignedEntity().getCode().getCode());
		assertEquals("Custodian assigned entity code-code system was incorrect after compliance check", PatientDiscoveryResponseComplianceChecker.CUSTODIAN_ASSIGNED_ENTITY_CODE_CODE_SYSTEM, response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian().getAssignedEntity().getCode().getCodeSystem());
	}
	
	@Test
	public void testCustodianAssignedEntityCodeIncorrect() {
		
		response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian().getAssignedEntity().setCode(new CE());
		response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian().getAssignedEntity().getCode().setCode(CUSTODIAN_ASSIGNED_ENTITY_CODE_CODE_INCORRECT);
		response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian().getAssignedEntity().getCode().setCodeSystem(CUSTODIAN_ASSIGNED_ENTITY_CODE_CODE_SYSTEM_INCORRECT);
		assertNotNull("Custodian assigned entity code was null before compliance check", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian().getAssignedEntity().getCode());
		assertEquals("Custodian assigned entity code-code was not incorrect before compliance check", CUSTODIAN_ASSIGNED_ENTITY_CODE_CODE_INCORRECT, response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian().getAssignedEntity().getCode().getCode());
		assertEquals("Custodian assigned entity code-code system was not incorrect before compliance check", CUSTODIAN_ASSIGNED_ENTITY_CODE_CODE_SYSTEM_INCORRECT, response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian().getAssignedEntity().getCode().getCodeSystem());
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Custodian assigned entity code was null after compliance check", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian().getAssignedEntity().getCode());
		assertEquals("Custodian assigned entity code-code was incorrect after compliance check", PatientDiscoveryResponseComplianceChecker.CUSTODIAN_ASSIGNED_ENTITY_CODE_CODE, response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian().getAssignedEntity().getCode().getCode());
		assertEquals("Custodian assigned entity code-code system was incorrect after compliance check", PatientDiscoveryResponseComplianceChecker.CUSTODIAN_ASSIGNED_ENTITY_CODE_CODE_SYSTEM, response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian().getAssignedEntity().getCode().getCodeSystem());
	}
	
	// ########## CONTROL ACT PROCESS CODE ##################

	@Test
	public void testControlActProcessCodeMissing() {
		
		assertNull("Control act process code was not null before compliance check", response.getControlActProcess().getCode());
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Control act process code was null after compliance check", response.getControlActProcess().getCode());
		assertEquals("Control act process code-code was incorrect after compliance check", PatientDiscoveryResponseComplianceChecker.CONTROL_ACT_PROCESS_CODE_CODE, response.getControlActProcess().getCode().getCode());
		assertEquals("Control act process code-code system was incorrect after compliance check", PatientDiscoveryResponseComplianceChecker.CONTROL_ACT_PROCESS_CODE_CODE_SYSTEM, response.getControlActProcess().getCode().getCodeSystem());
	}
	
	@Test
	public void testControlActProcessCodeEmptyStrings() {
		
		response.getControlActProcess().setCode(new CD());
		response.getControlActProcess().getCode().setCode("");
		response.getControlActProcess().getCode().setCodeSystem("");
		assertNotNull("Custodian assigned entity code was null before compliance check", response.getControlActProcess().getCode());
		assertEquals("Custodian assigned entity code-code was not an empty string before compliance check", "", response.getControlActProcess().getCode().getCode());
		assertEquals("Custodian assigned entity code-code system was not an empty string before compliance check", "", response.getControlActProcess().getCode().getCodeSystem());
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Control act process code was null after compliance check", response.getControlActProcess().getCode());
		assertEquals("Control act process code-code was incorrect after compliance check", PatientDiscoveryResponseComplianceChecker.CONTROL_ACT_PROCESS_CODE_CODE, response.getControlActProcess().getCode().getCode());
		assertEquals("Control act process code-code system was incorrect after compliance check", PatientDiscoveryResponseComplianceChecker.CONTROL_ACT_PROCESS_CODE_CODE_SYSTEM, response.getControlActProcess().getCode().getCodeSystem());
	}
	
	@Test
	public void testControlActProcessCodeIncorrect() {
		
		response.getControlActProcess().setCode(new CD());
		response.getControlActProcess().getCode().setCode(CONTROL_ACT_PROCESS_CODE_CODE_INCORRECT);
		response.getControlActProcess().getCode().setCodeSystem(CONTROL_ACT_PROCESS_CODE_CODE_SYSTEM_INCORRECT);
		assertNotNull("Control act process code was null before compliance check", response.getControlActProcess().getCode());
		assertEquals("Control act process code-code was not incorrect before compliance check", CONTROL_ACT_PROCESS_CODE_CODE_INCORRECT, response.getControlActProcess().getCode().getCode());
		assertEquals("Control act process code-code system was not incorrect before compliance check", CONTROL_ACT_PROCESS_CODE_CODE_SYSTEM_INCORRECT, response.getControlActProcess().getCode().getCodeSystem());
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Control act process code was null after compliance check", response.getControlActProcess().getCode());
		assertEquals("Control act process code-code was incorrect after compliance check", PatientDiscoveryResponseComplianceChecker.CONTROL_ACT_PROCESS_CODE_CODE, response.getControlActProcess().getCode().getCode());
		assertEquals("Control act process code-code system was incorrect after compliance check", PatientDiscoveryResponseComplianceChecker.CONTROL_ACT_PROCESS_CODE_CODE_SYSTEM, response.getControlActProcess().getCode().getCodeSystem());
	}
	
	// ########## TEST UTILITIES ##################

	private void verifyBaseObjectsNotNull(PRPAIN201306UV02 pdResponse) {
		assertNotNull("Base: PD response was null", response);
		assertNotNull("Base: ControlActProcess was null", response.getControlActProcess());
		assertFalse("Base: Subject list was empty", response.getControlActProcess().getSubject().isEmpty());
		assertNotNull("Base: Subject object was null", response.getControlActProcess().getSubject().get(0));
		assertNotNull("Base: Registration event was null", response.getControlActProcess().getSubject().get(0).getRegistrationEvent());
		assertNotNull("Base: Subject1 was null", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1());
		assertNotNull("Base: Patient was null", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient());
		assertNotNull("Base: Patient status code object was null", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getStatusCode());
		assertFalse("Base: Patient subject of was null", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().isEmpty());
		assertNotNull("Base: Patient subject of was null", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0));
		assertNotNull("Base: Patient subject of - query match observation was null", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getSubjectOf1().get(0).getQueryMatchObservation());
		assertNotNull("Base: Custodian was null", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian());
		assertNotNull("Base: Custodian assigned entity was null", response.getControlActProcess().getSubject().get(0).getRegistrationEvent().getCustodian().getAssignedEntity());
	}
	
	private PRPAIN201306UV02 buildResponseMessage() {
		PRPAIN201306UV02 msg = new PRPAIN201306UV02();
		msg.setControlActProcess(createControlActProcess());
		return msg;
	}

	private PRPAIN201306UV02MFMIMT700711UV01ControlActProcess createControlActProcess() {
		PRPAIN201306UV02MFMIMT700711UV01ControlActProcess controlActProcess = new PRPAIN201306UV02MFMIMT700711UV01ControlActProcess();
		PRPAIN201306UV02MFMIMT700711UV01Subject1 subject = new PRPAIN201306UV02MFMIMT700711UV01Subject1();
		PRPAIN201306UV02MFMIMT700711UV01RegistrationEvent registrationEvent = new PRPAIN201306UV02MFMIMT700711UV01RegistrationEvent();
		PRPAIN201306UV02MFMIMT700711UV01Subject2 subject1 = new PRPAIN201306UV02MFMIMT700711UV01Subject2();
		subject1.setPatient(createPatient());
		registrationEvent.setSubject1(subject1 );
		registrationEvent.setCustodian(createCustodian());
		subject.setRegistrationEvent(registrationEvent );
		controlActProcess.getSubject().add(subject );
		return controlActProcess;
	}

	private PRPAMT201310UV02Patient createPatient() {
		PRPAMT201310UV02Patient patient = new PRPAMT201310UV02Patient();
		CS statusCode = new CS();
		// Intentionally did not set the code value for testing
		patient.setStatusCode(statusCode );
		PRPAMT201310UV02Subject subjectof = new PRPAMT201310UV02Subject();
		subjectof.setTypeCode(null);
		PRPAMT201310UV02QueryMatchObservation queryMatchObservation = new PRPAMT201310UV02QueryMatchObservation();
		subjectof.setQueryMatchObservation(queryMatchObservation );
		patient.getSubjectOf1().add(subjectof );
		return patient;
	}
	
	private MFMIMT700711UV01Custodian createCustodian() {
		MFMIMT700711UV01Custodian custodian = new MFMIMT700711UV01Custodian();
		COCTMT090003UV01AssignedEntity assignedEntity = new COCTMT090003UV01AssignedEntity();
		II id = new II();
		id.setRoot("1.5.3.4");
		assignedEntity.getId().add(id );
		custodian.setAssignedEntity(assignedEntity );
		return custodian;
	}

}
