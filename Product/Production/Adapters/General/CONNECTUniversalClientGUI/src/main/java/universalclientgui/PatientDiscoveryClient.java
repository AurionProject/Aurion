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

package universalclientgui;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetCommunitiesType;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerCache;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerException;
import gov.hhs.fha.nhinc.mpi.adapter.component.proxy.AdapterComponentMpiProxy;
import gov.hhs.fha.nhinc.mpi.adapter.component.proxy.AdapterComponentMpiProxyObjectFactory;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.patientdiscovery.entity.proxy.EntityPatientDiscoveryProxyWebServiceUnsecuredImpl;
import gov.hhs.fha.nhinc.properties.PropertyAccessException;
import gov.hhs.fha.nhinc.properties.PropertyAccessor;
import gov.hhs.fha.nhinc.transform.subdisc.HL7PRPA201305Transforms;
import gov.hhs.fha.nhinc.transform.subdisc.HL7PatientTransforms;
import gov.hhs.fha.nhinc.webserviceproxy.WebServiceProxyHelper;

import javax.xml.bind.JAXBElement;

import org.apache.log4j.Logger;
import org.hl7.v3.II;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201306UV02;
import org.hl7.v3.PRPAMT201301UV02Patient;
import org.hl7.v3.PRPAMT201301UV02Person;
import org.hl7.v3.RespondingGatewayPRPAIN201305UV02RequestType;
import org.hl7.v3.PRPAIN201306UV02MFMIMT700711UV01Subject1;
import org.hl7.v3.PRPAMT201310UV02Patient;
import org.hl7.v3.PRPAMT201310UV02Person;



/**
 * 
 * @author patlollav
 */
public class PatientDiscoveryClient {

    private static final String PROPERTY_FILE_NAME_ADAPTER = "adapter";
    private static final String PROPERTY_FILE_NAME_GATEWAY = "gateway";
    private static final String PROPERTY_FILE_KEY_LOCAL_DEVICE = "localDeviceId";
    private static final String PROPERTY_FILE_KEY_HOME_COMMUNITY = "localHomeCommunityId";
    private static final String PROPERTY_FILE_KEY_ASSIGN_AUTH = "assigningAuthorityId";
    private static final String SERVICE_NAME = NhincConstants.ENTITY_PATIENT_DISCOVERY_SERVICE_NAME;

    private static final Logger LOG = Logger.getLogger(PatientDiscoveryClient.class);
   
    /**
     * Retrieve the local home community id
     * 
     * @return Local home community id
     * 
     * @throws gov.hhs.fha.nhinc.properties.PropertyAccessException
     */

    private String getHomeCommunityId() throws PropertyAccessException {
        return PropertyAccessor.getInstance().getProperty(PROPERTY_FILE_NAME_GATEWAY, PROPERTY_FILE_KEY_HOME_COMMUNITY);
    }

    protected WebServiceProxyHelper createWebServiceProxyHelper() {
        return new WebServiceProxyHelper();
    }

    protected String getEndpointURL() throws ConnectionManagerException, PropertyAccessException {
        return ConnectionManagerCache.getInstance().getInternalEndpointURLByServiceName(SERVICE_NAME);
    }

    /**
     * 
     * @param assertion
     * @param patientSearchData
     */
    public void broadcastPatientDiscovery(AssertionType assertion, PatientSearchData patientSearchData) {

        try {

            RespondingGatewayPRPAIN201305UV02RequestType request = new RespondingGatewayPRPAIN201305UV02RequestType();
            NhinTargetCommunitiesType target = new NhinTargetCommunitiesType();
            request.setAssertion(assertion);
            request.setNhinTargetCommunities(target);

            String orgId = getHomeCommunityId();

            PRPAIN201305UV02 request201305 = this.create201305(patientSearchData, orgId);

            request.setPRPAIN201305UV02(request201305);

            String url = getEndpointURL();
            if (NullChecker.isNotNullish(url)) {
             
            	EntityPatientDiscoveryProxyWebServiceUnsecuredImpl instance = new EntityPatientDiscoveryProxyWebServiceUnsecuredImpl();
                instance.respondingGatewayPRPAIN201305UV02(request201305, assertion, request.getNhinTargetCommunities());
            } else {
                LOG.error("Error getting URL for: " + SERVICE_NAME + "url is null");
            }

        } catch (Exception ex) {
            LOG.error("Exception in patient discovery", ex);
        }
    }

