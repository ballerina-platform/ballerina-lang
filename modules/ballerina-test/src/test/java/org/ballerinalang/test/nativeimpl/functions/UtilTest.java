/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.test.nativeimpl.functions;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test Cases for ballerina.utils native functions.
 */
public class UtilTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile(this, "test-src", "nativeimpl/functions/utils-test.bal");
    }

    @Test
    public void testEncoding() {
        String[] stringsToTest = {"Hi", "`Welcome\" \r^to^\tBallerina\\ /$TestCases$\n # ~^!!!",
                "https://example.com/test/index.html#title1",
                "https://example.com/test/index.html?greeting=hello world&your-name=Ballerina Test cases",
                "バレリーナ日本語", "Ballerina 的中文翻译", "Traducción al español de  la bailarina"};

        for (String s : stringsToTest) {
            BValue[] returnVals = BRunUtil.invoke(compileResult, "testEncodeDecode", new BValue[]{new BString(s)});
            Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                               "Invalid Return Values for :" + s);
            Assert.assertEquals(returnVals[0].stringValue(), s, "Original and Return value didn't match");
        }
    }

    @Test
    public void testRandomString() {
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testRandomString");
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                           "Invalid return value");
    }

    @Test
    public void testHmac() {
        String messageString = "Ballerina HMAC test";
        BString message = new BString(messageString);
        String keyString = "abcdefghijk";
        BString key = new BString(keyString);

        String expectedMD5Hash = "3D5AC29160F2905A5C8153597798A4C1";
        String expectedSHA1Hash = "13DD8D54D0EB702EDC6E8EDCAF616837D3A51499";
        String expectedSHA256Hash = "2651203E18BF0088D3EF1215022D147E2534FD4BAD5689C9E5F12436E9758B15";

        BValue[] args = {message, key, new BString("MD5")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testHmac", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(returnValues[0].stringValue(), expectedMD5Hash);

        args = new BValue[]{message, key, new BString("SHA1")};
        returnValues = BRunUtil.invoke(compileResult, "testHmac", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(returnValues[0].stringValue(), expectedSHA1Hash);

        args = new BValue[]{message, key, new BString("SHA256")};
        returnValues = BRunUtil.invoke(compileResult, "testHmac", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(returnValues[0].stringValue(), expectedSHA256Hash);
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testHmacNegativeInvalidAlgo() {
        final String key = "abcdefghijk";
        BValue[] args = {new BString("Ballerina HMAC test"), new BString(key), new BString("SHA124")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testHmac", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                           "Invalid return value");
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testHmacNegativeInvalidKey() {
        BValue[] args = {new BString("Ballerina HMAC test"), new BString(""), new BString("SHA1")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testHmac", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                           "Invalid return value");
    }

    @Test
    public void testMessageDigest() {
        List<BValue[]> argsList = new ArrayList<>();
        argsList.add(new BValue[]{new BString("Ballerina HMAC BASE64 test"), new BString("SHA1")});
        argsList.add(new BValue[]{new BString("Ballerina HMAC BASE64 test"), new BString("SHA256")});
        argsList.add(new BValue[]{new BString("Ballerina HMAC BASE64 test"), new BString("MD5")});

        for (BValue[] args : argsList) {
            BValue[] returnValues = BRunUtil.invoke(compileResult, "testMessageDigest", args);
            Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                               "Invalid return value");
        }
    }

    @Test
    public void testBase64Encoding() {
        String expectedValue = "SGVsbG8gQmFsbGVyaW5h";
        BValue[] args = new BValue[]{new BString("Hello Ballerina")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testBase64Encoding", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                           "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test
    public void testBase64Decoding() {
        String expectedValue = "Hello Ballerina";
        BValue[] args = new BValue[]{new BString("SGVsbG8gQmFsbGVyaW5h")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testBase64Decoding", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                           "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test
    public void testBase16ToBase64Encoding() {
        String expectedValue = "SGVsbG8gQmFsbGVyaW5h";
        BValue[] args = new BValue[]{new BString("48656C6C6F2042616C6C6572696E61")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testBase16ToBase64Encoding", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                           "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test
    public void testBase64ToBase16Encoding() {
        String expectedValue = "48656C6C6F2042616C6C6572696E61";
        BValue[] args = new BValue[]{new BString("SGVsbG8gQmFsbGVyaW5h")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testBase64ToBase16Encoding", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                           "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test
    public void testHMACValueFromBase16ToBase64Encoding() {
        String expectedValue = "S4qhC4EdsEcvY64gs+JsmA==";
        BValue[] args = new BValue[]{new BString("Hello Ballerina"), new BString("abcdefghijk"), new BString("MD5")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testHMACValueFromBase16ToBase64Encoding", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                           "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test
    public void testHMACValueFromBase64ToBase16Encoding() {
        String expectedValue = "4B8AA10B811DB0472F63AE20B3E26C98";
        BValue[] args = new BValue[]{new BString("Hello Ballerina"), new BString("abcdefghijk"),
                new BString("MD5")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testHMACValueFromBase64ToBase16Encoding", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                           "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }
}
