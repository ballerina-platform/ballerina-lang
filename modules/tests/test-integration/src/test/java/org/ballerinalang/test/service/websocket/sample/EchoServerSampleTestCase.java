package org.ballerinalang.test.service.websocket.sample;

import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.util.websocket.client.WebSocketClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URISyntaxException;
import javax.net.ssl.SSLException;

/**
 * Test echo server sample located in ballerina_home/samples/websocket/echoServer/server/websocketEchoServer.bal
 * This sample echo the same message received to the client.
 */
public class EchoServerSampleTestCase extends WebSocketIntegrationTest {

    private final int threadSleepTime = 1000;
    private final int clientCount = 10;
    private final WebSocketClient[] wsClients = new WebSocketClient[clientCount];
    private ServerInstance ballerinaServer;

    @BeforeClass
    private void setup() throws Exception {
        ballerinaServer = ServerInstance.initBallerinaServer();
        String serviceSampleDir = ballerinaServer.getServerHome() + File.separator + Constant.SERVICE_SAMPLE_DIR;
        String balFile = serviceSampleDir + File.separator + "websocket" + File.separator + "echoServer"
                + File.separator + "server" + File.separator + "websocketEchoServer.bal";
        ballerinaServer.startBallerinaServer(balFile);

        // Initializing WebSocket clients
        for (int i = 0; i < clientCount; i++) {
            wsClients[i] = (new WebSocketClient("ws://localhost:9090/echo-server/ws"));
        }
    }

    @Test(priority = 0)
    public void testPushText() throws InterruptedException, SSLException, URISyntaxException {
        handshakeAllClients(wsClients);
        String sentText = "test";
        wsClients[0].sendText(sentText);
        Thread.sleep(threadSleepTime);
        Assert.assertEquals(wsClients[0].getTextReceived(), sentText);
        for (int i = 1; i < clientCount; i++) {
            Assert.assertEquals(null, wsClients[i].getTextReceived());
        }
        shutDownAllClients(wsClients);
    }

    @Test(priority = 1)
    public void testCloseConnection() throws InterruptedException, SSLException, URISyntaxException {
        handshakeAllClients(wsClients);
        String closeText = "closeMe";
        wsClients[0].sendText(closeText);
        Thread.sleep(threadSleepTime);
        Assert.assertFalse(wsClients[0].isOpen());
        shutDownAllClients(wsClients);
    }

    @AfterClass
    private void cleanup() throws Exception {
        ballerinaServer.stopServer();
    }
}
