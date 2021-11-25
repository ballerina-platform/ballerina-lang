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

import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.XmlFactory;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BIterator;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.model.values.BXML;
import org.ballerinalang.core.model.values.BXMLItem;
import org.ballerinalang.core.model.values.BXMLSequence;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test class for XML literal.
 *
 * @since 0.94
 */
public class XMLLiteralTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/xml/xml-literals.bal");
    }

//    @Test
//    public void testXMLNegativeSemantics() {
//        CompileResult negativeResult = BCompileUtil.compile("test-src/types/xml/xml-literals-negative.bal");
//        int index = 0;
//        BAssertUtil.validateError(negativeResult, index++, "invalid namespace prefix 'xmlns'", 4, 19);
//        BAssertUtil.validateError(negativeResult, index++, "invalid namespace prefix 'xmlns'", 4, 36);
//
//        // undeclared element prefix
//        BAssertUtil.validateError(negativeResult, index++, "undefined symbol 'ns1'", 9, 19);
//        BAssertUtil.validateError(negativeResult, index++, "undefined symbol 'ns1'", 9, 34);
//
//        // text with multi type expressions
//        BAssertUtil.validateError(negativeResult, index++,
//                                  "incompatible types: expected '(int|float|decimal|string|boolean|xml)', found 'map'",
//                                  15, 59);
//
//        // namespace conflict with block scope namespace
//        BAssertUtil.validateError(negativeResult, index++, "redeclared symbol 'ns0'", 28, 46);
//
//        // namespace conflict with package import
//        BAssertUtil.validateError(negativeResult, index++, "redeclared symbol 'x'", 33, 5);
//
//        // update qname
//        BAssertUtil.validateError(negativeResult, index++, "cannot assign values to an xml qualified name", 38, 5);
//
//        // use of undefined namespace for qname
//        BAssertUtil.validateError(negativeResult, index++, "cannot find xml namespace prefix 'ns0'", 46, 24);
//
//        // define namespace with empty URI
//        BAssertUtil.validateError(negativeResult, index++, "cannot bind prefix 'ns0' to the empty namespace name",
//                50, 5);
//
//        // XML elements with mismatching start and end tags
//        BAssertUtil.validateError(negativeResult, index++, "mismatching start and end tags found in xml element",
//                                  54, 13);
//        // XML interpolation is not allowed to interpolate XML namespace attributes
//        BAssertUtil.validateError(negativeResult, index++, "xml namespaces cannot be interpolated", 63, 29);
//        BAssertUtil.validateError(negativeResult, index++, "xml namespaces cannot be interpolated", 63, 47);
//
//        // XML sequence value assignment
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                        "'xml<xml:Text>', found 'xml:Comment'", 68, 30);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml:Text', found 'xml:Comment'", 69, 26);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                        "'xml<(xml:Text|xml:Comment)>', found 'xml:Element'", 70, 38);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml<(xml:Text|xml:Comment)>', found 'xml:Element'", 71, 43);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml<(xml:Text|xml:Comment)>', found 'xml:ProcessingInstruction'", 71, 90);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'(xml<xml:Text>|xml<xml:Comment>)', found 'xml:Element'", 72, 44);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'(xml<xml:Text>|xml<xml:Comment>)', found 'xml:Element'", 73, 49);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'(xml<xml:Text>|xml<xml:Comment>)', found 'xml:ProcessingInstruction'", 73, 96);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'(xml:Text|xml:Comment)', found 'xml:Element'", 74, 34);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                        "'(xml:Text|xml:Comment)', found 'xml'", 75, 34);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'(xml:Text|xml:Comment)', found 'xml:Element'", 75, 39);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'(xml:Text|xml:Comment)', found 'xml:ProcessingInstruction'", 75, 86);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                        "'(xml:Text|xml:Comment)', found 'xml'", 76, 34);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'(xml<xml:Text>|xml:Comment)', found 'xml'", 77, 39);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml:Element', found 'XML Sequence'", 78, 24);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'(xml:Comment|xml:Element)', found 'xml'", 79, 37);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'(xml:Comment|xml:Element)', found 'xml'", 80, 37);
//
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml:ProcessingInstruction', found 'XML Sequence'", 82, 38);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml:Element', found 'XML Sequence'", 83, 24);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml:Comment', found 'XML Sequence'", 84, 24);
//
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml<xml:ProcessingInstruction>', found 'xml:Element'", 87, 48);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml<xml:ProcessingInstruction>', found 'xml:Element'", 87, 63);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml<xml:ProcessingInstruction>', found 'xml:Text'", 88, 48);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml<xml:ProcessingInstruction>', found 'xml:Comment'", 89, 48);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml<xml:ProcessingInstruction>', found 'xml:Comment'", 89, 63);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml<xml:Element>', found 'xml:Text'", 91, 34);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml<xml:Element>', found 'xml:ProcessingInstruction'", 92, 34);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml<xml:Element>', found 'xml:ProcessingInstruction'", 92, 41);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml<xml:Element>', found 'xml:Comment'", 93, 34);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml<xml:Element>', found 'xml:Comment'", 93, 49);
//
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml<xml:Comment>', found 'xml:ProcessingInstruction'", 95, 34);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml<xml:Comment>', found 'xml:ProcessingInstruction'", 95, 41);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml<xml:Comment>', found 'xml:Element'", 96, 34);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml<xml:Comment>', found 'xml:Element'", 96, 49);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml<xml:Comment>', found 'xml:Text'", 97, 34);
//
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml<xml:Text>', found 'xml:Comment'", 99, 31);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml<xml:Text>', found 'xml:Comment'", 99, 46);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml<xml:Text>', found 'xml:Element'", 100, 31);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml<xml:Text>', found 'xml:Element'", 100, 46);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml<xml:Text>', found 'xml:ProcessingInstruction'", 101, 31);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml<xml:Text>', found 'xml:ProcessingInstruction'", 101, 38);
//        BAssertUtil.validateError(negativeResult, index++, "undefined symbol 'b'", 105, 32);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'(int|float|decimal|string|boolean)', found 'map<string>'", 111, 32);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'(int|float|decimal|string|boolean)', found 'error'", 111, 37);
//
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml:Element', found 'xml<xml<xml:Element>>'", 116, 22);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml:Comment', found 'xml<xml<xml<xml:Comment>>>'", 118, 22);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml:ProcessingInstruction', found 'xml<xml<xml<xml:ProcessingInstruction>>>'", 120, 36);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml:Text', found 'xml'", 122, 19);
//        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
//                "'xml:Text', found 'xml<xml>'", 124, 19);
//        BAssertUtil.validateError(negativeResult, index++, "missing xml CDATA end token", 128, 49);
//
//        Assert.assertEquals(index, negativeResult.getErrorCount());
//    }

    @Test
    public void testXMLSequence() {
        BRunUtil.invoke(result, "testXMLSequence");
    }

    @Test
    public void testXMLWithLeadingWhiteSpace() {
        BRunUtil.invoke(result, "testXMLWithLeadingWhiteSpace");
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
        Assert.assertEquals(returns[4].stringValue(), "aaa11b${bb22c\\}cc{d{}e}{f{");

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
        Assert.assertEquals(returns[4].stringValue(), "<!---a-aa11b${bb22c\\}cc{d{}e}{f{-->");

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
        Assert.assertEquals(returns[2].stringValue(), "<?foo aaa11bbb22ccc?>");

        Assert.assertTrue(returns[3] instanceof BXML);
        Assert.assertEquals(returns[3].stringValue(), "<?foo <aaa11bbb22ccc??d?e>?f<<{>>>?>");

        Assert.assertTrue(returns[4] instanceof BXML);
        Assert.assertEquals(returns[4].stringValue(), "<?foo ?a?aa11b${bb22c\\}cc{d{}e}{f{?>");
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
        Assert.assertEquals(returns[3].stringValue(), "<foo bar1=\"aaa{zzz}b${b&quot;b33&gt;22c\\}cc{d{}e}{f{\" "
                + "bar2=\"aaa{zzz}b${b&quot;b33&gt;22c\\}cc{d{}e}{f{\"></foo>");

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
        Assert.assertEquals(returns[0].stringValue(), "hello &lt; &gt; &amp;"); // BXMLItem.toString()
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 0);
        Assert.assertEquals(arrayToString(returns[2]), "");
    }

    private String arrayToString(BValue aReturn) {
        BValueArray ar = ((BValueArray) aReturn);
        StringBuilder builder = new StringBuilder();
        BIterator bIterator = ar.newIterator();
        while (bIterator.hasNext()) {
            String str = bIterator.getNext().stringValue();
            builder.append(str);
        }
        return builder.toString();
    }

    @Test
    public void testTextWithValidMultiTypeExpressions() {
        BValue[] returns = BRunUtil.invoke(result, "testTextWithValidMultiTypeExpressions");
        Assert.assertTrue(returns[0] instanceof BXMLSequence);

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

    @Test (description = "Test sequence of brackets in content of XML")
    public void testBracketSequenceInXMLLiteral() {
        BValue[] returns = BRunUtil.invoke(result, "testBracketSequenceInXMLLiteral");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "{}{{ {{{ { } }} }}} - extra }<elem>{}{{</elem>");
    }

    @Test (description = "Test interpolating xml using different types")
    public void testXMLLiteralInterpolation() {
        BValue[] returns = BRunUtil.invoke(result, "testInterpolatingVariousTypes");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<elem>42|3.14|31.4444|this-is-a-string|<abc></abc></elem>");
    }

    @Test (description = "Test interpolating xml when there are extra dollar signs")
    public void testXMLLiteralWithExtraDollarSigns() {
        BValue[] returns = BRunUtil.invoke(result, "testDollarSignOnXMLLiteralTemplate");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<foo id=\"hello $5\">hello</foo>");
        Assert.assertEquals(returns[1].stringValue(), "<foo id=\"hello $$5\">$hello</foo>");
        Assert.assertEquals(returns[2].stringValue(), "<foo id=\"hello $$ 5\">$$ hello</foo>");
    }

    @Test
    public void testXMLToString() {
        BXml xml = XmlFactory.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<!DOCTYPE foo [<!ELEMENT foo ANY ><!ENTITY data \"Example\" >]><foo>&data;</foo>");
        Assert.assertEquals(xml.toString(), "<foo>Example</foo>");
    }

    @Test(dataProvider = "function-name-provider")
    public void testXMLValue(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider(name = "function-name-provider")
    public Object[] provideFunctionNames() {
        return new String[]{"testXMLSequenceValueAssignment", "testXMLTextValueAssignment", "testXMLCDATASection",
                "testXMLReturnUnion"};
    }
}
