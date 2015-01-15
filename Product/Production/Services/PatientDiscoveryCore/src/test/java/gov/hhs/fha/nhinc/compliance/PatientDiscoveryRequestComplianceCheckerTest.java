package gov.hhs.fha.nhinc.compliance;

import javax.xml.bind.JAXBElement;

import org.hl7.v3.ADExplicit;
import org.hl7.v3.AdxpExplicitCity;
import org.hl7.v3.AdxpExplicitCountry;
import org.hl7.v3.AdxpExplicitPostalCode;
import org.hl7.v3.AdxpExplicitState;
import org.hl7.v3.AdxpExplicitStreetAddressLine;
import org.hl7.v3.CE;
import org.hl7.v3.COCTMT090300UV01AssignedDevice;
import org.hl7.v3.ENExplicit;
import org.hl7.v3.EnExplicitFamily;
import org.hl7.v3.EnExplicitGiven;
import org.hl7.v3.EnExplicitPrefix;
import org.hl7.v3.EnExplicitSuffix;
import org.hl7.v3.II;
import org.hl7.v3.IVLTSExplicit;
import org.hl7.v3.ObjectFactory;
import org.hl7.v3.PNExplicit;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201305UV02QUQIMT021001UV01ControlActProcess;
import org.hl7.v3.PRPAMT201306UV02LivingSubjectAdministrativeGender;
import org.hl7.v3.PRPAMT201306UV02LivingSubjectBirthPlaceAddress;
import org.hl7.v3.PRPAMT201306UV02LivingSubjectBirthPlaceName;
import org.hl7.v3.PRPAMT201306UV02LivingSubjectBirthTime;
import org.hl7.v3.PRPAMT201306UV02LivingSubjectId;
import org.hl7.v3.PRPAMT201306UV02LivingSubjectName;
import org.hl7.v3.PRPAMT201306UV02MothersMaidenName;
import org.hl7.v3.PRPAMT201306UV02ParameterList;
import org.hl7.v3.PRPAMT201306UV02PatientAddress;
import org.hl7.v3.PRPAMT201306UV02PatientTelecom;
import org.hl7.v3.PRPAMT201306UV02PrincipalCareProviderId;
import org.hl7.v3.PRPAMT201306UV02QueryByParameter;
import org.hl7.v3.QUQIMT021001UV01AuthorOrPerformer;
import org.hl7.v3.STExplicit;
import org.hl7.v3.TELExplicit;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the PatientDiscoveryRequestComplianceChecker class.
 * 
 * @author Neil Webb
 */
public class PatientDiscoveryRequestComplianceCheckerTest {

	private PRPAIN201305UV02 request = null;
	private PatientDiscoveryRequestComplianceChecker testSubject = null;
	private ObjectFactory hl7V3Factory = null;
	
	@Before
	public void setUp() {
		hl7V3Factory = new ObjectFactory();
		request = createBasePatientDiscoveryRequest();
		testSubject = getTestSubject();
		verifyBaseObjectsNotNull(request);
	}
	
	private PatientDiscoveryRequestComplianceChecker getTestSubject() {
		return new PatientDiscoveryRequestComplianceChecker(request) {
			protected boolean isComplianceCheckEnabled() {
				return true;
			}
		};
	}
	
	// ########## VERIFY COMPLIANCE CHECK SWITCH ##################
	/**
	 * This test verifies that the compliance check is working. Disabled so the assigned device class code must not be modified.
	 */
	@Test
	public void testComplianceCheckSwitch() {
		testSubject = new PatientDiscoveryRequestComplianceChecker(request) {
			protected boolean isComplianceCheckEnabled() {
				return false;
			}
		};
		
		assertNull("Compliance check switch: The assigned device class code was not null before compliance check", request.getControlActProcess().getAuthorOrPerformer().get(0).getAssignedDevice().getValue().getClassCode());
		
		testSubject.update2011SpecCompliance();
		assertNull("Compliance check switch: The assigned device class code was not null after compliance check", request.getControlActProcess().getAuthorOrPerformer().get(0).getAssignedDevice().getValue().getClassCode());
	}

	// ########## ASSIGNED DEVICE CLASS CODE ##################
	/**
	 * This test verifies that the 
	 * PRPA_IN201305UV02/controlActProcess/authorOrPerformer/assignedDevice/@classCode 
	 * attribute is set to the correct value if it was missing in the original request.
	 */
	@Test
	public void testAssignedDeviceClassCodeMissing() {
		assertNull("The assigned device class code was not null before compliance check", request.getControlActProcess().getAuthorOrPerformer().get(0).getAssignedDevice().getValue().getClassCode());
		
		testSubject.update2011SpecCompliance();
		assertNotNull("The assigned device class code was null after compliance check", request.getControlActProcess().getAuthorOrPerformer().get(0).getAssignedDevice().getValue().getClassCode());
		assertEquals("The assigned device class code was incorrect after compliance check", PatientDiscoveryRequestComplianceChecker.ASSIGNED_DEVICE_CLASS_CODE, request.getControlActProcess().getAuthorOrPerformer().get(0).getAssignedDevice().getValue().getClassCode());
	}

