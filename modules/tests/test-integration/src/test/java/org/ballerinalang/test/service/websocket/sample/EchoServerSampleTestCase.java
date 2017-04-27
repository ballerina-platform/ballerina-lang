package org.ballerinalang.test.service.websocket.sample;

import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.util.websocket.WebSocketClient;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import javax.net.ssl.SSLException;

/**
 * Test echo server sample located in ballerina_home/samples/websocket/echoServer/server/websocketEchoServer.bal
 * This sample echo the same message received to the client.
 */
public class EchoServerSampleTestCase extends IntegrationTestCase {

    private final int threadSleepTime = 100;
    private final int clientCount = 10;
    private final WebSocketClient[] wsClients = new WebSocketClient[clientCount];

    {
        for (int i = 0; i < clientCount; i++) {
            wsClients[i] = (new WebSocketClient("ws://localhost:9090/echo-server/ws"));
        }
    }

    @Test
    public void textPushText() throws InterruptedException, SSLException, URISyntaxException {
        handshakeAllClients();
        String sentText = "test";
        wsClients[0].sendText(sentText);
        Thread.sleep(threadSleepTime);
        Assert.assertEquals(wsClients[0].getTextReceived(), sentText);
        for (int i = 1; i < clientCount; i++) {
            Assert.assertEquals(null, wsClients[i].getTextReceived());
        }
        shutDownAllClients();
    }

    private void handshakeAllClients() throws InterruptedException, SSLException, URISyntaxException {
        for (WebSocketClient client: wsClients) {
            client.handhshake();
        }
    }

    private void shutDownAllClients() throws InterruptedException {
        for (WebSocketClient client: wsClients) {
            client.shutDown();
        }
    }
}
