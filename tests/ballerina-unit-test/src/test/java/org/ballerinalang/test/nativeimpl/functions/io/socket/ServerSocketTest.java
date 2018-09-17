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

package org.ballerinalang.test.nativeimpl.functions.io.socket;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.stdlib.io.socket.SelectorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Unit tests for server socket.
 */
@Test(enabled = false)
public class ServerSocketTest {

    private static final Logger log = LoggerFactory.getLogger(ServerSocketTest.class);
    private CompileResult normalServer;
    private final String welcomeMsg = "Hello Ballerina\n";

    @BeforeClass
    public void setup() {
        normalServer = BCompileUtil.compileAndSetup("test-src/io/server_socket_io.bal");
    }

    @Test(description = "Check server socket accept functionality.")
    public void testSeverSocketAccept() throws InterruptedException {
        final int port = ThreadLocalRandom.current().nextInt(47000, 51000);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                BValue[] args = { new BInteger(port), new BString(welcomeMsg) };
                BRunUtil.invokeStateful(normalServer, "startServerSocket", args);
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        });
        Thread.sleep(2500);
        connectClient(port, 1000, normalServer);
        executor.shutdownNow();
    }

    @Test(description = "Check server socket delayed accept functionality.",
          dependsOnMethods = "testSeverSocketAccept")
    public void testSeverSocketDelayedAccept() {
        CompileResult delayedStartServer = BCompileUtil
                .compileAndSetup("test-src/io/server_socket_io_delayed_accept.bal");
        SelectorManager.start();
        final int port = ThreadLocalRandom.current().nextInt(47000, 51000);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                BRunUtil.invokeStateful(delayedStartServer, "initServer", new BValue[] { new BInteger(port) });
                BRunUtil.invokeStateful(delayedStartServer, "startServerSocket",
                        new BValue[] { new BString(welcomeMsg) });
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        });
        connectClient(port, 1000, delayedStartServer);
        executor.shutdownNow();
    }

    @Test(description = "This will check the error situation when the server trying to bind to already occupied port.",
          dependsOnMethods = "testSeverSocketDelayedAccept")
    public void testServerStartOnDuplicatePort() {
        int port = ThreadLocalRandom.current().nextInt(47000, 51000);
        BValue[] args = { new BInteger(port) };
        final BValue[] results = BRunUtil.invokeStateful(normalServer, "runOnDuplicatePort", args);
        final BMap<String, BValue> result = (BMap) results[0];
        final BString message = (BString) result.get("message");
        Assert.assertEquals(message.stringValue(),
                "Error occurred while bind to the socket address: Address already in use",
                "Didn't get the expected error message for duplicate port open.");
    }

    private void connectClient(int port, int retryInterval, CompileResult compileResult) {
        try {
            final int numberOfRetryAttempts = 20;
            final String clientMsg = "This is the first type of message.";
            boolean connected = false;
            for (int retryCount = 0; retryCount < numberOfRetryAttempts; retryCount++) {
                try (SocketChannel socketChannel = SocketChannel.open()) {
                    socketChannel.connect(new InetSocketAddress("localhost", port));
                    ByteBuffer buf = ByteBuffer.allocate(512);
                    int bytesRead = socketChannel.read(buf);
                    if (bytesRead == -1) {
                        socketChannel.close();
                    }
                    String answer = new String(buf.array(), StandardCharsets.UTF_8).trim();
                    Assert.assertEquals(answer, welcomeMsg.trim(), "Didn't get the expected response from server.");
                    buf.clear();
                    buf.put(clientMsg.getBytes());
                    buf.flip();
                    while (buf.hasRemaining()) {
                        socketChannel.write(buf);
                    }
                    connected = true;
                    break;
                } catch (IOException e) {
                    sleep(retryInterval);
                }
            }
            Assert.assertTrue(connected, "Unable to connect to remote server.");
            int i = 0;
            do {
                final BValue[] resultValues = BRunUtil.invokeStateful(compileResult, "getResultValue");
                BString result = (BString) resultValues[0];
                final String str = result.stringValue();
                if (str == null || str.isEmpty()) {
                    sleep(retryInterval);
                    continue;
                }
                Assert.assertEquals(clientMsg, str, "Client message incorrect.");
                break;
            } while (i++ < 10);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void sleep(int retryInterval) {
        try {
            Thread.sleep(retryInterval);
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
    }
}
