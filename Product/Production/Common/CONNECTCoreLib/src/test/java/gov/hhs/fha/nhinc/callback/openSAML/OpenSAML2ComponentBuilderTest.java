/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services.
 * All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   * Redistributions of source code must retain the above
 *     copyright notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of the United States Government nor the
 *     names of its contributors may be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY
 *DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.hhs.fha.nhinc.callback.openSAML;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.opensaml.saml2.core.Attribute;
import java.util.List;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.util.AttributeMap;

/**
 * @author achidamb
 * 
 */
public class OpenSAML2ComponentBuilderTest {

    @Test
    public void generateEvAssertionWithoutUnderScore() {
        String uuid = "2345";
        Assertion assertion = (Assertion) OpenSAML2ComponentBuilder.getInstance().createAssertion(uuid);
        assertEquals(uuid, assertion.getID());
    }

    @Test
    public void generateEvAssertionWithUnderScore() {
        String uuid = "_".concat(String.valueOf(UUID.randomUUID()));
        Assertion assertion = (Assertion) OpenSAML2ComponentBuilder.getInstance().createAssertion(uuid);
        assertEquals(uuid, assertion.getID());
    }

    /**
     * Test namespace uri for purpose use.
     */
    @Test
    public void testNamespaceUriForPurposeUse() {
        boolean foundType = false;
        String purposeCode = "purposeCode";
        String purposeSystem = "purposeSystem";
        String purposeSystemName = "purposeSystemName";
        String purposeDisplay = "purposeDisplay";
        String type = "hl7:CE";
        Attribute attribute = OpenSAML2ComponentBuilder.getInstance().createPurposeOfUseAttribute(purposeCode,
                purposeSystem, purposeSystemName, purposeDisplay);
        List<XMLObject> attributeValue = attribute.getAttributeValues();
        for (XMLObject value : attributeValue) {
            for (XMLObject valueElement : value.getOrderedChildren()) {
                if (valueElement instanceof XSAny) {
                    XSAny role = (XSAny) valueElement;
                    AttributeMap map = role.getUnknownAttributes();
                    assertNotNull(map.get(new QName("http://www.w3.org/2001/XMLSchema-instance", "type", "xsi")));  
                    assertEquals(type, map.get(new QName("http://www.w3.org/2001/XMLSchema-instance", "type", "xsi")));                    
                    foundType = true;
                }
            }
        }
        assertFalse(!foundType);
    }

    /**
     * Test namespace uri for role.
     */
    @Test
    public void testNamespaceUriForRole() {
        boolean foundType = false;
        String userCode = "12345";
        String userSystem = "1.2.34.56";
        String userSystemName = "CANCER-Research";
        String userDisplay = "Public Health";
        String type = "hl7:CE";
        Attribute attribute = OpenSAML2ComponentBuilder.getInstance().createUserRoleAttribute(userCode, userSystem,
                userSystemName, userDisplay);
        List<XMLObject> attributeValue = attribute.getAttributeValues();
        for (XMLObject value : attributeValue) {
            for (XMLObject valueElement : value.getOrderedChildren()) {
                if (valueElement instanceof XSAny) {
                    XSAny role = (XSAny) valueElement;
                    AttributeMap map = role.getUnknownAttributes();
                    assertNotNull(map.get(new QName("http://www.w3.org/2001/XMLSchema-instance", "type", "xsi")));
                    assertEquals(type, map.get(new QName("http://www.w3.org/2001/XMLSchema-instance", "type", "xsi")));                     
                    foundType = true;
                }
            }
        }
        assertFalse(!foundType);
    }

}
