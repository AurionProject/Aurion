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

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.HomeCommunityType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetCommunitiesType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerCache;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerException;
import gov.hhs.fha.nhinc.connectmgr.UrlInfo;
import gov.hhs.fha.nhinc.gateway.executorservice.ExecutorServiceHelper;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.orchestration.OutboundResponseProcessor;
import gov.hhs.fha.nhinc.patientdiscovery.MessageGeneratorUtils;
import gov.hhs.fha.nhinc.patientdiscovery.PatientDiscoveryAuditLogger;
import gov.hhs.fha.nhinc.patientdiscovery.entity.OutboundPatientDiscoveryDelegate;
import gov.hhs.fha.nhinc.patientdiscovery.entity.OutboundPatientDiscoveryOrchestratable;
import gov.hhs.fha.nhinc.patientdiscovery.nhin.proxy.NhinPatientDiscoveryProxy;
import gov.hhs.fha.nhinc.patientdiscovery.nhin.proxy.NhinPatientDiscoveryProxyObjectFactory;
import gov.hhs.fha.nhinc.transform.subdisc.HL7PRPA201306Transforms;
import ihe.iti.xcpd._2009.PatientLocationQueryRequestType;
import ihe.iti.xcpd._2009.PatientLocationQueryResponseType;
import ihe.iti.xcpd._2009.RespondingGatewayPatientLocationQueryRequestType;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.v3.CommunityPRPAIN201306UV02ResponseType;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201306UV02;
import org.hl7.v3.RespondingGatewayPRPAIN201305UV02RequestType;
import org.hl7.v3.RespondingGatewayPRPAIN201306UV02ResponseType;

import com.google.common.base.Optional;

public class PassthroughOutboundPatientDiscovery implements OutboundPatientDiscovery {

    private static final MessageGeneratorUtils msgUtils = MessageGeneratorUtils.getInstance();
    private final OutboundPatientDiscoveryDelegate delegate;
    private final PatientDiscoveryAuditLogger auditLogger;
    private Log log = null;

    /**
     * Constructor.
     */
    public PassthroughOutboundPatientDiscovery() {
        this.delegate = new OutboundPatientDiscoveryDelegate();
        this.auditLogger = new PatientDiscoveryAuditLogger();
        log = createLogger();
    }

    /**
     * Constructor.
     * 
     * @param delegate
     * @param auditLogger
     */
    public PassthroughOutboundPatientDiscovery(OutboundPatientDiscoveryDelegate delegate,
            PatientDiscoveryAuditLogger auditLogger) {
        this.delegate = delegate;
        this.auditLogger = auditLogger;
        log = createLogger();
    }
    
    protected Log createLogger(){
        return ((log != null) ? log : LogFactory.getLog(getClass()));
    }

    @Override
    public RespondingGatewayPRPAIN201306UV02ResponseType respondingGatewayPRPAIN201305UV02(
            RespondingGatewayPRPAIN201305UV02RequestType request, AssertionType assertion) {

        RespondingGatewayPRPAIN201306UV02ResponseType response = sendToNhin(request.getPRPAIN201305UV02(), assertion,
                msgUtils.convertFirstToNhinTargetSystemType(request.getNhinTargetCommunities()));

        return response;
    }

    @Override
    public void setExecutorService(ExecutorService regularExecutor, ExecutorService largeJobExecutor) {
        // Do nothing. Passthrough does not do fan out.
    }

    private RespondingGatewayPRPAIN201306UV02ResponseType sendToNhin(PRPAIN201305UV02 request, AssertionType assertion,
            NhinTargetSystemType target) {
        PRPAIN201306UV02 response;

        try {
            OutboundPatientDiscoveryOrchestratable inMessage = new OutboundPatientDiscoveryOrchestratable(delegate,
                    Optional.<OutboundResponseProcessor> absent(), null, null, assertion,
                    NhincConstants.PATIENT_DISCOVERY_SERVICE_NAME, target, request);
            OutboundPatientDiscoveryOrchestratable outMessage = delegate.process(inMessage);
            response = outMessage.getResponse();
        } catch (Exception ex) {
            String err = ExecutorServiceHelper.getFormattedExceptionInfo(ex, target,
                    NhincConstants.PATIENT_DISCOVERY_SERVICE_NAME);
            response = generateErrorResponse(target, request, err);
        }

        return convert(response, target);
    }

    private RespondingGatewayPRPAIN201306UV02ResponseType convert(PRPAIN201306UV02 response, NhinTargetSystemType target) {
        String hcid = getHCID(target);
        CommunityPRPAIN201306UV02ResponseType communityResponse = msgUtils
                .createCommunityPRPAIN201306UV02ResponseType(hcid);
        communityResponse.setPRPAIN201306UV02(response);

        RespondingGatewayPRPAIN201306UV02ResponseType gatewayResponse = new RespondingGatewayPRPAIN201306UV02ResponseType();
        gatewayResponse.getCommunityResponse().add(communityResponse);

        return gatewayResponse;
    }

