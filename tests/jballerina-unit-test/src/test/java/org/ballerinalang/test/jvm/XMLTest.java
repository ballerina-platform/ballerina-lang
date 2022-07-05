/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.ballerinalang.test.jvm;

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlItem;
import io.ballerina.runtime.api.values.BXmlSequence;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.util.BFileUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test cases to cover some basic XML related tests on JBallerina.
 *
 * @since 0.955.0
 */
@Test
public class XMLTest {

    private CompileResult compileResult;
    private CompileResult literalWithNamespacesResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/jvm/xml.bal");
        literalWithNamespacesResult = BCompileUtil.compile("test-src/jvm/xml-literals-with-namespaces.bal");
    }

    @Test
    public void testBasicXML() {
        Object val = BRunUtil.invoke(compileResult, "testXML");
        BArray result = (BArray) val;
        Assert.assertEquals(result.get(0).toString(),
                "<ns0:foo xmlns:ns0=\"http://wso2.com/\" xmlns:ns1=\"http://ballerinalang.org/\" " +
                        "a=\"hello world\" ns0:b=\"active\"><ns1:bar1>hello1</ns1:bar1><bar2>hello2</bar2></ns0:foo>");
        Assert.assertEquals(result.get(1).toString(),
                "{\"{http://www.w3.org/2000/xmlns/}ns0\":\"http://wso2.com/\",\"a\":\"hello world\",\"{http://www.w3" +
                        ".org/2000/xmlns/}ns1\":\"http://ballerinalang.org/\",\"{http://wso2.com/}b\":\"active\"}");
        Assert.assertEquals(result.get(2).toString(), "active");
        Assert.assertEquals(result.get(3).toString(),
                "<ns1:bar1 xmlns:ns1=\"http://ballerinalang.org/\">hello1</ns1:bar1><bar2>hello2</bar2>");
    }

    @Test
    public void testElementLiteralWithNamespaces() {
        Object val = BRunUtil.invoke(literalWithNamespacesResult, "testElementLiteralWithNamespaces");
        BArray returns = (BArray) val;
        Assert.assertTrue(returns.get(0) instanceof BXml);
        Assert.assertEquals(returns.get(0).toString(),
                "<root xmlns=\"http://ballerina.com/\" " +
                        "xmlns:ns0=\"http://ballerina.com/a\" ns0:id=\"456\">" +
                        "<foo>123</foo>" +
                        "<bar xmlns:ns1=\"http://ballerina.com/c\" ns1:status=\"complete\"></bar></root>");

        Assert.assertTrue(returns.get(1) instanceof BXml);
        BXmlSequence seq = (BXmlSequence) returns.get(1);
        Assert.assertEquals(seq.toString(),
                "<foo xmlns=\"http://ballerina.com/\">123</foo>" +
                        "<bar xmlns=\"http://ballerina.com/\" " +
                        "xmlns:ns1=\"http://ballerina.com/c\" ns1:status=\"complete\"></bar>");

        BArray items = (BArray) seq.value();
        Assert.assertEquals(items.size(), 2);
    }

    @Test
    public void testElementWithQualifiedName() {
        Object val = BRunUtil.invoke(literalWithNamespacesResult, "testElementWithQualifiedName");
        BArray returns = (BArray) val;
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
        Object val = BRunUtil.invoke(literalWithNamespacesResult, "testDefineInlineDefaultNamespace");
        BArray returns = (BArray) val;
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

    @Test
    public void testComplexXMLLiteral() throws IOException {
        Object returns = BRunUtil.invoke(literalWithNamespacesResult, "testComplexXMLLiteral");
        Assert.assertTrue(returns instanceof BXmlItem);
        Assert.assertEquals(returns.toString(),
                BFileUtil.readFileAsString("src/test/resources/test-src/jvm/sampleXML.txt"));
    }

    @Test
    public void testNamespaceDclr() {
        Object val = BRunUtil.invoke(literalWithNamespacesResult, "testNamespaceDclr");
        BArray returns = (BArray) val;
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "{http://sample.com/wso2/a2}foo");

        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(1).toString(), "{http://sample.com/wso2/b2}foo");

        Assert.assertTrue(returns.get(2) instanceof BString);
        Assert.assertEquals(returns.get(2).toString(), "{http://sample.com/wso2/d2}foo");
    }

    @Test
    public void testInnerScopeNamespaceDclr() {
        Object val = BRunUtil.invoke(literalWithNamespacesResult, "testInnerScopeNamespaceDclr");
        BArray returns = (BArray) val;
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "{http://ballerina.com/b}foo");

        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(1).toString(), "{http://sample.com/wso2/a3}foo");

        Assert.assertTrue(returns.get(2) instanceof BString);
        Assert.assertEquals(returns.get(2).toString(), "{http://ballerina.com/b}foo");
    }

    @Test
    public void testObjectLevelXML() {
        Object returns = BRunUtil.invoke(literalWithNamespacesResult, "testObjectLevelXML");
        Assert.assertTrue(returns instanceof BXml);
        Assert.assertEquals(returns.toString(),
                "<p:person xmlns:p=\"foo\" xmlns:q=\"bar\">hello</p:person>");
    }

    @Test
    public void testXmlNavigation() {
        Object val = BRunUtil.invoke(compileResult, "testXmlNavigation");
        BArray returns = (BArray) val;
        Assert.assertEquals(returns.get(0).toString(),
                "<fname><foo>1</foo><bar>2</bar></fname><lname1><foo>3</foo><bar>4</bar></lname1><fname><foo>5</foo>" +
                        "<bar>6</bar></fname><lname2><foo>7</foo><bar>8</bar></lname2>apple");
        Assert.assertEquals(returns.get(1).toString(), "<fname><foo>1</foo><bar>2</bar></fname>");
        Assert.assertEquals(returns.get(2).toString(), "<foo>1</foo><foo>5</foo>");
        Assert.assertEquals(returns.get(3).toString(), "");
        Assert.assertEquals(returns.get(4).toString(), "");
        Assert.assertEquals(returns.get(5).toString(), "<foo>1</foo><foo>5</foo>");
        Assert.assertEquals(returns.get(6).toString(), "<bar>2</bar><bar>4</bar><bar>6</bar><bar>8</bar>");
        Assert.assertEquals(returns.get(7).toString(),
                "<foo>1</foo><bar>2</bar><foo>3</foo><bar>4</bar><foo>5</foo><bar>6</bar><foo>7</foo><bar>8</bar>");
    }

    @Test(description = "Test interpolating xml when there are extra dollar signs")
    public void testXMLLiteralWithExtraDollarSigns() {
        Object val = BRunUtil.invoke(compileResult, "testDollarSignOnXMLLiteralTemplate");
        BArray returns = (BArray) val;
        Assert.assertTrue(returns.get(0) instanceof BXml);
        Assert.assertEquals(returns.get(0).toString(), "<foo id=\"hello $5\">hello</foo>");
        Assert.assertEquals(returns.get(1).toString(), "<foo id=\"hello $$5\">$hello</foo>");
        Assert.assertEquals(returns.get(2).toString(), "<foo id=\"hello $$ 5\">$$ hello</foo>");
    }

    @Test
    public void testGetGlobalXML() {
        Object returns = BRunUtil.invoke(compileResult, "testGetGlobalXML");
        Assert.assertTrue(returns instanceof BXml);
        Assert.assertEquals(returns.toString(), "<test><name>ballerina</name></test>");
    }

    @Test
    public void testXMLWithNumericEscapes() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testXMLWithNumericEscapes");
        Assert.assertEquals(returns.size(), 10);
        Assert.assertTrue(returns.get(0) instanceof BXml);
        Assert.assertEquals(returns.get(0).toString(), "\\u{61}");
        Assert.assertEquals(returns.get(1).toString(), "\\u{61}ppl\\\\u{65}");
        Assert.assertEquals(returns.get(2).toString(), "A \\u{5C} B");
        Assert.assertEquals(returns.get(3).toString(), "A \\\\u{5C} B");
        Assert.assertEquals(returns.get(4).toString(), "\"\\u{D800}\"");
        Assert.assertEquals(returns.get(5).toString(), "\\u{DFFF}\"\\u{DFFF}\"");
        Assert.assertEquals(returns.get(6).toString(), "\"\\u{12FFFF} ABC \\u{DFFF} DEF \\u{DAFF}\"");
        Assert.assertEquals(returns.get(7).toString(), "\\u{12FFFF} ABC \\u{DFFFAAA} DEF \\u{FFFFFFF}");
        Assert.assertEquals(returns.get(8).toString(), "\\u{001B[");
        Assert.assertEquals(returns.get(9).toString(), "\"\\u{001B[\"");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
        literalWithNamespacesResult = null;
    }
}
