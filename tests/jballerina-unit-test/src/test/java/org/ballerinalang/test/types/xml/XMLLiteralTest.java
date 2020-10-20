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
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BIterator;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.model.values.BXML;
import org.ballerinalang.core.model.values.BXMLItem;
import org.ballerinalang.core.model.values.BXMLSequence;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for XML literal.
 *
 * @since 0.94
 */
@Test(groups = { "disableOnOldParser" })
public class XMLLiteralTest {

    private CompileResult result;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/xml/xml-literals.bal");
    }

    @Test(enabled = false)
    public void testXMLNegativeSemantics() {
        negativeResult = BCompileUtil.compile("test-src/types/xml/xml-literals-negative.bal");
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

        // update qname
        BAssertUtil.validateError(negativeResult, index++, "cannot assign values to an xml qualified name", 47, 5);

        // use of undefined namespace for qname
        BAssertUtil.validateError(negativeResult, index++, "cannot find xml namespace prefix 'ns0'", 55, 24);

        // define namespace with empty URI
        BAssertUtil.validateError(negativeResult, index++, "cannot bind prefix 'ns0' to the empty namespace name",
                59, 5);

        // XML elements with mismatching start and end tags
        BAssertUtil.validateError(negativeResult, index++, "mismatching start and end tags found in xml element",
                                  63, 18);
        // XML interpolation is not allowed to interpolate XML namespace attributes
        BAssertUtil.validateError(negativeResult, index++, "xml namespaces cannot be interpolated", 72, 29);
        BAssertUtil.validateError(negativeResult, index++, "xml namespaces cannot be interpolated", 72, 47);
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
        Assert.assertEquals(arrayToString(returns[1]), "hello < > &");
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 0);
        Assert.assertEquals(arrayToString(returns[3]), "");
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
        io.ballerina.runtime.api.values.BXML xml = XMLFactory.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<!DOCTYPE foo [<!ELEMENT foo ANY ><!ENTITY data \"Example\" >]><foo>&data;</foo>");
        Assert.assertEquals(xml.toString(), "<foo>Example</foo>");
    }
}
