/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Test cases for ballerina.security.crypto native functions.
 */
public class CryptoTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile(this, "test-src", "nativeimpl/functions/crypto-test.bal");
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
}
