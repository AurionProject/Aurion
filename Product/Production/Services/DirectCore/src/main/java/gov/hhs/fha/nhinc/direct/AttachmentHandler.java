package gov.hhs.fha.nhinc.direct;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.internet.MimeBodyPart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.log4j.Logger;
import org.nhindirect.xd.common.DirectDocument2;
import org.nhindirect.xd.common.DirectDocuments;

import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.properties.PropertyAccessException;
import gov.hhs.fha.nhinc.properties.PropertyAccessor;

/**
 * Class for handling attaching items to a outbound message.
 * 
 * @author Greg Gurr
 */
public class AttachmentHandler {
	private static final String DIRECT_ATTACHMENT_DIRECTORY = "directAttachments";
	private static final String DIRECT_ATTACHMENT_OPTION = "direct_attachment_option";
	private static final String DOMAINS_WANTING_DIRECT_ATTACHMENT_AS_ZIP = "domains_wanting_direct_attachment_as_zip";
	private static final String PERSIST_DIRECT_ATTACHMENTS_TO_FILE = "persist_direct_attachments_to_file";
	private static final String GATEWAY_PROPERTIES_FILE = "gateway";	
	
	private static final Logger LOG = Logger.getLogger(AttachmentHandler.class);


	/**
	 * Enum for the types of supported Direct attachments 
	 */
	public static enum DirectAttachmentOption {
		XML("xml"),
		XDM("xdm"),
		ZIP("zip");
		
		private String description;
		
		DirectAttachmentOption(String description) {
			this.description = description;
		}
		
		public String getDescription() {
			return description;
		}
		
		public static DirectAttachmentOption fromString(String text) {
			for (DirectAttachmentOption enumItem : DirectAttachmentOption.values()) {
				if (enumItem.description.equalsIgnoreCase(text)) {
					return enumItem;
				}
			}
			
			return null;
		}			
	}
	
	/**
	 * Determines if the Direct attachments are to be persisted to the file system.
	 * 
	 * @return
	 * 		Returns "true" if the Direct attachments should be persisted to the file system, "false" otherwise.
	 */
	protected boolean persistDirectAttachmentsToFile() {
		String propertyFileValue = "";
		
		try {
			propertyFileValue = PropertyAccessor.getInstance(GATEWAY_PROPERTIES_FILE).getProperty(PERSIST_DIRECT_ATTACHMENTS_TO_FILE);
		} catch (PropertyAccessException e) {
			e.printStackTrace();
			LOG.warn("Error occured in retrieving the property: '" + PERSIST_DIRECT_ATTACHMENTS_TO_FILE + "'. Defaulting to 'no'.");
			
			propertyFileValue = "no";
		}
		
		if ("yes".equalsIgnoreCase(propertyFileValue))
			return true;
		else
			return false;
	}	
	
	/**
	 * Get the default Direct attachment option from a properties file.
	 * 
	 * @return
	 * 		Returns a DirectAttachmentOption enum for the direct attachment option.
	 */
	protected DirectAttachmentOption getDefaultDirectAttachmentOption() {
		String propertyFileValue = "";
		StringBuilder buf = new StringBuilder();
		
		try {
			propertyFileValue = PropertyAccessor.getInstance(GATEWAY_PROPERTIES_FILE).getProperty(DIRECT_ATTACHMENT_OPTION);
		} catch (PropertyAccessException e) {
			e.printStackTrace();
			LOG.warn("Error occured in retrieving the property: '" + DIRECT_ATTACHMENT_OPTION + "'. Defaulting to 'xml'.");
			
			propertyFileValue = "xml";
		}
		
		DirectAttachmentOption retVal = DirectAttachmentOption.fromString(propertyFileValue);
		
		if (retVal == null) {
			buf = new StringBuilder();
			buf.append("\nUnsupported Direct attachment option for property value: '").append(propertyFileValue).append("'. ");
			buf.append("Defaulting to XML as the default attachment option.");
			
			LOG.warn(buf.toString());
			
			retVal = DirectAttachmentOption.XML;
		}
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("\nDefault Direct attachment option is: '" + retVal + "'");
		}
		
