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
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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

    @Test
    public void testEncodeToHex() {
        String input = "Ballerina encoding test";
        byte[] inputArray = input.getBytes(StandardCharsets.UTF_8);
        String expectedValue = "42616C6C6572696E6120656E636F64696E672074657374";

        BValue[] args = {new BValueArray(inputArray)};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "encodeToHex", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test
    public void testEncodeToBase64() {
        String input = "Ballerina encoding test";
        byte[] inputArray = input.getBytes(StandardCharsets.UTF_8);
        String expectedValue = "QmFsbGVyaW5hIGVuY29kaW5nIHRlc3Q=";

        BValue[] args = {new BValueArray(inputArray)};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "encodeToBase64", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test
    public void testDecodeFromHex() {
        String input = "42616C6C6572696E6120656E636F64696E672074657374";
        byte[] expectedValue = "Ballerina encoding test".getBytes(StandardCharsets.UTF_8);

        BValue[] args = {new BString(input)};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "decodeFromHex", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedValue);
    }

    @Test
    public void testDecodeFromBase64() {
        String input = "QmFsbGVyaW5hIGVuY29kaW5nIHRlc3Q=";
        byte[] expectedValue = "Ballerina encoding test".getBytes(StandardCharsets.UTF_8);

        BValue[] args = {new BString(input)};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "decodeFromBase64", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedValue);
    }
}
