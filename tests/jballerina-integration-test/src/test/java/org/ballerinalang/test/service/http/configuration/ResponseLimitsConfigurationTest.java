/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.service.http.configuration;

import org.ballerinalang.test.service.http.HttpBaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test case for services with inbound response Limit configurations.
 *
 * @since 1.2.11
 */
@Test(groups = "http-test")
public class ResponseLimitsConfigurationTest extends HttpBaseTest {

    private static final String X_TEST_TYPE = "x-test-type";
    private static final String ERROR = "error";
    private static final String SUCCESS = "success";

    @Test(description = "Test when status line length is less than the configured maxStatusLineLength threshold")
    public void testValidStatusLineLength() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(X_TEST_TYPE, SUCCESS);
        HttpResponse response = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(9261, "responseLimit/statusline"), headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "Hello World!!!", "Message content mismatched");
        Assert.assertEquals(response.getResponseMessage(), "HELLO", "Phrase content mismatched");
    }

    @Test(description = "Test when status line length is greater than the configured maxStatusLineLength threshold")
    public void testInvalidStatusLineLength() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(X_TEST_TYPE, ERROR);
        HttpResponse response = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(9261, "responseLimit/statusline"), headers);
        Assert.assertEquals(response.getResponseCode(), 500, "Response code mismatched");
        Assert.assertEquals(response.getData(), "error {ballerina/http}GenericClientError message=Response max " +
                "status line length exceeds: An HTTP line is larger than 1024 bytes.", "Message content mismatched");
    }

    @Test(description = "Test when header size is less than the configured maxHeaderSize threshold")
    public void testValidHeaderLength() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(X_TEST_TYPE, SUCCESS);
        HttpResponse response = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(9261, "responseLimit/header"), headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "Hello World!!!", "Message content mismatched");
        Assert.assertEquals(response.getHeaders().get("x-header"), "Validated", "Header content mismatched");
    }

    @Test(description = "Test when header size is greater than the configured maxHeaderSize threshold")
    public void testInvalidHeaderLength() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(X_TEST_TYPE, ERROR);
        HttpResponse response = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(9261,"responseLimit/header"), headers);
        Assert.assertEquals(response.getResponseCode(), 500, "Response code mismatched");
        Assert.assertEquals(response.getData(), "error {ballerina/http}GenericClientError message=Response max " +
                "header size exceeds: HTTP header is larger than 1024 bytes.", "Header content mismatched");
        Assert.assertNull(response.getHeaders().get("x-header"), "Message content mismatched");
    }

    @Test(description = "Test when entityBody size is less than the configured maxEntityBodySize threshold")
    public void testValidEntityBodyLength() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(X_TEST_TYPE, SUCCESS);
        HttpResponse response = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(9261, "responseLimit/entitybody"), headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "Small payload", "Message content mismatched");
    }

    @Test(description = "Test when entityBody size is greater than the configured maxEntityBodySize threshold")
    public void testInvalidEntityBodyLength() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(X_TEST_TYPE, ERROR);
        HttpResponse response = HttpClientRequest.doGet(
                serverInstance.getServiceURLHttp(9261, "responseLimit/entitybody"), headers);
        Assert.assertEquals(response.getResponseCode(), 500, "Response code mismatched");
        Assert.assertEquals(response.getData(), "error {ballerina/http}GenericClientError message=Response max " +
                                    "entity body size exceeds: Entity body is larger than 1024 bytes. ",
                            "Message content mismatched");
    }
}
