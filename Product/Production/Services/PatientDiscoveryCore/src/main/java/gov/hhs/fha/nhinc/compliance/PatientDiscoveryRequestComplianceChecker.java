package gov.hhs.fha.nhinc.compliance;

import gov.hhs.fha.nhinc.properties.PropertyAccessor;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.hl7.v3.BinaryDataEncoding;
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
import org.hl7.v3.QUQIMT021001UV01AuthorOrPerformer;
import org.hl7.v3.STExplicit;

/**
 * Utility class to ensure that a patient discovery request message meets 
 * specification compliance. This approach was chosen as message generated
 * at the consumer level are not enforced by the WSDL contract so this is a 
 * fail-safe step before sending the request message. 
 * 
 * The initial implementation of this utility only checks known problems and 
 * should not be considered conclusive of all potential discrepancies with 
 * the specification.
 * 
 * @author Neil Webb
 *
 */
public class PatientDiscoveryRequestComplianceChecker implements ComplianceChecker {

    private static final Logger LOG = Logger.getLogger(PatientDiscoveryRequestComplianceChecker.class);
	private static final String PATIENT_DISCOVERY_REQUEST_COMPLIANCE_CHECK_ENABLED_KEY = "PatientDiscoveryRequestComplianceCheckEnabled";
	
	public static final String ASSIGNED_DEVICE_CLASS_CODE = "ASSIGNED";
	
	public static final String SEMANTICS_TEXT_REPRESENTATION_PATIENT_GENDER = "LivingSubject.administrativeGender";
	public static final String SEMANTICS_TEXT_REPRESENTATION_PATIENT_BIRTH_TIME = "LivingSubject.birthTime";
	public static final String SEMANTICS_TEXT_REPRESENTATION_SUBJECT_ID = "LivingSubject.id";
	public static final String SEMANTICS_TEXT_REPRESENTATION_PATIENT_NAME = "LivingSubject.name";
	public static final String SEMANTICS_TEXT_REPRESENTATION_PATIENT_ADDRESS = "Patient.addr";
	public static final String SEMANTICS_TEXT_REPRESENTATION_PATIENT_BIRTH_PLACE_ADDRESS = "LivingSubject.BirthPlace.Addr";
	public static final String SEMANTICS_TEXT_REPRESENTATION_PATIENT_BIRTH_PLACE_NAME = "LivingSubject.BirthPlace.Place.Name";
	public static final String SEMANTICS_TEXT_REPRESENTATION_PRINCIPAL_CARE_PROVIDER = "AssignedProvider.id";
	public static final String SEMANTICS_TEXT_REPRESENTATION_PATIENT_MAIDEN_NAME_MOTHER = "Person.MothersMaidenName";
	public static final String SEMANTICS_TEXT_REPRESENTATION_PATIENT_TELECOM = "Patient.telecom";
	
	private PRPAIN201305UV02 pdRequest = null;
	
	public PatientDiscoveryRequestComplianceChecker(
			PRPAIN201305UV02 pdRequest) {
		this.pdRequest = pdRequest;
	}
	
	protected boolean isComplianceCheckEnabled() {
		boolean complianceCheckEnabled = true;
		try {
			complianceCheckEnabled = PropertyAccessor.getInstance().getPropertyBoolean(PROPERTIES_FILE_GATEWAY, PATIENT_DISCOVERY_REQUEST_COMPLIANCE_CHECK_ENABLED_KEY);
		} catch(Throwable t) {
			LOG.error("Error checking PD request compliance check flag: " + t.getMessage(), t);
		}
		return complianceCheckEnabled;
	}

	@Override
	public void update2011SpecCompliance() {
		if(pdRequest == null) {
			LOG.debug("Patient discovery request compliance check. Request was null - bypassing.");
			return;
		}
		
		if(!isComplianceCheckEnabled()) {
			LOG.debug("Patient discovery request compliance check was not enabled - bypassing.");
			return;
		}
		
		LOG.debug("Patient discovery request compliance check was enabled - performing updates.");
		updateControlActProcess();
	}

	private void updateControlActProcess() {
		if((pdRequest != null) && (pdRequest.getControlActProcess() != null)) {
			PRPAIN201305UV02QUQIMT021001UV01ControlActProcess controlActProcess = pdRequest.getControlActProcess();
			if(controlActProcess != null) {
				updateAuthorOrPerformer(controlActProcess);
				updateQueryByParameter(controlActProcess);
			}
		}
	}

