/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;
import org.ballerinalang.test.service.http.HttpBaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Testcase for the retry sample.
 */
@Test(groups = "http-test")
public class RetrySampleTestCase extends HttpBaseTest {

    private final int servicePort = 9105;
    private final int statusCodeServicePort = 9225;
    private static final String RETRY_HEADER = "x-retry";

    @Test(description = "Test basic retry functionality")
    public void testSimpleRetry() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        HttpResponse response = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(servicePort, "retry"),
                "{\"Name\":\"Ballerina\"}", headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString()),
                TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        Assert.assertEquals(response.getData(), "Hello World!!!", "Message content mismatched");
    }

    @Test(description = "Test retry functionality with multipart requests")
    public void testMultiPart() throws IOException {
        String multipartDataBoundary = Long.toHexString(PlatformDependent.threadLocalRandom().nextLong());
        String multipartBody = "--" + multipartDataBoundary + "\r\n" +
                "Content-Disposition: form-data; name=\"foo\"" + "\r\n" +
                "Content-Type: text/plain; charset=UTF-8" + "\r\n" +
                "\r\n" +
                "Part1" +
                "\r\n" +
                "--" + multipartDataBoundary + "\r\n" +
                "Content-Disposition: form-data; name=\"filepart\"; filename=\"file-01.txt\"" + "\r\n" +
                "Content-Type: text/plain" + "\r\n" +
                "Content-Transfer-Encoding: binary" + "\r\n" +
                "\r\n" +
                "Part2" + StringUtil.NEWLINE +
                "\r\n" +
                "--" + multipartDataBoundary + "--" + "\r\n";
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), "multipart/form-data; boundary=" + multipartDataBoundary);
        HttpResponse response = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(servicePort, "retry"),
                multipartBody, headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertTrue(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                        .contains("multipart/form-data;boundary=" + multipartDataBoundary),
                "Response is not form of multipart");
        Assert.assertTrue(response.getData().contains("form-data;name=\"foo\"content-id: 0Part1"),
                "Message content mismatched");
        Assert.assertTrue(response.getData().
                        contains("form-data;name=\"filepart\";filename=\"file-01.txt\"content-id: 1Part2"),
                "Message content mismatched");
    }

    @Test(description = "Test retry functionality when request has nested body parts")
    public void testNestedMultiPart() throws IOException {
        String multipartDataBoundary = Long.toHexString(PlatformDependent.threadLocalRandom().nextLong());
        String multipartMixedBoundary = Long.toHexString(PlatformDependent.threadLocalRandom().nextLong());
        String nestedMultipartBody = "--" + multipartDataBoundary + "\r\n" +
                "Content-Disposition: form-data; name=\"parent1\"" + "\r\n" +
                "Content-Type: text/plain; charset=UTF-8" + "\r\n" +
                "\r\n" +
                "Parent Part" + "\r\n" +
                "--" + multipartDataBoundary + "\r\n" +
                "Content-Disposition: form-data; name=\"parent2\"" + "\r\n" +
                "Content-Type: multipart/mixed; boundary=" + multipartMixedBoundary + "\r\n" +
                "\r\n" +
                "--" + multipartMixedBoundary + "\r\n" +
                "Content-Disposition: attachment; filename=\"file-02.txt\"" + "\r\n" +
                "Content-Type: text/plain" + "\r\n" +
                "Content-Transfer-Encoding: binary" + "\r\n" +
                "\r\n" +
                "Child Part 1" + StringUtil.NEWLINE +
                "\r\n" +
                "--" + multipartMixedBoundary + "\r\n" +
                "Content-Disposition: attachment; filename=\"file-02.txt\"" + "\r\n" +
                "Content-Type: text/plain" + "\r\n" +
                "Content-Transfer-Encoding: binary" + "\r\n" +
                "\r\n" +
                "Child Part 2" + StringUtil.NEWLINE +
                "\r\n" +
                "--" + multipartMixedBoundary + "--" + "\r\n" +
                "--" + multipartDataBoundary + "--" + "\r\n";
        String expectedChildPart1 =
                "Content-Transfer-Encoding: binary" +
                        "content-type: text/plain" +
                        "content-disposition: attachment;filename=\"file-02.txt\"content-id: 0" +
                        "Child Part 1";
        String expectedChildPart2 = "Content-Transfer-Encoding: binary" +
                "content-type: text/plain" +
                "content-disposition: attachment;filename=\"file-02.txt\"content-id: 1" +
                "Child Part 2";
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), "multipart/form-data; boundary=" + multipartDataBoundary);
        HttpResponse response = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(servicePort, "retry"),
                nestedMultipartBody, headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertTrue(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                        .contains("multipart/form-data;boundary=" + multipartDataBoundary),
                "Response is not form of multipart");
        Assert.assertTrue(response.getData().contains(expectedChildPart1), "Message content mismatched");
        Assert.assertTrue(response.getData().contains(expectedChildPart2), "Message content mismatched");
    }

    @Test(description = "Test retry functionality based on HTTP status codes")
    public void testRetryBasedOnHttpStatusCodes() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(RETRY_HEADER, "recover");
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        HttpResponse response = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(statusCodeServicePort,
                "retry"), "{\"Name\":\"Ballerina\"}", headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString()),
                TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        Assert.assertEquals(response.getData(), "Hello World!!!", "Message content mismatched");
    }

    @Test(description = "Test continuous 502 response code")
    public void testRetryBasedOnHttpStatusCodesContinuousFailure() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(RETRY_HEADER, "internalError");
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        HttpResponse response = HttpClientRequest.doPost(serverInstance.getServiceURLHttp(statusCodeServicePort,
                "retry"), "{\"Name\":\"Ballerina\"}", headers);
        Assert.assertEquals(response.getResponseCode(), 502, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString()),
                TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        Assert.assertEquals(response.getData(), "Gateway Timed out.", "Message content mismatched");
    }
}
