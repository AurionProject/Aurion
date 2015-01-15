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
package gov.hhs.fha.nhinc.docsubmission.inbound.deferred.response;

import gov.hhs.fha.nhinc.aspect.InboundProcessingEvent;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.docsubmission.XDRAuditLogger;
import gov.hhs.fha.nhinc.docsubmission.adapter.deferred.response.proxy.AdapterDocSubmissionDeferredResponseProxy;
import gov.hhs.fha.nhinc.docsubmission.adapter.deferred.response.proxy.AdapterDocSubmissionDeferredResponseProxyObjectFactory;
import gov.hhs.fha.nhinc.docsubmission.aspect.DeferredResponseDescriptionBuilder;
import gov.hhs.fha.nhinc.docsubmission.aspect.DocSubmissionArgTransformerBuilder;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.properties.PropertyAccessException;
import gov.hhs.fha.nhinc.properties.PropertyAccessor;
import gov.hhs.healthit.nhin.XDRAcknowledgementType;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;

import org.apache.log4j.Logger;

public abstract class AbstractInboundDocSubmissionDeferredResponse implements InboundDocSubmissionDeferredResponse {

	private static final Logger LOG = Logger.getLogger(AbstractInboundDocSubmissionDeferredResponse.class);
	
    abstract XDRAcknowledgementType processDocSubmissionResponse(RegistryResponseType body, AssertionType assertion);

    private XDRAuditLogger auditLogger;
    private AdapterDocSubmissionDeferredResponseProxyObjectFactory adapterFactory;

    public AbstractInboundDocSubmissionDeferredResponse(
            AdapterDocSubmissionDeferredResponseProxyObjectFactory adapterFactory, XDRAuditLogger auditLogger) {
        this.adapterFactory = adapterFactory;
        this.auditLogger = auditLogger;
    }
    
    @InboundProcessingEvent(beforeBuilder = DeferredResponseDescriptionBuilder.class,
            afterReturningBuilder = DocSubmissionArgTransformerBuilder.class,
            serviceType = "Document Submission Deferred Response", version = "")
    public XDRAcknowledgementType provideAndRegisterDocumentSetBResponse(RegistryResponseType body,
            AssertionType assertion) {
    	
    	boolean auditNhin = isAuditEnabled(NhincConstants.GATEWAY_PROPERTY_FILE, NhincConstants.NHIN_AUDIT_PROPERTY);
    	
    	if (auditNhin) {
    		auditRequestFromNhin(body, assertion);
    	}

        XDRAcknowledgementType response = processDocSubmissionResponse(body, assertion);

        if (auditNhin) {
        	auditResponseToNhin(response, assertion);
        }

        return response;
    }
    
    protected XDRAcknowledgementType sendToAdapter(RegistryResponseType body, AssertionType assertion) {
        AdapterDocSubmissionDeferredResponseProxy proxy = adapterFactory.getAdapterDocSubmissionDeferredResponseProxy();
        return proxy.provideAndRegisterDocumentSetBResponse(body, assertion);
    }

    protected void auditRequestToAdapter(RegistryResponseType body, AssertionType assertion) {
        auditLogger.auditAdapterXDRResponse(body, assertion, NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION);
    }

    protected void auditResponseFromAdapter(XDRAcknowledgementType response, AssertionType assertion) {
        auditLogger.auditAdapterAcknowledgement(response, assertion, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION,
                NhincConstants.XDR_RESPONSE_ACTION);
    }

    protected void auditRequestFromNhin(RegistryResponseType body, AssertionType assertion) {
        auditLogger.auditNhinXDRResponse(body, assertion, null, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION, false);
    }

    protected void auditResponseToNhin(XDRAcknowledgementType response, AssertionType assertion) {
        auditLogger.auditAcknowledgement(response, assertion, null, NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION,
                NhincConstants.XDR_RESPONSE_ACTION);
    }
    
    /**
     * Determine if audit is enabled for the NwHIN interface
     *
     * @return Flag to indicate if audit logging is enabled for this interface
     */
    protected boolean isAuditEnabled(String propertyFile, String propertyName) {
    	boolean isEnabled = false;
        try {
			isEnabled = PropertyAccessor.getInstance().getPropertyBoolean(propertyFile, propertyName);
		} catch (PropertyAccessException e) {
			LOG.error("Error: Failed to retrieve " + propertyName + " from property file: " + propertyFile);
        	LOG.error(e.getMessage(), e);
		}
        
        return isEnabled;
    }
}
