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

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.CeType;
import gov.hhs.fha.nhinc.common.nhinccommon.HomeCommunityType;
import gov.hhs.fha.nhinc.common.nhinccommon.PersonNameType;
import gov.hhs.fha.nhinc.common.nhinccommon.UserType;
import gov.hhs.fha.nhinc.properties.PropertyAccessException;
import gov.hhs.fha.nhinc.properties.PropertyAccessor;
import gov.hhs.fha.nhinc.common.nhinccommon.SamlAuthnStatementType;

import org.apache.log4j.Logger;

public class AssertionCreator {

    protected static final String PROPERTY_FILE_NAME = "universalClient";    
    protected static final String PROPERTY_KEY_MSG_ORG = "AssertionMsgOrganization";
    protected static final String PROPERTY_KEY_MSG_ORG_HC_ID = "AssertionMsgOrganizationHcId";     
    protected static final String PROPERTY_KEY_PURPOSE_CODE = "AssertionPurposeCode";
    protected static final String PROPERTY_KEY_PURPOSE_SYSTEM = "AssertionPurposeSystem";
    protected static final String PROPERTY_KEY_PURPOSE_SYSTEM_NAME = "AssertionPurposeSystemName";
    protected static final String PROPERTY_KEY_PURPOSE_DISPLAY = "AssertionPurposeDisplay";
    protected static final String PROPERTY_KEY_USER_FIRST = "AssertionUserFirstName";
    protected static final String PROPERTY_KEY_USER_MIDDLE = "AssertionUserMiddleName";
    protected static final String PROPERTY_KEY_USER_LAST = "AssertionUserLastName";
    protected static final String PROPERTY_KEY_USER_NAME = "AssertionUserName";
    protected static final String PROPERTY_KEY_USER_ORG = "AssertionUserOrganization";    
    protected static final String PROPERTY_KEY_USER_ORG_HC_ID = "AssertionUserOrganizationHcId";
    protected static final String PROPERTY_KEY_USER_CODE = "AssertionUserCode";
    protected static final String PROPERTY_KEY_USER_SYSTEM = "AssertionUserSystem";
    protected static final String PROPERTY_KEY_USER_SYSTEM_NAME = "AssertionUserSystemName";
    protected static final String PROPERTY_KEY_USER_DISPLAY = "AssertionUserDisplay";
    protected static final String PROPERTY_KEY_AUTHN_CONTEXT_CLASS_REF = "AssertionAuthContextClassRef";
    protected static final String PROPERTY_KEY_AUTHN_INSTANT = "AssertionAuthInstant";
    protected static final String PROPERTY_KEY_AUTHN_SESSION_INDEX = "AssertionAuthSessionIndex";
    protected static final String PROPERTY_KEY_AUTHN_SUBJECT_LOCALITY_ADDRESS = "AssertionAuthSubjectLocalityAddress";
    protected static final String PROPERTY_KEY_AUTHN_SUBJECT_LOCALITY_DNS = "AssertionAuthSubjectLocalityDNS";

