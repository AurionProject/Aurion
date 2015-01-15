package universalclientgui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.properties.PropertyAccessException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This class contains all junit test cases for the class "AssertionCreator".
 * 
 * @author Greg Gurr
 *
 */
public class AssertionCreatorTest {

	private AssertionCreator testSubject;
	
	
	@Before
	public void setUp() throws Exception {		
		testSubject = new AssertionCreator() {
			@Override
			protected String getProperty(String propertyFileName, String propertyName) throws PropertyAccessException {					
				return getPropertyValue(propertyName);				
			}			
		};
	}

	@After
	public void tearDown() throws Exception {
		testSubject = null;	
	}

	@Test
	public void testCreateAssertion() {
		
		AssertionType assertionType = testSubject.createAssertion();
		
		assertNotNull(assertionType);
		
		// Assert "home community"
		assertNotNull(assertionType.getHomeCommunity());
		assertEquals("Home community 'name' did not match", getPropertyValue(AssertionCreator.PROPERTY_KEY_MSG_ORG), assertionType.getHomeCommunity().getName());
		assertEquals("Home community 'id' did not match", getPropertyValue(AssertionCreator.PROPERTY_KEY_MSG_ORG_HC_ID), assertionType.getHomeCommunity().getHomeCommunityId());		
		
		// Assert "userInfo"
		assertNotNull(assertionType.getUserInfo());
		assertNotNull(assertionType.getUserInfo().getPersonName());
		assertEquals("PersonName.givenName did not match", getPropertyValue(AssertionCreator.PROPERTY_KEY_USER_FIRST), 
				assertionType.getUserInfo().getPersonName().getGivenName());			
		assertEquals("PersonName.familyName did not match", getPropertyValue(AssertionCreator.PROPERTY_KEY_USER_LAST), 
				assertionType.getUserInfo().getPersonName().getFamilyName());		
		assertEquals("PersonName.secondNameOrInitials did not match", getPropertyValue(AssertionCreator.PROPERTY_KEY_USER_MIDDLE), 
				assertionType.getUserInfo().getPersonName().getSecondNameOrInitials());				
		assertNotNull(assertionType.getUserInfo().getOrg());	
		assertEquals("Org.name did not match", getPropertyValue(AssertionCreator.PROPERTY_KEY_USER_ORG), 
				assertionType.getUserInfo().getOrg().getName());		
		assertEquals("Org.homeCommunitId did not match", getPropertyValue(AssertionCreator.PROPERTY_KEY_USER_ORG_HC_ID), 
				assertionType.getUserInfo().getOrg().getHomeCommunityId());			
		assertNotNull(assertionType.getUserInfo().getRoleCoded());		
		assertEquals("RoleCoded.code did not match", getPropertyValue(AssertionCreator.PROPERTY_KEY_USER_CODE), 
				assertionType.getUserInfo().getRoleCoded().getCode());		
		assertEquals("RoleCoded.codeSystem did not match", getPropertyValue(AssertionCreator.PROPERTY_KEY_USER_SYSTEM), 
				assertionType.getUserInfo().getRoleCoded().getCodeSystem());
		assertEquals("RoleCoded.codeSystemName did not match", getPropertyValue(AssertionCreator.PROPERTY_KEY_USER_SYSTEM_NAME), 
				assertionType.getUserInfo().getRoleCoded().getCodeSystemName());
		assertEquals("RoleCoded.displayName did not match", getPropertyValue(AssertionCreator.PROPERTY_KEY_USER_DISPLAY), 
				assertionType.getUserInfo().getRoleCoded().getDisplayName());
		
		// Assert "purpose of disclosure coded"
		assertNotNull(assertionType.getPurposeOfDisclosureCoded());			
		assertEquals("PurposeOfDisclosureCoded.code did not match", getPropertyValue(AssertionCreator.PROPERTY_KEY_PURPOSE_CODE), 
				assertionType.getPurposeOfDisclosureCoded().getCode());		
		assertEquals("PurposeOfDisclosureCoded.codeSystem did not match", getPropertyValue(AssertionCreator.PROPERTY_KEY_PURPOSE_SYSTEM), 
				assertionType.getPurposeOfDisclosureCoded().getCodeSystem());	
		assertEquals("PurposeOfDisclosureCoded.codeSystemName did not match", getPropertyValue(AssertionCreator.PROPERTY_KEY_PURPOSE_SYSTEM_NAME), 
				assertionType.getPurposeOfDisclosureCoded().getCodeSystemName());	
		assertEquals("PurposeOfDisclosureCoded.displayName did not match", getPropertyValue(AssertionCreator.PROPERTY_KEY_PURPOSE_DISPLAY), 
				assertionType.getPurposeOfDisclosureCoded().getDisplayName());	
		
		// Assert "Saml authentication statement"
		assertNotNull(assertionType.getSamlAuthnStatement());			
		assertEquals("SamlAuthnStatement.AuthInstant did not match", getPropertyValue(AssertionCreator.PROPERTY_KEY_AUTHN_INSTANT), 
				assertionType.getSamlAuthnStatement().getAuthInstant());		
		assertEquals("SamlAuthnStatement.AuthContextClassRef did not match", getPropertyValue(AssertionCreator.PROPERTY_KEY_AUTHN_CONTEXT_CLASS_REF), 
				assertionType.getSamlAuthnStatement().getAuthContextClassRef());		
		assertEquals("SamlAuthnStatement.SessionIndex did not match", getPropertyValue(AssertionCreator.PROPERTY_KEY_AUTHN_SESSION_INDEX), 
				assertionType.getSamlAuthnStatement().getSessionIndex());		
		assertEquals("SamlAuthnStatement.SubjectLocalityAddress did not match", getPropertyValue(AssertionCreator.PROPERTY_KEY_AUTHN_SUBJECT_LOCALITY_ADDRESS), 
				assertionType.getSamlAuthnStatement().getSubjectLocalityAddress());		
		assertEquals("SamlAuthnStatement.SubjectLocalityDNSName did not match", getPropertyValue(AssertionCreator.PROPERTY_KEY_AUTHN_SUBJECT_LOCALITY_DNS), 
				assertionType.getSamlAuthnStatement().getSubjectLocalityDNSName());		
		
	}
	
	
	
