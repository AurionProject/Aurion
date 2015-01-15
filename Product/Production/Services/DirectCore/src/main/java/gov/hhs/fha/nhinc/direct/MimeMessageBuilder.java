/*
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above
 *       copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the United States Government nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.hhs.fha.nhinc.direct;

import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType.Document;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import gov.hhs.fha.nhinc.properties.PropertyAccessException;
import gov.hhs.fha.nhinc.properties.PropertyAccessor;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nhindirect.xd.common.DirectDocument2;
import org.nhindirect.xd.common.DirectDocuments;
import org.nhindirect.xd.transform.util.type.MimeType;

/**
 * Builder for {@link MimeMessage}.
 */
public class MimeMessageBuilder {
	private final String DIRECT_ATTACHMENT_OPTION = "direct_attachment_option";
	@SuppressWarnings("unused")
	private final String XDM_OPTION = "xdm";
	private final String XML_OPTION = "xml";
	private final String GATEWAY_PROPERTIES_FILE = "gateway";
			
	
	private static final Logger LOG = Logger.getLogger(MimeMessageBuilder.class);
    private final Session session;
    private final Address fromAddress;
    private final Address[] recipients;

    private String subject;
    private String text;
    private DirectDocuments documents;
    private String messageId;
    private Document attachment;
    private String attachmentName;

    /**
     * Construct the Mime Message builder with required fields.
     * 
     * @param session used to build the message.
     * @param fromAddress sender of the message.
     * @param recipients - list of recipients of the message.
     */
    public MimeMessageBuilder(Session session, Address fromAddress, Address[] recipients) {
        this.session = session;
        this.fromAddress = fromAddress;
        this.recipients = recipients;
    }

    /**
     * @param str for subject.
     * @return builder
     */
    public MimeMessageBuilder subject(String str) {
        this.subject = str;
        return this;
    }

    /**
     * @param str for text
     * @return builder
     */
    public MimeMessageBuilder text(String str) {
        this.text = str;
        return this;
    }

    /**
     * @param directDocuments for attachment
     * @return builder of mime messages.
     */
    public MimeMessageBuilder documents(DirectDocuments directDocuments) {
        this.documents = directDocuments;
        return this;
    }
    
    /**
     * @param str messageId for message
     * @return builder of mime messages.
     */
    public MimeMessageBuilder messageId(String str) {
        this.messageId = str;
        return this;
    }

    /**
     * @param doc for attachment
     * @return builder
     */
    public MimeMessageBuilder attachment(Document doc) {
        this.attachment = doc;
        return this;
    }

    /**
     * @param str for attachment name
     * @return builder
     */
    public MimeMessageBuilder attachmentName(String str) {
        this.attachmentName = str;
        return this;
    }

    /**
     * Build the Mime Message.
     * 
     * @return the Mime message.
     */
    public MimeMessage build() {

        final MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(fromAddress);
        } catch (Exception e) {
            throw new DirectException("Exception setting from address: " + fromAddress, e);
        }

        try {
            message.addRecipients(Message.RecipientType.TO, recipients);
        } catch (Exception e) {
            throw new DirectException("Exception setting recipient to address(es): " + recipients, e);
        }

        try {
            message.setSubject(subject);
        } catch (Exception e) {
            throw new DirectException("Exception setting subject: " + subject, e);
        }

        MimeBodyPart messagePart = new MimeBodyPart();
        try {
            messagePart.setText(text);
        } catch (Exception e) {
            throw new DirectException("Exception setting mime message part text: " + text, e);
        }

        List<MimeBodyPart> attachmentParts = new ArrayList<MimeBodyPart>();
        MimeBodyPart attachmentPart = null;
        try {
            if (documents != null && !StringUtils.isBlank(messageId)) {            	
            	addAttachments(attachmentParts, documents, messageId);
            } else if (attachment != null && !StringUtils.isBlank(attachmentName)) {
                attachmentPart = createAttachmentFromSOAPRequest(attachment, attachmentName);               
                attachmentParts.add(attachmentPart);
            } else {
                throw new Exception(
                        "Could not create attachment. Need documents and messageId or attachment and attachmentName.");
            }
        } catch (Exception e) {
            throw new DirectException("Exception creating attachment: " + attachmentName, e);
        }

        Multipart multipart = new MimeMultipart();
        try {
            multipart.addBodyPart(messagePart);
            
			if (!attachmentParts.isEmpty()) {
				for (MimeBodyPart attPart : attachmentParts) {
					multipart.addBodyPart(attPart);
				}
			}            
            
            message.setContent(multipart);
        } catch (Exception e) {
            throw new DirectException("Exception creating multi-part attachment.", e);
        }

        try {
            message.saveChanges();
        } catch (Exception e) {
            throw new DirectException("Exception saving changes.", e);
        }

