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
package org.wso2.ballerina.core.nativeimpl.functions;

import org.apache.axiom.om.OMElement;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BXML;
import org.wso2.ballerina.core.nativeimpl.lang.xml.AddAttribute;
import org.wso2.ballerina.core.nativeimpl.lang.xml.AddElement;
import org.wso2.ballerina.core.nativeimpl.lang.xml.GetString;
import org.wso2.ballerina.core.nativeimpl.lang.xml.GetXML;
import org.wso2.ballerina.core.nativeimpl.lang.xml.Remove;
import org.wso2.ballerina.core.nativeimpl.lang.xml.SetString;
import org.wso2.ballerina.core.nativeimpl.lang.xml.SetXML;
import org.wso2.ballerina.core.nativeimpl.lang.xml.ToString;
import org.wso2.ballerina.core.utils.FunctionUtils;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

/**
 * Test Native function in ballerina.lang.xml
 */
public class XMLTest {

    private BallerinaFile bFile;
    private static final String s1 = "<persons><person><name>Jack</name><address>wso2</address></person></persons>";
    private static final String s2 = "<person><name>Jack</name></person>";

    @BeforeClass
    public void setup() {
        // Add Native functions.
        SymScope symScope = new SymScope(null);
        FunctionUtils.addNativeFunction(symScope, new GetString());
        FunctionUtils.addNativeFunction(symScope, new GetXML());
        FunctionUtils.addNativeFunction(symScope, new Remove());
        FunctionUtils.addNativeFunction(symScope, new SetString());
        FunctionUtils.addNativeFunction(symScope, new SetXML());
        FunctionUtils.addNativeFunction(symScope, new ToString());
        FunctionUtils.addNativeFunction(symScope, new AddAttribute());
        FunctionUtils.addNativeFunction(symScope, new AddElement());

        bFile = ParserUtils.parseBalFile("samples/nativeimpl/xmlTest.bal", symScope);
    }

