package org.ballerinalang.test.channel;

import org.ballerinalang.test.context.ServerInstance;
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
public class ChannelsResourcesTest {

    private ServerInstance ballerinaServer;

    @BeforeClass
    private void setup() throws Exception {

        ballerinaServer = ServerInstance.initBallerinaServer();
        String balFile = new File("src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "channels" + File.separator + "channel-service.bal")
                .getAbsolutePath();
        ballerinaServer.startBallerinaServer(balFile);
    }

    @Test(description = "Test channel message send before receiving")
    public void testSendBeforeReceive() throws IOException {

        HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(
                "channelService/sendChannelMessage"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched in channel send request");
        response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(
                "channelService/receiveChannelMessage"));
        Assert.assertEquals(response.getData(), "{\"message\":\"channel_message\"}");
    }

    @Test(description = "Test channel message receive before send")
    public void testReceiveBeforeSend() throws IOException, ExecutionException, InterruptedException {

        CompletableFuture<HttpResponse> future = CompletableFuture.supplyAsync(() -> {
            try {
                return HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(
                        "channelService/receiveChannelMessage"));
            } catch (IOException e) {
                Assert.fail("Channel send failed", e);
            }
            return null;
        });

        HttpResponse sendResponse = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(
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
            HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(
                    "channelService/sendChannelMessage"));
        }

        //receive 3 of them
        for (int i = 0; i < 3; i++) {
            HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(
                    "channelService/receiveChannelMessage"));
            Assert.assertEquals(response.getData(), "{\"message\":\"channel_message\"}");

        }

        //send 3 more
        for (int i = 0; i < 3; i++) {
            HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(
                    "channelService/sendChannelMessage"));
        }

        //receive rest of the 5
        for (int i = 0; i < 5; i++) {
            HttpResponse response = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(
                    "channelService/receiveChannelMessage"));
            Assert.assertEquals(response.getData(), "{\"message\":\"channel_message\"}");

        }
    }

    @AfterClass
    private void cleanup() throws Exception {

        ballerinaServer.stopServer();
    }

}
