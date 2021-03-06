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
package gov.hhs.fha.nhinc.auditrepository.nhinc.proxy;

import gov.hhs.fha.nhinc.auditrepository.nhinc.proxy.service.AuditRepositoryUnsecuredServicePortDescriptor;
import gov.hhs.fha.nhinc.common.auditlog.LogEventRequestType;
import gov.hhs.fha.nhinc.common.nhinccommon.AcknowledgementType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.messaging.client.CONNECTClient;
import gov.hhs.fha.nhinc.messaging.client.CONNECTCXFClientFactory;
import gov.hhs.fha.nhinc.messaging.service.port.ServicePortDescriptor;
import gov.hhs.fha.nhinc.nhinccomponentauditrepository.AuditRepositoryManagerPortType;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.webserviceproxy.WebServiceProxyHelper;

import org.apache.log4j.Logger;

/**
 * 
 * @author akong
 */
public class AuditRepositoryProxyWebServiceUnsecuredImpl implements AuditRepositoryProxy {
    private static final Logger LOG = Logger.getLogger(AuditRepositoryProxyWebServiceUnsecuredImpl.class);

    private WebServiceProxyHelper oProxyHelper = new WebServiceProxyHelper();

    public AcknowledgementType auditLog(LogEventRequestType request, AssertionType assertion) {
        LOG.debug("Entering AuditRepositoryProxyWebServiceUnsecuredImpl.auditLog(...)");
        AcknowledgementType result = new AcknowledgementType();

        if (request.getAuditMessage() == null) {
            LOG.error("Audit Request is null");
        }
        try {
            if (request != null) {
            	// Set the assertion in the body if necessary
            	if((assertion != null) && (request.getAssertion() == null)) {
            		request.setAssertion(assertion);
            	}

                String url = oProxyHelper.getUrlLocalHomeCommunity(NhincConstants.AUDIT_REPO_SERVICE_NAME);

                if (NullChecker.isNotNullish(url)) {

                    ServicePortDescriptor<AuditRepositoryManagerPortType> portDescriptor = new AuditRepositoryUnsecuredServicePortDescriptor();

                    CONNECTClient<AuditRepositoryManagerPortType> client = CONNECTCXFClientFactory.getInstance()
                            .getCONNECTClientUnsecured(portDescriptor, url, assertion);
                    
                    result = (AcknowledgementType) client.invokePort(AuditRepositoryManagerPortType.class, "logEvent", request);

                } else {
                    LOG.error("Failed to call the web service (" + NhincConstants.AUDIT_REPO_SERVICE_NAME
                            + ").  The URL is null.");
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to call the web service (" + NhincConstants.AUDIT_REPO_SERVICE_NAME
                    + ").  An unexpected exception occurred.  " + "Exception: " + e.getMessage(), e);
        }

        LOG.debug("In AuditRepositoryProxyWebServiceUnsecuredImpl.auditLog(...) - completed called to ConnectionManager to retrieve endpoint.");

        return result;
    }
}