    private String getHCID(NhinTargetSystemType target) {
        String hcid = null;
        if (target != null && target.getHomeCommunity() != null) {
            hcid = target.getHomeCommunity().getHomeCommunityId();
        }

        return hcid;

    }

    private PRPAIN201306UV02 generateErrorResponse(NhinTargetSystemType target, PRPAIN201305UV02 request, String error) {
        String errStr = "Error from target homeId=" + target.getHomeCommunity().getHomeCommunityId();
        errStr += "  The error received was " + error;
        return (new HL7PRPA201306Transforms()).createPRPA201306ForErrors(request, errStr);
    }

    /**
	 * Passthrough Outbound implementation of the PatientLocationQuery operation (ITI-56). Calls the NHIN service to handle the request.
	 * @param RespondingGatewayPatientLocationQueryRequestType message
	 * @param assertion
	 * @return RespondingGatewayPatientLocationQueryResponseType message
	 */
    @Override
	public PatientLocationQueryResponseType respondingGatewayPatientLocationQuery(
			RespondingGatewayPatientLocationQueryRequestType request,
			AssertionType assertion) {
		log.debug("Entering method respondingGatewayPatientLocationQuery...");
		PatientLocationQueryResponseType response = new PatientLocationQueryResponseType();
        PatientLocationQueryRequestType nhinRequest = request.getPatientLocationQueryRequest();
        List<UrlInfo> urlInfoList = getEndpoints(request.getNhinTargetCommunities());
        if (NullChecker.isNullish(urlInfoList)) {
            log.error("Error: entityPatientLocationQuery No NHIN endpoints for PatientLocationQuery service");
        } else {
            for (UrlInfo urlInfo : urlInfoList){
                log.debug("Processing entityPatientLocationQuery Target: " + urlInfo.getHcid());
                NhinTargetSystemType targetSystem = buildTargetSystem(urlInfo);
                response = sendPLQToNhin(nhinRequest,assertion,targetSystem);
            }
        }
        return response;
	}

	protected List<UrlInfo> getEndpoints(NhinTargetCommunitiesType targetCommunities){
    	List<UrlInfo> urlInfoList = null;

        try {
        	urlInfoList = ConnectionManagerCache.getInstance()
        			.getEndpointURLFromNhinTargetCommunities(targetCommunities, NhincConstants.PATIENT_DISCOVERY_SERVICE_NAME);
        } catch(ConnectionManagerException ex) {
            log.error("Method getEndpoints Failed to obtain target URLs", ex);
        }
        return urlInfoList;
    }
    
	/**
	 * Constructs and NhinTargetSystemType from the URL passed in by the calling method.
	 * @param urlInfo
	 * @return NhinTargetSystemType
	 */
    private NhinTargetSystemType buildTargetSystem(UrlInfo urlInfo){
        log.debug("Building the NhinTargetSystemType object...");
        NhinTargetSystemType result = new NhinTargetSystemType();
        HomeCommunityType hc = new HomeCommunityType();

        hc.setHomeCommunityId(urlInfo.getHcid());
        result.setHomeCommunity(hc);
        result.setUrl(urlInfo.getUrl());

        return result;
    }
    
    /**
	 * Sends the PatientLocationQueryRequest message to the Nhin Patient Discovery service for processing. The
	 * Nhin service takes the PatientLocationQueryRequestType and returns the PatientLocationQueryResponseType message.
	 * @param PatientLocationQueryRequestType nhinRequest
	 * @param assertion
	 * @param target
	 * @return PatientLocationQueryResponseType message
	 */
    protected PatientLocationQueryResponseType sendPLQToNhin(PatientLocationQueryRequestType nhinRequest, 
    		AssertionType assertion, NhinTargetSystemType target) {
        log.debug("Sending PatientLocationQueryRequest message to NhinPatientDiscoveryProxy...");
        PatientLocationQueryResponseType response = new PatientLocationQueryResponseType();
        NhinPatientDiscoveryProxy proxy = getNhinProxy();
        log.debug("Returning response...");
		try {
			response = proxy.respondingGatewayPatientLocationQuery(nhinRequest, assertion, target);
		} catch (Exception e) {
			log.error("The call to NhinPatientDiscoveryProxy was unsuccessful!!");
			e.printStackTrace();
		}
		return response;
    }

    /**
     * Returns an instance of the NhinPatientDiscoveryProxy. Protected method is used for mocking in unit tests.
     */
    protected NhinPatientDiscoveryProxy getNhinProxy(){
        return new NhinPatientDiscoveryProxyObjectFactory().getNhinPatientDiscoveryProxy();
    }
}
