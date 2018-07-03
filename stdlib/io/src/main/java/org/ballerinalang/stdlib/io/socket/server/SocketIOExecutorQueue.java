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

import org.ballerinalang.stdlib.io.events.EventExecutor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * This will hold all the Socket read/write related {@link EventExecutor} callbacks.
 */
public class SocketIOExecutorQueue {

    private static Map<Integer, Queue<EventExecutor>> readRegistry = new HashMap<>();
    private static Map<Integer, Queue<EventExecutor>> writeRegistry = new HashMap<>();

    /**
     * Register EventExecutor for a read operation against a channel.
     *
     * @param channelHash Channel's hashcode.
     * @param e           EventExecutor instance.
     */
    public static void registerRead(int channelHash, EventExecutor e) {
        Queue<EventExecutor> eventExecutorQueue = readRegistry.get(channelHash);
        if (eventExecutorQueue == null) {
            Queue<EventExecutor> queue = new LinkedList<>();
            readRegistry.put(channelHash, queue);
            eventExecutorQueue = queue;
        }
        eventExecutorQueue.add(e);
    }

    /**
     * Get EventExecutor queue for a given channel hashcode.
     *
     * @param channelHash Channel's hashcode.
     * @return EventExecutor queue that contains a read callbacks.
     */
    public static Queue<EventExecutor> getReadQueue(int channelHash) {
        return readRegistry.get(channelHash);
    }

    /**
     * Register EventExecutor for a write operation against a channel.
     *
     * @param channelHash Channel's hashcode.
     * @param e           EventExecutor instance.
     */
    public static void registerWrite(int channelHash, EventExecutor e) {
        Queue<EventExecutor> eventExecutorQueue = writeRegistry.get(channelHash);
        if (eventExecutorQueue == null) {
            Queue<EventExecutor> queue = new LinkedList<>();
            writeRegistry.put(channelHash, queue);
            eventExecutorQueue = queue;
        }
        eventExecutorQueue.add(e);
    }

    /**
     * Get EventExecutor queue for a given channel hashcode.
     *
     * @param channelHash Channel's hashcode.
     * @return EventExecutor queue that contains a write callbacks.
     */
    public static Queue<EventExecutor> getWriteQueue(int channelHash) {
        return writeRegistry.get(channelHash);
    }
}
