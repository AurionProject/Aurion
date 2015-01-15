/**
 *
 */
package gov.hhs.fha.nhinc.callback.openSAML;

import gov.hhs.fha.nhinc.callback.SamlConstants;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants.GATEWAY_API_LEVEL;
import gov.hhs.fha.nhinc.util.AbstractSuppressRootLoggerTest;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opensaml.saml2.core.Action;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.AuthzDecisionStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.DecisionTypeEnumeration;
import org.opensaml.saml2.core.Evidence;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Statement;
import org.opensaml.saml2.core.Subject;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.XSString;
import org.w3c.dom.Element;

/**
 * @author bhumphrey
 *
 */
public class HOKSAMLAssertionBuilderTest extends AbstractSuppressRootLoggerTest {

	private static final String ATTRIBUTE_VALUE_NPI = "npi";
	private static final String ATTRIBUTE_VALUE_USER_FULL_NAME = "Full Name";
	private static final String ATTRIBUTE_VALUE_ORGANIZATION_ID = "orgId";
	private static final String ATTRIBUTE_VALUE_HOME_COMMUNITY_ID = "hci";
	private static final String ATTRIBUTE_VALUE_USER_ORG = "userOrg";
	private static final String ATTRIBUTE_VALUE_PATIENT_ID = "pid";
	private static final String GENERIC_ATTRIBUTE_1_NAME = "generic_name_1";
	private static final String GENERIC_ATTRIBUTE_1_VALUE = "generic_value_1";
	private static final String GENERIC_ATTRIBUTE_2_NAME = "generic_name_2";
    private static final String GENERIC_ATTRIBUTE_2_VALUE = "generic_value_2";
    private static RSAPublicKey publicKey;
    private static PrivateKey privateKey;

    @BeforeClass
    static public void setUp() throws NoSuchAlgorithmException {

        KeyPairGenerator keyGen;
        keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(512);
        publicKey = (RSAPublicKey) keyGen.genKeyPair().getPublic();
        privateKey = keyGen.genKeyPair().getPrivate();

    }

