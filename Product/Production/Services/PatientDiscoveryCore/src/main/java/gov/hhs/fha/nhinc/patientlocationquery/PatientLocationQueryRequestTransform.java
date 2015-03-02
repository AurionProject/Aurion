package gov.hhs.fha.nhinc.patientlocationquery;

import gov.hhs.fha.nhinc.common.auditlog.LogEventRequestType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.UserType;
import gov.hhs.fha.nhinc.transform.audit.AuditDataTransformConstants;
import gov.hhs.fha.nhinc.transform.audit.AuditDataTransformHelper;
import gov.hhs.fha.nhinc.transform.marshallers.JAXBContextHandler;
import gov.hhs.fha.nhinc.util.format.PatientIdFormatUtil;
import ihe.iti.xcpd._2009.PatientLocationQueryRequestType;

import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.services.nhinc.schema.auditmessage.AuditMessageType;
import com.services.nhinc.schema.auditmessage.CodedValueType;
import com.services.nhinc.schema.auditmessage.EventIdentificationType;
import com.services.nhinc.schema.auditmessage.ParticipantObjectIdentificationType;

/**
 * Methods for transforming the PatientLocationQueryRequest message (ITI-56)
 * into an entry for the audit repository table.
 * 
 * @author Cindy Atherton
 * 
 */
public class PatientLocationQueryRequestTransform {
	public static final String EVENT_ID_CODE = "110112";
	public static final String EVENT_ID_CODE_SYS_NAME = "DCM";
	public static final String EVENT_ID_DISPLAY_NAME = "Query";
	public static final String EVENT_TYPE_CODE = "ITI-56";
	public static final String EVENT_TYPE_CODE_SYS_NAME = "IHE Transactions";
	public static final String EVENT_TYPE_CODE_SYS_NAME_DISPNAME = "IHE Transactions";
	public static final String EVENT_TYPE_CODE_DISPNAME = "Patien tLocation Query";

	private Log log = null;
	private PatientLocationQueryRequestType request = null;
	private AssertionType assertion = null;
	private String direction = null;
	private String messageInterface = null;

	public PatientLocationQueryRequestTransform(
			PatientLocationQueryRequestType request, AssertionType assertion,
			String direction, String messageInterface) {
		log = createLogger();
		this.request = request;
		this.assertion = assertion;
		this.direction = direction;
		this.messageInterface = messageInterface;
	}

	protected Log createLogger() {
		return ((log != null) ? log : LogFactory.getLog(getClass()));
	}

	public LogEventRequestType transformToAuditMessage() {
		log.debug("Begin transformToAuditMessage()");
		LogEventRequestType auditRequest = null;

        auditRequest = new LogEventRequestType();
		AuditMessageType auditMsg = new AuditMessageType();
		auditRequest.setAuditMessage(auditMsg);
		auditRequest.setDirection(direction);
		auditRequest.setInterface(messageInterface);
		extractUserInfo(auditMsg);
		extractParticipantObjectIdentification(auditMsg);

		log.debug("End transformToAuditMessage()");
		return auditRequest;
	}

	private void extractUserInfo(AuditMessageType auditMsg) {
		UserType userInfo = null;
		if (assertion != null && assertion.getUserInfo() != null) {
			userInfo = assertion.getUserInfo();
			CodedValueType eventId = AuditDataTransformHelper.createCodedValue(
					EVENT_ID_CODE, EVENT_ID_CODE_SYS_NAME,
					EVENT_ID_CODE_SYS_NAME, EVENT_ID_DISPLAY_NAME,
					null);
			CodedValueType eventTypeCode = AuditDataTransformHelper
					.createCodedValue(EVENT_TYPE_CODE,
							EVENT_TYPE_CODE_SYS_NAME,
							EVENT_TYPE_CODE_SYS_NAME_DISPNAME,
							EVENT_TYPE_CODE_DISPNAME, null);

			EventIdentificationType eventIdentification = AuditDataTransformHelper
					.createEventIdentification(
							AuditDataTransformConstants.EVENT_ACTION_CODE_EXECUTE,
							AuditDataTransformConstants.EVENT_OUTCOME_INDICATOR_SUCCESS,
							eventId, eventTypeCode);
			auditMsg.setEventIdentification(eventIdentification);

			// Create Active Participant Section
			if (userInfo != null) {
				AuditMessageType.ActiveParticipant participant = AuditDataTransformHelper
						.createActiveParticipantForHuman(userInfo);
				auditMsg.getActiveParticipant().add(participant);
			}

			ParticipantObjectIdentificationType communityRecord = AuditDataTransformHelper
					.createParticipantCommunityRecordFromUser(userInfo);
			auditMsg.getParticipantObjectIdentification().add(communityRecord);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void extractParticipantObjectIdentification(
			AuditMessageType auditMsg) throws RuntimeException {
		String patientId = null;
		if (request != null && request.getRequestedPatientId() != null) {
			String aa = request.getRequestedPatientId().getRoot();
			String extension = request.getRequestedPatientId().getExtension();
			patientId = PatientIdFormatUtil.hl7EncodePatientId(extension, aa);
		}
		// create the Participant Object Identification object 
		ParticipantObjectIdentificationType partObjId = null;
		partObjId = AuditDataTransformHelper
				.createParticipantObjectIdentification(
						AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_PERSON,
						AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_PATIENT,
						AuditDataTransformHelper
								.getPatientParticipantRoleIdCodedValue(),
						patientId, null);
		// Fill in the message field with the contents of the event message
		try {
			JAXBContextHandler oHandler = new JAXBContextHandler();
			JAXBContext jc = oHandler.getJAXBContext("ihe.iti.xcpd._2009");
			Marshaller marshaller = jc.createMarshaller();
			ByteArrayOutputStream baOutStrm = new ByteArrayOutputStream();
			baOutStrm.reset();
			marshaller.marshal(new JAXBElement(new QName(
					"urn:ihe:iti:xcpd:2009", "Patient Location Query"),
					PatientLocationQueryRequestType.class, request), baOutStrm);
			log.debug("Done marshalling the message.");
			partObjId.setParticipantObjectQuery(baOutStrm.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		auditMsg.getParticipantObjectIdentification().add(partObjId);
	}

}