	/**
	 * This test verifies that the 
	 * PRPA_IN201305UV02/controlActProcess/authorOrPerformer/assignedDevice/@classCode 
	 * attribute is set to the correct value if it was incorrect in the original request.
	 */
	@Test
	public void testAssignedDeviceClassCodeIncorrect() {
		assertNull("The assigned device class code was not null before compliance check", request.getControlActProcess().getAuthorOrPerformer().get(0).getAssignedDevice().getValue().getClassCode());
		request.getControlActProcess().getAuthorOrPerformer().get(0).getAssignedDevice().getValue().setClassCode("A");
		assertNotNull("The assigned device class code was null before compliance check", request.getControlActProcess().getAuthorOrPerformer().get(0).getAssignedDevice().getValue().getClassCode());
		assertFalse("The assigned device class code was already correct before compliance check", PatientDiscoveryRequestComplianceChecker.ASSIGNED_DEVICE_CLASS_CODE.equals(request.getControlActProcess().getAuthorOrPerformer().get(0).getAssignedDevice().getValue().getClassCode()));

		testSubject.update2011SpecCompliance();
		assertNotNull("The assigned device class code was null after compliance check", request.getControlActProcess().getAuthorOrPerformer().get(0).getAssignedDevice().getValue().getClassCode());
		assertEquals("The assigned device class code was incorrect after compliance check", PatientDiscoveryRequestComplianceChecker.ASSIGNED_DEVICE_CLASS_CODE, request.getControlActProcess().getAuthorOrPerformer().get(0).getAssignedDevice().getValue().getClassCode());
	}
	
