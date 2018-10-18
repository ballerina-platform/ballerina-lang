/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for Symbolic String Literal.
 * @since 0.982.1
 */
public class SymbolicStringTest {
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/string/symbolic-string-test.bal");
    }

    @Test
    public void testContains() {
        BValue[] returns = BRunUtil.invoke(result, "contains");
        Assert.assertEquals(returns[0].stringValue(), "true");
    }

    @Test
    public void testEqualsIgnoreCaseTest1() {
        BValue[] returns = BRunUtil.invoke(result, "equalsIgnoreCaseTest1");
        Assert.assertEquals(returns[0].stringValue(), "true");
    }

    @Test
    public void testEqualsIgnoreCaseTest2() {
        BValue[] returns = BRunUtil.invoke(result, "equalsIgnoreCaseTest2");
        Assert.assertEquals(returns[0].stringValue(), "false");
    }

    @Test
    public void testEqualsIgnoreCaseTest3() {
        BValue[] returns = BRunUtil.invoke(result, "equalsIgnoreCaseTest3");
        Assert.assertEquals(returns[0].stringValue(), "true");
    }

    @Test
    public void testHasPrefix() {
        BValue[] returns = BRunUtil.invoke(result, "hasPrefix");
        Assert.assertEquals(returns[0].stringValue(), "true");
    }

    @Test
    public void testHasSuffix() {
        BValue[] returns = BRunUtil.invoke(result, "hasSuffix");
        Assert.assertEquals(returns[0].stringValue(), "false");
    }

    @Test
    public void testIndexOf() {
        BValue[] returns = BRunUtil.invoke(result, "indexOf");
        Assert.assertEquals(returns[0].stringValue(), "7");
    }

    @Test
    public void testLastIndexOf() {
        BValue[] returns = BRunUtil.invoke(result, "lastIndexOf");
        Assert.assertEquals(returns[0].stringValue(), "6");
    }

    @Test
    public void testReplace() {
        BValue[] returns = BRunUtil.invoke(result, "replace");
        Assert.assertEquals(returns[0].stringValue(), "ByeWorld");
    }

    @Test
    public void testReplaceAll() {
        BValue[] returns = BRunUtil.invoke(result, "replaceAll");
        Assert.assertEquals(returns[0].stringValue(), "Hell0W0rld");
    }

    @Test
    public void testSubstring() {
        BValue[] returns = BRunUtil.invoke(result, "substring");
        Assert.assertEquals(returns[0].stringValue(), "Hell");
    }

    @Test
    public void testToLower() {
        BValue[] returns = BRunUtil.invoke(result, "toLower");
        Assert.assertEquals(returns[0].stringValue(), "helloworld");
    }

    @Test
    public void testToUpper() {
        BValue[] returns = BRunUtil.invoke(result, "toUpper");
        Assert.assertEquals(returns[0].stringValue(), "HELLOWORLD");
    }

    @Test
    public void testStringValueOf() {
        BValue[] returns = BRunUtil.invoke(result, "stringValueOf");
        Assert.assertEquals(returns[0].stringValue(), "HelloWorld");
    }

    @Test
    public void testLength() {
        BValue[] returns = BRunUtil.invoke(result, "length");
        Assert.assertEquals(returns[0].stringValue(), "10");
    }

    @Test
    public void testUnescape() {
        BValue[] returns = BRunUtil.invoke(result, "unescape");
        Assert.assertEquals(returns[0].stringValue(), "HelloWorld");
    }

    @Test
    public void testToByteArray() {
        BValue[] returns = BRunUtil.invoke(result, "toByteArray");
        Assert.assertEquals(returns[0].stringValue(), "[72, 101, 108, 108, 111, 87, 111, 114, 108, 100]");
    }

    @Test
    public void testSplit() {
        BValue[] returns = BRunUtil.invoke(result, "testSplit");
        Assert.assertEquals(returns[0].stringValue(), "[\"hell\", \"mell\", \"tell\"]");
    }

    @Test
    public void testBase64EncodeString() {
        BValue[] returns = BRunUtil.invoke(result, "testBase64EncodeString");
        Assert.assertEquals(returns[0].stringValue(), "SGVsbG9Xb3JsZA==");
    }

    @Test
    public void testBase16ToBase64Encoding() {
        BValue[] returns = BRunUtil.invoke(result, "testBase16ToBase64Encoding");
        Assert.assertEquals(returns[0].stringValue(), "/93d");
    }

    @Test
    public void testBase64ToBase16Encoding() {
        BValue[] returns = BRunUtil.invoke(result, "testBase64ToBase16Encoding");
        Assert.assertEquals(returns[0].stringValue(), "48656C6C6F2042616C6C6572696E61");
    }

    @Test
    public void testHMACValueFromBase16ToBase64Encoding() {
        BValue[] returns = BRunUtil.invoke(result, "testHMACValueFromBase16ToBase64Encoding");
        Assert.assertEquals(returns[0].stringValue(), "TEMPNUuD0bKeT91tIPBEUA==");
    }

    @Test
    public void testHMACValueFromBase64ToBase16Encoding() {
        BValue[] returns = BRunUtil.invoke(result, "testHMACValueFromBase64ToBase16Encoding");
        Assert.assertEquals(returns[0].stringValue(), "4C430F354B83D1B29E4FDD6D20F04450");
    }

    @Test
    public void testStringArrays() {
        BValue[] returns = BRunUtil.invoke(result, "testStringArray");
        Assert.assertEquals(returns[0].stringValue(), "hello");
    }

    @Test
    public void testMaps() {
        BValue[] returns = BRunUtil.invoke(result, "testMap");
        Assert.assertEquals(returns[0].stringValue(), "[\"SriLanka\", \"Mount_Lavinia\", \"PO00300\"]");
    }

    @Test
    public void testTuples() {
        BValue[] returns = BRunUtil.invoke(result, "testTuples");
        Assert.assertEquals(returns[0].stringValue(), "10");
        Assert.assertEquals(returns[1].stringValue(), "John_Snow");
        Assert.assertEquals(returns[2].stringValue(), "UK");
    }

    @Test
    public void testJson() {
        BValue[] returns = BRunUtil.invoke(result, "testJson");
        Assert.assertEquals(returns[0].stringValue(), "Apple");
        Assert.assertEquals(returns[1].stringValue(), "{\"name\":\"apple\", \"color\":\"red\"}");
        Assert.assertEquals(returns[2].stringValue(), "{\"fname\":\"John\", \"lname\":\"Stallone\", \"age\":66}");
        Assert.assertEquals(returns[3].stringValue(), "John");
        Assert.assertEquals(returns[4].stringValue(), "Stallone");
        Assert.assertEquals(returns[5].stringValue(), "66");
        Assert.assertEquals(returns[6].stringValue(), "{\"fname\":\"Peter\", \"lname\":\"Stallone\", \"age\":30, " +
                "\"address\":{\"line\":\"20 Palm Grove\", \"city\":\"Colombo 03\", \"country\":\"SriLanka\", " +
                "\"province\":\"Western\"}}");
    }

    @Test
    public void testObjects() {
        BValue[] returns = BRunUtil.invoke(result, "testObject");
        Assert.assertEquals(returns[0].stringValue(), "John-John Doe");
        Assert.assertEquals(returns[1].stringValue(), "Adam-Adam Page");
    }

    @Test
    public void testRecords() {
        BValue[] returns = BRunUtil.invoke(result, "testRecords");
        Assert.assertEquals(returns[0].stringValue(), "AdamPage");
    }

    @Test
    public void testGlobalVars() {
        BValue[] returns = BRunUtil.invoke(result, "testGlobalVars");
        Assert.assertEquals(returns[0].stringValue(), "abcdefghmnop123ijkl");
    }

    @Test
    public void testConditions() {
        BValue[] returns = BRunUtil.invoke(result, "testConditions");
        Assert.assertEquals(returns[0].stringValue(), "true");
    }

    @Test
    public void testUnicodeCharacters_1() {
        BValue[] returns = BRunUtil.invoke(result, "testUnicode_1");
        Assert.assertEquals(returns[0].stringValue(), "true");
    }

    @Test
    public void testUnicodeCharacters_2() {
        BValue[] returns = BRunUtil.invoke(result, "testUnicode_2");
        Assert.assertEquals(returns[0].stringValue(), "true");
    }

    @Test
    public void testConcatenation() {
        BValue[] returns = BRunUtil.invoke(result, "testConcatenation");
        Assert.assertEquals(returns[0].stringValue(), "helloworld");
    }

    @Test
    public void testUnicodeEquality() {
        BValue[] returns = BRunUtil.invoke(result, "testUnicodeEquality");
        Assert.assertEquals(returns[0].stringValue(), "true");
    }
}
