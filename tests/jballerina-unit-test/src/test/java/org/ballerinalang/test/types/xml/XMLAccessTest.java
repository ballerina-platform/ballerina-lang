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

import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for XML index-based and field-based access.
 *
 * @since 0.94.0
 */
public class XMLAccessTest {

    CompileResult result;
    CompileResult elementAccess;
    CompileResult navigation;
    CompileResult negativeResult;
    CompileResult navigationNegative;
    CompileResult navigationFilterNegative;


    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/xml/xml-indexed-access.bal");
        elementAccess = BCompileUtil.compile("test-src/types/xml/xml-element-access.bal");
        navigation = BCompileUtil.compile("test-src/types/xml/xml-navigation-access.bal");
        negativeResult = BCompileUtil.compile("test-src/types/xml/xml-indexed-access-negative.bal");
        navigationNegative = BCompileUtil.compile("test-src/types/xml/xml-nav-access-negative.bal");
        navigationFilterNegative = BCompileUtil.compile("test-src/types/xml/xml-nav-access-negative-filter.bal");
    }

    @Test
    public void testXMLElementAccessOnSingleElementXML() {
        BValue[] returns = BRunUtil.invoke(elementAccess, "testXMLElementAccessOnSingleElementXML");
        Assert.assertEquals(returns[0].stringValue(), "<ns:root xmlns:ns=\"foo\"></ns:root>");
        Assert.assertEquals(returns[1].stringValue(), "<ns:root xmlns:ns=\"foo\"></ns:root>");
        Assert.assertEquals(returns[2].stringValue(), "<ns:root xmlns:ns=\"foo\"></ns:root>");
        Assert.assertEquals(returns[3].stringValue(), "");
        Assert.assertEquals(returns[4].stringValue(), "");
        Assert.assertEquals(returns[5].stringValue(), "");
    }

    @Test
    public void testXMLElementAccessOnXMLSequence() {
        BValue[] returns = BRunUtil.invoke(elementAccess, "testXMLElementAccessOnXMLSequence");
        Assert.assertEquals(returns[0].stringValue(),
                "<ns:root xmlns:ns=\"foo\"></ns:root>" +
                        "<k:root xmlns:k=\"bar\"></k:root>" +
                        "<k:item xmlns:k=\"bar\"></k:item>");
        Assert.assertEquals(returns[1].stringValue(), "<ns:root xmlns:ns=\"foo\"></ns:root>");
        Assert.assertEquals(returns[2].stringValue(), "<ns:root xmlns:ns=\"foo\"></ns:root>");
        Assert.assertEquals(returns[3].stringValue(), "");
        Assert.assertEquals(returns[4].stringValue(), "");
        Assert.assertEquals(returns[5].stringValue(),
                "<k:root xmlns:k=\"bar\"></k:root><k:item xmlns:k=\"bar\"></k:item>");
    }

    @Test
    public void testXMLElementAccessMultipleFilters() {
        BValue[] returns = BRunUtil.invoke(elementAccess, "testXMLElementAccessMultipleFilters");
        Assert.assertEquals(returns[0].stringValue(),
                "<ns:root xmlns:ns=\"foo\"></ns:root>" +
                        "<k:root xmlns:k=\"bar\"></k:root>" +
                        "<k:item xmlns:k=\"bar\"></k:item>");

        Assert.assertEquals(returns[1].stringValue(),
                "<ns:root xmlns:ns=\"foo\"></ns:root>" +
                        "<k:root xmlns:k=\"bar\"></k:root>" +
                        "<k:item xmlns:k=\"bar\"></k:item>");

        Assert.assertEquals(returns[2].stringValue(),
                "<ns:root xmlns:ns=\"foo\"></ns:root>" +
                        "<k:root xmlns:k=\"bar\"></k:root>");

        Assert.assertEquals(returns[3].stringValue(),
                "<k:item xmlns:k=\"bar\"></k:item>");
    }

    @Test
    public void testXMLNavigationOnSingleElement() {
        BValue[] returns = BRunUtil.invoke(navigation, "testXMLNavigationOnSingleElement");
        Assert.assertEquals(returns[0].stringValue(), "<child attr=\"attr-val\"></child>");
        Assert.assertEquals(returns[1].stringValue(), "<child attr=\"attr-val\"></child>");
        Assert.assertEquals(returns[2].stringValue(), "<child attr=\"attr-val\"></child>");
        Assert.assertEquals(returns[3].stringValue(), "<child attr=\"attr-val\"></child>");
        Assert.assertEquals(returns[4].stringValue(), "<child attr=\"attr-val\"></child>");
    }

    @Test
    public void testXMLNavigationOnSingleElementWithNamespaces() {
        BValue[] returns = BRunUtil.invoke(navigation, "testXMLNavigationOnSingleElementWithNamespaces");
        Assert.assertEquals(returns[0].stringValue(), "<ns:child xmlns:ns=\"foo\"></ns:child>");
        Assert.assertEquals(returns[1].stringValue(), "<ns:child xmlns:ns=\"foo\"></ns:child>");
        Assert.assertEquals(returns[2].stringValue(), "<ns:child xmlns:ns=\"foo\"></ns:child>");
        Assert.assertEquals(returns[3].stringValue(), "<ns:child xmlns:ns=\"foo\"></ns:child>");
        Assert.assertEquals(returns[4].stringValue(), "<ns:child xmlns:ns=\"foo\"></ns:child>");
    }

    @Test
    public void testXMLNavigationOnSingleElementReferToDefaultNS() {
        BValue[] returns = BRunUtil.invoke(navigation, "testXMLNavigationOnSingleElementReferToDefaultNS");
        Assert.assertEquals(returns[0].stringValue(), "<child xmlns=\"foo\"></child>");
        Assert.assertEquals(returns[1].stringValue(), "<child xmlns=\"foo\"></child>");
        Assert.assertEquals(returns[2].stringValue(), "<child xmlns=\"foo\"></child>");
        Assert.assertEquals(returns[3].stringValue(), "<child xmlns=\"foo\"></child>");
        Assert.assertEquals(returns[4].stringValue(), "<child xmlns=\"foo\"></child>");
        Assert.assertEquals(returns[5].stringValue(), "<child xmlns=\"foo\"></child>");
        Assert.assertEquals(returns[6].stringValue(), "0");
        Assert.assertEquals(returns[7].stringValue(), "<child xmlns=\"foo\"></child>");
    }

    @Test
    public void testXMLNavigationOnSingleElementReferToDefaultNSViaPrefix() {
        BValue[] returns = BRunUtil.invoke(navigation, "testXMLNavigationOnSingleElementReferToDefaultNSViaPrefix");
        Assert.assertEquals(returns[0].stringValue(), "<child xmlns=\"foo\"></child>");
        Assert.assertEquals(returns[1].stringValue(), "<child xmlns=\"foo\"></child>");
        Assert.assertEquals(returns[2].stringValue(), "<child xmlns=\"foo\"></child>");
        Assert.assertEquals(returns[3].stringValue(), "<child xmlns=\"foo\"></child>");
        Assert.assertEquals(returns[4].stringValue(), "<child xmlns=\"foo\"></child>");
    }

    @Test
    public void testXMLNavigationOnSequence() {
        BValue[] returns = BRunUtil.invoke(navigation, "testXMLNavigationOnSequence");
        Assert.assertEquals(returns[0].stringValue(), "<child>A</child><child>B</child><child>C</child>");
        Assert.assertEquals(returns[1].stringValue(), "<child>A</child><child>B</child>" +
                "<child>C</child><it-child>D</it-child>TEXT");
        Assert.assertEquals(returns[2].stringValue(), "<child>A</child><child>B</child>" +
                "<child>C</child><it-child>D</it-child>");
        Assert.assertEquals(returns[3].stringValue(), "<child>A</child><child>B</child><child>C</child>");
        Assert.assertEquals(returns[4].stringValue(), "<child>A</child><child>B</child><child>C</child>");
    }

    @Test
    public void testXMLNavigationOnSequenceWithNamespaces() {
        BValue[] returns = BRunUtil.invoke(navigation, "testXMLNavigationOnSequenceWithNamespaces");
        Assert.assertEquals(returns[0].stringValue(),
                "<child xmlns=\"foo\">A</child><child xmlns=\"foo\" xmlns:ns=\"foo\">B</child>");
        Assert.assertEquals(returns[1].stringValue(),
                "<child xmlns=\"foo\">A</child><child xmlns=\"foo\" xmlns:ns=\"foo\">B</child>" +
                        "<k:child xmlns:k=\"bar\" xmlns=\"foo\">C</k:child><it-child xmlns=\"foo\">D</it-child>TEXT");
        Assert.assertEquals(returns[2].stringValue(),
                "<child xmlns=\"foo\">A</child>" +
                        "<child xmlns=\"foo\" xmlns:ns=\"foo\">B</child>" +
                        "<it-child xmlns=\"foo\">D</it-child>");
        Assert.assertEquals(returns[3].stringValue(),
                "<child xmlns=\"foo\">A</child><child xmlns=\"foo\" xmlns:ns=\"foo\">B</child>");
        Assert.assertEquals(returns[4].stringValue(),
                "<child xmlns=\"foo\">A</child><child xmlns=\"foo\" xmlns:ns=\"foo\">B</child>");
    }

    @Test
    public void testXMLNavigationOnSequenceWithNamespacesAndMultipleFilters() {
        BValue[] returns = BRunUtil.invoke(navigation, "testXMLNavigationOnSequenceWithNamespacesAndMultipleFilters");
        Assert.assertEquals(returns[0].stringValue(),
                "<child xmlns=\"foo\">A</child><child xmlns=\"foo\" xmlns:ns=\"foo\">B</child>" +
                        "<child2 xmlns=\"foo\">D</child2>");
        Assert.assertEquals(returns[2].stringValue(),
                "<child xmlns=\"foo\">A</child><child xmlns=\"foo\" xmlns:ns=\"foo\">B</child>" +
                        "<child2 xmlns=\"foo\">D</child2>");
        Assert.assertEquals(returns[3].stringValue(),
                "<child xmlns=\"foo\">A</child><child xmlns=\"foo\" xmlns:ns=\"foo\">B</child>" +
                        "<k:child xmlns:k=\"bar\" xmlns=\"foo\">C</k:child><child2 xmlns=\"foo\">D</child2>");
        Assert.assertEquals(returns[4].stringValue(),
                "<child xmlns=\"foo\">A</child><child xmlns=\"foo\" xmlns:ns=\"foo\">B</child>" +
                        "<child2 xmlns=\"foo\">D</child2>");
    }

    @Test
    public void testXMLElementAccessNavigationAccessComposition() {
        BValue[] returns = BRunUtil.invoke(navigation, "testXMLElementAccessNavigationAccessComposition");
        Assert.assertEquals(returns[0].stringValue(), "<lname>Gunae</lname><lname>Jayee</lname><lname>Kumarae</lname>");
        Assert.assertEquals(returns[1].stringValue(), "<fname>Kamal</fname><lname>Gunae</lname><fname>Nimal</fname>" +
                "<lname>Jayee</lname><fname>Sunil</fname><lname>Kumarae</lname>");
        Assert.assertEquals(returns[2].stringValue(), "<lname>Gunae</lname><lname>Jayee</lname><lname>Kumarae</lname>");
        Assert.assertEquals(returns[3].stringValue(), "<fname>Kamal</fname><fname>Nimal</fname><fname>Sunil</fname>");
        Assert.assertEquals(returns[4].stringValue(), "KamalGunaeNimalJayeeSunilKumarae");
        Assert.assertEquals(returns[5].stringValue(), "<fname>Kamal</fname><fname>Nimal</fname><fname>Sunil</fname>");
        Assert.assertEquals(returns[6].stringValue(), "<fname>Kamal</fname><fname>Nimal</fname><fname>Sunil</fname>");
    }

    @Test
    public void testXMLNavigationExpressionWithQuotedIdentifiers() {
        BValue[] returns = BRunUtil.invoke(navigation, "testXMLNavigationExpressionWithQuotedIdentifiers");
        Assert.assertEquals(returns[0].stringValue(),
                "<object xmlns=\"http://www.force.com/2009/06/asyncapi/dataload\">Account</object>");
        Assert.assertEquals(returns[1].stringValue(),
                "<object xmlns=\"http://www.force.com/2009/06/asyncapi/dataload\">Account</object>");
    }

    @Test
    public void testXMLNavigationExpressionWithXMLSubtypeOnLHS() {
        BRunUtil.invoke(navigation, "testXMLNavigationExpressionWithXMLSubtypeOnLHS");
    }

    @Test
    public void testXMLNavigationDescendantsStepWithXMLSubtypeOnLHS() {
        BRunUtil.invoke(navigation, "testXMLNavigationDescendantsStepWithXMLSubtypeOnLHS");
    }

    @Test
    public void testInvalidXMLAccessWithIndex() {
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "invalid expr in assignment lhs", 4, 5);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'string'", 9, 15);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'boolean'", 10, 15);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found 'float'", 11, 15);
        BAssertUtil.validateError(negativeResult, i++, "cannot update an xml sequence", 18, 5);
        BAssertUtil.validateError(negativeResult, i++, "invalid operation: type '(string|xml:Text)' does" +
                " not support member access", 21, 28);
        BAssertUtil.validateError(negativeResult, i++, "invalid operation: type '(string|xml:Text)' does " +
                "not support member access", 22, 14);
        Assert.assertEquals(negativeResult.getErrorCount(), i);
    }


    @Test
    public void testXMLAccessWithIndex() {
        BRunUtil.invoke(result, "testXMLAccessWithIndex");
        BRunUtil.invoke(result, "testXMLSequenceAccessWithIndex");
    }

    @Test
    public void testLengthOfXMLSequence() {
        BValue[] returns = BRunUtil.invoke(result, "testLengthOfXMLSequence");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 3);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 2);
    }

    @Test
    public void testXMLNavExpressionNegative() {
        String methodInvocMessage = "method invocations are not yet supported within XML navigation expressions, " +
                "use a grouping expression (parenthesis) " +
                "if you intend to invoke the method on the result of the navigation expression.";

        String navIndexingMessage = "member access operations are not yet supported within XML navigation " +
                "expressions, use a grouping expression (parenthesis) " +
                "if you intend to member-access the result of the navigation expression.";
        int i = 0;
        BAssertUtil.validateError(navigationNegative, i++, methodInvocMessage, 3, 13);
        BAssertUtil.validateError(navigationNegative, i++, methodInvocMessage, 4, 13);
        BAssertUtil.validateError(navigationNegative, i++, methodInvocMessage, 5, 13);
        BAssertUtil.validateError(navigationNegative, i++, methodInvocMessage, 6, 13);
        BAssertUtil.validateError(navigationNegative, i++, methodInvocMessage, 7, 13);
        BAssertUtil.validateError(navigationNegative, i++, navIndexingMessage, 8, 13);
        BAssertUtil.validateError(navigationNegative, i++, navIndexingMessage, 9, 13);
        Assert.assertEquals(navigationNegative.getErrorCount(), i);
    }

    @Test
    public void testXMLNavExpressionTypeCheckNegative() {
        CompileResult compile = BCompileUtil.compile("test-src/types/xml/xml-nav-access-type-check-negative.bal");
        int i = 0;
        BAssertUtil.validateError(compile, i++,
                "incompatible types: expected 'xml<xml:Comment>', found 'xml<xml:Element>'", 19, 27);
        BAssertUtil.validateError(compile, i++,
                "incompatible types: expected 'xml<xml:Comment>', found 'xml<xml:Element>'", 20, 27);
        BAssertUtil.validateError(compile, i++,
                "incompatible types: expected 'xml<xml:Comment>', found 'xml<xml:Element>'", 21, 28);
        Assert.assertEquals(compile.getErrorCount(), i);
    }

    @Test void testXMLFilterExpressionsNegative() {
        BAssertUtil.validateError(navigationFilterNegative, 0,
                "incompatible types: expected 'xml', found 'any'", 4, 14);
        BAssertUtil.validateError(navigationFilterNegative, 1,
                "incompatible types: expected 'xml', found 'int'", 6, 14);
        Assert.assertEquals(navigationFilterNegative.getErrorCount(), 2);
    }
}
