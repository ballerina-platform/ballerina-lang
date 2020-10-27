/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.test;

import org.ballerinalang.core.model.util.XMLNodeType;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BXML;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * This class tests xml lang module functionality.
 *
 * @since 1.0
 */
public class LangLibXMLTest {

    private CompileResult compileResult, negativeResult, constrainedTest, constraintNegative;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/xmllib_test.bal");
        constrainedTest = BCompileUtil.compile("test-src/xmllib_constrained_test.bal");
        negativeResult = BCompileUtil.compile("test-src/xmllib_test_negative.bal");
    }

    @Test(dataProvider = "XMLDataProvider")
    public void testLength(BValue val, long expectedLength) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testLength", new BValue[]{val});
        assertEquals(((BInteger) returns[0]).intValue(), expectedLength);
    }

    @DataProvider(name = "XMLDataProvider")
    public Object[][] getXML() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getXML");
        return new Object[][]{
                {returns[0], 1},
                {returns[1], 1},
                {returns[2], 1}
        };
    }

    @Test
    public void testFromXml() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFromString");
        assertEquals(returns[0].stringValue(),
                "<TITLE>Empire Burlesque</TITLE><TITLE>Hide your heart</TITLE><TITLE>Greatest Hits</TITLE>");
    }

    @Test
    public void testEmptyConcatCall() {
        BValue[] returns = BRunUtil.invoke(compileResult, "emptyConcatCall");
        assertTrue(((BXML<?>) returns[0]).getNodeType() == XMLNodeType.SEQUENCE);
        assertEquals(((BXML<?>) returns[0]).size(), 0);
    }

    @Test
    public void testConcat() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConcat");
        assertTrue(((BXML<?>) returns[0]).getNodeType() == XMLNodeType.SEQUENCE);
        assertEquals(returns[0].size(), 5);
        assertEquals(returns[0].stringValue(),
                "<hello>xml content</hello><TITLE>Empire Burlesque</TITLE><TITLE>Hide your heart</TITLE>" +
                        "<TITLE>Greatest Hits</TITLE>hello from String");
    }

    @Test
    public void testIsElement() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIsElement");
        assertFalse(((BBoolean) returns[0]).booleanValue());
        assertTrue(((BBoolean) returns[1]).booleanValue());
        assertFalse(((BBoolean) returns[2]).booleanValue());
    }

    @Test
    public void testXmlPI() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testXmlPI");
        assertTrue(((BBoolean) returns[0]).booleanValue());
        assertFalse(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testXmlIsComment() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testXmlIsComment");
        assertTrue(((BBoolean) returns[0]).booleanValue());
        assertFalse(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testXmlIsText() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testXmlIsText");
        assertTrue(((BBoolean) returns[0]).booleanValue());
        assertTrue(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testGetNameOfElement() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getNameOfElement");
        assertEquals(returns[0].stringValue(), "elem");
    }

    @Test
    public void testSetElementName() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetElementName");
        assertEquals(((BXML) returns[0]).stringValue(), "<el2 attr=\"attr1\">content</el2>");
    }

    @Test
    public void testGetChildren() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetChildren");
        assertEquals((returns[0]).stringValue(), "<TITLE>Empire Burlesque</TITLE><ARTIST>Bob Dylan</ARTIST>");
    }

    @Test
    public void testSetChildren() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetChildren");
        assertEquals((returns[0]).stringValue(), "<CD><e>child</e></CD>");
    }

    @Test
    public void testGetAttributes() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetAttributes");
        assertEquals((returns[0]).stringValue(), "{\"attr\":\"attr1\", \"attr2\":\"attr2\"}");
    }

    @Test
    public void testGetTarget() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetTarget");
        assertEquals((returns[0]).stringValue(), "xml-stylesheet");
    }

    @Test
    public void testGetContent() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetContent");
        assertEquals((returns[0]).stringValue(), "hello world");
        assertEquals((returns[1]).stringValue(), "type=\"cont\"");
        assertEquals((returns[2]).stringValue(), " this is a comment text ");
    }

    @Test
    public void testCreateElement() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateElement");
        assertEquals((returns[0]).stringValue(), "<elem>hello world</elem>");
        assertEquals((returns[1]).stringValue(), "hello world");
        assertEquals((returns[2]).stringValue(), "");
    }

    @Test
    public void testCreateProcessingInstruction() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateProcessingInstruction");
        assertEquals((returns[0]).stringValue(), "<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>");
    }

    @Test
    public void testCreateComment() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCreateComment");
        assertEquals((returns[0]).stringValue(), "<!--This text should be wraped in xml comment-->");
    }

    @Test
    public void testCreateText() {
        BRunUtil.invoke(compileResult, "testCreateText");
    }

    @Test
    public void testForEach() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testForEach");
        assertEquals((returns[0]).size(), 3);
    }

    @Test
    public void testSlice() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSlice");
        assertEquals(returns[0].stringValue(), "<elemL>content</elemL><elemN>content</elemN>");
        assertEquals(returns[1].stringValue(), "<elemN>content</elemN><elemM>content</elemM>");
        assertEquals(returns[2].stringValue(), "<elemN>content</elemN><elemM>content</elemM>");
    }

    @Test
    public void testXMLCycleError() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testXMLCycleError");
        assertEquals(returns[0].stringValue(),
                "{ballerina/lang.xml}XMLOperationError " +
                        "{\"message\":\"Failed to set children to xml element: Cycle detected\"}");
        assertTrue(returns[1].stringValue().contains("<CD><CD>"));
        assertTrue(returns[1].stringValue().contains("</CD></CD>"));
    }

    @Test
    public void testXMLCycleDueToChildrenOfChildren() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testXMLCycleDueToChildrenOfChildren");
        assertEquals(returns[0].stringValue(),
                "{ballerina/lang.xml}XMLOperationError " +
                        "{\"message\":\"Failed to set children to xml element: Cycle detected\"}");
    }

    @Test
    public void testGet() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGet");
        assertEquals(returns[0].stringValue(), "<elem/>");
        assertEquals(returns[1].stringValue(), "xml sequence index out of range. Length: '1' requested: '3' {}");
        assertEquals(returns[2].stringValue(), "<!--Comment content-->");
        assertEquals(returns[3].stringValue(), "<?PITarget VAL-0?>");
        assertEquals(returns[4].stringValue(), "xml sequence index out of range. Length: '3' requested: '-1' {}");
    }

    @Test
    public void testAsyncFpArgsWithXmls() {
        BValue[] results = BRunUtil.invoke(compileResult, "testAsyncFpArgsWithXmls");
        assertTrue(results[0] instanceof BInteger);
        assertTrue(results[1] instanceof BXML);
        assertEquals(((BInteger) results[0]).intValue(), 6021);
        BXML bxml = (BXML) results[1];
        assertEquals(bxml.getItem(0).children().getItem(1).getTextValue().stringValue(), "Harry Potter");
        assertEquals(bxml.getItem(1).children().getItem(1).getTextValue().stringValue(), "Learning XML");
    }

    public void testChildren() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testChildren");
    }

    @Test
    public void testElements() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testElements");
    }

    @Test
    public void testElementsNS()  {
        BValue[] returns = BRunUtil.invoke(compileResult, "testElementsNS");
    }

    @Test
    public void testElementChildren() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testElementChildren");
    }

    @Test
    public void testElementChildrenNS()  {
        BValue[] returns = BRunUtil.invoke(compileResult, "testElementChildrenNS");
    }

    @Test
    public void testNegativeCases() {
        negativeResult = BCompileUtil.compile("test-src/xmllib_test_negative.bal");
        int i = 0;
        validateError(negativeResult, i++, "incompatible types: expected 'xml:Element', found 'xml'", 21, 12);
        validateError(negativeResult, i++, "incompatible types: expected 'xml:Element', found 'xml'", 28, 5);
        validateError(negativeResult, i++, "incompatible types: expected 'xml:Element', found 'xml'", 36, 13);
        validateError(negativeResult, i++, "incompatible types: expected 'xml:Element', found 'xml'", 44, 5);
        validateError(negativeResult, i++, "incompatible types: expected 'xml:Element', found 'xml'", 51, 12);
        validateError(negativeResult, i++, "incompatible types: expected 'xml:ProcessingInstruction', found 'xml'",
                56, 8);
        validateError(negativeResult, i++, "incompatible types: expected " +
                "'(xml:Text|xml:ProcessingInstruction|xml:Comment)', found 'xml:Element'", 61, 12);
        assertEquals(negativeResult.getErrorCount(), i);
    }

    @Test(dataProvider = "ConstraintTestFunctionList")
    public void testXMLConstrained(String functionName) {
        BValue[] returns = BRunUtil.invoke(constrainedTest, functionName);
    }

    @DataProvider(name = "ConstraintTestFunctionList")
    public Object[][] getTestFunctions() {
        return new Object[][]{
                {"basicXMLConstrainedType"},
                {"xmlConstraintMultipleElement"},
                {"xmlConstraintRuntimeCast"},
                {"xmlCastSingleElementAsConstrainedSequence"},
                {"xmlSubtypeArray"},
                {"xmlSubtypeArrayTwo"},
                {"xmlSubtypeMap"}
        };
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: " +
                    "'xml\\<lang\\.xml:Element\\|lang\\.xml:Comment\\|lang\\.xml:ProcessingInstruction" +
                    "\\|lang\\.xml:Text\\>' cannot be cast to 'xml\\<lang\\.xml:Comment\\>.*")
    public void xmlConstraintRuntimeCastInvalid() {
        BRunUtil.invoke(constrainedTest, "xmlConstraintRuntimeCastInvalid");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: " +
                    "'xml\\<lang\\.xml:Element\\|lang\\.xml:Comment\\|lang\\.xml:ProcessingInstruction" +
                    "\\|lang\\.xml:Text\\>' cannot be cast to 'xml\\<lang\\.xml:Element\\|lang\\.xml:Text\\>'.*")
    public void xmlConstraintRuntimeCastUnionInvalid() {
        BRunUtil.invoke(constrainedTest, "xmlConstraintRuntimeCastUnionInvalid");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: " +
                    "'lang\\.xml:Comment' cannot be cast to 'xml\\<lang\\.xml:Element\\>'.*")
    public void xmlElementToConstraintClassInvalid() {
        BRunUtil.invoke(constrainedTest, "xmlElementToConstraintClassInvalid");
    }

    @Test
    public void testNegativeConstraint() {
        constraintNegative = BCompileUtil.compile("test-src/xmllib_constrained_negative_test.bal");
        int i = 0;
        validateError(constraintNegative, i++, "incompatible types: expected 'xml:Comment', found 'xml:Element'",
                20, 28);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml:ProcessingInstruction', " +
                "found 'xml:Element'", 21, 42);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml<xml:Comment>', found 'xml:Element'",
                25, 33);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml<xml:ProcessingInstruction>'," +
                " found 'xml:Element'", 26, 47);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml:Comment', found 'xml<xml:Element>'",
                29, 26);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml<xml:Element>', found 'xml:Comment'",
                32, 41);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml<xml:Comment>'," +
                        " found 'xml<xml:Element>'",
                38, 41);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml<xml:Element>'," +
                " found 'xml<(xml:Element|xml:Comment)>'", 41, 29);

        validateError(constraintNegative, i++, "incompatible types: expected 'xml:Element', found 'xml:Comment'",
                45, 31);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml:Element'," +
                " found 'xml:ProcessingInstruction'", 46, 18);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml:Element', found 'xml:Text'",
                47, 18);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml:Element', found 'xml<xml:Comment>'",
                50, 13);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml<xml:Comment>', found 'xml:Element'",
                52, 51);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml<xml:Comment>'," +
                " found 'xml<xml:Element>'", 55, 28);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml:Element', found 'xml:Comment'",
                60, 26);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml:Element', found 'xml:Comment'",
                62, 34);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml:Element', found 'xml<xml:Comment>'",
                65, 19);
        assertEquals(constraintNegative.getErrorCount(), i);
    }
}