	// ################# PATIENT GENDER ####################
	@Test
	public void testPatientGenderSemanticsTextMissing() {
		assertNull("Patient Gender semantics text was not null before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectAdministrativeGender().get(0).getSemanticsText());

		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Gender semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectAdministrativeGender().get(0).getSemanticsText());
		assertEquals("Patient Gender semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectAdministrativeGender().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Gender semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_GENDER, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectAdministrativeGender().get(0).getSemanticsText().getContent().get(0));
	}

	@Test
	public void testPatientGenderSemanticsTextContentListEmpty() {
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectAdministrativeGender().get(0).setSemanticsText(new STExplicit());
		assertNotNull("Patient Gender semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectAdministrativeGender().get(0).getSemanticsText());
		assertTrue("Patient Gender semantics text value list was not empty before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectAdministrativeGender().get(0).getSemanticsText().getContent().isEmpty());
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Gender semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectAdministrativeGender().get(0).getSemanticsText());
		assertEquals("Patient Gender semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectAdministrativeGender().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Gender semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_GENDER, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectAdministrativeGender().get(0).getSemanticsText().getContent().get(0));
	}

	@Test
	public void testPatientGenderSemanticsTextIncorrect() {
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectAdministrativeGender().get(0).setSemanticsText(new STExplicit());
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectAdministrativeGender().get(0).getSemanticsText().getContent().add("Wrong_Content");
		assertNotNull("Patient Gender semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectAdministrativeGender().get(0).getSemanticsText());
		assertFalse("Patient Gender semantics text value list was empty before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectAdministrativeGender().get(0).getSemanticsText().getContent().isEmpty());
		assertFalse("Patient Gender semantics text value was already correct before compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_GENDER.equals(request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectAdministrativeGender().get(0).getSemanticsText().getContent().get(0)));
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Gender semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectAdministrativeGender().get(0).getSemanticsText());
		assertEquals("Patient Gender semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectAdministrativeGender().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Gender semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_GENDER, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectAdministrativeGender().get(0).getSemanticsText().getContent().get(0));
	}
	
	// ################### PATIENT BIRTH TIME #################
	@Test
	public void testPatientDobSemanticsTextMissing() {
		assertNull("Patient DOB semantics text was not null before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthTime().get(0).getSemanticsText());

		testSubject.update2011SpecCompliance();
		assertNotNull("Patient DOB semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthTime().get(0).getSemanticsText());
		assertEquals("Patient DOB semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthTime().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient DOB semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_BIRTH_TIME, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthTime().get(0).getSemanticsText().getContent().get(0));
	}

	@Test
	public void testPatientDobSemanticsTextContentListEmpty() {
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthTime().get(0).setSemanticsText(new STExplicit());
		assertNotNull("Patient DOB semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthTime().get(0).getSemanticsText());
		assertTrue("Patient DOB semantics text value list was not empty before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthTime().get(0).getSemanticsText().getContent().isEmpty());
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Patient DOB semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthTime().get(0).getSemanticsText());
		assertEquals("Patient DOB semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthTime().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient DOB semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_BIRTH_TIME, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthTime().get(0).getSemanticsText().getContent().get(0));
	}

	@Test
	public void testPatientDobSemanticsTextIncorrect() {
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthTime().get(0).setSemanticsText(new STExplicit());
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthTime().get(0).getSemanticsText().getContent().add("Wrong_Content");
		assertNotNull("Patient DOB semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthTime().get(0).getSemanticsText());
		assertFalse("Patient DOB semantics text value list was empty before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthTime().get(0).getSemanticsText().getContent().isEmpty());
		assertFalse("Patient DOB semantics text value was already correct before compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_BIRTH_TIME.equals(request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthTime().get(0).getSemanticsText().getContent().get(0)));
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Patient DOB semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthTime().get(0).getSemanticsText());
		assertEquals("Patient DOB semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthTime().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient DOB semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_BIRTH_TIME, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthTime().get(0).getSemanticsText().getContent().get(0));
	}

	// ################### SUBJECT ID #######################
	@Test
	public void testPatientIdSemanticsTextMissing() {
		assertNull("Patient Id semantics text was not null before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectId().get(0).getSemanticsText());

		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Id semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectId().get(0).getSemanticsText());
		assertEquals("Patient Id semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectId().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Id semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_SUBJECT_ID, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectId().get(0).getSemanticsText().getContent().get(0));
	}

	@Test
	public void testPatientIdSemanticsTextContentListEmpty() {
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectId().get(0).setSemanticsText(new STExplicit());
		assertNotNull("Patient Id semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectId().get(0).getSemanticsText());
		assertTrue("Patient Id semantics text value list was not empty before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectId().get(0).getSemanticsText().getContent().isEmpty());
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Id semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectId().get(0).getSemanticsText());
		assertEquals("Patient Id semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectId().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Id semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_SUBJECT_ID, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectId().get(0).getSemanticsText().getContent().get(0));
	}

	@Test
	public void testPatientIdSemanticsTextIncorrect() {
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectId().get(0).setSemanticsText(new STExplicit());
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectId().get(0).getSemanticsText().getContent().add("Wrong_Content");
		assertNotNull("Patient Id semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectId().get(0).getSemanticsText());
		assertFalse("Patient Id semantics text value list was empty before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectId().get(0).getSemanticsText().getContent().isEmpty());
		assertFalse("Patient Id semantics text value was already correct before compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_SUBJECT_ID.equals(request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectId().get(0).getSemanticsText().getContent().get(0)));
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Id semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectId().get(0).getSemanticsText());
		assertEquals("Patient Id semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectId().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Id semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_SUBJECT_ID, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectId().get(0).getSemanticsText().getContent().get(0));
	}

	// ################### SUBJECT NAME #######################
	@Test
	public void testPatientNameSemanticsTextMissing() {
		assertNull("Patient Name semantics text was not null before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectName().get(0).getSemanticsText());

		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Name semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectName().get(0).getSemanticsText());
		assertEquals("Patient Name semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectName().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Name semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_NAME, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectName().get(0).getSemanticsText().getContent().get(0));
	}

	@Test
	public void testPatientNameSemanticsTextContentListEmpty() {
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectName().get(0).setSemanticsText(new STExplicit());
		assertNotNull("Patient Name semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectName().get(0).getSemanticsText());
		assertTrue("Patient Name semantics text value list was not empty before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectName().get(0).getSemanticsText().getContent().isEmpty());
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Name semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectName().get(0).getSemanticsText());
		assertEquals("Patient Name semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectName().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Name semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_NAME, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectName().get(0).getSemanticsText().getContent().get(0));
	}

	@Test
	public void testPatientNameSemanticsTextIncorrect() {
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectName().get(0).setSemanticsText(new STExplicit());
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectName().get(0).getSemanticsText().getContent().add("Wrong_Content");
		assertNotNull("Patient Name semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectName().get(0).getSemanticsText());
		assertFalse("Patient Name semantics text value list was empty before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectName().get(0).getSemanticsText().getContent().isEmpty());
		assertFalse("Patient Name semantics text value was already correct before compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_NAME.equals(request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectName().get(0).getSemanticsText().getContent().get(0)));
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Name semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectName().get(0).getSemanticsText());
		assertEquals("Patient Name semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectName().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Name semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_NAME, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectName().get(0).getSemanticsText().getContent().get(0));
	}

	// ################### PATIENT ADDRESS ####################
	@Test
	public void testPatientAddressSemanticsTextMissing() {
		assertNull("Patient Address semantics text was not null before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientAddress().get(0).getSemanticsText());

		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Address semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientAddress().get(0).getSemanticsText());
		assertEquals("Patient Address semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientAddress().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Address semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_ADDRESS, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientAddress().get(0).getSemanticsText().getContent().get(0));
	}

	@Test
	public void testPatientAddressSemanticsTextContentListEmpty() {
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientAddress().get(0).setSemanticsText(new STExplicit());
		assertNotNull("Patient Address semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientAddress().get(0).getSemanticsText());
		assertTrue("Patient Address semantics text value list was not empty before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientAddress().get(0).getSemanticsText().getContent().isEmpty());
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Address semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientAddress().get(0).getSemanticsText());
		assertEquals("Patient Address semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientAddress().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Address semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_ADDRESS, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientAddress().get(0).getSemanticsText().getContent().get(0));
	}

	@Test
	public void testPatientAddressSemanticsTextIncorrect() {
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientAddress().get(0).setSemanticsText(new STExplicit());
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientAddress().get(0).getSemanticsText().getContent().add("Wrong_Content");
		assertNotNull("Patient Address semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientAddress().get(0).getSemanticsText());
		assertFalse("Patient Address semantics text value list was empty before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientAddress().get(0).getSemanticsText().getContent().isEmpty());
		assertFalse("Patient Address semantics text value was already correct before compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_ADDRESS.equals(request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientAddress().get(0).getSemanticsText().getContent().get(0)));
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Address semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientAddress().get(0).getSemanticsText());
		assertEquals("Patient Address semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientAddress().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Address semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_ADDRESS, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientAddress().get(0).getSemanticsText().getContent().get(0));
	}

	// ################### BIRTH PLACE ADDRESS #######################
	@Test
	public void testPatientBirthAddressSemanticsTextMissing() {
		assertNull("Patient Birth Address semantics text was not null before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceAddress().get(0).getSemanticsText());

		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Birth Address semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceAddress().get(0).getSemanticsText());
		assertEquals("Patient Birth Address semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceAddress().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Birth Address semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_BIRTH_PLACE_ADDRESS, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceAddress().get(0).getSemanticsText().getContent().get(0));
	}

	@Test
	public void testPatientBirthAddressSemanticsTextContentListEmpty() {
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceAddress().get(0).setSemanticsText(new STExplicit());
		assertNotNull("Patient Birth Address semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceAddress().get(0).getSemanticsText());
		assertTrue("Patient Birth Address semantics text value list was not empty before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceAddress().get(0).getSemanticsText().getContent().isEmpty());
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Birth Address semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceAddress().get(0).getSemanticsText());
		assertEquals("Patient Birth Address semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceAddress().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Birth Address semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_BIRTH_PLACE_ADDRESS, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceAddress().get(0).getSemanticsText().getContent().get(0));
	}

	@Test
	public void testPatientBirthAddressSemanticsTextIncorrect() {
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceAddress().get(0).setSemanticsText(new STExplicit());
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceAddress().get(0).getSemanticsText().getContent().add("Wrong_Content");
		assertNotNull("Patient Birth Address semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceAddress().get(0).getSemanticsText());
		assertFalse("Patient Birth Address semantics text value list was empty before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceAddress().get(0).getSemanticsText().getContent().isEmpty());
		assertFalse("Patient Birth Address semantics text value was already correct before compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_BIRTH_PLACE_ADDRESS.equals(request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceAddress().get(0).getSemanticsText().getContent().get(0)));
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Birth Address semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceAddress().get(0).getSemanticsText());
		assertEquals("Patient Birth Address semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceAddress().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Birth Address semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_BIRTH_PLACE_ADDRESS, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceAddress().get(0).getSemanticsText().getContent().get(0));
	}

	// ################### BIRTH PLACE NAME #######################
	@Test
	public void testPatientBirthPlaceNameSemanticsTextMissing() {
		assertNull("Patient Birth Place Name semantics text was not null before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceName().get(0).getSemanticsText());

		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Birth Place Name semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceName().get(0).getSemanticsText());
		assertEquals("Patient Birth Place Name semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceName().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Birth Place Name semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_BIRTH_PLACE_NAME, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceName().get(0).getSemanticsText().getContent().get(0));
	}

	@Test
	public void testPatientBirthPlaceNameSemanticsTextContentListEmpty() {
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceName().get(0).setSemanticsText(new STExplicit());
		assertNotNull("Patient Birth Place Name semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceName().get(0).getSemanticsText());
		assertTrue("Patient DOB semantics text value list was not empty before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceName().get(0).getSemanticsText().getContent().isEmpty());
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Birth Place Name semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceName().get(0).getSemanticsText());
		assertEquals("Patient Birth Place Name semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceName().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Birth Place Name semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_BIRTH_PLACE_NAME, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceName().get(0).getSemanticsText().getContent().get(0));
	}

	@Test
	public void testPatientBirthPlaceNameSemanticsTextIncorrect() {
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceName().get(0).setSemanticsText(new STExplicit());
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceName().get(0).getSemanticsText().getContent().add("Wrong_Content");
		assertNotNull("Patient Birth Place Name semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceName().get(0).getSemanticsText());
		assertFalse("Patient Birth Place Name semantics text value list was empty before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceName().get(0).getSemanticsText().getContent().isEmpty());
		assertFalse("Patient Birth Place Name semantics text value was already correct before compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_BIRTH_PLACE_NAME.equals(request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceName().get(0).getSemanticsText().getContent().get(0)));
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Birth Place Name semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceName().get(0).getSemanticsText());
		assertEquals("Patient Birth Place Name semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceName().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Birth Place Name semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_BIRTH_PLACE_NAME, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceName().get(0).getSemanticsText().getContent().get(0));
	}

	// ################### PRINCIPAL CARE PROVIDER #######################
	@Test
	public void testPrincipalCareProviderIdSemanticsTextMissing() {
		assertNull("Principal CareProvider ID semantics text was not null before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPrincipalCareProviderId().get(0).getSemanticsText());

		testSubject.update2011SpecCompliance();
		assertNotNull("Principal CareProvider ID semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPrincipalCareProviderId().get(0).getSemanticsText());
		assertEquals("Principal CareProvider ID semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPrincipalCareProviderId().get(0).getSemanticsText().getContent().size());
		assertEquals("Principal CareProvider ID semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PRINCIPAL_CARE_PROVIDER, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPrincipalCareProviderId().get(0).getSemanticsText().getContent().get(0));
	}

	@Test
	public void testPrincipalCareProviderIdSemanticsTextContentListEmpty() {
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPrincipalCareProviderId().get(0).setSemanticsText(new STExplicit());
		assertNotNull("Principal CareProvider ID semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPrincipalCareProviderId().get(0).getSemanticsText());
		assertTrue("Principal CareProvider ID semantics text value list was not empty before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPrincipalCareProviderId().get(0).getSemanticsText().getContent().isEmpty());
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Principal CareProvider ID semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPrincipalCareProviderId().get(0).getSemanticsText());
		assertEquals("Principal CareProvider ID semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPrincipalCareProviderId().get(0).getSemanticsText().getContent().size());
		assertEquals("Principal CareProvider ID semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PRINCIPAL_CARE_PROVIDER, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPrincipalCareProviderId().get(0).getSemanticsText().getContent().get(0));
	}

	@Test
	public void testPrincipalCareProviderIdSemanticsTextIncorrect() {
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPrincipalCareProviderId().get(0).setSemanticsText(new STExplicit());
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPrincipalCareProviderId().get(0).getSemanticsText().getContent().add("Wrong_Content");
		assertNotNull("Principal CareProvider ID semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPrincipalCareProviderId().get(0).getSemanticsText());
		assertFalse("Principal CareProvider ID semantics text value list was empty before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPrincipalCareProviderId().get(0).getSemanticsText().getContent().isEmpty());
		assertFalse("Principal CareProvider ID semantics text value was already correct before compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PRINCIPAL_CARE_PROVIDER.equals(request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPrincipalCareProviderId().get(0).getSemanticsText().getContent().get(0)));
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Principal CareProvider ID semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPrincipalCareProviderId().get(0).getSemanticsText());
		assertEquals("Principal CareProvider ID semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPrincipalCareProviderId().get(0).getSemanticsText().getContent().size());
		assertEquals("Principal CareProvider ID semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PRINCIPAL_CARE_PROVIDER, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPrincipalCareProviderId().get(0).getSemanticsText().getContent().get(0));
	}

	// ################### MOTHERS MAIDEN NAME #######################
	@Test
	public void testPatientMothersMaidenNameSemanticsTextMissing() {
		assertNull("Patient Mothers Maiden Name semantics text was not null before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().get(0).getSemanticsText());

		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Mothers Maiden Name semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().get(0).getSemanticsText());
		assertEquals("Patient Mothers Maiden Name semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Mothers Maiden Name semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_MAIDEN_NAME_MOTHER, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().get(0).getSemanticsText().getContent().get(0));
	}

	@Test
	public void testPatientMothersMaidenNameSemanticsTextContentListEmpty() {
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().get(0).setSemanticsText(new STExplicit());
		assertNotNull("Patient Mothers Maiden Name semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().get(0).getSemanticsText());
		assertTrue("Patient Mothers Maiden Name semantics text value list was not empty before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().get(0).getSemanticsText().getContent().isEmpty());
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Mothers Maiden Name semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().get(0).getSemanticsText());
		assertEquals("Patient Mothers Maiden Name semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Mothers Maiden Name semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_MAIDEN_NAME_MOTHER, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().get(0).getSemanticsText().getContent().get(0));
	}

	@Test
	public void testPatientMothersMaidenNameSemanticsTextIncorrect() {
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().get(0).setSemanticsText(new STExplicit());
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().get(0).getSemanticsText().getContent().add("Wrong_Content");
		assertNotNull("Patient Mothers Maiden Name semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().get(0).getSemanticsText());
		assertFalse("Patient Mothers Maiden Name semantics text value list was empty before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().get(0).getSemanticsText().getContent().isEmpty());
		assertFalse("Patient Mothers Maiden Name semantics text value was already correct before compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_MAIDEN_NAME_MOTHER.equals(request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().get(0).getSemanticsText().getContent().get(0)));
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Mothers Maiden Name semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().get(0).getSemanticsText());
		assertEquals("Patient Mothers Maiden Name semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Mothers Maiden Name semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_MAIDEN_NAME_MOTHER, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().get(0).getSemanticsText().getContent().get(0));
	}

	@Test
	public void testPatientMothersMaidenNameRemoveIfEmptyString() {
		assertFalse("Patient Mothers Maiden Name value list was empty", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().get(0).getValue().isEmpty());
		assertNotNull("Patient Mothers Maiden Name value list object was null", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().get(0).getValue().get(0));
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().get(0).getValue().get(0).getContent().clear();
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().get(0).getValue().get(0).getContent().add("");

		testSubject.update2011SpecCompliance();
		assertTrue("Patient Mothers Maiden Name semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().isEmpty());
	}

	// ################### PATIENT TELECOM #######################
	@Test
	public void testPatientTelecomSemanticsTextMissing() {
		assertNull("Patient Mothers Maiden Name semantics text was not null before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientTelecom().get(0).getSemanticsText());

		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Telecom semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientTelecom().get(0).getSemanticsText());
		assertEquals("Patient Telecom semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientTelecom().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Telecom semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_TELECOM, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientTelecom().get(0).getSemanticsText().getContent().get(0));
	}

	@Test
	public void testPatientTelecomSemanticsTextContentListEmpty() {
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientTelecom().get(0).setSemanticsText(new STExplicit());
		assertNotNull("Patient Telecom semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientTelecom().get(0).getSemanticsText());
		assertTrue("Patient Telecom semantics text value list was not empty before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientTelecom().get(0).getSemanticsText().getContent().isEmpty());
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Telecom semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientTelecom().get(0).getSemanticsText());
		assertEquals("Patient Telecom semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientTelecom().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Telecom semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_TELECOM, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientTelecom().get(0).getSemanticsText().getContent().get(0));
	}

	@Test
	public void testPatientTelecomSemanticsTextIncorrect() {
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientTelecom().get(0).setSemanticsText(new STExplicit());
		request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientTelecom().get(0).getSemanticsText().getContent().add("Wrong_Content");
		assertNotNull("Patient Telecom semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientTelecom().get(0).getSemanticsText());
		assertFalse("Patient Telecom semantics text value list was empty before compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientTelecom().get(0).getSemanticsText().getContent().isEmpty());
		assertFalse("Patient Telecom semantics text value was already correct before compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_TELECOM.equals(request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientTelecom().get(0).getSemanticsText().getContent().get(0)));
		
		testSubject.update2011SpecCompliance();
		assertNotNull("Patient Telecom semantics text was null after compliance update", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientTelecom().get(0).getSemanticsText());
		assertEquals("Patient Telecom semantics text value list did not have one single value after compliance update", 1, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientTelecom().get(0).getSemanticsText().getContent().size());
		assertEquals("Patient Telecom semantics text value was not correct after compliance update", PatientDiscoveryRequestComplianceChecker.SEMANTICS_TEXT_REPRESENTATION_PATIENT_TELECOM, request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientTelecom().get(0).getSemanticsText().getContent().get(0));
	}

	// ####################### TEST UTILITY #########################
	private void verifyBaseObjectsNotNull(PRPAIN201305UV02 request) {
		assertNotNull("PRPAIN201305UV02 request was null", request);
		assertNotNull("ControlActProcess was null", request.getControlActProcess());
		assertNotNull("AuthorOrPerformer list was null", request.getControlActProcess().getAuthorOrPerformer());
		assertFalse("AuthorOrPerformer list was empty", request.getControlActProcess().getAuthorOrPerformer().isEmpty());
		assertNotNull("AssignedDevice JAXB element was null", request.getControlActProcess().getAuthorOrPerformer().get(0).getAssignedDevice());
		assertNotNull("AssignedDevice object was null", request.getControlActProcess().getAuthorOrPerformer().get(0).getAssignedDevice().getValue());
		assertNotNull("Query by parameter JAXB element was null", request.getControlActProcess().getQueryByParameter());
		assertNotNull("Query by parameter object was null", request.getControlActProcess().getQueryByParameter().getValue());
		assertNotNull("Parameter list was null", request.getControlActProcess().getQueryByParameter().getValue().getParameterList());
		assertFalse("Patient Gender list was empty", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectAdministrativeGender().isEmpty());
		assertNotNull("Patient Gender list object was null", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectAdministrativeGender().get(0));
		assertFalse("Patient DOB list was empty", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthTime().isEmpty());
		assertNotNull("Patient DOB list object was null", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthTime().get(0));
		assertFalse("Patient Id list was empty", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectId().isEmpty());
		assertNotNull("Patient Id list object was null", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectId().get(0));
		assertFalse("Patient Name list was empty", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectName().isEmpty());
		assertNotNull("Patient Name list object was null", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectName().get(0));
		assertFalse("Patient Address list was empty", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientAddress().isEmpty());
		assertNotNull("Patient Address list object was null", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientAddress().get(0));
		assertFalse("Patient Birth Place Address list was empty", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceAddress().isEmpty());
		assertNotNull("Patient Birth Place Address list object was null", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceAddress().get(0));
		assertFalse("Patient Birth Place Name list was empty", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceName().isEmpty());
		assertNotNull("Patient Birth Place Name list object was null", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getLivingSubjectBirthPlaceName().get(0));
		assertFalse("Patient Principal Care Provider ID list was empty", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPrincipalCareProviderId().isEmpty());
		assertNotNull("Patient Principal Care Provider ID list object was null", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPrincipalCareProviderId().get(0));
		assertFalse("Patient Mothers Maiden Name list was empty", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().isEmpty());
		assertNotNull("Patient Mothers Maiden Name list object was null", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getMothersMaidenName().get(0));
		assertFalse("Patient Telecom list was empty", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientTelecom().isEmpty());
		assertNotNull("Patient Telecom list object was null", request.getControlActProcess().getQueryByParameter().getValue().getParameterList().getPatientTelecom().get(0));
	}

	private PRPAIN201305UV02 createBasePatientDiscoveryRequest() {
		PRPAIN201305UV02 request = new PRPAIN201305UV02();
		request.setControlActProcess(createControlActProcess());
		return request;
	}

	private PRPAIN201305UV02QUQIMT021001UV01ControlActProcess createControlActProcess() {
		PRPAIN201305UV02QUQIMT021001UV01ControlActProcess controlActProcess = new PRPAIN201305UV02QUQIMT021001UV01ControlActProcess();
		controlActProcess.getAuthorOrPerformer().add(createAuthorOrPerformer());
		controlActProcess.setQueryByParameter(createQueryByParameter());
		return controlActProcess;
	}

	private QUQIMT021001UV01AuthorOrPerformer createAuthorOrPerformer() {
		QUQIMT021001UV01AuthorOrPerformer authorOrPerformer = new QUQIMT021001UV01AuthorOrPerformer();
        COCTMT090300UV01AssignedDevice assignedDevice = new COCTMT090300UV01AssignedDevice();
        javax.xml.namespace.QName xmlqname = new javax.xml.namespace.QName("urn:hl7-org:v3", "assignedDevice");
        JAXBElement<COCTMT090300UV01AssignedDevice> assignedDeviceJAXBElement = new JAXBElement<COCTMT090300UV01AssignedDevice>(xmlqname, COCTMT090300UV01AssignedDevice.class, assignedDevice);
        authorOrPerformer.setAssignedDevice(assignedDeviceJAXBElement);
        return authorOrPerformer;
	}
	
	private JAXBElement<PRPAMT201306UV02QueryByParameter> createQueryByParameter() {
        PRPAMT201306UV02QueryByParameter params = new PRPAMT201306UV02QueryByParameter();

//        params.setQueryId(HL7MessageIdGenerator.GenerateHL7MessageId(localAssigningAuthority));
//        params.setResponsePriorityCode(HL7DataTransformHelper.CSFactory("I"));
//        params.setResponseModalityCode(HL7DataTransformHelper.CSFactory("R"));
//        params.setStatusCode(HL7DataTransformHelper.CSFactory("new"));

        // Parameter list
        PRPAMT201306UV02ParameterList parameterList = new PRPAMT201306UV02ParameterList();
        params.setParameterList(parameterList);

        setGender(parameterList);
        setDateOfBirth(parameterList);
        setPatientId(parameterList);        
        setPersonName(parameterList);
        setAddresses(parameterList);
        setBirthPlaceAddress(parameterList);
        setBirthPlaceName(parameterList);
        setPrincipalCareProviderID(parameterList);
        setMothersMaidenName(parameterList);
        setPatientTelecom(parameterList);        
        
        return hl7V3Factory.createPRPAIN201305UV02QUQIMT021001UV01ControlActProcessQueryByParameter(params);
	}

	private void setGender(PRPAMT201306UV02ParameterList parameterList) {
        PRPAMT201306UV02LivingSubjectAdministrativeGender patientGender = new PRPAMT201306UV02LivingSubjectAdministrativeGender();
        CE patientGenderValue = new CE();
        patientGenderValue.setCode("U");
        patientGender.getValue().add(patientGenderValue);
        parameterList.getLivingSubjectAdministrativeGender().add(patientGender);
    }
    
    private void setDateOfBirth(PRPAMT201306UV02ParameterList parameterList) {
        PRPAMT201306UV02LivingSubjectBirthTime dob = new PRPAMT201306UV02LivingSubjectBirthTime();
        IVLTSExplicit dobValue = new IVLTSExplicit();
        dobValue.setValue("19600101");
        dob.getValue().add(dobValue);
        parameterList.getLivingSubjectBirthTime().add(dob);
    }

    private void setPatientId(PRPAMT201306UV02ParameterList parameterList) {
		PRPAMT201306UV02LivingSubjectId patientId = new PRPAMT201306UV02LivingSubjectId();
		II id = new II();
		id.setRoot("aa");
		id.setExtension("id");
		patientId.getValue().add(id );
		parameterList.getLivingSubjectId().add(patientId );
	}

    private void setPersonName(PRPAMT201306UV02ParameterList parameterList) {
        PRPAMT201306UV02LivingSubjectName personName = new PRPAMT201306UV02LivingSubjectName();
        ENExplicit personNameValues = new ENExplicit();
        personName.getValue().add(personNameValues);
        parameterList.getLivingSubjectName().add(personName);
        EnExplicitFamily lastName = new EnExplicitFamily();
        lastName.setContent("Last");
        personNameValues.getContent().add(hl7V3Factory.createENExplicitFamily(lastName));
        EnExplicitGiven firstName = new EnExplicitGiven();
        firstName.setContent("First");
        personNameValues.getContent().add(hl7V3Factory.createENExplicitGiven(firstName));
        EnExplicitGiven middleName = new EnExplicitGiven();
        middleName.setContent("Middle");
        personNameValues.getContent().add(hl7V3Factory.createENExplicitGiven(middleName));
        EnExplicitPrefix prefix = new EnExplicitPrefix();
        prefix.setContent("Title");
        personNameValues.getContent().add(hl7V3Factory.createENExplicitPrefix(prefix));
        EnExplicitSuffix suffix = new EnExplicitSuffix();
        suffix.setContent("Suffix");
        personNameValues.getContent().add(hl7V3Factory.createENExplicitSuffix(suffix));
    }

	private void setAddresses(PRPAMT201306UV02ParameterList parameterList) {
        PRPAMT201306UV02PatientAddress address = new PRPAMT201306UV02PatientAddress();
        parameterList.getPatientAddress().add(address);
        ADExplicit addressValues = new ADExplicit();
        addressValues.getUse().add("H");
        AdxpExplicitStreetAddressLine street1 = new AdxpExplicitStreetAddressLine();
        street1.setContent("Street 1");
        addressValues.getContent().add(hl7V3Factory.createADExplicitStreetAddressLine(street1));
        AdxpExplicitStreetAddressLine street2 = new AdxpExplicitStreetAddressLine();
        street2.setContent("Street 2");
        addressValues.getContent().add(hl7V3Factory.createADExplicitStreetAddressLine(street2));
        AdxpExplicitCity city = new AdxpExplicitCity();
        city.setContent("City");
        addressValues.getContent().add(hl7V3Factory.createADExplicitCity(city));
        AdxpExplicitState state = new AdxpExplicitState();
        state.setContent("ST");
        addressValues.getContent().add(hl7V3Factory.createADExplicitState(state));
        AdxpExplicitPostalCode postalCode = new AdxpExplicitPostalCode();
        postalCode.setContent("POSTAL");
        addressValues.getContent().add(hl7V3Factory.createADExplicitPostalCode(postalCode));
        AdxpExplicitCountry country = new AdxpExplicitCountry();
        country.setContent("US");
        addressValues.getContent().add(hl7V3Factory.createADExplicitCountry(country));
        address.getValue().add(addressValues);
    }

    private void setBirthPlaceAddress(PRPAMT201306UV02ParameterList parameterList) {
		
		PRPAMT201306UV02LivingSubjectBirthPlaceAddress birthPlaceAddress = new PRPAMT201306UV02LivingSubjectBirthPlaceAddress();
		parameterList.getLivingSubjectBirthPlaceAddress().add(birthPlaceAddress);
        ADExplicit addressValues = new ADExplicit();
        addressValues.getUse().add("H");
        AdxpExplicitStreetAddressLine street1 = new AdxpExplicitStreetAddressLine();
        street1.setContent("Street 1");
        addressValues.getContent().add(hl7V3Factory.createADExplicitStreetAddressLine(street1));
        AdxpExplicitStreetAddressLine street2 = new AdxpExplicitStreetAddressLine();
        street2.setContent("Street 2");
        addressValues.getContent().add(hl7V3Factory.createADExplicitStreetAddressLine(street2));
        AdxpExplicitCity city = new AdxpExplicitCity();
        city.setContent("City");
        addressValues.getContent().add(hl7V3Factory.createADExplicitCity(city));
        AdxpExplicitState state = new AdxpExplicitState();
        state.setContent("ST");
        addressValues.getContent().add(hl7V3Factory.createADExplicitState(state));
        AdxpExplicitPostalCode postalCode = new AdxpExplicitPostalCode();
        postalCode.setContent("POSTAL");
        addressValues.getContent().add(hl7V3Factory.createADExplicitPostalCode(postalCode));
        AdxpExplicitCountry country = new AdxpExplicitCountry();
        country.setContent("US");
        addressValues.getContent().add(hl7V3Factory.createADExplicitCountry(country));
        birthPlaceAddress.getValue().add(addressValues);
	}

	private void setBirthPlaceName(PRPAMT201306UV02ParameterList parameterList) {
		PRPAMT201306UV02LivingSubjectBirthPlaceName birthPlaceName = new PRPAMT201306UV02LivingSubjectBirthPlaceName();
		ENExplicit birthPlaceNameValue = new ENExplicit();
		birthPlaceNameValue.getContent().add("BirthPlaceName");
		birthPlaceName.getValue().add(birthPlaceNameValue );
		parameterList.getLivingSubjectBirthPlaceName().add(birthPlaceName );
	}

	private void setPrincipalCareProviderID(PRPAMT201306UV02ParameterList parameterList) {
		PRPAMT201306UV02PrincipalCareProviderId principalCareProviderId = new PRPAMT201306UV02PrincipalCareProviderId();
		II providerId = new II();
		providerId.setRoot("Root");
		providerId.setExtension("ProviderID");
		principalCareProviderId.getValue().add(providerId );
		parameterList.getPrincipalCareProviderId().add(principalCareProviderId );
	}

    private void setMothersMaidenName(PRPAMT201306UV02ParameterList parameterList) {
        PRPAMT201306UV02MothersMaidenName mothersMaidenName = new PRPAMT201306UV02MothersMaidenName();
        PNExplicit maidenNameValue = new PNExplicit();
        maidenNameValue.getContent().add("Maiden");
		mothersMaidenName.getValue().add(maidenNameValue);
		parameterList.getMothersMaidenName().add(mothersMaidenName );
    }

	private void setPatientTelecom(PRPAMT201306UV02ParameterList parameterList) {
		PRPAMT201306UV02PatientTelecom patientTelecom = new PRPAMT201306UV02PatientTelecom();
		TELExplicit telecomValue = new TELExplicit();
		telecomValue.setValue("TelValue");
		patientTelecom.getValue().add(telecomValue );
		parameterList.getPatientTelecom().add(patientTelecom );
	}

}
