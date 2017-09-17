/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.transport.http.netty.websocket;

import org.testng.Assert;
import org.wso2.carbon.transport.http.netty.util.client.websocket.WebSocketTestClient;

import java.nio.ByteBuffer;

/**
 * Timeout assertion executor for WebSocket test cases.
 */
public class WeSocketTestCase {

    private static int threadSleepTime = 100;
    private static int messageDeliveryCountDown = 100;

    public static void setThreadSleepTime(int threadSleepTime) {
        WeSocketTestCase.threadSleepTime = threadSleepTime;
    }

    public void setMessageDeliveryCountDown(int messageDeliveryCountDown) {
        WeSocketTestCase.messageDeliveryCountDown = messageDeliveryCountDown;
    }

    public void assertWebSocketClientTextMessage(WebSocketTestClient client, String expected)
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

    public void assertWebSocketClientTextMessage(WebSocketTestClientConnectorListener clientConnectorListener,
                                                    String expected) throws InterruptedException {
        for (int j = 0; j < messageDeliveryCountDown; j++) {
            Thread.sleep(threadSleepTime);
            if (expected.equals(clientConnectorListener.getReceivedTextToClient())) {
                Assert.assertTrue(true);
                return;
            }
        }
        Assert.assertTrue(false);
    }

    public void assertWebSocketClientBinaryMessage(WebSocketTestClient client, ByteBuffer bufferExpected)
            throws InterruptedException {
        for (int j = 0; j < messageDeliveryCountDown; j++) {
            Thread.sleep(threadSleepTime);
            ByteBuffer bufferReceived = client.getBufferReceived();
            if (bufferReceived != null) {
                 if (bufferReceived.capacity() == bufferExpected.capacity()) {
                     while (bufferReceived.hasRemaining()) {
                         byte receivedByte = bufferReceived.get();
                         byte expectedByte = bufferExpected.get();
                         if (receivedByte != expectedByte) {
                             Assert.assertTrue(false);
                             return;
                         }
                     }
                     Assert.assertTrue(true);
                     return;
                 } else {
                     Assert.assertTrue(false);
                     return;
                 }
            }
        }
        Assert.assertTrue(false);
    }

    public void assertWebSocketClientBinaryMessage(WebSocketTestClientConnectorListener clientConnectorListener,
                                                      ByteBuffer bufferExpected) throws InterruptedException {
        for (int j = 0; j < messageDeliveryCountDown; j++) {
            Thread.sleep(threadSleepTime);
            ByteBuffer bufferReceived = clientConnectorListener.getReceivedByteBufferToClient();
            if (bufferReceived != null) {
                if (bufferReceived.capacity() == bufferExpected.capacity()) {
                    while (bufferReceived.hasRemaining()) {
                        byte receivedByte = bufferReceived.get();
                        byte expectedByte = bufferExpected.get();
                        if (receivedByte != expectedByte) {
                            Assert.assertTrue(false);
                            return;
                        }
                    }
                    Assert.assertTrue(true);
                    return;
                } else {
                    Assert.assertTrue(false);
                    return;
                }
            }
        }
        Assert.assertTrue(false);
    }

    public void assertWebSocketClientPongMessage(WebSocketTestClientConnectorListener clientConnectorListener)
            throws InterruptedException {
        for (int j = 0; j < messageDeliveryCountDown; j++) {
            Thread.sleep(threadSleepTime);
            if (clientConnectorListener.isPongReceived()) {
                Assert.assertTrue(true);
                return;
            }
        }
        Assert.assertTrue(false);
    }
}