	/**
	 * Update the author or performer
	 * Set the author or performer -> assigned device class code value
	 * 
	 * @param controlActProcess
	 */
	private void updateAuthorOrPerformer(PRPAIN201305UV02QUQIMT021001UV01ControlActProcess controlActProcess) {
		if(!controlActProcess.getAuthorOrPerformer().isEmpty()) {
			for(QUQIMT021001UV01AuthorOrPerformer authorOrPerformer : controlActProcess.getAuthorOrPerformer()) {
				if((authorOrPerformer.getAssignedDevice() != null) && (authorOrPerformer.getAssignedDevice().getValue() != null)) {
					// Update the value of authorOrPerformer/assignedDevice/@classCode
					authorOrPerformer.getAssignedDevice().getValue().setClassCode(ASSIGNED_DEVICE_CLASS_CODE);
				}
			}
		}
	}

	/**
	 * Update query by parameter values as needed
	 * 
	 * @param controlActProcess
	 */
	private void updateQueryByParameter(PRPAIN201305UV02QUQIMT021001UV01ControlActProcess controlActProcess) {
			if((controlActProcess.getQueryByParameter() != null) && (controlActProcess.getQueryByParameter().getValue() != null) && (controlActProcess.getQueryByParameter().getValue().getParameterList() != null)) {
				PRPAMT201306UV02ParameterList parameterList = controlActProcess.getQueryByParameter().getValue().getParameterList();
				updateQueryParamPatientGender(parameterList);
				updateQueryParamPatientBirthTime(parameterList);
				updateQueryParamSubjectId(parameterList);
				updateQueryParamPatientName(parameterList);
				updateQueryParamPatientAddress(parameterList);
				updateQueryParamPatientBirthPlaceAddress(parameterList);
				updateQueryParamPatientBirthPlaceName(parameterList);
				updateQueryParamPrincipalCareProviderId(parameterList);
				updateQueryParamMothersMaidenName(parameterList);
				updateQueryParamPatientTelecom(parameterList);
			}
	}

	/**
	 * Update the patient gender query parameter
	 * Set the semantics text value.
	 * 
	 * @param queryByParameter
	 */
	private void updateQueryParamPatientGender(PRPAMT201306UV02ParameterList parameterList) {
		if(!parameterList.getLivingSubjectAdministrativeGender().isEmpty()) {
			for(PRPAMT201306UV02LivingSubjectAdministrativeGender gender : parameterList.getLivingSubjectAdministrativeGender()) {
				gender.setSemanticsText(createSTExplicit(SEMANTICS_TEXT_REPRESENTATION_PATIENT_GENDER));
			}
		}
		
	}
	
	/**
	 * Update the patient birth time query parameter
	 * Set the semantics text value.
	 * 
	 * @param queryByParameter
	 */
	private void updateQueryParamPatientBirthTime(PRPAMT201306UV02ParameterList parameterList) {
		if(!parameterList.getLivingSubjectBirthTime().isEmpty()) {
			for(PRPAMT201306UV02LivingSubjectBirthTime birthTime : parameterList.getLivingSubjectBirthTime()) {
				birthTime.setSemanticsText(createSTExplicit(SEMANTICS_TEXT_REPRESENTATION_PATIENT_BIRTH_TIME));
			}
		}
		
	}
	
	/**
	 * Update the subject id query parameter
	 * Set the semantics text value.
	 * 
	 * @param queryByParameter
	 */
	private void updateQueryParamSubjectId(PRPAMT201306UV02ParameterList parameterList) {
		if(!parameterList.getLivingSubjectId().isEmpty()) {
			for(PRPAMT201306UV02LivingSubjectId subjectId : parameterList.getLivingSubjectId()) {
				subjectId.setSemanticsText(createSTExplicit(SEMANTICS_TEXT_REPRESENTATION_SUBJECT_ID));
			}
		}
		
	}
	
	/**
	 * Update the patient name query parameter
	 * Set the semantics text value.
	 * 
	 * @param queryByParameter
	 */
	private void updateQueryParamPatientName(PRPAMT201306UV02ParameterList parameterList) {
		if(!parameterList.getLivingSubjectName().isEmpty()) {
			for(PRPAMT201306UV02LivingSubjectName name : parameterList.getLivingSubjectName()) {
				name.setSemanticsText(createSTExplicit(SEMANTICS_TEXT_REPRESENTATION_PATIENT_NAME));
			}
		}
		
	}
	
	/**
	 * Update the patient address query parameter
	 * Set the semantics text value.
	 * 
	 * @param queryByParameter
	 */
	private void updateQueryParamPatientAddress(PRPAMT201306UV02ParameterList parameterList) {
		if(!parameterList.getPatientAddress().isEmpty()) {
			for(PRPAMT201306UV02PatientAddress patientAddress : parameterList.getPatientAddress()) {
				patientAddress.setSemanticsText(createSTExplicit(SEMANTICS_TEXT_REPRESENTATION_PATIENT_ADDRESS));
			}
		}
		
	}
	
