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
package org.ballerinalang.nativeimpl.functions;

import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test Cases for ballerina.utils native functions.
 */
public class BallerinaUtilsTest {

    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("samples/utilsTest.bal");
    }

    @Test
    public void testEncoding() {
        String[] stringsToTest = {"Hi", "`Welcome\" \r^to^\tBallerina\\ /$TestCases$\n # ~^!!!",
                "https://example.com/test/index.html#title1",
                "https://example.com/test/index.html?greeting=hello world&your-name=Ballerina Test cases",
                "バレリーナ日本語", "Ballerina 的中文翻译", "Traducción al español de  la bailarina"};

        for (String s : stringsToTest) {
            BValue[] returnVals = BLangFunctions.invoke(bLangProgram, "testEncodeDecode", new BValue[]{new BString(s)});
            Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                    "Invalid Return Values for :" + s);
            Assert.assertEquals(returnVals[0].stringValue(), s, "Original and Return value didn't match");
        }
    }

    @Test
    public void testRandomString() {
        BValue[] returnVals = BLangFunctions.invoke(bLangProgram, "testRandomString");
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values for");
    }

    @Test
    public void testHmac() {
        final String key = "abcdefghijk";
        List<BValue[]> argsList = new ArrayList<>();

        argsList.add(new BValue[]{new BString("Ballerina HMAC test"), new BString(key), new BString("SHA1")});
        argsList.add(new BValue[]{new BString("Ballerina HMAC test"), new BString(key), new BString("SHA256")});
        argsList.add(new BValue[]{new BString("Ballerina HMAC test"), new BString(key), new BString("MD5")});

        for (BValue[] args : argsList) {
            BValue[] returnVals = BLangFunctions.invoke(bLangProgram, "testHmac", args);
            Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                    "Invalid Return Values for");
        }
    }

    @Test(expectedExceptions = BallerinaException.class)
    public void testHmacNagativeInvalidAlgo() {
        final String key = "abcdefghijk";
        BValue[] returnVals = BLangFunctions.invoke(bLangProgram, "testHmac",
                new BValue[]{new BString("Ballerina HMAC test"), new BString(key), new BString("SHA124")});
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values for");

    }

    @Test(expectedExceptions = BallerinaException.class)
    public void testHmacNagativeInvalidKey() {
        final String key = "";
        BValue[] returnVals = BLangFunctions.invoke(bLangProgram, "testHmac",
                new BValue[]{new BString("Ballerina HMAC test"), new BString(key), new BString("SHA1")});
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values for");

    }

    @Test
    public void testHmacBase64() {
        final String key = "abcdefghijk";
        List<BValue[]> argsList = new ArrayList<>();

        argsList.add(new BValue[]{new BString("Ballerina HMAC BASE64 test"), new BString(key), new BString("SHA1")});
        argsList.add(new BValue[]{new BString("Ballerina HMAC BASE64 test"), new BString(key), new BString("SHA256")});
        argsList.add(new BValue[]{new BString("Ballerina HMAC BASE64 test"), new BString(key), new BString("MD5")});

        for (BValue[] args : argsList) {
            BValue[] returnVals = BLangFunctions.invoke(bLangProgram, "testHmacFromBase64", args);
            Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                               "Invalid Return Values for");
        }
    }

    @Test
    public void testMessageDigest() {
        List<BValue[]> argsList = new ArrayList<>();

        argsList.add(new BValue[]{new BString("Ballerina HMAC BASE64 test"), new BString("SHA1")});
        argsList.add(new BValue[]{new BString("Ballerina HMAC BASE64 test"), new BString("SHA256")});
        argsList.add(new BValue[]{new BString("Ballerina HMAC BASE64 test"), new BString("MD5")});

        for (BValue[] args : argsList) {
            BValue[] returnVals = BLangFunctions.invoke(bLangProgram, "testMessageDigest", args);
            Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                               "Invalid Return Values for");
        }
    }

    @Test
    public void testBase64toBase16Encode() {
        BValue[] args = new BValue[]{new BString("Ballerina HMAC BASE64 test")};
        BValue[] returnVals = BLangFunctions.invoke(bLangProgram, "testBase64ToBase16Encode", args);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                           "Invalid Return Values for");
    }

}
