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
package gov.hhs.fha.nhinc.patientdiscovery.outbound;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import gov.hhs.fha.nhinc.aspect.OutboundProcessingEvent;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.patientdiscovery.PatientDiscoveryAuditLogger;
import gov.hhs.fha.nhinc.patientdiscovery.aspect.PRPAIN201305UV02ArgTransformer;
import gov.hhs.fha.nhinc.patientdiscovery.aspect.RespondingGatewayPRPAIN201306UV02Builder;

import java.lang.reflect.Method;

import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.RespondingGatewayPRPAIN201305UV02RequestType;
import org.hl7.v3.RespondingGatewayPRPAIN201306UV02ResponseType;
import org.junit.Test;

/**
 * @author akong
 *
 */
public class StandardOutboundPatientDiscoveryTest {
	private StandardOutboundPatientDiscovery patientDiscovery;
	private RespondingGatewayPRPAIN201305UV02RequestType request;
	private AssertionType assertion;
		    
    /**
     * Tests that audit logging occurs when Entity Audit Logging is enabled.
     */
    @Test
    public void EnableEntityAuditLoggingTest() {   
    	setUpTestParams(true); 
    	    	
    	patientDiscovery.respondingGatewayPRPAIN201305UV02(request, assertion);
    	
    	verify(patientDiscovery.getNewPatientDiscoveryAuditLogger()).auditEntity201305(any(RespondingGatewayPRPAIN201305UV02RequestType.class), any(AssertionType.class),
                anyString());
    	verify(patientDiscovery.getNewPatientDiscoveryAuditLogger()).auditEntity201306(any(RespondingGatewayPRPAIN201306UV02ResponseType.class), any(AssertionType.class), 
    			anyString());    	    	
    }
    
    /**
     * Tests that audit logging does not occur when Entity Audit Logging is disabled.
     */
    @Test
    public void DisableEntityAuditLoggingTest() {   
    	setUpTestParams(false);
    	
    	patientDiscovery.respondingGatewayPRPAIN201305UV02(request, assertion);
    	
    	verify(patientDiscovery.getNewPatientDiscoveryAuditLogger(), never()).auditEntity201305(any(RespondingGatewayPRPAIN201305UV02RequestType.class), any(AssertionType.class),
                anyString());
    	verify(patientDiscovery.getNewPatientDiscoveryAuditLogger(), never()).auditEntity201306(any(RespondingGatewayPRPAIN201306UV02ResponseType.class), any(AssertionType.class), 
    			anyString());    	    	
    }   
    
	/**
     * Initializes test parameters and forces/disables audit logging.
     * @param isAuditEnabled value for audit enabled check
     */
    private void setUpTestParams(final boolean isAuditEnabled) {
    	patientDiscovery = new StandardOutboundPatientDiscovery() {
    		private PatientDiscoveryAuditLogger auditLogger;
    		
    		@Override
    		protected boolean isAuditEnabled(String gatewayPropertiesFile, String auditEnabledPropertyKey)
    		{
    			return isAuditEnabled;
    		}
    		
    		@Override
    	    protected PatientDiscoveryAuditLogger getNewPatientDiscoveryAuditLogger() {
    			if(auditLogger == null) {
    				auditLogger = mock(PatientDiscoveryAuditLogger.class);
    			}
    				
    	        return auditLogger;
    	    }
    		
    		@Override
    	    protected RespondingGatewayPRPAIN201306UV02ResponseType getResponseFromCommunities(
    	            RespondingGatewayPRPAIN201305UV02RequestType request, AssertionType assertion) {
    			return new RespondingGatewayPRPAIN201306UV02ResponseType();
    		}
    	};
    	
    	request = new RespondingGatewayPRPAIN201305UV02RequestType(); 
    	PRPAIN201305UV02 prpain201305UV02 = new PRPAIN201305UV02();
    	request.setPRPAIN201305UV02(prpain201305UV02);		
    	
    	assertion = new AssertionType();  
    }
    
    @Test
    public void hasOutboundProcessingEvent() throws Exception {
        Class<StandardOutboundPatientDiscovery> clazz = StandardOutboundPatientDiscovery.class;
        Method method = clazz.getMethod("respondingGatewayPRPAIN201305UV02", RespondingGatewayPRPAIN201305UV02RequestType.class,
                AssertionType.class);
        OutboundProcessingEvent annotation = method.getAnnotation(OutboundProcessingEvent.class);
        assertNotNull(annotation);
        assertEquals(PRPAIN201305UV02ArgTransformer.class, annotation.beforeBuilder());
        assertEquals(RespondingGatewayPRPAIN201306UV02Builder.class, annotation.afterReturningBuilder());
        assertEquals("Patient Discovery", annotation.serviceType());
        assertEquals("1.0", annotation.version());
    }
}
