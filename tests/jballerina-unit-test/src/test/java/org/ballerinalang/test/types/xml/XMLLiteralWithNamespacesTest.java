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

import io.ballerina.runtime.XMLFactory;
import io.ballerina.runtime.values.XMLValue;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.model.values.BXML;
import org.ballerinalang.core.model.values.BXMLItem;
import org.ballerinalang.core.model.values.BXMLSequence;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
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

    @Test (enabled = false)
    public void testElementLiteralWithNamespaces() {
        BValue[] returns =
                BRunUtil.invoke(literalWithNamespacesResult, "testElementLiteralWithNamespaces");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://ballerina.com/\" " +
                        "xmlns:ns0=\"http://ballerina.com/a\" " +
                        "ns0:id=\"456\"><foo>123</foo><bar " +
                        "xmlns:ns1=\"http://ballerina.com/c\" ns1:status=\"complete\"></bar></root>");

        Assert.assertTrue(returns[1] instanceof BXML);
        BXMLSequence seq = (BXMLSequence) returns[1];
        Assert.assertEquals(seq.stringValue(),
                "<foo xmlns=\"http://ballerina.com/\">123</foo><bar " +
                        "xmlns=\"http://ballerina.com/\" " +
                        "xmlns:ns1=\"http://ballerina.com/c\" " +
                        "ns1:status=\"complete\"></bar>");

        BValueArray items = seq.value();
        Assert.assertEquals(items.size(), 2);
    }

    @Test (enabled = false)
    public void testElementWithQualifiedName() {
        BValue[] returns = BRunUtil.invoke(literalWithNamespacesResult, "testElementWithQualifiedName");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root>hello</root>");

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(),
                "<root xmlns=\"http://ballerina.com/\">hello</root>");

        Assert.assertTrue(returns[2] instanceof BXML);
        Assert.assertEquals(returns[2].stringValue(),
                "<ns1:root xmlns:ns1=\"http://ballerina.com/b\" xmlns=\"http://ballerina.com/\">hello</ns1:root>");
    }

    @Test (enabled = false)
    public void testDefineInlineNamespace() {
        BValue[] returns = BRunUtil.invoke(literalWithNamespacesResult, "testDefineInlineNamespace");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<nsx:foo xmlns:nsx=\"http://wso2.com\" nsx:id=\"123\">hello</nsx:foo>");
    }

    @Test (enabled = false)
    public void testDefineInlineDefaultNamespace() {
        BValue[] returns =
                BRunUtil.invoke(literalWithNamespacesResult, "testDefineInlineDefaultNamespace");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<foo xmlns=\"http://ballerina.com/default/namespace\" xmlns:nsx=\"http://wso2.com/aaa\">hello</foo>");

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(),
                "<foo xmlns=\"http://wso2.com\" xmlns:nsx=\"http://wso2.com/aaa\">hello</foo>");
    }

    @Test
    public void testUsingNamespcesOfParent() {
        BValue[] returns = BRunUtil.invoke(literalWithNamespacesResult, "testUsingNamespcesOfParent");
        Assert.assertTrue(returns[0] instanceof BXMLItem);

        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns:ns0=\"http://ballerinalang.com/\"><ns0:foo>hello</ns0:foo></root>");
    }

    @Test(enabled = false)
    public void testComplexXMLLiteral() throws IOException {
        BValue[] returns = BRunUtil.invoke(literalWithNamespacesResult, "testComplexXMLLiteral");
        Assert.assertTrue(returns[0] instanceof BXMLItem);
        Assert.assertEquals(returns[0].stringValue(),
                BCompileUtil.readFileAsString("test-src/types/xml/sampleXML.txt"));
    }

    @Test
    public void testNamespaceDclr() {
        BValue[] returns = BRunUtil.invoke(literalWithNamespacesResult, "testNamespaceDclr");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "{http://sample.com/wso2/a2}foo");

        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "{http://sample.com/wso2/b2}foo");

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "{http://sample.com/wso2/d2}foo");
    }

    @Test
    public void testInnerScopeNamespaceDclr() {
        BValue[] returns = BRunUtil.invoke(literalWithNamespacesResult, "testInnerScopeNamespaceDclr");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "{http://ballerina.com/b}foo");

        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "{http://sample.com/wso2/a3}foo");

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "{http://ballerina.com/b}foo");
    }

    @Test (enabled = false)
    public void testPackageLevelXML() {
        CompileResult result = BCompileUtil.compile("test-src/types/xml/package_level_xml_literals.bal");
        BValue[] returns = BRunUtil.invoke(result, "testPackageLevelXML");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<p:person xmlns:p=\"foo\" xmlns:q=\"bar\">hello</p:person>");

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(),
                "<ns1:student xmlns:ns1=\"http://ballerina.com/b\">hello</ns1:student>");
    }

    @Test (enabled = false)
    public void testObjectLevelXML() {
        BValue[] returns = BRunUtil.invoke(literalWithNamespacesResult, "testObjectLevelXML");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<p:person xmlns:p=\"foo\" xmlns:q=\"bar\">hello</p:person>");
    }


    @Test
    public void xmlWithDefaultNamespaceToString() {
        BValue[] returns = BRunUtil.invoke(literalWithNamespacesResult, "XMLWithDefaultNamespaceToString");
        Assert.assertEquals(returns[0].stringValue(),
                "<Order xmlns=\"http://acme.company\" xmlns:acme=\"http://acme.company\">\n" +
                        "        <OrderLines>\n" +
                        "            <OrderLine acme:lineNo=\"334\" itemCode=\"334-2\"></OrderLine>\n" +
                        "        </OrderLines>\n" +
                        "        <ShippingAddress>\n" +
                        "        </ShippingAddress>\n" +
                        "    </Order>");
    }

    @Test (enabled = false)
    public void testXMLSerialize() {
        BValue[] returns = BRunUtil.invoke(literalWithNamespacesResult, "getXML");
        Assert.assertTrue(returns[0] instanceof BXML);

        XMLValue xmlItem = (XMLValue) XMLFactory.parse(returns[0].stringValue());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        xmlItem.serialize(baos);
        Assert.assertEquals(new String(baos.toByteArray()),
                "<foo xmlns=\"http://wso2.com/\">hello</foo>");
    }

    @Test
    public void testXMLToString() {
        io.ballerina.runtime.api.values.BXML xml = XMLFactory.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<!DOCTYPE foo [<!ELEMENT foo ANY ><!ENTITY data \"Example\" >]><foo>&data;</foo>");
        Assert.assertEquals(xml.toString(), "<foo>Example</foo>");
    }
}
