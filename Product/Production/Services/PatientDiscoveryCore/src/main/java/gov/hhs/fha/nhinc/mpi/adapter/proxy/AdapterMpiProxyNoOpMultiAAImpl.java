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
package gov.hhs.fha.nhinc.mpi.adapter.proxy;

import gov.hhs.fha.nhinc.aspect.AdapterDelegationEvent;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.patientdiscovery.aspect.PRPAIN201305UV02EventDescriptionBuilder;
import gov.hhs.fha.nhinc.patientdiscovery.aspect.PRPAIN201306UV02EventDescriptionBuilder;
import gov.hhs.fha.nhinc.transform.marshallers.Marshaller;

import org.apache.log4j.Logger;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201306UV02;

/**
 * NoOp Implementation for the AdapterMpi component proxy.
 * 
 * @author Les Westberg
 */
public class AdapterMpiProxyNoOpMultiAAImpl implements AdapterMpiProxy {

	private static final Logger LOG = Logger.getLogger(AdapterMpiProxyNoOpMultiAAImpl.class);
    private static final String HL7_V3_CONTEXT = "org.hl7.v3";
    private static final String RESPONSE_MESSAGE_XML = 
    "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
    "<PRPA_IN201306UV02 xmlns=\"urn:hl7-org:v3\" xmlns:ns2=\"urn:gov:hhs:fha:nhinc:common:nhinccommon\" xmlns:ns3=\"http://www.w3.org/2005/08/addressing\" xmlns:ns4=\"urn:gov:hhs:fha:nhinc:common:patientcorrelationfacade\" ITSVersion=\"XML_1.0\">" +
    "  <id root=\"1.1\" extension=\"4f3ece0e:13e8f54aab1:-7ff6\"/>" +
    "  <creationTime value=\"2013510164619\"/>" +
    "  <interactionId root=\"2.16.840.1.113883.1.6\" extension=\"PRPA_IN201306UV02\"/>" +
    "  <processingCode code=\"P\"/>" +
    "  <processingModeCode code=\"T\"/>" +
    "  <acceptAckCode code=\"NE\"/>" +
    "  <receiver typeCode=\"RCV\">" +
    "    <device classCode=\"DEV\" determinerCode=\"INSTANCE\">" +
    "      <id root=\"2.2\"/>" +
    "      <asAgent classCode=\"AGNT\">" +
    "        <representedOrganization classCode=\"ORG\" determinerCode=\"INSTANCE\">" +
    "          <id root=\"2.2\"/>" +
    "        </representedOrganization>" +
    "      </asAgent>" +
    "    </device>" +
    "  </receiver>" +
    "  <sender typeCode=\"SND\">" +
    "    <device classCode=\"DEV\" determinerCode=\"INSTANCE\">" +
    "      <id root=\"1.1\"/>" +
    "      <asAgent classCode=\"AGNT\">" +
    "        <representedOrganization classCode=\"ORG\" determinerCode=\"INSTANCE\">" +
    "          <id root=\"1.1\"/>" +
    "        </representedOrganization>" +
    "      </asAgent>" +
    "    </device>" +
    "  </sender>" +
    "  <acknowledgement>" +
    "    <typeId root=\"2.16.840.1.113883.1.6\" extension=\"PRPA_IN201305UV02\"/>" +
    "    <typeCode code=\"AA\"/>" +
    "    <targetMessage xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>" +
    "  </acknowledgement>" +
    "  <controlActProcess classCode=\"CACT\" moodCode=\"EVN\">" +
    "    <code code=\"PRPA_TE201306UV\" codeSystem=\"2.16.840.1.113883.1.6\"/>" +
    "    <subject typeCode=\"SUBJ\">" +
    "      <registrationEvent classCode=\"REG\" moodCode=\"EVN\">" +
    "        <id nullFlavor=\"NA\"/>" +
    "        <statusCode code=\"active\"/>" +
    "        <subject1 typeCode=\"SBJ\">" +
    "          <patient classCode=\"PAT\">" +
    "            <id root=\"1.1\" extension=\"D123401\"/>" +
    "            <statusCode code=\"SD\"/>" +
    "            <patientPerson classCode=\"PSN\" determinerCode=\"INSTANCE\">" +
    "              <name>" +
    "                <family partType=\"FAM\">Younger</family>" +
    "                <given partType=\"GIV\">Gallow</given>" +
    "              </name>" +
    "              <telecom value=\"tel:+1-888-555-1234\"/>" +
    "              <administrativeGenderCode code=\"M\"/>" +
    "              <birthTime value=\"19630804\"/>" +
    "              <addr>" +
    "                <city>LEESBURG</city>" +
    "                <state>VA</state>" +
    "                <postalCode>20176</postalCode>" +
    "              </addr>" +
    "              <asOtherIDs classCode=\"SD\">" +
    "                <id root=\"2.16.840.1.113883.4.1\" extension=\"999123401\"/>" +
    "                <scopingOrganization classCode=\"ORG\" determinerCode=\"INSTANCE\">" +
    "                  <id root=\"2.16.840.1.113883.4.1\"/>" +
    "                </scopingOrganization>" +
    "              </asOtherIDs>" +
    "              <asOtherIDs classCode=\"SD\">" +
    "                <id root=\"1.1\" extension=\"D123401\"/>" +
    "                <scopingOrganization>" +
    "                  <id root=\"1.1\"/>" +
    "                </scopingOrganization>" +
    "              </asOtherIDs>" +
    "              <asOtherIDs classCode=\"SD\">" +
    "                <id root=\"2.2\" extension=\"500000000\"/>" +
    "                <scopingOrganization>" +
    "                  <id root=\"2.2\"/>" +
    "                </scopingOrganization>" +
    "              </asOtherIDs>" +
    "              <asOtherIDs classCode=\"SD\">" +
    "                <id root=\"4.4\" extension=\"D123401\"/>" +
    "                <scopingOrganization>" +
    "                  <id root=\"4.4\"/>" +
    "                </scopingOrganization>" +
    "              </asOtherIDs>" +
    "            </patientPerson>" +
    "            <providerOrganization classCode=\"ORG\" determinerCode=\"INSTANCE\">" +
    "              <id root=\"1.1\"/>" +
    "              <contactParty xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>" +
    "            </providerOrganization>" +
    "            <subjectOf1>" +
    "              <queryMatchObservation classCode=\"CASE\" moodCode=\"EVN\">" +
    "                <code code=\"IHE_PDQ\"/>" +
    "                <value xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"INT\" value=\"100\"/>" +
    "              </queryMatchObservation>" +
    "            </subjectOf1>" +
    "          </patient>" +
    "        </subject1>" +
    "        <custodian typeCode=\"CST\">" +
    "          <assignedEntity classCode=\"ASSIGNED\">" +
    "            <id root=\"1.1\"/>" +
    "            <code code=\"NotHealthDataLocator\" codeSystem=\"1.3.6.1.4.1.19376.1.2.27.2\"/>" +
    "          </assignedEntity>" +
    "        </custodian>" +
    "      </registrationEvent>" +
    "    </subject>" +
    "    <subject typeCode=\"SUBJ\">" +
    "      <registrationEvent classCode=\"REG\" moodCode=\"EVN\">" +
    "        <id nullFlavor=\"NA\"/>" +
    "        <statusCode code=\"active\"/>" +
    "        <subject1 typeCode=\"SBJ\">" +
    "          <patient classCode=\"PAT\">" +
    "            <id root=\"1.1.1\" extension=\"D123401.1\"/>" +
    "            <statusCode code=\"SD\"/>" +
    "            <patientPerson classCode=\"PSN\" determinerCode=\"INSTANCE\">" +
    "              <name>" +
    "                <family partType=\"FAM\">Younger</family>" +
    "                <given partType=\"GIV\">Gallow</given>" +
    "              </name>" +
    "              <telecom value=\"tel:+1-888-555-1234\"/>" +
    "              <administrativeGenderCode code=\"M\"/>" +
    "              <birthTime value=\"19630804\"/>" +
    "              <addr>" +
    "                <city>LEESBURG</city>" +
    "                <state>VA</state>" +
    "                <postalCode>20176</postalCode>" +
    "              </addr>" +
    "              <asOtherIDs classCode=\"SD\">" +
    "                <id root=\"2.16.840.1.113883.4.1\" extension=\"999123401\"/>" +
    "                <scopingOrganization classCode=\"ORG\" determinerCode=\"INSTANCE\">" +
    "                  <id root=\"2.16.840.1.113883.4.1\"/>" +
    "                </scopingOrganization>" +
    "              </asOtherIDs>" +
    "              <asOtherIDs classCode=\"SD\">" +
    "                <id root=\"1.1\" extension=\"D123401\"/>" +
    "                <scopingOrganization>" +
    "                  <id root=\"1.1\"/>" +
    "                </scopingOrganization>" +
    "              </asOtherIDs>" +
    "              <asOtherIDs classCode=\"SD\">" +
    "                <id root=\"2.2\" extension=\"500000000\"/>" +
    "                <scopingOrganization>" +
    "                  <id root=\"2.2\"/>" +
    "                </scopingOrganization>" +
    "              </asOtherIDs>" +
    "              <asOtherIDs classCode=\"SD\">" +
    "                <id root=\"4.4\" extension=\"D123401\"/>" +
    "                <scopingOrganization>" +
    "                  <id root=\"4.4\"/>" +
    "                </scopingOrganization>" +
    "              </asOtherIDs>" +
    "            </patientPerson>" +
    "            <providerOrganization classCode=\"ORG\" determinerCode=\"INSTANCE\">" +
    "              <id root=\"1.1\"/>" +
    "              <contactParty xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>" +
    "            </providerOrganization>" +
    "            <subjectOf1>" +
    "              <queryMatchObservation classCode=\"CASE\" moodCode=\"EVN\">" +
    "                <code code=\"IHE_PDQ\"/>" +
    "                <value xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"INT\" value=\"100\"/>" +
    "              </queryMatchObservation>" +
    "            </subjectOf1>" +
    "          </patient>" +
    "        </subject1>" +
    "        <custodian typeCode=\"CST\">" +
    "          <assignedEntity classCode=\"ASSIGNED\">" +
    "            <id root=\"1.1\"/>" +
    "            <code code=\"NotHealthDataLocator\" codeSystem=\"1.3.6.1.4.1.19376.1.2.27.2\"/>" +
    "          </assignedEntity>" +
    "        </custodian>" +
    "      </registrationEvent>" +
    "    </subject>" +
    "    <subject typeCode=\"SUBJ\">" +
    "      <registrationEvent classCode=\"REG\" moodCode=\"EVN\">" +
    "        <id nullFlavor=\"NA\"/>" +
    "        <statusCode code=\"active\"/>" +
    "        <subject1 typeCode=\"SBJ\">" +
    "          <patient classCode=\"PAT\">" +
    "            <id root=\"1.1.2\" extension=\"D123401.2\"/>" +
    "            <statusCode code=\"SD\"/>" +
    "            <patientPerson classCode=\"PSN\" determinerCode=\"INSTANCE\">" +
    "              <name>" +
    "                <family partType=\"FAM\">Younger</family>" +
    "                <given partType=\"GIV\">Gallow</given>" +
    "              </name>" +
    "              <telecom value=\"tel:+1-888-555-1234\"/>" +
    "              <administrativeGenderCode code=\"M\"/>" +
    "              <birthTime value=\"19630804\"/>" +
    "              <addr>" +
    "                <city>LEESBURG</city>" +
    "                <state>VA</state>" +
    "                <postalCode>20176</postalCode>" +
    "              </addr>" +
    "              <asOtherIDs classCode=\"SD\">" +
    "                <id root=\"2.16.840.1.113883.4.1\" extension=\"999123401\"/>" +
    "                <scopingOrganization classCode=\"ORG\" determinerCode=\"INSTANCE\">" +
    "                  <id root=\"2.16.840.1.113883.4.1\"/>" +
    "                </scopingOrganization>" +
    "              </asOtherIDs>" +
    "              <asOtherIDs classCode=\"SD\">" +
    "                <id root=\"1.1\" extension=\"D123401\"/>" +
    "                <scopingOrganization>" +
    "                  <id root=\"1.1\"/>" +
    "                </scopingOrganization>" +
    "              </asOtherIDs>" +
    "              <asOtherIDs classCode=\"SD\">" +
    "                <id root=\"2.2\" extension=\"500000000\"/>" +
    "                <scopingOrganization>" +
    "                  <id root=\"2.2\"/>" +
    "                </scopingOrganization>" +
    "              </asOtherIDs>" +
    "              <asOtherIDs classCode=\"SD\">" +
    "                <id root=\"4.4\" extension=\"D123401\"/>" +
    "                <scopingOrganization>" +
    "                  <id root=\"4.4\"/>" +
    "                </scopingOrganization>" +
    "              </asOtherIDs>" +
    "            </patientPerson>" +
    "            <providerOrganization classCode=\"ORG\" determinerCode=\"INSTANCE\">" +
    "              <id root=\"1.1\"/>" +
    "              <contactParty xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>" +
    "            </providerOrganization>" +
    "            <subjectOf1>" +
    "              <queryMatchObservation classCode=\"CASE\" moodCode=\"EVN\">" +
    "                <code code=\"IHE_PDQ\"/>" +
    "                <value xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"INT\" value=\"100\"/>" +
    "              </queryMatchObservation>" +
    "            </subjectOf1>" +
    "          </patient>" +
    "        </subject1>" +
    "        <custodian typeCode=\"CST\">" +
    "          <assignedEntity classCode=\"ASSIGNED\">" +
    "            <id root=\"1.1\"/>" +
    "            <code code=\"NotHealthDataLocator\" codeSystem=\"1.3.6.1.4.1.19376.1.2.27.2\"/>" +
    "          </assignedEntity>" +
    "        </custodian>" +
    "      </registrationEvent>" +
    "    </subject>" +
    "    <queryAck>" +
    "      <queryId root=\"1.1\" extension=\"4f3ece0e:13e8f54aab1:-7ff7\"/>" +
    "      <queryResponseCode code=\"OK\"/>" +
    "    </queryAck>" +
    "    <queryByParameter>" +
    "      <queryId root=\"1.1\" extension=\"4f3ece0e:13e8f54aab1:-7ff7\"/>" +
    "      <statusCode code=\"new\"/>" +
    "      <responseModalityCode code=\"R\"/>" +
    "      <responsePriorityCode code=\"I\"/>" +
    "      <parameterList>" +
    "        <livingSubjectAdministrativeGender>" +
    "          <value code=\"M\"/>" +
    "          <semanticsText/>" +
    "        </livingSubjectAdministrativeGender>" +
    "        <livingSubjectBirthTime>" +
    "          <value value=\"19630804\"/>" +
    "          <semanticsText/>" +
    "        </livingSubjectBirthTime>" +
    "        <livingSubjectId>" +
    "          <value root=\"2.2\" extension=\"500000000\"/>" +
    "          <semanticsText/>" +
    "        </livingSubjectId>" +
    "        <livingSubjectId>" +
    "          <value root=\"2.16.840.1.113883.4.1\" extension=\"999123401\"/>" +
    "          <semanticsText/>" +
    "        </livingSubjectId>" +
    "        <livingSubjectId>" +
    "          <value root=\"1.1\" extension=\"D123401\"/>" +
    "          <semanticsText/>" +
    "        </livingSubjectId>" +
    "        <livingSubjectId>" +
    "          <value root=\"2.2\" extension=\"500000000\"/>" +
    "          <semanticsText/>" +
    "        </livingSubjectId>" +
    "        <livingSubjectId>" +
    "          <value root=\"4.4\" extension=\"D123401\"/>" +
    "          <semanticsText/>" +
    "        </livingSubjectId>" +
    "        <livingSubjectName>" +
    "          <value>" +
    "            <family partType=\"FAM\">Younger</family>" +
    "            <given partType=\"GIV\">Gallow</given>" +
    "          </value>" +
    "          <semanticsText/>" +
    "        </livingSubjectName>" +
    "        <patientAddress>" +
    "          <value>" +
    "            <city>LEESBURG</city>" +
    "            <state>VA</state>" +
    "            <postalCode>20176</postalCode>" +
    "          </value>" +
    "          <semanticsText/>" +
    "        </patientAddress>" +
    "        <patientTelecom>" +
    "          <value value=\"tel:+1-888-555-1234\"/>" +
    "          <semanticsText/>" +
    "        </patientTelecom>" +
    "      </parameterList>" +
    "    </queryByParameter>" +
    "  </controlActProcess>" +
    "</PRPA_IN201306UV02>";
    
	
    /**
     * Find the matching candidates from the MPI.
     * 
     * @param findCandidatesRequest
     *            The information to use for matching.
     * @param assertion
     *            The assertion data.
     * @return The matches that are found.
     */
    @Override
    @AdapterDelegationEvent(beforeBuilder = PRPAIN201305UV02EventDescriptionBuilder.class,
            afterReturningBuilder = PRPAIN201306UV02EventDescriptionBuilder.class,
            serviceType = "Patient Discovery MPI", version = "1.0")
    public PRPAIN201306UV02 findCandidates(PRPAIN201305UV02 findCandidatesRequest, AssertionType assertion) {
    	LOG.info("AdapterMpiProxyNoOpMultiAAImpl: AdapterMpi running with Noop Multiple Assigning Authorities - " +
                  "Returning canned response containing multiple assigning authority/ID pairs.");
    	PRPAIN201306UV02 oResponse = null;
    	oResponse = createResponse();
        return oResponse;
    }
    
    private PRPAIN201306UV02 createResponse() {
        Marshaller marshaller = new Marshaller();
        
        return (PRPAIN201306UV02) marshaller.unmarshal(RESPONSE_MESSAGE_XML, HL7_V3_CONTEXT);
    	
    }
}
