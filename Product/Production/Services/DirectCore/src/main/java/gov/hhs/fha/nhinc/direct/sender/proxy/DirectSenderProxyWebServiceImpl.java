/*
 * Copyright (c) 2009-2014, United States Government, as represented by the Secretary of Health and Human Services.
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
package gov.hhs.fha.nhinc.direct.sender.proxy;

import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerCache;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerException;
import gov.hhs.fha.nhinc.direct.ConnectCustomSendMimeMessage;
import gov.hhs.fha.nhinc.direct.DirectConstants;
import gov.hhs.fha.nhinc.direct.DirectSenderPortType;
import gov.hhs.fha.nhinc.direct.DirectSenderService;
import java.io.IOException;
import java.util.logging.Level;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.ws.BindingType;
import javax.xml.ws.soap.Addressing;
import org.apache.log4j.Logger;

/**
 *
 * @author svalluripalli
 */
@BindingType(value = javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)
@Addressing(enabled = true)
public class DirectSenderProxyWebServiceImpl {

    private static final Logger LOG = Logger.getLogger(DirectSenderProxyWebServiceImpl.class);

    /**
     *
     * @param message
     */
    public void sendOutboundDirect(MimeMessage message) {
        LOG.debug("Begin DirectSenderUnsecuredProxy.sendOutboundDirect(MimeMessage)");

        try { // Call Web Service Operation        
            String url = ConnectionManagerCache.getInstance().getInternalEndpointURLByServiceName(DirectConstants.DIRECT_SENDER_SERVICE_NAME);
            DirectSenderPortType port = getPort(url);
            gov.hhs.fha.nhinc.direct.SendoutMessage parameters = new gov.hhs.fha.nhinc.direct.SendoutMessage();
            ConnectCustomSendMimeMessage senderMessage = new ConnectCustomSendMimeMessage();
            senderMessage.setContent((byte[]) message.getContent());
            InternetAddress senderAdds = (InternetAddress) message.getSender();
            senderMessage.setSender(senderAdds.getAddress());
            senderMessage.setSubject(message.getSubject());
            parameters.setMessage(senderMessage);
            port.sendOutboundDirect(parameters);
        } catch (IOException ex) {
            LOG.error("DirectSender WebService Failed :" + ex.getMessage());
        } catch (MessagingException ex) {
            LOG.error("DirectSender WebService Failed :" + ex.getMessage());
        } catch (ConnectionManagerException ex) {
            java.util.logging.Logger.getLogger(DirectSenderProxyWebServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        LOG.debug("End DirectSenderUnsecuredProxy.sendOutboundDirect(MimeMessage)");
    }

    private DirectSenderPortType getPort(String url) {
        DirectSenderService service = new DirectSenderService();
        DirectSenderPortType port = service.getDirectSenderPortType();
        ((javax.xml.ws.BindingProvider) port).getRequestContext().put(javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
        return port;
    }
}
