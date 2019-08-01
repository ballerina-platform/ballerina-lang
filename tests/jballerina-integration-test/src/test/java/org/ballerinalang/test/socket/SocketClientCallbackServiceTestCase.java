/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.socket;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.ballerinalang.test.util.TestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Testing socket client callback service functionality.
 */
@Test(groups = "socket-test")
public class SocketClientCallbackServiceTestCase extends SocketBaseTest {

    @BeforeClass
    private void setup() throws Exception {
        TestUtils.prepareBalo(this);
    }

    @Test(description = "Test socket client callback service read ready")
    public void testSocketClientCallbackServerEcho() throws Exception {
        String requestMessage = "Hello Ballerina";
        LogLeecher serverLeecher = new LogLeecher(requestMessage);
        serverInstance.addLogLeecher(serverLeecher);
        Map<String, String> headers = new HashMap<>(1);
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_TEXT_PLAIN);
        HttpResponse response = HttpClientRequest
                .doPost(serverInstance.getServiceURLHttp(58291, "echo"), requestMessage, headers);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 202, "Response code mismatched");
        serverLeecher.waitForText(20000);
        serverInstance.removeAllLeechers();
    }

    @Test(description = "Check numbers of joinees and leavers")
    public void testSocketServerJoinLeave() throws BallerinaTestException {
        LogLeecher joineeServerLeecher = new LogLeecher("Join: 5");
        serverInstance.addLogLeecher(joineeServerLeecher);
        for (int i = 0; i < 5; i++) {
            try (SocketChannel socketChannel = SocketChannel.open()) {
                socketChannel.configureBlocking(true);
                socketChannel.connect(new InetSocketAddress("localhost", 61598));
                ByteBuffer buf = ByteBuffer.allocate(64);
                String welcomeMsg = "Hello Ballerina\n";
                buf.put(welcomeMsg.getBytes(StandardCharsets.UTF_8));
                buf.flip();
                while (buf.hasRemaining()) {
                    socketChannel.write(buf);
                }
            } catch (IOException e) {
                Assert.fail(e.getMessage(), e);
            }
        }
        joineeServerLeecher.waitForText(20000);
        serverInstance.removeAllLeechers();
    }

    @Test(description = "Check read timeout")
    public void testSocketReadTimeout() {
        LogLeecher timeoutLeecher = new LogLeecher("Read timed out");
        serverInstance.addLogLeecher(timeoutLeecher);
        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.configureBlocking(true);
            socketChannel.connect(new InetSocketAddress("localhost", 61599));
            ByteBuffer buf = ByteBuffer.allocate(64);
            String welcomeMsg = "Hello Ballerina";
            buf.put(welcomeMsg.getBytes(StandardCharsets.UTF_8));
            buf.flip();
            while (buf.hasRemaining()) {
                socketChannel.write(buf);
            }
            Thread.sleep(2000L);
            timeoutLeecher.waitForText(22000);
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
        serverInstance.removeAllLeechers();
    }
}