    @Test
    public void testGetString() {
        BValue[] args = {new BXML(s1), new BString("/persons/person/name/text()")};
        BValue[] returns = Functions.invoke(bFile, "getString", args);

        Assert.assertTrue(returns[0] instanceof BString);

        final String expected = "Jack";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testGetXML() {
        BValue[] args = {new BXML(s1), new BString("/persons/person")};
        BValue[] returns = Functions.invoke(bFile, "getXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMElement returnElement = ((BXML) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<person><name>Jack</name>" +
                "<address>wso2</address></person>");
    }

    @Test
    public void testSetString() {
        BValue[] args = {new BXML(s1), new BString("/persons/person/name/text()"), new BString("Peter")};
        BValue[] returns = Functions.invoke(bFile, "setString", args);

        Assert.assertEquals(returns[0].stringValue(), "<persons><person><name>Peter" +
                "</name><address>wso2</address></person></persons>");
    }

    @Test
    public void testSetXML() {
        BValue[] args = {new BXML(s2), new BString("/person/name"),
                new BXML("<name><fname>Jack</fname><lname>Peter</lname></name>")};
        BValue[] returns = Functions.invoke(bFile, "setXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMElement returnElement = ((BXML) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<person><name><fname>Jack" +
                "</fname><lname>Peter</lname></name></person>");
    }

    @Test
    public void testAddElement() {
        BValue[] args = {new BXML(s2), new BString("/person"), new BXML("<address>wso2</address>")};
        BValue[] returns = Functions.invoke(bFile, "addElement", args);

        Assert.assertEquals(returns[0].stringValue(), "<person><name>Jack</name>" +
                "<address>wso2</address></person>");
    }

    @Test
    public void testAddAttribute() {
        BValue[] args = {new BXML(s2), new BString("/person/name"), new BString("id"), new BString("person123")};
        BValue[] returns = Functions.invoke(bFile, "addAttribute", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMElement returnElement = ((BXML) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), "<person><name id=\"person123\">Jack</name></person>");
    }

    @Test
    public void testRemove() {
        BValue[] args = {new BXML(s1), new BString("/persons/person/address")};
        BValue[] returns = Functions.invoke(bFile, "remove", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMElement returnElement = ((BXML) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), "<persons><person><name>Jack" +
                "</name></person></persons>");
    }


    @Test(expectedExceptions = BallerinaException.class)
    public void testGetNonExistingString() {
        BValue[] args = {new BXML(s1), new BString("/xxx/text()")};
        BValue[] returns = Functions.invoke(bFile, "getString", args);

        Assert.assertEquals(returns[0], null);
    }

    @Test(expectedExceptions = BallerinaException.class)
    public void testGetNonExistingXML() {
        BValue[] args = {new BXML(s1), new BString("/xxx")};
        BValue[] returns = Functions.invoke(bFile, "getXML", args);

        Assert.assertEquals(returns[0], null);
    }

    @Test
    public void testSetStringToNonExistingElement() {
        BValue[] args = {new BXML(s1), new BString("/xxx/text()"), new BString("Peter")};
        BValue[] returns = Functions.invoke(bFile, "setString", args);

        Assert.assertEquals(returns[0].stringValue(), s1);
    }

    @Test
    public void testSetXMLToNonExistingElement() {
        BValue[] args = {new BXML(s2), new BString("/xxx"),
                new BXML("<name><fname>Jack</fname><lname>Peter</lname></name>")};
        BValue[] returns = Functions.invoke(bFile, "setXML", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMElement returnElement = ((BXML) returns[0]).value();
        Assert.assertEquals(returnElement.toString().replaceAll("\\r|\\n|\\t| ", ""), s2);
    }

    @Test(expectedExceptions = BallerinaException.class)
    public void testAddElementToNonExistingElement() {
        BValue[] args = {new BXML(s2), new BString("/xxx"), new BXML("<address>wso2</address>")};
        BValue[] returns = Functions.invoke(bFile, "addElement", args);

        Assert.assertEquals(returns[0].stringValue(), s2);
    }

    @Test(expectedExceptions = BallerinaException.class)
    public void testAddAttributeToNonExistingElement() {
        BValue[] args = {new BXML(s2), new BString("/xxx"), new BString("id"), new BString("person123")};
        BValue[] returns = Functions.invoke(bFile, "addAttribute", args);

        Assert.assertTrue(returns[0] instanceof BXML);

        OMElement returnElement = ((BXML) returns[0]).value();
        Assert.assertEquals(returnElement.toString(), s2);
    }

    @Test
    public void testRemoveNonExistingElement() {
        BValue[] args = {new BXML(s1), new BString("/xxx")};
        Functions.invoke(bFile, "remove", args);
    }


    @Test(expectedExceptions = {BallerinaException.class})
    public void testGetStringFromMalformedXpath() {
        BValue[] args = {new BXML(s1), new BString("$worng#path")};
        Functions.invoke(bFile, "getString", args);
    }

    @Test(expectedExceptions = {BallerinaException.class})
    public void testGetXMLFromMalformedXpath() {
        BValue[] args = {new BXML(s1), new BString("$worng#path")};
        Functions.invoke(bFile, "getXML", args);
    }

    @Test(expectedExceptions = {BallerinaException.class})
    public void testSetStringToMalformedXpath() {
        BValue[] args = {new BXML(s1), new BString("$worng#path"), new BString("Peter")};
        Functions.invoke(bFile, "setString", args);
    }

    @Test(expectedExceptions = {BallerinaException.class})
    public void testSetXMLToMalformedXpath() {
        BValue[] args = {new BXML(s2), new BString("$worng#path"),
                new BXML("<name><fname>Jack</fname><lname>Peter</lname></name>")};
        Functions.invoke(bFile, "setXML", args);
    }

    @Test(expectedExceptions = {BallerinaException.class})
    public void testAddElementToMalformedXpath() {
        BValue[] args = {new BXML(s2), new BString("$worng#path"), new BXML("<address>wso2</address>")};
        Functions.invoke(bFile, "addElement", args);
    }

    @Test(expectedExceptions = {BallerinaException.class})
    public void testAddAttributeToMalformedXpath() {
        BValue[] args = {new BXML(s2), new BString("$worng#path"), new BString("id"), new BString("person123")};
        Functions.invoke(bFile, "addAttribute", args);
    }

    @Test(expectedExceptions = {BallerinaException.class})
    public void testRemoveFromMalformedXpath() {
        BValue[] args = {new BXML(s1), new BString("$worng#path")};
        Functions.invoke(bFile, "remove", args);
    }
}
