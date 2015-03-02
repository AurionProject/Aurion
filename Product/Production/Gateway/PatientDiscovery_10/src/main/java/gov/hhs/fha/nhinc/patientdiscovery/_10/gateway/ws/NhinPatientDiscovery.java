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
package gov.hhs.fha.nhinc.patientdiscovery._10.gateway.ws;

import gov.hhs.fha.nhinc.aspect.InboundMessageEvent;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.messaging.server.BaseService;
import gov.hhs.fha.nhinc.patientdiscovery.PatientDiscoveryException;
import gov.hhs.fha.nhinc.patientdiscovery.aspect.PRPAIN201305UV02EventDescriptionBuilder;
import gov.hhs.fha.nhinc.patientdiscovery.aspect.PRPAIN201306UV02EventDescriptionBuilder;
import gov.hhs.fha.nhinc.patientdiscovery.inbound.InboundPatientDiscovery;
import gov.hhs.healthit.nhin.PatientDiscoveryFaultType;
import gov.hhs.healthit.nhin.PatientLocationQueryFaultType;
import ihe.iti.xcpd._2009.PRPAIN201305UV02Fault;
import ihe.iti.xcpd._2009.PatientLocationQueryFault;
import ihe.iti.xcpd._2009.PatientLocationQueryRequestType;
import ihe.iti.xcpd._2009.PatientLocationQueryResponseType;
import ihe.iti.xcpd._2009.RespondingGatewayPortType;

import javax.annotation.Resource;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.Addressing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201306UV02;

@Addressing(enabled = true)
@BindingType(value = javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)
public class NhinPatientDiscovery extends BaseService implements RespondingGatewayPortType {

    private InboundPatientDiscovery inboundPatientDiscovery;

    private WebServiceContext context;
    
    private Log log = null;

    /**
     * Constructor.
     */
    public NhinPatientDiscovery() {
        super();
        log = createLogger();
    }
    
    protected Log createLogger() {
		return ((log != null) ? log : LogFactory.getLog(getClass()));
	}

    /**
     * The web service implementation of Patient Discovery.
     * 
     * @param body the body of the request
     * @return the Patient discovery Response
     * @throws PRPAIN201305UV02Fault a fault if there's an exception
     */
    @InboundMessageEvent(beforeBuilder = PRPAIN201305UV02EventDescriptionBuilder.class, 
            afterReturningBuilder = PRPAIN201306UV02EventDescriptionBuilder.class, 
            serviceType = "Patient Discovery", version = "1.0")
    public PRPAIN201306UV02 respondingGatewayPRPAIN201305UV02(PRPAIN201305UV02 body) throws PRPAIN201305UV02Fault {
        try {
            AssertionType assertion = getAssertion(context, null);

            return inboundPatientDiscovery.respondingGatewayPRPAIN201305UV02(body, assertion);
        } catch (PatientDiscoveryException e) {
            PatientDiscoveryFaultType type = new PatientDiscoveryFaultType();
            type.setErrorCode("920");
            type.setMessage(e.getLocalizedMessage());
            PRPAIN201305UV02Fault fault = new PRPAIN201305UV02Fault(e.getMessage(), type);
            throw fault;
        }
    }

    @Resource
    public void setContext(WebServiceContext context) {
        this.context = context;
    }

    public void setInboundPatientDiscovery(InboundPatientDiscovery inboundPatientDiscovery) {
        this.inboundPatientDiscovery = inboundPatientDiscovery;
    }

    /**
     * Gets the inbound patient discovery dependency.
     *
     * @return the inbound patient discovery
     */
    public InboundPatientDiscovery getInboundPatientDiscovery() {
        return this.inboundPatientDiscovery;
    }

    /**
     * The web service implementation of Patient Location Query (ITI-56).
     * 
     * @param body the body of the request
     * @return the Patient discovery Response
     * @throws PatientLocationQueryFault a fault if there's an exception
     */
    @InboundMessageEvent(beforeBuilder = PRPAIN201305UV02EventDescriptionBuilder.class, 
            afterReturningBuilder = PRPAIN201306UV02EventDescriptionBuilder.class, 
            serviceType = "Patient Discovery", version = "1.0")
    public PatientLocationQueryResponseType respondingGatewayPatientLocationQuery(PatientLocationQueryRequestType request) throws PatientLocationQueryFault
    {
    	log.debug("Entering respondingGatewayPatientLocationQuery...");
		
        try {
        	AssertionType assertion = getAssertion(context, null);
			return inboundPatientDiscovery.respondingGatewayPatientLocationQuery(request, assertion);
		} 
        catch (PatientDiscoveryException e) 
        {
			PatientLocationQueryFaultType type = new PatientLocationQueryFaultType();
			type.setErrorCode("Sender");
			type.setReason(e.getLocalizedMessage());
            PatientLocationQueryFault fault = new PatientLocationQueryFault(e.getMessage(), type);
            throw fault;

		}
	}
}
