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

package org.ballerinalang.test.nativeimpl.functions.socket;

import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.stdlib.socket.tcp.SocketUtils;
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

/**
 * Unit tests for server socket.
 */
@Test
public class ServerSocketTest {

    private static final Logger log = LoggerFactory.getLogger(ServerSocketTest.class);
    private CompileResult normalServer;
    private final String welcomeMsg = "Hello Ballerina\n";

    @BeforeClass
    public void setup() {
        normalServer = BServiceUtil.setupProgramFile(this, "test-src/socket/server_socket_io.bal");
    }

    @Test(description = "Check echo server")
    public void testSeverEcho() {
        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.configureBlocking(true);
            socketChannel.connect(new InetSocketAddress("localhost", 59152));
            ByteBuffer buf = ByteBuffer.allocate(512);
            buf.put(welcomeMsg.getBytes(StandardCharsets.UTF_8));
            buf.flip();
            while (buf.hasRemaining()) {
                socketChannel.write(buf);
            }
            buf.clear();
            socketChannel.read(buf);
            Assert.assertEquals(new String(SocketUtils.getByteArrayFromByteBuffer(buf), StandardCharsets.UTF_8), welcomeMsg);
        } catch (IOException e) {
            Assert.fail(e.getMessage(), e);
        }
    }
}
