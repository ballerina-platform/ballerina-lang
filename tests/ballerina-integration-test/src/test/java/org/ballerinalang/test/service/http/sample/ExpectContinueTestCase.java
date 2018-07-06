/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.service.http.sample;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.TestUtils;
import org.ballerinalang.test.util.client.HttpClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.List;

/**
 * Test case for verifying the server-side 100-continue behaviour.
 */
public class ExpectContinueTestCase {

    private ServerInstance ballerinaServer;

    @BeforeClass
    public void setup() throws Exception {
        ballerinaServer = ServerInstance.initBallerinaServer();
        String resourceRoot = getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        String balFile = Paths.get(resourceRoot, "httpService", "100_continue.bal").toAbsolutePath().toString();
        ballerinaServer.startBallerinaServer(balFile);
    }

    @Test
    public void test100Continue() {
        HttpClient httpClient = new HttpClient("localhost", 9090);

        DefaultHttpRequest reqHeaders = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/continue");
        DefaultLastHttpContent reqPayload = new DefaultLastHttpContent(
                Unpooled.wrappedBuffer(TestUtils.LARGE_ENTITY.getBytes()));

        reqHeaders.headers().set(HttpHeaderNames.CONTENT_LENGTH, TestUtils.LARGE_ENTITY.getBytes().length);
        reqHeaders.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);

        List<FullHttpResponse> responses = httpClient.sendExpectContinueRequest(reqHeaders, reqPayload);

        Assert.assertFalse(httpClient.waitForChannelClose());

        // 100-continue response
        Assert.assertEquals(responses.get(0).status(), HttpResponseStatus.CONTINUE);
        Assert.assertEquals(Integer.parseInt(responses.get(0).headers().get(HttpHeaderNames.CONTENT_LENGTH)), 0);

        // Actual response
        String responsePayload = TestUtils.getEntityBodyFrom(responses.get(1));
        Assert.assertEquals(responses.get(1).status(), HttpResponseStatus.OK);
        Assert.assertEquals(responsePayload, TestUtils.LARGE_ENTITY);
        Assert.assertEquals(responsePayload.getBytes().length, TestUtils.LARGE_ENTITY.getBytes().length);
        Assert.assertEquals(Integer.parseInt(responses.get(1).headers().get(HttpHeaderNames.CONTENT_LENGTH)),
                            TestUtils.LARGE_ENTITY.getBytes().length);
    }

    @AfterClass
    public void cleanup() throws Exception {
        ballerinaServer.stopServer();
    }
}