	/**
	 * Update the patient birth place address query parameter
	 * Set the semantics text value.
	 * 
	 * @param queryByParameter
	 */
	private void updateQueryParamPatientBirthPlaceAddress(PRPAMT201306UV02ParameterList parameterList) {
		if(!parameterList.getLivingSubjectBirthPlaceAddress().isEmpty()) {
			for(PRPAMT201306UV02LivingSubjectBirthPlaceAddress patientBirthPlaceAddress : parameterList.getLivingSubjectBirthPlaceAddress()) {
				patientBirthPlaceAddress.setSemanticsText(createSTExplicit(SEMANTICS_TEXT_REPRESENTATION_PATIENT_BIRTH_PLACE_ADDRESS));
			}
		}
		
	}
	
	/**
	 * Update the patient birth place name query parameter
	 * Set the semantics text value.
	 * 
	 * @param queryByParameter
	 */
	private void updateQueryParamPatientBirthPlaceName(PRPAMT201306UV02ParameterList parameterList) {
		if(!parameterList.getLivingSubjectBirthPlaceName().isEmpty()) {
			for(PRPAMT201306UV02LivingSubjectBirthPlaceName birthPlaceName : parameterList.getLivingSubjectBirthPlaceName()) {
				birthPlaceName.setSemanticsText(createSTExplicit(SEMANTICS_TEXT_REPRESENTATION_PATIENT_BIRTH_PLACE_NAME));
			}
		}
		
	}
	
	/**
	 * Update the principal care provider ID query parameter
	 * Set the semantics text value.
	 * 
	 * @param queryByParameter
	 */
	private void updateQueryParamPrincipalCareProviderId(PRPAMT201306UV02ParameterList parameterList) {
		if(!parameterList.getPrincipalCareProviderId().isEmpty()) {
			for(PRPAMT201306UV02PrincipalCareProviderId principalCareProviderId : parameterList.getPrincipalCareProviderId()) {
				principalCareProviderId.setSemanticsText(createSTExplicit(SEMANTICS_TEXT_REPRESENTATION_PRINCIPAL_CARE_PROVIDER));
			}
		}
		
	}
	
	/**
	 * Update the mother's maiden name query parameter. 
	 * Remove if the maiden name value is null or an empty string.
	 * Set the semantics text value.
	 * 
	 * @param queryByParameter
	 */
	private void updateQueryParamMothersMaidenName(PRPAMT201306UV02ParameterList parameterList) {
		if(!parameterList.getMothersMaidenName().isEmpty()) {
			Iterator<PRPAMT201306UV02MothersMaidenName> maidenNameIter = parameterList.getMothersMaidenName().iterator();
			while(maidenNameIter.hasNext()) {
				PRPAMT201306UV02MothersMaidenName mothersMaidenName = maidenNameIter.next();
				String maidenNameValue = null;
				if((!mothersMaidenName.getValue().isEmpty()) && (mothersMaidenName.getValue().get(0) != null) && (!mothersMaidenName.getValue().get(0).getContent().isEmpty())) {
					Object serializableValue = mothersMaidenName.getValue().get(0).getContent().get(0);
					if(serializableValue instanceof String) {
						maidenNameValue = (String)serializableValue;
					}
				}
				if((maidenNameValue == null) || (maidenNameValue.trim().equals(""))) {
					maidenNameIter.remove();
				} else {
					mothersMaidenName.setSemanticsText(createSTExplicit(SEMANTICS_TEXT_REPRESENTATION_PATIENT_MAIDEN_NAME_MOTHER));
				}
			}
		}
		
	}

	/**
	 * Update the Patient Telecom query parameter
	 * Set the semantics text value.
	 * 
	 * @param queryByParameter
	 */
	private void updateQueryParamPatientTelecom(PRPAMT201306UV02ParameterList parameterList) {
		if(!parameterList.getPatientTelecom().isEmpty()) {
			for(PRPAMT201306UV02PatientTelecom patientTelecom : parameterList.getPatientTelecom()) {
				patientTelecom.setSemanticsText(createSTExplicit(SEMANTICS_TEXT_REPRESENTATION_PATIENT_TELECOM));
			}
		}
		
	}
	
	private STExplicit createSTExplicit(String stContent) {
		STExplicit st = new STExplicit();
		st.setRepresentation(BinaryDataEncoding.TXT);
		st.getContent().add(stContent);
		return st;
	}
	
}
