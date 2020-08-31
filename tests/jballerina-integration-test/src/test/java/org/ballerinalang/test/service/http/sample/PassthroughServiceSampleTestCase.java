/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.service.http.sample;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.internal.StringUtil;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.test.service.http.HttpBaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.activation.MimeTypeParseException;

import static org.ballerinalang.mime.util.MimeConstants.MULTIPART_FORM_DATA;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Testing the passthrough service sample located in
 * ballerina_home/samples/passthroughService/passthroughService.bal.
 */
@Test(groups = "http-test")
public class PassthroughServiceSampleTestCase extends HttpBaseTest {
    private final String responseMessage = "{\"exchange\":\"nyse\", \"name\":\"IBM\", \"value\":\"127.50\"}";

    @Test(description = "Test Passthrough sample test case invoking base path")
    public void testPassthroughServiceByBasePath() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9113, "passthrough"));
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString()),
                TestConstant.CONTENT_TYPE_JSON, "Content-Type mismatched");
        assertEquals(response.getData(), responseMessage, "Message content mismatched");
    }

    @Test(description = "Test passthrough with a multipart request")
    public void testPassthroughWithMultiparts() throws IOException, MimeTypeParseException {
        String multipartDataBoundary = MimeUtil.getNewMultipartDelimiter();
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), "multipart/form-data; boundary=" + multipartDataBoundary);
        String multipartBody = "--" + multipartDataBoundary + "\r\n" +
                "Content-Type: text/plain; charset=UTF-8" + "\r\n" +
                "\r\n" +
                "Part1" +
                "\r\n" +
                "--" + multipartDataBoundary + "\r\n" +
                "Content-Type: text/plain" + "\r\n" +
                "\r\n" +
                "Part2" + StringUtil.NEWLINE +
                "\r\n" +
                "--" + multipartDataBoundary + "--" + "\r\n";
        HttpResponse response = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(9113,
                "passthrough/forwardMultipart"), multipartBody, headers);
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        assertTrue(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString()).contains(MULTIPART_FORM_DATA),
                "Content-Type mismatched");
        assertTrue(response.getData().contains("Part1"));
        assertTrue(response.getData().contains("Part2"));
    }

    @Test(description = "Test Passthrough with mime entity contents")
    public void testPassthroughServiceWithMimeEntity() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), "text/plain");
        HttpResponse response = HttpClientRequest.doPost(
                serverInstance.getServiceURLHttp(9113, "passthrough/forward"), "Hello from POST!", headers);
        assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString()),
                     TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        assertEquals(response.getData(), "payload :Hello from POST!, header: text/plain, entity-check-header");
    }
}
