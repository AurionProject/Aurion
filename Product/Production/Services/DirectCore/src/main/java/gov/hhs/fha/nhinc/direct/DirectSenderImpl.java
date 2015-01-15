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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import gov.hhs.fha.nhinc.direct.event.DirectEventLogger;
import gov.hhs.fha.nhinc.direct.event.DirectEventType;
import gov.hhs.fha.nhinc.mail.MailSender;
import gov.hhs.fha.nhinc.properties.PropertyAccessException;
import gov.hhs.fha.nhinc.properties.PropertyAccessor;

import javax.mail.Address;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.nhindirect.gateway.smtp.SmtpAgent;
import org.nhindirect.xd.common.DirectDocuments;

/**
 * Used to send outbound direct messages.
 */
public class DirectSenderImpl extends DirectAdapter implements DirectSender {
	
	private static final String OVERRIDE_MIME_MESSAGE_ID = "override_mime_message_id";
	private static final String GATEWAY_PROPERTIES_FILE = "gateway";
	private static final String DATE_HEADER_FIELD = "Date";

    private static final Logger LOG = Logger.getLogger(DirectSenderImpl.class);

    private static final String MSG_SUBJECT = "DIRECT Message";
    private static final String MSG_TEXT = "DIRECT Message body text";
    
    /**
     * @param externalMailSender used to send messages.
     * @param smtpAgent used to process direct messages.
     * @param directEventLogger used to log direct events.
     */
    public DirectSenderImpl(MailSender externalMailSender, SmtpAgent smtpAgent, DirectEventLogger directEventLogger) {
        super(externalMailSender, smtpAgent, directEventLogger);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendOutboundDirect(MimeMessage message) {
        getDirectEventLogger().log(DirectEventType.BEGIN_OUTBOUND_DIRECT, message);
        try {        	
        	ensureOrigDateHeaderExistsInMimeMessage(message);
            MimeMessage processedMessage = process(message).getProcessedMessage().getMessage();
            getExternalMailSender().send(message.getAllRecipients(), processedMessage);
        } catch (Exception e) {
            throw new DirectException("Exception sending outbound direct.", e, message);
        }
        getDirectEventLogger().log(DirectEventType.END_OUTBOUND_DIRECT, message);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendOutboundDirect(Address sender, Address[] recipients, DirectDocuments documents, String messageId) {
        MimeMessage message = null;
        try {
            message = new MimeMessageBuilder(getExternalMailSender().getMailSession(), sender, recipients)
                    .subject(MSG_SUBJECT).text(MSG_TEXT).documents(documents).messageId(messageId).build();
            
            LOG.debug("\nDirectSenderImpl - Created MimeMessage - System generated Message-ID: '" + message.getMessageID() + "'");   
            
            if (getOverrideMimeMessageIdOption()) {            	
                // Override the generated header value for "Message-ID". This is needed so that a sending system can "link" the Direct 
                // message that is sent out with the corresponding MDN message that comes back in at a later time.
                //---------------------------------------------------------------------------------------------------------------------
                message.setHeader("Message-ID", messageId);    
                
                LOG.debug("\nDirectSenderImpl - Overriding header 'Message-ID' to value: '" + message.getMessageID() + "'"); 
			}
            
            sendOutboundDirect(message);
         } catch (Exception e) {
            throw new DirectException("Error building and sending mime message.", e, message);
        }
    }   
    
    
	/**
	 * Gets the override mime message id option from a properties file.
	 * 
	 * @return
	 * 		Returns a boolean representing the value for the override mime message id option.
	 */
	protected boolean getOverrideMimeMessageIdOption() {
		boolean overrideMimeMessageIdFlag = false;
		
		try {
			String overrideMimeMessageIdOption = PropertyAccessor.getInstance(GATEWAY_PROPERTIES_FILE).getProperty(OVERRIDE_MIME_MESSAGE_ID);
			
			if ("true".equalsIgnoreCase(overrideMimeMessageIdOption)) {
				overrideMimeMessageIdFlag = true;
			}
		} catch (PropertyAccessException e) {
			LOG.warn("Error occured in retrieving the override mime message id option. Defaulting to 'false'.");
			e.printStackTrace();
		}
		
		LOG.debug("Direct: Override mime message id option: '" + overrideMimeMessageIdFlag + "'");	
		
		return overrideMimeMessageIdFlag;
	}
    
    
	/**
     * The "origination-date" is a required message "header" field according to RFC 5322. 
     * Make sure the message header "Date" exists in the passed in MimeMessage. If it does not
     * exist add the header to the message.
     * 
     * @param mimeMessage
     * 		Contains the MimeMessage. Upon return this message will be updated with a "Date" header if one does not exist.
     */
	private void ensureOrigDateHeaderExistsInMimeMessage(MimeMessage mimeMessage) {
		if (mimeMessage != null) { 
			try {
				@SuppressWarnings("rawtypes")
				Enumeration headers = mimeMessage.getAllHeaders();					

				if (headers != null) {
					boolean foundDateItem = false;
					
					while (headers.hasMoreElements()) {
						Header hdr = (Header) headers.nextElement();

						if (DATE_HEADER_FIELD.equalsIgnoreCase(hdr.getName())) {
							foundDateItem = true;
							
							LOG.debug("Found header item '" + DATE_HEADER_FIELD + "' in mimeMessage. Value: '" + hdr.getValue() + "'");
							
							break;
						}
					}
					
					if (!foundDateItem) {						
						String datePattern = "E, dd MMM yyyy HH:mm:ss Z";
						SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
						
						String dateValue = dateFormat.format(new Date());
						
						// Add the "Date" header to the mime message
						mimeMessage.setHeader(DATE_HEADER_FIELD, dateValue);								
						
						String message = "\nDid NOT find header item '" + DATE_HEADER_FIELD + "' in mimeMessage. " +
								"Adding header '" + DATE_HEADER_FIELD + "' with value: '" + dateValue + "' to the mimeMessage.";
						
						LOG.debug(message);
					}
				}
			} catch (MessagingException e) {
				LOG.error("Error occurred in adding 'Date' message header item to mimeMessage." + e.getMessage());
						
				e.printStackTrace();
			}
			
			logDebugMessageHeaderItems(mimeMessage);			
		}				
	}

	/**
     * Log debug header information about the mime message.
     * 
     * @param mimeMessage
     * 		Contains the MimeMessage for which to log header information.
     */
    private void logDebugMessageHeaderItems(MimeMessage mimeMessage) {
    	if (LOG.isDebugEnabled()) {
        	StringBuilder buf = new StringBuilder("\nMimeMessage Headers:\n");
        	
    		if (mimeMessage != null) {			
    			try {
    				@SuppressWarnings("rawtypes")
    				Enumeration headers = mimeMessage.getAllHeaders();
    				
    				if (headers != null) {
    					while (headers.hasMoreElements()) {
    						Header hdr = (Header) headers.nextElement();

    						buf.append("\tName: '" + hdr.getName() + "'  Value: '" + hdr.getValue() + "'\n");
    					}
    				} else {
    					buf.append("\tHeaders are NULL \n");
    				}
    			} catch (MessagingException e) {
    				LOG.debug("Error occurred in logging mime message headers. " + e.getMessage());
    			}					
    		} else {
    			buf.append("\tHeaders are NULL \n");
    		}  	
        	
    		LOG.debug(buf.toString());	    		
		}
	}    
    
    
    
}
