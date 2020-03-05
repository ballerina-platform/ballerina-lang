/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.service.websocket;

import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.ballerinalang.test.util.websocket.client.WebSocketTestClient;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test WebSocket continuation frames and the aggregation of fragments when the content is other than a string.
 */
@Test(groups = {"websocket-test"})
public class ContinuationAndAggregationTest extends WebSocketTestCommons {

    @Test(description = "Tests string support for pushText and onText")
    public void testString() throws URISyntaxException, InterruptedException {
        String url = "http://localhost:21003/onTextString";
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        String msg = "Hello";
        assertSuccess(client, msg, msg);
        client.shutDown();
    }

    @Test(description = "Tests JSON support for pushText and onText")
    public void testJson() throws URISyntaxException, InterruptedException {
        String url = "http://localhost:21023/onTextJSON";
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        assertSuccess(client, "{'id':1234, 'name':'Riyafa'}", "{\"id\":1234, \"name\":\"Riyafa\"}");
        assertFailure(client, "{'id':1234, 'name':'Riyafa'", "expected , or } at line: 1 column: 29");
        client.shutDown();
    }

    @Test(description = "Tests string support for pushText and onText")
    public void testXml() throws URISyntaxException, InterruptedException {
        String url = "http://localhost:21024/onTextXML";
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        String msg = "<note><to>Tove</to></note>";
        assertSuccess(client, msg, msg);
        assertFailure(client, "<note><to>Tove</to>",
                      "failed to parse xml: ParseError at [row,col]:[1,20]\n" +
                              "Message: XML document structures must start and end within the same ...");
        client.shutDown();
    }

    @Test(description = "Tests string support for pushText and onText")
    public void testRecord() throws URISyntaxException, InterruptedException {
        String url = "http://localhost:21025/onTextRecord";
        WebSocketTestClient client = new WebSocketTestClient(url);
        client.handshake();
        assertSuccess(client, "{'id':1234, 'name':'Riyafa'}", "{\"id\":1234, \"name\":\"Riyafa\"}");
        client.shutDown();
    }

    private void assertSuccess(WebSocketTestClient client, String msg, String expectedMsg)
            throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText(msg);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        Assert.assertEquals(client.getTextReceived(), expectedMsg, "Invalid message received");
    }

    private void assertFailure(WebSocketTestClient client, String msg, String expected)
            throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        client.setCountDownLatch(countDownLatch);
        client.sendText(msg);
        countDownLatch.await(TIMEOUT_IN_SECS, TimeUnit.SECONDS);
        CloseWebSocketFrame closeFrame = client.getReceivedCloseFrame();
        Assert.assertEquals(closeFrame.statusCode(), 1003, "Invalid status code");
        Assert.assertEquals(closeFrame.reasonText(), expected, "Invalid close reason");
    }
}
