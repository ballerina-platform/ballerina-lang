/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.stdlib.encoding;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Test cases for ballerina.encoding native functions.
 *
 * @since 0.990.3
 */
public class EncodingTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/encoding/encoding-test.bal");
    }

    @Test(description = "Check byte array to hex encoding.")
    public void testEncodeToHex() {
        String input = "Ballerina encoding test";
        byte[] inputArray = input.getBytes(StandardCharsets.UTF_8);
        String expectedValue = "42616C6C6572696E6120656E636F64696E672074657374";

        BValue[] args = {new BValueArray(inputArray)};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "encodeToHex", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test(description = "Check byte array to base64 encoding.")
    public void testEncodeToBase64() {
        String input = "Ballerina encoding test";
        byte[] inputArray = input.getBytes(StandardCharsets.UTF_8);
        String expectedValue = "QmFsbGVyaW5hIGVuY29kaW5nIHRlc3Q=";

        BValue[] args = {new BValueArray(inputArray)};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "encodeToBase64", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test(description = "Check hex encoded string to byte array decoding.")
    public void testDecodeFromHex() {
        String input = "42616C6C6572696E6120656E636F64696E672074657374";
        byte[] expectedValue = "Ballerina encoding test".getBytes(StandardCharsets.UTF_8);

        BValue[] args = {new BString(input)};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "decodeFromHex", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedValue);
    }

    @Test(description = "Check base64 encoded string to byte array decoding.")
    public void testDecodeFromBase64() {
        String input = "QmFsbGVyaW5hIGVuY29kaW5nIHRlc3Q=";
        byte[] expectedValue = "Ballerina encoding test".getBytes(StandardCharsets.UTF_8);

        BValue[] args = {new BString(input)};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "decodeFromBase64", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedValue);
    }

    @Test(description = "Check decoding an non-hex string using hex decoding function.")
    public void testDecodeFromInvalidHex() {
        String input = "42616C6C6572696E6120656X636F64696E672074657374";

        BValue[] args = {new BString(input)};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "decodeFromHex", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BMap) ((BError) returnValues[0]).getDetails()).get(Constants.MESSAGE).stringValue(),
                "input is not a valid Hex value");
    }

    @Test(description = "Check decoding an non-base64 string using base64 decoding function.")
    public void testDecodeFromInvalidBase64() {
        String input = "QmFsbGVyaW5hIGVuY29kaW5nIHRlc#3Q=";

        BValue[] args = {new BString(input)};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "decodeFromBase64", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BMap) ((BError) returnValues[0]).getDetails()).get(Constants.MESSAGE).stringValue(),
                "input is not a valid Base64 value");
    }

    @Test(description = "Check encoding a complex strings using base64.")
    public void testEncoding() throws UnsupportedEncodingException {
        String[] stringsToTest = {"Hi", "`Welcome\" \r^to^\tBallerina\\ /$TestCases$\n # ~^!!!",
                "https://example.com/test/index.html#title1",
                "https://example.com/test/index.html?greeting=hello world&your-name=Ballerina Test cases",
                "バレリーナ日本語", "Ballerina 的中文翻译", "Traducción al español de  la bailarina"};

        for (String s : stringsToTest) {
            BValue[] returnVals = BRunUtil.invoke(compileResult, "testEncodeDecode", new BValue[]{new BString(s)});
            Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                    "Invalid Return Values for :" + s);
            Assert.assertEquals(((BValueArray) returnVals[0]).getBytes(), s.getBytes("UTF-8"),
                    "Original and Return value didn't match");
        }
    }

    @Test(description = "Check encoding a simple strings using base64.")
    public void testBase64EncodeString() {
        String expectedValue = "SGVsbG8gQmFsbGVyaW5h";
        BValue[] args = new BValue[]{new BString("Hello Ballerina")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testBase64EncodeString", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test(description = "Check decoding a simple base64 encodded string.")
    public void testBase64DecodeString() throws UnsupportedEncodingException {
        byte[] expectedValue = "Hello Ballerina".getBytes("UTF-8");
        BValue[] args = new BValue[]{new BString("SGVsbG8gQmFsbGVyaW5h")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testBase64DecodeString", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                "Invalid return value");
        Assert.assertNotNull(returnValues[0], "Invalid return value");
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedValue);
    }

    @Test(description = "Check decoding a hex encoded value and re-encoding same with base64")
    public void testBase16ToBase64Encoding() {
        String expectedValue = "SGVsbG8gQmFsbGVyaW5h";
        BValue[] args = new BValue[]{new BString("48656C6C6F2042616C6C6572696E61")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testBase16ToBase64Encoding", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test(description = "Check decoding a base64 encoded value and re-encoding same with hex")
    public void testBase64ToBase16Encoding() {
        String expectedValue = "48656C6C6F2042616C6C6572696E61";
        BValue[] args = new BValue[]{new BString("SGVsbG8gQmFsbGVyaW5h")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testBase64ToBase16Encoding", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test(description = "Get string representation of byte array using UTF-8 encoding using default encoding")
    public void testByteArrayToString1() throws UnsupportedEncodingException {
        String content = "This is a sample string";

        BValue[] args = {new BValueArray(content.getBytes("UTF-8"))};
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteArrayToString1", args);

        Assert.assertEquals(returns[0].stringValue(), content);
    }

    @Test(description = "Get string representation of byte array using UTF-8 encoding")
    public void testByteArrayToString2() throws UnsupportedEncodingException {
        String content = "This is a sample string";

        BValue[] args = {new BValueArray(content.getBytes("UTF-8")), new BString("UTF-8")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteArrayToString2", args);

        Assert.assertEquals(returns[0].stringValue(), content);
    }

    @Test(description = "Get string representation of byte array using UTF-16 encoding")
    public void testByteArrayToString3() throws UnsupportedEncodingException {
        String content = "This is a sample string";

        BValue[] args = {new BValueArray(content.getBytes("UTF-16")), new BString("UTF-16")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteArrayToString2", args);

        Assert.assertEquals(returns[0].stringValue(), content);
    }
}
