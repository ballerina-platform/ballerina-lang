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
import org.ballerinalang.model.util.StringUtils;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.IOConstants;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.nativeimpl.util.Base64ByteChannel;
import org.ballerinalang.nativeimpl.util.Base64Wrapper;
import org.ballerinalang.test.mime.Util;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Base64;

/**
 * Test Cases for ballerina.utils native functions.
 */
public class UtilTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/nativeimpl/functions/util-test.bal");
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
    public void testBase64EncodeString() {
        String expectedValue = "SGVsbG8gQmFsbGVyaW5h";
        BValue[] args = new BValue[]{new BString("Hello Ballerina")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testBase64EncodeString", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test
    public void testBase64EncodeBlob() {
        String expectedValue = "SGVsbG8gQmFsbGVyaW5h";
        BValue[] args = new BValue[]{new BBlob("Hello Ballerina".getBytes())};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testBase64EncodeBlob", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test
    public void testBase64EncodeByteChannel() throws IOException, URISyntaxException {
        String expectedValue = "SGVsbG8gQmFsbGVyaW5h";
        BStruct byteChannelStruct = Util.getByteChannelStruct(compileResult);
        InputStream inputStream = new ByteArrayInputStream("Hello Ballerina".getBytes());
        Base64ByteChannel base64ByteChannel = new Base64ByteChannel(inputStream);
        byteChannelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME, new Base64Wrapper(base64ByteChannel));
        BValue[] args = new BValue[]{byteChannelStruct};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testBase64EncodeByteChannel", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                "Invalid return value");
        BStruct decodedByteChannel = (BStruct) returnValues[0];
        Channel byteChannel = (Channel) decodedByteChannel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
        Assert.assertEquals(StringUtils.getStringFromInputStream(byteChannel.getInputStream()),
                expectedValue);
    }

    @Test
    public void testBase64DecodeString() throws IOException, URISyntaxException {
        String expectedValue = "Hello Ballerina";
        BValue[] args = new BValue[]{new BString("SGVsbG8gQmFsbGVyaW5h")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testBase64DecodeString", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
        Assert.assertFalse(returnValues[0] == null, "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test
    public void testBase64DecodeBlob() {
        String expectedValue = "Hello Ballerina";
        BValue[] args = new BValue[]{new BBlob("SGVsbG8gQmFsbGVyaW5h".getBytes())};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testBase64DecodeBlob", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test
    public void testBase64DecodeByteChannel() throws IOException, URISyntaxException {
        String expectedValue = "Hello Ballerina!";
        BStruct byteChannelStruct = Util.getByteChannelStruct(compileResult);
        byte[] encodedByteArray = Base64.getEncoder().encode(expectedValue.getBytes());
        InputStream encodedStream = new ByteArrayInputStream(encodedByteArray);
        Base64ByteChannel base64ByteChannel = new Base64ByteChannel(encodedStream);
        byteChannelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME, new Base64Wrapper(base64ByteChannel));
        BValue[] args = new BValue[]{byteChannelStruct};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testBase64DecodeByteChannel", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                "Invalid return value");
        BStruct decodedByteChannel = (BStruct) returnValues[0];
        Channel byteChannel = (Channel) decodedByteChannel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
        Assert.assertEquals(StringUtils.getStringFromInputStream(byteChannel.getInputStream()),
                expectedValue);
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
        BValue[] args = new BValue[]{new BString("Hello Ballerina"), new BString("abcdefghijk")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testHMACValueFromBase16ToBase64Encoding", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test
    public void testHMACValueFromBase64ToBase16Encoding() {
        String expectedValue = "4B8AA10B811DB0472F63AE20B3E26C98";
        BValue[] args = new BValue[]{new BString("Hello Ballerina"), new BString("abcdefghijk")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testHMACValueFromBase64ToBase16Encoding", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                "Invalid return value");
        Assert.assertEquals(returnValues[0].stringValue(), expectedValue);
    }

    @Test
    public void testParseJson() {
        String jsonString = "{\"name\":\"apple\",\"color\":\"red\",\"price\":25}";
        BValue[] args = new BValue[]{new BString(jsonString)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testParseJson", args);
        Assert.assertTrue(returns[0] instanceof BJSON);
    }

    @Test
    public void testParseInvalidJson() {
        String jsonString = "{\"name\":\"apple\",\"color\":\"red\",\"price\":25} sample invalid json";
        BValue[] args = new BValue[]{new BString(jsonString)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testParseJson", args);
        Assert.assertTrue(returns[0] instanceof BStruct);
        Assert.assertTrue(((BStruct) returns[0]).getStringField(0).contains("Failed to parse json string:"));
    }
}
