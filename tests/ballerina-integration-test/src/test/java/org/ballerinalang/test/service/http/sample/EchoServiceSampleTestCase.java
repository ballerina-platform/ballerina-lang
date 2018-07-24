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
import org.ballerinalang.test.util.HttpsClientRequest;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Testing the Echo service sample located in
 * ballerina_home/samples/echoService/echoService.bal.
 * Request message should be returned as response message
 */
public class EchoServiceSampleTestCase extends IntegrationTestCase {
    private ServerInstance ballerinaServer;
    private final String requestMessage = "{\"exchange\":\"nyse\",\"name\":\"WSO2\",\"value\":\"127.50\"}";

    @Test(description = "Test echo service sample test case invoking base path")
    public void testEchoServiceByBasePath() throws Exception {
        try {
            String relativePath = new File("src" + File.separator + "test" + File.separator + "resources"
                    + File.separator + "httpService" + File.separator + "echo_service.bal").getAbsolutePath();
            startServer(relativePath);
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
            HttpResponse response = HttpClientRequest.doPost(ballerinaServer
                    .getServiceURLHttp("echo"), requestMessage, headers);
            Assert.assertNotNull(response);
            Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
            Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                    , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
            //request should be returned as response
            Assert.assertEquals(response.getData(), requestMessage, "Message content mismatched");
        } finally {
            ballerinaServer.stopServer();
        }
    }

    @Test(description = "Test echo service with dynamic port sample test case")
    public void testEchoServiceWithDynamicPortByBasePath() throws Exception {
        try {
            String relativePath = new File("src" + File.separator + "test" + File.separator + "resources"
                    + File.separator + "httpService" + File.separator + "http_echo_service.bal").getAbsolutePath();
            startServer(relativePath);
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
            String serviceUrl = "http://localhost:9094/echo";
            String requestMsg = "{\"key\":\"value\"}";
            HttpResponse response = HttpClientRequest.doPost(serviceUrl, requestMsg, headers);
            if (response == null) {
                //Retrying to avoid intermittent test failure
                response = HttpClientRequest.doPost(serviceUrl, requestMsg, headers);
            }
            Assert.assertNotNull(response);
            Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
            Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                    , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
            String respMsg = "hello world";
            Assert.assertEquals(response.getData(), respMsg, "Message content mismatched");
        } finally {
            ballerinaServer.stopServer();
        }

    }

    @Test(description = "Test echo service with dynamic port shared")
    public void testEchoServiceWithDynamicPortShared() throws Exception {
        try {
            String relativePath = new File("src" + File.separator + "test" + File.separator + "resources"
                    + File.separator + "httpService" + File.separator + "http_echo_service.bal").getAbsolutePath();
            startServer(relativePath);
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
            String serviceUrl = "http://localhost:9094/echoOne/abc";
            String requestMsg = "{\"key\":\"value\"}";
            HttpResponse response = HttpClientRequest.doPost(serviceUrl, requestMsg, headers);
            if (response == null) {
                //Retrying to avoid intermittent test failure
                response = HttpClientRequest.doPost(serviceUrl, requestMsg, headers);
            }
            Assert.assertNotNull(response);
            Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
            Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                    , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
            String respMsg = "hello world";
            Assert.assertEquals(response.getData(), respMsg, "Message content mismatched");
        } finally {
            ballerinaServer.stopServer();
        }

    }

    @Test(description = "Test echo service with dynamic port and scheme https")
    public void testEchoServiceWithDynamicPortHttpsByBasePath() throws Exception {
        try {
            String relativePath = new File("src" + File.separator + "test" + File.separator + "resources"
                    + File.separator + "httpService" + File.separator + "https_echo_service.bal").getAbsolutePath();
            startServer(relativePath);
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
            String serviceUrl = "https://localhost:9095/echo";
            String serverHome = getServerInstance().getServerHome();
            String requestMsg = "{\"key\":\"value\"}";
            HttpResponse response = HttpsClientRequest.doPost(serviceUrl, requestMsg, headers, serverHome);
            if (response == null) {
                //Retrying to avoid intermittent test failure
                response = HttpsClientRequest.doPost(serviceUrl, requestMsg, headers, serverHome);
            }
            Assert.assertNotNull(response);
            Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
            Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                    , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
            String respMsg = "hello world";
            Assert.assertEquals(response.getData(), respMsg, "Message content mismatched");
        } finally {
            ballerinaServer.stopServer();
        }

    }

    @Test(description = "Test echo service with dynamic port and scheme https with port shared")
    public void testEchoServiceWithDynamicPortHttpsShared() throws Exception {
        try {
            String relativePath = new File("src" + File.separator + "test" + File.separator + "resources"
                    + File.separator + "httpService" + File.separator + "https_echo_service.bal").getAbsolutePath();
            startServer(relativePath);
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_JSON);
            String serviceUrl = "https://localhost:9095/echoOne/abc";
            String serverHome = getServerInstance().getServerHome();
            String requestMsg = "{\"key\":\"value\"}";
            HttpResponse response = HttpsClientRequest.doPost(serviceUrl, requestMsg, headers, serverHome);
            if (response == null) {
                //Retrying to avoid intermittent test failure
                response = HttpsClientRequest.doPost(serviceUrl, requestMsg, headers, serverHome);
            }
            Assert.assertNotNull(response);
            Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
            Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString())
                    , TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
            String respMsg = "hello world";
            Assert.assertEquals(response.getData(), respMsg, "Message content mismatched");
        } finally {
            ballerinaServer.stopServer();
        }

    }

    @Test(description = "Test echo service sample test case invoking base path")
    public void testHttpImportAsAlias() throws Exception {
        try {
            String relativePath = new File("src" + File.separator + "test" + File.separator + "resources"
                    + File.separator + "httpService" + File.separator + "http_import_as_alias.bal").getAbsolutePath();
            startServer(relativePath);
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
            HttpResponse response = HttpClientRequest.doPost(ballerinaServer
                    .getServiceURLHttp("echo"), requestMessage, headers);
            Assert.assertNotNull(response);
            Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
            Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString()),
                    TestConstant.CONTENT_TYPE_TEXT_PLAIN, "Content-Type mismatched");
            //request should be returned as response
            Assert.assertEquals(response.getData(), requestMessage, "Message content mismatched");
        } finally {
            ballerinaServer.stopServer();
        }
    }

    private void startServer(String balFile) throws Exception {
        ballerinaServer = ServerInstance.initBallerinaServer();
        ballerinaServer.startBallerinaServer(balFile);
    }


}
