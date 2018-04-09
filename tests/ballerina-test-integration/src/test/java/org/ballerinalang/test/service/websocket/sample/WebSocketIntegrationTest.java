package org.ballerinalang.test.service.websocket.sample;

import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.util.websocket.client.WebSocketTestClient;

import java.net.URISyntaxException;
import javax.net.ssl.SSLException;

/**
 * Facilitate the common functionality of WebSocket integration tests.
 */
public class WebSocketIntegrationTest extends IntegrationTestCase {

    protected void handshakeAllClients(WebSocketTestClient[] wsClients)
            throws InterruptedException, SSLException, URISyntaxException {
        for (WebSocketTestClient client: wsClients) {
            client.handshake();
        }
    }

    protected void shutDownAllClients(WebSocketTestClient[] wsClients) throws InterruptedException {
        for (WebSocketTestClient client: wsClients) {
            if (client.isOpen()) {
                client.shutDown();
            }
        }
    }
}