    private static final Logger LOG = Logger.getLogger(AssertionCreator.class);
    
    
    /**
     * A wrapper around the "PropertyAccessor.getProperty" method.
     * 
     * @param propertyFileName
     * 		Contains the name of the property file. This is the name 
   	 * 		of the file without a path and without the ".properties" extension. 
   	 *		Examples of this would be "connection" or "gateway".
     * @param propertyName
     * 		Contains the name of the property within the property file.  	 
     * @return
     * 		Returns the "value" of the property specified by the property "file" and property "name".
     * @throws PropertyAccessException
     */
    protected String getProperty(String propertyFileName, String propertyName) throws PropertyAccessException {    	
    	return PropertyAccessor.getInstance().getProperty(propertyFileName, propertyName);   	
    }
    
    
    /**
     * Creates an "AssertionType" object.
     * 
     * @return
     * 		Returns an "AssertionType" object.
     */
    AssertionType createAssertion() {

        AssertionType assertOut = new AssertionType();
        CeType purposeCoded = new CeType();
        UserType user = new UserType();
        PersonNameType userPerson = new PersonNameType();
        CeType userRole = new CeType();
        HomeCommunityType userHc = new HomeCommunityType();
        
        HomeCommunityType msgHc = new HomeCommunityType();
        
        user.setPersonName(userPerson);
        user.setOrg(userHc);
        user.setRoleCoded(userRole);
        
        assertOut.setHomeCommunity(msgHc);
        
        assertOut.setUserInfo(user);
        assertOut.setPurposeOfDisclosureCoded(purposeCoded);
        
        SamlAuthnStatementType oAuthnStatement = new SamlAuthnStatementType();
        assertOut.setSamlAuthnStatement(oAuthnStatement);
        
        LOG.debug("Begin AssertionCreator.createAssertion");

        try {
            msgHc.setName(getProperty(PROPERTY_FILE_NAME, PROPERTY_KEY_MSG_ORG));
            msgHc.setHomeCommunityId(getProperty(PROPERTY_FILE_NAME, PROPERTY_KEY_MSG_ORG_HC_ID));            
            
            userPerson.setGivenName(getProperty(PROPERTY_FILE_NAME, PROPERTY_KEY_USER_FIRST));
            userPerson.setFamilyName(getProperty(PROPERTY_FILE_NAME, PROPERTY_KEY_USER_LAST));
            userPerson.setSecondNameOrInitials(getProperty(PROPERTY_FILE_NAME,
                    PROPERTY_KEY_USER_MIDDLE));
            userHc.setName(getProperty(PROPERTY_FILE_NAME, PROPERTY_KEY_USER_ORG));            
            userHc.setHomeCommunityId(getProperty(PROPERTY_FILE_NAME, PROPERTY_KEY_USER_ORG_HC_ID));
            
            user.setUserName(getProperty(PROPERTY_FILE_NAME, PROPERTY_KEY_USER_NAME));
            userRole.setCode(getProperty(PROPERTY_FILE_NAME, PROPERTY_KEY_USER_CODE));
            userRole.setCodeSystem(getProperty(PROPERTY_FILE_NAME, PROPERTY_KEY_USER_SYSTEM));
            userRole.setCodeSystemName(getProperty(PROPERTY_FILE_NAME, PROPERTY_KEY_USER_SYSTEM_NAME));
            userRole.setDisplayName(getProperty(PROPERTY_FILE_NAME, PROPERTY_KEY_USER_DISPLAY));

            purposeCoded.setCode(getProperty(PROPERTY_FILE_NAME, PROPERTY_KEY_PURPOSE_CODE));
            purposeCoded.setCodeSystem(getProperty(PROPERTY_FILE_NAME, PROPERTY_KEY_PURPOSE_SYSTEM));
            purposeCoded.setCodeSystemName(getProperty(PROPERTY_FILE_NAME,
                    PROPERTY_KEY_PURPOSE_SYSTEM_NAME));
            purposeCoded.setDisplayName(getProperty(PROPERTY_FILE_NAME, PROPERTY_KEY_PURPOSE_DISPLAY));
            
            // Fill in the AuthnStatement
            //---------------------------
            oAuthnStatement.setAuthInstant(getProperty(PROPERTY_FILE_NAME, PROPERTY_KEY_AUTHN_INSTANT));
            oAuthnStatement.setAuthContextClassRef(getProperty(PROPERTY_FILE_NAME, PROPERTY_KEY_AUTHN_CONTEXT_CLASS_REF));
            oAuthnStatement.setSessionIndex(getProperty(PROPERTY_FILE_NAME, PROPERTY_KEY_AUTHN_SESSION_INDEX));
            oAuthnStatement.setSubjectLocalityAddress(getProperty(PROPERTY_FILE_NAME, PROPERTY_KEY_AUTHN_SUBJECT_LOCALITY_ADDRESS));
            oAuthnStatement.setSubjectLocalityDNSName(getProperty(PROPERTY_FILE_NAME, PROPERTY_KEY_AUTHN_SUBJECT_LOCALITY_DNS));
            
            // assertOut.getSamlAuthzDecisionStatement().getEvidence().getAssertion().getConditions().setNotBefore(PropertyAccessor.getProperty(PROPERTY_FILE_NAME,
            // PROPERTY_KEY_SIGN));
            // assertOut.getSamlAuthzDecisionStatement().getEvidence().getAssertion().getConditions().setNotOnOrAfter(PropertyAccessor.getProperty(PROPERTY_FILE_NAME,
            // PROPERTY_KEY_EXPIRE));
            // assertOut.getSamlAuthzDecisionStatement().getEvidence().getAssertion().setAccessConsentPolicy(PropertyAccessor.getProperty(PROPERTY_FILE_NAME,
            // PROPERTY_KEY_ACCESS_CONSENT));

        } catch (PropertyAccessException ex) {
            LOG.error("Universal Client can not access property: " + ex.getMessage());
        }
        
        LOG.debug("End AssertionCreator.createAssertion");       
        
        return assertOut;
    }

}