    /**
     * 
     * @param first
     * @param last
     * @param gender
     * @param birthdate
     * @param ssn
     * @param senderOID
     * @param receiverOID
     * @return
     */
    public PRPAIN201305UV02 create201305(PatientSearchData patientSearchData, String receiverOID) {
        PRPAIN201305UV02 resp = new PRPAIN201305UV02();

        String localDeviceId = null;

        try {
            localDeviceId = PropertyAccessor.getInstance().getProperty(PROPERTY_FILE_NAME_GATEWAY,
                    PROPERTY_FILE_KEY_LOCAL_DEVICE);
        } catch (PropertyAccessException ex) {
            LOG.error(ex);
        }

        JAXBElement<PRPAMT201301UV02Person> person = HL7PatientTransforms.create201301PatientPerson(
                patientSearchData.getFirstName(), patientSearchData.getLastName(), patientSearchData.getGender(),
                patientSearchData.getDob(), patientSearchData.getSsn());
        // PRPAMT201301UV02Patient patient = HL7PatientTransforms.create201301Patient(person,
        // patientSearchData.getPatientId(), localDeviceId);
        PRPAMT201301UV02Patient patient = HL7PatientTransforms.create201301Patient(person,
                patientSearchData.getPatientId(), patientSearchData.getAssigningAuthorityID());

        // We should have this - but lets be sure.
        //-----------------------------------------
        if ((patient != null) &&
            (patient.getPatientPerson() != null) &&
            (patient.getPatientPerson().getValue() != null))
        {
            retrieveAndFillInAddrAndPhone(patientSearchData, patient.getPatientPerson().getValue());
        }        
        
        // resp = HL7PRPA201305Transforms.createPRPA201305(patient, patientSearchData.getAssigningAuthorityID(),
        // receiverOID, localDeviceId);
        resp = HL7PRPA201305Transforms.createPRPA201305(patient, localDeviceId, receiverOID,
                patientSearchData.getAssigningAuthorityID());

        return resp;
    }
    
