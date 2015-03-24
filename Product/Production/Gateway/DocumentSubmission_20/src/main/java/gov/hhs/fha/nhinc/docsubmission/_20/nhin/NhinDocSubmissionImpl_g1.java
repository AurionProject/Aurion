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
package gov.hhs.fha.nhinc.docsubmission._20.nhin;

import java.io.InputStream;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.docsubmission.inbound.InboundDocSubmission;
import gov.hhs.fha.nhinc.messaging.server.BaseService;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType.Document;

import javax.xml.ws.WebServiceContext;

import org.apache.log4j.Logger;

import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;

/**
 *
 * @author dunnek
 */
public class NhinDocSubmissionImpl_g1 extends BaseService {

    private static final Logger LOG = Logger.getLogger(NhinDocSubmissionImpl_g1.class);
    private InboundDocSubmission inboundDocSubmission;
    
    public NhinDocSubmissionImpl_g1(InboundDocSubmission inboundDocSubmission) {
        this.inboundDocSubmission = inboundDocSubmission;
    }
    
    public RegistryResponseType documentRepositoryProvideAndRegisterDocumentSetB(
            ProvideAndRegisterDocumentSetRequestType body, WebServiceContext context) {
        AssertionType assertion = getAssertion(context, null);
        resetDocumentInputStreams(body);
        return inboundDocSubmission.documentRepositoryProvideAndRegisterDocumentSetB(body, assertion);
    }

    /**
     * Workaround for large documents being dropped after received. Unsure why this solves the problem.
     * This was discovered when attempting to log the message at several different locations in the processing
     * flow.
     * 
     * @param body Document submission request message
     */
	private void resetDocumentInputStreams(ProvideAndRegisterDocumentSetRequestType body) {
		try {
			if((body != null) && NullChecker.isNotNullish(body.getDocument())) {
				for(Document document : body.getDocument()) {
					if((document != null) && (document.getValue() != null)) {
						InputStream is = document.getValue().getInputStream();
						if(is != null) {
							if (is.markSupported()) {
								is.reset();
							}
						}
					}
				}
			}
		} catch(Throwable t) {
			LOG.error("Exception encountered reseting inbound document submission document input streams (did not cause processing failure): " + t.getMessage(), t);
		}
		
	}
	
}
