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

import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.HttpsClientRequest;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Testing the Echo service sample located in
 * ballerina_home/samples/echoService/echoService.bal.
 * Request message should be returned as response message
 */
public class EchoServiceSampleTestCase extends IntegrationTestCase {
    private final String requestMessage = "{\"exchange\":\"nyse\",\"name\":\"WSO2\",\"value\":\"127.50\"}";

    @Test(description = "Test echo service sample test case invoking base path")
    public void testEchoServiceByBasePath() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(TestConstant.HEADER_CONTENT_TYPE, TestConstant.CONTENT_TYPE_JSON);
        HttpResponse response = HttpClientRequest.doPost(getServiceURLHttp("echo"), requestMessage, headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(TestConstant.HEADER_CONTENT_TYPE)
                , TestConstant.CONTENT_TYPE_JSON, "Content-Type mismatched");
        //request should be returned as response
        Assert.assertEquals(response.getData(), requestMessage, "Message content mismatched");
    }

    @Test(description = "Test echo service with dynamic port sample test case")
    public void testEchoServiceWithDynamicPortByBasePath() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(TestConstant.HEADER_CONTENT_TYPE, TestConstant.CONTENT_TYPE_JSON);
        String serviceUrl = "http://localhost:9094/echo";
        HttpResponse response = HttpClientRequest.doPost(serviceUrl, requestMessage, headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(TestConstant.HEADER_CONTENT_TYPE)
                , TestConstant.CONTENT_TYPE_JSON, "Content-Type mismatched");
        //request should be returned as response
        Assert.assertEquals(response.getData(), requestMessage, "Message content mismatched");
    }

    @Test(description = "Test echo service with dynamic port and scheme https")
    public void testEchoServiceWithDynamicPortHttpsByBasePath() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(TestConstant.HEADER_CONTENT_TYPE, TestConstant.CONTENT_TYPE_JSON);
        String serviceUrl = "https://localhost:9095/echo";
        String serverHome = getServerInstance().getServerHome();
        HttpResponse response = HttpsClientRequest.doPost(serviceUrl, requestMessage, headers, serverHome);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(TestConstant.HEADER_CONTENT_TYPE)
                , TestConstant.CONTENT_TYPE_JSON, "Content-Type mismatched");
        //request should be returned as response
        Assert.assertEquals(response.getData(), requestMessage, "Message content mismatched");
    }


}
