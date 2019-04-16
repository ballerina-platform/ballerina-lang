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

import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * TestCases for package ballerina/http.
 */
public class NetURITest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/nativeimpl/functions/net-uri.bal");
    }

    @Test
    public void testEncode() {
        BValue[] args = {
                new BString("http://localhost:9090"),
                new BString("http://localhost:9090/echoService/hello world/"),
                new BString("http://localhost:9090/echoService?type=string&value=hello world"),
                new BString("http://localhost:9090/echoService#abc"),
                new BString("http://localhost:9090/echoService:abc"),
                new BString("http://localhost:9090/echoService+abc"),
                new BString("http://localhost:9090/echoService*abc"),
                new BString("http://localhost:9090/echoService%abc"),
                new BString("http://localhost:9090/echoService~abc"),
        };

        for (BValue arg : args) {
            BValue[] inputArg = {arg};
            BValue[] returnVals = BRunUtil.invoke(compileResult, "testEncode", inputArg);
            Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                    "Invalid Return Values for " + arg.stringValue());
            Assert.assertFalse(returnVals[0].stringValue().contains(" "), "Encoded valued can't contain space.");
            Assert.assertFalse(returnVals[0].stringValue().contains("*"), "Encoded valued can't contain *.");
            Assert.assertFalse(returnVals[0].stringValue().contains("+"), "Encoded valued can't contain +.");
            Assert.assertFalse(returnVals[0].stringValue().contains("%7E"), "Encoded valued can't contain %7E.");
        }
    }

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testEncodeNegative() {
        BValue[] inputArg = { new BString(null) };
        BRunUtil.invoke(compileResult, "testEncode", inputArg);
    }

    @Test(description = "Test url encode function with invalid character set in ballerina/http package")
    public void testUrlEncodeWithInvalidCharset() {
        BString url = new BString("http://localhost:9090/echoService#abc");
        BString expected = new BString("Error occurred while encoding the url. abc");
        BValue[] inputArg = { url };
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testInvalidEncode", inputArg);
        Assert.assertTrue(returnVals[0].stringValue().contains(expected.stringValue()),
                "Error message is not propagated.");
    }

    @Test(description = "Test url decode function against simple url in ballerina/http package")
    public void testSimpleUrlDecode() {
        BString url = new BString("http%3A%2F%2Flocalhost%3A9090");
        BString expected = new BString("http://localhost:9090");
        BValue[] inputArg = { url };
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testDecode", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values for " + url.stringValue());
        Assert.assertEquals(returnVals[0].stringValue(), expected.stringValue(), "Decoded url string is not correct.");
    }

    @Test(description = "Test url decode function against url with spaces in ballerina/http package")
    public void testUrlDecodeWithSpaces() {
        BString url = new BString("http%3A%2F%2Flocalhost%3A9090%2FechoService%2Fhello%20world%2F");
        BString expected = new BString("http://localhost:9090/echoService/hello world/");
        BValue[] inputArg = { url };
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testDecode", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values for " + url.stringValue());
        Assert.assertEquals(returnVals[0].stringValue(), expected.stringValue(), "Decoded url string is not correct.");
        Assert.assertTrue(returnVals[0].stringValue().contains(" "), "Decoded url string doesn't contain spaces.");
    }

    @Test(description = "Test url decode function against url with # in ballerina/http package")
    public void testUrlDecodeWithHashSign() {
        BString url = new BString("http%3A%2F%2Flocalhost%3A9090%2FechoService%23abc");
        BString expected = new BString("http://localhost:9090/echoService#abc");
        BValue[] inputArg = { url };
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testDecode", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values for " + url.stringValue());
        Assert.assertEquals(returnVals[0].stringValue(), expected.stringValue(), "Decoded url string is not correct.");
        Assert.assertTrue(returnVals[0].stringValue().contains("#"), "Decoded url string doesn't contain # character.");
    }

    @Test(description = "Test url decode function against url with colon(:) in ballerina/http package")
    public void testUrlDecodeWithColon() {
        BString url = new BString("http%3A%2F%2Flocalhost%3A9090%2FechoService%3Aabc");
        BString expected = new BString("http://localhost:9090/echoService:abc");
        BValue[] inputArg = { url };
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testDecode", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values for " + url.stringValue());
        Assert.assertEquals(returnVals[0].stringValue(), expected.stringValue(), "Decoded url string is not correct.");
        Assert.assertTrue(returnVals[0].stringValue().contains(":"), "Decoded url string doesn't contain : character.");
    }

    @Test(description = "Test url decode function against url with plus(+) in ballerina/http package")
    public void testUrlDecodeWithPlusSign() {
        BString url = new BString("http%3A%2F%2Flocalhost%3A9090%2FechoService%2Babc");
        BString expected = new BString("http://localhost:9090/echoService+abc");
        BValue[] inputArg = { url };
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testDecode", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values for " + url.stringValue());
        Assert.assertEquals(returnVals[0].stringValue(), expected.stringValue(), "Decoded url string is not correct.");
        Assert.assertTrue(returnVals[0].stringValue().contains(":"), "Decoded url string doesn't contain + character.");
    }

    @Test(description = "Test url decode function against url with asterisk(*) in ballerina/http package")
    public void testUrlDecodeWithAsterisk() {
        BString url = new BString("http%3A%2F%2Flocalhost%3A9090%2FechoService%2Aabc");
        BString expected = new BString("http://localhost:9090/echoService*abc");
        BValue[] inputArg = { url };
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testDecode", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values for " + url.stringValue());
        Assert.assertEquals(returnVals[0].stringValue(), expected.stringValue(), "Decoded url string is not correct.");
        Assert.assertTrue(returnVals[0].stringValue().contains(":"), "Decoded url string doesn't contain * character.");
    }

    @Test(description = "Test url decode function against url with percentage(%) in ballerina/http package")
    public void testUrlDecodeWithPercentageMark() {
        BString url = new BString("http%3A%2F%2Flocalhost%3A9090%2FechoService%25abc");
        BString expected = new BString("http://localhost:9090/echoService%abc");
        BValue[] inputArg = { url };
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testDecode", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values for " + url.stringValue());
        Assert.assertEquals(returnVals[0].stringValue(), expected.stringValue(), "Decoded url string is not correct.");
        Assert.assertTrue(returnVals[0].stringValue().contains(":"), "Decoded url string doesn't contain % mark.");
    }

    @Test(description = "Test url decode function against url with tilde(~) in ballerina/http package")
    public void testUrlDecodeWithTilde() {
        BString url = new BString("http%3A%2F%2Flocalhost%3A9090%2FechoService~abc");
        BString expected = new BString("http://localhost:9090/echoService~abc");
        BValue[] inputArg = { url };
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testDecode", inputArg);
        Assert.assertFalse(returnVals == null || returnVals.length == 0 || returnVals[0] == null,
                "Invalid Return Values for " + url.stringValue());
        Assert.assertEquals(returnVals[0].stringValue(), expected.stringValue(), "Decoded url string is not correct.");
        Assert.assertTrue(returnVals[0].stringValue().contains(":"), "Decoded url string doesn't contain ~ character.");
    }

    @Test(description = "Test url decode function with invalid character set in ballerina/http package")
    public void testUrlDecodeWithInvalidCharset() {
        BString url = new BString("http%3A%2F%2Flocalhost%3A9090%2FechoService~abc");
        BString expected = new BString("Error occurred while decoding the url. abc");
        BValue[] inputArg = { url };
        BValue[] returnVals = BRunUtil.invoke(compileResult, "testInvalidDecode", inputArg);
        Assert.assertTrue(returnVals[0].stringValue().contains(expected.stringValue()),
                "Error message is not propagated.");
    }

}
