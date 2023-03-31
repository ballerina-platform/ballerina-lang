/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.types.string;

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.JsonParser;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.utils.ByteArrayUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Test Native functions in ballerina.model.string.
 */
public class StringTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/string/string-test.bal");
    }

    @Test
    public void testBooleanValueOf() {
        Object[] args = {(true)};
        Object returns = BRunUtil.invoke(result, "booleanValueOf", args);
        Assert.assertTrue(returns instanceof BString);
        final String expected = "true";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test
    public void testFloatValueOf() {
        Object[] args = {(1.345f)};
        Object returns = BRunUtil.invoke(result, "floatValueOf", args);
        Assert.assertTrue(returns instanceof BString);
        final String expected = "1.345";
        Assert.assertEquals(returns.toString().substring(0, 5), expected);
    }

    @Test
    public void testHasPrefix() {
        Object[] args = {StringUtils.fromString("Expendables"), StringUtils.fromString("Ex")};
        Object results = BRunUtil.invoke(result, "hasPrefix", args);
        Assert.assertTrue((Boolean) results);
    }

    @Test
    public void testHasSuffix() {
        Object[] args = {StringUtils.fromString("One Two"), StringUtils.fromString("Two")};
        Object results = BRunUtil.invoke(result, "hasSuffix", args);
        Assert.assertTrue((Boolean) results);
    }

    @Test
    public void testIndexOf() {
        Object[] args = {StringUtils.fromString("Lion in the town"), StringUtils.fromString("in")};
        Object results = BRunUtil.invoke(result, "indexOf", args);
        Assert.assertEquals(results, 5L);
    }

    @Test
    public void testIndexOfAfterEmoji() {
        Object[] args = {StringUtils.fromString("Lion\uD83E\uDD81 in the town"), StringUtils.fromString("in")};
        Object results = BRunUtil.invoke(result, "indexOf", args);
        Assert.assertEquals(results, 6L);
    }

    @Test
    public void testIndexOfAtEmoji() {
        Object[] args =
                {StringUtils.fromString("Lion\uD83E\uDD81 in the town"), StringUtils.fromString("\uD83E\uDD81")};
        Object results = BRunUtil.invoke(result, "indexOf", args);
        Assert.assertEquals(results, 4L);
    }

    @Test
    public void testIndexOfBeforeEmoji() {
        Object[] args = {StringUtils.fromString("Lion\uD83E\uDD81 in the town"), StringUtils.fromString("Lion")};
        Object results = BRunUtil.invoke(result, "indexOf", args);
        Assert.assertEquals(results, 0L);
    }

    @Test
    public void testIntValueOf() {
        Object[] args = {(25)};
        Object returns = BRunUtil.invoke(result, "intValueOf", args);
        Assert.assertTrue(returns instanceof BString);
        final String expected = "25";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test
    public void testJsonValueOf() {
        Object[] args = {JsonParser.parse("{\"name\":\"chanaka\"}")};
        Object returns = BRunUtil.invoke(result, "jsonValueOf", args);
        Assert.assertTrue(returns instanceof BString);
        final String expected = "{\"name\":\"chanaka\"}";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test
    public void testLength() {
        Object[] args = {StringUtils.fromString("Bandwagon")};
        Object returns = BRunUtil.invoke(result, "lengthOfStr", args);
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 9L);
    }

    @Test
    public void testStringValueOf() {
        Object[] args = {StringUtils.fromString("This is a String")};
        Object returns = BRunUtil.invoke(result, "stringValueOf", args);
        Assert.assertTrue(returns instanceof BString);
        final String expected = "This is a String";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test
    public void testSubString() {
        Object[] args = {StringUtils.fromString("testValues"), (0), (9)};
        Object returns = BRunUtil.invoke(result, "substring", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "testValue");
    }

    @Test
    public void testSubStringAfterEmoji() {
        Object[] args = {StringUtils.fromString("test\uD83D\uDC87\uD83C\uDFFE\u200D♂️Values"), (0), (12)};
        Object returns = BRunUtil.invoke(result, "substring", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "test\uD83D\uDC87\uD83C\uDFFE\u200D♂️Val");
    }

    @Test
    public void testSubStringBeforeEmoji() {
        Object[] args = {StringUtils.fromString("test\uD83D\uDC69\uD83C\uDFFC\u200D⚖️️️Values"), (0), (4)};
        Object returns = BRunUtil.invoke(result, "substring", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "test");
    }

    @Test
    public void testSubStringAtEmoji() {
        Object[] args = {StringUtils.fromString("test\uD83D\uDC69\uD83C\uDFFE\u200D\uD83C\uDF7C️️Values"), (0),
                (8)};
        Object returns = BRunUtil.invoke(result, "substring", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "test\uD83D\uDC69\uD83C\uDFFE\u200D\uD83C\uDF7C");
    }

    @Test
    public void testToLowerCase() {
        Object[] args = {StringUtils.fromString("COMPANY")};
        Object returns = BRunUtil.invoke(result, "toLower", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "company");
    }

    @Test
    public void testToUpperCase() {
        Object[] args = {StringUtils.fromString("company")};
        Object returns = BRunUtil.invoke(result, "toUpper", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "COMPANY");
    }

    @Test
    public void testTrim() {
        Object[] args = {StringUtils.fromString(" This is a String ")};
        Object returns = BRunUtil.invoke(result, "trim", args);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "This is a String");
    }

    @Test
    public void testXmlValueOf() {
        BXml xmlArg = ValueCreator.createXmlValue("<test>name</test>");
        Object[] args = {xmlArg};
        Object returns = BRunUtil.invoke(result, "xmlValueOf", args);
        Assert.assertTrue(returns instanceof BString);
        final String expected = "<test>name</test>";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test
    public void testToByteArray() {
        String content = "Sample Ballerina Byte Array Content";
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        Object[] args = {StringUtils.fromString(content)};
        Object returns = BRunUtil.invoke(result, "toByteArray", args);
        BArray bByteArray = (BArray) returns;
        Assert.assertEquals(bByteArray.size(), bytes.length);
        ByteArrayUtils.assertJBytesWithBBytes(bytes, bByteArray.getBytes());
    }

    @Test
    public void testMultilineStringLiterals() {
        CompileResult multilineLiterals = BCompileUtil.compile("test-src/types/string/string_negative.bal");
        int indx = 0;
        validateError(multilineLiterals, indx++, "missing double quote", 18, 17);
        validateError(multilineLiterals, indx++, "missing semicolon token", 19, 1);
        validateError(multilineLiterals, indx++, "invalid expression statement", 19, 5);
        validateError(multilineLiterals, indx++, "undefined symbol 'World'", 19, 5);
        validateError(multilineLiterals, indx++, "invalid expression statement", 19, 10);
        validateError(multilineLiterals, indx++, "missing semicolon token", 19, 10);
        validateError(multilineLiterals, indx++, "operator '!' not defined for 'string'", 19, 10);
        validateError(multilineLiterals, indx++, "missing double quote", 19, 11);
        validateError(multilineLiterals, indx++, "missing semicolon token", 20, 1);
        validateError(multilineLiterals, indx++, "invalid expression statement", 20, 28);
        validateError(multilineLiterals, indx++, "missing semicolon token", 20, 28);
        validateError(multilineLiterals, indx++, "undefined symbol 'Bob'", 20, 28);
        validateError(multilineLiterals, indx++, "missing double quote", 20, 32);
        validateError(multilineLiterals, indx++, "missing semicolon token", 20, 32);
        validateError(multilineLiterals, indx++, "missing pipe token", 21, 1);
        validateError(multilineLiterals, indx++, "missing binary operator", 21, 22);
        validateError(multilineLiterals, indx++, "missing double quote", 21, 22);
        validateError(multilineLiterals, indx++, "missing semicolon token", 22, 1);
        validateError(multilineLiterals, indx++, "invalid escape sequence '\\B'", 25, 16);
        Assert.assertEquals(multilineLiterals.getErrorCount(), indx);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
