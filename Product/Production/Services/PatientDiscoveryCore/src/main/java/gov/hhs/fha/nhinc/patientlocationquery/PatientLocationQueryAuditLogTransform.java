package gov.hhs.fha.nhinc.patientlocationquery;

import gov.hhs.fha.nhinc.common.auditlog.LogEventRequestType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import ihe.iti.xcpd._2009.PatientLocationQueryRequestType;
import ihe.iti.xcpd._2009.PatientLocationQueryResponseType;

/**
 * Provides methods for calling the PatientLocationQueryRequest/Response Transform classes.
 * 
 * @author Cindy Atherton
 *
 */
public class PatientLocationQueryAuditLogTransform {

	/**
	 * Transform an PatientLocationQueryRequest message into an log request message
	 * 
	 * @param request
	 *            PatientLocationQueryRequestType message
	 * @param assertion
	 *            SAML assertion
	 * @param direction
	 *            Message direction (inbound or outbound)
	 * @param messageInterface
	 *            Message interface (NHIN,Entity, Adapter)
	 * @return Audit log request message
	 */
	public LogEventRequestType transformPatientLocationQueryRequest(
			PatientLocationQueryRequestType request, AssertionType assertion,
			String direction, String messageInterface) {
		LogEventRequestType auditRequest = null;
		PatientLocationQueryRequestTransform transform = getPatientLocationQueryRequestTransform(
				request, assertion, direction, messageInterface);
		auditRequest = transform.transformToAuditMessage();
		return auditRequest;
	}
	
	/**
	 * Create the audit transform object for PatientLocationQueryRequest message
	 * 
	 * @param request
	 *            PatientLocationQueryRequestType message
	 * @param assertion
	 *            SAML assertion
	 * @param direction
	 *            Message direction (inbound or outbound)
	 * @param messageInterface
	 *            Message interface (NHIN, Entity, Adapter)
	 * @return a auditTransform object 
	 */
	protected PatientLocationQueryRequestTransform getPatientLocationQueryRequestTransform(
			PatientLocationQueryRequestType request, AssertionType assertion,
			String direction, String messageInterface) {
		return new PatientLocationQueryRequestTransform(request, assertion,
				direction, messageInterface);
	}

	/**
	 * Transform an PatientLocationQueryResponse response message into an log request message
	 * 
	 * @param response
	 *            PatientLocationQueryResponseType message
	 * @param assertion
	 *            SAML assertion
	 * @param direction
	 *            Message direction (inbound or outbound)
	 * @param messageInterface
	 *            (NHIN, Entity, Adapter)
	 * @return Audit log request message
	 */
	public LogEventRequestType transformPatientLocationQueryResponse(
			PatientLocationQueryResponseType response, AssertionType assertion,
			String direction, String messageInterface) {
		LogEventRequestType auditRequest = null;
		PatientLocationQueryResponseTransform transform = getPatientLocationQueryResponseTransform(
				response, assertion, direction, messageInterface);
		auditRequest = transform.transformToAuditMessage();
		return auditRequest;
	}
	
	/**
	 * Create the audit transform object for PatientLocationQueryResponse message
	 * 
	 * @param response
	 *            PatientLocationQueryResponseType message
	 * @param assertion
	 *            SAML assertion
	 * @param direction
	 *            Message direction (inbound or outbound)
	 * @param messageInterface
	 *            (NHIN, Entity, Adapter)
	 * @return a auditTransform object 
	 */
	protected PatientLocationQueryResponseTransform getPatientLocationQueryResponseTransform(
			PatientLocationQueryResponseType response, AssertionType assertion,
			String direction, String messageInterface) {
		return new PatientLocationQueryResponseTransform(response, assertion,
				direction, messageInterface);
	}
}