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


import org.ballerinalang.model.util.XMLNodeType;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * This class tests xml lang module functionality.
 *
 * @since 1.0
 */
public class LangLibXMLTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/xmllib_test.bal");
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
        assertTrue(((BBoolean) returns[1]).booleanValue());
        assertFalse(((BBoolean) returns[2]).booleanValue());
    }

    @Test
    public void testXmlIsComment() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testXmlIsComment");
        assertTrue(((BBoolean) returns[0]).booleanValue());
        assertTrue(((BBoolean) returns[1]).booleanValue());
        assertFalse(((BBoolean) returns[2]).booleanValue());
    }

    @Test
    public void testXmlIsText() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testXmlIsText");
        assertTrue(((BBoolean) returns[0]).booleanValue());
        assertTrue(((BBoolean) returns[1]).booleanValue());
        assertFalse(((BBoolean) returns[2]).booleanValue());
    }

    @Test
    public void testGetNameOfElement() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getNameOfElement");
        assertEquals(((BString) returns[0]).stringValue(), "elem");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: lang.xml function 'getName' only accept xml type 'element'.*")
    public void testGetNameOfElementNegative() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getNameOfElementNegative");
    }

    @Test
    public void testSetElementName() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetElementName");
        assertEquals(((BXML) returns[0]).stringValue(), "<el2 attr=\"attr1\">content</el2>");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: lang.xml function 'setName' only accept xml type 'element'.*")
    public void testSetElementNameNegative() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetElementNameNegative");
    }

    @Test
    public void testGetChildren() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetChildren");
        assertEquals((returns[0]).stringValue(), "<TITLE>Empire Burlesque</TITLE><ARTIST>Bob Dylan</ARTIST>");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: lang.xml function 'getChildren' only accept xml type 'element'.*")
    public void testGetChildrenNegative() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetChildrenNegative");
    }

    @Test
    public void testSetChildren() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetChildren");
        assertEquals((returns[0]).stringValue(), "<CD><e>child</e></CD>");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: lang.xml function 'setChildren' only accept xml type 'element'.*")
    public void testSetChildrenNegative() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSetChildrenNegative");
    }

    @Test
    public void testGetAttributes() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetAttributes");
        assertEquals((returns[0]).stringValue(), "{\"attr\":\"attr1\", \"attr2\":\"attr2\"}");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp =
                    "error: lang.xml function 'getAttributes' only accept xml type 'element'.*")
    public void testGetAttributesNegative() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetAttributesNegative");
    }

    @Test
    public void testGetTarget() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetTarget");
        assertEquals((returns[0]).stringValue(), "xml-stylesheet");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: lang.xml function 'getTarget' only accept xml type " +
                    "'processing instruction'.*")
    public void testGetTargetNegative() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetTargetNegative");
    }

    @Test
    public void testGetContent() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetContent");
        assertEquals((returns[0]).stringValue(), "hello world");
        assertEquals((returns[1]).stringValue(), "type=\"cont\"");
        assertEquals((returns[2]).stringValue(), " this is a comment text ");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: lang.xml function 'getContent' only accept xml type " +
                    "'text\\|processing instruction\\|comment'.*")
    public void testGetContentNegative() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetContentNegative");
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
    public void testCopyFuncOnComment() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCopingComment");
        Assert.assertEquals(((BXMLItem) returns[0]).getNodeType(),  XMLNodeType.COMMENT);
        Assert.assertEquals((returns[0]).stringValue(), "<!--some comment-->");
    }

    @Test
    public void testForEach() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testForEach");
        Assert.assertEquals((returns[0]).size(), 3);
    }

    @Test
    public void testSlice() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSlice");
        Assert.assertEquals(returns[0].stringValue(), "<elemL>content</elemL><elemN>content</elemN>");
        Assert.assertEquals(returns[1].stringValue(), "<elemN>content</elemN><elemM>content</elemM>");
        Assert.assertEquals(returns[2].stringValue(), "<elemN>content</elemN><elemM>content</elemM>");
    }

    @Test
    public void testXMLCycleError() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testXMLCycleError");
        Assert.assertEquals(returns[0].stringValue(),
                "{ballerina/lang.xml}XMLOperationError " +
                        "{message:\"Failed to set children to xml element: Cycle detected\"}");
        Assert.assertTrue(returns[1].stringValue().contains("<CD><CD>"));
        Assert.assertTrue(returns[1].stringValue().contains("</CD></CD>"));
    }

    @Test
    public void testXMLCycleDueToChildrenOfChildren() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testXMLCycleDueToChildrenOfChildren");
        Assert.assertEquals(returns[0].stringValue(),
                "{ballerina/lang.xml}XMLOperationError " +
                        "{message:\"Failed to set children to xml element: Cycle detected\"}");
    }
}
