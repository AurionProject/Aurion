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

import gov.hhs.fha.nhinc.aspect.InboundProcessingEvent;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.compliance.PatientDiscoveryResponseComplianceChecker;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.patientdiscovery.PatientDiscovery201305Processor;
import gov.hhs.fha.nhinc.patientdiscovery.PatientDiscoveryAuditLogger;
import gov.hhs.fha.nhinc.patientdiscovery.PatientDiscoveryAuditor;
import gov.hhs.fha.nhinc.patientdiscovery.PatientDiscoveryException;
import gov.hhs.fha.nhinc.patientdiscovery.aspect.PRPAIN201305UV02EventDescriptionBuilder;
import gov.hhs.fha.nhinc.patientdiscovery.aspect.PRPAIN201306UV02EventDescriptionBuilder;
import gov.hhs.fha.nhinc.patientlocationquery.PatientLocationQueryProcessor;
import ihe.iti.xcpd._2009.PRPAIN201305UV02Fault;
import ihe.iti.xcpd._2009.PatientLocationQueryFault;
import ihe.iti.xcpd._2009.PatientLocationQueryRequestType;
import ihe.iti.xcpd._2009.PatientLocationQueryResponseType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201306UV02;

/**
 * @author akong
 * 
 */
public class StandardInboundPatientDiscovery extends AbstractInboundPatientDiscovery {

    private final PatientDiscovery201305Processor patientDiscoveryProcessor;
    private final PatientLocationQueryProcessor patientLocationQueryProcessor;
    private final PatientDiscoveryAuditor auditLogger;
    private Log log = null;

    /**
     * Constructor.
     */
    public StandardInboundPatientDiscovery() {
        patientDiscoveryProcessor = new PatientDiscovery201305Processor();
        patientLocationQueryProcessor = new PatientLocationQueryProcessor();
        auditLogger = new PatientDiscoveryAuditLogger();
        log = createLogger();
    }

    /**
     * Constructor.
     * 
     * @param patientDiscoveryProcessor
     * @param auditLogger
     */
    public StandardInboundPatientDiscovery(PatientDiscovery201305Processor patientDiscoveryProcessor, 
    		PatientLocationQueryProcessor patientLocationQueryProcessor,
            PatientDiscoveryAuditor auditLogger) {
        this.patientDiscoveryProcessor = patientDiscoveryProcessor;
        this.patientLocationQueryProcessor = patientLocationQueryProcessor;
        this.auditLogger = auditLogger;
        log = createLogger();
    }
    
    protected Log createLogger() {
		return ((log != null) ? log : LogFactory.getLog(getClass()));
	}

    @Override
    @InboundProcessingEvent(beforeBuilder = PRPAIN201305UV02EventDescriptionBuilder.class, afterReturningBuilder = PRPAIN201306UV02EventDescriptionBuilder.class, serviceType = "Patient Discovery", version = "1.0")
    public PRPAIN201306UV02 respondingGatewayPRPAIN201305UV02(PRPAIN201305UV02 body, AssertionType assertion)
            throws PatientDiscoveryException {
        auditRequestFromNhin(body, assertion);

        PRPAIN201306UV02 response = process(body, assertion);

        auditResponseToNhin(response, assertion);

        return response;
    }

    @Override
    //@InboundProcessingEvent(beforeBuilder = PRPAIN201305UV02EventDescriptionBuilder.class, afterReturningBuilder = PRPAIN201306UV02EventDescriptionBuilder.class, serviceType = "Patient Discovery", version = "1.0")
    PRPAIN201306UV02 process(PRPAIN201305UV02 body, AssertionType assertion) throws PatientDiscoveryException {
    	boolean auditAdapter = isAuditEnabled(NhincConstants.GATEWAY_PROPERTY_FILE, 
    			NhincConstants.ADAPTER_AUDIT_PROPERTY);
    	if (auditAdapter) {
    		auditRequestToAdapter(body, assertion);
    	}
    	
        PRPAIN201306UV02 response = patientDiscoveryProcessor.process201305(body, assertion);

        PatientDiscoveryResponseComplianceChecker complianceChecker = new PatientDiscoveryResponseComplianceChecker(response);
        complianceChecker.update2011SpecCompliance();
        
        if (auditAdapter) {
        	auditResponseFromAdapter(response, assertion);
        }
        
        return response;
    }
    
    @Override
	PatientLocationQueryResponseType processPatientLocationQuery(PatientLocationQueryRequestType body, 
			AssertionType assertion) throws PatientDiscoveryException, PatientLocationQueryFault
    {
    	
    	log.debug("Calling method processPatientLocationQuery in class StandardInboundPatientDiscovery...");

		PatientLocationQueryResponseType response = patientLocationQueryProcessor.processPatientLocationQuery(body, assertion);
        
        return response;
	}
    
    /*
     * (non-Javadoc)
     * 
     * @see gov.hhs.fha.nhinc.patientdiscovery.inbound.AbstractInboundPatientDiscovery#getAuditLogger()
     */
    @Override
    PatientDiscoveryAuditor getAuditLogger() {
        return auditLogger;
    }

}
