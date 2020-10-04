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


import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
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
        BValue[] returns = BRunUtil.invoke(compileResult, "testToLower");
        assertEquals(returns[0].stringValue(), "hello ballerina!");
    }

    @Test
    public void testLength() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testLength");
        assertEquals(((BInteger) returns[0]).intValue(), "Hello Ballerina!".length());
    }

    @Test
    public void testSubString() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSubString");
        assertEquals(returns[0].stringValue(), "Bal");
        assertEquals(returns[1].stringValue(), "Ballerina!");
        assertEquals(returns[2].stringValue(), "Ballerina!");
    }

    @Test
    public void testIterator() {
        String[] expected = new String[]{"F", "o", "o", " ", "B", "a", "r"};
        BValue[] returns = BRunUtil.invoke(compileResult, "testIterator");
        BValueArray arr = (BValueArray) returns[0];
        long size = arr.size();

        for (int i = 0; i < size; i++) {
            assertEquals(arr.getString(i), expected[i]);
        }
    }

    @Test
    public void testConcat() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConcat");
        assertEquals(returns[0].stringValue(), "Hello from Ballerina");
    }

    @Test
    public void testFromBytes() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFromBytes");
        assertEquals(returns[0].stringValue(), "Hello Ballerina!");
    }

    @Test
    public void testJoin() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJoin");
        assertEquals(returns[0].stringValue(), "Sunday, Monday, Tuesday");
    }

    @Test
    public void testStartsWith() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStartsWith");
        assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(dataProvider = "SubStringsForEndsWith")
    public void testEndsWith(BString str, boolean expected) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEndsWith", new BValue[]{str});
        assertEquals(((BBoolean) returns[0]).booleanValue(), expected);
    }

    @Test(dataProvider = "SubStringsForIndexOf")
    public void testIndexOf(BString substr, Object expected) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIndexOf", new BValue[]{substr});

        if (expected == null) {
            assertNull(returns[0]);
        } else {
            assertEquals(((BInteger) returns[0]).intValue(), (long) expected, "For substring: " + substr);
        }
    }

    @Test(description = "Test the lastIndexOf() method.")
    public void testLastIndexOf() {
        BRunUtil.invoke(compileResult, "testLastIndexOf");
    }

    @Test(dataProvider = "codePointCompareProvider")
    public void testCodePointCompare(String st1, String st2, int expected) {
        BValue[] args = {new BString(st1), new BString(st2)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testCodePointCompare", args);
        assertEquals(((BInteger) returns[0]).intValue(), expected);
    }

    @Test(dataProvider = "codePointAtProvider")
    public void testGetCodepoint(String st1, int at, int expected) {
        BValue[] args = {new BString(st1), new BInteger(at)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetCodepoint", args);
        assertEquals(((BInteger) returns[0]).intValue(), expected);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
        expectedExceptionsMessageRegExp = ".*IndexOutOfRange \\{\"message\":\"String codepoint index out of range: " +
                "1\"\\}.*")
    public void testGetCodepointNegative() {
        testGetCodepoint("", 1, 0);
    }

    @Test(dataProvider = "stringToCodepointsProvider")
    public void testToCodepointInts(String st1, int[] expected) {
        BValue[] args = {new BString(st1)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToCodepointInts", args);
        assertEquals(((BValueArray) returns[0]).size(), expected.length);
        int[] codePoints = toIntArray((BValueArray) returns[0]);
        assertEquals(codePoints, expected);
    }

    @Test(dataProvider = "codePointsToString")
    public void testFromCodePointInts(long[] array, String expected) {
        BValue[] args = {new BValueArray(array)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testFromCodePointInts", args);
        assertEquals(((BString) returns[0]).stringValue(), expected);
    }

    @Test
    public void testFromCodePointIntsNegative() {
        BValue[] args = {new BValueArray(new long[]{0x10FFFF, 0x10FFFF + 1})};
        BValue[] returns = BRunUtil.invoke(compileResult, "testFromCodePointInts", args);
        assertEquals(((BError) returns[0]).stringValue(), "Invalid codepoint: 1114112 {}");
    }

    private int[] toIntArray(BValueArray array) {
        int[] ar = new int[(int) array.size()];
        for (int i = 0; i < ar.length; i++) {
            ar[i] = (int) array.getInt(i);
        }
        return ar;
    }

    @DataProvider(name = "SubStringsForIndexOf")
    public Object[][] getSubStrings() {
        return new Object[][]{
                {new BString("Ballerina"), 6L},
                {new BString("Invalid"), null},
        };
    }

    @DataProvider(name = "SubStringsForEndsWith")
    public Object[][] getSubStringsForMatching() {
        return new Object[][]{
                {new BString("Ballerina!"), true},
                {new BString("Invalid"), false},
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
    public void testSubstring(String str, int start, int end, String result) {
        BValue[] args = {new BString(str), new BInteger(start), new BInteger(end)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSubstring", args);
        Assert.assertEquals(returns[0].stringValue(),
                            "{ballerina/lang.string}StringOperationError {\"message\":\"" + result + "\"}");
    }

    @Test
    public void testEqualsIgnoreCaseAscii() {
        BRunUtil.invoke(compileResult, "testEqualsIgnoreCaseAscii");
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

}
