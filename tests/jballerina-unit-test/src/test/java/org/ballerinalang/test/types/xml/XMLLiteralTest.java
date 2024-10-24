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
import io.ballerina.runtime.api.values.BIterator;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlItem;
import io.ballerina.runtime.api.values.BXmlSequence;
import io.ballerina.runtime.internal.XmlFactory;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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

    @Test
    public void testXMLNegativeSemantics() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/types/xml/xml-literals-negative.bal");
        int index = 0;
        BAssertUtil.validateError(negativeResult, index++, "invalid namespace prefix 'xmlns'", 4, 19);
        BAssertUtil.validateError(negativeResult, index++, "invalid namespace prefix 'xmlns'", 4, 36);

        // undeclared element prefix
        BAssertUtil.validateError(negativeResult, index++, "undefined symbol 'ns1'", 9, 19);
        BAssertUtil.validateError(negativeResult, index++, "undefined symbol 'ns1'", 9, 34);

        // text with multi type expressions
        BAssertUtil.validateError(negativeResult, index++,
                "incompatible types: expected '(int|float|decimal|string|boolean|xml)', found 'map'",
                15, 59);

        // namespace conflict with block scope namespace
        BAssertUtil.validateError(negativeResult, index++, "redeclared symbol 'ns0'", 28, 46);

        // namespace conflict with package import
        BAssertUtil.validateError(negativeResult, index++, "redeclared symbol 'x'", 33, 5);

        // update qname
        BAssertUtil.validateError(negativeResult, index++, "cannot assign values to an xml qualified name", 38, 5);

        // use of undefined namespace for qname
        BAssertUtil.validateError(negativeResult, index++, "cannot find the prefix 'ns0'", 46, 24);

        // define namespace with empty URI
        BAssertUtil.validateError(negativeResult, index++, "cannot bind prefix 'ns0' to the empty namespace name",
                50, 5);

        // XML elements with mismatching start and end tags
        BAssertUtil.validateError(negativeResult, index++, "mismatching start and end tags found in xml element",
                54, 13);
        // XML interpolation is not allowed to interpolate XML namespace attributes
        BAssertUtil.validateError(negativeResult, index++, "xml namespaces cannot be interpolated", 63, 29);
        BAssertUtil.validateError(negativeResult, index++, "xml namespaces cannot be interpolated", 63, 47);

        // XML sequence value assignment
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<xml:Text>', found 'xml:Comment'", 68, 30);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml:Text', found 'xml:Comment'", 69, 26);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<(xml:Text|xml:Comment)>', found 'xml:Element'", 70, 38);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<(xml:Text|xml:Comment)>', found 'xml:Element'", 71, 43);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<(xml:Text|xml:Comment)>', found 'xml:ProcessingInstruction'", 71, 90);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(xml<xml:Text>|xml<xml:Comment>)', found 'xml:Element'", 72, 44);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(xml<xml:Text>|xml<xml:Comment>)', found 'xml'", 73, 44);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(xml<xml:Text>|xml<xml:Comment>)', found 'xml:Element'", 73, 49);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(xml<xml:Text>|xml<xml:Comment>)', found 'xml:ProcessingInstruction'", 73, 96);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(xml:Text|xml:Comment)', found 'xml:Element'", 74, 34);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(xml:Text|xml:Comment)', found 'xml'", 75, 34);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(xml:Text|xml:Comment)', found 'xml:Element'", 75, 39);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(xml:Text|xml:Comment)', found 'xml:ProcessingInstruction'", 75, 86);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(xml:Text|xml:Comment)', found 'xml'", 76, 34);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(xml<xml:Text>|xml:Comment)', found 'xml'", 77, 39);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml:Element', found 'XML Sequence'", 78, 24);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(xml:Comment|xml:Element)', found 'xml'", 79, 37);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(xml:Comment|xml:Element)', found 'xml'", 80, 37);

        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml:ProcessingInstruction', found 'XML Sequence'", 82, 38);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml:Element', found 'XML Sequence'", 83, 24);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml:Comment', found 'XML Sequence'", 84, 24);

        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<xml:ProcessingInstruction>', found 'xml:Element'", 87, 48);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<xml:ProcessingInstruction>', found 'xml:Element'", 87, 63);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<xml:ProcessingInstruction>', found 'xml:Text'", 88, 48);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<xml:ProcessingInstruction>', found 'xml:Comment'", 89, 48);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<xml:ProcessingInstruction>', found 'xml:Comment'", 89, 63);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<xml:Element>', found 'xml:Text'", 91, 34);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<xml:Element>', found 'xml:ProcessingInstruction'", 92, 34);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<xml:Element>', found 'xml:ProcessingInstruction'", 92, 41);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<xml:Element>', found 'xml:Comment'", 93, 34);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<xml:Element>', found 'xml:Comment'", 93, 49);

        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<xml:Comment>', found 'xml:ProcessingInstruction'", 95, 34);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<xml:Comment>', found 'xml:ProcessingInstruction'", 95, 41);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<xml:Comment>', found 'xml:Element'", 96, 34);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<xml:Comment>', found 'xml:Element'", 96, 49);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<xml:Comment>', found 'xml:Text'", 97, 34);

        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<xml:Text>', found 'xml:Comment'", 99, 31);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<xml:Text>', found 'xml:Comment'", 99, 46);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<xml:Text>', found 'xml:Element'", 100, 31);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<xml:Text>', found 'xml:Element'", 100, 46);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<xml:Text>', found 'xml:ProcessingInstruction'", 101, 31);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml<xml:Text>', found 'xml:ProcessingInstruction'", 101, 38);
        BAssertUtil.validateError(negativeResult, index++, "undefined symbol 'b'", 105, 32);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(int|float|decimal|string|boolean)', found 'map<string>'", 111, 32);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(int|float|decimal|string|boolean)', found 'error'", 111, 37);

        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml:Element', found 'xml<xml<xml:Element>>'", 116, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml:Comment', found 'xml<xml<xml<xml:Comment>>>'", 118, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml:ProcessingInstruction', found 'xml<xml<xml<xml:ProcessingInstruction>>>'", 120, 36);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml:Text', found 'xml'", 122, 19);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'xml:Text', found 'xml<xml>'", 124, 19);
        BAssertUtil.validateError(negativeResult, index++, "missing xml CDATA end token", 128, 49);
        BAssertUtil.validateError(negativeResult, index++, "xml namespaces cannot be interpolated", 133, 45);
        BAssertUtil.validateError(negativeResult, index++,
                "incompatible types: expected '(int|float|decimal|string|boolean|xml)', found 'string[]'", 140, 25);
        BAssertUtil.validateError(negativeResult, index++,
                "incompatible types: expected '(int|float|decimal|string|boolean|xml)', found 'string[]'", 141, 25);
        BAssertUtil.validateError(negativeResult, index++,
                "incompatible types: expected '(int|float|decimal|string|boolean|xml)', found 'int[]'", 142, 38);
        BAssertUtil.validateError(negativeResult, index++,
                "incompatible types: expected '(int|float|decimal|string|boolean|xml)', " +
                        "found 'ballerina/lang.object:0.0.0:RawTemplate[]'", 143, 25);
        BAssertUtil.validateError(negativeResult, index++,
                "incompatible types: 'ballerina/lang.object:0.0.0:RawTemplate[]' cannot be cast to 'string'", 144, 25);

        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(xml<xml:Text>|xml<xml:Comment>)', found 'xml'", 148, 44);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(xml<xml:Element>|xml:Text)', found 'xml'", 149, 39);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(xml<xml<xml:Text>>|xml<xml<xml:Comment>>)', found 'xml'", 150, 54);
        BAssertUtil.validateError(negativeResult, index++, "missing gt token", 154, 22);
        BAssertUtil.validateError(negativeResult, index++, "missing gt token", 155, 28);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'UX', found 'X'", 166, 12);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'UX', found 'X'", 169, 12);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'UX', found 'X'", 172, 12);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'UX', found 'X'", 175, 12);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'UX', found 'X'", 178, 12);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'UX', found 'X'", 181, 12);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'UX', found 'X'", 184, 12);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'UX', found 'X'", 187, 12);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'UX', found 'X'", 190, 12);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'UX', found 'X'", 193, 12);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected 'UX', found 'X'", 196, 12);
        Assert.assertEquals(index, negativeResult.getErrorCount());
    }

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
        BArray returns = (BArray) BRunUtil.invoke(result, "testXMLTextLiteral");
        Assert.assertTrue(returns.get(0) instanceof BXml);
        Assert.assertEquals(returns.get(0).toString(), "aaa");

        Assert.assertTrue(returns.get(1) instanceof BXml);
        Assert.assertEquals(returns.get(1).toString(), "11");

        Assert.assertTrue(returns.get(2) instanceof BXml);
        Assert.assertEquals(returns.get(2).toString(), "aaa11bbb22ccc");

        Assert.assertTrue(returns.get(3) instanceof BXml);
        Assert.assertEquals(returns.get(3).toString(), "aaa11bbb22ccc{d{}e}{f{");

        Assert.assertTrue(returns.get(4) instanceof BXml);
        Assert.assertEquals(returns.get(4).toString(), "aaa11b${bb22c\\}cc{d{}e}{f{");

        Assert.assertTrue(returns.get(5) instanceof BXml);
        Assert.assertEquals(returns.get(5).toString(), " ");
    }

    @Test
    public void testXMLCommentLiteral() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testXMLCommentLiteral");
        Assert.assertTrue(returns.get(0) instanceof BXml);
        Assert.assertEquals(returns.get(0).toString(), "<!--aaa-->");

        Assert.assertTrue(returns.get(1) instanceof BXml);
        Assert.assertEquals(returns.get(1).toString(), "<!--11-->");

        Assert.assertTrue(returns.get(2) instanceof BXml);
        Assert.assertEquals(returns.get(2).toString(), "<!--aaa11bbb22ccc-->");

        Assert.assertTrue(returns.get(3) instanceof BXml);
        Assert.assertEquals(returns.get(3).toString(), "<!--<aaa11bbb22cccd->e->-f<<{>>>-->");

        Assert.assertTrue(returns.get(4) instanceof BXml);
        Assert.assertEquals(returns.get(4).toString(), "<!---a-aa11b${bb22c\\}cc{d{}e}{f{-->");

        Assert.assertTrue(returns.get(5) instanceof BXml);
        Assert.assertEquals(returns.get(5).toString(), "<!---->");
    }

    @Test
    public void testXMLPILiteral() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testXMLPILiteral");
        Assert.assertTrue(returns.get(0) instanceof BXml);
        Assert.assertEquals(returns.get(0).toString(), "<?foo ?>");

        Assert.assertTrue(returns.get(1) instanceof BXml);
        Assert.assertEquals(returns.get(1).toString(), "<?foo 11?>");

        Assert.assertTrue(returns.get(2) instanceof BXml);
        Assert.assertEquals(returns.get(2).toString(), "<?foo aaa11bbb22ccc?>");

        Assert.assertTrue(returns.get(3) instanceof BXml);
        Assert.assertEquals(returns.get(3).toString(), "<?foo <aaa11bbb22ccc??d?e>?f<<{>>>?>");

        Assert.assertTrue(returns.get(4) instanceof BXml);
        Assert.assertEquals(returns.get(4).toString(), "<?foo ?a?aa11b${bb22c\\}cc{d{}e}{f{?>");
    }

    @Test
    public void testExpressionAsAttributeValue() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testExpressionAsAttributeValue");
        Assert.assertTrue(returns.get(0) instanceof BXml);
        Assert.assertEquals(returns.get(0).toString(), "<foo bar=\"&quot;zzz&quot;\"/>");

        Assert.assertTrue(returns.get(1) instanceof BXml);
        Assert.assertEquals(returns.get(1).toString(), "<foo bar=\"aaazzzbb'b33>22ccc?\"/>");

        Assert.assertTrue(returns.get(2) instanceof BXml);
        Assert.assertEquals(returns.get(2).toString(), "<foo bar=\"}aaazzzbbb33>22ccc{d{}e}{f{\"/>");

        Assert.assertTrue(returns.get(3) instanceof BXml);
        Assert.assertEquals(returns.get(3).toString(), "<foo bar1=\"aaa{zzz}b${b&quot;b33>22c\\}cc{d{}e}{f{\" "
                + "bar2=\"aaa{zzz}b${b&quot;b33>22c\\}cc{d{}e}{f{\"/>");

        Assert.assertTrue(returns.get(4) instanceof BXml);
        Assert.assertEquals(returns.get(4).toString(), "<foo bar=\"\"/>");
    }

    @Test
    public void testElementLiteralWithTemplateChildren() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testElementLiteralWithTemplateChildren");
        Assert.assertTrue(returns.get(0) instanceof BXml);
        Assert.assertEquals(returns.get(0).toString(), "<root>hello aaa&lt;bbb good morning <fname>John</fname> "
                + "<lname>Doe</lname>. Have a nice day!<foo>123</foo><bar/></root>");

        Assert.assertTrue(returns.get(1) instanceof BXml);
        BXmlSequence seq = (BXmlSequence) returns.get(1);
        Assert.assertEquals(seq.toString(), "hello aaa&lt;bbb good morning <fname>John</fname> <lname>Doe</lname>. "
                + "Have a nice day!<foo>123</foo><bar/>");

        BArray items = (BArray) seq.value();
        Assert.assertEquals(items.size(), 7);
    }

    @Test
    public void testXMLStartTag() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testXMLStartTag");
        Assert.assertTrue(returns.get(0) instanceof BXml);
        Assert.assertEquals(returns.get(0).toString(), "<fname>John</fname>");

        Assert.assertTrue(returns.get(1) instanceof BXml);
        Assert.assertEquals(returns.get(1).toString(), "<Country>US</Country>");

        Assert.assertTrue(returns.get(2) instanceof BXml);
        Assert.assertEquals(returns.get(2).toString(), "<_foo id=\"hello 5\">hello</_foo>");

        Assert.assertTrue(returns.get(3) instanceof BXml);
        Assert.assertEquals(returns.get(3).toString(), "<_-foo id=\"hello 5\">hello</_-foo>");
    }

    @Test
    public void testXMLLiteralWithEscapeSequence() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testXMLLiteralWithEscapeSequence");
        Assert.assertTrue(returns.get(0) instanceof BXml);
        Assert.assertEquals(returns.get(0).toString(), "hello &lt; &gt; &amp;"); // BXmlItem.toString()
        Assert.assertEquals(returns.get(1), 0L);
        Assert.assertEquals(arrayToString(returns.get(2)), "");
    }

    private String arrayToString(Object aReturn) {
        BArray ar = ((BArray) aReturn);
        StringBuilder builder = new StringBuilder();
        BIterator<?> bIterator = ar.getIterator();
        while (bIterator.hasNext()) {
            String str = bIterator.next().toString();
            builder.append(str);
        }
        return builder.toString();
    }

    @Test
    public void testTextWithValidMultiTypeExpressions() {
        Object returns = BRunUtil.invoke(result, "testTextWithValidMultiTypeExpressions");
        Assert.assertTrue(returns instanceof BXmlSequence);

        Assert.assertEquals(returns.toString(), "hello 11 world. How 1.35 are you true?");
    }

    @Test
    public void testArithmaticExpreesionInXMLTemplate() {
        Object returns = BRunUtil.invoke(result, "testArithmaticExpreesionInXMLTemplate");
        Assert.assertTrue(returns instanceof BXmlItem);

        Assert.assertEquals(returns.toString(), "<foo id=\"hello 5\">hello</foo>");
    }

    @Test
    public void testFunctionCallInXMLTemplate() {
        Object returns = BRunUtil.invoke(result, "testFunctionCallInXMLTemplate");
        Assert.assertTrue(returns instanceof BXmlItem);

        Assert.assertEquals(returns.toString(), "<foo>&lt;-->returned from a function</foo>");
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

    @Test(description = "Test sequence of brackets in content of XML")
    public void testBracketSequenceInXMLLiteral() {
        Object returns = BRunUtil.invoke(result, "testBracketSequenceInXMLLiteral");
        Assert.assertTrue(returns instanceof BXml);
        Assert.assertEquals(returns.toString(),
                "{}{{ {{{ { } }} }}} - extra }<elem>{}{{</elem>");
    }

    @Test(description = "Test interpolating xml using different types")
    public void testXMLLiteralInterpolation() {
        Object returns = BRunUtil.invoke(result, "testInterpolatingVariousTypes");
        Assert.assertTrue(returns instanceof BXml);
        Assert.assertEquals(returns.toString(),
                "<elem>42|3.14|31.4444|this-is-a-string|<abc/></elem>");
    }

    @Test(description = "Test interpolating xml when there are extra dollar signs")
    public void testXMLLiteralWithExtraDollarSigns() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testDollarSignOnXMLLiteralTemplate");
        Assert.assertTrue(returns.get(0) instanceof BXml);
        Assert.assertEquals(returns.get(0).toString(), "<foo id=\"hello $5\">hello</foo>");
        Assert.assertEquals(returns.get(1).toString(), "<foo id=\"hello $$5\">$hello</foo>");
        Assert.assertEquals(returns.get(2).toString(), "<foo id=\"hello $$ 5\">$$ hello</foo>");
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

    @Test
    public void testXMLInterpolationExprWithUserDefinedType() {
        BRunUtil.invoke(result, "testXMLInterpolationExprWithUserDefinedType");
    }

    @Test
    public void testQueryInXMLTemplateExpr() {
        BRunUtil.invoke(result, "testQueryInXMLTemplateExpr");
    }

    @Test
    public void testXMLLiteralWithConditionExpr() {
        BRunUtil.invoke(result, "testXMLLiteralWithConditionExpr");
    }

    @Test
    public void testXMLSubtype() {
        BRunUtil.invoke(result, "testXMLSubtype");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
