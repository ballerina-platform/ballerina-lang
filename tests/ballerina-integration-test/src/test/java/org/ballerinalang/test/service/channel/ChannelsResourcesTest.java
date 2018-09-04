package org.ballerinalang.test.service.channel;

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
    public void testReceivebeforeSend() throws IOException {

        HttpResponse receiveResponse = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(
                "channelService/receiveChannelMessage"));
        Assert.assertEquals(receiveResponse.getResponseCode(), 200, "Response code mismatched in channel send request");
        HttpResponse sendResponse = HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp(
                "channelService/sendChannelMessage"));
        Assert.assertEquals(receiveResponse.getData(), "{\"message\":\"channel_message\"}");

    }

    public void testMultipleChannels() {

    }

    public void testMultipleInteractions() {

    }

    public void testNullKeys() {

    }

    @AfterClass
    private void cleanup() throws Exception {

        ballerinaServer.stopServer();
    }

}
