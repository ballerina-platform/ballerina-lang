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

import io.ballerina.runtime.api.types.XmlNodeType;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.JvmRunUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
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
    public void testLength(BXml val, long expectedLength) {
        Object returns = JvmRunUtil.invoke(compileResult, "testLength", new Object[]{val});
        assertEquals((returns), expectedLength);
    }

    @DataProvider(name = "XMLDataProvider")
    public Object[][] getXML() {
        Object returns = JvmRunUtil.invoke(compileResult, "getXML");
        BArray result = (BArray) returns;
        return new Object[][]{
                {result.get(0), 1},
                {result.get(1), 1},
                {result.get(2), 1}
        };
    }

    @Test
    public void testXMLIteratorInvocation() {
        JvmRunUtil.invoke(compileResult, "testXMLIteratorInvocation");
    }

    @Test
    public void testFromXml() {
        Object returns = JvmRunUtil.invoke(compileResult, "testFromString");
        assertEquals(returns.toString(),
                "<TITLE>Empire Burlesque</TITLE><TITLE>Hide your heart</TITLE><TITLE>Greatest Hits</TITLE>");
    }

    @Test
    public void testEmptyConcatCall() {
        Object returns = JvmRunUtil.invoke(compileResult, "emptyConcatCall");
        assertTrue(((BXml) returns).getNodeType() == XmlNodeType.SEQUENCE);
        assertEquals(((BXml) returns).size(), 0);
    }

    @Test
    public void testConcat() {
        Object returns = JvmRunUtil.invoke(compileResult, "testConcat");
        assertTrue(((BXml) returns).getNodeType() == XmlNodeType.SEQUENCE);
        assertEquals(((BXml) returns).size(), 5);
        assertEquals(returns.toString(),
                "<hello>xml content</hello><TITLE>Empire Burlesque</TITLE><TITLE>Hide your heart</TITLE>" +
                        "<TITLE>Greatest Hits</TITLE>hello from String");
    }

    @Test
    public void testConcatWithXMLSequence() {
        JvmRunUtil.invoke(compileResult, "testConcatWithXMLSequence");
    }

    @Test
    public void testIsElement() {
        Object returns = JvmRunUtil.invoke(compileResult, "testIsElement");
        BArray result = (BArray) returns;
        assertFalse(result.getBoolean(0));
        assertTrue(result.getBoolean(1));
        assertFalse(result.getBoolean(2));
    }

    @Test
    public void testXmlPI() {
        Object returns = JvmRunUtil.invoke(compileResult, "testXmlPI");
        BArray result = (BArray) returns;
        assertTrue(result.getBoolean(0));
        assertFalse(result.getBoolean(1));
    }

    @Test
    public void testXmlIsComment() {
        Object returns = JvmRunUtil.invoke(compileResult, "testXmlIsComment");
        BArray result = (BArray) returns;
        assertTrue(result.getBoolean(0));
        assertFalse(result.getBoolean(1));
    }

    @Test
    public void testXmlIsText() {
        Object returns = JvmRunUtil.invoke(compileResult, "testXmlIsText");
        BArray result = (BArray) returns;
        assertTrue(result.getBoolean(0));
        assertTrue(result.getBoolean(1));
    }

    @Test
    public void testGetNameOfElement() {
        Object returns = JvmRunUtil.invoke(compileResult, "getNameOfElement");
        assertEquals(returns.toString(), "elem");
    }

    @Test
    public void testSetElementName() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSetElementName");
        BArray results = (BArray) returns;
        assertEquals(results.get(0).toString(), "<el2 attr=\"attr1\">content</el2>");
        assertEquals(results.get(1).toString(), "<Elem xmlns=\"http://www.ballerina-schema.io/schema\"/>");
    }

    @Test
    public void testGetChildren() {
        Object returns = JvmRunUtil.invoke(compileResult, "testGetChildren");
        assertEquals(returns.toString(), "<TITLE>Empire Burlesque</TITLE><ARTIST>Bob Dylan</ARTIST>");
    }

    @Test
    public void testSetChildren() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSetChildren");
        assertEquals(returns.toString(), "<CD><e>child</e></CD>");
    }

    @Test
    public void testGetAttributes() {
        Object returns = JvmRunUtil.invoke(compileResult, "testGetAttributes");
        assertEquals(returns.toString(), "{\"attr\":\"attr1\",\"attr2\":\"attr2\"}");
    }

    @Test
    public void testGetTarget() {
        Object returns = JvmRunUtil.invoke(compileResult, "testGetTarget");
        assertEquals(returns.toString(), "xml-stylesheet");
    }

    @Test
    public void testGetContent() {
        Object returns = JvmRunUtil.invoke(compileResult, "testGetContent");
        BArray results = (BArray) returns;
        assertEquals(results.get(0).toString(), "type=\"cont\"");
        assertEquals(results.get(1).toString(), " this is a comment text ");
    }

    @Test
    public void xmlGetContentOverACommentSequence() {
        JvmRunUtil.invoke(compileResult, "testXmlGetContentOverACommentSequence");
    }

    @Test
    public void xmlGetContentOverAProcessingInstructionSequence() {
        JvmRunUtil.invoke(compileResult, "testXmlGetContentOverAProcInstructionSequence");
    }

    @Test
    public void testCreateElement() {
        JvmRunUtil.invoke(compileResult, "testCreateElement");
    }

    @Test
    public void testCreateProcessingInstruction() {
        Object returns = JvmRunUtil.invoke(compileResult, "testCreateProcessingInstruction");
        assertEquals(returns.toString(), "<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>");
    }

    @Test
    public void testCreateComment() {
        Object returns = JvmRunUtil.invoke(compileResult, "testCreateComment");
        assertEquals(returns.toString(), "<!--This text should be wraped in xml comment-->");
    }

    @Test
    public void testCreateText() {
        JvmRunUtil.invoke(compileResult, "testCreateText");
    }

    @Test
    public void testForEach() {
        JvmRunUtil.invoke(compileResult, "testForEach");
    }

    @Test
    public void testSlice() {
        Object returns = JvmRunUtil.invoke(compileResult, "testSlice");
        BArray results = (BArray) returns;
        assertEquals(results.get(0).toString(), "<elemL>content</elemL><elemN>content</elemN>");
        assertEquals(results.get(1).toString(), "<elemN>content</elemN><elemM>content</elemM>");
        assertEquals(results.get(2).toString(), "<elemN>content</elemN><elemM>content</elemM>");
    }

    @Test
    public void testXMLCycleError() {
        Object returns = JvmRunUtil.invoke(compileResult, "testXMLCycleError");
        BArray results = (BArray) returns;
        assertEquals(results.get(0).toString(),
                "error(\"{ballerina/lang.xml}XMLOperationError\",message=\"Failed to set children to xml element: " +
                        "Cycle detected\")");
        assertTrue(results.get(1).toString().contains("<CD><CD>"));
        assertTrue(results.get(1).toString().contains("</CD></CD>"));
    }

    @Test
    public void testXMLCycleDueToChildrenOfChildren() {
        Object returns = JvmRunUtil.invoke(compileResult, "testXMLCycleDueToChildrenOfChildren");
        assertEquals(returns.toString(), "error(\"{ballerina/lang.xml}XMLOperationError\",message=\"Failed to set " +
                "children to xml element: Cycle detected\")");
    }

    @Test
    public void testXMLStrip() {
        JvmRunUtil.invoke(compileResult, "testXMLStrip");
    }

    @Test
    public void testGet() {
        Object returns = JvmRunUtil.invoke(compileResult, "testGet");
        assertEquals(returns.toString(),
                "[`<elem/>`,error(\"xml sequence index out of range. Length: '1' requested: '3'\"),`<!--Comment " +
                        "content-->`,`<?PITarget VAL-0?>`,error(\"xml sequence index out of range. Length: '3' " +
                        "requested: '-1'\")]");
    }

    @Test
    public void testAsyncFpArgsWithXmls() {
        Object results = JvmRunUtil.invoke(compileResult, "testAsyncFpArgsWithXmls");
        BArray arr = (BArray) results;
        assertTrue(arr.get(0) instanceof Long);
        assertTrue(arr.get(1) instanceof BXml);
        assertEquals(arr.get(0), 6021L);
        BXml bxml = (BXml) arr.get(1);
        assertEquals(bxml.getItem(0).children().getItem(1).getTextValue(), "Harry Potter");
        assertEquals(bxml.getItem(1).children().getItem(1).getTextValue(), "Learning XML");
    }

    public void testChildren() {
        JvmRunUtil.invoke(compileResult, "testChildren");
    }

    @Test
    public void testElements() {
        JvmRunUtil.invoke(compileResult, "testElements");
    }

    @Test
    public void testElementsNS()  {
        JvmRunUtil.invoke(compileResult, "testElementsNS");
    }

    @Test
    public void testElementChildren() {
        JvmRunUtil.invoke(compileResult, "testElementChildren");
    }

    @Test
    public void testElementChildrenNS()  {
        JvmRunUtil.invoke(compileResult, "testElementChildrenNS");
    }

    @Test
    public void testSelectingTextFromXml() {
        JvmRunUtil.invoke(compileResult, "testSelectingTextFromXml");
    }

    @Test
    public void testXmlFilter() {
        JvmRunUtil.invoke(compileResult, "testXmlFilter");
    }

    @Test
    public void testGetDescendants() {
        JvmRunUtil.invoke(compileResult, "testGetDescendants");
    }

    @Test
    public void testData() {
        JvmRunUtil.invoke(compileResult, "testData");
    }

    @Test
    public void fromStringTest() {
        JvmRunUtil.invoke(compileResult, "fromStringTest");
    }

    @Test
    public void testXmlSubtypeFillerValue() {
        JvmRunUtil.invoke(compileResult, "testXmlSubtypeFillerValue");
    }

    @Test
    public void testXmlIteratorNextValue() {
        JvmRunUtil.invoke(compileResult, "testXmlIteratorNextInvocations");
    }

    @Test
    public void testNamespaces() {
        JvmRunUtil.invoke(compileResult, "testNamespaces");
    }

    @Test
    public void testSetChildrenFunction() {
        JvmRunUtil.invoke(compileResult, "testSetChildrenFunction");
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
                "'(xml:ProcessingInstruction|xml:Comment)', found 'xml:Element'", 61, 12);
        validateError(negativeResult, i++, "incompatible types: expected 'xml:Element', found 'xml'", 69, 13);
        validateError(negativeResult, i++, "incompatible types: expected 'xml<xml:Element>', found 'xml'",
                75, 28);
        validateError(negativeResult, i++, "incompatible types: expected 'map<string>', " +
                        "found 'record {| string x; anydata...; |}'",
                95, 49);
        validateError(negativeResult, i++, "incompatible types: expected 'map<string>', found 'attributesRecord'",
                96, 49);
        validateError(negativeResult, i++, "incompatible types: expected 'xml', found 'string'",
                97, 62);
        validateError(negativeResult, i++, "incompatible types: expected 'string', found 'xml:Element'",
                98, 41);
        assertEquals(negativeResult.getErrorCount(), i);
    }

    @Test(dataProvider = "ConstraintTestFunctionList")
    public void testXMLConstrained(String functionName) {
        JvmRunUtil.invoke(constrainedTest, functionName);
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
                    "'xml\\<\\(lang\\.xml:Element\\|lang\\.xml:Comment\\|lang\\.xml:ProcessingInstruction" +
                    "\\|lang\\.xml:Text\\)\\>' cannot be cast to 'xml\\<lang\\.xml:Comment\\>.*")
    public void xmlConstraintRuntimeCastInvalid() {
        JvmRunUtil.invoke(constrainedTest, "xmlConstraintRuntimeCastInvalid");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: " +
                    "'xml\\<\\(lang\\.xml:Element\\|lang\\.xml:Comment\\|lang\\.xml:ProcessingInstruction" +
                    "\\|lang\\.xml:Text\\)\\>' cannot be cast to 'xml\\<\\(lang\\.xml:Element\\|lang\\.xml:Text\\)" +
                    "\\>'.*")
    public void xmlConstraintRuntimeCastUnionInvalid() {
        JvmRunUtil.invoke(constrainedTest, "xmlConstraintRuntimeCastUnionInvalid");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: " +
                    "'lang\\.xml:Comment' cannot be cast to 'xml\\<lang\\.xml:Element\\>'.*")
    public void xmlElementToConstraintClassInvalid() {
        JvmRunUtil.invoke(constrainedTest, "xmlElementToConstraintClassInvalid");
    }

    @Test
    public void testNegativeConstraint() {
        constraintNegative = BCompileUtil.compile("test-src/xmllib_constrained_negative_test.bal");
        int i = 0;
        validateError(constraintNegative, i++, "incompatible types: expected 'xml:Comment', found 'xml:Element'",
                20, 23);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml:ProcessingInstruction', " +
                "found 'xml:Element'", 21, 37);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml<xml:Comment>', found 'xml:Element'",
                25, 28);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml<xml:ProcessingInstruction>'," +
                " found 'xml:Element'", 26, 42);
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
                45, 26);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml:Element'," +
                " found 'xml:ProcessingInstruction'", 46, 13);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml:Element', found 'xml:Text'",
                47, 13);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml:Element', found 'xml<xml:Comment>'",
                50, 13);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml<xml:Comment>', found 'xml:Element'",
                52, 46);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml<xml:Comment>'," +
                " found 'xml<xml:Element>'", 55, 28);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml:Element', found 'xml:Comment'",
                60, 21);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml:Element', found 'xml:Comment'",
                62, 29);
        validateError(constraintNegative, i++, "incompatible types: expected 'xml:Element', found 'xml<xml:Comment>'",
                65, 19);
        assertEquals(constraintNegative.getErrorCount(), i);
    }
}