    /**
     * This method is a quick fix and is used to retrieve the patient data again from the system so that we have all of the
     * patient data to be included in the patient discovery request.  Otherwise it only sends the last name. first name, DOB,
     * and gender.
     *
     * @param patientSearchData 
     * 		Contains the patient search data that we know.
     * @param person
     * 		Upon return if an address and/or phone number were found in the MPI search, these values will be added
     * 		to this object.
     */
    private void retrieveAndFillInAddrAndPhone(PatientSearchData patientSearchData, PRPAMT201301UV02Person person)
    {
    	LOG.debug("Begin retrieveAndFillInAddrAndPhone.");
    	
        if (patientSearchData == null)
        {
            return;
        }

        String firstName = null;
        if ((patientSearchData.getFirstName() != null) &&
            (patientSearchData.getFirstName().length() > 0))
        {
            firstName = patientSearchData.getFirstName();
        }

        String lastName = null;
        if ((patientSearchData.getLastName() != null) &&
            (patientSearchData.getLastName().length() > 0))
        {
            lastName = patientSearchData.getLastName();
        }

        String gender = "";
        if ((patientSearchData.getGender() != null) &&
            (patientSearchData.getGender().length() > 0))
        {
            gender = patientSearchData.getGender();
        }

        String birthTime = "";
        if ((patientSearchData.getDob() != null) &&
            (patientSearchData.getDob().length() > 0))
        {
            birthTime = patientSearchData.getDob();
        }

        try
        {
            String assigningAuthId = PropertyAccessor.getInstance().getProperty(PROPERTY_FILE_NAME_ADAPTER, PROPERTY_FILE_KEY_ASSIGN_AUTH);
            String orgId = PropertyAccessor.getInstance().getProperty(PROPERTY_FILE_NAME_GATEWAY, PROPERTY_FILE_KEY_HOME_COMMUNITY);

            II patId = new II();
            patId.setRoot(assigningAuthId);
            PRPAMT201301UV02Patient patient = HL7PatientTransforms.create201301Patient(HL7PatientTransforms.create201301PatientPerson(firstName, lastName, gender, birthTime, null), patId);
            PRPAIN201305UV02 searchPat = HL7PRPA201305Transforms.createPRPA201305(patient, orgId, orgId, assigningAuthId);

            AdapterComponentMpiProxyObjectFactory mpiFactory = new AdapterComponentMpiProxyObjectFactory();
            AdapterComponentMpiProxy mpiProxy = mpiFactory.getAdapterComponentMpiProxy();
            AssertionCreator assertionCreator = new AssertionCreator();
            AssertionType oAssertion = assertionCreator.createAssertion();
            PRPAIN201306UV02 patients = mpiProxy.findCandidates(searchPat, oAssertion);

            List<PRPAMT201310UV02Patient> mpiPatResultList = new ArrayList<PRPAMT201310UV02Patient>();
            
            if ((patients != null) &&
                (patients.getControlActProcess() != null) &&
                (patients.getControlActProcess().getSubject() != null))
            {
                List<PRPAIN201306UV02MFMIMT700711UV01Subject1> subjectList = patients.getControlActProcess().getSubject();
                LOG.debug("Search MPI found " + subjectList.size() + " candidates");
                
                for (PRPAIN201306UV02MFMIMT700711UV01Subject1 subject1 : subjectList) {
                    if ((subject1 != null) &&
                        (subject1.getRegistrationEvent() != null) &&
                        (subject1.getRegistrationEvent().getSubject1() != null) &&
                        (subject1.getRegistrationEvent().getSubject1().getPatient() != null))
                    {
                        PRPAMT201310UV02Patient mpiPat = subject1.getRegistrationEvent().getSubject1().getPatient();
                        mpiPatResultList.add(mpiPat);
                    }
                }
                // Because of the way we are searching there should be exactly one match.  Lets just make sure that
                // is the case.
                //-------------------------------------------------------------------------------------------------
                if (mpiPatResultList.size() == 1)
                {
                    PRPAMT201310UV02Patient patient201310 = mpiPatResultList.get(0);
                    if ((patient201310 != null) &&
                        (patient201310.getPatientPerson() != null) &&
                        (patient201310.getPatientPerson().getValue() != null))
                    {
                        LOG.debug("Found a '201310' PatientPerson object.");
                        PRPAMT201310UV02Person person201310 = patient201310.getPatientPerson().getValue();
                        if (person201310.getAddr().size() > 0)
                        {
                            LOG.debug("Setting search address.");
                            person.getAddr().clear();
                            person.getAddr().addAll(person201310.getAddr());
                        }
                        else
                        {
                        	LOG.debug("There was no search address");
                        }

                        if (person201310.getTelecom().size() > 0)
                        {
                        	LOG.debug("Setting search telephone.");
                            person.getTelecom().clear();
                            person.getTelecom().addAll(person201310.getTelecom());
                        }
                        else
                        {
                        	LOG.debug("There was no search telephone.");
                        }
                    }
                    else
                    {
                    	LOG.debug("There was no '201310' PatientPerson object.");
                    }
                }
                else
                {
                	LOG.error("When re-retrieving the patient data to fill in the 201305 - we found more than one" +
                              " result when we should have only found 1.");
                }
            }

        }
        catch (Exception e)
        {
        	LOG.error("When re-retrieving the patient data to fill in the 201305 - we received an unexpected exception: " +
                      e.getMessage(), e);
        }
        
        LOG.debug("End retrieveAndFillInAddrAndPhone.");
    }
    
    
    
    
    
}
