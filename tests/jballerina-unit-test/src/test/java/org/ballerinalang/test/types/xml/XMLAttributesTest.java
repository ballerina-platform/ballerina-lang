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

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
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
                        "xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                        "xmlns:ns1=\"http://sample.com/wso2/b1\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/d1\" " +
                        "xmlns:ns2=\"http://sample.com/wso2/e\" " +
                        "foo1=\"bar1\" ns2:foo2=\"bar2\" ns4:foo3=\"bar3\"></root>");
    }
    
    @Test(expectedExceptions = {BLangRuntimeException.class}, 
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
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                        "xmlns:ns1=\"http://sample.com/wso2/b1\" foo1=\"bar\"></root>");
    }
    
    @Test
    public void testAddNamespaceAsAttribute1() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testAddNamespaceAsAttribute");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                        "xmlns:ns1=\"http://sample.com/wso2/b1\" " +
                        "xmlns:ns4=\"http://wso2.com\"></root>");
        
        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(),
                "<root xmlns=\"http://ballerinalang.org/\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                        "xmlns:ns1=\"http://sample.com/wso2/b1\" att=\"http://wso2.com\"></root>");
    }
    
    @Test
    public void testAddAttributeWithQName() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testAddAttributeWithQName");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://sample.com/wso2/c1\" xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                        "xmlns:ns1=\"http://sample.com/wso2/b1\" ns0:foo1=\"bar1\"></root>");
    }

    @Test
    public void testAddAttributeWithQName_1() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testAddAttributeWithDiffQName_1");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns4=\"http://sample.com/wso2/f/\" " +
                        "xmlns:ns5=\"http://sample.com/wso2/f/\" " +
                        "xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                        "xmlns:ns1=\"http://sample.com/wso2/b1\" ns5:diff=\"yes\" ns3:foo1=\"bar1\"></root>");
    }

    @Test
    public void testAddAttributeWithQName_2() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testAddAttributeWithDiffQName_2");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns4=\"http://sample.com/wso2/f/\" " +
                        "xmlns:ns5=\"http://sample.com/wso2/f/\" " +
                        "xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                        "xmlns:ns1=\"http://sample.com/wso2/b1\" ns5:diff=\"yes\" ns5:foo1=\"bar1\"></root>");
    }

    @Test
    public void testAddAttributeWithQName_3() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testAddAttributeWithDiffQName_3");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns4=\"http://sample.com/wso2/f/\" " +
                        "xmlns:ns5=\"http://sample.com/wso2/f/\" " +
                        "xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                        "xmlns:ns1=\"http://sample.com/wso2/b1\" ns5:diff=\"yes\" ns5:foo1=\"bar1\"></root>");
    }

    @Test
    public void testAddAttributeWithQName_5() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testAddAttributeWithDiffQName_5");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root " +
                        "xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns4=\"http://sample.com/wso2/f/\" " +
                        "xmlns:ns5=\"http://sample.com/wso2/f/\" " +
                        "xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                        "xmlns:ns1=\"http://sample.com/wso2/b1\" " +
                        "ns5:diff=\"yes\" foo1=\"bar1\" foo2=\"bar2\" foo3=\"bar3\"></root>");
    }
    
    @Test
    public void testUpdateAttributeWithString() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testUpdateAttributeWithString");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns=\"http://defaultNs/\" " +
                "xmlns:ns0=\"http://sample.com/wso2/e\" xmlns:ns1=\"http://sample.com/wso2/b1\" " +
                "xmlns:ns3=\"http://sample.com/wso2/d1\" foo1=\"newbar1\" ns0:foo2=\"newbar2\" foo3=\"newbar3\">" +
                "</root>");
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
                        "xmlns:ns3=\"http://wso2.com\" " +
                        "xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                        "xmlns:ns1=\"http://sample.com/wso2/b1\"></root>");
    }
    
    @Test
    public void testUpdateAttributeWithQName() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testUpdateAttributeWithQName");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/f\" " +
                        "xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                        "xmlns:ns1=\"http://sample.com/wso2/b1\" " +
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
                        "xmlns:ns1=\"http://sample.com/wso2/b1\" " +
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
    public void testGetAttributeWithEmptyNamespace() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testGetAttributeWithEmptyNamespace");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "bar1");
        
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "bar1");
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
    public void testGetAttributesAsMap() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testGetAttributesAsMap");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(),
                "{\"{http://www.w3.org/2000/xmlns/}xmlns\":\"http://sample.com/wso2/c1\", " +
                        "\"{http://www.w3.org/2000/xmlns/}ns0\":\"http://sample.com/wso2/a1\", " +
                        "\"{http://www.w3.org/2000/xmlns/}ns1\":\"http://sample.com/wso2/b1\", " +
                        "\"{http://www.w3.org/2000/xmlns/}ns3\":\"http://sample.com/wso2/d1\", " +
                        "\"{http://sample.com/wso2/a1}foo1\":\"bar1\", " +
                        "\"{http://sample.com/wso2/c1}foo2\":\"bar2\"}");

        Assert.assertTrue(returns[1] instanceof BMap);
        Assert.assertEquals(returns[1].stringValue(),
                "{\"{http://www.w3.org/2000/xmlns/}xmlns\":\"http://sample.com/default/namepsace\", " +
                        "\"{http://www.w3.org/2000/xmlns/}ns0\":\"http://sample.com/wso2/a1\", " +
                        "\"{http://www.w3.org/2000/xmlns/}ns1\":\"http://sample.com/wso2/b1\", " +
                        "\"{http://www.w3.org/2000/xmlns/}ns3\":\"http://sample.com/wso2/d1\", " +
                        "\"{http://sample.com/wso2/a1}foo1\":\"bar1\", " +
                        "\"{http://sample.com/default/namepsace}foo2\":\"bar2\"}");

        Assert.assertTrue(returns[2] instanceof BString);
        Assert.assertEquals(returns[2].stringValue(), "bar1");

        Assert.assertTrue(returns[3] instanceof BString);
        Assert.assertEquals(returns[3].stringValue(), "bar1");
    }
    
    @Test
    public void testXMLAttributesToAny() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testXMLAttributesToAny");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(),
                "{\"{http://www.w3.org/2000/xmlns/}xmlns\":\"http://sample.com/wso2/c1\", " +
                        "\"{http://www.w3.org/2000/xmlns/}ns0\":\"http://sample.com/wso2/a1\", " +
                        "\"{http://www.w3.org/2000/xmlns/}ns1\":\"http://sample.com/wso2/b1\", " +
                        "\"{http://www.w3.org/2000/xmlns/}ns3\":\"http://sample.com/wso2/d1\", " +
                        "\"{http://sample.com/wso2/a1}foo1\":\"bar1\", " +
                        "\"{http://sample.com/wso2/c1}foo2\":\"bar2\"}");
    }
    
    @Test
    public void testRuntimeNamespaceLookup() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testRuntimeNamespaceLookup");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns401=\"http://sample.com/wso2/a1\" " +
                        "xmlns:ns402=\"http://sample.com/wso2/d2\" " +
                        "xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                        "xmlns:ns1=\"http://sample.com/wso2/b1\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/d1\" " +
                        "xmlns:ns2=\"http://sample.com/wso2/e3\" " +
                        "xmlns:ns4=\"http://sample.com/wso2/f3\" " +
                        "ns0:foo1=\"bar1\" ns1:foo2=\"bar2\" ns2:foo3=\"bar3\" ns4:foo4=\"bar4\"></root>");
    }
    
    @Test
    public void testRuntimeNamespaceLookupPriority() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testRuntimeNamespaceLookupPriority");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:p1=\"http://wso2.com\" xmlns:p2=\"http://sample.com/wso2/a1\" " +
                        "xmlns:ns401=\"http://sample.com/wso2/a1\" xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                        "xmlns:ns1=\"http://sample.com/wso2/b1\" xmlns:ns3=\"http://sample.com/wso2/d1\" " +
                        "ns0:foo1=\"bar1\" p1:foo2=\"bar2\"></root>");
    }

    @Test
    public void testSetAttributes() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testSetAttributes");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:p1=\"http://wso2.com\" " +
                        "xmlns:p2=\"http://sample.com/wso2/a1\" " +
                        "xmlns:ns0=\"http://sample.com/wso2/a1\" " +
                        "xmlns:ns1=\"http://sample.com/wso2/b1\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/d1\" " +
                        "foo1=\"bar1\" p1:foo2=\"bar2\" ns0:foo3=\"bar3\"></root>");
    }

    @Test
    public void testGetAttributeFromSingletonSeq() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testGetAttributeFromSingletonSeq");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "bar");
    }

    @Test
    public void testGetAttributeFromLiteral() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testGetAttributeFromLiteral");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "5");
    }

    @Test(description = "Test getting a xml attributes as a map using xmlElement@ syntax")
    public void testGetAttributeMap() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testGetAttributeMap");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(),
                "{\"{http://www.w3.org/2000/xmlns/}xmlns\":\"http://sample.com/wso2/c1\", " +
                        "\"{http://www.w3.org/2000/xmlns/}p1\":\"http://wso2.com/\", " +
                        "\"{http://www.w3.org/2000/xmlns/}p2\":\"http://sample.com/wso2/a1/\", " +
                        "\"{http://www.w3.org/2000/xmlns/}ns0\":\"http://sample.com/wso2/a1\", " +
                        "\"{http://www.w3.org/2000/xmlns/}ns1\":\"http://sample.com/wso2/b1\", " +
                        "\"{http://www.w3.org/2000/xmlns/}ns3\":\"http://sample.com/wso2/d1\", " +
                        "\"{http://wso2.com/}foo\":\"bar\"}");
    }

    @Test(description = "Test pass xml attributes as a argument to a function")
    public void testPassXmlAttributeAsAMap() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "passXmlAttrToFunction");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertEquals(returns[0].stringValue(),
                "{\"{http://www.w3.org/2000/xmlns/}xmlns\":\"http://sample.com/wso2/c1\", " +
                        "\"{http://www.w3.org/2000/xmlns/}ns0\":\"http://sample.com/wso2/a1\", " +
                        "\"{http://www.w3.org/2000/xmlns/}ns1\":\"http://sample.com/wso2/b1\", " +
                        "\"{http://www.w3.org/2000/xmlns/}ns3\":\"http://sample.com/wso2/d1\", " +
                        "\"{http://sample.com/wso2/c1}foo\":\"bar\", \"tracer\":\"1\"}");
    }

    @Test(description = "Test map operations on xml@ value")
    public void testMapOperations() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "mapOperationsOnXmlAttribute");
        Assert.assertEquals(returns[0].stringValue(), "5");
        Assert.assertEquals(returns[1].stringValue(),
                "[\"{http://www.w3.org/2000/xmlns/}xmlns\", " +
                        "\"{http://www.w3.org/2000/xmlns/}ns0\", " +
                        "\"{http://www.w3.org/2000/xmlns/}ns1\", " +
                        "\"{http://www.w3.org/2000/xmlns/}ns3\", " +
                        "\"{http://sample.com/wso2/c1}foo\"]");
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
    }

    @Test(description = "Test map insertion on xml@ value")
    public void testMapUpdateOnXmlAttributeMap() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "mapUpdateOnXmlAttribute");
        BXML xml = (BXML) returns[0];
        String abcAttrVal = xml.getAttribute("abc", null);
        Assert.assertEquals(abcAttrVal, "xyz");
        Assert.assertEquals(xml.getAttribute("baz", "http://example.com/ns"), "value");
        Assert.assertEquals(xml.getAttribute("bar", "abc}}bak"), "theNewVal");
        Assert.assertEquals(xml.getAttribute("foo", "the{}url"), "foo2");
    }

    @Test(description = "Test xml@ return nil when xml is non singleton xml item")
    public void testAttributeAccessOfNonSingletonXML() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "nonSingletonXmlAttributeAccess");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testAttributeAccessUsingDirectAtCharacter() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testAttributeAccess");
        Assert.assertEquals(returns[0].stringValue(), "available");
    }

    @Test
    public void testAttribMapUpdate() {
        BValue[] returns = BRunUtil.invoke(xmlAttrProgFile, "testAttribMapUpdate");
        Assert.assertEquals(returns[0].stringValue(),
                "<Person xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns0=\"http://sample.com/wso2/a1\" xmlns:ns1=\"http://sample.com/wso2/b1\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/d1\" name=\"Foo\"></Person>");
        Assert.assertEquals(((BMap) returns[1]).get("{http://sample.com/wso2/c1}name").stringValue(), "Foo");
        Assert.assertEquals(returns[2].stringValue(),
                "<Person xmlns=\"http://sample.com/wso2/c1\" " +
                        "xmlns:ns0=\"http://sample.com/wso2/a1\" xmlns:ns1=\"http://sample.com/wso2/b1\" " +
                        "xmlns:ns3=\"http://sample.com/wso2/d1\" name=\"Bar\"></Person>");
        Assert.assertEquals(((BMap) returns[3]).get("{http://sample.com/wso2/c1}name").stringValue(), "Bar");
    }

    @Test
    public void testPrintAttribMap() {
        PrintStream original = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(outContent));
            BRunUtil.invoke(xmlAttrProgFile, "testPrintAttribMap");
            Assert.assertEquals(outContent.toString(),
                    "{http://www.w3.org/2000/xmlns/}xmlns=http://sample.com/wso2/c1 " +
                            "{http://www.w3.org/2000/xmlns/}ns0=http://sample.com/wso2/a1 " +
                            "{http://www.w3.org/2000/xmlns/}ns1=http://sample.com/wso2/b1 " +
                            "{http://www.w3.org/2000/xmlns/}ns3=http://sample.com/wso2/d1 " +
                            "{http://sample.com/wso2/c1}name=Foo",
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
