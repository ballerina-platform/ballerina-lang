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


import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.model.values.BXMLSequence;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test class for XML literal.
 *
 * @since 0.94
 */
public class XMLLiteralTest {

    CompileResult result;
    CompileResult literalWithNamespacesResult;
    CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/xml/xml-literals.bal");
        literalWithNamespacesResult = BCompileUtil.compile("test-src/types/xml/xml-literals-with-namespaces.bal");
        negativeResult = BCompileUtil.compile("test-src/types/xml/xml-literals-negative.bal");
    }

    @Test
    public void testXMLNegativeSemantics() {
        BAssertUtil.validateError(negativeResult, 0, "invalid namespace prefix 'xmlns'", 5, 19);
        BAssertUtil.validateError(negativeResult, 1, "invalid namespace prefix 'xmlns'", 5, 36);

        // undeclared element prefix
        BAssertUtil.validateError(negativeResult, 2, "undefined symbol 'ns1'", 10, 19);
        BAssertUtil.validateError(negativeResult, 3, "undefined symbol 'ns1'", 10, 34);

        // text with multi type expressions
        BAssertUtil.validateError(negativeResult, 4, "incompatible types: expected 'xml', found 'map'", 16, 59);

        // combined expressions as element name
        // TODO:

        // text with invalid multi type expressions
        BAssertUtil.validateError(negativeResult, 5, "incompatible types: expected 'string', found 'xml'", 33, 53);

        // redeclare namespaces
        BAssertUtil.validateError(negativeResult, 6, "redeclared symbol 'ns0'", 42, 9);

        // assigning attributes-map to a map
        BAssertUtil.validateError(negativeResult, 7, "incompatible types: expected 'map', found 'xml-attributes'", 48,
                14);

        // namespace conflict with package import
        BAssertUtil.validateError(negativeResult, 8, "redeclared symbol 'x'", 52, 5);

        // get attributes from non-xml
        BAssertUtil.validateError(negativeResult, 9, "incompatible types: expected 'xml', found 'map'", 57, 16);

        // update attributes map
        BAssertUtil.validateError(negativeResult, 10,
                "xml attributes cannot be updated as a collection. update attributes one at a time", 62, 5);

        // update qname
        BAssertUtil.validateError(negativeResult, 11, "cannot assign values to an xml qualified name", 67, 5);

        // use of undefined namespace for qname
        BAssertUtil.validateError(negativeResult, 12, "undefined package 'ns0'", 75, 19);
        
        // define namespace with empty URI
        BAssertUtil.validateError(negativeResult, 13, "cannot bind prefix 'ns0' to the empty namespace name", 79, 5);
    }

    @Test
    public void testCombinedExpressionsAsElementName() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/types/xml/xml-invalid-syntax-1.bal");
        Assert.assertEquals(negativeResult.getErrorCount(), 1);
        BAssertUtil.validateError(negativeResult, 0, "invalid token '{{'", 3, 24);
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
        Assert.assertEquals(returns[4].stringValue(), "aaa11b{{bb22c}}cc{d{}e}{f{");

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
        Assert.assertEquals(returns[3].stringValue(), "<!--<aaa11bbb22ccc--d->e->-f<<{>>>-->");

        Assert.assertTrue(returns[4] instanceof BXML);
        Assert.assertEquals(returns[4].stringValue(), "<!---a-aa11b{{bb22c}}cc{d{}e}{f{-->");

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
        Assert.assertEquals(returns[4].stringValue(), "<?foo  ?a?aa11b{{bb22c}}cc{d{}e}{f{?>");
    }

    @Test
    public void testExpressionAsElementName() {
        BValue[] returns = BRunUtil.invoke(result, "testExpressionAsElementName");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<foo>hello</foo>");

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<bar3>hello</bar3>");
    }

    @Test
    public void testExpressionAsAttributeName() {
        BValue[] returns = BRunUtil.invoke(result, "testExpressionAsAttributeName");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<foo foo=\"attribute value\">hello</foo>");

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<foo bar5=\"attribute value\">hello</foo>");
    }
    
    @Test
    public void testExpressionAsAttributeValue() {
        BValue[] returns = BRunUtil.invoke(result, "testExpressionAsAttributeValue");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<foo bar=\"&quot;zzz&quot;\"/>");

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<foo bar=\"aaazzzbb'b33>22ccc?\"/>");

        Assert.assertTrue(returns[2] instanceof BXML);
        Assert.assertEquals(returns[2].stringValue(), "<foo bar=\"}aaazzzbbb33>22ccc{d{}e}{f{\"/>");

        Assert.assertTrue(returns[3] instanceof BXML);
        Assert.assertEquals(returns[3].stringValue(), "<foo bar1=\"aaa{zzz}b{{b&quot;b33>22c}}cc{d{}e}{f{\" "
                + "bar2=\"aaa{zzz}b{{b&quot;b33>22c}}cc{d{}e}{f{\"/>");

        Assert.assertTrue(returns[4] instanceof BXML);
        Assert.assertEquals(returns[4].stringValue(), "<foo bar=\"\"/>");
    }

    @Test
    public void testElementLiteralWithTemplateChildren() {
        BValue[] returns = BRunUtil.invoke(result, "testElementLiteralWithTemplateChildren");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root>hello aaa&lt;bbb good morning <fname>John</fname> "
                + "<lname>Doe</lname>. Have a nice day!<foo>123</foo><bar/></root>");

        Assert.assertTrue(returns[1] instanceof BXML);
        BXMLSequence seq = (BXMLSequence) returns[1];
        Assert.assertEquals(seq.stringValue(), "hello aaa<bbb good morning <fname>John</fname> <lname>Doe</lname>. "
                + "Have a nice day!<foo>123</foo><bar/>");

        BRefValueArray items = seq.value();
        Assert.assertEquals(items.size(), 7);
    }

    @Test
    public void testElementLiteralWithNamespaces() {
        BValue[] returns =
                BRunUtil.invoke(literalWithNamespacesResult, "testElementLiteralWithNamespaces");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://ballerina.com/\" xmlns:ns0=\"http://ballerina.com/a\" "
                        + "xmlns:ns1=\"http://ballerina.com/c\" ns0:id=\"456\"><foo>123</foo>"
                        + "<bar ns1:status=\"complete\"/></root>");

        Assert.assertTrue(returns[1] instanceof BXML);
        BXMLSequence seq = (BXMLSequence) returns[1];
        Assert.assertEquals(seq.stringValue(),
                "<foo xmlns=\"http://ballerina.com/\" "
                        + "xmlns:ns0=\"http://ballerina.com/a\" xmlns:ns1=\"http://ballerina.com/c\">123</foo>"
                        + "<bar xmlns=\"http://ballerina.com/\" xmlns:ns0=\"http://ballerina.com/a\" "
                        + "xmlns:ns1=\"http://ballerina.com/c\" ns1:status=\"complete\"/>");

        BRefValueArray items = seq.value();
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

        Assert.assertTrue(returns[3] instanceof BXML);
        Assert.assertEquals(returns[3].stringValue(),
                "<nsRJUck:root xmlns:nsRJUck=\"http://wso2.com\" xmlns=\"http://ballerina.com/\" "
                        + "xmlns:ns1=\"http://ballerina.com/b\">hello</nsRJUck:root>");

        Assert.assertTrue(returns[4] instanceof BXML);
        Assert.assertEquals(returns[4].stringValue(),
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

        Assert.assertTrue(returns[2] instanceof BXML);
        Assert.assertEquals(returns[2].stringValue(),
                "<foo xmlns=\"http://ballerina.com\" xmlns:nsx=\"http://wso2.com/aaa\" "
                        + "xmlns:ns1=\"http://ballerina.com/b\">hello</foo>");
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error: error, message: start and end tag names mismatch: 'foo' and " +
                    "'bar'.*")
    public void testMismatchTagNameVar() {
        BRunUtil.invoke(result, "testMismatchTagNameVar");
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

        Assert.assertEquals(returns[0].stringValue(), "<foo>&lt;-->returned from a function</foo>");
    }

    @Test
    public void testUsingNamespcesOfParent() {
        BValue[] returns = BRunUtil.invoke(literalWithNamespacesResult, "testUsingNamespcesOfParent");
        Assert.assertTrue(returns[0] instanceof BXMLItem);

        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns0=\"http://ballerinalang.com/\" "
                + "xmlns:ns1=\"http://ballerina.com/b\"><ns0:foo>hello</ns0:foo></root>");
    }

    @Test
    public void testComplexXMLLiteral() throws IOException {
        BValue[] returns = BRunUtil.invoke(literalWithNamespacesResult, "testComplexXMLLiteral");
        Assert.assertTrue(returns[0] instanceof BXMLItem);
        Assert.assertEquals(returns[0].stringValue(),
                BCompileUtil.readFileAsString("test-src/types/xml/sampleXML.txt"));
    }

    @Test
    public void testElementWithEmptyUriQualifiedName() {
        BValue[] returns = BRunUtil.invoke(literalWithNamespacesResult, "testElementWithEmptyUriQualifiedName");
        Assert.assertTrue(returns[0] instanceof BXMLItem);

        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns1=\"http://ballerina.com/b\">hello</root>");
        Assert.assertEquals(returns[1].stringValue(),
                "<root xmlns=\"http://ballerina.com/\" xmlns:ns1=\"http://ballerina.com/b\">hello</root>");
        Assert.assertEquals(returns[2].stringValue(),
                "<root xmlns=\"http://ballerina.com/\" xmlns:ns1=\"http://ballerina.com/b\">hello</root>");
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
    public void testNullXMLinXMLLiteral() {
        BValue[] returns = BRunUtil.invoke(result, "testNullXMLinXMLLiteral");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<root></root>");
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error: error, message: invalid xml qualified name: unsupported " +
                    "characters in '11'.*")
    public void testInvalidElementName_1() {
        BRunUtil.invoke(result, "testInvalidElementName_1");
    }

    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error: error, message: invalid xml qualified name: unsupported " +
                    "characters in 'foo&gt;bar'.*")
    public void testInvalidElementName_2() {
        BRunUtil.invoke(result, "testInvalidElementName_2");
    }
    
    @Test(expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*invalid xml qualified name: unsupported characters in 'foo&gt;bar'.*")
    public void testIvalidAttributeName() {
        BRunUtil.invoke(result, "testIvalidAttributeName");
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
}