		return retVal;
	}	
	
	/**
	 * Determines if the recipient "domains" wants ZIP attachments for their Direct attachment option.
	 * 
	 * @param recipientDomain
	 * 		Contains the "domain" of the recipients email address.
	 * @return
	 * 		Returns true if the domain wants ZIP attachments, false otherwise.
	 */
	protected boolean doesRecipientDomainWantZipAttachments(String recipientDomain) {
		String zipAttachmentDomains = null;
		
		if (NullChecker.isNotNullish(recipientDomain)) {
			try {
				zipAttachmentDomains = PropertyAccessor.getInstance(GATEWAY_PROPERTIES_FILE).getProperty(DOMAINS_WANTING_DIRECT_ATTACHMENT_AS_ZIP);
			} catch (PropertyAccessException e) {
				e.printStackTrace();
				LOG.warn("\nError occured in retrieving the property: '" + DOMAINS_WANTING_DIRECT_ATTACHMENT_AS_ZIP + "'. Defaulting to FALSE.");
				
				return false;
			}		
			
			if (NullChecker.isNotNullish(zipAttachmentDomains)) {
				// The value from the properties file is a comma separated list of "domains"
				String[] domainsWantingZip = zipAttachmentDomains.split(",");
				
				for (int i = 0; i < domainsWantingZip.length; i++) {
					if (recipientDomain.equalsIgnoreCase(domainsWantingZip[i])) {
						LOG.info("\nEmail domain: '" + recipientDomain + "' wants to receive Direct attachments as ZIP files");
						
						return true;
					}
				}
			}
			
			return false;
		} else {
			return false;
		}
	}		
	
    /**
     *  Determines the absolute file path where the passed in attachment file will be stored.
     * 
     * @return String
     *      Returns the absolute path of where the attachment file will be stored.
     */
    protected String getAbsoluteAttachmentFilePath(String attachmentFileName) {
        String fileAbsolutePath = "";
        
        File attachmentDirectory = new File(PropertyAccessor.getInstance().getPropertyFileLocation() + DIRECT_ATTACHMENT_DIRECTORY);

        if (!attachmentDirectory.exists()) {
        	attachmentDirectory.mkdir();
		}
         
        fileAbsolutePath = attachmentDirectory.getAbsolutePath() + File.separator + attachmentFileName;

        return fileAbsolutePath;
    }   
	
	/**
	 * Generate the XDM file to attache to the Direct message.
	 * 
	 * @param directDocuments
	 * 		Contains the documents to include in the XDM file.
	 * @param formattedMessageId
	 * 		Contains a formatted message id.
	 * @return
	 * 		Returns a File object which represents the XDM file to attach to the Direct message.
	 */
	protected File generateXdmPackageFile(DirectDocuments directDocuments, String formattedMessageId) {		
		return directDocuments.toXdmPackage(getAbsoluteAttachmentFilePath(formattedMessageId)).toFile();
	}	
	
	/**
	 * Get the the attachment file object.
	 * 
	 * @param fileName
	 * 		Contains the name of the file.
	 * @return
	 * 		Returns a File object.
	 */
	protected File getAttachmentFile(String fileName) {
		return new File(fileName);
	}
	
	/**
	 * Get an instance of a BufferedOutputStream object.
	 * 
	 * @param fos
	 * 		Contains a FileOutputStream object.
	 * @return
	 * 		Returns a handle to a BufferedOutputStream object.
	 */
	protected BufferedOutputStream getBufferedOutputStream(FileOutputStream fos) {		
		return new BufferedOutputStream(fos);
	}	
	
	/**
	 * Get an instance of a FileOutputStream object.
	 * 
	 * @param attachmentFile
	 * 		Contains the file for which to get a FileOutputStream.
	 * @return
	 * 		Returns a handle to a FileOutputStream object.
	 * @throws FileNotFoundException
	 */
	protected FileOutputStream getFileOutputStream(File attachmentFile) throws FileNotFoundException {
		return new FileOutputStream(attachmentFile);		
	}
	
	/**
	 * Creates the Direct attachments that will be added to the Direct message.
	 * 
	 * @param documents
	 *		Contains the documents to add as attachments.
	 * @param messageId
	 * 		Contains the "id" of the message.
	 * @param recipient
	 * 		Contains the email address of the recipient. This is optional. The "domain" of the recipient is used to 
	 * 		determine if the recipient wants the attachment as a "zip" file or not. If this parameter is not passed
	 * 		in, then the default Direct attachment option will be used.
	 * @return
	 * 		Returns a list of MimeBodyPart objects which represent the attachments to be added to the Direct message.
	 * @throws Exception 
	 */
	public List<MimeBodyPart> createDirectAttachments(DirectDocuments directDocuments, String messageId,
			Address recipient) throws Exception {
		
		List<MimeBodyPart> attachmentParts = new ArrayList<MimeBodyPart>();
		
		validateParams(directDocuments, messageId);		
		DirectAttachmentOption attachmentOption = getDirectAttachmentOption(recipient);
				
		if (attachmentOption.equals(DirectAttachmentOption.XML) ||
				attachmentOption.equals(DirectAttachmentOption.ZIP)) {
			
			for (DirectDocument2 document2 : directDocuments.getDocuments()) {
				if (document2.getData() != null) {
					attachmentParts.add(createAttachmentPart(document2, attachmentOption));
				}
			}
		} else if (attachmentOption.equals(DirectAttachmentOption.XDM)) {
			String formattedMessageId = formatMessageIdForXDMAttachmentName(messageId);						
			File xdmPackageFile = generateXdmPackageFile(directDocuments, formattedMessageId);	
			MimeBodyPart attachmentPart = new MimeBodyPart();
			
			attachmentPart.attachFile(xdmPackageFile);		
			attachmentParts.add(attachmentPart);	
			
			logXdmAttachment(xdmPackageFile);
		} 	
		
		return attachmentParts;
	}

    /**
     * Log information about the Direct attachment.
     * 
     * @param attachmentFile
     * 		Contains the Direct attachment file.
     */
    private void logXdmAttachment(File attachmentFile) {
    	if (attachmentFile != null) {
            LOG.info("\nAdded Direct XDM attachment: '" + attachmentFile.getAbsolutePath() + "' to the message");			
		} 
	}

	/**
     * Creates the Direct message attachment part.
     * 
     * @param document2
     * 		Contains the Direct document for which to create an attachment part.
     * @param attachmentOption
     * 		Contains an enum for the attachment option (i.e. .xml, .zip, etc.)
     * @return
     * 		Returns a MimeBodyPart object for the attachment item.
     */
    private MimeBodyPart createAttachmentPart(DirectDocument2 document2, DirectAttachmentOption attachmentOption) {  	    	
    	MimeBodyPart attachmentPart = new MimeBodyPart();    	
    	String fileName = formatAttachmentFileName(document2, attachmentOption);
    	BufferedOutputStream bufferedOutput = null;
    	
		try {
			String attachmentMimeType = null;
			
			if (DirectAttachmentOption.XML.equals(attachmentOption)) {
				attachmentMimeType = "application/xml";
			} else if(DirectAttachmentOption.ZIP.equals(attachmentOption)) {
				attachmentMimeType = "application/zip";
			}
			
			DataSource ds = new ByteArrayDataSource(document2.getData(), attachmentMimeType);
			attachmentPart.setDataHandler(new DataHandler(ds));
			attachmentPart.setFileName(fileName);
			
			// NOTE: The header added below is needed in order for the "signature" validation to succeed when sending Direct 
			//       messages with XML attachments to the NIST TTT tool. It MUST also come AFTER the xml file has been 
	        //		 attached by the code above this line.
			//---------------------------------------------------------------------------------------------------------------
			attachmentPart.setHeader("Content-Transfer-Encoding", "base64");
			attachmentPart.setHeader("Content-Type", attachmentMimeType);	
			
			LOG.info("\nAttached Direct message: '" + fileName + "'");			
			
			if (persistDirectAttachmentsToFile()) {
				File attachmentFile = getAttachmentFile(getAbsoluteAttachmentFilePath(fileName));
				FileOutputStream fos = getFileOutputStream(attachmentFile);
				
				bufferedOutput = getBufferedOutputStream(fos);
				bufferedOutput.write(document2.getData());
				bufferedOutput.flush();
		        
		        LOG.info("\nPersisted Direct attachment to file system: '" + attachmentFile + "'");
			}		
		} catch (Exception e) {
			e.printStackTrace();
			
			String errMessage = "Direct: Error occurred writing xml/zip attachment to bufferedOutputStream. " + e.getMessage();
			LOG.error(errMessage);
						
			throw new DirectException(errMessage, e);
		} finally {
			if (bufferedOutput != null) {
				try {
					bufferedOutput.close();				
				} catch (IOException e) {
					StringBuilder errMsg = new StringBuilder();
					errMsg.append("\nError occurred in attempting to close the buffered output stream for the Direct attachment file. ");
					errMsg.append(e.getMessage());
					
					LOG.warn(errMsg.toString());
				}
			}						
		}
    	
		return attachmentPart;
	}

	/**
     * Formats the Direct attachment file name.
     * 
     * @param document2
     * 		Contains the Direct document to attach.
     * @param attachmentOption
     * 		Contains a DirectAttachmentOption object signifying the attachment "suffix".
     * @return
     * 		Returns the absolute path for the file name for the Direct attachment.
     */
    private String formatAttachmentFileName(DirectDocument2 document2, DirectAttachmentOption attachmentOption) {
		String formattedFileName = document2.getMetadata().getId();	
		
		formattedFileName = formattedFileName.replace("urn:uuid:", "");
		
		if (!formattedFileName.endsWith("." + attachmentOption.getDescription())) {
			formattedFileName = formattedFileName + "." + attachmentOption.getDescription();
		}
		
		return formattedFileName;
	}

	/**
     * Format the "messageId" to be used to name the XDM attachment.
     * 
     * @param messageId
     * 		Contains a "messageId" to format.
     * @return
     * 		Returns a formatted "messageId".
     */
    private String formatMessageIdForXDMAttachmentName(String messageId) {
    	String formattedMessageId = messageId;
    	
    	if (NullChecker.isNotNullish(messageId)) {
    		formattedMessageId = messageId.replace(NhincConstants.WS_SOAP_HEADER_MESSAGE_ID_PREFIX, "");	
    		
    		if (formattedMessageId.startsWith("<")) {
    			formattedMessageId = formattedMessageId.substring(1);
			}
    		
    		if (formattedMessageId.endsWith(">")) {
    			formattedMessageId = formattedMessageId.substring(0, formattedMessageId.length() - 1);
			}
		}
     	
		return formattedMessageId;
	}

	/**
	 * Determines the Direct attachment "type" that is needed.
	 * 
	 * @param recipient
	 * 		Contains the email address of the recipient.
	 * @return
	 * 		Returns a DirectAttachmentOption enum signifying the Direct attachment type.
	 */
	private DirectAttachmentOption getDirectAttachmentOption(Address recipient) {
		String recipientDomain = extractRecipientDomain(recipient);
		
		if (doesRecipientDomainWantZipAttachments(recipientDomain)) {			
			return DirectAttachmentOption.ZIP;
		} else {
			return getDefaultDirectAttachmentOption();
		}
	}

	/**
	 * Parse out the email "domain" of the Direct recipient.
	 * 
	 * @param recipient	
	 * 		Contains the email address of the recipient.
	 * @return
	 * 		Returns the "domain" portion of the email, i.e. the part 
	 * 		after the "@" character.
	 */
	private String extractRecipientDomain(Address recipient) {
		String domain = null;
		
		if (recipient != null) {
			int emailAtIndex = recipient.toString().indexOf("@");
			
			if (emailAtIndex != -1) {
				domain = recipient.toString().substring(emailAtIndex + 1);
			}
		}

		return domain;
	}

	/**
	 * Validates parameters passed to the addAttachment method.
	 * 
	 * @param directDocuments
	 *		Contains the documents to add as attachments.
	 * @param messageId
	 * 		Contains the "id" of the message.
	 */
	private void validateParams(DirectDocuments directDocuments, String messageId) {
		if (directDocuments == null) {
			throw new IllegalArgumentException("Invalid param 'directDocuments' passed to 'addAttachments'. This param cannot be null.");			
		}
		
		if (NullChecker.isNullish(directDocuments.getDocuments())) {
			throw new IllegalArgumentException("Invalid param 'directDocuments.documents' passed to 'addAttachments'. This param cannot be null or blank.");			
		}		
		
		if (NullChecker.isNullish(messageId)) {
			throw new IllegalArgumentException("Invalid param 'messageId' passed to 'addAttachments'. This param cannot be null or blank.");						
		}
	}


}