    /*
     * KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType()); 54
     * InputStream is = null; 55 try { 56 is = new ClassPathResource(
     * "/org/springframework/ws/soap/security/xwss/test-keystore.jks"
     * ).getInputStream(); 57 keyStore.load(is, "password".toCharArray()); 58 }
     * 59 finally { 60 if (is != null) { 61 is.close(); 62 } 63 } 64 certificate
     * = (X509Certificate) keyStore.getCertificate("alias");
     */
    /**
     *
     * @throws Exception
     */
    @Test
    public void testBuild() throws Exception {
        SAMLAssertionBuilder builder = new HOKSAMLAssertionBuilder(
            new CertificateManager() {
                @Override
                public RSAPublicKey getDefaultPublicKey() {
                    return publicKey;

                }

                @Override
                public PrivateKey getDefaultPrivateKey() throws Exception {

                    return privateKey;
                }

                @Override
                public KeyStore getKeyStore() {
                    return null;
                }

                @Override
                public KeyStore getTrustStore() {
                    return null;
                }

                @Override
                public X509Certificate getDefaultCertificate()
                    throws Exception {
                    return new X509Certificate() {
                        @Override
                        public boolean hasUnsupportedCriticalExtension() {

                            return false;
                        }

                        @Override
                        public Set<String> getNonCriticalExtensionOIDs() {

                            return Collections.EMPTY_SET;
                        }

                        @Override
                        public byte[] getExtensionValue(String oid) {

                            return new byte[1];
                        }

                        @Override
                        public Set<String> getCriticalExtensionOIDs() {

                            return Collections.EMPTY_SET;
                        }

                        @Override
                        public void verify(PublicKey key, String sigProvider)
                            throws CertificateException,
                            NoSuchAlgorithmException,
                            InvalidKeyException,
                            NoSuchProviderException, SignatureException {
                        }

                        @Override
                        public void verify(PublicKey key)
                            throws CertificateException,
                            NoSuchAlgorithmException,
                            InvalidKeyException,
                            NoSuchProviderException, SignatureException {
                        }

                        @Override
                        public String toString() {

                            return null;
                        }

                        @Override
                        public PublicKey getPublicKey() {

                            return publicKey;
                        }

                        @Override
                        public byte[] getEncoded()
                            throws CertificateEncodingException {

                            return new byte[1];
                        }

                        @Override
                        public int getVersion() {

                            return 0;
                        }

                        @Override
                        public byte[] getTBSCertificate()
                            throws CertificateEncodingException {

                            return new byte[1];
                        }

                        @Override
                        public boolean[] getSubjectUniqueID() {

                            return new boolean[1];
                        }

                        @Override
                        public Principal getSubjectDN() {

                            return null;
                        }

                        @Override
                        public byte[] getSignature() {

                            return new byte[1];
                        }

                        @Override
                        public byte[] getSigAlgParams() {

                            return new byte[1];
                        }

                        @Override
                        public String getSigAlgOID() {

                            return null;
                        }

                        @Override
                        public String getSigAlgName() {

                            return null;
                        }

                        @Override
                        public BigInteger getSerialNumber() {

                            return null;
                        }

                        @Override
                        public Date getNotBefore() {

                            return null;
                        }

                        @Override
                        public Date getNotAfter() {

                            return null;
                        }

                        @Override
                        public boolean[] getKeyUsage() {

                            return new boolean[1];
                        }

                        @Override
                        public boolean[] getIssuerUniqueID() {

                            return new boolean[1];
                        }

                        @Override
                        public Principal getIssuerDN() {

                            return null;
                        }

                        @Override
                        public int getBasicConstraints() {

                            return 0;
                        }

                        @Override
                        public void checkValidity(Date date)
                            throws CertificateExpiredException,
                            CertificateNotYetValidException {
                        }

                        @Override
                        public void checkValidity()
                            throws CertificateExpiredException,
                            CertificateNotYetValidException {
                        }
                    };

                }
            });
        Element assertion = builder.build(getProperties());
        assertNotNull(assertion);
    }

    @Test
    public void testCreateAuthenicationStatement() {
        List<AuthnStatement> authnStatement = HOKSAMLAssertionBuilder
            .createAuthenicationStatements(getProperties());
        assertNotNull(authnStatement);

        assertFalse(authnStatement.isEmpty());
    }

    @Test
    public void testCreateAuthenticationDecisionStatements() {
        CallbackProperties callbackProps = mock(CallbackProperties.class);
        Subject subject = mock(Subject.class);
        DateTime beforeCreation = new DateTime();

        when(callbackProps.getAuthenicationStatementExists()).thenReturn(true);

        List<AuthzDecisionStatement> statementList = HOKSAMLAssertionBuilder
            .createAuthenicationDecsionStatements(callbackProps, subject);

        assertFalse(statementList.isEmpty());
        AuthzDecisionStatement statement = statementList.get(0);
        assertEquals(statement.getDecision(), DecisionTypeEnumeration.PERMIT);

        Action action = statement.getActions().get(0);
        assertEquals(action.getAction(),
            SAMLAssertionBuilder.AUTHZ_DECISION_ACTION_EXECUTE);

        Evidence evidence = statement.getEvidence();
        Assertion assertion = evidence.getAssertions().get(0);
        assertTrue(assertion.getID().startsWith("_"));

        assertTrue(beforeCreation.isBefore(assertion.getIssueInstant())
            || beforeCreation.isEqual(assertion.getIssueInstant()));

        Issuer issuer = assertion.getIssuer();
        assertEquals(issuer.getFormat(), SAMLAssertionBuilder.X509_NAME_ID);

        Conditions conditions = assertion.getConditions();
        assertTrue(beforeCreation.isBefore(conditions.getNotBefore())
            || beforeCreation.isEqual(conditions.getNotBefore()));
        assertTrue(beforeCreation.isBefore(conditions.getNotOnOrAfter())
            || beforeCreation.isEqual(conditions.getNotOnOrAfter()));

        List<AttributeStatement> attributeStatement = assertion
            .getAttributeStatements();
        assertEquals(attributeStatement.get(0).getAttributes().size(), 2);

        Attribute firstAttribute = attributeStatement.get(0).getAttributes()
            .get(0);
        Attribute secondAttribute = attributeStatement.get(0).getAttributes()
            .get(1);
        assertEquals(firstAttribute.getName(), "AccessConsentPolicy");
        assertEquals(secondAttribute.getName(), "InstanceAccessConsentPolicy");
    }

