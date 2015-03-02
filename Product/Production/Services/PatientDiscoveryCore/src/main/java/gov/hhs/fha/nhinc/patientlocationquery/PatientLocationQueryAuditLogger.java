package gov.hhs.fha.nhinc.patientlocationquery;

import gov.hhs.fha.nhinc.auditrepository.nhinc.proxy.AuditRepositoryProxy;
import gov.hhs.fha.nhinc.auditrepository.nhinc.proxy.AuditRepositoryProxyObjectFactory;
import gov.hhs.fha.nhinc.common.auditlog.LogEventRequestType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import ihe.iti.xcpd._2009.PatientLocationQueryRequestType;
import ihe.iti.xcpd._2009.PatientLocationQueryResponseType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Records the PatientLocationQueryRequest and PatientLocationQueryResponse in
 * the Audit Repository table. The ITI-56 specification defines the audit
 * parameters for the request. The response is not defined as it is optional.
 * 
 * @author Cindy Atherton
 * 
 */
public class PatientLocationQueryAuditLogger {
	private Log log = null;

	public PatientLocationQueryAuditLogger() {
		log = createLogger();
	}

	/**
	 * Create the application logger
	 * 
	 * @return Application log object
	 */
	protected Log createLogger() {
		return LogFactory.getLog(getClass());
	}

	/**
	 * Audits a PatientLocationQueryRequest message
	 * 
	 * @param request
	 *            Request message to be logged
	 * @param assertion
	 *            SAML assertion
	 * @param direction
	 *            Message direction (inbound or outbound)
	 * @param messageInterface
	 *            The interface where the message is being sent or received
	 */
	public void auditPatientLocationQueryRequest(
			PatientLocationQueryRequestType request, AssertionType assertion,
			String direction, String messageInterface) {
		log.debug("Entering auditPatientLocationQueryRequest...");
		PatientLocationQueryAuditLogTransform transform = getPatientLocationQueryAuditLogTransform();
		LogEventRequestType logEvent = transform
				.transformPatientLocationQueryRequest(request, assertion,
						direction, messageInterface);
		if (logEvent != null) {
			log.debug("Audit log request created - calling the audit log storage proxy");
			AuditRepositoryProxy proxy = getAuditRepositoryProxy();
			proxy.auditLog(logEvent, assertion);
		}

		log.debug("Leaving auditPatientLocationQueryRequest.");
	}

	/**
	 * Audit a PatientLocationQueryResponse message
	 * 
	 * @param response
	 *            Response message to be logged
	 * @param assertion
	 *            SAML assertion
	 * @param direction
	 *            Message direction (inbound or outbound)
	 * @param messageInterface
	 *            The interface where the message is being sent or received
	 */
	public void auditPatientLocationQueryResponse(
			PatientLocationQueryResponseType response, AssertionType assertion,
			String direction, String messageInterface) {
		log.debug("Entering auditPatientLocationQueryResponse...");
		PatientLocationQueryAuditLogTransform transform = getPatientLocationQueryAuditLogTransform();
		LogEventRequestType logEvent = transform.transformPatientLocationQueryResponse(response, assertion,
						direction, messageInterface);
		if (logEvent != null) {
			log.debug("Audit log request created - calling the audit log storage proxy");
			AuditRepositoryProxy proxy = getAuditRepositoryProxy();
			proxy.auditLog(logEvent, assertion);
		}
	}

	/**
	 * Create the audit repository proxy object used to store audit messages.
	 * 
	 * @return Audit repository proxy object.
	 */
	protected AuditRepositoryProxy getAuditRepositoryProxy() {
		AuditRepositoryProxyObjectFactory auditRepoFactory = new AuditRepositoryProxyObjectFactory();
		return auditRepoFactory.getAuditRepositoryProxy();
	}

	/**
	 * Create the audit repository log message transformer. This is used to
	 * transform messages into an audit log request message.
	 * 
	 * @return Audit repository log message transformer.
	 */
	protected PatientLocationQueryAuditLogTransform getPatientLocationQueryAuditLogTransform() {
		return new PatientLocationQueryAuditLogTransform();
	}
}