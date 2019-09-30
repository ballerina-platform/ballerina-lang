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

import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.values.XMLItem;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BIterator;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.model.values.BXMLSequence;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Test class for XML literal.
 *
 * @since 0.94
 */
public class XMLLiteralTest {

    private CompileResult result;
    private CompileResult literalWithNamespacesResult;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/xml/xml-literals.bal");
        literalWithNamespacesResult = BCompileUtil.compile("test-src/types/xml/xml-literals-with-namespaces.bal");
        negativeResult = BCompileUtil.compile("test-src/types/xml/xml-literals-negative.bal");
    }

    @Test
    public void testXMLNegativeSemantics() {
        int index = 0;
        BAssertUtil.validateError(negativeResult, index++, "invalid namespace prefix 'xmlns'", 5, 19);
        BAssertUtil.validateError(negativeResult, index++, "invalid namespace prefix 'xmlns'", 5, 36);

        // undeclared element prefix
        BAssertUtil.validateError(negativeResult, index++, "undefined symbol 'ns1'", 10, 19);
        BAssertUtil.validateError(negativeResult, index++, "undefined symbol 'ns1'", 10, 34);

        // text with multi type expressions
        BAssertUtil.validateError(negativeResult, index++,
                                  "incompatible types: expected '(int|float|decimal|string|boolean|xml)', found 'map'",
                                  16, 59);

        // text with invalid multi type expressions
        BAssertUtil.validateError(negativeResult, index++,
                                  "incompatible types: expected '(int|float|decimal|string|boolean)', found 'xml'",
                                  28, 51);

        // namespace conflict with block scope namespace
        BAssertUtil.validateError(negativeResult, index++, "redeclared symbol 'ns0'", 37, 46);

        // namespace conflict with package import
        BAssertUtil.validateError(negativeResult, index++, "redeclared symbol 'x'", 42, 5);

        // get attributes from non-xml
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'xml', found 'map'", 47, 17);

        // update attributes map
        BAssertUtil.validateError(negativeResult, index++,
                "xml attributes cannot be updated as a collection. update attributes one at a time", 52, 5);

        // update qname
        BAssertUtil.validateError(negativeResult, index++, "cannot assign values to an xml qualified name", 57, 5);

        // use of undefined namespace for qname
        BAssertUtil.validateError(negativeResult, index++, "undefined module 'ns0'", 65, 20);

        // define namespace with empty URI
        BAssertUtil.validateError(negativeResult, index++, "cannot bind prefix 'ns0' to the empty namespace name",
                69, 5);

        // XML elements with mismatching start and end tags
        BAssertUtil.validateError(negativeResult, index++, "mismatching start and end tags found in xml element",
                                  73, 18);
        // XML interpolation is not allowed to interpolate XML namespace attributes
        BAssertUtil.validateError(negativeResult, index++, "xml namespaces cannot be interpolated", 82, 29);
        BAssertUtil.validateError(negativeResult, index++, "xml namespaces cannot be interpolated", 82, 47);
        Assert.assertEquals(index, negativeResult.getErrorCount());
    }

    @Test
    public void testXMLTextLiteral() {
        BValue[] returns = BRunUtil.invoke(result, "testXMLTextLiteral");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "aaa");

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "11");

        Assert.assertTrue(returns[2] instanceof BXML);
        Assert.assertEquals(returns[2].stringValue(), "aaa11bbb22ccc");

        Assert.assertTrue(returns[3] instanceof BXML);
        Assert.assertEquals(returns[3].stringValue(), "aaa11bbb22ccc{d{}e}{f{");

        Assert.assertTrue(returns[4] instanceof BXML);
        Assert.assertEquals(returns[4].stringValue(), "aaa11b${bb22c}cc{d{}e}{f{");

        Assert.assertTrue(returns[5] instanceof BXML);
        Assert.assertEquals(returns[5].stringValue(), " ");
    }

    @Test
    public void testXMLCommentLiteral() {
        BValue[] returns = BRunUtil.invoke(result, "testXMLCommentLiteral");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<!--aaa-->");

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<!--11-->");

        Assert.assertTrue(returns[2] instanceof BXML);
        Assert.assertEquals(returns[2].stringValue(), "<!--aaa11bbb22ccc-->");

        Assert.assertTrue(returns[3] instanceof BXML);
        Assert.assertEquals(returns[3].stringValue(), "<!--<aaa11bbb22cccd->e->-f<<{>>>-->");

        Assert.assertTrue(returns[4] instanceof BXML);
        Assert.assertEquals(returns[4].stringValue(), "<!---a-aa11b${bb22c}cc{d{}e}{f{-->");

        Assert.assertTrue(returns[5] instanceof BXML);
        Assert.assertEquals(returns[5].stringValue(), "<!---->");
    }

    @Test
    public void testXMLPILiteral() {
        BValue[] returns = BRunUtil.invoke(result, "testXMLPILiteral");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<?foo ?>");

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<?foo 11?>");

        Assert.assertTrue(returns[2] instanceof BXML);
        Assert.assertEquals(returns[2].stringValue(), "<?foo  aaa11bbb22ccc?>");

        Assert.assertTrue(returns[3] instanceof BXML);
        Assert.assertEquals(returns[3].stringValue(), "<?foo  <aaa11bbb22ccc??d?e>?f<<{>>>?>");

        Assert.assertTrue(returns[4] instanceof BXML);
        Assert.assertEquals(returns[4].stringValue(), "<?foo  ?a?aa11b${bb22c}cc{d{}e}{f{?>");
    }

    @Test
    public void testExpressionAsAttributeValue() {
        BValue[] returns = BRunUtil.invoke(result, "testExpressionAsAttributeValue");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<foo bar=\"&quot;zzz&quot;\"></foo>");

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<foo bar=\"aaazzzbb'b33&gt;22ccc?\"></foo>");

        Assert.assertTrue(returns[2] instanceof BXML);
        Assert.assertEquals(returns[2].stringValue(), "<foo bar=\"}aaazzzbbb33&gt;22ccc{d{}e}{f{\"></foo>");

        Assert.assertTrue(returns[3] instanceof BXML);
        Assert.assertEquals(returns[3].stringValue(), "<foo bar1=\"aaa{zzz}b${b&quot;b33&gt;22c}cc{d{}e}{f{\" "
                + "bar2=\"aaa{zzz}b${b&quot;b33&gt;22c}cc{d{}e}{f{\"></foo>");

        Assert.assertTrue(returns[4] instanceof BXML);
        Assert.assertEquals(returns[4].stringValue(), "<foo bar=\"\"></foo>");
    }

    @Test
    public void testElementLiteralWithTemplateChildren() {
        BValue[] returns = BRunUtil.invoke(result, "testElementLiteralWithTemplateChildren");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root>hello aaa&lt;bbb good morning <fname>John</fname> "
                + "<lname>Doe</lname>. Have a nice day!<foo>123</foo><bar></bar></root>");

        Assert.assertTrue(returns[1] instanceof BXML);
        BXMLSequence seq = (BXMLSequence) returns[1];
        Assert.assertEquals(seq.stringValue(), "hello aaa&lt;bbb good morning <fname>John</fname> <lname>Doe</lname>. "
                + "Have a nice day!<foo>123</foo><bar></bar>");

        BValueArray items = seq.value();
        Assert.assertEquals(items.size(), 7);
    }

    @Test
    public void testXMLStartTag() {
        BValue[] returns = BRunUtil.invoke(result, "testXMLStartTag");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<fname>John</fname>");

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<Country>US</Country>");

        Assert.assertTrue(returns[2] instanceof BXML);
        Assert.assertEquals(returns[2].stringValue(), "<_foo id=\"hello 5\">hello</_foo>");

        Assert.assertTrue(returns[3] instanceof BXML);
        Assert.assertEquals(returns[3].stringValue(), "<_-foo id=\"hello 5\">hello</_-foo>");
    }

    @Test
    public void testXMLLiteralWithEscapeSequence() {
        BValue[] returns = BRunUtil.invoke(result, "testXMLLiteralWithEscapeSequence");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "hello &lt; &gt; &amp;");
        Assert.assertEquals(arrayToString(returns[1]), "hello < > &");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 11);
        Assert.assertEquals(arrayToString(returns[3]), "hello < > &");
    }

    private String arrayToString(BValue aReturn) {
        BValueArray ar = ((BValueArray) aReturn);
        StringBuilder builder = new StringBuilder();
        BIterator bIterator = ar.newIterator();
        while (bIterator.hasNext()) {
            String str = ((BString) bIterator.getNext()).stringValue();
            builder.append(str);
        }
        return builder.toString();
    }

    @Test
    public void testElementLiteralWithNamespaces() {
        BValue[] returns =
                BRunUtil.invoke(literalWithNamespacesResult, "testElementLiteralWithNamespaces");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://ballerina.com/\" xmlns:ns0=\"http://ballerina.com/a\" "
                        + "xmlns:ns1=\"http://ballerina.com/c\" ns0:id=\"456\"><foo>123</foo>"
                        + "<bar ns1:status=\"complete\"></bar></root>");

        Assert.assertTrue(returns[1] instanceof BXML);
        BXMLSequence seq = (BXMLSequence) returns[1];
        Assert.assertEquals(seq.stringValue(),
                "<foo xmlns=\"http://ballerina.com/\" "
                        + "xmlns:ns0=\"http://ballerina.com/a\" xmlns:ns1=\"http://ballerina.com/c\">123</foo>"
                        + "<bar xmlns=\"http://ballerina.com/\" xmlns:ns0=\"http://ballerina.com/a\" "
                        + "xmlns:ns1=\"http://ballerina.com/c\" ns1:status=\"complete\"></bar>");

        BValueArray items = seq.value();
        Assert.assertEquals(items.size(), 2);
    }

    @Test
    public void testElementWithQualifiedName() {
        BValue[] returns = BRunUtil.invoke(literalWithNamespacesResult, "testElementWithQualifiedName");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns1=\"http://ballerina.com/b\">hello</root>");

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(),
                "<root xmlns=\"http://ballerina.com/\" xmlns:ns1=\"http://ballerina.com/b\">hello</root>");

        Assert.assertTrue(returns[2] instanceof BXML);
        Assert.assertEquals(returns[2].stringValue(),
                "<ns1:root xmlns:ns1=\"http://ballerina.com/b\" xmlns=\"http://ballerina.com/\">hello</ns1:root>");
    }

    @Test
    public void testDefineInlineNamespace() {
        BValue[] returns = BRunUtil.invoke(literalWithNamespacesResult, "testDefineInlineNamespace");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<nsx:foo xmlns:nsx=\"http://wso2.com\" "
                + "xmlns:ns1=\"http://ballerina.com/b\" nsx:id=\"123\">hello</nsx:foo>");
    }

    @Test
    public void testDefineInlineDefaultNamespace() {
        BValue[] returns =
                BRunUtil.invoke(literalWithNamespacesResult, "testDefineInlineDefaultNamespace");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<foo xmlns=\"http://ballerina.com/default/namespace\" "
                + "xmlns:nsx=\"http://wso2.com/aaa\" xmlns:ns1=\"http://ballerina.com/b\">hello</foo>");

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(),
                "<foo xmlns=\"http://wso2.com\" xmlns:nsx=\"http://wso2.com/aaa\" "
                        + "xmlns:ns1=\"http://ballerina.com/b\">hello</foo>");
    }

    @Test
    public void testTextWithValidMultiTypeExpressions() {
        BValue[] returns = BRunUtil.invoke(result, "testTextWithValidMultiTypeExpressions");
        Assert.assertTrue(returns[0] instanceof BXMLItem);

        Assert.assertEquals(returns[0].stringValue(), "hello 11 world. How 1.35 are you true?");
    }

    @Test
    public void testArithmaticExpreesionInXMLTemplate() {
        BValue[] returns = BRunUtil.invoke(result, "testArithmaticExpreesionInXMLTemplate");
        Assert.assertTrue(returns[0] instanceof BXMLItem);

        Assert.assertEquals(returns[0].stringValue(), "<foo id=\"hello 5\">hello</foo>");
    }

    @Test
    public void testFunctionCallInXMLTemplate() {
        BValue[] returns = BRunUtil.invoke(result, "testFunctionCallInXMLTemplate");
        Assert.assertTrue(returns[0] instanceof BXMLItem);

        Assert.assertEquals(returns[0].stringValue(), "<foo>&lt;--&gt;returned from a function</foo>");
    }

    @Test
    public void testUsingNamespcesOfParent() {
        BValue[] returns = BRunUtil.invoke(literalWithNamespacesResult, "testUsingNamespcesOfParent");
        Assert.assertTrue(returns[0] instanceof BXMLItem);

        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns0=\"http://ballerinalang.com/\" "
                + "xmlns:ns1=\"http://ballerina.com/b\"><ns0:foo>hello</ns0:foo></root>");
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

    @Test
    public void testPackageLevelXML() {
        CompileResult result = BCompileUtil.compile("test-src/types/xml/package_level_xml_literals.bal");
        BValue[] returns = BRunUtil.invoke(result, "testPackageLevelXML");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<p:person xmlns:p=\"foo\" xmlns:q=\"bar\" xmlns:ns1=\"http://ballerina.com/b\">hello</p:person>");

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(),
                "<ns1:student xmlns:ns1=\"http://ballerina.com/b\">hello</ns1:student>");
    }

    @Test(groups = "brokenOnJBallerina")
    // todo: enable this once we fix the method too large issue on jBallerina
    public void testLargeXMLLiteral() {
        BCompileUtil.compile("test-src/types/xml/xml_inline_large_literal.bal");
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessage("/test/getXML", "GET");
        HttpCarbonMessage response = Services.invoke(9091, cMsg);
        Assert.assertNotNull(response);
        BXML<?> xml = new BXMLItem(new HttpMessageDataStreamer(response).getInputStream());
        Assert.assertTrue(xml.stringValue().contains("<line2>Sigiriya</line2>"));
    }

    @Test
    public void testObjectLevelXML() {
        BValue[] returns = BRunUtil.invoke(literalWithNamespacesResult, "testObjectLevelXML");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<p:person xmlns:p=\"foo\" xmlns:q=\"bar\" xmlns:ns1=\"http://ballerina.com/b\">hello</p:person>");
    }

    @Test(description = "Test sequence of brackets in content of XML")
    public void testBracketSequenceInXMLLiteral() {
        BValue[] returns = BRunUtil.invoke(result, "testBracketSequenceInXMLLiteral");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "{}{{ {{{ { } }} }}} - extra }<elem>{}{{</elem>");
    }

    @Test(description = "Test interpolating xml using different types")
    public void testXMLLiteralInterpolation() {
        BValue[] returns = BRunUtil.invoke(result, "testInterpolatingVariousTypes");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<elem>42|3.14|31.4444|this-is-a-string|<abc></abc></elem>");
    }

    @Test(description = "Test interpolating xml when there are extra dollar signs")
    public void testXMLLiteralWithExtraDollarSigns() {
        BValue[] returns = BRunUtil.invoke(result, "testDollarSignOnXMLLiteralTemplate");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<foo id=\"hello $5\">hello</foo>");
        Assert.assertEquals(returns[1].stringValue(), "<foo id=\"hello $$5\">$hello</foo>");
        Assert.assertEquals(returns[2].stringValue(), "<foo id=\"hello $$ 5\">$$ hello</foo>");
    }

    @Test
    public void testXMLSerialize() {
        BValue[] returns = BRunUtil.invoke(literalWithNamespacesResult, "getXML");
        Assert.assertTrue(returns[0] instanceof BXML);

        XMLItem xmlItem = new XMLItem(returns[0].stringValue());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        xmlItem.serialize(baos);
        Assert.assertEquals(new String(baos.toByteArray()),
                "<foo xmlns=\"http://wso2.com/\" xmlns:ns1=\"http://ballerina.com/b\">hello</foo>");
    }

    @Test
    public void testXMLToString() {
        XMLValue<?> xml = XMLFactory.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<!DOCTYPE foo [<!ELEMENT foo ANY ><!ENTITY data \"Example\" >]><foo>&data;</foo>");
        Assert.assertEquals(xml.toString(), "<foo>Example</foo>");
    }
}