    @Test
    public void testAttributeStatements() {
    	List<Statement> attributeStatements = HOKSAMLAssertionBuilder.createAttributeStatements(getProperties(), null);
        
    	assertNotNull(attributeStatements);
        assertEquals("Attribute statement size", 2, attributeStatements.size());
        AttributeStatement attributeStatement = null;
        int attributeStatementCount = 0;
        for(Statement statement : attributeStatements) {
        	if(statement instanceof AttributeStatement) {
        		attributeStatement = (AttributeStatement)statement;
        		attributeStatementCount++;
        	}
        }
        assertEquals("AttributeStatement count", 1, attributeStatementCount);
        assertEquals("Attribute size", 10, attributeStatement.getAttributes().size());
        
        List<Attribute> attributes = attributeStatement.getAttributes();
        
        //1. Subject ID
        Attribute subjectIdAttr = getAttribute(attributes, SamlConstants.USERNAME_ATTR);
        assertNotNull("Subject id attribute was null", subjectIdAttr);
        validateAttribute(subjectIdAttr, SamlConstants.USERNAME_ATTR, ATTRIBUTE_VALUE_USER_FULL_NAME); 
        
        //2. Subject Organization
        Attribute subjectOrgAttr = getAttribute(attributes, SamlConstants.USER_ORG_ATTR);
        assertNotNull("Subject organization attribute was null", subjectOrgAttr);
        validateAttribute(subjectOrgAttr, SamlConstants.USER_ORG_ATTR, ATTRIBUTE_VALUE_USER_ORG); 
        
        //3. Subject Role
        Attribute subjectRoleAttr = getAttribute(attributes, SamlConstants.USER_ROLE_ATTR);
        assertNotNull("Subject role attribute was null", subjectRoleAttr);
        assertEquals("Subject role attribute value size", 1, subjectRoleAttr.getAttributeValues().size());
        assertTrue("Subject role attribute value was not an XSAny object", (subjectRoleAttr.getAttributeValues().get(0) instanceof XSAny));
        
        //4. Purpose Of Use
        Attribute purposeOfUseAttr = getAttribute(attributes, SamlConstants.PURPOSE_ROLE_ATTR);
        assertNotNull("Purpose of use attribute was null", purposeOfUseAttr);
        assertEquals("Purpose of use attribute value size", 1, purposeOfUseAttr.getAttributeValues().size());
        assertTrue("Purpose of use attribute value was not an XSAny object", (purposeOfUseAttr.getAttributeValues().get(0) instanceof XSAny));
        
        //5. Home Community ID
        Attribute homeCommunityAttr = getAttribute(attributes, SamlConstants.HOME_COM_ID_ATTR);
        assertNotNull("Home community attribute was null", homeCommunityAttr);
        validateAttribute(homeCommunityAttr, SamlConstants.HOME_COM_ID_ATTR, ATTRIBUTE_VALUE_HOME_COMMUNITY_ID); 
        
        //6. Organization ID
        Attribute organizationIdAttr = getAttribute(attributes, SamlConstants.USER_ORG_ID_ATTR);
        assertNotNull("Organization ID attribute was null", organizationIdAttr);
        validateAttribute(organizationIdAttr, SamlConstants.USER_ORG_ID_ATTR, ATTRIBUTE_VALUE_ORGANIZATION_ID); 
        
        //7. Resource ID (Optional)
        Attribute resourceIdAttr = getAttribute(attributes, SamlConstants.PATIENT_ID_ATTR);
        assertNotNull("Resource id attribute was null", resourceIdAttr);
        validateAttribute(resourceIdAttr, SamlConstants.PATIENT_ID_ATTR, ATTRIBUTE_VALUE_PATIENT_ID); 
        
        //8. National Provider Identifier (Optional)
        Attribute npiAttr = getAttribute(attributes, SamlConstants.ATTRIBUTE_NAME_NPI);
        assertNotNull("National Provider Identifier attribute was null", npiAttr);
        validateAttribute(npiAttr, SamlConstants.ATTRIBUTE_NAME_NPI, ATTRIBUTE_VALUE_NPI); 
        
        
        // User defined attribute 1
        Attribute userDefined1Attr = getAttribute(attributes, GENERIC_ATTRIBUTE_1_NAME);
        assertNotNull("User defined attribute 1 was null", userDefined1Attr);
        validateAttribute(userDefined1Attr, GENERIC_ATTRIBUTE_1_NAME, GENERIC_ATTRIBUTE_1_VALUE); 
        
        // User defined attribute 2
        Attribute userDefined2Attr = getAttribute(attributes, GENERIC_ATTRIBUTE_2_NAME);
        assertNotNull("User defined attribute 2 was null", userDefined2Attr);
        validateAttribute(userDefined2Attr, GENERIC_ATTRIBUTE_2_NAME, GENERIC_ATTRIBUTE_2_VALUE); 

    }
    
