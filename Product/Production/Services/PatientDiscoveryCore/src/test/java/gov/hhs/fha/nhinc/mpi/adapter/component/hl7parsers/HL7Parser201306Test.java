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
package gov.hhs.fha.nhinc.mpi.adapter.component.hl7parsers;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.hl7.v3.EnExplicitFamily;
import org.hl7.v3.EnExplicitGiven;
import org.hl7.v3.EnExplicitPrefix;
import org.hl7.v3.EnExplicitSuffix;
import org.hl7.v3.II;
import org.hl7.v3.PNExplicit;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201306UV02;
import org.hl7.v3.PRPAMT201310UV02OtherIDs;
import org.hl7.v3.PRPAMT201310UV02Person;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import gov.hhs.fha.nhinc.mpi.adapter.component.TestHelper;
import gov.hhs.fha.nhinc.mpilib.Address;
import gov.hhs.fha.nhinc.mpilib.Identifier;
import gov.hhs.fha.nhinc.mpilib.Identifiers;
import gov.hhs.fha.nhinc.mpilib.Patient;
import gov.hhs.fha.nhinc.mpilib.Patients;
import gov.hhs.fha.nhinc.mpilib.PersonName;
import gov.hhs.fha.nhinc.mpilib.PhoneNumber;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.transform.subdisc.HL7Constants;

/**
 *
 * @author dunnek
 */
public class HL7Parser201306Test {

    //CHECKSTYLE:OFF
    private static class PatientName {
        public String FirstName = "";
        public String LastName = "";
        public String MiddleName = "";
        public String Title = "";
        public String Suffix = "";
    }