	/**
	 * Gets a property "value" for the passed in property "name".
	 * 
	 * @param propertyName
	 * 		Contains the property "name" for which to get the corresponding
	 * 		property "value".
	 * @return
	 * 		Returns the "value" for the passed in property "name".
	 */
	private String getPropertyValue(String propertyName) {
		String propertyValue = "";
			
		if(AssertionCreator.PROPERTY_KEY_MSG_ORG.equals(propertyName)) {
			propertyValue = "defaultMsgOrganization";
		} else if(AssertionCreator.PROPERTY_KEY_MSG_ORG_HC_ID.equals(propertyName)) {
			propertyValue = "1.1";
		} else if(AssertionCreator.PROPERTY_KEY_USER_FIRST.equals(propertyName)) {
			propertyValue = "defaultUserFirstName";
		} else if(AssertionCreator.PROPERTY_KEY_USER_LAST.equals(propertyName)) {
			propertyValue = "defaultUserLastName";
		} else if(AssertionCreator.PROPERTY_KEY_USER_MIDDLE.equals(propertyName)) {
			propertyValue = "defaultUserMiddleName";
		} else if(AssertionCreator.PROPERTY_KEY_USER_ORG.equals(propertyName)) {
			propertyValue = "defaultUserOrganization";
		} else if(AssertionCreator.PROPERTY_KEY_USER_ORG_HC_ID.equals(propertyName)) {
			propertyValue = "1.1";
		} else if(AssertionCreator.PROPERTY_KEY_USER_NAME.equals(propertyName)) {
			propertyValue = "defaultUserName";
		} else if(AssertionCreator.PROPERTY_KEY_USER_CODE.equals(propertyName)) {
			propertyValue = "defaultUserRoleCode";
		} else if(AssertionCreator.PROPERTY_KEY_USER_SYSTEM.equals(propertyName)) {
			propertyValue = "defaultUserRoleCodeSystem";
		} else if(AssertionCreator.PROPERTY_KEY_USER_SYSTEM_NAME.equals(propertyName)) {
			propertyValue = "defaultUserRoleCodeSystemName";
		} else if(AssertionCreator.PROPERTY_KEY_USER_DISPLAY.equals(propertyName)) {
			propertyValue = "defaultUserRoleCodeDisplayName";
		} else if(AssertionCreator.PROPERTY_KEY_PURPOSE_CODE.equals(propertyName)) {
			propertyValue = "defaultPurposeOfUseRoleCode";
		} else if(AssertionCreator.PROPERTY_KEY_PURPOSE_SYSTEM.equals(propertyName)) {
			propertyValue = "defaultPurposeOfUseCodeSystem";
		} else if(AssertionCreator.PROPERTY_KEY_PURPOSE_SYSTEM_NAME.equals(propertyName)) {
			propertyValue = "defaultPurposeOfUseCodeSystemName";
		} else if(AssertionCreator.PROPERTY_KEY_PURPOSE_DISPLAY.equals(propertyName)) {
			propertyValue = "defaultPurposeOfUseDisplayName";
		} else if(AssertionCreator.PROPERTY_KEY_AUTHN_INSTANT.equals(propertyName)) {
			propertyValue = "2009-04-16T13:15:39Z";
		} else if(AssertionCreator.PROPERTY_KEY_AUTHN_CONTEXT_CLASS_REF.equals(propertyName)) {
			propertyValue = "urn:oasis:names:tc:SAML:2.0:ac:classes:X509";
		} else if(AssertionCreator.PROPERTY_KEY_AUTHN_SESSION_INDEX.equals(propertyName)) {
			propertyValue = "987";
		} else if(AssertionCreator.PROPERTY_KEY_AUTHN_SUBJECT_LOCALITY_ADDRESS.equals(propertyName)) {
			propertyValue = "158.147.185.168";
		} else if(AssertionCreator.PROPERTY_KEY_AUTHN_SUBJECT_LOCALITY_DNS.equals(propertyName)) {
			propertyValue = "cs.myharris.net";
		} else {						
			propertyValue = "someDefaultPropertyValue";
		}
		
		return propertyValue;							
	}
	
	
	

}
