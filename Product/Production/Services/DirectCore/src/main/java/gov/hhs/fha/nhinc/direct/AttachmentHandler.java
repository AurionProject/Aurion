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

import org.apache.commons.lang.StringUtils;
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
	private static final String DIRECT_ATTACHMENT_FILE_PREFIX = "OUTBOUND-DIRECT-";
	private static final String PERSIST_DIRECT_ATTACHMENTS_TO_FILE = "persist_direct_attachments_to_file";
	private static final String GATEWAY_PROPERTIES_FILE = "gateway";	
	
	private static final Logger LOG = Logger.getLogger(AttachmentHandler.class);


	/**
	 * Enum for the types of supported Direct attachments 
	 */
	public static enum DirectMimeType {
		TEXT_PLAIN("text/plain", "txt"), 
		TEXT_XML("text/xml", "xml"), 
		TEXT_HTML("text/html", "html"), 
		TEXT_CDA_XML("text/cda+xml", "xml"), 
		APPLICATION_CCR("application/ccr", "xml"), 
		APPLICATION_XML("application/xml", "xml"), 
		APPLICATION_PDF("application/pdf", "pdf"), 
		APPLICATION_ZIP("application/zip", "zip"), 
		MDN("message/disposition-notification", "txt"),
		UNKNOWN("application/octet-stream", "zip");

		private String type;
		private String suffix;

		/**
		 * Enumeration constructor.
		 * 
		 * @param type
		 *            Contains the MIME type.
		 * @param suffix
		 *            Contains the file suffix.
		 */
		private DirectMimeType(String type, String suffix) {
			this.type = type;
			this.suffix = suffix;
		}

		/**
		 * Determine if the input matches or contains the current element by first comparing
		 * equalsIgnoreCase and then comparing startsWith.
		 * 
		 * @param type
		 *            Contains the MIME type to compare.
		 * @return true if the string is a reasonable match, false otherwise.
		 */
		public boolean matches(String type) {
			if (StringUtils.containsIgnoreCase(type, this.type))
				return true;		
			
			return false;
		}

		/**
		 * Lookup a MimeType enumeration by type.
		 * 
		 * @param type
		 *            The type to use for lookup.
		 * @return the matching MimeType or UNKNOWN if not found.
		 */
		public static DirectMimeType lookup(String type) {
			for (DirectMimeType m : values()) {
				if (m.matches(type))
					return m;
			}

			return UNKNOWN;
		}

		/**
		 * Return the type.
		 * 
		 * @return the type.
		 */
		public String getType() {
			return type;
		}

		/**
		 * Return the suffix.
		 * 
		 * @return the suffix.
		 */
		public String getSuffix() {
			return suffix;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return type;
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
		
		for (DirectDocument2 document2 : directDocuments.getDocuments()) {
			if (document2.getData() != null) {					
				attachmentParts.add(createAttachmentPart(document2));
			}
		}		
		
		// We are not sure how we are going to flag attachments of type XDM. Keep this commented out code for now
		// until we design how to handle this.
/*		String formattedMessageId = formatMessageIdForXDMAttachmentName(messageId);						
		File xdmPackageFile = generateXdmPackageFile(directDocuments, formattedMessageId);	
		MimeBodyPart attachmentPart = new MimeBodyPart();
		
		attachmentPart.attachFile(xdmPackageFile);		
		attachmentParts.add(attachmentPart);*/		
		
		return attachmentParts;
	}
    
	/**
     * Creates the Direct message attachment part.
     * 
     * @param document2
     * 		Contains the Direct document for which to create an attachment part.
     * @return
     * 		Returns a MimeBodyPart object for the attachment item.
     */
    private MimeBodyPart createAttachmentPart(DirectDocument2 document2) {  	    	
    	MimeBodyPart attachmentPart = new MimeBodyPart(); 
    	DirectMimeType directMimeType = getDirectMimeType(document2);
    	String fileName = formatAttachmentFileName(document2, directMimeType);
    	BufferedOutputStream bufferedOutput = null;
    	
		try {
			LOG.info("\nMime-type of Direct document attachment: '" + directMimeType.getType() + "'\n");
			
			DataSource ds = new ByteArrayDataSource(document2.getData(), directMimeType.getType());
			attachmentPart.setDataHandler(new DataHandler(ds));
			attachmentPart.setFileName(fileName);
			
			// NOTE: The header added below is needed in order for the "signature" validation to succeed when sending Direct 
			//       messages with XML attachments to the NIST TTT tool. It MUST also come AFTER the xml file has been 
	        //		 attached by the code above this line.
			//---------------------------------------------------------------------------------------------------------------
			attachmentPart.setHeader("Content-Transfer-Encoding", "base64");
			attachmentPart.setHeader("Content-Type", directMimeType.getType());	
			
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
			
			String errMessage = "Direct: Error occurred writing Direct attachment to bufferedOutputStream. " + e.getMessage();
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
	 * Looks up the "DirectMimeType" for the passed in "DirectDocument2" object.
	 * 
	 * @param document2
	 * 		Contains the "DirectDocument2" object for which to look up the "DirectMimeType".
	 * @return
	 * 		Returns a DirectMimeType enum.
	 */
	private DirectMimeType getDirectMimeType(DirectDocument2 document2) {		
		if (document2.getMetadata() != null && 
				document2.getMetadata().getMimeType() != null) 
			return DirectMimeType.lookup(document2.getMetadata().getMimeType());
		else 
			return DirectMimeType.UNKNOWN;
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
    private String formatAttachmentFileName(DirectDocument2 document2, DirectMimeType directMimeType) {
		String formattedFileName = DIRECT_ATTACHMENT_FILE_PREFIX + document2.getMetadata().getId();	
		
		formattedFileName = formattedFileName.replace("urn:uuid:", "");
		
		if (!formattedFileName.endsWith("." + directMimeType.getSuffix())) {
			formattedFileName = formattedFileName + "." + directMimeType.getSuffix();
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
