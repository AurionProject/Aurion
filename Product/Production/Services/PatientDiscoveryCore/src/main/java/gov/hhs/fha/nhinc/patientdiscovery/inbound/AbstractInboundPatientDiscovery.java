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
package gov.hhs.fha.nhinc.patientdiscovery.inbound;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.patientdiscovery.PatientDiscoveryAuditor;
import gov.hhs.fha.nhinc.patientdiscovery.PatientDiscoveryException;
import gov.hhs.fha.nhinc.properties.PropertyAccessException;
import gov.hhs.fha.nhinc.properties.PropertyAccessor;
import ihe.iti.xcpd._2009.PRPAIN201305UV02Fault;
import ihe.iti.xcpd._2009.PatientLocationQueryFault;
import ihe.iti.xcpd._2009.PatientLocationQueryRequestType;
import ihe.iti.xcpd._2009.PatientLocationQueryResponseType;

import org.apache.log4j.Logger;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201306UV02;

public abstract class AbstractInboundPatientDiscovery implements InboundPatientDiscovery {
	
	private static final Logger LOG = Logger.getLogger(AbstractInboundPatientDiscovery.class);

    abstract PRPAIN201306UV02 process(PRPAIN201305UV02 body, AssertionType assertion) throws PatientDiscoveryException;
    abstract PatientLocationQueryResponseType processPatientLocationQuery(PatientLocationQueryRequestType body, 
    		AssertionType assertion) throws PatientDiscoveryException, PatientLocationQueryFault;

    abstract PatientDiscoveryAuditor getAuditLogger();

    /**
     * Method that processes the Patient Discovery request
     * 
     * @param body the body of the PD request
     * @param assertion the assertion of the PD request
     * @return PRPAIN201306UV02 response message
     */
    @Override
    public PRPAIN201306UV02 respondingGatewayPRPAIN201305UV02(PRPAIN201305UV02 body, AssertionType assertion)
            throws PatientDiscoveryException {
    	
   		auditRequestFromNhin(body, assertion);
    	
        PRPAIN201306UV02 response = process(body, assertion);
            
       	auditResponseToNhin(response, assertion);    
        
        return response;
    }

    /**
     * Method that processes the Patient Location Query requests (ITI-56)
     * 
     * @param body the body of the PLQ request
     * @param assertion the assertion of the PLQ request
     * @return PatientLocationQueryResponseType response message
     */
    @Override
    public PatientLocationQueryResponseType respondingGatewayPatientLocationQuery(
			PatientLocationQueryRequestType body, AssertionType assertion) throws PatientDiscoveryException, PatientLocationQueryFault {
    	
    	PatientLocationQueryResponseType response = processPatientLocationQuery(body, assertion);
        
        return response;
    }

    protected void auditRequestFromNhin(PRPAIN201305UV02 body, AssertionType assertion) {
    	boolean auditNhin = isAuditEnabled(NhincConstants.GATEWAY_PROPERTY_FILE, NhincConstants.NHIN_AUDIT_PROPERTY);
    	if (auditNhin) {
            getAuditLogger().auditNhin201305(body, assertion, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION);
    	}
    }

    protected void auditResponseToNhin(PRPAIN201306UV02 response, AssertionType assertion) {
    	boolean auditNhin = isAuditEnabled(NhincConstants.GATEWAY_PROPERTY_FILE, NhincConstants.NHIN_AUDIT_PROPERTY);
    	if (auditNhin) {
            getAuditLogger().auditNhin201306(response, assertion, NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION);
    	}
    }

    protected void auditRequestToAdapter(PRPAIN201305UV02 body, AssertionType assertion) {
    	boolean auditAdapter = isAuditEnabled(NhincConstants.GATEWAY_PROPERTY_FILE, NhincConstants.ADAPTER_AUDIT_PROPERTY);
    	if (auditAdapter) {
            getAuditLogger().auditAdapter201305(body, assertion, NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION);
    	}
    }

    protected void auditResponseFromAdapter(PRPAIN201306UV02 response, AssertionType assertion) {
    	boolean auditAdapter = isAuditEnabled(NhincConstants.GATEWAY_PROPERTY_FILE, NhincConstants.ADAPTER_AUDIT_PROPERTY);
    	if (auditAdapter) {
            getAuditLogger().auditAdapter201306(response, assertion, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION);
    	}
    }
    
	/**
	 * Retrieves flag for audit enabling. If true, audit is enabled.
	 * 
	 * @param gatewayPropertiesFile
	 *            Properties File
	 * @param auditEnabledPropertyKey
	 *            Property Name
	 * @return Property Value
	 */
	protected boolean isAuditEnabled(String gatewayPropertiesFile, String auditEnabledPropertyKey) {
		boolean propertyValue = false;
		try {
			PropertyAccessor propertyAccessor = PropertyAccessor.getInstance();
			propertyValue = propertyAccessor.getPropertyBoolean(
					gatewayPropertiesFile, auditEnabledPropertyKey);
		} catch (PropertyAccessException ex) {
			LOG.error(ex.getMessage());
		}
		return propertyValue;
	}   
}
