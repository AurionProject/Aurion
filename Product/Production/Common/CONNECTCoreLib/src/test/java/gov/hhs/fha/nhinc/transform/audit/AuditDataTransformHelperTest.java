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
package gov.hhs.fha.nhinc.transform.audit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import gov.hhs.fha.nhinc.common.nhinccommon.HomeCommunityType;
import gov.hhs.fha.nhinc.common.nhinccommon.PersonNameType;
import gov.hhs.fha.nhinc.common.nhinccommon.SamlAuthnStatementType;
import gov.hhs.fha.nhinc.common.nhinccommon.UserType;
import gov.hhs.fha.nhinc.util.Base64Coder;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import oasis.names.tc.ebxml_regrep.xsd.rim._3.ExternalIdentifierType;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.services.nhinc.schema.auditmessage.AuditMessageType;
import com.services.nhinc.schema.auditmessage.AuditMessageType.ActiveParticipant;
import com.services.nhinc.schema.auditmessage.AuditSourceIdentificationType;
import com.services.nhinc.schema.auditmessage.CodedValueType;
import com.services.nhinc.schema.auditmessage.EventIdentificationType;
import com.services.nhinc.schema.auditmessage.ParticipantObjectIdentificationType;

/**
 * 
 * @author MFLYNN02
 */
public class AuditDataTransformHelperTest {
    
    public AuditDataTransformHelperTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of createEventIdentification method, of class AuditDataTransformHelper.
     */
    @Test
    public void testCreateEventIdentification() {
    	String actionCode = AuditDataTransformConstants.EVENT_ACTION_CODE_EXECUTE;
        Integer eventOutcome = AuditDataTransformConstants.EVENT_OUTCOME_INDICATOR_SUCCESS;
        
        CodedValueType eventId = new CodedValueType();
        eventId.setCode(AuditDataTransformConstants.DR_REQUEST_EVENT_ID_CODE);
        eventId.setCodeSystem(AuditDataTransformConstants.DR_REQUEST_EVENT_ID_CODE_SYSTEM);
        eventId.setCodeSystemName(AuditDataTransformConstants.DR_REQUEST_EVENT_ID_CODE_SYSTEM_NAME);
        eventId.setDisplayName(AuditDataTransformConstants.DR_REQUEST_EVENT_ID_CODE_SYSTEM_DISPLAY_NAME);

        CodedValueType eventType = new CodedValueType();
        eventType.setCode(AuditDataTransformConstants.DR_EVENT_TYPE_CODE);
        eventType.setCodeSystem(AuditDataTransformConstants.DR_EVENT_TYPE_CODE_SYSTEM);
        eventType.setCodeSystemName(AuditDataTransformConstants.DR_EVENT_TYPE_CODE_SYSTEM_NAME);
        eventType.setDisplayName(AuditDataTransformConstants.DR_EVENT_TYPE_CODE_SYSTEM_DISPLAY_NAME);
        
        EventIdentificationType expResult = new EventIdentificationType();
        expResult.setEventID(eventId);
        expResult.setEventActionCode(actionCode);

        EventIdentificationType result = AuditDataTransformHelper.createEventIdentification(actionCode, eventOutcome, eventId, eventType);

        assertSame(expResult.getEventID(), result.getEventID());
        assertSame(expResult.getEventActionCode(), result.getEventActionCode());
    }

    /**
     * Test of createEventId method, of class AuditDataTransformHelper.
     */
    @Test
    public void testCreateEventId() {
        String eventCode = AuditDataTransformConstants.PD_REQUEST_EVENT_ID_CODE;
        String eventCodeSys = AuditDataTransformConstants.PD_REQUEST_EVENT_ID_CODE_SYSTEM;
        String eventCodeSysName = AuditDataTransformConstants.PD_REQUEST_EVENT_ID_CODE_SYSTEM_NAME;
        String dispName = AuditDataTransformConstants.PD_REQUEST_EVENT_ID_CODE_SYSTEM_DISPLAY_NAME;
        String originalText = "foobar";
        
        CodedValueType expResult = new CodedValueType();
        expResult.setCode(eventCode);
        expResult.setCodeSystem(eventCodeSys);
        expResult.setCodeSystemName(eventCodeSysName);
        expResult.setDisplayName(dispName);
        expResult.setOriginalText(originalText);


        CodedValueType result = AuditDataTransformHelper.createCodedValue(eventCode, eventCodeSys, eventCodeSysName, dispName, originalText);

        assertSame(expResult.getCode(), result.getCode());
        assertSame(expResult.getCodeSystem(), result.getCodeSystem());
        assertSame(expResult.getCodeSystemName(), result.getCodeSystemName());
        assertSame(expResult.getDisplayName(), result.getDisplayName());
        assertSame(expResult.getOriginalText(), result.getOriginalText());

    }

