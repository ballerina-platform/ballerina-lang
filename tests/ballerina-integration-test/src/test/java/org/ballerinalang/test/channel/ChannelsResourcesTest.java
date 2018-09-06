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

package org.ballerinalang.test.channel;

import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.context.BServerInstance;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Integration test for Channels.
 *
 * @since 0.982.0
 */
public class ChannelsResourcesTest extends BaseTest {

    private static BServerInstance serverInstance;
    private static final int SERVICE_PORT = 9600;


    @BeforeClass
    private void setup() throws Exception {

        serverInstance = new BServerInstance(balServer);
        String balFile = new File("src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "channels" + File.separator + "channel-service.bal")
                .getAbsolutePath();
        serverInstance.startServer(balFile, new int[SERVICE_PORT]);
    }

    @Test(description = "Test channel message send before receiving")
    public void testSendBeforeReceive() throws IOException {

        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(SERVICE_PORT,
                "channelService/sendChannelMessage"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched in channel send request");
        response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(SERVICE_PORT,
                "channelService/receiveChannelMessage"));
        Assert.assertEquals(response.getData(), "{\"message\":\"channel_message\"}");
    }

    @Test(description = "Test channel message receive before send")
    public void testReceiveBeforeSend() throws IOException, ExecutionException, InterruptedException {

        CompletableFuture<HttpResponse> future = CompletableFuture.supplyAsync(() -> {
            try {
                return HttpClientRequest.doGet(serverInstance.getServiceURLHttp(SERVICE_PORT,
                        "channelService/receiveChannelMessage"));
            } catch (IOException e) {
                Assert.fail("Channel send failed", e);
            }
            return null;
        });

        HttpResponse sendResponse = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(SERVICE_PORT,
                "channelService/sendChannelMessage"));
        Assert.assertEquals(sendResponse.getResponseCode(), 200, "Response code mismatched in channel send " +
                "request");
        HttpResponse receiveResponse = future.get();
        if (receiveResponse != null) {
            Assert.assertEquals(receiveResponse.getResponseCode(), 200, "Response code mismatched in channel receive " +
                    "request");
            Assert.assertEquals(receiveResponse.getData(), "{\"message\":\"channel_message\"}");
        }

    }

    @Test(description = "Test channel message multiple interactions")
    public void testMultipleInteractions() throws IOException {

        //send 5 send requests
        for (int i = 0; i < 5; i++) {
            HttpClientRequest.doGet(serverInstance.getServiceURLHttp(SERVICE_PORT,
                    "channelService/sendChannelMessage"));
        }

        //receive 3 of them
        for (int i = 0; i < 3; i++) {
            HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(SERVICE_PORT,
                    "channelService/receiveChannelMessage"));
            Assert.assertEquals(response.getData(), "{\"message\":\"channel_message\"}");

        }

        //send 3 more
        for (int i = 0; i < 3; i++) {
            HttpClientRequest.doGet(serverInstance.getServiceURLHttp(SERVICE_PORT,
                    "channelService/sendChannelMessage"));
        }

        //receive rest of the 5
        for (int i = 0; i < 5; i++) {
            HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(SERVICE_PORT,
                    "channelService/receiveChannelMessage"));
            Assert.assertEquals(response.getData(), "{\"message\":\"channel_message\"}");

        }
    }

    @AfterClass
    private void cleanup() throws Exception {

        serverInstance.removeAllLeechers();
        serverInstance.shutdownServer();
    }

}
