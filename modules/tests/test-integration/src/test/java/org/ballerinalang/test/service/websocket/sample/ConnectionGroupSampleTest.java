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
import javax.net.ssl.SSLException;

/**
 * Test the scenarios of WebSocket connection groups.
 */
public class ConnectionGroupSampleTest extends WebSocketIntegrationTest {

    private final int threadSleepTime = 1000;
    private final int clientCount = 10;
    private final WebSocketClient[] clients = new WebSocketClient[clientCount];
    private ServerInstance ballerinaServer;

    @BeforeClass
    private void setup() throws Exception {
        ballerinaServer = ServerInstance.initBallerinaServer();
        String serviceSampleDir = ballerinaServer.getServerHome() + File.separator + Constant.SERVICE_SAMPLE_DIR;
        String balFile = serviceSampleDir + File.separator + "websocket" + File.separator + "connectionGroupSample"
                + File.separator + "oddEvenWebSocketSample.balx";
        ballerinaServer.startBallerinaServer(balFile);

        // Initializing WebSocket clients
        for (int i = 0; i < clientCount; i++) {
            clients[i] = new WebSocketClient("ws://localhost:9090/group/ws");
        }
    }

    @Test(priority = 0)
    public void testGroupTextMessages() throws InterruptedException, SSLException, URISyntaxException {
        handshakeAllClients(clients);
        String sentText = "test1";
        clients[0].sendText(sentText);
        Thread.sleep(threadSleepTime);
        for (int i = 0; i < clientCount; i++) {
            if (i % 2 == 0) {
                Assert.assertEquals(clients[i].getTextReceived(), "evenGroup: " + sentText);
            } else {
                Assert.assertEquals(clients[i].getTextReceived(), "oddGroup: " + sentText);
            }
        }
    }

    @Test(priority = 1)
    public void testSendTestToSpecificGroup() throws IOException, InterruptedException {
        String evenString = "hi even";
        String oddString = "hi odd";
        Map<String, String> headers = new HashMap<>();
        HttpClientRequest.doPost(ballerinaServer.getServiceURLHttp("groupInfo/even"), evenString, headers);
        HttpClientRequest.doPost(ballerinaServer.getServiceURLHttp("groupInfo/odd"), oddString, headers);
        Thread.sleep(threadSleepTime);
        for (int i = 0; i < clientCount; i++) {
            if (i % 2 == 0) {
                Assert.assertEquals(clients[i].getTextReceived(), evenString);
            } else {
                Assert.assertEquals(clients[i].getTextReceived(), oddString);
            }
        }
    }

    @Test(priority = 2)
    public void testRemoveConnectionFromGroup() throws IOException, InterruptedException {
        // Removing the connection
        clients[0].sendText("removeMe");
        Map<String, String> headers = new HashMap<>();
        String oddString = "hi even 0 removed";
        HttpClientRequest.doPost(ballerinaServer.getServiceURLHttp("groupInfo/odd"), oddString, headers);
        Thread.sleep(threadSleepTime);
        Assert.assertEquals(clients[0].getTextReceived(), null);
        for (int i = 1; i < clientCount; i++) {
            if (i % 2 == 0) {
                Assert.assertEquals(clients[i].getTextReceived(), null);
            } else {
                Assert.assertEquals(clients[i].getTextReceived(), oddString);
            }
        }
    }

    @Test(priority = 3)
    public void testRemoveGroup() throws InterruptedException, IOException {
        HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp("groupInfo/rm-odd"));

        String evenString = "hi even";
        String oddString = "hi odd";
        Map<String, String> headers = new HashMap<>();
        HttpClientRequest.doPost(ballerinaServer.getServiceURLHttp("groupInfo/even"), evenString, headers);
        HttpClientRequest.doPost(ballerinaServer.getServiceURLHttp("groupInfo/odd"), oddString, headers);

        Thread.sleep(threadSleepTime);

        Assert.assertEquals(clients[0].getTextReceived(), null);
        for (int i = 1; i < clientCount; i++) {
            if (i % 2 == 0) {
                Assert.assertEquals(clients[i].getTextReceived(), evenString);
            } else {
                Assert.assertEquals(clients[i].getTextReceived(), null);
            }
        }
    }


    @Test(priority = 4)
    public void testCloseGroup() throws InterruptedException, IOException {
        HttpClientRequest.doGet(ballerinaServer.getServiceURLHttp("groupInfo/close-even"));
        Thread.sleep(threadSleepTime);
        for (int i = 1; i < clientCount; i++) {
            if (i % 2 == 0) {
                Assert.assertFalse(clients[i].isOpen());
            } else {
                Assert.assertTrue(clients[i].isOpen());
            }
        }
        shutDownAllClients(clients);
    }

    @AfterClass
    private void cleanup() throws Exception {
        ballerinaServer.stopServer();
    }
}
