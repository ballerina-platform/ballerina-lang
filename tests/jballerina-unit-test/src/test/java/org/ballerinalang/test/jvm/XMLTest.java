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
        BValue[] result = BRunUtil.invoke(compileResult, "testXML", new BValue[] {});
        Assert.assertEquals(result[0].stringValue(),
                "<ns0:foo xmlns:ns0=\"http://wso2.com/\" xmlns:ns1=\"http://ballerinalang.org/\" " +
                        "a=\"hello world\" ns0:b=\"active\"><ns1:bar1>hello1</ns1:bar1><bar2>hello2</bar2></ns0:foo>");
        Assert.assertEquals(result[1].stringValue(),
                "{\"{http://www.w3.org/2000/xmlns/}ns0\":\"http://wso2.com/\", " +
                        "\"a\":\"hello world\", " +
                        "\"{http://www.w3.org/2000/xmlns/}ns1\":\"http://ballerinalang.org/\", " +
                        "\"{http://wso2.com/}b\":\"active\"}");
        Assert.assertEquals(result[2].stringValue(), "active");
        Assert.assertEquals(result[3].stringValue(),
                "<ns1:bar1 xmlns:ns1=\"http://ballerinalang.org/\">hello1</ns1:bar1><bar2>hello2</bar2>");
    }

    @Test
    public void testElementLiteralWithNamespaces() {
        BValue[] returns = BRunUtil.invoke(literalWithNamespacesResult, "testElementLiteralWithNamespaces");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://ballerina.com/\" " +
                        "xmlns:ns0=\"http://ballerina.com/a\" ns0:id=\"456\">" +
                        "<foo>123</foo>" +
                        "<bar xmlns:ns1=\"http://ballerina.com/c\" ns1:status=\"complete\"></bar></root>");

        Assert.assertTrue(returns[1] instanceof BXML);
        BXMLSequence seq = (BXMLSequence) returns[1];
        Assert.assertEquals(seq.stringValue(),
                "<foo xmlns=\"http://ballerina.com/\">123</foo>" +
                        "<bar xmlns=\"http://ballerina.com/\" " +
                        "xmlns:ns1=\"http://ballerina.com/c\" ns1:status=\"complete\"></bar>");

        BValueArray items = seq.value();
        Assert.assertEquals(items.size(), 2);
    }

    @Test
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

    @Test
    public void testDefineInlineNamespace() {
        BValue[] returns = BRunUtil.invoke(literalWithNamespacesResult, "testDefineInlineNamespace");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<nsx:foo xmlns:nsx=\"http://wso2.com\" nsx:id=\"123\">hello</nsx:foo>");
    }

    @Test
    public void testDefineInlineDefaultNamespace() {
        BValue[] returns = BRunUtil.invoke(literalWithNamespacesResult, "testDefineInlineDefaultNamespace");
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

    @Test
    public void testComplexXMLLiteral() throws IOException {
        BValue[] returns = BRunUtil.invoke(literalWithNamespacesResult, "testComplexXMLLiteral");
        Assert.assertTrue(returns[0] instanceof BXMLItem);
        Assert.assertEquals(returns[0].stringValue(),
                BCompileUtil.readFileAsString("src/test/resources/test-src/jvm/sampleXML.txt"));
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

    @Test
    public void testObjectLevelXML() {
        BValue[] returns = BRunUtil.invoke(literalWithNamespacesResult, "testObjectLevelXML");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<p:person xmlns:p=\"foo\" xmlns:q=\"bar\">hello</p:person>");
    }

    @Test(enabled =  false) // todo: re-enable when implementing getElement syntax (seq.<elm>)
    public void testFieldBasedAccess() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFieldBasedAccess");
        Assert.assertEquals(returns[0].stringValue(),
                "<fname><foo>1</foo><bar>2</bar></fname><lname1><foo>3</foo><bar>4</bar></lname1><fname><foo>5</foo>" +
                        "<bar>6</bar></fname><lname2><foo>7</foo><bar>8</bar></lname2>apple");
        Assert.assertEquals(returns[1].stringValue(), "<fname><foo>1</foo><bar>2</bar></fname>");
        Assert.assertEquals(returns[2].stringValue(), "<foo>5</foo>");
        Assert.assertEquals(returns[3].stringValue(), "<foo>5</foo>");
        Assert.assertEquals(returns[4].stringValue(), "<bar>4</bar>");
        Assert.assertEquals(returns[5].stringValue(),
                "<foo>1</foo><bar>2</bar><foo>3</foo><bar>4</bar><foo>5</foo><bar>6</bar><foo>7</foo><bar>8</bar>");
    }

    @Test(description = "Test interpolating xml when there are extra dollar signs")
    public void testXMLLiteralWithExtraDollarSigns() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testDollarSignOnXMLLiteralTemplate");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<foo id=\"hello $5\">hello</foo>");
        Assert.assertEquals(returns[1].stringValue(), "<foo id=\"hello $$5\">$hello</foo>");
        Assert.assertEquals(returns[2].stringValue(), "<foo id=\"hello $$ 5\">$$ hello</foo>");
    }

    @Test
    public void testGetGlobalXML() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetGlobalXML");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<test><name>ballerina</name></test>");
    }
}
