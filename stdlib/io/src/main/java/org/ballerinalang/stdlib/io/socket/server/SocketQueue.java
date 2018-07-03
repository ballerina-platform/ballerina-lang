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

package org.ballerinalang.stdlib.io.socket.server;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * This queue will hold all the newly accepted socket until it start to use.
 *
 * @since 0.975.1
 */
public class SocketQueue {

    private static Map<Integer, Queue<SocketChannel>> clientSockets = new HashMap<>();

    public static void addSocket(int serverSocketHash, SocketChannel socket) {
        Queue<SocketChannel> socketChannels = clientSockets.get(serverSocketHash);
        if (socketChannels == null) {
            Queue<SocketChannel> queue = new LinkedList<>();
            clientSockets.put(serverSocketHash, queue);
            socketChannels = queue;
        }
        socketChannels.add(socket);
    }

    public static SocketChannel getSocket(int serverSocketHash) {
        final Queue<SocketChannel> clientQueue = clientSockets.get(serverSocketHash);
        if (clientQueue == null) {
            return null;
        }
        return clientQueue.poll();
    }
}
