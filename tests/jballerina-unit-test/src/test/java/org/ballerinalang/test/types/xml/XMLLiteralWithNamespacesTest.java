/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.types.xml;

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlItem;
import io.ballerina.runtime.api.values.BXmlSequence;
import io.ballerina.runtime.internal.XmlFactory;
import io.ballerina.runtime.internal.values.XmlValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.util.BFileUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Test class for XML literal.
 *
 * @since 0.94
 */
@Test
public class XMLLiteralWithNamespacesTest {

    private CompileResult literalWithNamespacesResult;

    @BeforeClass
    public void setup() {
        literalWithNamespacesResult = BCompileUtil.compile("test-src/types/xml/xml-literals-with-namespaces.bal");
    }

    @Test
    public void testElementLiteralWithNamespaces() {
        BArray returns = (BArray)
                BRunUtil.invoke(literalWithNamespacesResult, "testElementLiteralWithNamespaces");
        Assert.assertTrue(returns.get(0) instanceof BXml);
        Assert.assertEquals(returns.get(0).toString(),
                "<root xmlns=\"http://ballerina.com/\" " +
                        "xmlns:ns0=\"http://ballerina.com/a\" " +
                        "ns0:id=\"456\"><foo>123</foo><bar " +
                        "xmlns:ns1=\"http://ballerina.com/c\" ns1:status=\"complete\"/></root>");

        Assert.assertTrue(returns.get(1) instanceof BXml);
        BXmlSequence seq = (BXmlSequence) returns.get(1);
        Assert.assertEquals(seq.toString(),
                "<foo xmlns=\"http://ballerina.com/\">123</foo><bar " +
                        "xmlns=\"http://ballerina.com/\" " +
                        "xmlns:ns1=\"http://ballerina.com/c\" " +
                        "ns1:status=\"complete\"/>");

        BArray items = (BArray) seq.value();
        Assert.assertEquals(items.size(), 2);
    }

    @Test
    public void testElementWithQualifiedName() {
        BArray returns = (BArray) BRunUtil.invoke(literalWithNamespacesResult, "testElementWithQualifiedName");
        Assert.assertTrue(returns.get(0) instanceof BXml);
        Assert.assertEquals(returns.get(0).toString(), "<root>hello</root>");

        Assert.assertTrue(returns.get(1) instanceof BXml);
        Assert.assertEquals(returns.get(1).toString(),
                "<root xmlns=\"http://ballerina.com/\">hello</root>");

        Assert.assertTrue(returns.get(2) instanceof BXml);
        Assert.assertEquals(returns.get(2).toString(),
                "<ns1:root xmlns=\"http://ballerina.com/\" xmlns:ns1=\"http://ballerina.com/b\">hello</ns1:root>");
    }

    @Test
    public void testDefineInlineNamespace() {
        Object returns = BRunUtil.invoke(literalWithNamespacesResult, "testDefineInlineNamespace");
        Assert.assertTrue(returns instanceof BXml);
        Assert.assertEquals(returns.toString(),
                "<nsx:foo xmlns:nsx=\"http://wso2.com\" nsx:id=\"123\">hello</nsx:foo>");
    }

    @Test
    public void testDefineInlineDefaultNamespace() {
        BArray returns = (BArray)
                BRunUtil.invoke(literalWithNamespacesResult, "testDefineInlineDefaultNamespace");
        Assert.assertTrue(returns.get(0) instanceof BXml);
        Assert.assertEquals(returns.get(0).toString(),
                "<foo xmlns=\"http://ballerina.com/default/namespace\" xmlns:nsx=\"http://wso2.com/aaa\">hello</foo>");

        Assert.assertTrue(returns.get(1) instanceof BXml);
        Assert.assertEquals(returns.get(1).toString(),
                "<foo xmlns=\"http://wso2.com\" xmlns:nsx=\"http://wso2.com/aaa\">hello</foo>");
    }

    @Test
    public void testUsingNamespcesOfParent() {
        Object returns = BRunUtil.invoke(literalWithNamespacesResult, "testUsingNamespcesOfParent");
        Assert.assertTrue(returns instanceof BXmlItem);

        Assert.assertEquals(returns.toString(),
                "<root xmlns:ns0=\"http://ballerinalang.com/\"><ns0:foo>hello</ns0:foo></root>");
    }

