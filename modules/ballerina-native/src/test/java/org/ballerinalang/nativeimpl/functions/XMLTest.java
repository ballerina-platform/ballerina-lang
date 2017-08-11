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
import org.apache.axiom.om.OMNode;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLAttributes;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.model.values.BXMLSequence;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.nativeimpl.util.XMLUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;

/**
 * Test Native function in ballerina.model.xml.
 */
public class XMLTest {

    private ProgramFile programFile;
    private ProgramFile xmlAttrProgFile;
    ProgramFile namespaceProgFile;
    private static final String s1 = "<persons><person><name>Jack</name><address>wso2</address></person></persons>";
    private static final String s2 = "<person><name>Jack</name></person>";
    private static String l1;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("samples/xml/xmlTest.bal");
        xmlAttrProgFile = BTestUtils.getProgramFile("samples/xml/xmlAttributeTest.bal");
        namespaceProgFile = BTestUtils.getProgramFile("samples/xml/xmlNamespaceTest.bal");
    }

    @Test
    public void testGetString() {
        BValue[] args = {new BXMLItem(s1), new BString("/persons/person/name/text()")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "getString", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "Jack";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }
    
    @Test
    public void testGetStringFromSeq() {
        BRefValueArray seq = new BRefValueArray();
        seq.add(0, new BXMLItem(s1));
        BValue[] args = { new BXMLSequence(seq), new BString("/persons/person/name/text()") };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "getString", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "Jack";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }
    

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testGetNonExistingString() {
        BValue[] args = {new BXMLItem(s1), new BString("/xxx/text()")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "getString", args);

        Assert.assertEquals(returns[0], null);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testGetStringFromMalformedXpath() {
        BValue[] args = {new BXMLItem(s1), new BString("$worng#path")};
        BLangFunctions.invokeNew(programFile, "getString", args);
    }

    @Test
    public void testGetXML() {
        BValue[] args = {new BXMLItem(s1), new BString("/persons/person")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "getXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<person><name>Jack</name>" +
                "<address>wso2</address></person>");
    }

    @Test
    public void testGetXMLFromSingletonSequence() {
        BRefValueArray seq = new BRefValueArray();
        seq.add(0, new BXMLItem(s1));
        BValue[] args = { new BXMLSequence(seq), new BString("/persons/person") };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "getXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<person><name>Jack</name>" +
                "<address>wso2</address></person>");
    }
    
    @Test(expectedExceptions = {BLangRuntimeException.class},
          expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:Error, message: failed to get element " +
                  "from xml: cannot execute xpath on a xml sequence.*")
    public void testGetXMLFromSequence() {
        BRefValueArray seq = new BRefValueArray();
        seq.add(0, new BXMLItem(s1));
        seq.add(1, new BXMLItem(s2));
        BValue[] args = { new BXMLSequence(seq), new BString("/persons/person") };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "getXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<person><name>Jack</name>" +
                "<address>wso2</address></person>");
    }
    
    @Test
    public void testGetXMLLarge() {
        // Load large xml
        l1 = XMLUtils.readFileToString("datafiles/message13k.xml");
        BValue[] args = {new BXMLItem(l1),
                new BString("/persons/person[160]")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "getXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<person>" +
                "<name>Jill</name>" +
                "<address>wso2</address>" +
                "</person>");
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testGetNonExistingXML() {
        BValue[] args = {new BXMLItem(s1), new BString("/xxx")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "getXML", args);

        Assert.assertEquals(returns[0], null);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testGetXMLFromMalformedXpath() {
        BValue[] args = {new BXMLItem(s1), new BString("$worng#path")};
        BLangFunctions.invokeNew(programFile, "getXML", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testGetXMLFromText() {
        BValue[] args = {new BXMLItem(s1), new BString("/persons/person/name/text()")};
        BLangFunctions.invokeNew(programFile, "getXML", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testGetXMLFromDocumentElement() {
        String d1 = XMLUtils.readFileToString("datafiles/xmlDocumentSample.xml");
        BValue[] args = {new BXMLItem(d1), new BString("/")};
        BLangFunctions.invokeNew(programFile, "getXML", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testGetXMLFromAttribute() {
        String a1 = XMLUtils.readFileToString("datafiles/messageComplex.xml");
        BValue[] args = {new BXMLItem(a1), new BString("/employees/employee/@id")};
        BLangFunctions.invokeNew(programFile, "getXML", args);
    }

    @Test
    public void testSetString() {
        BValue[] args = {new BXMLItem(s1), new BString("/persons/person/name/text()"), new BString("Peter")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "setString", args);

        Assert.assertEquals(returns[0].stringValue(), "<persons><person><name>Peter" +
                "</name><address>wso2</address></person></persons>");
    }

    @Test
    public void testSetStringToSingletonSequence() {
        BRefValueArray seq = new BRefValueArray();
        seq.add(0, new BXMLItem(s1));
        BValue[] args = { new BXMLSequence(seq), new BString("/persons/person/name/text()"), new BString("Peter")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "setString", args);

        Assert.assertEquals(returns[0].stringValue(), "<persons><person><name>Peter" +
                "</name><address>wso2</address></person></persons>");
    }
    
    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:Error, message: failed to set " +
                    "string in xml: cannot execute xpath on a xml sequence.*")
    public void testSetStringToSequence() {
        BRefValueArray seq = new BRefValueArray();
        seq.add(0, new BXMLItem(s1));
        seq.add(1, new BXMLItem(s2));
        BValue[] args = { new BXMLSequence(seq), new BString("/persons/person/name/text()"), new BString("Peter")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "setString", args);

        Assert.assertEquals(returns[0].stringValue(), "<persons><person><name>Peter" +
                "</name><address>wso2</address></person></persons>");
    }
    
    @Test
    public void testSetStringToNonExistingElement() {
        BValue[] args = {new BXMLItem(s1), new BString("/xxx/text()"), new BString("Peter")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "setString", args);

        Assert.assertEquals(returns[0].stringValue(), s1);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testSetStringToMalformedXpath() {
        BValue[] args = {new BXMLItem(s1), new BString("$worng#path"), new BString("Peter")};
        BLangFunctions.invokeNew(programFile, "setString", args);
    }

    @Test
    public void testSetStringToAttribute() {
        String a1 = XMLUtils.readFileToString("datafiles/messageSimple.xml");
        BValue[] args = {new BXMLItem(a1), new BString("/employee/@id"), new BString("0")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "setString", args);

        Assert.assertEquals(returns[0].stringValue(), "<employee id=\"0\">\n" +
                "    <name>Parakum</name>\n" +
                "    <age>32</age>\n" +
                "</employee>");
    }

    @Test
    public void testSetXML() {
        BValue[] args = {new BXMLItem(s2), new BString("/person/name"),
                new BXMLItem("<name><fname>Jack</fname><lname>Peter</lname></name>")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "setXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<person><name><fname>Jack" +
                "</fname><lname>Peter</lname></name></person>");
    }

    @Test
    public void testSetXMLToNonExistingElement() {
        BValue[] args = {new BXMLItem(s2), new BString("/xxx"),
                new BXMLItem("<name><fname>Jack</fname><lname>Peter</lname></name>")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "setXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), s2);
    }


    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testSetXMLToMalformedXpath() {
        BValue[] args = {new BXMLItem(s2), new BString("$worng#path"),
                new BXMLItem("<name><fname>Jack</fname><lname>Peter</lname></name>")};
        BLangFunctions.invokeNew(programFile, "setXML", args);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testSetXMLNullValue() {
        BValue[] args = {new BXMLItem(s2), new BString("/person/name"),
                null};
        BLangFunctions.invokeNew(programFile, "setXML", args);
    }

    @Test
    public void testAddElement() {
        BValue[] args = {new BXMLItem(s2), new BString("/person"), new BXMLItem("<address>wso2</address>")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "addElement", args);

        Assert.assertEquals(returns[0].stringValue(), "<person><name>Jack</name>" +
                "<address>wso2</address></person>");
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddElementToNonExistingElement() {
        BValue[] args = {new BXMLItem(s2), new BString("/xxx"), new BXMLItem("<address>wso2</address>")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "addElement", args);

        Assert.assertEquals(returns[0].stringValue(), s2);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testAddElementToMalformedXpath() {
        BValue[] args = {new BXMLItem(s2), new BString("$worng#path"), new BXMLItem("<address>wso2</address>")};
        BLangFunctions.invokeNew(programFile, "addElement", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddElementToText() {
        BValue[] args = {new BXMLItem(s1), new BString("/persons/person/name/text()"), 
            new BXMLItem("<address>wso2</address>")};
        BLangFunctions.invokeNew(programFile, "addElement", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddElementToDocumentElement() {
        String d1 = XMLUtils.readFileToString("datafiles/xmlDocumentSample.xml");
        BValue[] args = {new BXMLItem(d1), new BString("/"), new BXMLItem("<address>wso2</address>")};
        BLangFunctions.invokeNew(programFile, "addElement", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddElementToAttribute() {
        String a1 = XMLUtils.readFileToString("datafiles/messageComplex.xml");
        BValue[] args = {new BXMLItem(a1), new BString("/employees/employee/@id"), 
                new BXMLItem("<address>wso2</address>")};
        BLangFunctions.invokeNew(programFile, "addElement", args);
    }

    @Test
    public void testAddAttributeWithXPath() {
        BValue[] args = {new BXMLItem(s2), new BString("/person/name"), new BString("id"), new BString("person123")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "addAttribute", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<person><name id=\"person123\">Jack</name></person>");
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddAttributeToNonExistingElement() {
        BValue[] args = {new BXMLItem(s2), new BString("/xxx"), new BString("id"), new BString("person123")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "addAttribute", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), s2);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testAddAttributeToMalformedXpath() {
        BValue[] args = {new BXMLItem(s2), new BString("$worng#path"), new BString("id"), new BString("person123")};
        BLangFunctions.invokeNew(programFile, "addAttribute", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddAttributeToText() {
        BValue[] args = {new BXMLItem(s2), new BString("/persons/person/name/text()"), new BString("id"),
                new BString("person123")};
        BLangFunctions.invokeNew(programFile, "addAttribute", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddAttributeToDocumentElement() {
        String d1 = XMLUtils.readFileToString("datafiles/xmlDocumentSample.xml");
        BValue[] args = {new BXMLItem(d1), new BString("/"), new BString("id"), new BString("person123")};
        BLangFunctions.invokeNew(programFile, "addAttribute", args);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testAddAttributeToAttribute() {
        String a1 = XMLUtils.readFileToString("datafiles/messageComplex.xml");
        BValue[] args = {new BXMLItem(a1), new BString("/employees/employee/@id"), new BString("id"),
                new BString("person123")};
        BLangFunctions.invokeNew(programFile, "addAttribute", args);
    }

    @Test
    public void testRemove() {
        BValue[] args = {new BXMLItem(s1), new BString("/persons/person/address")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "remove", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMNode returnElement = ((BXMLItem) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<persons><person><name>Jack" +
                "</name></person></persons>");
    }

    @Test
    public void testRemoveNonExistingElement() {
        BValue[] args = {new BXMLItem(s1), new BString("/xxx")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "remove", args);
        Assert.assertEquals(returns[0].stringValue(), s1);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class})
    public void testRemoveFromMalformedXpath() {
        BValue[] args = {new BXMLItem(s1), new BString("$worng#path")};
        BLangFunctions.invokeNew(programFile, "remove", args);
    }

    @Test(description = "Test xml element string value replacement")
    public void testSetXmlElementText() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "xmlSetString1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        OMElement xmlMessage = (OMElement) ((BXMLItem) returns[0]).value();
        String actualDName = xmlMessage.getFirstChildWithName(new QName("doctorName")).getText();
        Assert.assertEquals(actualDName, "DName1", "XML Element text not set properly");
    }

    @Test(description = "Test xml text value replacement")
    public void testSetXmlText() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "xmlSetString2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BXML);
        OMElement xmlMessage = (OMElement) ((BXMLItem) returns[0]).value();
        String actualDName = xmlMessage.getFirstChildWithName(new QName("doctorName")).getText();
        Assert.assertEquals(actualDName, "DName2", "XML Element text not set properly");
    }

    @Test
    public void testIsSingleton() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testIsSingleton");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);
        
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), true);
    }

    @Test
    public void testIsSingletonWithMultipleChildren() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testIsSingletonWithMultipleChildren");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true);

    }
    
    @Test
    public void testIsEmpty() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testIsEmpty");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);
    }

    @Test
    public void testIsEmptyWithNoElementTextValue() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testIsEmptyWithNoElementTextValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);
    }

    @Test
    public void testIsEmptyWithMultipleChildren() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testIsEmptyWithMultipleChildren");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), false);
    }
    
    @Test
    public void testGetItemType() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetItemType");
        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "element");
        
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertEquals(returns[1].stringValue(), "element");

        Assert.assertSame(returns[2].getClass(), BString.class);
        Assert.assertEquals(returns[2].stringValue(), "");
    }

    @Test
    public void testGetItemTypeForElementWithPrefix() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetItemTypeForElementWithPrefix");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "element");
    }

    @Test
    public void testGetItemTypeForElementWithDefaultNamespace() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetItemTypeForElementWithDefaultNamespace");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "element");
    }
    
    @Test
    public void testGetElementName() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetElementName");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "{http://sample.com/test}name");
    }

    @Test
    public void testGetElementNameWithDefaultNamespace() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetElementNameForElementWithDefaultNamespace");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "{http://sample.com/test}name");
    }

     @Test
    public void testGetElementNameWithoutNamespace() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetElementNameForElementWithoutNamespace");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "{http://sample.com/test/core}name");
    }
    
    @Test
    public void testGetTextValue() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetTextValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "supun");
    }

    @Test
    public void testGetTextValueDefaultNamespace() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetTextValueDefaultNamespace");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "supun");
    }
    
    @Test
    public void testGetElements() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetElements");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        
        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);
    }

    @Test
    public void testGetElementsFromSequence() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetElementsFromSequence");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }
    
    @Test
    public void testGetElementsByName() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetElementsByName");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);
        
        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testGetElementsByNameWithDefaultNamespace() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetElementsByNameWithDefaultNamespace");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testGetElementsByNameWithPrefix() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetElementsByNameByPrefix");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testGetElementsByNameWithDifferentPrefix() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetElementsByNameByDifferentPrefix");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testGetElementsByNameEmptyNamespace() {
        //related issue 3062
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetElementsByNameEmptyNamespace");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testGetElementsByNameWithPrefixForDefaultNamespace() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetElementsByNamePrefixForDefaultNamespace");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is element seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is element seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testGetElementsByNameWithDifferentNamespaces() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetElementsByNameDifferentNamespaces");
        Assert.assertEquals(returns.length, 6);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 1);

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[1]).value().size(), 1);

        // is element seq one is empty?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);

        // is element seq one is singleton?
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[3]).booleanValue(), true);

        // is element seq two is empty?
        Assert.assertSame(returns[4].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), false);

        // is element seq two is singleton?
        Assert.assertSame(returns[5].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[5]).booleanValue(), true);
    }
    
    @Test
    public void testGetChildren() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetChildren");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testGetChildrenFromComplexXml() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetChildrenFromComplexXml");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }
    
    @Test
    public void testGetNonExistingChildren() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetNonExistingChildren");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), true);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }
    
    @Test
    public void testSelectChildren() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSelectChildren");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testSelectChildrenWithDefaultNamespace() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSelectChildrenWithDefaultNamespace");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testSelectChildrenPrefixedDefaultNamespace() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSelectChildrenPrefixedDefaultNamespace");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testSelectChildrenWtihSamePrefix() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSelectChildrenWithSamePrefix");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testSelectChildrenWtihDifferentPrefix() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSelectChildrenWithDifferentPrefix");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }

    @Test
    public void testSelectChildrenWtihDifferentNamespaces() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSelectChildrenWithDifferentNamespaces");
        Assert.assertEquals(returns.length, 6);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 1);

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[1]).value().size(), 1);

        // is children seq one is empty?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);

        // is children seq one is singleton?
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[3]).booleanValue(), true);

        // is children seq two is empty?
        Assert.assertSame(returns[4].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[4]).booleanValue(), false);

        // is children seq two is singleton?
        Assert.assertSame(returns[5].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[5]).booleanValue(), true);
    }
    
    @Test
    public void testConcat() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testConcat");
        Assert.assertEquals(returns.length, 3);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);
        
        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\"><fname>supun" +
                "</fname><lname>setunga</lname></ns0:name><ns1:address xmlns:ns1=\"http://sample.com/test\">" +
                "<country>SL</country><city>Colombo</city></ns1:address>");
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), false);
    }
    
    @Test
    public void testSetChildren() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSetChildren");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);
        
        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\"><newFname>" +
                "supun-new</newFname><newMname>thilina-new</newMname><newLname>setunga-new</newLname></ns0:name>");
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);
        
        // Check children
        Assert.assertTrue(returns[3] instanceof BXML);
        BRefValueArray children = ((BXMLSequence) returns[3]).value();
        Assert.assertEquals(children.size(), 3);
        Assert.assertEquals(children.get(0).stringValue(), "<newFname>supun-new</newFname>");
        Assert.assertEquals(children.get(1).stringValue(), "<newMname>thilina-new</newMname>");
        Assert.assertEquals(children.get(2).stringValue(), "<newLname>setunga-new</newLname>");
    }

    @Test
    public void testSetChildrenWithDefaultNamespace() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSetChildrenDefaultNamespace");
        Assert.assertEquals(returns.length, 5);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(), "<name xmlns=\"http://sample.com/test\"><fname>supun</fname>"
                + "<lname>setunga</lname><residency xmlns=\"\" citizen=\"true\">true</residency></name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check children
        Assert.assertTrue(returns[3] instanceof BXML);
        BRefValueArray children = ((BXMLSequence) returns[3]).value();
        Assert.assertEquals(children.size(), 3);
        Assert.assertEquals(children.get(0).stringValue(), "<fname xmlns=\"http://sample.com/test\">supun</fname>");
        Assert.assertEquals(children.get(1).stringValue(), "<lname xmlns=\"http://sample.com/test\">setunga</lname>");
        Assert.assertEquals(children.get(2).stringValue(), "<residency citizen=\"true\">true</residency>");

        // Check attribute value
        Assert.assertSame(returns[4].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[4]).stringValue(), "true");
    }

    @Test
    public void testSetChildrenWithDifferentNamespaceForAttribute() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSetChildrenWithDifferentNamespaceForAttribute");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[3]).stringValue(), "true");
    }

    @Test
    public void testSetChildrenWithPrefixedAttribute() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSetChildrenWithPrefixedAttribute");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(), "<name xmlns=\"http://sample.com/test\">" +
                "<fname>supun</fname><lname>setunga</lname><residency xmlns=\"\" " + 
                "xmlns:pre=\"http://sample.com/test/code\" pre:citizen=\"true\">true</residency></name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[3]).stringValue(), "true");
    }

    @Test
    public void testSetChildrenSameNamespace() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSetChildrenWithSameNamespace");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\">" +
                "<ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname>" +
                "<ns0:residency ns0:citizen=\"yes\">true</ns0:residency></ns0:name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[3]).stringValue(), "yes");
    }

    @Test
    public void testSetChildrenDifferentNamespace() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSetChildrenWithDifferentNamespace");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\">" +
                "<ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname>" +
                "<ns0:residency xmlns:ns0=\"http://sample.com/test/code\" " +
                "ns0:citizen=\"yes\">true</ns0:residency></ns0:name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[3]).stringValue(), "yes");
    }

    @Test
    public void testSetChildrenDiffNamespaceWithoutPrefix() {
        //related issue 3074
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSetChildrenWithDiffNamespaceWithoutPrefix");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\" " +
                "xmlns=\"http://sample.com/test/code\"><ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname>" +
                "<residency xmlns:citizen=\"yes\">true</residency></ns0:name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[3]).stringValue(), "yes");
    }

    @Test
    public void testSetChildrenDiffAttribute() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSetChildrenWithAttributeDiffNamespace");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\" " +
                "xmlns:pre=\"http://sample.com/test/code\">" +
                "<ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname>" +
                "<ns0:residency pre:citizen=\"yes\">true</ns0:residency></ns0:name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[3]).stringValue(), "yes");
    }

    @Test
    public void testSetChildrenDiffElement() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSetChildrenWithElementDiffNamespace");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\" " +
                "xmlns:pre=\"http://sample.com/test/code\"><ns0:fname>supun</ns0:fname><ns0:lname>setunga</ns0:lname>" +
                "<pre:residency ns0:citizen=\"yes\">true</pre:residency></ns0:name>");

        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);

        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);

        // Check attribute value
        Assert.assertSame(returns[3].getClass(), BString.class);
        Assert.assertEquals(((BString) returns[3]).stringValue(), "yes");
    }

    @Test
    public void testCopy() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testCopy");
        Assert.assertEquals(returns.length, 4);
        Assert.assertTrue(returns[0] instanceof BXMLItem);
        
        Assert.assertEquals(returns[0].stringValue(), "<ns0:name xmlns:ns0=\"http://sample.com/test\"><newFname>" +
                "supun-new</newFname><newMname>thilina-new</newMname><newLname>setunga-new</newLname></ns0:name>");
        
        // Check children of the copied xml
        Assert.assertTrue(returns[3] instanceof BXML);
        BRefValueArray children = ((BXMLSequence) ((BXML) returns[0]).children()).value();
        Assert.assertEquals(children.size(), 3);
        Assert.assertEquals(children.get(0).stringValue(), "<newFname>supun-new</newFname>");
        Assert.assertEquals(children.get(1).stringValue(), "<newMname>thilina-new</newMname>");
        Assert.assertEquals(children.get(2).stringValue(), "<newLname>setunga-new</newLname>");
        
        // is children seq is empty?
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[1]).booleanValue(), false);
        
        // is children seq is singleton?
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);
        
        // Check children of the original xml
        Assert.assertTrue(returns[3] instanceof BXML);
        BRefValueArray originalChildren = ((BXMLSequence) returns[3]).value();
        Assert.assertEquals(originalChildren.size(), 2);
        Assert.assertEquals(originalChildren.get(0).stringValue(), "<fname>supun</fname>");
        Assert.assertEquals(originalChildren.get(1).stringValue(), "<lname>setunga</lname>");
    }
    
    @Test
    public void testToString() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToString");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BString);
        
        Assert.assertEquals(returns[0].stringValue(), "<!-- comment about the book--><bookName>Book1</bookName>" +
                "<bookId>001</bookId><bookAuthor>Author01</bookAuthor><?word document=\"book.doc\" ?>");
    }
    
    @Test
    public void testStrip() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testStrip");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertTrue(returns[1] instanceof BXML);
        
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 5);
        Assert.assertEquals(returns[0].stringValue(), "<!-- comment about the book-->     <bookId>001</bookId>" +
                "<?word document=\"book.doc\" ?>");
        
        Assert.assertEquals(((BXMLSequence) returns[1]).value().size(), 3);
        Assert.assertEquals(returns[1].stringValue(), "<!-- comment about the book--><bookId>001</bookId>" +
                "<?word document=\"book.doc\" ?>");
    }
    
    @Test
    public void testStripSingleton() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testStripSingleton");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertTrue(returns[1] instanceof BXML);
        
        Assert.assertEquals(returns[0].stringValue(), "<bookId>001</bookId>");
        Assert.assertEquals(returns[1].stringValue(), "<bookId>001</bookId>");
    }
    
    @Test
    public void testStripEmptySingleton() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testStripEmptySingleton");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "");
        Assert.assertEquals(returns[1].stringValue(), "");
        Assert.assertEquals(((BBoolean) returns[2]).booleanValue(), true);
    }
    
    @Test
    public void testSlice() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSlice");
        Assert.assertTrue(returns[0] instanceof BXML);
        
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 3);
        Assert.assertEquals(returns[0].stringValue(), "<bookName>Book1</bookName><bookId>001</bookId><bookAuthor>" +
                "Author01</bookAuthor>");
    }
    
    @Test
    public void testSliceAll() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSliceAll");
        Assert.assertTrue(returns[0] instanceof BXML);
        
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 5);
        Assert.assertEquals(returns[0].stringValue(), "<!-- comment about the book--><bookName>Book1</bookName>" +
                "<bookId>001</bookId><bookAuthor>Author01</bookAuthor><?word document=\"book.doc\" ?>");
    }
    
    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:Error, message: failed to slice xml: " +
                    "invalid indices: 4 < 1.*")
    public void testSliceInvalidIndex() {
        BLangFunctions.invokeNew(programFile, "testSliceInvalidIndex");
    }
    
    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:Error, message: failed to slice xml: " +
                    "index out of range: \\[4,10\\].*")
    public void testSliceOutOfRangeIndex() {
        BLangFunctions.invokeNew(programFile, "testSliceOutOfRangeIndex");
    }
    
    @Test
    public void testSliceSingleton() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSliceSingleton");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<bookName>Book1</bookName>");
    }
    
    @Test
    public void testXPathOnCopiedXML() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testXPathOnCopiedXML");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root><bookId>001</bookId><bookAuthor>Author01</bookAuthor>" +
                "</root>");
        Assert.assertEquals(returns[1].stringValue(), "<root><bookName>Book1</bookName><bookId>001</bookId></root>");
    }
    
    @Test
    public void testSeqCopy() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSeqCopy");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<!-- comment about the book--><bookName>Book1</bookName>" +
                "<bookId>Updated Book ID</bookId><bookAuthor>Author01</bookAuthor><?word document=\"book.doc\" ?>");
        Assert.assertEquals(returns[1].stringValue(), "<!-- comment about the book--><bookName>Book1</bookName>" +
                "<bookId>001</bookId><bookAuthor>Author01</bookAuthor><?word document=\"book.doc\" ?>");
    }

    @Test
    public void testAddAttributeWithString() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testAddAttributeWithString");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns4=\"http://sample.com/wso2/f\" "
                + "xmlns:ns0Kf5j=\"http://sample.com/wso2/e\" foo1=\"bar1\" ns0Kf5j:foo2=\"bar2\" ns4:foo3=\"bar3\"/>");
    }
    
    @Test(expectedExceptions = {BLangRuntimeException.class}, 
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:Error, message: localname of the " +
            "attribute cannot be empty.*")
    public void testAddAttributeWithoutLocalname() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testAddAttributeWithoutLocalname");
    }
    
    @Test
    public void testAddAttributeWithEmptyNamespace() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testAddAttributeWithEmptyNamespace");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns3=\"http://sample.com/wso2/f\" foo1=\"bar\"/>");
    }
    
    @Test
    public void testAddNamespaceAsAttribute1() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testAddNamespaceAsAttribute");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns3=\"http://sample.com/wso2/f\" " +
            "xmlns:ns4=\"http://wso2.com\"/>");
        
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<root xmlns=\"http://ballerinalang.org/\" " +
            "xmlns:ns3=\"http://sample.com/wso2/f\" xmlns:ns4=\"http://wso2.com\"/>");
    }
    
    @Test
    public void testAddAttributeWithQName() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testAddAttributeWithQName");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns3=\"http://sample.com/wso2/f\" " +
                "xmlns:ns0=\"http://sample.com/wso2/a1\" ns0:foo1=\"bar1\"/>");
    }

    @Test
    public void testAddAttributeWithQName_1() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testAddAttributeWithDiffQName_1");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns=\"http://sample.com/wso2/c1\" " +
                "xmlns:ns5=\"http://sample.com/wso2/f/\" xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                "xmlns:ns1=\"http://sample.com/wso2/b1\" xmlns:ns4=\"http://sample.com/wso2/f/\" " +
                "xmlns:ns3=\"http://sample.com/wso2/f\" xmlns:pre=\"http://sample.com/wso2/f\" " +
                "ns4:diff=\"yes\" pre:foo1=\"bar1\"/>");
    }

    @Test
    public void testAddAttributeWithQName_2() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testAddAttributeWithDiffQName_2");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns=\"http://sample.com/wso2/c1\" " +
                "xmlns:ns5=\"http://sample.com/wso2/f/\" xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                "xmlns:ns1=\"http://sample.com/wso2/b1\" xmlns:ns4=\"http://sample.com/wso2/f/\" " +
                "xmlns:ns3=\"http://sample.com/wso2/f\" ns4:diff=\"yes\" ns5:foo1=\"bar1\"/>");
    }

    @Test
    public void testAddAttributeWithQName_3() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testAddAttributeWithDiffQName_3");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns=\"http://sample.com/wso2/c1\" " +
                "xmlns:ns5=\"http://sample.com/wso2/f/\" xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                "xmlns:ns1=\"http://sample.com/wso2/b1\" xmlns:ns4=\"http://sample.com/wso2/f/\" " +
                "xmlns:ns3=\"http://sample.com/wso2/f\" ns4:diff=\"yes\" ns4:foo1=\"bar1\"/>");
    }

    @Test(expectedExceptions = { BLangRuntimeException.class }, 
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:Error, message: failed to add attribute " +
            "'ns5:foo1'. prefix 'ns5' is already bound to namespace 'http://sample.com/wso2/f/'.*")
    public void testAddAttributeWithQName_4() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testAddAttributeWithDiffQName_4");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns=\"http://sample.com/wso2/c1\" " +
                "xmlns:ns5=\"http://sample.com/wso2/f/\" xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                "xmlns:ns1=\"http://sample.com/wso2/b1\" xmlns:ns4=\"http://sample.com/wso2/f/\" " +
                "xmlns:ns3=\"http://sample.com/wso2/f\" ns4:diff=\"yes\"/>");
    }

    @Test
    public void testAddAttributeWithQName_5() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testAddAttributeWithDiffQName_5");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns=\"http://sample.com/wso2/c1\" " +
                "xmlns:ns5=\"http://sample.com/wso2/f/\" xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                "xmlns:ns1=\"http://sample.com/wso2/b1\" xmlns:ns4=\"http://sample.com/wso2/f/\" " +
                "xmlns:ns3=\"http://sample.com/wso2/f\" xmlns:foo1=\"bar1\" " +
                "ns4:diff=\"yes\" foo2=\"bar2\" foo3=\"bar3\"/>");
    }


    
    @Test
    public void testUpdateAttributeWithString() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testUpdateAttributeWithString");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns0=\"http://sample.com/wso2/e\" "
                + "foo1=\"newbar1\" ns0:foo2=\"newbar2\" foo3=\"newbar3\"/>");
    }

    @Test
    public void testUpdateAttributeWithString_1() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testUpdateAttributeWithString_1");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns4=\"http://sample.com/wso2/f\" " +
                "xmlns:ns0Kf5j=\"http://sample.com/wso2/e\" xmlns:nsbrlwf=\"http://sample.com/wso2/f/t\" " +
                "foo1=\"bar1\" ns0Kf5j:foo2=\"bar2\" ns4:foo3=\"bar3\" nsbrlwf:foo3=\"newbar3\"/>");
    }
    
    @Test
    public void testUpdateNamespaceAsAttribute() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testUpdateNamespaceAsAttribute");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns3=\"http://wso2.com\"/>");
        
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<root xmlns=\"http://ballerinalang.org/\" " +
            "xmlns:ns3=\"http://wso2.com\"/>");
    }
    
    @Test
    public void testUpdateAttributeWithQName() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testUpdateAttributeWithQName");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns3=\"http://sample.com/wso2/f\" " +
                "xmlns:ns0=\"http://sample.com/wso2/a1\" ns0:foo1=\"newbar1\" ns3:foo2=\"newbar2\"/>");
    }

    @Test
    public void testUpdateAttributeWithQName_1() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testUpdateAttributeWithQName_1");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns3=\"http://sample.com/wso2/f\" " +
                "xmlns:ns0=\"http://sample.com/wso2/a1\" xmlns:ns5=\"http://sample.com/wso2/a1\" " +
                "ns0:foo1=\"newaddedbar1\" ns3:foo2=\"bar2\"/>");
    }
    
    @Test
    public void testGetAttributeWithString() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testGetAttributeWithString");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "bar1");
        
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "bar2");
        
        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "");
    }
    
    @Test
    public void testGetAttributeWithoutLocalname() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testGetAttributeWithoutLocalname");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "");
    }
    
    @Test
    public void testGetAttributeWithEmptyNamespace() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testGetAttributeWithEmptyNamespace");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "bar1");
    }

    @Test
    public void testGetNamespaceAsAttribute() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testGetNamespaceAsAttribute");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "http://sample.com/wso2/f");
    }

    @Test
    public void testGetAttributeWithQName() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testGetAttributeWithQName");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "bar1");

        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "bar2");

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "");
    }

    @Test
    public void testUsingQNameAsString() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testUsingQNameAsString");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "{http://sample.com/wso2/a1}wso2");

        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "{http://sample.com/wso2/a1}ballerina");
    }

    @Test
    public void testGetAttributesAsMap() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testGetAttributesAsMap");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(), "{\"{http://www.w3.org/2000/xmlns/}ns0\":"
                + "\"http://sample.com/wso2/a1\", \"{http://sample.com/wso2/a1}foo1\":\"bar1\", \"foo2\":\"bar2\"}");

        Assert.assertTrue(returns[1] instanceof BMap);
        Assert.assertEquals(returns[1].stringValue(), "{\"{http://sample.com/default/namepsace}ns0\":"
                + "\"http://sample.com/wso2/a1\", \"{http://sample.com/wso2/a1}foo1\":\"bar1\", \"foo2\":\"bar2\"}");

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "bar1");

        Assert.assertTrue(returns[3] instanceof BString);
        Assert.assertEquals(returns[3].stringValue(), "bar1");
    }

    @Test
    public void testNamespaceDclr() {
        BValue[] returns = BLangFunctions.invokeNew(namespaceProgFile, "testNamespaceDclr");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "{http://sample.com/wso2/a2}foo");
        
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "{http://sample.com/wso2/b1}foo");
        
        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "{http://sample.com/wso2/d2}foo");
    }

    @Test
    public void testInnerScopeNamespaceDclr() {
        BValue[] returns = BLangFunctions.invokeNew(namespaceProgFile, "testInnerScopeNamespaceDclr");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "{http://sample.com/wso2/a1}foo");

        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "{http://sample.com/wso2/a3}foo");

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "{http://sample.com/wso2/a1}foo");
    }

    @Test(expectedExceptions = { SemanticException.class }, 
            expectedExceptionsMessageRegExp = "redeclareNamespaces.bal:11: redeclared symbol 'ns0'")
    public void testRedeclaringNamespace() {
        BTestUtils.getProgramFile("samples/xml/redeclareNamespaces.bal");
    }

    @Test(expectedExceptions = { SemanticException.class }, 
            expectedExceptionsMessageRegExp = "attributeMapInvalidUse.bal:6: incompatible types: 'xml-attributes' " +
            "cannot be assigned to 'map'")
    public void testXMlAttributesMapInvalidUsage() {
        ProgramFile xmlAttributeMapInvalidUsage = BTestUtils.getProgramFile("samples/xml/attributeMapInvalidUse.bal");
        BLangFunctions.invokeNew(xmlAttributeMapInvalidUsage, "testXMlAttributesMapInvalidUsage");
    }
    
    @Test
    public void testXMLAttributesToAny() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testXMLAttributesToAny");
        Assert.assertTrue(returns[0] instanceof BXMLAttributes);
        Assert.assertEquals(returns[0].stringValue(), "{\"{http://www.w3.org/2000/xmlns/}ns0\":" +
            "\"http://sample.com/wso2/a1\", \"{http://sample.com/wso2/a1}foo1\":\"bar1\", \"foo2\":\"bar2\"}");
    }

    @Test(expectedExceptions = { SemanticException.class },
            expectedExceptionsMessageRegExp = "namespaceConflictWithPkgImport.bal:6: redeclared symbol 'x'")
    public void testNamespaceConflictWithPkgImport() {
        ProgramFile xmlAttributeMapInvalidUsage = BTestUtils
                .getProgramFile("samples/xml/namespaceConflictWithPkgImport.bal");
    }

    @Test(expectedExceptions = { SemanticException.class },
            expectedExceptionsMessageRegExp = "pkgImportConflictWithNamespace.bal:4: redeclared symbol 'x'")
    public void testPkgImportConflictWithNamespace() {
        ProgramFile xmlAttributeMapInvalidUsage = BTestUtils
                .getProgramFile("samples/xml/pkgImportConflictWithNamespace.bal");
    }

    @Test(expectedExceptions = { SemanticException.class },
            expectedExceptionsMessageRegExp = "getAttributesFromNonXml.bal:4: incompatible types: expected 'xml', "
                    + "found 'map'")
    public void testGetAttributesFromNonXml() {
        ProgramFile xmlAttributeMapInvalidUsage = BTestUtils.getProgramFile("samples/xml/getAttributesFromNonXml.bal");
    }

    @Test(expectedExceptions = { SemanticException.class }, 
            expectedExceptionsMessageRegExp = "updateAttributeMap.bal:3: xml attributes cannot be updated as a " +
            "collection. update attributes one at a time")
    public void testUpdateAttributeMap() {
        ProgramFile xmlAttributeMapInvalidUsage =
                BTestUtils.getProgramFile("samples/xml/updateAttributeMap.bal");
    }
    
    @Test(expectedExceptions = { SemanticException.class }, 
            expectedExceptionsMessageRegExp = "updateQname.bal:4: cannot assign values to an xml qualified name")
    public void testUpdateQname() {
        ProgramFile xmlAttributeMapInvalidUsage =
                BTestUtils.getProgramFile("samples/xml/updateQname.bal");
    }
    
    @Test(expectedExceptions = { SemanticException.class }, 
            expectedExceptionsMessageRegExp = "undefinedNamespace.bal:8: undefined namespace 'ns0'")
    public void testUndefinedNamespace() {
        ProgramFile xmlAttributeMapInvalidUsage =
                BTestUtils.getProgramFile("samples/xml/undefinedNamespace.bal");
    }
    
    @Test
    public void testRuntimeNamespaceLookup() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testRuntimeNamespaceLookup");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns401=\"http://sample.com/wso2/a1\" xmlns:ns1=" +
            "\"http://sample.com/wso2/b1\" xmlns:ns403=\"http://sample.com/wso2/e3\" xmlns:nsn7xFP=" +
            "\"http://sample.com/wso2/f3\" ns401:foo1=\"bar1\" ns1:foo2=\"bar2\" ns403:foo3=\"bar3\" " +
            "nsn7xFP:foo4=\"bar4\"/>");
    }
    
    @Test
    public void testRuntimeNamespaceLookupPriority() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testRuntimeNamespaceLookupPriority");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:p1=\"http://wso2.com\" " +
            "xmlns:p2=\"http://sample.com/wso2/a1\" xmlns:ns401=\"http://sample.com/wso2/a1\" ns401:foo1=\"bar1\" " +
            "p1:foo2=\"bar2\"/>");
    }

    @Test
    public void testSetAttributes() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testSetAttributes");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:nsRJUck=\"http://wso2.com\" xmlns:nsn7xDi="
                + "\"http://sample.com/wso2/a1\" nsRJUck:foo2=\"bar2\" nsn7xDi:foo3=\"bar3\" foo1=\"bar1\"/>");
    }

    @Test
    public void testGetAttributeFromSingletonSeq() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testGetAttributeFromSingletonSeq");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "bar");
    }

    @Test
    public void testSetChildrenToElemntInDefaultNameSpace() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSetChildrenToElemntInDefaultNameSpace");
        Assert.assertTrue(returns[0] instanceof BXML);

        Assert.assertEquals(returns[0].stringValue(),
                "<name xmlns=\"http://sample.com/test\"><newFname xmlns=\"\">supun-new</newFname></name>");
    }

    @Test(expectedExceptions = { SemanticException.class },
            expectedExceptionsMessageRegExp = "defineEmptyNamespace.bal:2: cannot bind a prefix \\('ns0'\\) to the "
                    + "empty namespace name")
    public void testDefineEmptyNamespace() {
        BTestUtils.getProgramFile("samples/xml/defineEmptyNamespace.bal");
    }

    @Test
    public void testToJsonForValue() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJsonForValue");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "value");
    }

    @Test
    public void testToJsonForEmptyValue() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJsonForEmptyValue");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "");
    }

    @Test
    public void testToJsonForComment() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJsonForComment");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{}");
    }

    @Test
    public void testToJsonForPI() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJsonForPI");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{}");
    }

    @Test
    public void testToJsonForSingleElement() {
        String xmlStr = "<key>value</key>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"key\":\"value\"}");
    }

    @Test
    public void testToJsonForEmptySingleElement() {
        String xmlStr = "<key/>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"key\":\"\"}");
    }

    @Test
    public void testToJsonForSimpleXML() {
        String xmlStr = "<person><name>Jack</name><age>40</age></person>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"person\":{\"name\":\"Jack\",\"age\":\"40\"}}");
    }

    @Test
    public void testToJsonForXMLWithTwoLevels() {
        String xmlStr = "<persons><person><name>Jack</name><address>wso2</address></person></persons>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "{\"persons\":{\"person\":{\"name\":\"Jack\",\"address\":\"wso2\"}}}");
    }

    @Test
    public void testToJsonForXMLWithThreeLevels() {
        String xmlStr = "<persons><person><test><name>Jack</name><address>wso2</address></test></person></persons>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "{\"persons\":{\"person\":{\"test\":{\"name\":\"Jack\",\"address\":\"wso2\"}}}}");
    }

    @Test
    public void testToJsonXMLWithSingleElementAndAttributes() {
        String xmlStr = "<name test=\"5\">Jack</name>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":{\"@test\":\"5\",\"#text\":\"Jack\"}}");
    }

    @Test
    public void testToJsonXMLWithSingleElementAttributesNamespace() {
        String xmlStr = "<ns0:name test=\"5\" xmlns:ns0=\"http://sample0.com/test\">Jack</ns0:name>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"ns0:name\":{\"@xmlns:ns0\":\"http://sample0.com/test\","
                + "\"@test\":\"5\",\"#text\":\"Jack\"}}");
    }

    @Test
    public void testToJsonXMLWithSingleEmptyElementAndAttributes() {
        String xmlStr = "<ns0:name test=\"5\" xmlns:ns0=\"http://sample0.com/test\"></ns0:name>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"ns0:name\":{\"@xmlns:ns0\":\"http://sample0.com/test\","
                + "\"@test\":\"5\"}}");
    }

    @Test
    public void testToJsonWithComplexObject() {
        String xmlStr = "<bookStore><storeName>foo</storeName><postalCode>94</postalCode><isOpen>true</isOpen><address>"
                + "<street>foo</street><city>94</city><country>true</country></address><codes><item>4</item>"
                + "<item>8</item><item>9</item></codes></bookStore>\n";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"bookStore\":{\"storeName\":\"foo\",\"postalCode\":\"94\","
                + "\"isOpen\":\"true\",\"address\":{\"street\":\"foo\",\"city\":\"94\",\"country\":\"true\"},"
                + "\"codes\":{\"item\":[\"4\",\"8\",\"9\"]}}}");
    }

    @Test
    public void testToJsonWithXMLInMiddle() {
        String xmlStr = "<person><name>Jack</name><age>40</age><!-- some comment --></person>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"person\":{\"name\":\"Jack\",\"age\":\"40\"}}");
    }

    @Test
    public void testToJsonWithSimpleXMLAndAttributes() {
        String xmlStr = "<person id = \"5\"><name>Jack</name><age>40</age></person>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"person\":{\"@id\":\"5\",\"name\":\"Jack\",\"age\":\"40\"}}");
    }

    @Test
    public void testToJsonWithMultipleAttributes() {
        String xmlStr = "<person id = \"5\"><name cat = \"A\">Jack</name><age>40</age></person>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "{\"person\":{\"@id\":\"5\",\"age\":\"40\",\"name\":{\"@cat\":\"A\",\"#text\":\"Jack\"}}}");
    }

    @Test
    public void testToJsonWithComplexObjectAttributes() {
        String xmlStr = "<bookStore status = \"online\"><storeName>foo</storeName><postalCode>94</postalCode>"
                + "<isOpen>true</isOpen><address><street>foo</street><city code = \"A\">94</city><country>true"
                + "</country></address><codes quality=\"b\"><item>4</item><item>8</item><item>9</item></codes>"
                + "</bookStore>\n";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "{\"bookStore\":{\"@status\":\"online\",\"storeName\":\"foo\",\"postalCode\":\"94\","
                        + "\"isOpen\":\"true\",\"address\":{\"street\":\"foo\",\"country\":\"true\","
                        + "\"city\":{\"@code\":\"A\",\"#text\":\"94\"}},\"codes\":{\"@quality\":\"b\","
                        + "\"item\":[\"4\",\"8\",\"9\"]}}}");
    }

    @Test
    public void testToJsonWithComplexObjectWithMultipleAttributes() {
        String xmlStr = "<bookStore status = \"online\" id = \"5\"><storeName>foo</storeName><postalCode>94"
                + "</postalCode><isOpen>true</isOpen><address><street>foo</street>"
                + "<city code = \"A\" reg = \"C\">94</city><country>true</country></address>"
                + "<codes quality=\"b\" type = \"0\"><item>4</item><item>8</item><item>9</item></codes></bookStore>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"bookStore\":{\"@status\":\"online\",\"@id\":\"5\","
                + "\"storeName\":\"foo\",\"postalCode\":\"94\",\"isOpen\":\"true\",\"address\":{\"street\":\"foo\","
                + "\"country\":\"true\",\"city\":{\"@code\":\"A\",\"@reg\":\"C\",\"#text\":\"94\"}},"
                + "\"codes\":{\"@quality\":\"b\",\"@type\":\"0\",\"item\":[\"4\",\"8\",\"9\"]}}}");
    }

    @Test
    public void testToJsonWithDifferentAttributeTag() {
        String xmlStr = "<bookStore status = \"online\" id = \"5\"><storeName>foo</storeName><postalCode>94"
                + "</postalCode><isOpen>true</isOpen><address><street>foo</street>"
                + "<city code = \"A\" reg = \"C\">94</city><country>true</country></address>"
                + "<codes quality=\"b\" type = \"0\"><item>4</item><item>8</item><item>9</item></codes></bookStore>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSONWithOptions", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"bookStore\":{\"#status\":\"online\",\"#id\":\"5\","
                + "\"storeName\":\"foo\",\"postalCode\":\"94\",\"isOpen\":\"true\",\"address\":{\"street\":\"foo\","
                + "\"country\":\"true\",\"city\":{\"#code\":\"A\",\"#reg\":\"C\",\"#text\":\"94\"}},"
                + "\"codes\":{\"#quality\":\"b\",\"#type\":\"0\",\"item\":[\"4\",\"8\",\"9\"]}}}");
    }

    @Test
    public void testToJsonWithMultipleAttributesNamespaces() {
        String xmlStr = "<ns0:bookStore status=\"online\" xmlns:ns0=\"http://sample0.com/test\" "
                + "xmlns:ns1=\"http://sample1.com/test\"><ns0:storeName>foo</ns0:storeName>"
                + "<ns3:postalCode xmlns:ns3=\"http://sample3.com/test\">94</ns3:postalCode>"
                + "<ns0:isOpen>true</ns0:isOpen><ns2:address xmlns:ns2=\"http://sample2.com/test\">"
                + "<ns2:street>foo</ns2:street><ns2:city>111</ns2:city><ns2:country>true</ns2:country>"
                + "</ns2:address><ns4:codes xmlns:ns4=\"http://sample4.com/test\"><ns4:item>4</ns4:item><ns4:item>8"
                + "</ns4:item><ns4:item>9</ns4:item></ns4:codes></ns0:bookStore>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"ns0:bookStore\":{\"@xmlns:ns0\":\"http://sample0.com/test\","
                + "\"@xmlns:ns1\":\"http://sample1.com/test\",\"@status\":\"online\",\"ns0:storeName\":\"foo\","
                + "\"ns0:isOpen\":\"true\",\"ns3:postalCode\":{\"@xmlns:ns3\":\"http://sample3.com/test\","
                + "\"#text\":\"94\"},\"ns2:address\":{\"@xmlns:ns2\":\"http://sample2.com/test\","
                + "\"ns2:street\":\"foo\",\"ns2:city\":\"111\",\"ns2:country\":\"true\"},"
                + "\"ns4:codes\":{\"@xmlns:ns4\":\"http://sample4.com/test\",\"ns4:item\":[\"4\",\"8\",\"9\"]}}}");
    }

    @Test
    public void testToJsonWithMultipleAttributesNamespacesWithOptions() {
        String xmlStr = "<ns0:bookStore status=\"online\" xmlns:ns0=\"http://sample0.com/test\" "
                + "xmlns:ns1=\"http://sample1.com/test\"><ns0:storeName>foo</ns0:storeName>"
                + "<ns3:postalCode xmlns:ns3=\"http://sample3.com/test\">94</ns3:postalCode>"
                + "<ns0:isOpen>true</ns0:isOpen><ns2:address xmlns:ns2=\"http://sample2.com/test\">"
                + "<ns2:street>foo</ns2:street><ns2:city>111</ns2:city><ns2:country>true</ns2:country>"
                + "</ns2:address><ns4:codes xmlns:ns4=\"http://sample4.com/test\"><ns4:item>4</ns4:item><ns4:item>8"
                + "</ns4:item><ns4:item>9</ns4:item></ns4:codes></ns0:bookStore>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSONWithoutNamespace", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"bookStore\":{\"@status\":\"online\","
                + "\"storeName\":\"foo\",\"postalCode\":\"94\",\"isOpen\":\"true\",\"address\":{\"street\":\"foo\","
                + "\"city\":\"111\",\"country\":\"true\"},\"codes\":{\"item\":[\"4\",\"8\",\"9\"]}}}");
    }

    @Test
    public void testToJsonWithObjectArray() {
        String xmlStr = "<books><item><bookName>book1</bookName><bookId>101</bookId></item><item>"
                + "<bookName>book2</bookName><bookId>102</bookId></item><item><bookName>book3</bookName>"
                + "<bookId>103</bookId></item></books>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "{\"books\":{\"item\":[{\"bookName\":\"book1\",\"bookId\":\"101\"},{\"bookName\":\"book2\","
                        + "\"bookId\":\"102\"},{\"bookName\":\"book3\",\"bookId\":\"103\"}]}}");
    }

    @Test
    public void testToJsonWithChildElementsWithSameKey() {
        String xmlStr = "<books><item><item><bookName>book1</bookName><bookId>101</bookId></item></item><item><item>"
                + "<bookName>book2</bookName><bookId>102</bookId></item></item><item><item><bookName>book3</bookName>"
                + "<bookId>103</bookId></item></item></books>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(),
                "{\"books\":{\"item\":[{\"item\":{\"bookName\":\"book1\",\"bookId\":\"101\"}},{\"item\":"
                        + "{\"bookName\":\"book2\",\"bookId\":\"102\"}},{\"item\":{\"bookName\":\"book3\","
                        + "\"bookId\":\"103\"}}]}}");
    }

    @Test
    public void testToJsonWithArray() {
        String xmlStr = "<books><item>book1</item><item>book2</item><item>book3</item></books>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"books\":{\"item\":[\"book1\",\"book2\",\"book3\"]}}");
    }

    @Test
    public void testToJSONWithSequenceDistinctKeys() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSONWithSequenceDistinctKeys");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"key1\":\"value1\",\"key2\":\"value2\"}");
    }

    @Test
    public void testToJSONWithSequenceSimilarKeys() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSONWithSequenceSimilarKeys");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"key\":[\"value1\",\"value2\",\"value3\"]}");
    }

    @Test
    public void testToJSONWithSequenceWithValueArray() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSONWithSequenceWithValueArray");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\",\"b\",\"c\"]");
    }

    @Test
    public void testToJSONWithSequenceWithMultipleElements() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSONWithSequenceWithMultipleElements");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"person\":{\"name\":\"Jack\",\"age\":\"40\"},"
                + "\"metadata\":\"5\"}");
    }

    @Test
    public void testToJSONWithSequenceWithElementAndText() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSONWithSequenceWithElementAndText");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\",\"b\",{\"key\":\"value3\"}]");
    }

    @Test
    public void testToJSONWithSequenceWithElementAndTextArray() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSONWithSequenceWithElementAndTextArray");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\",\"b\",{\"key\":[\"value3\",\"value4\",\"value4\"]}]");
    }

    @Test
    public void testToJSONWithSequenceWithDifferentElements() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSONWithSequenceWithDifferentElements");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "[\"a\",\"b\",{\"key\":[\"value3\",\"value4\",\"value4\"],"
                + "\"bookName\":\"Book1\",\"bookId\":[\"001\",\"001\"]}]");
    }

    @Test
    public void testToJSONWithSequenceWithDifferentComplexElements() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSONWithSequenceWithDifferentComplexElements");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"bookStore\":{\"@status\":\"online\",\"storeName\":\"foo\","
                + "\"postalCode\":\"94\",\"isOpen\":\"true\",\"address\":{\"street\":\"foo\",\"city\":\"94\","
                + "\"country\":\"true\"},\"codes\":{\"item\":[\"4\",\"8\",\"9\"]}},\"metaInfo\":\"some info\"}");
    }

    @Test
    public void testToJSONWithAttributeNamespacesAndPreserveNamespace() {
        String xmlStr = "<ns0:bookStore xmlns:ns0=\"http://sample0.com/test\" status=\"online\" ns0:id = \"10\">"
                + "<ns0:storeName>foo</ns0:storeName><ns0:isOpen>true</ns0:isOpen>"
                + "<ns2:address xmlns:ns2=\"http://sample2.com/test\" status=\"online\" ns0:id = \"10\" "
                + "ns2:code= \"test\">srilanka</ns2:address></ns0:bookStore>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSON", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"ns0:bookStore\":{\"@xmlns:ns0\":"
                + "\"http://sample0.com/test\",\"@status\":\"online\",\"@ns0:id\":\"10\",\"ns0:storeName\":\"foo\","
                + "\"ns0:isOpen\":\"true\",\"ns2:address\":{\"@xmlns:ns2\":\"http://sample2.com/test\","
                + "\"@status\":\"online\",\"@ns0:id\":\"10\",\"@ns2:code\":\"test\",\"#text\":\"srilanka\"}}}");
    }

    @Test
    public void testToJSONWithAttributeNamespacesAndNoPreserveNamespace() {
        String xmlStr = "<ns0:bookStore xmlns:ns0=\"http://sample0.com/test\" status=\"online\" "
                + "ns0:id = \"10\"><ns0:storeName>foo</ns0:storeName><ns0:isOpen>true</ns0:isOpen><ns2:address "
                + "xmlns:ns2=\"http://sample2.com/test\" status=\"online\" ns0:id = \"10\" ns2:code= \"test\">"
                + "srilanka</ns2:address></ns0:bookStore>";
        BValue[] args = { new BXMLItem(xmlStr) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testToJSONWithoutNamespace", args);

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"bookStore\":{\"@status\":\"online\",\"@id\":\"10\","
                + "\"storeName\":\"foo\",\"isOpen\":\"true\",\"address\":{\"@status\":\"online\",\"@id\":\"10\","
                + "\"@code\":\"test\",\"#text\":\"srilanka\"}}}");
    }
    
    @Test
    public void testGetAttributeFromLiteral() {
        BValue[] returns = BLangFunctions.invokeNew(xmlAttrProgFile, "testGetAttributeFromLiteral");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "5");
    }

    @Test
    public void testSelectChildrenWithEmptyNs() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSelectChildrenWithEmptyNs");
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        Assert.assertEquals(returns[0].stringValue(), "<fname>supun</fname><fname>thilina</fname>");
    }

    @Test
    public void testSelectElementsWithEmptyNs() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSelectElementsWithEmptyNs");
        Assert.assertEquals(returns.length, 2);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(((BXMLSequence) returns[0]).value().size(), 2);

        Assert.assertEquals(returns[0].stringValue(), "<fname>supun</fname><fname>thilina</fname>");
    }

    @Test
    public void testSelectDescendants() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSelectDescendants");
        Assert.assertTrue(returns[0] instanceof BXML);
        BXMLSequence seq = (BXMLSequence) returns[0];
        Assert.assertEquals(seq.value().size(), 2);

        Assert.assertEquals(seq.stringValue(), "<name xmlns=\"http://ballerinalang.org/\" "
                + "xmlns:ns0=\"http://ballerinalang.org/aaa\"><name>Supun</name><lname>Setunga</lname></name>"
                + "<name xmlns=\"http://ballerinalang.org/\" xmlns:ns0=\"http://ballerinalang.org/aaa\">John</name>");
    }

    @Test
    public void testSelectDescendantsWithEmptyNs() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSelectDescendantsWithEmptyNs");
        Assert.assertTrue(returns[0] instanceof BXML);
        BXMLSequence seq = (BXMLSequence) returns[0];
        Assert.assertEquals(seq.value().size(), 2);

        Assert.assertEquals(seq.stringValue(), "<name xmlns:ns0=\"http://ballerinalang.org/aaa\"><name>Supun</name>"
                + "<lname>Setunga</lname></name><name xmlns:ns0=\"http://ballerinalang.org/aaa\">John</name>");
    }

    @Test
    public void testSelectDescendantsFromSeq() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testSelectDescendantsFromSeq");
        Assert.assertTrue(returns[0] instanceof BXML);
        BXMLSequence seq = (BXMLSequence) returns[0];
        Assert.assertEquals(seq.value().size(), 3);

        Assert.assertEquals(seq.stringValue(), "<name xmlns=\"http://ballerinalang.org/\" "
                + "xmlns:ns0=\"http://ballerinalang.org/aaa\"><name>Supun</name><lname>Setunga</lname></name>"
                + "<name xmlns=\"http://ballerinalang.org/\" xmlns:ns0=\"http://ballerinalang.org/aaa\">John</name>"
                + "<name xmlns=\"http://ballerinalang.org/\" xmlns:ns0=\"http://ballerinalang.org/aaa\">Doe</name>");
    }

    @Test(expectedExceptions = { BLangRuntimeException.class }, 
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:Error, message: failed to add attribute " +
            "'a:text'. prefix 'a' is already bound to namespace 'yyy'.*")
    public void testUpdateAttributeWithDifferentUri() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testUpdateAttributeWithDifferentUri");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<name xmlns:a=\"yyy\" a:text=\"hello\"/>");
    }
}
