package gov.hhs.fha.nhinc.direct.messagevalidation.proxy;

import org.nhindirect.gateway.smtp.MessageProcessResult;

/**
 * Interface for validating a Direct "message".
 * 
 * @author Greg Gurr
 */
public interface DirectMessageValidationProxy {
		
	/**
	 * Determines if the Direct message is "valid".
	 * 
	 * @param messageProcessResult
	 * 		Contains the Direct message after it has been processed by the Direct smtp agent.
	 * @return
	 * 		Returns a "DirectMessageValidationResult" object which contains a status value and any error message that might
	 * 		have occurred.
	 */
	public DirectMessageValidationResult validateDirectMessage(MessageProcessResult messageProcessResult);
	
}
