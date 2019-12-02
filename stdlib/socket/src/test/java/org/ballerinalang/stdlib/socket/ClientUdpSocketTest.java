/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.socket;

import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.stdlib.socket.tcp.SocketUtils;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Unit tests for UDP client socket.
 */
@Test(timeOut = 120000)
public class ClientUdpSocketTest {

    private static final Logger log = LoggerFactory.getLogger(ClientUdpSocketTest.class);

    private CompileResult socketClient;
    private ExecutorService executor;
    private MockUdpServer mockUdpServer;

    @BeforeClass
    public void setup() {
        try {
            executor = Executors.newSingleThreadExecutor();
            mockUdpServer = new MockUdpServer();
            executor.execute(mockUdpServer);
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            log.error("Unable to open Socket Server: " + e.getMessage(), e);
            Assert.fail(e.getMessage());
        }
        String resourceRoot = Paths.get("src", "test", "resources").toAbsolutePath().toString();
        Path testResourceRoot = Paths.get(resourceRoot, "test-src");
        socketClient = BCompileUtil.compileOffline(testResourceRoot.resolve("udp_client_socket.bal").toString());
    }

    @AfterClass
    public void cleanup() {
        mockUdpServer.stop();
        SocketUtils.shutdownExecutorGracefully(executor);
    }

    @Test()
    public void testClientEcho() {
        String msg = "Hello Ballerina echo";
        BValue[] args = { new BString(msg) };
        final BValue[] echoResult = BRunUtil.invoke(socketClient, "echo", args);
        String echo = echoResult[0].stringValue();
        Assert.assertEquals(echo, msg, "Client did not receive expected echoed message");
        Assert.assertEquals(mockUdpServer.getReceivedString(), msg, "Server didn't get expected msg");
    }

    @Test(dependsOnMethods = "testClientEcho")
    public void testContentReceive() {
        ExecutorService client = Executors.newSingleThreadExecutor();
        String serverContent = "This is server";
        client.execute(() -> sendContent(serverContent, 48827));
        final BValue[] echoResult = BRunUtil.invoke(socketClient, "contentReceive");
        String echo = echoResult[0].stringValue();
        Assert.assertEquals(echo, serverContent, "Client did not receive expected message");
        SocketUtils.shutdownExecutorGracefully(client);
    }

    @Test(dependsOnMethods = "testContentReceive")
    public void testContentReceiveWithLength() {
        ExecutorService client = Executors.newSingleThreadExecutor();
        String serverContent = "This is going to be a tricky";
        client.execute(() -> sendContent(serverContent, 48828));
        final BValue[] echoResult = BRunUtil.invoke(socketClient, "contentReceiveWithLength");
        String echo = echoResult[0].stringValue();
        Assert.assertEquals(echo, serverContent + serverContent, "Client did not receive expected message");
        SocketUtils.shutdownExecutorGracefully(client);
    }

    private void sendContent(String serverContent, int port) {
        try (DatagramChannel channel = DatagramChannel.open()) {
            final InetSocketAddress localhost = new InetSocketAddress("localhost", port);
            final byte[] contentBytes = serverContent.getBytes(Charset.defaultCharset());
            for (int i = 0; i < 40; i++) {
                ByteBuffer content = ByteBuffer.wrap(contentBytes);
                channel.send(content, localhost);
                Thread.sleep(500);
            }
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }
    }
}
