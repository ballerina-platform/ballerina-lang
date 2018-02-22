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

import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * Test class to validate HTTP service configs.
 */
public class HttpServiceConfigTestCase {
    private ServerInstance ballerinaServer;

    @BeforeClass
    private void setup() throws Exception {
        ballerinaServer = ServerInstance.initBallerinaServer();
        String relativePath = new File("src" + File.separator + "test" + File.separator + "resources"
                                               + File.separator + "httpService" + File.separator +
                                               "httpServiceConfigTest.bal").getAbsolutePath();
        ballerinaServer.startBallerinaServer(relativePath);
    }

    @Test(description = "Test config compressionEnabled true")
    public void testOptionCompressionEnabled() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(TestConstant.HEADER_ACCEPT_ENCODING, TestConstant.ENCODING_GZIP);
        String serviceUrl = "http://localhost:9090/compressEnabled";
        HttpResponse response = HttpClientRequest.doGet(serviceUrl, headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(TestConstant.HEADER_CONTENT_ENCODING), "gzip",
                            "Content-Encoding header value mismatched");
        String respMsg = "\u001F�\b\u0000\u0000\u0000\u0000\u0000\u0000\u0000�H���W" +
                "(�/�I\u0001\u0000\u0000\u0000��\u0003\u0000�\u0011J\u000B\u0000\u0000\u0000";
        Assert.assertEquals(response.getData(), respMsg, "Message content mismatched");
    }

    @Test(description = "Test config compressionEnabled false")
    public void testOptionCompressionDisabled() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(TestConstant.HEADER_ACCEPT_ENCODING, TestConstant.ENCODING_GZIP);
        String serviceUrl = "http://localhost:9090/compressDisabled";
        HttpResponse response = HttpClientRequest.doGet(serviceUrl, headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(TestConstant.HEADER_CONTENT_ENCODING),
                            TestConstant.HTTP_TRANSFER_ENCODING_IDENTITY,
                            "Content-Encoding header value mismatched");
        String respMsg = "hello world";
        Assert.assertEquals(response.getData(), respMsg, "Message content mismatched");
    }

    @AfterClass
    private void cleanup() throws Exception {
        ballerinaServer.stopServer();
    }
}
