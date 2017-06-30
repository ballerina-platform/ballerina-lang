package org.ballerinalang.test.service.websocket.sample;

import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.websocket.WebSocketClient;
import org.testng.Assert;
import org.testng.annotations.Test;

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
        HttpClientRequest.doPost(getServiceURLHttp("storeInfo/0"), sentText + "0", headers);
        HttpClientRequest.doPost(getServiceURLHttp("storeInfo/1"), sentText + "1", headers);
        HttpClientRequest.doPost(getServiceURLHttp("storeInfo/2"), sentText + "2", headers);
        HttpClientRequest.doPost(getServiceURLHttp("storeInfo/3"), sentText + "3", headers);
        HttpClientRequest.doPost(getServiceURLHttp("storeInfo/4"), sentText + "4", headers);
        Thread.sleep(threadSleepTime);
        for (int i = 0; i < clientCount; i++) {
            Assert.assertEquals(webSocketClients[i].getTextReceived(), sentText + i);
        }
    }

    @Test(priority = 1)
    public void testRemoveStoredClient() throws IOException, InterruptedException, URISyntaxException {
        HttpClientRequest.doGet(getServiceURLHttp("storeInfo/rm/0"));
        Thread.sleep(threadSleepTime);
        Map<String, String> headers = new HashMap<>();
        HttpClientRequest.doPost(getServiceURLHttp("storeInfo/0"), "You are out", headers);
        Assert.assertEquals(webSocketClients[0].getTextReceived(), null);
    }

    @Test(priority = 2)
    public void testCloseConnection() throws IOException, InterruptedException {
        HttpClientRequest.doGet(getServiceURLHttp("storeInfo/close/1"));
        Assert.assertFalse(webSocketClients[1].isOpen());
        shutDownAllClients(webSocketClients);
    }
}
