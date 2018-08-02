/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
 * Test HTTP client actions with direct payload.
 */
public class HTTPClientActionsTestCase extends IntegrationTestCase {
    private ServerInstance ballerinaServer;
    String balFile = new File("src" + File.separator + "test" + File.separator + "resources"
            + File.separator + "httpService" + File.separator + "http_client_actions.bal").getAbsolutePath();

    @BeforeClass
    private void setup() throws Exception {
        ballerinaServer = ServerInstance.initBallerinaServer();
        ballerinaServer.startBallerinaServer(balFile);
    }

    @Test
    public void testGetAction() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp("test2/clientGet"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
        Assert.assertEquals(response.getData(), "HelloHelloHello", "Message content mismatched");
    }

    @Test
    public void testPostAction() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(
                "test2/clientPostWithoutBody"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "Entity body is not text compatible since the received " +
                "content-type is : null", "Message content mismatched");
    }

    @Test
    public void testPostActionWithBody() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(
                "test2/clientPostWithBody"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "Sample TextSample Xml{\"name\":\"apple\", \"color\":\"red\"}",
                "Message content mismatched");
    }

    @Test
    public void testPostWithBlob() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(
                "test2/handleBinary"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "Sample Text", "Message content mismatched");
    }

    @Test
    public void testPostWithByteChannel() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        HttpResponse response = HttpClientRequest.doPost(ballerinaServer.getServiceURLHttp(
                "test2/handleByteChannel"), "Sample Text", headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "Sample Text", "Message content mismatched");
    }

    @Test
    public void testPostWithBodyParts() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(
                "test2/handleMultiparts"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "{\"name\":\"wso2\"}Hello", "Message content mismatched");
    }

    @AfterClass
    private void cleanup() throws Exception {
        ballerinaServer.stopServer();
    }
}
