/*
*   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.nativeimpl.functions;

import org.apache.axiom.om.OMElement;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.nativeimpl.util.XMLUtils;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;

/**
 * Test Native function in ballerina.model.xml.
 */
public class XMLTest {

    private BLangProgram bLangProgram;
    private static final String s1 = "<persons><person><name>Jack</name><address>wso2</address></person></persons>";
    private static final String s2 = "<person><name>Jack</name></person>";
    private static String l1;



    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("samples/xmlTest.bal");
    }

    @Test
    public void testGetString() {
        BValue[] args = {new BXML(s1), new BString("/persons/person/name/text()")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "getString", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "Jack";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test(expectedExceptions = BallerinaException.class)
    public void testGetNonExistingString() {
        BValue[] args = {new BXML(s1), new BString("/xxx/text()")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "getString", args);

        Assert.assertEquals(returns[0], null);
    }

    @Test(expectedExceptions = {BallerinaException.class})
    public void testGetStringFromMalformedXpath() {
        BValue[] args = {new BXML(s1), new BString("$worng#path")};
        BLangFunctions.invoke(bLangProgram, "getString", args);
    }

    @Test
    public void testGetXML() {
        BValue[] args = {new BXML(s1), new BString("/persons/person")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "getXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMElement returnElement = ((BXML) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<person><name>Jack</name>" +
                "<address>wso2</address></person>");
    }

    @Test
    public void testGetXMLLarge() {
        // Load large xml
        l1 = XMLUtils.readFileToString("datafiles/message13k.xml");
        BValue[] args = {new BXML(l1),
                new BString("/persons/person[160]")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "getXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMElement returnElement = ((BXML) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<person>" +
                "<name>Jill</name>" +
                "<address>wso2</address>" +
                "</person>");
    }

    @Test(expectedExceptions = BallerinaException.class)
    public void testGetNonExistingXML() {
        BValue[] args = {new BXML(s1), new BString("/xxx")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "getXML", args);

        Assert.assertEquals(returns[0], null);
    }

    @Test(expectedExceptions = {BallerinaException.class})
    public void testGetXMLFromMalformedXpath() {
        BValue[] args = {new BXML(s1), new BString("$worng#path")};
        BLangFunctions.invoke(bLangProgram, "getXML", args);
    }

    @Test(expectedExceptions = BallerinaException.class)
    public void testGetXMLFromText() {
        BValue[] args = {new BXML(s1), new BString("/persons/person/name/text()")};
        BLangFunctions.invoke(bLangProgram, "getXML", args);
    }

    @Test(expectedExceptions = BallerinaException.class)
    public void testGetXMLFromDocumentElement() {
        String d1 = XMLUtils.readFileToString("datafiles/xmlDocumentSample.xml");
        BValue[] args = {new BXML(d1), new BString("/")};
        BLangFunctions.invoke(bLangProgram, "getXML", args);
    }

    @Test(expectedExceptions = BallerinaException.class)
    public void testGetXMLFromAttribute() {
        String a1 = XMLUtils.readFileToString("datafiles/messageComplex.xml");
        BValue[] args = {new BXML(a1), new BString("/employees/employee/@id")};
        BLangFunctions.invoke(bLangProgram, "getXML", args);
    }

    @Test
    public void testSetString() {
        BValue[] args = {new BXML(s1), new BString("/persons/person/name/text()"), new BString("Peter")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "setString", args);

        Assert.assertEquals(returns[0].stringValue(), "<persons><person><name>Peter" +
                "</name><address>wso2</address></person></persons>");
    }

    @Test
    public void testSetStringToNonExistingElement() {
        BValue[] args = {new BXML(s1), new BString("/xxx/text()"), new BString("Peter")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "setString", args);

        Assert.assertEquals(returns[0].stringValue(), s1);
    }

    @Test(expectedExceptions = {BallerinaException.class})
    public void testSetStringToMalformedXpath() {
        BValue[] args = {new BXML(s1), new BString("$worng#path"), new BString("Peter")};
        BLangFunctions.invoke(bLangProgram, "setString", args);
    }

    @Test
    public void testSetStringToAttribute() {
        String a1 = XMLUtils.readFileToString("datafiles/messageSimple.xml");
        BValue[] args = {new BXML(a1), new BString("/employee/@id"), new BString("0")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "setString", args);

        Assert.assertEquals(returns[0].stringValue(), "<employee id=\"0\">\n" +
                "    <name>Parakum</name>\n" +
                "    <age>32</age>\n" +
                "</employee>");
    }

    @Test
    public void testSetXML() {
        BValue[] args = {new BXML(s2), new BString("/person/name"),
                new BXML("<name><fname>Jack</fname><lname>Peter</lname></name>")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "setXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMElement returnElement = ((BXML) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<person><name><fname>Jack" +
                "</fname><lname>Peter</lname></name></person>");
    }

    @Test
    public void testSetXMLToNonExistingElement() {
        BValue[] args = {new BXML(s2), new BString("/xxx"),
                new BXML("<name><fname>Jack</fname><lname>Peter</lname></name>")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "setXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMElement returnElement = ((BXML) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), s2);
    }


    @Test(expectedExceptions = {BallerinaException.class})
    public void testSetXMLToMalformedXpath() {
        BValue[] args = {new BXML(s2), new BString("$worng#path"),
                new BXML("<name><fname>Jack</fname><lname>Peter</lname></name>")};
        BLangFunctions.invoke(bLangProgram, "setXML", args);
    }

    @Test(expectedExceptions = {BallerinaException.class})
    public void testSetXMLNullValue() {
        BValue[] args = {new BXML(s2), new BString("/person/name"),
                null};
        BLangFunctions.invoke(bLangProgram, "setXML", args);
    }

    @Test
    public void testAddElement() {
        BValue[] args = {new BXML(s2), new BString("/person"), new BXML("<address>wso2</address>")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "addElement", args);

        Assert.assertEquals(returns[0].stringValue(), "<person><name>Jack</name>" +
                "<address>wso2</address></person>");
    }

    @Test(expectedExceptions = BallerinaException.class)
    public void testAddElementToNonExistingElement() {
        BValue[] args = {new BXML(s2), new BString("/xxx"), new BXML("<address>wso2</address>")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "addElement", args);

        Assert.assertEquals(returns[0].stringValue(), s2);
    }

    @Test(expectedExceptions = {BallerinaException.class})
    public void testAddElementToMalformedXpath() {
        BValue[] args = {new BXML(s2), new BString("$worng#path"), new BXML("<address>wso2</address>")};
        BLangFunctions.invoke(bLangProgram, "addElement", args);
    }

    @Test(expectedExceptions = BallerinaException.class)
    public void testAddElementToText() {
        BValue[] args = {new BXML(s1), new BString("/persons/person/name/text()"), new BXML("<address>wso2</address>")};
        BLangFunctions.invoke(bLangProgram, "addElement", args);
    }

    @Test(expectedExceptions = BallerinaException.class)
    public void testAddElementToDocumentElement() {
        String d1 = XMLUtils.readFileToString("datafiles/xmlDocumentSample.xml");
        BValue[] args = {new BXML(d1), new BString("/"), new BXML("<address>wso2</address>")};
        BLangFunctions.invoke(bLangProgram, "addElement", args);
    }

    @Test(expectedExceptions = BallerinaException.class)
    public void testAddElementToAttribute() {
        String a1 = XMLUtils.readFileToString("datafiles/messageComplex.xml");
        BValue[] args = {new BXML(a1), new BString("/employees/employee/@id"), new BXML("<address>wso2</address>")};
        BLangFunctions.invoke(bLangProgram, "addElement", args);
    }

    @Test
    public void testAddAttribute() {
        BValue[] args = {new BXML(s2), new BString("/person/name"), new BString("id"), new BString("person123")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "addAttribute", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMElement returnElement = ((BXML) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<person><name id=\"person123\">Jack</name></person>");
    }

    @Test(expectedExceptions = BallerinaException.class)
    public void testAddAttributeToNonExistingElement() {
        BValue[] args = {new BXML(s2), new BString("/xxx"), new BString("id"), new BString("person123")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "addAttribute", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMElement returnElement = ((BXML) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), s2);
    }

    @Test(expectedExceptions = {BallerinaException.class})
    public void testAddAttributeToMalformedXpath() {
        BValue[] args = {new BXML(s2), new BString("$worng#path"), new BString("id"), new BString("person123")};
        BLangFunctions.invoke(bLangProgram, "addAttribute", args);
    }

    @Test(expectedExceptions = BallerinaException.class)
    public void testAddAttributeToText() {
        BValue[] args = {new BXML(s2), new BString("/persons/person/name/text()"), new BString("id"),
                new BString("person123")};
        BLangFunctions.invoke(bLangProgram, "addAttribute", args);
    }

    @Test(expectedExceptions = BallerinaException.class)
    public void testAddAttributeToDocumentElement() {
        String d1 = XMLUtils.readFileToString("datafiles/xmlDocumentSample.xml");
        BValue[] args = {new BXML(d1), new BString("/"), new BString("id"), new BString("person123")};
        BLangFunctions.invoke(bLangProgram, "addAttribute", args);
    }

    @Test(expectedExceptions = BallerinaException.class)
    public void testAddAttributeToAttribute() {
        String a1 = XMLUtils.readFileToString("datafiles/messageComplex.xml");
        BValue[] args = {new BXML(a1), new BString("/employees/employee/@id"), new BString("id"),
                new BString("person123")};
        BLangFunctions.invoke(bLangProgram, "addAttribute", args);
    }

    @Test
    public void testRemove() {
        BValue[] args = {new BXML(s1), new BString("/persons/person/address")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "remove", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMElement returnElement = ((BXML) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<persons><person><name>Jack" +
                "</name></person></persons>");
    }

    @Test
    public void testRemoveNonExistingElement() {
        BValue[] args = {new BXML(s1), new BString("/xxx")};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "remove", args);
        Assert.assertEquals(returns[0].stringValue(), s1);
    }

    @Test(expectedExceptions = {BallerinaException.class})
    public void testRemoveFromMalformedXpath() {
        BValue[] args = {new BXML(s1), new BString("$worng#path")};
        BLangFunctions.invoke(bLangProgram, "remove", args);
    }

    @Test
    public void testToString() {
        BValue[] args = {new BXML(s1)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "toString", args);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), s1);
    }

    @Test(description = "Test xml element string value replacement")
    public void testSetXmlElementText() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "xmlSetString1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BXML.class);
        BXML returnedXml = (BXML) returns[0];
        OMElement xmlMessage = returnedXml.value();
        String actualDName = xmlMessage.getFirstChildWithName(new QName("doctorName")).getText();
        Assert.assertEquals(actualDName, "DName1", "XML Element text not set properly");
    }

    @Test(description = "Test xml text value replacement")
    public void testSetXmlText() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "xmlSetString2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BXML.class);
        BXML returnedXml = (BXML) returns[0];
        OMElement xmlMessage = returnedXml.value();
        String actualDName = xmlMessage.getFirstChildWithName(new QName("doctorName")).getText();
        Assert.assertEquals(actualDName, "DName2", "XML Element text not set properly");
    }

}
