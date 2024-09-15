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
        BArray returns = (BArray) BRunUtil.invoke(elementAccess, "testXMLElementAccessOnSingleElementXML");
        Assert.assertEquals(returns.get(0).toString(), "<ns:root xmlns:ns=\"foo\"/>");
        Assert.assertEquals(returns.get(1).toString(), "<ns:root xmlns:ns=\"foo\"/>");
        Assert.assertEquals(returns.get(2).toString(), "<ns:root xmlns:ns=\"foo\"/>");
        Assert.assertEquals(returns.get(3).toString(), "");
        Assert.assertEquals(returns.get(4).toString(), "");
        Assert.assertEquals(returns.get(5).toString(), "");
    }

    @Test
    public void testXMLElementAccessOnXMLSequence() {
        BArray returns = (BArray) BRunUtil.invoke(elementAccess, "testXMLElementAccessOnXMLSequence");
        Assert.assertEquals(returns.get(0).toString(),
                "<ns:root xmlns:ns=\"foo\"/>" +
                        "<k:root xmlns:k=\"bar\"/>" +
                        "<k:item xmlns:k=\"bar\"/>");
        Assert.assertEquals(returns.get(1).toString(), "<ns:root xmlns:ns=\"foo\"/>");
        Assert.assertEquals(returns.get(2).toString(), "<ns:root xmlns:ns=\"foo\"/>");
        Assert.assertEquals(returns.get(3).toString(), "");
        Assert.assertEquals(returns.get(4).toString(), "");
        Assert.assertEquals(returns.get(5).toString(),
                "<k:root xmlns:k=\"bar\"/><k:item xmlns:k=\"bar\"/>");
    }

    @Test
    public void testXMLElementAccessMultipleFilters() {
        BArray returns = (BArray) BRunUtil.invoke(elementAccess, "testXMLElementAccessMultipleFilters");
        Assert.assertEquals(returns.get(0).toString(),
                "<ns:root xmlns:ns=\"foo\"/>" +
                        "<k:root xmlns:k=\"bar\"/>" +
                        "<k:item xmlns:k=\"bar\"/>");

        Assert.assertEquals(returns.get(1).toString(),
                "<ns:root xmlns:ns=\"foo\"/>" +
                        "<k:root xmlns:k=\"bar\"/>" +
                        "<k:item xmlns:k=\"bar\"/>");

        Assert.assertEquals(returns.get(2).toString(),
                "<ns:root xmlns:ns=\"foo\"/>" +
                        "<k:root xmlns:k=\"bar\"/>");

        Assert.assertEquals(returns.get(3).toString(),
                "<k:item xmlns:k=\"bar\"/>");
    }

    @Test
    public void testXMLNavigationOnSingleElement() {
        BArray returns = (BArray) BRunUtil.invoke(navigation, "testXMLNavigationOnSingleElement");
        Assert.assertEquals(returns.get(0).toString(), "<child attr=\"attr-val\"/>");
        Assert.assertEquals(returns.get(1).toString(), "<child attr=\"attr-val\"/>");
        Assert.assertEquals(returns.get(2).toString(), "<child attr=\"attr-val\"/>");
        Assert.assertEquals(returns.get(3).toString(), "<child attr=\"attr-val\"/>");
        Assert.assertEquals(returns.get(4).toString(), "<child attr=\"attr-val\"/>");
    }

    @Test
    public void testXMLNavigationOnSingleElementWithNamespaces() {
        BArray returns = (BArray) BRunUtil.invoke(navigation, "testXMLNavigationOnSingleElementWithNamespaces");
        Assert.assertEquals(returns.get(0).toString(), "<ns:child xmlns:ns=\"foo\"/>");
        Assert.assertEquals(returns.get(1).toString(), "<ns:child xmlns:ns=\"foo\"/>");
        Assert.assertEquals(returns.get(2).toString(), "<ns:child xmlns:ns=\"foo\"/>");
        Assert.assertEquals(returns.get(3).toString(), "<ns:child xmlns:ns=\"foo\"/>");
        Assert.assertEquals(returns.get(4).toString(), "<ns:child xmlns:ns=\"foo\"/>");
    }

    @Test
    public void testXMLNavigationOnSingleElementReferToDefaultNS() {
        BArray returns = (BArray) BRunUtil.invoke(navigation, "testXMLNavigationOnSingleElementReferToDefaultNS");
        Assert.assertEquals(returns.get(0).toString(), "<child xmlns=\"foo\"/>");
        Assert.assertEquals(returns.get(1).toString(), "<child xmlns=\"foo\"/>");
        Assert.assertEquals(returns.get(2).toString(), "<child xmlns=\"foo\"/>");
        Assert.assertEquals(returns.get(3).toString(), "<child xmlns=\"foo\"/>");
        Assert.assertEquals(returns.get(4).toString(), "<child xmlns=\"foo\"/>");
        Assert.assertEquals(returns.get(5).toString(), "<child xmlns=\"foo\"/>");
        Assert.assertEquals(returns.get(6).toString(), "0");
        Assert.assertEquals(returns.get(7).toString(), "<child xmlns=\"foo\"/>");
    }

    @Test
    public void testXMLNavigationOnSingleElementReferToDefaultNSViaPrefix() {
        BArray returns =
                (BArray) BRunUtil.invoke(navigation, "testXMLNavigationOnSingleElementReferToDefaultNSViaPrefix");
        Assert.assertEquals(returns.get(0).toString(), "<child xmlns=\"foo\"/>");
        Assert.assertEquals(returns.get(1).toString(), "<child xmlns=\"foo\"/>");
        Assert.assertEquals(returns.get(2).toString(), "<child xmlns=\"foo\"/>");
        Assert.assertEquals(returns.get(3).toString(), "<child xmlns=\"foo\"/>");
        Assert.assertEquals(returns.get(4).toString(), "<child xmlns=\"foo\"/>");
    }

    @Test
    public void testXMLNavigationOnSequence() {
        BArray returns = (BArray) BRunUtil.invoke(navigation, "testXMLNavigationOnSequence");
        Assert.assertEquals(returns.get(0).toString(), "<child>A</child><child>B</child><child>C</child>");
        Assert.assertEquals(returns.get(1).toString(), "<child>A</child><child>B</child>" +
                "<child>C</child><it-child>D</it-child>TEXT");
        Assert.assertEquals(returns.get(2).toString(), "<child>A</child><child>B</child>" +
                "<child>C</child><it-child>D</it-child>");
        Assert.assertEquals(returns.get(3).toString(), "<child>A</child><child>B</child><child>C</child>");
        Assert.assertEquals(returns.get(4).toString(), "<child>A</child><child>B</child><child>C</child>");
    }

    @Test
    public void testXMLNavigationOnSequenceWithNamespaces() {
        BArray returns = (BArray) BRunUtil.invoke(navigation, "testXMLNavigationOnSequenceWithNamespaces");
        Assert.assertEquals(returns.get(0).toString(),
                "<child xmlns=\"foo\">A</child><child xmlns=\"foo\" xmlns:ns=\"foo\">B</child>");
        Assert.assertEquals(returns.get(1).toString(),
                "<child xmlns=\"foo\">A</child><child xmlns=\"foo\" xmlns:ns=\"foo\">B</child><k:child xmlns=\"foo\" " +
                        "xmlns:k=\"bar\">C</k:child><it-child xmlns=\"foo\">D</it-child>TEXT");
        Assert.assertEquals(returns.get(2).toString(),
                "<child xmlns=\"foo\">A</child><child xmlns=\"foo\" xmlns:ns=\"foo\">B</child><k:child xmlns=\"foo\" " +
                        "xmlns:k=\"bar\">C</k:child><it-child xmlns=\"foo\">D</it-child>");
        Assert.assertEquals(returns.get(3).toString(),
                "<child xmlns=\"foo\">A</child><child xmlns=\"foo\" xmlns:ns=\"foo\">B</child>");
        Assert.assertEquals(returns.get(4).toString(),
                "<child xmlns=\"foo\">A</child><child xmlns=\"foo\" xmlns:ns=\"foo\">B</child>");
    }

    @Test
    public void testXMLNavigationOnSequenceWithNamespacesAndMultipleFilters() {
        BArray returns =
                (BArray) BRunUtil.invoke(navigation, "testXMLNavigationOnSequenceWithNamespacesAndMultipleFilters");
        Assert.assertEquals(returns.get(0).toString(),
                "<child xmlns=\"foo\">A</child><child xmlns=\"foo\" xmlns:ns=\"foo\">B</child>" +
                        "<child2 xmlns=\"foo\">D</child2>");
        Assert.assertEquals(returns.get(2).toString(),
                "<child xmlns=\"foo\">A</child><child xmlns=\"foo\" xmlns:ns=\"foo\">B</child><k:child xmlns=\"foo\" " +
                        "xmlns:k=\"bar\">C</k:child><child2 xmlns=\"foo\">D</child2>");
        Assert.assertEquals(returns.get(3).toString(),
                "<child xmlns=\"foo\">A</child><child xmlns=\"foo\" xmlns:ns=\"foo\">B</child><k:child xmlns=\"foo\" " +
                        "xmlns:k=\"bar\">C</k:child><child2 xmlns=\"foo\">D</child2>");
        Assert.assertEquals(returns.get(4).toString(),
                "<child xmlns=\"foo\">A</child><child xmlns=\"foo\" xmlns:ns=\"foo\">B</child>" +
                        "<child2 xmlns=\"foo\">D</child2>");
    }

    @Test
    public void testXMLElementAccessNavigationAccessComposition() {
        BArray returns = (BArray) BRunUtil.invoke(navigation, "testXMLElementAccessNavigationAccessComposition");
        Assert.assertEquals(returns.get(0).toString(),
                "<lname>Gunae</lname><lname>Jayee</lname><lname>Kumarae</lname>");
        Assert.assertEquals(returns.get(1).toString(), "<fname>Kamal</fname><lname>Gunae</lname><fname>Nimal</fname>" +
                "<lname>Jayee</lname><fname>Sunil</fname><lname>Kumarae</lname>");
        Assert.assertEquals(returns.get(2).toString(),
                "<lname>Gunae</lname><lname>Jayee</lname><lname>Kumarae</lname>");
        Assert.assertEquals(returns.get(3).toString(), "<fname>Kamal</fname><fname>Nimal</fname><fname>Sunil</fname>");
        Assert.assertEquals(returns.get(4).toString(), "KamalGunaeNimalJayeeSunilKumarae");
        Assert.assertEquals(returns.get(5).toString(), "<fname>Kamal</fname><fname>Nimal</fname><fname>Sunil</fname>");
        Assert.assertEquals(returns.get(6).toString(), "<fname>Kamal</fname><fname>Nimal</fname><fname>Sunil</fname>");
    }

    @Test
    public void testXMLNavigationExpressionWithQuotedIdentifiers() {
        BArray returns = (BArray) BRunUtil.invoke(navigation, "testXMLNavigationExpressionWithQuotedIdentifiers");
        Assert.assertEquals(returns.get(0).toString(),
                "<object xmlns=\"http://www.force.com/2009/06/asyncapi/dataload\">Account</object>");
        Assert.assertEquals(returns.get(1).toString(),
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
    public void testXMLNavigationWithEscapeCharacter() {
        BRunUtil.invoke(navigation, "testXMLNavigationWithEscapeCharacter");
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
        BArray returns = (BArray) BRunUtil.invoke(result, "testLengthOfXMLSequence");
        Assert.assertEquals(returns.get(0), 1L);
        Assert.assertEquals(returns.get(1), 3L);
        Assert.assertEquals(returns.get(2), 1L);
        Assert.assertEquals(returns.get(3), 2L);
    }

    @Test
    public void testInvalidXMLUnionAccessWithNegativeIndex() {
        BRunUtil.invoke(result, "testInvalidXMLUnionAccessWithNegativeIndex");
    }

    @Test
    public void testInvalidXMLAccessWithNegativeIndex() {
        BRunUtil.invoke(result, "testInvalidXMLAccessWithNegativeIndex");
    }

    @Test
    public void testXmlAccessWithLargerIndex() {
        BRunUtil.invoke(result, "testXmlAccessWithLargerIndex");
    }

    @Test
    public void testXmlIndexedAccessWithUnionType() {
        BRunUtil.invoke(result, "testXmlIndexedAccessWithUnionType");
    }

    @Test(enabled = false) // disabling until providing semantic support for step extension
    public void testXmlNavigationWithUnionType() {
        BRunUtil.invoke(navigation, "testXmlNavigationWithUnionType");
    }

    @Test
    public void testXmlNavigationWithDefaultNamespaceDefinedAfter() {
        BRunUtil.invoke(navigation, "testXmlNavigationWithDefaultNamespaceDefinedAfter");
    }

    @Test(dataProvider = "xmlStepExtension")
    public void testXmlStepExtension(String function) {
        BRunUtil.invoke(navigation, function);
    }

    @DataProvider
    private Object[] xmlStepExtension() {
        return new Object[]{
                "testXmlIndexedStepExtend",
                "testXmlFilterStepExtend",
                "testXmlIndexedAndFilterStepExtend",
                "testXmlMethodCallStepExtend",
                "testXmlMethodCallIndexedAndFilterStepExtend"
        };
    }

    @Test
    public void testXMLNavExpressionNegative() {
        int i = 0;
        BAssertUtil.validateError(navigationNegative, i++, "undefined symbol 'j'", 4, 14);
        BAssertUtil.validateError(navigationNegative, i++, "incompatible types: expected 'int', found 'string'", 5, 14);
        BAssertUtil.validateError(navigationNegative, i++, "cannot find xml namespace prefix 'ns'", 6, 15);
        BAssertUtil.validateError(navigationNegative, i++, "undefined symbol 'x2'", 7, 9);
        BAssertUtil.validateError(navigationNegative, i++, "undefined symbol 'x2'", 8, 9);
        BAssertUtil.validateError(navigationNegative, i++, "undefined symbol 'j'", 8, 18);
        BAssertUtil.validateError(navigationNegative, i++, "incompatible types: expected 'int', found 'string'", 17,
                18);
        BAssertUtil.validateError(navigationNegative, i++, "too many arguments in call to 'get()'", 18, 13);
        BAssertUtil.validateError(navigationNegative, i++, "incompatible types: expected 'int', found 'string'", 19,
                18);
        BAssertUtil.validateError(navigationNegative, i++, "undefined function 'foo' in type 'xml'", 21, 14);
        BAssertUtil.validateError(navigationNegative, i++, "incompatible types: expected 'xml', found 'int'", 22, 13);
        BAssertUtil.validateError(navigationNegative, i++, "incompatible types: expected 'int', found 'string'", 23,
                23);
        BAssertUtil.validateError(navigationNegative, i++, "incompatible types: expected 'xml', found 'int'", 25, 22);
        BAssertUtil.validateError(navigationNegative, i++, "undefined symbol 'r'", 29, 23);
        BAssertUtil.validateError(navigationNegative, i++,
                "incompatible types: expected 'boolean', found 'xml:Element'", 31, 31);
        BAssertUtil.validateError(navigationNegative, i++,
                "incompatible types: expected 'xml:ProcessingInstruction', found 'xml:Element'", 33, 60);
        BAssertUtil.validateError(navigationNegative, i++, "incompatible types: expected 'xml', found '()'", 34, 18);
        BAssertUtil.validateError(navigationNegative, i++, "undefined symbol 'r'", 36, 26);
        BAssertUtil.validateError(navigationNegative, i++,
                "incompatible types: expected 'boolean', found 'xml:Element'", 38, 34);
        BAssertUtil.validateError(navigationNegative, i++,
                "incompatible types: expected 'function (ballerina/lang.xml:0.0.0:ItemType) returns (boolean)', found" +
                        " 'function (xml) returns ()'",
                39, 29);

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

    @Test
    void testXMLFilterExpressionsNegative() {
        int index = 0;
        BAssertUtil.validateError(navigationFilterNegative, index++,
                "incompatible types: expected 'xml', found 'any'", 6, 14);
        BAssertUtil.validateError(navigationFilterNegative, index++,
                "incompatible types: expected 'xml', found 'int'", 8, 14);
        BAssertUtil.validateError(navigationFilterNegative, index++,
                "cannot find xml namespace prefix 'foo'", 13, 16);
        Assert.assertEquals(navigationFilterNegative.getErrorCount(), index);
    }

    @Test
    void testXmlStepExprWithUnionTypeNegative() {
        CompileResult navigationStepExprNegative =
                BCompileUtil.compile("test-src/types/xml/xml_step_expr_negative.bal");
        int index = 0;
        BAssertUtil.validateError(navigationStepExprNegative, index++,
                "incompatible types: expected 'xml', found '(xml<xml:Element>|int)'", 23, 9);
        BAssertUtil.validateError(navigationStepExprNegative, index++,
                "incompatible types: expected 'xml', found '(xml<xml:Element>|int)'", 24, 9);
        BAssertUtil.validateError(navigationStepExprNegative, index++,
                "incompatible types: expected 'xml', found '(xml:Comment|string)'", 27, 9);
        BAssertUtil.validateError(navigationStepExprNegative, index++,
                "incompatible types: expected 'xml', found '(xml:Comment|string)'", 28, 9);
        BAssertUtil.validateError(navigationStepExprNegative, index++,
                "incompatible types: expected 'xml', found '(xml:Element|xml:ProcessingInstruction|record {| xml x; " +
                        "|})'", 31, 9);
        BAssertUtil.validateError(navigationStepExprNegative, index++,
                "incompatible types: expected 'xml', found '(xml:Element|xml:ProcessingInstruction|record {| xml x; " +
                        "|})'", 32, 9);
        BAssertUtil.validateError(navigationStepExprNegative, index++,
                "incompatible types: expected 'xml', found '(xml:Text|xml:ProcessingInstruction|boolean)'", 35, 9);
        BAssertUtil.validateError(navigationStepExprNegative, index++,
                "incompatible types: expected 'xml', found '(xml:Text|xml:ProcessingInstruction|boolean)'", 36, 9);
        BAssertUtil.validateError(navigationStepExprNegative, index++,
                "incompatible types: expected 'xml', found 'XCE'", 39, 9);
        BAssertUtil.validateError(navigationStepExprNegative, index++,
                "incompatible types: expected 'xml', found 'XCE'", 40, 9);
        BAssertUtil.validateError(navigationStepExprNegative, index++,
                "incompatible types: expected 'xml', found '" +
                        "(xml<xml:Element>|xml<xml:Comment>|xml<xml:ProcessingInstruction>|xml:Text|int)'", 43, 9);
        BAssertUtil.validateError(navigationStepExprNegative, index++,
                "incompatible types: expected 'xml', found '" +
                        "(xml<xml:Element>|xml<xml:Comment>|xml<xml:ProcessingInstruction>|xml:Text|int)'", 44, 9);
        Assert.assertEquals(navigationStepExprNegative.getErrorCount(), index);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        elementAccess = null;
        navigation = null;
        negativeResult = null;
        navigationNegative = null;
        navigationFilterNegative = null;
    }
}
