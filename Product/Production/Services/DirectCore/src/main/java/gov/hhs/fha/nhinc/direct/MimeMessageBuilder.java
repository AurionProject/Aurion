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

import gov.hhs.fha.nhinc.util.StreamUtils;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType.Document;

import java.io.IOException;
import java.io.InputStream;
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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nhindirect.xd.common.DirectDocuments;

/**
 * Builder for {@link MimeMessage}.
 */
public class MimeMessageBuilder {

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
    private AttachmentHandler attachmentHandler;

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
            throw new DirectException("Exception setting 'from' address: " + fromAddress, e);
        }

        try {
            message.addRecipients(Message.RecipientType.TO, recipients);
        } catch (Exception e) {
        	StringBuilder addressInfo = new StringBuilder();
        	
        	addressInfo.append("Exception setting recipient 'to' address(es):\n");
        	for (Address address : recipients) {
        		addressInfo.append("\tAddress: ").append(address.toString()).append("\n");				
			}      	
        	
            throw new DirectException("Exception setting recipient to address(es): " + addressInfo.toString(), e);
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
            	attachmentParts.addAll(getAttachmentHandler().createDirectAttachments(documents, messageId, recipients[0]));            	
            } else if (attachment != null && !StringUtils.isBlank(attachmentName)) {
                attachmentPart = createAttachmentFromSOAPRequest(attachment, attachmentName);               
                attachmentParts.add(attachmentPart);
            } else {
                throw new Exception(
                        "Could not create attachment. Need documents and messageId or attachment and attachmentName.");
            }
        } catch (Exception e) {
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("Error occurred in adding attachments to the Direct message. ").append(e.getMessage());
			
			LOG.error(errMsg.toString());
        	
            throw new DirectException(errMsg.toString(), e);
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
	 * Get a handle to a AttachmentHandler object.
	 * 
	 * @return
	 * 		Returns a handle to a AttachmentHandler object.
	 */
	protected AttachmentHandler getAttachmentHandler() {
		if (attachmentHandler == null)
			attachmentHandler = new AttachmentHandler();
		
		return attachmentHandler;
	}

    private MimeBodyPart createAttachmentFromSOAPRequest(Document data, String name) throws MessagingException,
            IOException {

        InputStream is = null;
        DataSource source = null;
        DataHandler dhnew = null;
        MimeBodyPart bodypart = null;

        try {
            is = data.getValue().getInputStream();
            source = new ByteArrayDataSource(is, "application/octet-stream");
            dhnew = new DataHandler(source);
            bodypart = new MimeBodyPart();

            bodypart.setDataHandler(dhnew);
            bodypart.setHeader("Content-Type", "application/octet-stream");
            bodypart.setDisposition(Part.ATTACHMENT);
            bodypart.setFileName(name);
        } finally {
            StreamUtils.closeStreamSilently(is);
        }

        return (MimeBodyPart) bodypart;
    }
}
