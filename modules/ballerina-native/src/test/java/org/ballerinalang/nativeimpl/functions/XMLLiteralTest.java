/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.nativeimpl.functions;

import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.model.values.BXMLSequence;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.ParserException;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Test class for XML literal.
 */
public class XMLLiteralTest {
    private ProgramFile programFile;
    private ProgramFile literalWithNamespacesProgFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("samples/xml/xmlLiteralTest.bal");
        literalWithNamespacesProgFile = BTestUtils.getProgramFile("samples/xml/xmlLiteralsWithNamespaces.bal");
    }

    @Test
    public void testXMLTextLiteral() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testXMLTextLiteral", args);
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
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testXMLCommentLiteral", args);
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
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testXMLPILiteral", args);
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
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testExpressionAsElementName", args);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<11>hello</11>");

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<foo&gt;bar>hello</foo&gt;bar>");
    }

    @Test(expectedExceptions = { ParserException.class },
            expectedExceptionsMessageRegExp = "xmlElementNameWithCominedExpressions.bal:3:18: invalid identifier '<'")
    public void testCombinedExpressionsAsElementName() {
        BTestUtils.getProgramFile("samples/xml/xmlElementNameWithCominedExpressions.bal");
    }

    @Test
    public void testExpressionAsAttributeName() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testExpressionAsAttributeName", args);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<foo bar=\"attribute value\">hello</foo>");

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<foo foo&gt;bar=\"attribute value\">hello</foo>");
    }
    
    @Test
    public void testExpressionAsAttributeValue() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testExpressionAsAttributeValue", args);
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
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testElementLiteralWithTemplateChildren", args);
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
        BValue[] args = {};
        BValue[] returns =
                BLangFunctions.invokeNew(literalWithNamespacesProgFile, "testElementLiteralWithNamespaces", args);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root xmlns=\"http://ballerina.com/\" xmlns:ns0=\"http://ballerina.com/a\" xmlns:ns1="
                        + "\"http://ballerina.com/c\" ns0:id=\"456\"><foo>123</foo><bar ns1:status=\"complete\"/>"
                        + "</root>");

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
        BValue[] args = {};
        BValue[] returns =
                BLangFunctions.invokeNew(literalWithNamespacesProgFile, "testElementWithQualifiedName", args);
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
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(literalWithNamespacesProgFile, "testDefineInlineNamespace", args);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "<nsx:foo xmlns:nsx=\"http://wso2.com\" "
                + "xmlns:ns1=\"http://ballerina.com/b\" nsx:id=\"123\">hello</nsx:foo>");
    }

    @Test
    public void testDefineInlineDefaultNamespace() {
        BValue[] args = {};
        BValue[] returns =
                BLangFunctions.invokeNew(literalWithNamespacesProgFile, "testDefineInlineDefaultNamespace", args);
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
            expectedExceptionsMessageRegExp = ".*error: ballerina.lang.errors:Error, message: start and end tag names"
                    + " mismatch: 'foo' and 'bar'.*")
    public void testMismatchTagNameVar() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testMismatchTagNameVar", args);
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(), "");
    }

    @Test(expectedExceptions = { SemanticException.class },
            expectedExceptionsMessageRegExp = "xmlTextWithMultiTypeExpressions.bal:6: incompatible types: expected"
                    + " 'string', found 'xml'")
    public void testTextWithInvalidMultiTypeExpressions() {
        BTestUtils.getProgramFile("samples/xml/xmlTextWithMultiTypeExpressions.bal");
    }

    @Test
    public void testTextWithValidMultiTypeExpressions() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testTextWithValidMultiTypeExpressions", args);
        Assert.assertTrue(returns[0] instanceof BXMLItem);

        Assert.assertEquals(returns[0].stringValue(), "hello 11 world. How 1.35 are you true?");
    }

    @Test
    public void testArithmaticExpreesionInXMLTemplate() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testArithmaticExpreesionInXMLTemplate", args);
        Assert.assertTrue(returns[0] instanceof BXMLItem);

        Assert.assertEquals(returns[0].stringValue(), "<foo id=\"hello 5\">hello</foo>");
    }

    @Test
    public void testFunctionCallInXMLTemplate() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testFunctionCallInXMLTemplate", args);
        Assert.assertTrue(returns[0] instanceof BXMLItem);

        Assert.assertEquals(returns[0].stringValue(), "<foo>&lt;-->returned from a function</foo>");
    }

    @Test(expectedExceptions = { SemanticException.class },
            expectedExceptionsMessageRegExp = "xmlRestrictedElementPrefix.bal:3: invalid namespace prefix 'xmlns'")
    public void xmlRestrictedElementPrefix() {
        BTestUtils.getProgramFile("samples/xml/xmlRestrictedElementPrefix.bal");
    }

    @Test(expectedExceptions = { SemanticException.class },
            expectedExceptionsMessageRegExp = "xmlUndeclaredElementPrefix.bal:3: undefined namespace 'ns1'")
    public void xmlUndeclaredElementPrefix() {
        BTestUtils.getProgramFile("samples/xml/xmlUndeclaredElementPrefix.bal");
    }

    @Test(expectedExceptions = { SemanticException.class },
            expectedExceptionsMessageRegExp = "xmlTemplateWithNonXML.bal:3: incompatible types in xml template "
                    + "literal. expected 'xml' or 'string', found 'map'")
    public void testTextWithMultiTypeExpressions() {
        BTestUtils.getProgramFile("samples/xml/xmlTemplateWithNonXML.bal");
    }

    @Test(expectedExceptions = { SemanticException.class },
            expectedExceptionsMessageRegExp = "defineEmptyNamespaceInline.bal:2: cannot bind a prefix \\('ns0'\\) to"
                    + " the empty namespace name")
    public void testDefineEmptyNamespaceInline() {
        BTestUtils.getProgramFile("samples/xml/defineEmptyNamespaceInline.bal");
    }

    @Test
    public void testUsingNamespcesOfParent() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(literalWithNamespacesProgFile, "testUsingNamespcesOfParent", args);
        Assert.assertTrue(returns[0] instanceof BXMLItem);

        Assert.assertEquals(returns[0].stringValue(), "<root xmlns:ns0=\"http://ballerinalang.com/\" "
                + "xmlns:ns1=\"http://ballerina.com/b\"><ns0:foo>hello</ns0:foo></root>");
    }
    
    @Test
    public void testComplexXMLLiteral() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(literalWithNamespacesProgFile, "testComplexXMLLiteral", args);
        Assert.assertTrue(returns[0] instanceof BXMLItem);
        Assert.assertEquals(returns[0].stringValue(), getComplexXMLContent());
    }
    
    private String getComplexXMLContent() {
        InputStream is = ClassLoader.getSystemResourceAsStream("samples/xml/sampleXML.txt");
        InputStreamReader inputStreamREader = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            inputStreamREader = new InputStreamReader(is);
            br = new BufferedReader(inputStreamREader);
            String content = br.readLine();
            if (content == null) {
                return sb.toString();
            }

            sb.append(content);

            while ((content = br.readLine()) != null) {
                sb.append("\n" + content);
            }
        } catch (IOException ignore) {
        } finally {
            if (inputStreamREader != null) {
                try {
                    inputStreamREader.close();
                } catch (IOException ignore) {
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignore) {
                }
            }
        }
        return sb.toString();
    }
}
