package gov.hhs.fha.nhinc.compliance;

import gov.hhs.fha.nhinc.properties.PropertyAccessor;

import org.apache.log4j.Logger;
import org.hl7.v3.CD;
import org.hl7.v3.CE;
import org.hl7.v3.CS;
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

/**
 * Utility class to ensure that a patient discovery response message meets 
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
public class PatientDiscoveryResponseComplianceChecker implements ComplianceChecker {

	private static final String PATIENT_DISCOVERY_RESPONSE_COMPLIANCE_CHECK_ENABLED_KEY = "PatientDiscoveryResponseComplianceCheckEnabled";
	public static final String PATIENT_STATUS_CODE = "active";
	public static final String PATIENT_SUBJECT_OF_QUERY_MATCH_OBS_CLASS_CODE = "OBS";
	public static final String CUSTODIAN_ASSIGNED_ENTITY_CODE_CODE = "NotHealthDataLocator";
	public static final String CUSTODIAN_ASSIGNED_ENTITY_CODE_CODE_SYSTEM = "1.3.6.1.4.1.19376.1.2.27.2";
	public static final String CONTROL_ACT_PROCESS_CODE_CODE = "PRPA_TE201306UV02";
	public static final String CONTROL_ACT_PROCESS_CODE_CODE_SYSTEM = "2.16.840.1.113883.1.6";
	
    private static final Logger LOG = Logger.getLogger(PatientDiscoveryResponseComplianceChecker.class);
	private PRPAIN201306UV02 response = null;
	
	public PatientDiscoveryResponseComplianceChecker(PRPAIN201306UV02 response) {
		this.response = response;
	}

	protected boolean isComplianceCheckEnabled() {
		boolean complianceCheckEnabled = true;
		try {
			complianceCheckEnabled = PropertyAccessor.getInstance().getPropertyBoolean(PROPERTIES_FILE_GATEWAY, PATIENT_DISCOVERY_RESPONSE_COMPLIANCE_CHECK_ENABLED_KEY);
		} catch(Throwable t) {
			LOG.error("Error checking PD request compliance check flag: " + t.getMessage(), t);
		}
		return complianceCheckEnabled;
	}

	@Override
	public void update2011SpecCompliance() {
		if(response == null) {
			LOG.debug("Patient discovery response compliance check. Response message was null - bypassing.");
			return;
		}
		
		if(!isComplianceCheckEnabled()) {
			LOG.debug("Patient discovery response compliance check was not enabled - bypassing.");
			return;
		}
		
		LOG.debug("Patient discovery response compliance check was enabled - performing updates.");
		updateControlActProcess();
	}

	private void updateControlActProcess() {
		if(response.getControlActProcess() != null) {
			PRPAIN201306UV02MFMIMT700711UV01ControlActProcess controlActProcess = response.getControlActProcess();
			if(controlActProcess != null) {
				if(controlActProcess.getCode() == null) {
					controlActProcess.setCode(new CD());
				}
				controlActProcess.getCode().setCode(CONTROL_ACT_PROCESS_CODE_CODE);
				controlActProcess.getCode().setCodeSystem(CONTROL_ACT_PROCESS_CODE_CODE_SYSTEM);
				
				if(!controlActProcess.getSubject().isEmpty()) {
					for(PRPAIN201306UV02MFMIMT700711UV01Subject1 subject : controlActProcess.getSubject()) {
						updateSubject(subject);
					}
				}
			}
		}
	}

	private void updateSubject(PRPAIN201306UV02MFMIMT700711UV01Subject1 subject) {
		if(subject != null) {
			updateRegistrationEvent(subject.getRegistrationEvent());
		}
	}

	private void updateRegistrationEvent(PRPAIN201306UV02MFMIMT700711UV01RegistrationEvent registrationEvent) {
		if(registrationEvent != null) {
			updateSubject1(registrationEvent.getSubject1());
			updateCustodian(registrationEvent.getCustodian());
		}
	}

	private void updateSubject1(PRPAIN201306UV02MFMIMT700711UV01Subject2 subject1) {
		if(subject1 != null) {
			updatePatient(subject1.getPatient());
		}
	}

	private void updatePatient(PRPAMT201310UV02Patient patient) {
		if(patient != null) {
			updatePatientStatusCode(patient);
			if(!patient.getSubjectOf1().isEmpty()) {
				for(PRPAMT201310UV02Subject subjectof1 : patient.getSubjectOf1()) {
					updateSubjectOf1(subjectof1);
				}
			}
		}
	}

	private void updatePatientStatusCode(PRPAMT201310UV02Patient patient) {
		if(patient.getStatusCode() == null) {
			patient.setStatusCode(new CS());
		}
		patient.getStatusCode().setCode(PATIENT_STATUS_CODE);
	}

	private void updateSubjectOf1(PRPAMT201310UV02Subject subjectof1) {
		if(subjectof1 != null) {
			subjectof1.setTypeCode(ParticipationTargetSubject.SBJ);
			if(subjectof1.getQueryMatchObservation() != null) {
				PRPAMT201310UV02QueryMatchObservation queryMatchObservation = subjectof1.getQueryMatchObservation();
				queryMatchObservation.getClassCode().clear();
				queryMatchObservation.getClassCode().add(PATIENT_SUBJECT_OF_QUERY_MATCH_OBS_CLASS_CODE);
			}
		}
	}

	private void updateCustodian(MFMIMT700711UV01Custodian custodian) {
		if(custodian != null) {
			if(custodian.getAssignedEntity() != null) {
				if(custodian.getAssignedEntity().getCode() == null) {
					custodian.getAssignedEntity().setCode(new CE());
				}
				custodian.getAssignedEntity().getCode().setCode(CUSTODIAN_ASSIGNED_ENTITY_CODE_CODE);
				custodian.getAssignedEntity().getCode().setCodeSystem(CUSTODIAN_ASSIGNED_ENTITY_CODE_CODE_SYSTEM);
			}
		}
		
	}

}
