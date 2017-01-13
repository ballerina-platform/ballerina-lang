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
package org.wso2.ballerina.test.service.http.sample;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerina.test.IntegrationTestCase;
import org.wso2.ballerina.test.util.HttpClientRequest;
import org.wso2.ballerina.test.util.HttpResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Testing the Echo service sample located in
 * ballerina_home/samples/echoService/echoService.bal
 */
public class EchoServiceSampleTestCase extends IntegrationTestCase {
    private static final String payload = "{\"exchange\":\"nyse\",\"name\":\"WSO2\",\"value\":\"127.50\"}";

    @Test(description = "Test echo service sample test case invoking base path")
    public void testEchoServiceByBasePath() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        HttpResponse response = HttpClientRequest.doPost(getServiceURLHttp("echo"), payload, headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get("Content-Type"), "application/json"
                , "Content-Type mismatched");
        Assert.assertEquals(response.getData(), "{\"exchange\":\"nyse\",\"name\":\"WSO2\",\"value\":\"127.50\"}"
                , "Message content mismatched");
    }

    @Test(description = "Test echo service sample test case")
    public void testEchoServiceByResourcePath() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        HttpResponse response = HttpClientRequest.doPost(getServiceURLHttp("echo/resource"), payload, headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get("Content-Type"), "application/json"
                , "Content-Type mismatched");
        Assert.assertEquals(response.getData(), "{\"exchange\":\"nyse\",\"name\":\"WSO2\",\"value\":\"127.50\"}"
                , "Message content mismatched");
    }
}
