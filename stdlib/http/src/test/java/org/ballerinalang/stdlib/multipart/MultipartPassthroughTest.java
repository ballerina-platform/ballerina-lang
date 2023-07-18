/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.multipart;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.stdlib.utils.HTTPTestRequest;
import org.ballerinalang.stdlib.utils.MessageUtils;
import org.ballerinalang.stdlib.utils.ResponseReader;
import org.ballerinalang.stdlib.utils.Services;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * Test cases for multipart passthrough.
 */
public class MultipartPassthroughTest {

    private static final int TEST_SERVICE_PORT = 9092;

    @BeforeClass
    public void setup() {
        String sourceFilePath = "test-src/multipart/multipart-passthrough.bal";
        CompileResult result = BCompileUtil.compile(sourceFilePath);
        if (result.getErrorCount() > 0) {
            Assert.fail("Compilation errors");
        }
    }

    @Test(description = "Test multipart passthrough without consuming")
    public void testMultipartPassthroughWithoutConsuming() {
        String path = "/passthrough/process";
        String boundary = MimeUtil.getNewMultipartDelimiter();
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.CONTENT_TYPE.toString(),
                "multipart/form-data; boundary=" + boundary);
        String multipartBody = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"text\"\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "This is a text part\r\n" +
                "--" + boundary + "--\r\n";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_POST, headers,
                multipartBody);
        HttpCarbonMessage response = Services.invoke(TEST_SERVICE_PORT, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "This is a text part");
    }

    @Test(description = "Test multipart passthrough with consuming")
    public void testMultipartPassthroughWithConsuming() {
        String path = "/passthrough/consume";
        String boundary = MimeUtil.getNewMultipartDelimiter();
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.CONTENT_TYPE.toString(),
                "multipart/form-data; boundary=" + boundary);
        String multipartBody = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"text\"\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "This is a text part\r\n" +
                "--" + boundary + "--\r\n";
        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_POST, headers,
                multipartBody);
        HttpCarbonMessage response = Services.invoke(TEST_SERVICE_PORT, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "This is a text part");
    }

    @Test(description = "Test multipart passthrough with type parameter")
    public void testMultipartPassthroughWithTypeParam() {
        String path = "/passthrough/consume";
        String boundary = MimeUtil.getNewMultipartDelimiter();
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.CONTENT_TYPE.toString(),
                "multipart/form-data; boundary=" + boundary);
        String multipartBody = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"text\"\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "This is a text part\r\n" +
                "--" + boundary + "\r\n" +
                "Content-Type: application/xml; charset=UTF-8; type=\"text/xml\"\r\n" +
                "\r\n" +
                "<text>This is a xml part</text>\r\n" +
                "--" + boundary + "\r\n" +
                "Content-Type: application/json\r\n" +
                "\r\n" +
                "{ \"name\": \"This is a json part\" }\r\n" +
                "--" + boundary + "--\r\n";

        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_POST, headers,
                multipartBody);
        HttpCarbonMessage response = Services.invoke(TEST_SERVICE_PORT, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response),
                "This is a text part, <text>This is a xml part</text>, name=This is a json part");
    }

    @Test(description = "Test multipart passthrough with nested parts")
    public void testMultipartPassthroughWithNestedParts() {
        String path = "/passthrough/consume";
        String parentBoundary = MimeUtil.getNewMultipartDelimiter();
        String childBoundary = MimeUtil.getNewMultipartDelimiter();
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaderNames.CONTENT_TYPE.toString(),
                "multipart/form-data; boundary=" + parentBoundary);
        String multipartBody = "--" + parentBoundary + "\r\n" +
                "Content-Disposition: form-data; name=\"entity\"\r\n" +
                "Content-Type: multipart/mixed; boundary=" + childBoundary + "\r\n" +
                "\r\n" +
                "--" + childBoundary + "\r\n" +
                "Content-Disposition: form-data; name=\"text\"\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n" +
                "This is a nested text part\r\n" +
                "--" + childBoundary + "--\r\n" +
                "--" + parentBoundary + "--\r\n";

        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, HttpConstants.HTTP_METHOD_POST, headers,
                multipartBody);
        HttpCarbonMessage response = Services.invoke(TEST_SERVICE_PORT, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), "This is a nested text part");
    }
}