    /**
     * Test of createActiveParticipantForHuman method, of class AuditDataTransformHelper.
     */
    @Test
    public void testCreateActiveParticipantFromAssertion() {
        String ipAddr = null;

        try {
            ipAddr = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException();
        }

        PersonNameType personName = new PersonNameType();
        personName.setFamilyName("Jones");
        personName.setGivenName("John");
        UserType userInfo = new UserType();
        userInfo.setPersonName(personName);
        userInfo.setUserName("tester");
        Boolean userIsReq = true;

        ActiveParticipant expResult = new ActiveParticipant();
        expResult.setUserName("John Jones");
        expResult.setUserID("tester");

        ActiveParticipant result = AuditDataTransformHelper.createActiveParticipantForHuman(userInfo);

        assertEquals(expResult.getUserID(), result.getUserID());
        assertEquals(expResult.getUserName(), result.getUserName());
    }

    /**
     * Test of createActiveParticipantSource method, of class AuditDataTransformHelper.
     */
    @Test
    public void testCreateActiveParticipantSource() {
    	boolean isSender = true;
    	
        ActiveParticipant expResult = new ActiveParticipant();
        expResult.setAlternativeUserID(AuditDataTransformHelper.getProcessId());
        expResult.setUserIsRequestor(true);
        expResult.getRoleIDCode().add(AuditDataTransformHelper.getActiveParticipantSourceRoleIdCode());
        expResult.setNetworkAccessPointTypeCode(AuditDataTransformConstants.NETWORK_ACCESS_POINT_TYPE_CODE_IP);

        //get IP address
        String ipAddr = "209.72.108.12";
        expResult.setNetworkAccessPointID(ipAddr);

        ActiveParticipant result = AuditDataTransformHelper.createActiveParticipantSource(isSender, ipAddr);

        assertEquals(expResult.isUserIsRequestor(), result.isUserIsRequestor());
        assertEquals(expResult.getAlternativeUserID(), result.getAlternativeUserID());
        
        assertEquals(expResult.getRoleIDCode().get(0).getCode(), result.getRoleIDCode().get(0).getCode());
        assertEquals(expResult.getRoleIDCode().get(0).getCodeSystem(), result.getRoleIDCode().get(0).getCodeSystem());
        assertEquals(expResult.getRoleIDCode().get(0).getCodeSystemName(), result.getRoleIDCode().get(0).getCodeSystemName());
        assertEquals(expResult.getRoleIDCode().get(0).getDisplayName(), result.getRoleIDCode().get(0).getDisplayName());
        
        assertEquals(expResult.getNetworkAccessPointTypeCode(), result.getNetworkAccessPointTypeCode());
        assertEquals(expResult.getNetworkAccessPointID(), result.getNetworkAccessPointID());
    }
    
    /**
     * Test of createActiveParticipantSource method, of class AuditDataTransformHelper.
     */
    @Test
    public void testCreateActiveParticipantDestination() {
    	boolean isRecipient = true;
    	String ipAddr = "60.148.12.109";
        ActiveParticipant expResult = new ActiveParticipant();
        expResult.setAlternativeUserID(AuditDataTransformHelper.getProcessId());
        expResult.setUserIsRequestor(false);
        expResult.getRoleIDCode().add(AuditDataTransformHelper.getActiveParticipantDestinationRoleIdCode());
        expResult.setNetworkAccessPointTypeCode(AuditDataTransformConstants.NETWORK_ACCESS_POINT_TYPE_CODE_IP);
        expResult.setNetworkAccessPointID(ipAddr);
        
        SamlAuthnStatementType authnStatement = new SamlAuthnStatementType();
        authnStatement.setSubjectLocalityAddress(ipAddr);
        ActiveParticipant result = AuditDataTransformHelper.createActiveParticipantDestination(authnStatement, isRecipient);

        assertEquals(expResult.isUserIsRequestor(), result.isUserIsRequestor());
        assertEquals(expResult.getAlternativeUserID(), result.getAlternativeUserID());
        
        assertEquals(expResult.getRoleIDCode().get(0).getCode(), result.getRoleIDCode().get(0).getCode());
        assertEquals(expResult.getRoleIDCode().get(0).getCodeSystem(), result.getRoleIDCode().get(0).getCodeSystem());
        assertEquals(expResult.getRoleIDCode().get(0).getCodeSystemName(), result.getRoleIDCode().get(0).getCodeSystemName());
        assertEquals(expResult.getRoleIDCode().get(0).getDisplayName(), result.getRoleIDCode().get(0).getDisplayName());
        
        assertEquals(expResult.getNetworkAccessPointTypeCode(), result.getNetworkAccessPointTypeCode());
        assertEquals(expResult.getNetworkAccessPointID(), result.getNetworkAccessPointID());
    }

