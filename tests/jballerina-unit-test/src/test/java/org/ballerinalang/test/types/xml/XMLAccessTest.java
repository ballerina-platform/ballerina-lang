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

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for XML index-based and field-based access.
 *
 * @since 0.94.0
 */
@Test (groups = "brokenOnXMLLangLibChange")
public class XMLAccessTest {

    CompileResult result;
    CompileResult elementAccess;
    CompileResult navigation;
    CompileResult negativeResult;
    CompileResult navigationNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/xml/xml-indexed-access.bal");
        elementAccess = BCompileUtil.compile("test-src/types/xml/xml-element-access.bal");
        navigation = BCompileUtil.compile("test-src/types/xml/xml-navigation-access.bal");
        negativeResult = BCompileUtil.compile("test-src/types/xml/xml-indexed-access-negative.bal");
        navigationNegative = BCompileUtil.compile("test-src/types/xml/xml-nav-access-negative.bal");
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
                "<child xmlns=\"foo\">A</child><ns:child xmlns:ns=\"foo\" xmlns=\"foo\">B</ns:child>");
        Assert.assertEquals(returns[1].stringValue(),
                "<child xmlns=\"foo\">A</child><ns:child xmlns:ns=\"foo\" xmlns=\"foo\">B</ns:child>" +
                        "<k:child xmlns:k=\"bar\" xmlns=\"foo\">C</k:child><it-child xmlns=\"foo\">D</it-child>TEXT");
        Assert.assertEquals(returns[2].stringValue(),
                "<child xmlns=\"foo\">A</child>" +
                        "<ns:child xmlns:ns=\"foo\" xmlns=\"foo\">B</ns:child>" +
                        "<it-child xmlns=\"foo\">D</it-child>");
        Assert.assertEquals(returns[3].stringValue(),
                "<child xmlns=\"foo\">A</child><ns:child xmlns:ns=\"foo\" xmlns=\"foo\">B</ns:child>");
        Assert.assertEquals(returns[4].stringValue(),
                "<child xmlns=\"foo\">A</child><ns:child xmlns:ns=\"foo\" xmlns=\"foo\">B</ns:child>");
    }

    @Test
    public void testXMLNavigationOnSequenceWithNamespacesAndMultipleFilters() {
        BValue[] returns = BRunUtil.invoke(navigation, "testXMLNavigationOnSequenceWithNamespacesAndMultipleFilters");
        Assert.assertEquals(returns[0].stringValue(),
                "<child xmlns=\"foo\">A</child>" +
                        "<ns:child xmlns:ns=\"foo\" xmlns=\"foo\">B</ns:child>" +
                        "<child2 xmlns=\"foo\">D</child2>");
        Assert.assertEquals(returns[2].stringValue(),
                "<child xmlns=\"foo\">A</child>" +
                        "<ns:child xmlns:ns=\"foo\" xmlns=\"foo\">B</ns:child><child2 xmlns=\"foo\">D</child2>");
        Assert.assertEquals(returns[3].stringValue(),
                "<child xmlns=\"foo\">A</child><ns:child xmlns:ns=\"foo\" xmlns=\"foo\">B</ns:child>" +
                        "<k:child xmlns:k=\"bar\" xmlns=\"foo\">C</k:child><child2 xmlns=\"foo\">D</child2>");
        Assert.assertEquals(returns[4].stringValue(),
                "<child xmlns=\"foo\">A</child>" +
                        "<ns:child xmlns:ns=\"foo\" xmlns=\"foo\">B</ns:child>" +
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
    public void testInvalidXMLAccessWithIndex() {
        BAssertUtil.validateError(negativeResult, 0, "cannot update an xml sequence", 5, 5);

        BAssertUtil.validateError(negativeResult, 1, "cannot update an xml sequence", 13, 5);
    }

    @Test
    public void testXMLAccessWithIndex() {
        BValue[] returns = BRunUtil.invoke(result, "testXMLAccessWithIndex");
        Assert.assertTrue(returns[0] instanceof BXML);
        Assert.assertEquals(returns[0].stringValue(),
                "<root><!-- comment node--><name>supun</name><city>colombo</city></root>");

        Assert.assertTrue(returns[1] instanceof BXML);
        Assert.assertEquals(returns[1].stringValue(), "<!-- comment node-->");

        Assert.assertTrue(returns[2] instanceof BXML);
        Assert.assertEquals(returns[2].stringValue(), "<name>supun</name>");
    }
    
    @Test (groups = "brokenOnXMLLangLibChange", expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = ".*index out of range: index: 1, size: 1.*")
    public void testXMLAccessWithOutOfIndex() {
        BRunUtil.invoke(result, "testXMLAccessWithOutOfIndex");
    }

    @Test (groups = "brokenOnXMLLangLibChange", expectedExceptions = { BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = ".*error: \\{ballerina/lang.xml\\}XMLOperationError " +
                    "message=IndexOutOfRange Index: 5, Size: 3.*")
    public void testXMLSequenceAccessWithOutOfIndex() {
        BRunUtil.invoke(result, "testXMLSequenceAccessWithOutOfIndex");
    }

    @Test
    public void testLengthOfXMLSequence() {
        BValue[] returns = BRunUtil.invoke(result, "testLengthOfXMLSequence");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 3);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 1);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 2);
    }

    //@Test() // x3.* operation is no longer there in new xml proposal. Hence rather than fixing
    //// disabling this test case for now, later we can re-write this
    //public void testFieldBasedAccess() {
    //    BValue[] returns = BRunUtil.invoke(result, "testFieldBasedAccess");
    //    Assert.assertEquals(returns[0].stringValue(),
    //            "<fname><foo>1</foo><bar>2</bar></fname><lname1><foo>3</foo><bar>4</bar></lname1><fname><foo>5</foo>"
    //            +
    //                    "<bar>6</bar></fname><lname2><foo>7</foo><bar>8</bar></lname2>apple");
    //    Assert.assertEquals(returns[1].stringValue(), "<fname><foo>1</foo><bar>2</bar></fname>");
    //    Assert.assertEquals(returns[2].stringValue(), "<foo>5</foo>");
    //    Assert.assertEquals(returns[3].stringValue(), "<foo>5</foo>");
    //    Assert.assertEquals(returns[4].stringValue(), "<bar>4</bar>");
    //    Assert.assertEquals(returns[5].stringValue(),
    //            "<foo>1</foo><bar>2</bar><foo>3</foo><bar>4</bar><foo>5</foo><bar>6</bar><foo>7</foo><bar>8</bar>");
    //}

    @Test (groups = { "brokenOnSpecDeviation" })
    public void testFieldBasedAccessWithNamespaces() {
        BValue[] returns = BRunUtil.invoke(result, "testFieldBasedAccessWithNamespaces");
        Assert.assertEquals(returns[0].stringValue(),
                "<ns0:fname xmlns:ns0=\"http://test.com\" xmlns=\"http://test.com/default\">John</ns0:fname>");
        Assert.assertEquals(returns[1].stringValue(),
                "<ns0:fname xmlns:ns0=\"http://test.com\" xmlns=\"http://test.com/default\">John</ns0:fname>");
        Assert.assertTrue(((BXML<?>) returns[2]).isEmpty().booleanValue());
        Assert.assertEquals(returns[3].stringValue(),
                "<ns0:fname xmlns:ns0=\"http://test.com\" xmlns=\"http://test.com/default\">John</ns0:fname>");
    }

    @Test
    public void testXMLNavExpressionMethodInvocationNegative() {
        String message = "method invocations are not yet supported within XML navigation expressions, " +
                "use a grouping expression (parenthesis) " +
                "if you intend to invoke the method on the result of the navigation expression.";
        Assert.assertEquals(navigationNegative.getErrorCount(), 5);
        BAssertUtil.validateError(navigationNegative, 0, message, 3, 14);
        BAssertUtil.validateError(navigationNegative, 1, message, 4, 14);
        BAssertUtil.validateError(navigationNegative, 2, message, 5, 14);
        BAssertUtil.validateError(navigationNegative, 3, message, 6, 14);
        BAssertUtil.validateError(navigationNegative, 4, message, 7, 14);
    }
}
