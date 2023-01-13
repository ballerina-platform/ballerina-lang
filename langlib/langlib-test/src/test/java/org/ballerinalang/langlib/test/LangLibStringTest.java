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

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for the lang.string library.
 *
 * @since 1.0
 */
public class LangLibStringTest {

    private CompileResult compileResult;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/stringlib_test.bal");
        negativeResult = BCompileUtil.compile("test-src/stringlib_negative_test.bal");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }

    @Test
    public void testToLower() {
        Object returns = BRunUtil.invoke(compileResult, "testToLower");
        assertEquals(returns.toString(), "hello ballerina!");
    }

    @Test
    public void testSubString() {
        Object returns = BRunUtil.invoke(compileResult, "testSubString");
        assertEquals(returns.toString(), "[\"Bal\",\"Ballerina!\",\"Ballerina!\"]");
    }

    @Test
    public void testConcat() {
        Object returns = BRunUtil.invoke(compileResult, "testConcat");
        assertEquals(returns.toString(), "Hello from Ballerina");
    }

    @Test
    public void testFromBytes() {
        Object returns = BRunUtil.invoke(compileResult, "testFromBytes");
        assertEquals(returns.toString(), "Hello Ballerina!");
    }

    @Test
    public void testJoin() {
        Object returns = BRunUtil.invoke(compileResult, "testJoin");
        assertEquals(returns.toString(), "Sunday, Monday, Tuesday");
    }

    @Test
    public void testStartsWith() {
        Object returns = BRunUtil.invoke(compileResult, "testStartsWith");
        assertTrue((Boolean) returns);
    }

    @Test(dataProvider = "SubStringsForEndsWith")
    public void testEndsWith(BString str, boolean expected) {
        Object returns = BRunUtil.invoke(compileResult, "testEndsWith", new Object[]{str});
        assertEquals(returns, expected);
    }

    @Test(dataProvider = "SubStringsForIndexOf")
    public void testIndexOf(BString substr, Object expected) {
        Object returns = BRunUtil.invoke(compileResult, "testIndexOf", new Object[]{substr});

        if (expected == null) {
            assertNull(returns);
        } else {
            assertEquals(returns, expected, "For substring: " + substr);
        }
    }

    @Test(dataProvider = "codePointCompareProvider")
    public void testCodePointCompare(String st1, String st2, long expected) {
        Object[] args = {StringUtils.fromString(st1), StringUtils.fromString(st2)};
        Object returns = BRunUtil.invoke(compileResult, "testCodePointCompare", args);
        assertEquals(returns, expected);
    }

    @Test(dataProvider = "codePointAtProvider")
    public void testGetCodepoint(String st1, long at, long expected) {
        Object[] args = {StringUtils.fromString(st1), at};
        Object returns = BRunUtil.invoke(compileResult, "testGetCodepoint", args);
        assertEquals(returns, expected);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
        expectedExceptionsMessageRegExp = ".*IndexOutOfRange \\{\"message\":\"string codepoint index out of range: " +
                "1\"\\}.*")
    public void testGetCodepointNegative() {
        testGetCodepoint("", 1, 0);
    }

    @Test(dataProvider = "stringToCodepointsProvider")
    public void testToCodepointInts(String st1, int[] expected) {
        Object[] args = {StringUtils.fromString(st1)};
        Object returns = BRunUtil.invoke(compileResult, "testToCodepointInts", args);
        assertEquals(((BArray) returns).size(), expected.length);
        int[] codePoints = toIntArray((BArray) returns);
        assertEquals(codePoints, expected);
    }

    @Test(dataProvider = "codePointsToString")
    public void testFromCodePointInts(long[] array, String expected) {
        Object[] args = {ValueCreator.createArrayValue(array)};
        Object returns = BRunUtil.invoke(compileResult, "testFromCodePointInts", args);
        assertEquals(returns.toString(), expected);
    }

    @Test
    public void testFromCodePointIntsNegative() {
        Object[] args = {ValueCreator.createArrayValue(new long[]{0x10FFFF, 0x10FFFF + 1})};
        Object returns = BRunUtil.invoke(compileResult, "testFromCodePointInts", args);
        assertEquals(returns.toString(), "error(\"Invalid codepoint: 1114112\")");
    }

    private int[] toIntArray(BArray array) {
        int[] ar = new int[(int) array.size()];
        for (int i = 0; i < ar.length; i++) {
            ar[i] = (int) array.getInt(i);
        }
        return ar;
    }

    @DataProvider(name = "SubStringsForIndexOf")
    public Object[][] getSubStrings() {
        return new Object[][]{
                {StringUtils.fromString("Ballerina"), 6L},
                {StringUtils.fromString("Invalid"), null},
        };
    }

    @DataProvider(name = "SubStringsForEndsWith")
    public Object[][] getSubStringsForMatching() {
        return new Object[][]{
                {StringUtils.fromString("Ballerina!"), true},
                {StringUtils.fromString("Invalid"), false},
        };
    }

    @DataProvider(name = "codePointCompareProvider")
    public Object[][] codePointCompareProvider() {
        return new Object[][]{
                {"a",    "a",     0},
                {"abc",  "abcd", -1},
                {"abcd", "abc",   1},
                {"",     "a",    -1},
                {"a",    "",      1},
                {"",     "",      0},
        };
    }

    @DataProvider(name = "codePointAtProvider")
    public Object[][] codePointAtProvider() {
        return new Object[][]{
                {"a", 0, "a".codePointAt(0)},
                {"a👻cd", 1, "👻".codePointAt(0)},
        };
    }

    @DataProvider(name = "stringToCodepointsProvider")
    public Object[][] stringToCodepointsProvider() {
        return new Object[][]{
                {"",      "".codePoints().toArray()},
                {"a",     "a".codePoints().toArray()},
                {"a👻cd", "a👻cd".codePoints().toArray()},
        };
    }

    @DataProvider(name = "codePointsToString")
    public Object[][] codePointsToString() {
        return new Object[][]{
                {"".codePoints().asLongStream().toArray(),      ""},
                {"a".codePoints().asLongStream().toArray(),     "a"},
                {"a👻cd".codePoints().asLongStream().toArray(), "a👻cd"},
        };
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.string\\}StringOperationError " +
                    "\\{\"message\":\"string index out of range. Length:'6' requested: '7' to '9'\"\\}.*")
    public void testSubstringOutRange() {
        BRunUtil.invoke(compileResult, "testSubstringOutRange");
        Assert.fail();
    }

    @Test(dataProvider = "testSubstringDataProvider")
    public void testSubstring(String str, long start, long end, String result) {
        Object[] args = {StringUtils.fromString(str), start, end};
        Object returns = BRunUtil.invoke(compileResult, "testSubstring", args);
        Assert.assertEquals(returns.toString(),
                "error(\"{ballerina/lang.string}StringOperationError\",message=\"" + result + "\")");
    }

    @DataProvider(name = "testSubstringDataProvider")
    public Object[][] testSubstringDataProvider() {
        return new Object[][]{
                {"abcdef", -2, -1, "string index out of range. Length:'6' requested: '-2' to '-1'"},
                {"abcdef", -2, -5, "string index out of range. Length:'6' requested: '-2' to '-5'"},
                {"abcdef",  0, -1, "invalid substring range. Length:'6' requested: '0' to '-1'"},
                {"",        0, -1, "invalid substring range. Length:'0' requested: '0' to '-1'"},
                {"abcdef",  3,  2, "invalid substring range. Length:'6' requested: '3' to '2'"},
        };
    }

    @Test
    public void testIncludes() {
        Object returns = BRunUtil.invoke(compileResult, "testIncludes");
        assertTrue((Boolean) returns);
    }

    @Test
    public void testChainedStringFunctions() {
        Object returns = BRunUtil.invoke(compileResult, "testChainedStringFunctions");
        assertEquals(returns.toString(), "foo1foo2foo3foo4");
    }

    @Test(dataProvider = "unicodeCharProvider")
    public void testIteratorWithUnicodeChar(long codePoint, long[] expected) {
        Object[] args = {codePoint, ValueCreator.createArrayValue(expected)};
        BRunUtil.invoke(compileResult, "testIteratorWithUnicodeChar", args);
    }

    @DataProvider(name = "unicodeCharProvider")
    public Object[][] testUnicodeCharIteratorProvider() {
        long asciiValue = Long.parseLong("7E", 16);
        long nonAsciiLatinValue = Long.parseLong("DF", 16);
        long twoByteValue = Long.parseLong("03BB", 16);
        long threeByteValue = Long.parseLong("0BF8", 16);
        long emojiValue = Long.parseLong("1F4A9", 16);
        long encodedValue = Long.parseLong("DFFFF", 16);

        return new Object[][]{
                {asciiValue, new long[]{asciiValue}},
                {nonAsciiLatinValue, new long[]{nonAsciiLatinValue}},
                {twoByteValue, new long[]{twoByteValue}},
                {threeByteValue, new long[]{threeByteValue}},
                {emojiValue, new long[]{emojiValue}},
                {encodedValue, new long[]{encodedValue}},
        };
    }

    @Test(dataProvider = "StringPrefixProvider")
    public void testConcatNonBMPStrings(String prefix) {
        BString bString = StringUtils.fromString(prefix);
        BString resultString = StringUtils.fromString(prefix + "👋world🤷!");
        BRunUtil.invoke(compileResult, "concatNonBMP", new Object[]{bString, resultString});
    }

    @Test(dataProvider = "StringPrefixProvider")
    public void testCharIterator(String prefix) {
        BString bString = StringUtils.fromString(prefix + "👋world🤷!");
        BRunUtil.invoke(compileResult, "testCharIterator", new Object[]{bString});
    }

    @DataProvider(name = "StringPrefixProvider")
    public Object[] testBMPStringProvider() {
        return new String[]{"ascii~?", "£ßóµ¥", "ęЯλĢŃ", "☃✈௸ऴᛤ", "😀🄰🍺" };
    }

    @Test
    public void stringlibNegativeTest() {
        int err = 0;
        BAssertUtil.validateError(negativeResult, err++, "missing required parameter 'len' in call to 'padStart()'",
                33, 16);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'string:Char', found 'string'",
                34, 36);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'string:Char', found 'string'",
                35, 36);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'int', found 'string'",
                36, 13);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'string:Char', found 'string'",
                38, 36);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'byte', found 'string'",
                39, 14);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'string:Char', found 'string'",
                40, 21);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'string:Char', found 'string'",
                41, 21);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'string:Char', found 'string'",
                41, 45);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'int', found 'float'",
                43, 31);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'int', found 'decimal'",
                44, 31);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'int', found 'float'",
                45, 35);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'int', found 'string'",
                46, 31);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'string:Char', found 'string'",
                48, 35);
        BAssertUtil.validateError(negativeResult, err++, "missing required parameter 'len' in call to 'padEnd()'",
                52, 16);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'string:Char', found 'string'",
                53, 34);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'string:Char', found 'string'",
                54, 34);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'int', found 'string'",
                55, 13);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'string:Char', found 'string'",
                57, 34);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'byte', found 'string'",
                58, 14);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'string:Char', found 'string'",
                59, 21);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'string:Char', found 'string'",
                60, 21);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'string:Char', found 'string'",
                60, 43);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'int', found 'float'",
                62, 29);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'int', found 'decimal'",
                63, 29);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'int', found 'float'",
                64, 33);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'int', found 'string'",
                65, 29);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'string:Char', found 'string'",
                67, 33);
        BAssertUtil.validateError(negativeResult, err++, "missing required parameter 'len' in call to 'padZero()'",
                71, 16);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'string:Char', found 'string'",
                72, 37);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'string:Char', found 'string'",
                73, 38);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'int', found 'string'",
                74, 13);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'string:Char', found 'string'",
                76, 35);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'byte', found 'string'",
                77, 14);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'string:Char', found 'string'",
                78, 21);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'string:Char', found 'string'",
                79, 21);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'string:Char', found 'string'",
                79, 44);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'int', found 'float'",
                81, 30);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'int', found 'decimal'",
                82, 30);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'int', found 'float'",
                83, 34);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'int', found 'string'",
                84, 30);
        BAssertUtil.validateError(negativeResult, err++, "incompatible types: expected 'string:Char', found 'string'",
                86, 34);
        Assert.assertEquals(negativeResult.getErrorCount(), err);
    }

    @Test
    public void testStringlibPanicTest() {
        CompileResult resultNegative = BCompileUtil.compile("test-src/stringlib_panic_test.bal");

        Assert.assertEquals(resultNegative.getErrorCount(), 0);

        Object[] args = {1};
        Object returns = BRunUtil.invoke(resultNegative, "testInvalidLengthForStringPadding", args);
        Assert.assertEquals(returns.toString(), "error(\"{ballerina/lang.string}length greater that '2147483647' not" +
                " yet supported\")");

        args = new Object[] {2};
        returns = BRunUtil.invoke(resultNegative, "testInvalidLengthForStringPadding", args);
        Assert.assertEquals(returns.toString(), "error(\"{ballerina/lang.string}length greater that '2147483647' not" +
                " yet supported\")");

        args = new Object[] {3};
        returns = BRunUtil.invoke(resultNegative, "testInvalidLengthForStringPadding", args);
        Assert.assertEquals(returns.toString(), "error(\"{ballerina/lang.string}length greater that '2147483647' not" +
                " yet supported\")");

        args = new Object[] {4};
        returns = BRunUtil.invoke(resultNegative, "testInvalidLengthForStringPadding", args);
        Assert.assertEquals(returns.toString(), "error(\"{ballerina/lang.string}length greater that '2147483647' not" +
                " yet supported\")");

        args = new Object[] {5};
        returns = BRunUtil.invoke(resultNegative, "testInvalidLengthForStringPadding", args);
        Assert.assertEquals(returns.toString(), "error(\"{ballerina/lang.string}length greater that '2147483647' not" +
                " yet supported\")");

        args = new Object[] {6};
        returns = BRunUtil.invoke(resultNegative, "testInvalidLengthForStringPadding", args);
        Assert.assertEquals(returns.toString(), "error(\"{ballerina/lang.string}length greater that '2147483647' not" +
                " yet supported\")");
    }

    @Test(dataProvider = "stringFunctionProvider")
    public void testStringLibFunctions(String functionName) {
        BRunUtil.invoke(compileResult, functionName);
    }

    @DataProvider(name = "stringFunctionProvider")
    public Object[] testStringFunctionProvider() {

        return new String[]{
                "testLength",
                "testIterator",
                "testFromBytesInvalidValues",
                "testLastIndexOf",
                "testEqualsIgnoreCaseAscii",
                "testLangLibCallOnStringSubTypes",
                "testLangLibCallOnFiniteType",
                "testPadStart",
                "testPadEnd",
                "testPadZero",
                "testMatches",
                "testIncludesMatch",
                "testFromBytesAsync",
                "testEqualsIgnoreCaseAsciiAsync"
        };
    }

}