    /**
     * Test of createAuditSourceIdentificationFromUser method, of class AuditDataTransformHelper.
     */
    @Test
    public void testCreateAuditSourceIdentificationFromAssertion() {
        HomeCommunityType home = new HomeCommunityType();
        home.setHomeCommunityId("2.16.840.1.113883.3.200");
        home.setName("Federal - VA");
        UserType userInfo = new UserType();
        userInfo.setOrg(home);

        AuditSourceIdentificationType expResult = new AuditSourceIdentificationType();
        expResult.setAuditSourceID(home.getHomeCommunityId());
        expResult.setAuditEnterpriseSiteID(home.getName());

        AuditSourceIdentificationType result = AuditDataTransformHelper.createAuditSourceIdentificationFromUser(userInfo, null);

        assertEquals(expResult.getAuditEnterpriseSiteID(), result.getAuditEnterpriseSiteID());
        assertEquals(expResult.getAuditSourceID(), result.getAuditSourceID());
    }

    /**
     * Test of createAuditSourceIdentification method, of class AuditDataTransformHelper.
     */
    @Test
    public void testCreateAuditSourceIdentification() {
        String communityId = "2.16.840.1.113883.3.198";
        String communityName = "Federal - DoD";

        AuditSourceIdentificationType expResult = new AuditSourceIdentificationType();
        expResult.setAuditSourceID(communityId);
        expResult.setAuditEnterpriseSiteID(communityName);

        AuditSourceIdentificationType result = AuditDataTransformHelper.createAuditSourceIdentification(communityId, communityName, null);

        assertEquals(expResult.getAuditEnterpriseSiteID(), result.getAuditEnterpriseSiteID());
        assertEquals(expResult.getAuditSourceID(), result.getAuditSourceID());
    }

    /**
     * Test of createParticipantObjectIdentification method, of class AuditDataTransformHelper.
     */
    @Test
    public void testCreateParticipantObjectIdentification() {
        String patientId = "44444";

        Short objectTypeCode = AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM;
        Short objectTypeCodeRole = AuditDataTransformConstants.PARTICIPANT_OBJECT_TYPE_CODE_ROLE_QUERY;
        CodedValueType  objectIdTypeCode = AuditDataTransformHelper.getDocumentParticipantRoleIdCodedValue();
        String ObjectId = patientId;
        byte[] objectQuery = {'a', 'b', 'c', '1', '2', '3'};
        
        ParticipantObjectIdentificationType expResult = new ParticipantObjectIdentificationType();
        expResult.setParticipantObjectTypeCode(objectTypeCode);
        expResult.setParticipantObjectTypeCodeRole(objectTypeCodeRole);
        expResult.setParticipantObjectIDTypeCode(objectIdTypeCode);
        expResult.setParticipantObjectID(ObjectId);
        //char[] objectQueryB64 = Base64Coder.encode(objectQuery);
        //byte[] objectQueryBytes = new String(objectQueryB64).getBytes();
        expResult.setParticipantObjectQuery(objectQuery);

        ParticipantObjectIdentificationType result = AuditDataTransformHelper.createParticipantObjectIdentification(
        		objectTypeCode, 
        		objectTypeCodeRole, 
        		objectIdTypeCode, 
        		patientId, 
        		objectQuery);

        assertEquals(expResult.getParticipantObjectTypeCode(), result.getParticipantObjectTypeCode());
        assertEquals(expResult.getParticipantObjectTypeCodeRole(), result.getParticipantObjectTypeCodeRole());
        assertEquals(expResult.getParticipantObjectIDTypeCode(), result.getParticipantObjectIDTypeCode());
        assertEquals(expResult.getParticipantObjectID(), result.getParticipantObjectID());
        int expectedArraySize = expResult.getParticipantObjectQuery().length;
        int resultArraySize = result.getParticipantObjectQuery().length;
        assertEquals(expectedArraySize, resultArraySize);
        byte[] expectedBytes = expResult.getParticipantObjectQuery();
        byte[] resultBytes = result.getParticipantObjectQuery();
        for (int i=0; i < expectedArraySize; i++) {
        	assertEquals(expectedBytes[i], resultBytes[i]);
        }
    }