    private Attribute getAttribute(List<Attribute> attributes,
			String attributeName) {
		Attribute attribute = null;
		for(Attribute attr : attributes) {
			if(attributeName.equals(attr.getName())) {
				attribute = attr;
				break;
			}
		}
		return attribute;
	}

	@Test
    public void testUserDefinedAttributeStatements() {
    	List<Attribute> userDefinedAttributes = HOKSAMLAssertionBuilder.createUserDefinedAttributes(getProperties());
        
    	assertNotNull(userDefinedAttributes);
        assertEquals("User defined attribute statement size", 2, userDefinedAttributes.size());
        
        boolean attribute1Found = false;
        boolean attribute2Found = false;
        
        assertEquals("User defined attribute size", 2, userDefinedAttributes.size());
        Attribute attribute1 = userDefinedAttributes.get(0);
        assertNotNull("User defined attribute 1 name was null", userDefinedAttributes.get(0).getName());
        if(GENERIC_ATTRIBUTE_1_NAME.equals(attribute1.getName())) {
        	validateAttribute(attribute1, GENERIC_ATTRIBUTE_1_NAME, GENERIC_ATTRIBUTE_1_VALUE);
        	attribute1Found = true;
        } else {
        	validateAttribute(attribute1, GENERIC_ATTRIBUTE_2_NAME, GENERIC_ATTRIBUTE_2_VALUE);
        	attribute2Found = true;
        }
        
        Attribute attribute2 = userDefinedAttributes.get(1);
        assertNotNull("User defined attribute 2 name was null", userDefinedAttributes.get(1).getName());
        if(GENERIC_ATTRIBUTE_1_NAME.equals(attribute2.getName())) {
        	validateAttribute(attribute2, GENERIC_ATTRIBUTE_1_NAME, GENERIC_ATTRIBUTE_1_VALUE);
        	attribute1Found = true;
        } else {
        	validateAttribute(attribute2, GENERIC_ATTRIBUTE_2_NAME, GENERIC_ATTRIBUTE_2_VALUE);
        	attribute2Found = true;
        }
        
        assertTrue("Attribute 1 was not found", attribute1Found);
        assertTrue("Attribute 2 was not found", attribute2Found);
        
    }
    
    private void validateAttribute(Attribute attribute, String expectedName, String expectedValue) {
        assertEquals("User defined attribute name", expectedName, attribute.getName());
        assertEquals("User defined attribute value size", 1, attribute.getAttributeValues().size());
        assertEquals("User defined attribute value", expectedValue, ((XSString)attribute.getAttributeValues().get(0)).getValue());
    }

    @Test
    public void testGenericAssertionAttributes() {
        List<AuthnStatement> authnStatement = HOKSAMLAssertionBuilder.createAuthenicationStatements(getProperties());
        assertNotNull(authnStatement);

        assertFalse(authnStatement.isEmpty());
    }

