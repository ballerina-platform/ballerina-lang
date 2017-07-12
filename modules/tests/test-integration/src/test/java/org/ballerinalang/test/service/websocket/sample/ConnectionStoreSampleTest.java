package org.ballerinalang.test.service.websocket.sample;

import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.websocket.client.WebSocketClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test all the scenarios of WebSocket connection store.
 */
public class ConnectionStoreSampleTest extends WebSocketIntegrationTest {

    private final int threadSleepTime = 500;
    private final int clientCount = 5;
    private final WebSocketClient[] webSocketClients = new WebSocketClient[clientCount];
    private ServerInstance ballerinaServer;

    @BeforeClass
    private void setup() throws Exception {
        ballerinaServer = ServerInstance.initBallerinaServer();
        String serviceSampleDir = ballerinaServer.getServerHome() + File.separator + Constant.SERVICE_SAMPLE_DIR;
        String balFile = serviceSampleDir + File.separator + "websocket" + File.separator + "connectionStoreSample"
                + File.separator + "storeConnection.balx";
        ballerinaServer.startBallerinaServer(balFile);
    }

    {
        for (int i = 0; i < 5; i++) {
            webSocketClients[i] = new WebSocketClient("ws://localhost:9090/store/ws");
        }
    }

    @Test(priority = 0)
    public void testSendTextToStoredClient() throws IOException, InterruptedException, URISyntaxException {
        handshakeAllClients(webSocketClients);
        String sentText = "hello ";
        Map<String, String> headers = new HashMap<>();
        HttpClientRequest.doPost(ballerinaServer.getServiceURLHttp("storeInfo/0"), sentText + "0", headers);
        HttpClientRequest.doPost(ballerinaServer.getServiceURLHttp("storeInfo/1"), sentText + "1", headers);
        HttpClientRequest.doPost(ballerinaServer.getServiceURLHttp("storeInfo/2"), sentText + "2", headers);
        HttpClientRequest.doPost(ballerinaServer.getServiceURLHttp("storeInfo/3"), sentText + "3", headers);
        HttpClientRequest.doPost(ballerinaServer.getServiceURLHttp("storeInfo/4"), sentText + "4", headers);
        Thread.sleep(threadSleepTime);
        for (int i = 0; i < clientCount; i++) {
            Assert.assertEquals(webSocketClients[i].getTextReceived(), sentText + i);
        }
    }

    @Test(priority = 1)
    public void testRemoveStoredClient() throws IOException, InterruptedException, URISyntaxException {
        HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp("storeInfo/rm/0"));
        Thread.sleep(threadSleepTime);
        Map<String, String> headers = new HashMap<>();
        HttpClientRequest.doPost(ballerinaServer.getServiceURLHttp("storeInfo/0"), "You are out", headers);
        Assert.assertEquals(webSocketClients[0].getTextReceived(), null);
    }

    //TODO: disabled due to intermittent build failure
    @Test(priority = 2, enabled = false)
    public void testCloseConnection() throws IOException, InterruptedException {
        HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp("storeInfo/close/1"));
        Assert.assertFalse(webSocketClients[1].isOpen());
        shutDownAllClients(webSocketClients);
    }

    @AfterClass
    private void cleanup() throws Exception {
        ballerinaServer.stopServer();
    }
}