    /**
     * Test of logAuditMessage method, of class AuditDataTransformHelper.
     */
    @Test
    public void testLogAuditMessage() {
        EventIdentificationType eventId = new EventIdentificationType();
        CodedValueType code = new CodedValueType();
        code.setCode(AuditDataTransformConstants.EVENT_ACTION_CODE_EXECUTE);
        code.setCodeSystem(AuditDataTransformConstants.EVENT_ID_CODE_SYS_NAME_SDN);
        eventId.setEventActionCode(AuditDataTransformConstants.EVENT_ACTION_CODE_EXECUTE);
        eventId.setEventID(code);

        ActiveParticipant participant = new ActiveParticipant();
        participant.setUserID("tester");

        AuditSourceIdentificationType sourceId = new AuditSourceIdentificationType();
        sourceId.setAuditSourceID("2.16.840.1.113883.3.198");
        sourceId.setAuditEnterpriseSiteID("Federal - DoD");

        AuditMessageType message = new AuditMessageType();
        message.setEventIdentification(eventId);
        message.getActiveParticipant().add(participant);
        message.getAuditSourceIdentification().add(sourceId);

        AuditDataTransformHelper.logAuditMessage(message);

    }

    /**
     * Test of findSingleExternalIdentifier method, of class AuditDataTransformHelper.
     */
    @Test
    public void testFindSingleExternalIdentifier() {
        ExternalIdentifierType extId1 = new ExternalIdentifierType();
        ExternalIdentifierType extId2 = new ExternalIdentifierType();
        extId1.setIdentificationScheme("2.16.840.1.113883.3.198");
        List<ExternalIdentifierType> olExtId = new ArrayList();
        olExtId.add(extId1);
        extId2.setIdentificationScheme("2.16.840.1.113883.3.200");
        olExtId.add(extId2);

        String sIdentScheme = "2.16.840.1.113883.3.198";
        ExternalIdentifierType expResult = new ExternalIdentifierType();
        expResult = extId1;
        ExternalIdentifierType result = AuditDataTransformHelper.findSingleExternalIdentifier(olExtId, sIdentScheme);
        assertEquals(expResult.getIdentificationScheme(), result.getIdentificationScheme());

    }

    /**
     * Test of findSingleExternalIdentifierAndExtractValue method, of class AuditDataTransformHelper.
     */
    @Test
    public void testFindSingleExternalIdentifierAndExtractValue() {
        ExternalIdentifierType extId1 = new ExternalIdentifierType();
        extId1.setIdentificationScheme("2.16.840.1.113883.3.198");
        extId1.setValue("198");

        ExternalIdentifierType extId2 = new ExternalIdentifierType();
        extId2.setIdentificationScheme("2.16.840.1.113883.3.200");
        extId2.setValue("200");

        List<ExternalIdentifierType> olExtId = new ArrayList();
        olExtId.add(extId1);
        olExtId.add(extId2);

        String sIdentScheme = "2.16.840.1.113883.3.198";
        String expResult = extId1.getValue();

        String result = AuditDataTransformHelper.findSingleExternalIdentifierAndExtractValue(olExtId, sIdentScheme);

        assertEquals(expResult, result);

    }

    /**
     * Test of createCompositePatientId method, of class AuditDataTransformHelper.
     */
    @Test
    public void testCreateCompositePatientId() {
        String communityId = "2.16.840.1.113883.3.200";
        String patientId = "12332";
        String expResult = patientId + "^^^&" + communityId + "&ISO";

        String result = AuditDataTransformHelper.createCompositePatientId(communityId, patientId);

        assertEquals(expResult, result);

    }

    /**
     * Test of createCompositePatientIdFromAssertion method, of class AuditDataTransformHelper.
     */
    @Test
    public void testCreateCompositePatientIdFromAssertion() {
        HomeCommunityType home = new HomeCommunityType();
        home.setHomeCommunityId("2.16.840.1.113883.3.200");
        home.setName("Federal - VA");
        UserType userInfo = new UserType();
        userInfo.setOrg(home);

        String uniquePatientId = "56765";

        String expResult = uniquePatientId + "^^^&" + userInfo.getOrg().getHomeCommunityId() + "&ISO";
        String result = AuditDataTransformHelper.createCompositePatientIdFromAssertion(userInfo, uniquePatientId);
        assertEquals(expResult, result);

    }

}