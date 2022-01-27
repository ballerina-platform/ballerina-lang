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
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.JvmRunUtil;
import org.testng.Assert;
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

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/stringlib_test.bal");
    }

    @Test
    public void testToLower() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testToLower");
        assertEquals(returns[0].toString(), "hello ballerina!");
    }

    @Test
    public void testLength() {
        JvmRunUtil.invoke(compileResult, "testLength");
    }

    @Test
    public void testSubString() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testSubString");
        assertEquals(returns[0].toString(), "[\"Bal\",\"Ballerina!\",\"Ballerina!\"]");
    }

    @Test
    public void testIterator() {
        JvmRunUtil.invoke(compileResult, "testIterator");
    }

    @Test
    public void testConcat() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testConcat");
        assertEquals(returns[0].toString(), "Hello from Ballerina");
    }

    @Test
    public void testFromBytes() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testFromBytes");
        assertEquals(returns[0].toString(), "Hello Ballerina!");
    }

    @Test
    public void testJoin() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testJoin");
        assertEquals(returns[0].toString(), "Sunday, Monday, Tuesday");
    }

    @Test
    public void testStartsWith() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testStartsWith");
        assertTrue((Boolean) returns[0]);
    }

    @Test(dataProvider = "SubStringsForEndsWith")
    public void testEndsWith(BString str, boolean expected) {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testEndsWith", new Object[]{str});
        assertEquals(returns[0], expected);
    }

    @Test(dataProvider = "SubStringsForIndexOf")
    public void testIndexOf(BString substr, Object expected) {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testIndexOf", new Object[]{substr});

        if (expected == null) {
            assertNull(returns[0]);
        } else {
            assertEquals(returns[0], expected, "For substring: " + substr);
        }
    }

    @Test(description = "Test the lastIndexOf() method.")
    public void testLastIndexOf() {
        JvmRunUtil.invoke(compileResult, "testLastIndexOf");
    }

    @Test(dataProvider = "codePointCompareProvider")
    public void testCodePointCompare(String st1, String st2, long expected) {
        Object[] args = {StringUtils.fromString(st1), StringUtils.fromString(st2)};
        Object[] returns = JvmRunUtil.invoke(compileResult, "testCodePointCompare", args);
        assertEquals(returns[0], expected);
    }

    @Test(dataProvider = "codePointAtProvider")
    public void testGetCodepoint(String st1, long at, long expected) {
        Object[] args = {StringUtils.fromString(st1), at};
        Object[] returns = JvmRunUtil.invoke(compileResult, "testGetCodepoint", args);
        assertEquals(returns[0], expected);
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
        Object[] returns = JvmRunUtil.invoke(compileResult, "testToCodepointInts", args);
        assertEquals(((BArray) returns[0]).size(), expected.length);
        int[] codePoints = toIntArray((BArray) returns[0]);
        assertEquals(codePoints, expected);
    }

    @Test(dataProvider = "codePointsToString")
    public void testFromCodePointInts(long[] array, String expected) {
        Object[] args = {ValueCreator.createArrayValue(array)};
        Object[] returns = JvmRunUtil.invoke(compileResult, "testFromCodePointInts", args);
        assertEquals(returns[0].toString(), expected);
    }

    @Test
    public void testFromCodePointIntsNegative() {
        Object[] args = {ValueCreator.createArrayValue(new long[]{0x10FFFF, 0x10FFFF + 1})};
        Object[] returns = JvmRunUtil.invoke(compileResult, "testFromCodePointInts", args);
        assertEquals(returns[0].toString(), "error(\"Invalid codepoint: 1114112\")");
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
        JvmRunUtil.invoke(compileResult, "testSubstringOutRange");
        Assert.fail();
    }

    @Test(dataProvider = "testSubstringDataProvider")
    public void testSubstring(String str, long start, long end, String result) {
        Object[] args = {StringUtils.fromString(str), start, end};
        Object[] returns = JvmRunUtil.invoke(compileResult, "testSubstring", args);
        Assert.assertEquals(returns[0].toString(),
                "error(\"{ballerina/lang.string}StringOperationError\",message=\"" + result + "\")");
    }

    @Test
    public void testEqualsIgnoreCaseAscii() {
        JvmRunUtil.invoke(compileResult, "testEqualsIgnoreCaseAscii");
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
        Object[] returns = JvmRunUtil.invoke(compileResult, "testIncludes");
        assertTrue((Boolean) returns[0]);
    }

    @Test
    public void testChainedStringFunctions() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testChainedStringFunctions");
        assertEquals(returns[0].toString(), "foo1foo2foo3foo4");
    }

    @Test
    public void testLangLibCallOnStringSubTypes() {
        JvmRunUtil.invoke(compileResult, "testLangLibCallOnStringSubTypes");
    }

    @Test
    public void testLangLibCallOnFiniteType() {
        JvmRunUtil.invoke(compileResult, "testLangLibCallOnFiniteType");
    }

    @Test(dataProvider = "unicodeCharProvider")
    public void testIteratorWithUnicodeChar(long codePoint, long[] expected) {
        Object[] args = {codePoint, ValueCreator.createArrayValue(expected)};
        JvmRunUtil.invoke(compileResult, "testIteratorWithUnicodeChar", args);
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
        JvmRunUtil.invoke(compileResult, "concatNonBMP", new Object[]{bString, resultString});
    }

    @Test(dataProvider = "StringPrefixProvider")
    public void testCharIterator(String prefix) {
        BString bString = StringUtils.fromString(prefix + "👋world🤷!");
        JvmRunUtil.invoke(compileResult, "testCharIterator", new Object[]{bString});
    }

    @DataProvider(name = "StringPrefixProvider")
    public Object[] testBMPStringProvider() {
        return new String[]{"ascii~?", "£ßóµ¥", "ęЯλĢŃ", "☃✈௸ऴᛤ", "😀🄰🍺" };
    }
}
