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

import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * Testing the Http headers availability in pass-through scenarios.
 */
public class HttpHeaderTestCases extends IntegrationTestCase {
    private ServerInstance ballerinaServer;

    @BeforeClass
    private void setup() throws Exception {
        ballerinaServer = ServerInstance.initBallerinaServer();
        String balFile = new File("src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "httpService" + File.separator + "httpHeaderTest.bal").getAbsolutePath();
        ballerinaServer.startBallerinaServer(balFile);
    }

    @Test(description = "Test outbound request headers availability at backend with URL. /product/value")
    public void testOutboundRequestHeaders() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp("product/value"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "{\"header1\":\"aaa\",\"header2\":\"bbb\"}");
    }

    @Test(description = "Test inbound response headers availability with URL. /product/id")
    public void testInboundResponseHeaders() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp("product/id"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(), "{\"header1\":\"kkk\",\"header2\":\"jjj\"}");
    }

    @AfterClass
    private void cleanup() throws Exception {
        ballerinaServer.stopServer();
    }
}
