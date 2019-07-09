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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * This UDP server socket will use to mock the backend server.
 */
public class MockUdpServer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MockUdpServer.class);

    private static final int SERVER_PORT = 48826;
    private boolean execute = true;
    private Selector selector = null;
    private String receivedString;

    @Override
    public void run() {
        try {
            selector = Selector.open();
            DatagramChannel serverSocket = DatagramChannel.open();
            serverSocket.bind(new InetSocketAddress("localhost", SERVER_PORT));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_READ);
            ByteBuffer buffer = ByteBuffer.allocate(256);
            while (execute) {
                try {
                    final int select = selector.select();
                    if (select == 0) {
                        continue;
                    }
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = selectedKeys.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        if (key.isReadable()) {
                            answerWithEcho(buffer, key);
                        }
                    }
                } catch (Throwable e) {
                    log.error("Error in MockUdpServer loop: " + e.getMessage());
                }
            }
            serverSocket.close();
        } catch (Throwable e) {
            log.error(e.getMessage());
        }
    }

    private void answerWithEcho(ByteBuffer buffer, SelectionKey key) throws IOException {
        DatagramChannel channel = (DatagramChannel) key.channel();
        SocketAddress client = channel.receive(buffer);
        byte[] readBytes = buffer.array();
        receivedString = new String(readBytes, StandardCharsets.UTF_8.name()).trim();
        buffer.flip();
        channel.send(buffer, client);
        buffer.clear();
    }

    void stop() {
        execute = false;
        if (selector == null) {
            return;
        }
        selector.wakeup();
        try {
            selector.close();
        } catch (IOException e) {
            // Do nothing.
        }
    }

    String getReceivedString() {
        return receivedString;
    }
}
