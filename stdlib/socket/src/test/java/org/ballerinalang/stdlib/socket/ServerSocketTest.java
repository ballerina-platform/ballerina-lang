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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.socket;

import org.awaitility.Awaitility;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.stdlib.socket.tcp.SelectorManager;
import org.ballerinalang.stdlib.socket.tcp.SocketUtils;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Unit tests for server socket.
 */
@Test(timeOut = 120000)
public class ServerSocketTest {

    private static final Logger log = LoggerFactory.getLogger(ServerSocketTest.class);
    private static final int SERVER1_PORT = 59152;
    private static final int SERVER2_PORT = 59153;
    private static final int SERVER3_PORT = 59154;
    private static final int SERVER4_PORT = 59155;
    private Path testResourceRoot;
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        String resourceRoot = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath())
                .getAbsolutePath();
        testResourceRoot = Paths.get(resourceRoot, "test-src");
        compileResult = BServiceUtil.setupProgramFile(this, testResourceRoot.resolve("server_socket.bal").toString());
        boolean connectionStatus;
        int numberOfRetryAttempts = 20;
        connectionStatus = isConnected(numberOfRetryAttempts, SERVER1_PORT);
        if (!connectionStatus) {
            Assert.fail("Unable to open connection with the test TCP server: " + SERVER1_PORT);
        }
        connectionStatus = isConnected(numberOfRetryAttempts, SERVER2_PORT);
        if (!connectionStatus) {
            Assert.fail("Unable to open connection with the test TCP server: " + SERVER2_PORT);
        }
        connectionStatus = isConnected(numberOfRetryAttempts, SERVER3_PORT);
        if (!connectionStatus) {
            Assert.fail("Unable to open connection with the test TCP server: " + SERVER3_PORT);
        }
    }

    @Test(description = "Check echo server")
    public void testSeverEcho() {
        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.configureBlocking(true);
            socketChannel.connect(new InetSocketAddress("localhost", SERVER1_PORT));
            ByteBuffer buf = ByteBuffer.allocate(64);
            String welcomeMsg = "Hello Ballerina\n";
            buf.put(welcomeMsg.getBytes(StandardCharsets.UTF_8));
            buf.flip();
            socketChannel.write(buf);
            buf.clear();
            socketChannel.read(buf);
            Assert.assertEquals(new String(SocketUtils.getByteArrayFromByteBuffer(buf), StandardCharsets.UTF_8),
                    welcomeMsg);
        } catch (IOException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    @Test(description = "Check partial read server")
    public void testPartialRead() {
        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.configureBlocking(true);
            socketChannel.connect(new InetSocketAddress("localhost", SERVER2_PORT));
            socketChannel.finishConnect();
            ByteBuffer buf = ByteBuffer.allocate(64);
            write(socketChannel, buf, "Hello");
            Thread.sleep(1000);
            write(socketChannel, buf, "from");
            Thread.sleep(1000);
            write(socketChannel, buf, "client");
            Thread.sleep(1000);
            socketChannel.read(buf);
            Assert.assertEquals(new String(SocketUtils.getByteArrayFromByteBuffer(buf), StandardCharsets.UTF_8),
                    "Hello Client");
            final BValue[] result = BRunUtil.invokeStateful(compileResult, "getTotalLength");
            BInteger totalValue = (BInteger) result[0];
            Assert.assertEquals(totalValue.intValue(), 15, "Server didn't receive the expected bytes");
        } catch (IOException | InterruptedException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    @Test(description = "Check blocking read server")
    public void testBlockingRead() {
        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.configureBlocking(true);
            socketChannel.connect(new InetSocketAddress("localhost", SERVER3_PORT));
            socketChannel.finishConnect();
            ByteBuffer buf = ByteBuffer.allocate(64);
            String str1 = "ThisIs";
            write(socketChannel, buf, str1);
            Thread.sleep(2000);
            String str2 = "BlockingRead";
            write(socketChannel, buf, str2);
            socketChannel.read(buf);
            Assert.assertEquals(new String(SocketUtils.getByteArrayFromByteBuffer(buf), StandardCharsets.UTF_8),
                    str1 + str2);
        } catch (IOException | InterruptedException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    private void write(SocketChannel socketChannel, ByteBuffer buf, String msg) throws IOException {
        buf.put(msg.getBytes(StandardCharsets.UTF_8));
        buf.flip();
        socketChannel.write(buf);
        buf.clear();
    }

    @Test
    public void testOnDuplicatePortNegative() {
        try {
            BServiceUtil.setupProgramFile(this,
                    testResourceRoot.resolve("server_socket_duplicate_port_negative.bal").toString());
        } catch (BLangRuntimeException e) {
            String errorStr = e.getMessage().substring(47, 47 + 58);
            Assert.assertEquals(errorStr, "Unable to start the socket service: Address already in use");
        }
    }

    @Test(description = "Check on error resource")
    public void testOnError() {
        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.configureBlocking(true);
            socketChannel.connect(new InetSocketAddress("localhost", SERVER4_PORT));
            ByteBuffer buf = ByteBuffer.allocate(64);
            String welcomeMsg = "123456";
            buf.put(welcomeMsg.getBytes(StandardCharsets.UTF_8));
            buf.flip();
            socketChannel.write(buf);
            buf.clear();
            Awaitility.await().atMost(1, MINUTES).until(() -> {
                BValue[] result = BRunUtil.invokeStateful(compileResult, "getError");
                return ("Error while on read".equals((result[0]).stringValue()));
            });
        } catch (IOException e) {
            Assert.fail(e.getMessage(), e);
        }
    }

    @AfterClass
    public void cleanUp() {
        SelectorManager.getInstance().stop();
    }

    private boolean isConnected(int numberOfRetries, int port) {
        Socket temporarySocketConnection = null;
        boolean isConnected = false;
        final int retryInterval = 1000;
        final int initialRetryCount = 0;
        for (int retryCount = initialRetryCount; retryCount < numberOfRetries && !isConnected; retryCount++) {
            try {
                //Attempts to establish a connection with the server
                temporarySocketConnection = new Socket("localhost", port);
                isConnected = true;
            } catch (IOException e) {
                log.error("Error occurred while establishing a connection with test server", e);
                sleep(retryInterval);
            } finally {
                if (null != temporarySocketConnection) {
                    //We close the connection once completed.
                    close(temporarySocketConnection);
                }
            }
        }
        return isConnected;
    }

    private void sleep(int retryInterval) {
        try {
            Thread.sleep(retryInterval);
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
    }

    private void close(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            log.error("Error occurred while closing the Socket connection", e);
        }
    }
}
