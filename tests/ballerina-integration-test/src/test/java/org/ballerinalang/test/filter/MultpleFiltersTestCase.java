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
package org.ballerinalang.test.filter;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for verifying multiple http filters for a service.
 */
@Test(groups = "filter-test")
public class MultpleFiltersTestCase extends FilterBaseTest {

    @Test(description = "Single filter execution success case")
    public void testMultipleFiltersSuccess() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9092, "echo/test"), headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
    }

    @Test(description = "Single filter execution failure case")
    public void testMultipleFiltersFailureFromLast() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9094, "echo/test"), headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 403, "Response code mismatched");
    }

    @Test(description = "Single filter execution failure case")
    public void testMultipleFiltersFailureFromMiddle() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9091, "echo/test"), headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 405, "Response code mismatched");
    }

    @Test(description = "Multiple filter attribute sharing test")
    public void testMultipleFiltersContextSharingTest() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9090, "echo/test"),
                headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
    }

    @Test(description = "Single response filter execution failure case")
    public void testMultipleResponseFiltersFailure() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(9093, "echo/test"),
                headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 500, "Response code mismatched");
    }
}
