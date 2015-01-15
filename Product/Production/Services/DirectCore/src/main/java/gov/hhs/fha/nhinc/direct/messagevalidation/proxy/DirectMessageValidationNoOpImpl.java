package gov.hhs.fha.nhinc.direct.messagevalidation.proxy;

import org.nhindirect.gateway.smtp.MessageProcessResult;

/**
 * No-op implementation for the "DirectPatientValidationProxy" interface.
 * 
 * @author Greg Gurr
 *
 */
public class DirectMessageValidationNoOpImpl implements DirectMessageValidationProxy {
	
	/* (non-Javadoc)
	 * @see gov.hhs.fha.nhinc.direct.messagevalidation.proxy.DirectMessageValidationProxy#isDirectMessageValid(org.nhindirect.gateway.smtp.MessageProcessResult)
	 */
	public DirectMessageValidationResult validateDirectMessage(MessageProcessResult messageProcessResult) {
		DirectMessageValidationResult result = new DirectMessageValidationResult();
		
		result.setStatus(DirectMessageValidationStatus.SUCCESS);
		result.setErrorMessage(null);
		
		return result;
	}		

}
