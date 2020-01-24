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

import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

/**
 * Test cases for ballerina.encoding native functions.
 *
 * @since 0.990.3
 */
public class Base64UrlEncodingTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/bas64-url-encoding-test.bal");
    }

    @Test(description = "Check Base64 URL encoding")
    public void testEncodeBase64Url() {
        String input = "Ballerina Base64 URL encoding test";
        byte[] inputArray = input.getBytes(StandardCharsets.UTF_8);
        String expectedValue = "QmFsbGVyaW5hIEJhc2U2NCBVUkwgZW5jb2RpbmcgdGVzdA";
        BValue[] args = {new BValueArray(inputArray)};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncodeBase64Url", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test(description = "Check Base64 URL decoding")
    public void testDecodeBase64Url() {
        String input = "QmFsbGVyaW5hIEJhc2U2NCBVUkwgZW5jb2RpbmcgdGVzdA";
        byte[] expectedValue = "Ballerina Base64 URL encoding test".getBytes(StandardCharsets.UTF_8);
        BValue[] args = {new BString(input)};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testDecodeBase64Url", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedValue);
    }
}
