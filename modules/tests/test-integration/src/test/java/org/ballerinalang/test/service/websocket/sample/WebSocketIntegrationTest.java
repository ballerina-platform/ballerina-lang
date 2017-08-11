package org.ballerinalang.test.service.websocket.sample;

import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.util.websocket.client.WebSocketClient;
import org.testng.Assert;

import java.net.URISyntaxException;
import javax.net.ssl.SSLException;

/**
 * Facilitate the common functionality of WebSocket integration tests.
 */
public class WebSocketIntegrationTest extends IntegrationTestCase {

    protected void handshakeAllClients(WebSocketClient[] wsClients)
            throws InterruptedException, SSLException, URISyntaxException {
        for (WebSocketClient client: wsClients) {
            client.handhshake();
        }
    }

    protected void shutDownAllClients(WebSocketClient[] wsClients) throws InterruptedException {
        for (WebSocketClient client: wsClients) {
            if (client.isOpen()) {
                client.shutDown();
            }
        }
    }

    protected void assertWebSocketClientStringMessage(WebSocketClient client, String expected,
                                                      int threadSleepTime, int messageDeliveryCountDown)
            throws InterruptedException {
        for (int j = 0; j < messageDeliveryCountDown; j++) {
            Thread.sleep(threadSleepTime);
            if (expected.equals(client.getTextReceived())) {
                Assert.assertTrue(true);
                return;
            }
        }
        Assert.assertTrue(false);
    }
}