        return message;
    }

    
    
    /**
     * Add attachments to the passed in "MimeBodyPart" list. These attachments can be XDM or XML.
     * 
     * @param attachmentParts
     * 		Contains the existing "MimeBodyPart" list for which to add other attachments. Upon return this list
     * 		will be updated with new items being added.
     * @param documents
     * 		Contains the Direct documents to attach.
     * @param messageId
     * 		Contains the Direct message "id".
     * @throws Exception 
     */
	private void addAttachments(List<MimeBodyPart> attachmentParts,
			DirectDocuments documents, String messageId) throws Exception {

		LOG.debug("Begin MimeMessageBuilder.addAttachments");
		
		String directAttachmentOption = getDirectAttachmentOption();			

		if (XML_OPTION.equalsIgnoreCase(directAttachmentOption)) {

			for (DirectDocument2 document : documents.getDocuments()) {
				if (document.getData() != null) {
					
				    File xmlFile = null;					
					MimeBodyPart attachmentPart = getMimeBodyPart();
					String fileName = document.getMetadata().getId();
					
					fileName = fileName.replace("urn:uuid:", "");
					fileName = fileName + getSuffix(document.getMetadata().getMimeType());
						
					LOG.debug("Direct: Processing attachment fileName: '" + fileName + "'");

					BufferedOutputStream bufferedOutput = null;					
					
					try {
						xmlFile = new File(fileName);
						
						LOG.debug("Direct: Writing xml attachment to fileOutputStream");
						bufferedOutput = new BufferedOutputStream(new FileOutputStream(xmlFile));
						bufferedOutput.write(document.getData());
						bufferedOutput.flush();
						
				        attachmentPart.attachFile(xmlFile);	
						attachmentParts.add(attachmentPart);				
						
					} catch (Exception e) {
						e.printStackTrace();
						
						String errMessage = "Direct: Error occurred writing xml attachment to bufferedOutputStream. " + e.getMessage();
						LOG.error(errMessage);
						
						throw new Exception(errMessage, e);
					} finally {
						if (bufferedOutput != null) {
							bufferedOutput.close();
						}
					}	

					LOG.debug("Direct: Successfully added XML attachment to MimeBodyPart list");
				}
			}
		} else {
			// Default to "xdm" attachment option			
			String formattedMessageId = formatMessageIdForXDMAttachmentName(messageId);
			
			MimeBodyPart attachmentPart = getMimeBodyPart();
			attachmentPart.attachFile(documents.toXdmPackage(formattedMessageId).toFile());					
			attachmentParts.add(attachmentPart);
		}
		
		LOG.debug("End MimeMessageBuilder.addAttachments");
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
    	
    	LOG.debug("MimeMessageBuilder.formatMessageIdForXDMAttachmentName - Passed in value: '" + messageId + "'");
    	
    	if (NullChecker.isNotNullish(messageId)) {
    		formattedMessageId = messageId.replace(NhincConstants.WS_SOAP_HEADER_MESSAGE_ID_PREFIX, "");	
    		
    		if (formattedMessageId.startsWith("<")) {
    			formattedMessageId = formattedMessageId.substring(1);
			}
    		
    		if (formattedMessageId.endsWith(">")) {
    			formattedMessageId = formattedMessageId.substring(0, formattedMessageId.length() - 1);
			}
		}
    	
    	LOG.debug("MimeMessageBuilder.formatMessageIdForXDMAttachmentName - Return value: '" + formattedMessageId + "'");
     	
		return formattedMessageId;
	}

	/**
     * Get the mime type suffix.
     * 
     * @param mimeType
     * 		Contains the mime type for which to get the suffix.
     * @return
     * 		Returns a String of the mime suffix.
     */
    private String getSuffix(String mimeType) {
        return "." + MimeType.lookup(mimeType).getSuffix();
    }
    
    
	/**
	 * Get the direct attachment option from a properties file.
	 * 
	 * @return
	 * 		Returns the direct attachment option.
	 */
	private String getDirectAttachmentOption() {
		String directAttachmentOption = "";
		
		try {
			directAttachmentOption = PropertyAccessor.getInstance(GATEWAY_PROPERTIES_FILE).getProperty(DIRECT_ATTACHMENT_OPTION);
		} catch (PropertyAccessException e) {
			e.printStackTrace();
			LOG.error("Error occured in retrieving the direct attachment option. Defaulting to 'xdm'.");
			
			directAttachmentOption = "xdm";
		}
		
		LOG.debug("Direct: Direct attachment option is: '" + directAttachmentOption + "'");	
		
		return directAttachmentOption;
	}

	/**
     * @return mime body part of the message.
     */
    protected MimeBodyPart getMimeBodyPart() {
        return new MimeBodyPart();
    }

    private MimeBodyPart createAttachmentFromSOAPRequest(Document data, String name) throws MessagingException,
            IOException {
        DataSource source = new ByteArrayDataSource(data.getValue().getInputStream(), "application/octet-stream");
        DataHandler dhnew = new DataHandler(source);
        MimeBodyPart bodypart = new MimeBodyPart();
        bodypart.setDataHandler(dhnew);
        bodypart.setHeader("Content-Type", "application/octet-stream");
        bodypart.setDisposition(Part.ATTACHMENT);
        bodypart.setFileName(name);
        return (MimeBodyPart) bodypart;
    }

}
