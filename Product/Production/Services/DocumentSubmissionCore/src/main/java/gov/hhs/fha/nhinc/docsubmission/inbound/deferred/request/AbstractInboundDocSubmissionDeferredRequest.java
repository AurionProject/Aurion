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
package gov.hhs.fha.nhinc.docsubmission.inbound.deferred.request;

import gov.hhs.fha.nhinc.aspect.InboundProcessingEvent;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.docsubmission.XDRAuditLogger;
import gov.hhs.fha.nhinc.docsubmission.adapter.deferred.request.proxy.AdapterDocSubmissionDeferredRequestProxy;
import gov.hhs.fha.nhinc.docsubmission.adapter.deferred.request.proxy.AdapterDocSubmissionDeferredRequestProxyObjectFactory;
import gov.hhs.fha.nhinc.docsubmission.aspect.DocSubmissionArgTransformerBuilder;
import gov.hhs.fha.nhinc.docsubmission.aspect.DocSubmissionBaseEventDescriptionBuilder;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.properties.PropertyAccessException;
import gov.hhs.fha.nhinc.properties.PropertyAccessor;
import gov.hhs.healthit.nhin.XDRAcknowledgementType;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;

import org.apache.log4j.Logger;

public abstract class AbstractInboundDocSubmissionDeferredRequest implements InboundDocSubmissionDeferredRequest {

	private static final Logger LOG = Logger.getLogger(AbstractInboundDocSubmissionDeferredRequest.class);
	
    abstract XDRAcknowledgementType processDocSubmissionRequest(ProvideAndRegisterDocumentSetRequestType body,
            AssertionType assertion);
    
    private XDRAuditLogger auditLogger;
    private AdapterDocSubmissionDeferredRequestProxyObjectFactory adapterFactory;
    
    public AbstractInboundDocSubmissionDeferredRequest(
            AdapterDocSubmissionDeferredRequestProxyObjectFactory adapterFactory, XDRAuditLogger auditLogger) {
        this.adapterFactory = adapterFactory;
        this.auditLogger = auditLogger;
    }
    
    @InboundProcessingEvent(beforeBuilder = DocSubmissionBaseEventDescriptionBuilder.class,
            afterReturningBuilder = DocSubmissionArgTransformerBuilder.class, 
            serviceType = "Document Submission Deferred Request",
            version = "")
    public XDRAcknowledgementType provideAndRegisterDocumentSetBRequest(ProvideAndRegisterDocumentSetRequestType body,
            AssertionType assertion) {

    	boolean auditNhin = isAuditEnabled(NhincConstants.GATEWAY_PROPERTY_FILE, NhincConstants.NHIN_AUDIT_PROPERTY);
    	
    	if (auditNhin) {
    		auditRequestFromNhin(body, assertion);
    	}
        
        XDRAcknowledgementType response = processDocSubmissionRequest(body, assertion);

        if (auditNhin) {
        	auditResponseToNhin(response, assertion);
        }

        return response;
    }
    
    protected XDRAcknowledgementType sendToAdapter(ProvideAndRegisterDocumentSetRequestType body, AssertionType assertion) {
        AdapterDocSubmissionDeferredRequestProxy proxy = adapterFactory.getAdapterDocSubmissionDeferredRequestProxy();
        return proxy.provideAndRegisterDocumentSetBRequest(body, assertion);
    }
    
    protected void auditRequestToAdapter(ProvideAndRegisterDocumentSetRequestType request, AssertionType assertion) {
        auditLogger.auditAdapterXDR(request, assertion, NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION);
    }

    protected void auditResponseFromAdapter(XDRAcknowledgementType response, AssertionType assertion) {
        auditLogger.auditAdapterAcknowledgement(response, assertion, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION,
                NhincConstants.XDR_REQUEST_ACTION);
    }

    protected void auditRequestFromNhin(ProvideAndRegisterDocumentSetRequestType request, AssertionType assertion) {
        auditLogger.auditNhinXDR(request, assertion, null, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION);
    }

    protected void auditResponseToNhin(XDRAcknowledgementType response, AssertionType assertion) {
        auditLogger.auditAcknowledgement(response, assertion, null, NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION,
                NhincConstants.XDR_REQUEST_ACTION);
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
