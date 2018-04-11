package org.ballerinalang.test.service.websocket.sample;

import org.ballerinalang.test.util.websocket.client.WebSocketTestClient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Facilitate the common functionality of WebSocket integration tests.
 */
public class WebSocketIntegrationTest {

    protected static final int TIMEOUT_IN_SECS = 10;
    protected static final int REMOTE_SERVER_PORT = 15500;

    /**
     * This method is only used when ack is needed from Ballerina WebSocket Proxy in order to sync ballerina
     * WebSocket Server and Client after the handshake in initiated from the {@link WebSocketTestClient}.
     *
     * @param client {@link WebSocketTestClient}.
     * @throws InterruptedException If connection is interrupted during handshake.
     */
    protected void handshakeAndAck(WebSocketTestClient client) throws InterruptedException {
        CountDownLatch ackCountDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(ackCountDownLatch);
        client.handshake();
        ackCountDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        if (!"send".equals(client.getTextReceived())) {
            throw new IllegalArgumentException("Could not receive acknowledgment");
        }
    }
}