    @Test(enabled = false)
    public void testComplexXMLLiteral() throws IOException {
        Object returns = BRunUtil.invoke(literalWithNamespacesResult, "testComplexXMLLiteral");
        Assert.assertTrue(returns instanceof BXmlItem);
        Assert.assertEquals(returns.toString(), BFileUtil.readFileAsString(
                "src/test/resources/test-src/types/xml/sampleXML.txt"));
    }

    @Test
    public void testNamespaceDclr() {
        BArray returns = (BArray) BRunUtil.invoke(literalWithNamespacesResult, "testNamespaceDclr");
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "{http://sample.com/wso2/a2}foo");

        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(1).toString(), "{http://sample.com/wso2/b2}foo");

        Assert.assertTrue(returns.get(2) instanceof BString);
        Assert.assertEquals(returns.get(2).toString(), "{http://sample.com/wso2/d2}foo");
    }

    @Test
    public void testInnerScopeNamespaceDclr() {
        BArray returns = (BArray) BRunUtil.invoke(literalWithNamespacesResult, "testInnerScopeNamespaceDclr");
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "{http://ballerina.com/b}foo");

        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(1).toString(), "{http://sample.com/wso2/a3}foo");

        Assert.assertTrue(returns.get(2) instanceof BString);
        Assert.assertEquals(returns.get(2).toString(), "{http://ballerina.com/b}foo");
    }

    @Test
    public void testPackageLevelXML() {
        CompileResult result = BCompileUtil.compile("test-src/types/xml/package_level_xml_literals.bal");
        BArray returns = (BArray) BRunUtil.invoke(result, "testPackageLevelXML");
        Assert.assertTrue(returns.get(0) instanceof BXml);
        Assert.assertEquals(returns.get(0).toString(),
                "<p:person xmlns:p=\"foo\" xmlns:q=\"bar\">hello</p:person>");

        Assert.assertTrue(returns.get(1) instanceof BXml);
        Assert.assertEquals(returns.get(1).toString(),
                "<ns1:student xmlns:ns1=\"http://ballerina.com/b\">hello</ns1:student>");
    }

    @Test
    public void testObjectLevelXML() {
        Object returns = BRunUtil.invoke(literalWithNamespacesResult, "testObjectLevelXML");
        Assert.assertTrue(returns instanceof BXml);
        Assert.assertEquals(returns.toString(),
                "<p:person xmlns:p=\"foo\" xmlns:q=\"bar\">hello</p:person>");
    }

    @Test
    public void xmlWithDefaultNamespaceToString() {
        Object returns = BRunUtil.invoke(literalWithNamespacesResult, "XMLWithDefaultNamespaceToString");
        Assert.assertEquals(returns.toString(),
                "<Order xmlns=\"http://acme.company\" xmlns:acme=\"http://acme.company.nondefault\">\n" +
                        "        <OrderLines>\n" +
                        "            <OrderLine acme:lineNo=\"334\" itemCode=\"334-2\"/>\n" +
                        "        </OrderLines>\n" +
                        "        <ShippingAddress>\n" +
                        "        </ShippingAddress>\n" +
                        "    </Order>");
    }

    @Test
    public void testXMLSerialize() {
        Object returns = BRunUtil.invoke(literalWithNamespacesResult, "getXML");
        Assert.assertTrue(returns instanceof BXml);

        XmlValue xmlItem = (XmlValue) XmlFactory.parse(returns.toString());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        xmlItem.serialize(baos);
        Assert.assertEquals(new String(baos.toByteArray()),
                "<foo xmlns=\"http://wso2.com/\">hello</foo>");
    }

    @Test
    public void testXMLToString() {
        BXml xml = XmlFactory.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<!DOCTYPE foo [<!ELEMENT foo ANY ><!ENTITY data \"Example\" >]><foo>&data;</foo>");
        Assert.assertEquals(xml.toString(), "<foo>Example</foo>");
    }

    @Test (dataProvider = "xmlValueFunctions")
    public void testXmlStrings(String functionName) {
        BRunUtil.invoke(literalWithNamespacesResult, functionName);
    }

    @DataProvider(name = "xmlValueFunctions")
    private String[] xmlValueFunctions() {
        return new String[]{
                "testXmlLiteralUsingXmlNamespacePrefix",
                "testXmlInterpolationWithQuery",
                "testAddAttributeToDefaultNS"
        };
    }

    @AfterClass
    public void tearDown() {
        literalWithNamespacesResult = null;
    }
}