    /**
     * Public constructor for the test class.
     */
    public HL7Parser201306Test() {
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

    //CHECKSTYLE:ON
    /**
     * Test of BuildMessageFromMpiPatient method, of class HL7Parser201306.
     */
    @Test
    public void testBuildMessageFromMpiPatient() {
        System.out.println("BuildMessageFromMpiPatient");
        II subjectId = new II();
        subjectId.setRoot("2.16.840.1.113883.3.200");
        subjectId.setExtension("1234");

        String firstExpectedName = "Joe";
        String lastExpectedName = "Smith";
        String middleExpectedName = "Middle";
        String expectedTitle = "Title";
        String expectedSuffix = "Suffix";

        PRPAIN201305UV02 query = TestHelper.build201305(firstExpectedName, lastExpectedName, "M", "March 1, 1956",
                subjectId);

        Identifier patId = new Identifier();
        patId.setId("1234");
        patId.setOrganizationId("2.16.840.1.113883.3.200");
        Patient patient = TestHelper.createMpiPatient(firstExpectedName, lastExpectedName, middleExpectedName, "M",
                "March 1, 1956", patId);

        patient.getNames().get(0).setSuffix(expectedSuffix);
        patient.getNames().get(0).setTitle(expectedTitle);

        Patients patients = new Patients();
        patients.add(patient);

        PRPAIN201306UV02 result = HL7Parser201306.buildMessageFromMpiPatient(patients, query);

        PNExplicit pnResult = result.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1()
                .getPatient().getPatientPerson().getValue().getName().get(0);

        PatientName patientName = extractName(pnResult);

        assertEquals(lastExpectedName, patientName.LastName);
        assertEquals(firstExpectedName, patientName.FirstName);
        assertEquals(middleExpectedName, patientName.MiddleName);
        assertEquals(expectedTitle, patientName.Title);
        assertEquals(expectedSuffix, patientName.Suffix);

    }

    /**
     * Test Build Message From MPI Patient Phone Number.
     */
    @Test
    public void testBuildMessageFromMpiPatientPhoneNumber() {
        System.out.println("testBuildMessageFromMpiPatient_PhoneNumber");
        II subjectId = new II();
        subjectId.setRoot("2.16.840.1.113883.3.200");
        subjectId.setExtension("1234");

        String firstExpectedName = "Joe";
        String lastExpectedName = "Smith";
        String middleExpectedName = "Middle";
        String expectedTitle = "Title";
        String expectedSuffix = "Suffix";

        PRPAIN201305UV02 query = TestHelper.build201305(firstExpectedName, lastExpectedName, "M", "March 1, 1956",
                subjectId);

        Identifier patId = new Identifier();
        patId.setId("1234");
        patId.setOrganizationId("2.16.840.1.113883.3.200");
        Patient patient = TestHelper.createMpiPatient(firstExpectedName, lastExpectedName, middleExpectedName, "M",
                "March 1, 1956", patId);

        patient.getNames().get(0).setSuffix(expectedSuffix);
        patient.getNames().get(0).setTitle(expectedTitle);

        patient.getPhoneNumbers().add(new PhoneNumber("7031231234"));

        Patients patients = new Patients();
        patients.add(patient);

        PRPAIN201306UV02 result = HL7Parser201306.buildMessageFromMpiPatient(patients, query);

        PRPAMT201310UV02Person person = result.getControlActProcess().getSubject().get(0).getRegistrationEvent()
                .getSubject1().getPatient().getPatientPerson().getValue();

        assertEquals(1, person.getTelecom().size());
        assertEquals("7031231234", person.getTelecom().get(0).getValue());

    }

    /**
     * Test method for building a message from mpi when a patient has multiple phone numbers.
     */
    @Test
    public void testBuildMessageFromMpiPatientMultiPhoneNumber() {
        System.out.println("testBuildMessageFromMpiPatient_MultiPhoneNumber");
        II subjectId = new II();
        subjectId.setRoot("2.16.840.1.113883.3.200");
        subjectId.setExtension("1234");

        String firstExpectedName = "Joe";
        String lastExpectedName = "Smith";
        String middleExpectedName = "Middle";
        String expectedTitle = "Title";
        String expectedSuffix = "Suffix";

        PRPAIN201305UV02 query = TestHelper.build201305(firstExpectedName, lastExpectedName, "M", "March 1, 1956",
                subjectId);

        Identifier patId = new Identifier();
        patId.setId("1234");
        patId.setOrganizationId("2.16.840.1.113883.3.200");
        Patient patient = TestHelper.createMpiPatient(firstExpectedName, lastExpectedName, middleExpectedName, "M",
                "March 1, 1956", patId);

        patient.getNames().get(0).setSuffix(expectedSuffix);
        patient.getNames().get(0).setTitle(expectedTitle);

        patient.getPhoneNumbers().add(new PhoneNumber("7031231234"));
        patient.getPhoneNumbers().add(new PhoneNumber("2021231234"));

        Patients patients = new Patients();
        patients.add(patient);

        PRPAIN201306UV02 result = HL7Parser201306.buildMessageFromMpiPatient(patients, query);
        // TODO review the generated test code and remove the default call to fail.

        PRPAMT201310UV02Person person = result.getControlActProcess().getSubject().get(0).getRegistrationEvent()
                .getSubject1().getPatient().getPatientPerson().getValue();

        assertEquals(2, person.getTelecom().size());
        assertEquals("7031231234", person.getTelecom().get(0).getValue());
        assertEquals("2021231234", person.getTelecom().get(1).getValue());

    }

    /**
     *
     */
    @Test
    public void testBuildMessageFromMpiPatientAddress() {
        System.out.println("testBuildMessageFromMpiPatient_PhoneNumber");
        II subjectId = new II();
        subjectId.setRoot("2.16.840.1.113883.3.200");
        subjectId.setExtension("1234");

        String firstExpectedName = "Joe";
        String lastExpectedName = "Smith";
        String middleExpectedName = "Middle";
        String expectedTitle = "Title";
        String expectedSuffix = "Suffix";

        PRPAIN201305UV02 query = TestHelper.build201305(firstExpectedName, lastExpectedName, "M", "March 1, 1956",
                subjectId);

        Identifier patId = new Identifier();
        patId.setId("1234");
        patId.setOrganizationId("2.16.840.1.113883.3.200");
        Patient patient = TestHelper.createMpiPatient(firstExpectedName, lastExpectedName, middleExpectedName, "M",
                "March 1, 1956", patId);

        patient.getNames().get(0).setSuffix(expectedSuffix);
        patient.getNames().get(0).setTitle(expectedTitle);

        Address add = new Address();
        add.setCity("Chantilly");
        add.setState("VA");
        add.setStreet1("5155 Parkstone Drive");
        add.setStreet2("Att:Developer");
        add.setZip("20151");
        patient.getAddresses().add(add);

        Patients patients = new Patients();
        patients.add(patient);

        PRPAIN201306UV02 result = HL7Parser201306.buildMessageFromMpiPatient(patients, query);
        // TODO review the generated test code and remove the default call to fail.

        PRPAMT201310UV02Person person = result.getControlActProcess().getSubject().get(0).getRegistrationEvent()
                .getSubject1().getPatient().getPatientPerson().getValue();

        assertEquals(1, person.getAddr().size());

    }

    /**
     *
     */
    @Test
    public void testBuildMessageFromMpiPatientMultiAddress() {
        System.out.println("testBuildMessageFromMpiPatient_MultiAddress");
        II subjectId = new II();
        subjectId.setRoot("2.16.840.1.113883.3.200");
        subjectId.setExtension("1234");

        String firstExpectedName = "Joe";
        String lastExpectedName = "Smith";
        String middleExpectedName = "Middle";
        String expectedTitle = "Title";
        String expectedSuffix = "Suffix";

        PRPAIN201305UV02 query = TestHelper.build201305(firstExpectedName, lastExpectedName, "M", "March 1, 1956",
                subjectId);

        Identifier patId = new Identifier();
        patId.setId("1234");
        patId.setOrganizationId("2.16.840.1.113883.3.200");
        Patient patient = TestHelper.createMpiPatient(firstExpectedName, lastExpectedName, middleExpectedName, "M",
                "March 1, 1956", patId);

        patient.getNames().get(0).setSuffix(expectedSuffix);
        patient.getNames().get(0).setTitle(expectedTitle);

        Address add = new Address();
        add.setCity("Chantilly");
        add.setState("VA");
        add.setStreet1("5155 Parkstone Drive");
        add.setStreet2("Att:Developer");
        add.setZip("20151");

        Address add2 = new Address();
        add2.setCity("Melbourne");
        add2.setState("FL");
        add2.setStreet1("1025 West NASA Boulevard");
        add2.setStreet2("Att:Developer");
        add2.setZip("32919-0001");

        patient.getAddresses().add(add);
        patient.getAddresses().add(add2);

        Patients patients = new Patients();
        patients.add(patient);

        PRPAIN201306UV02 result = HL7Parser201306.buildMessageFromMpiPatient(patients, query);
        // TODO review the generated test code and remove the default call to fail.

        PRPAMT201310UV02Person person = result.getControlActProcess().getSubject().get(0).getRegistrationEvent()
                .getSubject1().getPatient().getPatientPerson().getValue();

        assertEquals(2, person.getAddr().size());

    }

    /**
     *
     */
    @Test
    public void testBuildMessageFromMpiPatientMultiNames() {
        System.out.println("BuildMessageFromMpiPatient");
        II subjectId = new II();
        subjectId.setRoot("2.16.840.1.113883.3.200");
        subjectId.setExtension("1234");

        String firstExpectedName = "Joe";
        String lastExpectedName = "Smith";
        String middleExpectedName = "Middle";
        String expectedTitle = "Title";
        String expectedSuffix = "Suffix";

        PRPAIN201305UV02 query = TestHelper.build201305(firstExpectedName, lastExpectedName, "M", "March 1, 1956",
                subjectId);

        Identifier patId = new Identifier();
        patId.setId("1234");
        patId.setOrganizationId("2.16.840.1.113883.3.200");
        Patient patient = TestHelper.createMpiPatient(firstExpectedName, lastExpectedName, middleExpectedName, "M",
                "March 1, 1956", patId, expectedTitle, expectedSuffix);

        patient.getNames().add(new PersonName("lastname", "firstName"));

        Patients patients = new Patients();
        patients.add(patient);

        PRPAIN201306UV02 result = HL7Parser201306.buildMessageFromMpiPatient(patients, query);

        assertEquals(2, result.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1()
                .getPatient().getPatientPerson().getValue().getName().size());

        PNExplicit pnResult = result.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1()
                .getPatient().getPatientPerson().getValue().getName().get(0);
        PNExplicit pnResult2 = result.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1()
                .getPatient().getPatientPerson().getValue().getName().get(1);

        PatientName patientName = extractName(pnResult);
        PatientName patientName2 = extractName(pnResult2);

        assertEquals(lastExpectedName, patientName.LastName);
        assertEquals(firstExpectedName, patientName.FirstName);
        assertEquals(middleExpectedName, patientName.MiddleName);
        assertEquals(expectedTitle, patientName.Title);
        assertEquals(expectedSuffix, patientName.Suffix);

        assertEquals("lastname", patientName2.LastName);
        assertEquals("firstName", patientName2.FirstName);

    }

    /**
     * Test of handling of additional IDs and SSN when calling BuildMessageFromMpiPatient method, of class HL7Parser201306.
     */
    @Test
    public void testBuildMessageWithSSNAndOtherIDs() {
        System.out.println("testBuildMessageWithSSNAndOtherIDs");
        II subjectId = new II();
        subjectId.setRoot("2.16.840.1.113883.3.200");
        subjectId.setExtension("1234");

        String firstExpectedName = "Joe";
        String lastExpectedName = "Smith";
        String middleExpectedName = "Middle";
        String expectedTitle = "Title";
        String expectedSuffix = "Suffix";

        PRPAIN201305UV02 query = TestHelper.build201305(firstExpectedName, lastExpectedName, "M", "March 1, 1956",
                subjectId);

        Identifiers ids = new Identifiers();

        // Add main ID
        //--------------
        Identifier patId = new Identifier();
        patId.setId("1234");
        patId.setOrganizationId("2.16.840.1.113883.3.200");
        ids.add(patId);
        
        // Add SSN
        //---------
        String ssn = "111111111";
        
        // Add other IDs
        //--------------
        Identifier otherId = new Identifier();
        otherId.setId("5555");
        otherId.setOrganizationId("5.5.5.5");
        ids.add(otherId);
        otherId = new Identifier();
        otherId.setId("6666");
        otherId.setOrganizationId("6.6.6.6");
        ids.add(otherId);
        
        Patient patient = TestHelper.createMpiPatient(firstExpectedName, lastExpectedName, middleExpectedName, "M",
                "March 1, 1956", ssn, ids);

        patient.getNames().get(0).setSuffix(expectedSuffix);
        patient.getNames().get(0).setTitle(expectedTitle);

        Patients patients = new Patients();
        patients.add(patient);

        PRPAIN201306UV02 result = HL7Parser201306.buildMessageFromMpiPatient(patients, query);
        
        assertNotNull(result);
        assertNotNull(result.getControlActProcess());
        assertNotNull(result.getControlActProcess().getSubject());
        assertEquals(1, result.getControlActProcess().getSubject().size());
        assertNotNull(result.getControlActProcess().getSubject().get(0));
        assertNotNull(result.getControlActProcess().getSubject().get(0).getRegistrationEvent());
        assertNotNull(result.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1());
        assertNotNull(result.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient());
        assertNotNull(result.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getPatientPerson());
        assertNotNull(result.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getPatientPerson().getValue());
        
        PRPAMT201310UV02Person person = result.getControlActProcess().getSubject().get(0).getRegistrationEvent().getSubject1().getPatient().getPatientPerson().getValue();
        
        assertNotNull(person.getName());
        assertEquals(1, person.getName().size());
        assertNotNull(person.getName().get(0));

        PNExplicit pnResult = person.getName().get(0);

        // Verify the name
        //----------------
        PatientName patientName = extractName(pnResult);
        assertEquals(lastExpectedName, patientName.LastName);
        assertEquals(firstExpectedName, patientName.FirstName);
        assertEquals(middleExpectedName, patientName.MiddleName);
        assertEquals(expectedTitle, patientName.Title);
        assertEquals(expectedSuffix, patientName.Suffix);
        
        // Check the IDs to make sure they ended up in the right places..
        //----------------------------------------------------------------
        assertNotNull(person.getAsOtherIDs());
        assertEquals(4, person.getAsOtherIDs().size());
        
        boolean bFoundMainId = false;
        boolean bFoundSSN = false;
        boolean bFoundOtherId1 = false;
        boolean bFoundOtherId2 = false;
        
        for (PRPAMT201310UV02OtherIDs asOtherId : person.getAsOtherIDs()) {
        	assertNotNull(asOtherId.getId());
        	assertEquals(1, asOtherId.getId().size());
        	assertNotNull(asOtherId.getId().get(0));
        	assertNotNull(asOtherId.getId().get(0).getExtension());
        	String sIdExtension = asOtherId.getId().get(0).getExtension();
        	assertNotNull(asOtherId.getScopingOrganization());
        	assertNotNull(asOtherId.getScopingOrganization().getId());
        	assertEquals(1, asOtherId.getScopingOrganization().getId().size());
        	assertNotNull(asOtherId.getScopingOrganization().getId().get(0));
        	
        	String sOrgOid = asOtherId.getScopingOrganization().getId().get(0).getRoot();
        	
        	if (NullChecker.isNotNullish(sOrgOid)) {
        		if ((sOrgOid.equals(HL7Constants.SSN_ID_ROOT)) &&
        		    (sIdExtension.equals("111111111"))){
        			bFoundSSN = true;
        		}
        		else if ((sOrgOid.equals("2.16.840.1.113883.3.200")) &&
            		     (sIdExtension.equals("1234"))) {
        			bFoundMainId = true;
        		}
        		else if ((sOrgOid.equals("5.5.5.5")) &&
           		         (sIdExtension.equals("5555"))) {
	       			bFoundOtherId1 = true;
	       		}
        		else if ((sOrgOid.equals("6.6.6.6")) &&
          		         (sIdExtension.equals("6666"))) {
	       			bFoundOtherId2 = true;
	       		}
        	}
        }
        
        assertTrue("Failed to find Main ID in the list.", bFoundMainId);
        assertTrue("Failed to find SSN in the list.", bFoundSSN);
        assertTrue("Failed to find Other ID 1 in the list.", bFoundOtherId1);
        assertTrue("Failed to find Other ID 2 in the list.", bFoundOtherId2);

    }

    
    private static PatientName extractName(PNExplicit name) {
        String nameString = "";
        Boolean hasName = false;
        PatientName result = new PatientName();
        List<Serializable> choice = name.getContent();
        Iterator<Serializable> iterSerialObjects = choice.iterator();

        EnExplicitFamily familyName = new EnExplicitFamily();
        EnExplicitGiven givenName = new EnExplicitGiven();

        while (iterSerialObjects.hasNext()) {
            Serializable contentItem = iterSerialObjects.next();

            if (contentItem instanceof JAXBElement) {
                JAXBElement<?> oJAXBElement = (JAXBElement<?>) contentItem;
                if (oJAXBElement.getValue() instanceof EnExplicitFamily) {
                    familyName = (EnExplicitFamily) oJAXBElement.getValue();
                    result.LastName = familyName.getContent();
                    hasName = true;
                } else if (oJAXBElement.getValue() instanceof EnExplicitGiven) {
                    givenName = (EnExplicitGiven) oJAXBElement.getValue();
                    if (result.FirstName == "") {
                        result.FirstName = givenName.getContent();
                    } else {
                        result.MiddleName = givenName.getContent();
                    }
                    hasName = true;
                } else if (oJAXBElement.getValue() instanceof EnExplicitPrefix) {
                    EnExplicitPrefix prefix = (EnExplicitPrefix) oJAXBElement.getValue();
                    result.Title = prefix.getContent();
                } else if (oJAXBElement.getValue() instanceof EnExplicitSuffix) {
                    EnExplicitSuffix suffix = (EnExplicitSuffix) oJAXBElement.getValue();
                    result.Suffix = suffix.getContent();
                }
            }
        }

        if (hasName) {
            nameString = familyName.getContent();
            System.out.println(nameString);
        }

        return result;
    }

}