    CallbackProperties getProperties() {
        return new CallbackProperties() {
            @Override
            public String getUsername() {
                return "userName";
            }

            @Override
            public String getUserSystemName() {
                return "sytemName";
            }

            @Override
            public String getUserSystem() {
                return "userSystem";
            }

            @Override
            public String getUserOrganization() {
                return ATTRIBUTE_VALUE_USER_ORG;
            }

            @Override
            public String getUserFullName() {
                return ATTRIBUTE_VALUE_USER_FULL_NAME;
            }

            @Override
            public String getUserDisplay() {
                return "display";
            }

            @Override
            public String getUserCode() {
                return "userCode";
            }

            @Override
            public String getSubjectLocality() {
                return "subject";
            }

            @Override
            public String getSubjectDNS() {
                return "dns";
            }

            @Override
            public String getPurposeSystemName() {
                return "systemname";
            }

            @Override
            public String getPurposeSystem() {
                return "purpose";
            }

            @Override
            public String getPurposeDisplay() {
                return "disply";
            }

            @Override
            public String getPurposeCode() {
                return "code";
            }

            @Override
            public String getPatientID() {
                return ATTRIBUTE_VALUE_PATIENT_ID;
            }

            @Override
            public String getIssuer() {
                return "issuer";
            }

            @Override
            public String getHomeCommunity() {
                return ATTRIBUTE_VALUE_HOME_COMMUNITY_ID;
            }

            @Override
            public String getEvidenceIssuerFormat() {
                return "format";
            }

            @Override
            public String getEvidenceIssuer() {
                return "issuer";
            }

            @Override
            public String getEvidenceSubject() {
                return "evidenceSubject";
            }

            @Override
            public DateTime getEvidenceInstant() {
                return new DateTime();
            }

            @Override
            public List getEvidenceInstantAccessConsent() {
                return Collections.EMPTY_LIST;
            }

            @Override
            public String getEvidenceID() {
                return "evidence id";
            }

            @Override
            public DateTime getEvidenceConditionNotBefore() {
                return new DateTime();
            }

            @Override
            public DateTime getEvidenceConditionNotAfter() {
                return new DateTime();
            }

            @Override
            public List getEvidenceAccessConstent() {
                return Collections.EMPTY_LIST;
            }

            @Override
            public String getAuthnicationResource() {
                return "resource";
            }

            @Override
            public Boolean getAuthenicationStatementExists() {
                return false;
            }

            @Override
            public String getAuthenticationSessionIndex() {
                return "1";
            }

            @Override
            public DateTime getAuthenticationInstant() {
                return new DateTime();
            }

            @Override
            public String getAuthenicationDecision() {
                return null;
            }

            @Override
            public String getAuthenticationContextClass() {
                return "cntx";
            }

            @Override
            public String getAssertionIssuerFormat() {
                return "format";
            }

            @Override
            public String getTargetHomeCommunityId() {
                return "targetHomeCommunityId";
            }

            @Override
            public String getServiceName() {
                return "serviceName";
            }

            @Override
            public String getAction() {
                return "action";
            }

            @Override
            public GATEWAY_API_LEVEL getTargetApiLevel() {
                return GATEWAY_API_LEVEL.LEVEL_g1;
            }

            @Override
            public String getNPI() {
                return ATTRIBUTE_VALUE_NPI;
            }

            @Override
            public String getUserOrganizationId() {
                return ATTRIBUTE_VALUE_ORGANIZATION_ID;
            }
            
        	@Override
            public Map<String, String> getGenericAttributes() {
        		Map<String, String> genericAttributes = new HashMap<String, String>();
        		genericAttributes.put(GENERIC_ATTRIBUTE_1_NAME, GENERIC_ATTRIBUTE_1_VALUE);
        		genericAttributes.put(GENERIC_ATTRIBUTE_2_NAME, GENERIC_ATTRIBUTE_2_VALUE);
        		return genericAttributes;
        	}
            
        };
    }
}
