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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gov.hhs.fha.nhinc.aspect.InboundProcessingEvent;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import gov.hhs.fha.nhinc.docsubmission.XDRAuditLogger;
import gov.hhs.fha.nhinc.docsubmission.adapter.deferred.response.proxy.AdapterDocSubmissionDeferredResponseProxy;
import gov.hhs.fha.nhinc.docsubmission.adapter.deferred.response.proxy.AdapterDocSubmissionDeferredResponseProxyObjectFactory;
import gov.hhs.fha.nhinc.docsubmission.aspect.DeferredResponseDescriptionBuilder;
import gov.hhs.fha.nhinc.docsubmission.aspect.DocSubmissionArgTransformerBuilder;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.healthit.nhin.XDRAcknowledgementType;

import java.lang.reflect.Method;

import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;

import org.junit.Test;

/**
 * @author akong
 * 
 */
public class PassthroughInboundDocSubmissionDeferredResponseTest {

    @Test
    public void passthroughInboundDocSubmissionDeferredResponse() {
        RegistryResponseType regResponse = new RegistryResponseType();
        AssertionType assertion = new AssertionType();
        XDRAcknowledgementType expectedResponse = new XDRAcknowledgementType();

        AdapterDocSubmissionDeferredResponseProxyObjectFactory adapterFactory = mock(AdapterDocSubmissionDeferredResponseProxyObjectFactory.class);
        AdapterDocSubmissionDeferredResponseProxy adapterProxy = mock(AdapterDocSubmissionDeferredResponseProxy.class);
        XDRAuditLogger auditLogger = mock(XDRAuditLogger.class);

        when(adapterFactory.getAdapterDocSubmissionDeferredResponseProxy()).thenReturn(adapterProxy);

        when(adapterProxy.provideAndRegisterDocumentSetBResponse(regResponse, assertion)).thenReturn(expectedResponse);

        PassthroughInboundDocSubmissionDeferredResponse passthroughDocSubmission = new PassthroughInboundDocSubmissionDeferredResponse(
                adapterFactory, auditLogger){
        	
        	@Override
        	protected boolean isAuditEnabled(String propertyFile, String propertyName) {
                return true;
            }
        };

        XDRAcknowledgementType actualResponse = passthroughDocSubmission.provideAndRegisterDocumentSetBResponse(
                regResponse, assertion);

        assertSame(expectedResponse, actualResponse);

        verify(auditLogger, never()).auditAdapterXDRResponse(any(RegistryResponseType.class), any(AssertionType.class),
                anyString());

        verify(auditLogger, never()).auditAdapterAcknowledgement(any(XDRAcknowledgementType.class),
                any(AssertionType.class), anyString(), anyString());

        verify(auditLogger).auditNhinXDRResponse(eq(regResponse), eq(assertion), isNull(NhinTargetSystemType.class),
                eq(NhincConstants.AUDIT_LOG_INBOUND_DIRECTION), eq(false));

        verify(auditLogger).auditAcknowledgement(eq(actualResponse), eq(assertion), isNull(NhinTargetSystemType.class),
                eq(NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION), eq(NhincConstants.XDR_RESPONSE_ACTION));
    }
    
    /**
     * This test is a standard PassthroughInboundDocSubmissionDeferredResponse test that verifies 
     * no auditing is performed when the property is false
     */
    @Test
    public void passthroughInboundDocSubmissionDeferredResponseNoAuditTest() {
        RegistryResponseType regResponse = new RegistryResponseType();
        AssertionType assertion = new AssertionType();
        XDRAcknowledgementType expectedResponse = new XDRAcknowledgementType();

        AdapterDocSubmissionDeferredResponseProxyObjectFactory adapterFactory = mock(AdapterDocSubmissionDeferredResponseProxyObjectFactory.class);
        AdapterDocSubmissionDeferredResponseProxy adapterProxy = mock(AdapterDocSubmissionDeferredResponseProxy.class);
        XDRAuditLogger auditLogger = mock(XDRAuditLogger.class);

        when(adapterFactory.getAdapterDocSubmissionDeferredResponseProxy()).thenReturn(adapterProxy);

        when(adapterProxy.provideAndRegisterDocumentSetBResponse(regResponse, assertion)).thenReturn(expectedResponse);

        PassthroughInboundDocSubmissionDeferredResponse passthroughDocSubmission = new PassthroughInboundDocSubmissionDeferredResponse(
                adapterFactory, auditLogger){
        	
        	@Override
        	protected boolean isAuditEnabled(String propertyFile, String propertyName) {
                return false;
            }
        };

        XDRAcknowledgementType actualResponse = passthroughDocSubmission.provideAndRegisterDocumentSetBResponse(
                regResponse, assertion);

        assertSame(expectedResponse, actualResponse);

        verify(auditLogger, never()).auditAdapterXDRResponse(any(RegistryResponseType.class), any(AssertionType.class),
                anyString());

        verify(auditLogger, never()).auditAdapterAcknowledgement(any(XDRAcknowledgementType.class),
                any(AssertionType.class), anyString(), anyString());

        verify(auditLogger, never()).auditNhinXDRResponse(eq(regResponse), eq(assertion), isNull(NhinTargetSystemType.class),
                eq(NhincConstants.AUDIT_LOG_INBOUND_DIRECTION), eq(false));

        verify(auditLogger, never()).auditAcknowledgement(eq(actualResponse), eq(assertion), isNull(NhinTargetSystemType.class),
                eq(NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION), eq(NhincConstants.XDR_RESPONSE_ACTION));
    }

    @Test
    public void hasInboundProcessingEvent() throws Exception {
        Class<PassthroughInboundDocSubmissionDeferredResponse> clazz = PassthroughInboundDocSubmissionDeferredResponse.class;
        Method method = clazz.getMethod("provideAndRegisterDocumentSetBResponse", RegistryResponseType.class,
                AssertionType.class);
        InboundProcessingEvent annotation = method.getAnnotation(InboundProcessingEvent.class);
        assertNotNull(annotation);
        assertEquals(DeferredResponseDescriptionBuilder.class, annotation.beforeBuilder());
        assertEquals(DocSubmissionArgTransformerBuilder.class, annotation.afterReturningBuilder());
        assertEquals("Document Submission Deferred Response", annotation.serviceType());
        assertEquals("", annotation.version());
    }
}
