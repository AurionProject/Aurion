package gov.hhs.fha.nhinc.direct.messagevalidation.proxy;


/**
 * Contains the results of the Direct message validation.
 * 
 * @author Greg Gurr
 *
 */
public class DirectMessageValidationResult {
	private DirectMessageValidationStatus status;
	private String errorMessage;
	
	public DirectMessageValidationStatus getStatus() {
		return status;
	}
	
	public void setStatus(DirectMessageValidationStatus status) {
		this.status = status;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
