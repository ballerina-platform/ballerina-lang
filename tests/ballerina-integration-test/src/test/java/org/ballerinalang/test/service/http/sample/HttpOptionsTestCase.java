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
import org.ballerinalang.test.util.BaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test class for HTTP options request's content length and payload handling behavior.
 */
@Test(groups = "http-test")
public class HttpOptionsTestCase extends BaseTest {

    @Test(description = "Test OPTIONS content length header sample test case")
    public void testOptionsContentLengthHeader() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        String serviceUrl = "http://localhost:9100/echoDummy";
        HttpResponse response = HttpClientRequest.doOptions(serviceUrl, headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_LENGTH.toString()), "0",
                "Content-Length mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.ALLOW.toString())
                , "POST, OPTIONS", "Content-Length mismatched");
    }

    @Test(description = "Test OPTIONS content length header sample test case")
    public void testOptionsResourceWithPayload() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        String serviceUrl = "http://localhost:9100/echoDummy/getOptions";
        HttpResponse response = HttpClientRequest.doOptions(serviceUrl, headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_LENGTH.toString()),
                String.valueOf(response.getData().length()), "Content-Length mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        String respMsg = "hello Options";
        Assert.assertEquals(response.getData(), respMsg, "Message content mismatched");
    }
}
