/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.exceptions.BLangTestException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Local function invocation test.
 *
 * @since 0.8.0
 */
public class XMLAttributesTest {

    CompileResult xmlAttrProgFile;

    @BeforeClass
    public void setup() {
        xmlAttrProgFile = BCompileUtil.compile("test-src/types/xml/xml-attributes.bal");
    }

    @Test
    public void testAddAttributeWithString() {
        Object returns = BRunUtil.invoke(xmlAttrProgFile, "testAddAttributeWithString");
        Assert.assertTrue(returns instanceof BXml);
        Assert.assertEquals(returns.toString(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns4=\"http://sample.com/wso2/f\" " +
                        "foo1=\"bar1\" xmlns:ns1=\"http://sample.com/wso2/e\" " +
                        "ns1:foo2=\"bar2\" ns4:foo3=\"bar3\"/>");
    }

    @Test(expectedExceptions = {BLangTestException.class},
            expectedExceptionsMessageRegExp = ".*localname of the attribute cannot be empty.*")
    public void testAddAttributeWithoutLocalname() {
        Object returns = BRunUtil.invoke(xmlAttrProgFile, "testAddAttributeWithoutLocalname");
    }

    @Test
    public void testAddAttributeWithEmptyNamespace() {
        Object returns = BRunUtil.invoke(xmlAttrProgFile, "testAddAttributeWithEmptyNamespace");
        Assert.assertTrue(returns instanceof BXml);
        Assert.assertEquals(returns.toString(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" foo1=\"bar\"/>");
    }

    @Test()
    public void testAddNamespaceAsAttribute1() {
        BArray returns = (BArray) BRunUtil.invoke(xmlAttrProgFile, "testAddNamespaceAsAttribute");
        Assert.assertTrue(returns.get(0) instanceof BXml);
        Assert.assertEquals(returns.get(0).toString(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns4=\"http://wso2.com\"/>");

        Assert.assertTrue(returns.get(1) instanceof BXml);
        Assert.assertEquals(returns.get(1).toString(),
                "<root xmlns=\"http://ballerinalang.org/\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "att=\"http://wso2.com\"/>");
    }

    @Test
    public void testAddAttributeWithQName() {
        Object returns = BRunUtil.invoke(xmlAttrProgFile, "testAddAttributeWithQName");
        Assert.assertTrue(returns instanceof BXml);
        Assert.assertEquals(returns.toString(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns1=\"http://sample.com/wso2/a1\" ns1:foo1=\"bar1\"/>");
    }

    @Test
    public void testAddAttributeWithQName_1() {
        Object returns = BRunUtil.invoke(xmlAttrProgFile, "testAddAttributeWithDiffQName_1");
        Assert.assertTrue(returns instanceof BXml);
        Assert.assertEquals(returns.toString(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns4=\"http://sample.com/wso2/f/\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns5=\"http://sample.com/wso2/f/\" ns5:diff=\"yes\" ns3:foo1=\"bar1\"/>");
    }

    @Test
    public void testAddAttributeWithQName_2() {
        Object returns = BRunUtil.invoke(xmlAttrProgFile, "testAddAttributeWithDiffQName_2");
        Assert.assertTrue(returns instanceof BXml);
        Assert.assertEquals(returns.toString(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns4=\"http://sample.com/wso2/f/\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns5=\"http://sample.com/wso2/f/\" ns5:diff=\"yes\" ns5:foo1=\"bar1\"/>");
    }

    @Test
    public void testAddAttributeWithQName_3() {
        Object returns = BRunUtil.invoke(xmlAttrProgFile, "testAddAttributeWithDiffQName_3");
        Assert.assertTrue(returns instanceof BXml);
        Assert.assertEquals(returns.toString(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns4=\"http://sample.com/wso2/f/\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns5=\"http://sample.com/wso2/f/\" ns5:diff=\"yes\" ns5:foo1=\"bar1\"/>");
    }

    @Test()
    public void testAddAttributeWithQName_5() {
        Object returns = BRunUtil.invoke(xmlAttrProgFile, "testAddAttributeWithDiffQName_5");
        Assert.assertTrue(returns instanceof BXml);
        Assert.assertEquals(returns.toString(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns4=\"http://sample.com/wso2/f/\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns5=\"http://sample.com/wso2/f/\" " +
                        "ns5:diff=\"yes\" foo1=\"bar1\" foo2=\"bar2\" foo3=\"bar3\"/>");
    }

    @Test
    public void testUpdateAttributeWithString() {
        Object returns = BRunUtil.invoke(xmlAttrProgFile, "testUpdateAttributeWithString");
        Assert.assertTrue(returns instanceof BXml);
        Assert.assertEquals(returns.toString(),
                "<root xmlns=\"http://defaultNs/\" " +
                        "xmlns:ns0=\"http://sample.com/wso2/e\" " +
                        "foo1=\"newbar1\" ns0:foo2=\"newbar2\" foo3=\"newbar3\"/>");
    }

    @Test
    public void testUpdateAttributeWithString_1() {
        Object returns = BRunUtil.invoke(xmlAttrProgFile, "testUpdateAttributeWithString_1");
        Assert.assertTrue(returns instanceof BXml);
        String xml = returns.toString();
        int urlPosition = xml.indexOf("http://sample.com/wso2/f/t");
        String preUrl = xml.substring(0, urlPosition);
        int colonPosition = preUrl.lastIndexOf(":");
        int equalPosition = preUrl.lastIndexOf("=");
        String nsPrefixName = preUrl.substring(colonPosition + 1, equalPosition);
        Assert.assertTrue(xml.contains(nsPrefixName + ":foo3=\"newbar3\""));
    }

    @Test
    public void testUpdateNamespaceAsAttribute() {
        Object returns = BRunUtil.invoke(xmlAttrProgFile, "testUpdateNamespaceAsAttribute");
        Assert.assertTrue(returns instanceof BXml);
        Assert.assertEquals(returns.toString(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns3=\"http://wso2.com\"/>");
    }

    @Test
    public void testUpdateAttributeWithQName() {
        Object returns = BRunUtil.invoke(xmlAttrProgFile, "testUpdateAttributeWithQName");
        Assert.assertTrue(returns instanceof BXml);
        Assert.assertEquals(returns.toString(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                        "ns0:foo1=\"newbar1\" ns3:foo2=\"newbar2\"/>");
    }

    @Test
    public void testUpdateAttributeWithQName_1() {
        Object returns = BRunUtil.invoke(xmlAttrProgFile, "testUpdateAttributeWithQName_1");
        Assert.assertTrue(returns instanceof BXml);
        Assert.assertEquals(returns.toString(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                        "xmlns:ns5=\"http://sample.com/wso2/a1\" " +
                        "ns5:foo1=\"newaddedbar1\" ns3:foo2=\"bar2\"/>");
    }

    @Test
    public void testGetAttributeWithString() {
        BArray returns = (BArray) BRunUtil.invoke(xmlAttrProgFile, "testGetAttributeWithString");
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "bar1");

        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(1).toString(), "bar2");

        Assert.assertNull(returns.get(2));
    }

    @Test
    public void testGetAttributeWithoutLocalname() {
        Object returns = BRunUtil.invoke(xmlAttrProgFile, "testGetAttributeWithoutLocalname");
        Assert.assertNull(returns);
    }

    @Test
    public void testGetNamespaceAsAttribute() {
        Object returns = BRunUtil.invoke(xmlAttrProgFile, "testGetNamespaceAsAttribute");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "http://sample.com/wso2/f");
    }

    @Test
    public void testGetAttributeWithQName() {
        BArray returns = (BArray) BRunUtil.invoke(xmlAttrProgFile, "testGetAttributeWithQName");
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "bar1");

        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(1).toString(), "bar2");

        Assert.assertNull(returns.get(2));
    }

    @Test
    public void testUsingQNameAsString() {
        BArray returns = (BArray) BRunUtil.invoke(xmlAttrProgFile, "testUsingQNameAsString");
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "{http://sample.com/wso2/a1}wso2");

        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(1).toString(), "{http://sample.com/wso2/a1}ballerina");
    }

    @Test
    public void testGetAttributeFromSingletonSeq() {
        Object returns = BRunUtil.invoke(xmlAttrProgFile, "testGetAttributeFromSingletonSeq");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "bar");
    }

    @Test(description = "Test map operations on xml@ value")
    public void testMapOperations() {
        BArray returns = (BArray) BRunUtil.invoke(xmlAttrProgFile, "mapOperationsOnXmlAttribute");
        Assert.assertEquals(returns.get(0).toString(), "2");
        Assert.assertEquals(returns.get(1).toString(),
                "[\"{http://www.w3.org/2000/xmlns/}xmlns\",\"foo\"]");
        Assert.assertTrue((Boolean) returns.get(2));
    }

    @Test
    public void testCharacterReferencesInXmlAttributeValue() {
        BRunUtil.invoke(xmlAttrProgFile, "testCharacterReferencesInXmlAttributeValue");
    }

    @Test
    public void testPrintAttribMap() {
        PrintStream original = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            BRunUtil.invoke(xmlAttrProgFile, "testPrintAttribMap");
            Assert.assertEquals(outContent.toString(),
                    "{\"{http://www.w3.org/2000/xmlns/}xmlns\":\"http://sample.com/wso2/c1\",\"name\":\"Foo\"}",
                    "Invalid attribute map printed");
        } finally {
            try {
                outContent.close();
            } catch (Throwable t) {
                // ignore
            }
            System.setOut(original);
        }
    }
    @Test
    public void testAttributesInEmptyXMLSequence() {
        BRunUtil.invoke(xmlAttrProgFile, "testAttributesInEmptyXMLSequence");
    }

    @AfterClass
    public void tearDown() {
        xmlAttrProgFile = null;
    }


}
