package org.ballerinalang.test.service.websocket;

import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.util.websocket.client.WebSocketTestClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class OnErrorWebSocketTest extends WebSocketIntegrationTest {

    private WebSocketTestClient client;
    private static final String URL = "ws://localhost:9090/error/ws";
    private LogLeecher logLeecher;

    @BeforeClass(description = "Initializes the Ballerina server with the error_log_service.bal file")
    public void setup() throws InterruptedException, BallerinaTestException, URISyntaxException {
        String expectingErrorLog = "error occurred: received continuation data frame outside fragmented message";
        logLeecher = new LogLeecher(expectingErrorLog);
        initBallerinaServer("error_log_service.bal", logLeecher);

        client = new WebSocketTestClient(URL);
        client.handshake();
    }

    @Test
    public void testOnError() throws InterruptedException, BallerinaTestException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendCorruptedFrame();
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        logLeecher.waitForText(TIMEOUT_IN_SECS * 1000);
        CloseWebSocketFrame closeWebSocketFrame = client.getReceiveCloseFrame();

        Assert.assertNotNull(closeWebSocketFrame);
        Assert.assertEquals(closeWebSocketFrame.statusCode(), 1002);

        closeWebSocketFrame.release();
    }

    @AfterClass(description = "Stops the Ballerina server")
    public void cleanup() throws BallerinaTestException, InterruptedException {
        client.shutDown();
        stopBallerinaServerInstance();
    }
}
