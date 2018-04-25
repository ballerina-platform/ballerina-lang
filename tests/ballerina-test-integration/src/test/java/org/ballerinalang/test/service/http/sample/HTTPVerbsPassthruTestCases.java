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
import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Testing the passthrough service for HTTP methods
 */
public class HTTPVerbsPassthruTestCases extends IntegrationTestCase {
    private ServerInstance ballerinaServer;

    @BeforeClass
    private void setup() throws Exception {
        ballerinaServer = ServerInstance.initBallerinaServer();
        String balFile = new File("src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "httpService" + File.separator + "httpMethodTest.bal").getAbsolutePath();
        ballerinaServer.startBallerinaServer(balFile);
    }

    @Test(description = "Test simple passthrough test case For HEAD with URL. /sampleHead")
    public void testPassthroughSampleForHEAD() throws IOException {
        HttpResponse response = HttpClientRequest.doHead(ballerinaServer.getServiceURLHttp("sampleHead"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), null, "Message content mismatched");
    }

    @Test(description = "Test simple passthrough test case For GET with URL. /headQuote/default")
    public void testPassthroughSampleForGET() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp("headQuote/default"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "wso2", "Message content mismatched");
    }

    @Test(description = "Test simple passthrough test case For POST")
    public void testPassthroughSampleForPOST() throws IOException {
        Map<String, String> headers = new HashMap<>();
        HttpResponse response = HttpClientRequest.doPost(ballerinaServer.getServiceURLHttp("headQuote/default")
                , "test", headers);
        if (response == null) {
            //Retrying to avoid intermittent test failure
            response = HttpClientRequest.doPost(ballerinaServer.getServiceURLHttp("headQuote/default")
                    , "test", headers);;
        }
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "ballerina", "Message content mismatched");
    }

    @Test(description = "Test simple passthrough test case with default resource")
    public void testPassthroughSampleWithDefaultResource() throws IOException {
        HttpResponse response = HttpClientRequest.doHead(ballerinaServer.getServiceURLHttp("headQuote/default"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get("Method"), "any", "Header mismatched");
        Assert.assertEquals(response.getData(), null, "Message content mismatched");
    }

    @Test(description = "Test default resource for outbound PUT with URL. /headQuote/getStock/PUT")
    public void testOutboundPUT() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp("headQuote/getStock/PUT"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get("Method"), "any", "Header mismatched");
        Assert.assertEquals(response.getData(), "default", "Message content mismatched");
    }

    @Test(description = "Test simple passthrough test case with 'forward' For GET with URL. /headQuote/forward11")
    public void testForwardActionWithGET() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp("headQuote/forward11"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "wso2", "Message content mismatched");
    }

    @Test(description = "Test simple passthrough test case with 'forward' For POST with URL. /headQuote/forward22")
    public void testForwardActionWithPOST() throws IOException {
        HttpResponse response = HttpClientRequest.doPost(ballerinaServer.getServiceURLHttp("headQuote/forward22")
                , "test", new HashMap<>());
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "ballerina", "Message content mismatched");
    }

    @Test(description = "Test HTTP data binding with JSON payload with URL. /getQuote/employee")
    public void testDataBindingJsonPayload() throws IOException {
        String payload = "{\"name\":\"WSO2\",\"team\":\"ballerina\"}";
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
        HttpResponse response = HttpClientRequest.doPost(ballerinaServer.getServiceURLHttp("getQuote/employee")
                , payload, headers);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), payload);
    }

    @Test(description = "Test HTTP data binding with incompatible payload with URL. /getQuote/employee")
    public void testDataBindingWithIncompatiblePayload() throws IOException {
        String payload = "name:WSO2,team:ballerina";
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        HttpResponse response = HttpClientRequest.doPost(ballerinaServer.getServiceURLHttp("getQuote/employee")
                , payload, headers);

        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 400, "Response code mismatched");
        Assert.assertTrue(response.getData()
                .contains("data binding failed: Error in reading payload : failed to create json: unrecognized " +
                        "token 'name:WSO2,team:ballerina'"));
    }

    @AfterClass
    private void cleanup() throws Exception {
        ballerinaServer.stopServer();
    }
}
