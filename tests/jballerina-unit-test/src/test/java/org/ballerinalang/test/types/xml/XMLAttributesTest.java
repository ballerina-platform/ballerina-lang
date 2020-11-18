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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BXML;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testAddAttributeWithString");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns4=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns1=\"http://sample.com/wso2/e\" " +
                        "foo1=\"bar1\" ns1:foo2=\"bar2\" ns4:foo3=\"bar3\"></root>");
    }
    
    @Test (expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*localname of the attribute cannot be empty.*")
    public void testAddAttributeWithoutLocalname() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testAddAttributeWithoutLocalname");
    }
    
    @Test
    public void testAddAttributeWithEmptyNamespace() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testAddAttributeWithEmptyNamespace");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" foo1=\"bar\"></root>");
    }
    
    @Test
    public void testAddNamespaceAsAttribute1() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testAddNamespaceAsAttribute");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns4=\"http://wso2.com\"></root>");
        
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(),
                "<root xmlns=\"http://ballerinalang.org/\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "att=\"http://wso2.com\"></root>");
    }
    
    @Test
    public void testAddAttributeWithQName() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testAddAttributeWithQName");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns1=\"http://sample.com/wso2/a1\" ns1:foo1=\"bar1\"></root>");
    }

    @Test
    public void testAddAttributeWithQName_1() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testAddAttributeWithDiffQName_1");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns4=\"http://sample.com/wso2/f/\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns5=\"http://sample.com/wso2/f/\" ns5:diff=\"yes\" ns3:foo1=\"bar1\"></root>");
    }

    @Test
    public void testAddAttributeWithQName_2() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testAddAttributeWithDiffQName_2");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns4=\"http://sample.com/wso2/f/\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns5=\"http://sample.com/wso2/f/\" ns5:diff=\"yes\" ns5:foo1=\"bar1\"></root>");
    }

    @Test
    public void testAddAttributeWithQName_3() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testAddAttributeWithDiffQName_3");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns4=\"http://sample.com/wso2/f/\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns5=\"http://sample.com/wso2/f/\" ns5:diff=\"yes\" ns5:foo1=\"bar1\"></root>");
    }

    @Test
    public void testAddAttributeWithQName_5() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testAddAttributeWithDiffQName_5");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns4=\"http://sample.com/wso2/f/\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns5=\"http://sample.com/wso2/f/\" " +
                        "ns5:diff=\"yes\" foo1=\"bar1\" foo2=\"bar2\" foo3=\"bar3\"></root>");
    }
    
    @Test
    public void testUpdateAttributeWithString() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testUpdateAttributeWithString");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://defaultNs/\" " +
                        "xmlns:ns0=\"http://sample.com/wso2/e\" " +
                        "foo1=\"newbar1\" ns0:foo2=\"newbar2\" foo3=\"newbar3\"></root>");
    }

    @Test
    public void testUpdateAttributeWithString_1() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testUpdateAttributeWithString_1");
        Assert.assertTrue(returns[0] instanceof BXML);
        String xml = returns[0].stringValue();
        int urlPosition = xml.indexOf("http://sample.com/wso2/f/t");
        String preUrl = xml.substring(0, urlPosition);
        int colonPosition = preUrl.lastIndexOf(":");
        int equalPosition = preUrl.lastIndexOf("=");
        String nsPrefixName = preUrl.substring(colonPosition + 1, equalPosition);
        Assert.assertTrue(xml.contains(nsPrefixName + ":foo3=\"newbar3\""));
    }
    
    @Test
    public void testUpdateNamespaceAsAttribute() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testUpdateNamespaceAsAttribute");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns3=\"http://wso2.com\"></root>");
    }
    
    @Test
    public void testUpdateAttributeWithQName() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testUpdateAttributeWithQName");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                        "ns0:foo1=\"newbar1\" ns3:foo2=\"newbar2\"></root>");
    }

    @Test
    public void testUpdateAttributeWithQName_1() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testUpdateAttributeWithQName_1");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                        "xmlns:ns5=\"http://sample.com/wso2/a1\" " +
                        "ns5:foo1=\"newaddedbar1\" ns3:foo2=\"bar2\"></root>");
    }
    
    @Test
    public void testGetAttributeWithString() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testGetAttributeWithString");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "bar1");
        
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "bar2");
        
        Assert.assertNull(returns[2]);
    }
    
    @Test
    public void testGetAttributeWithoutLocalname() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testGetAttributeWithoutLocalname");
        Assert.assertNull(returns[0]);
    }

    @Test
    public void testGetNamespaceAsAttribute() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testGetNamespaceAsAttribute");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "http://sample.com/wso2/f");
    }

    @Test
    public void testGetAttributeWithQName() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testGetAttributeWithQName");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "bar1");

        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "bar2");

        Assert.assertNull(returns[2]);
    }

    @Test
    public void testUsingQNameAsString() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testUsingQNameAsString");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "{http://sample.com/wso2/a1}wso2");

        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "{http://sample.com/wso2/a1}ballerina");
    }

    @Test
    public void testGetAttributeFromSingletonSeq() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testGetAttributeFromSingletonSeq");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "bar");
    }

    @Test (description = "Test map operations on xml@ value")
    public void testMapOperations() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "mapOperationsOnXmlAttribute");
        Assert.assertEquals(returns[0].stringValue(), "2");
        Assert.assertEquals(returns[1].stringValue(),
                "[\"{http://www.w3.org/2000/xmlns/}xmlns\", \"foo\"]");
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
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
}
