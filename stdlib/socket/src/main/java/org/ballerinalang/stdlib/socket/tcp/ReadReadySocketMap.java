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

package org.ballerinalang.stdlib.socket.tcp;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This map will hold all the read ready sockets until it is read through the
 * {@link org.ballerinalang.stdlib.socket.endpoint.tcp.ClientUtils} action.
 *
 * @since 0.995.0
 */
public class ReadReadySocketMap {
    private ConcurrentHashMap<Integer, SocketReader> socketReaderMap = new ConcurrentHashMap<>();

    private ReadReadySocketMap() {
    }

    private static class LazyHolder {
        private static final ReadReadySocketMap INSTANCE = new ReadReadySocketMap();
    }

    public static ReadReadySocketMap getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void add(SocketReader socketReader) {
        socketReaderMap.put(socketReader.getSocketService().getSocketChannel().hashCode(), socketReader);
    }

    public SocketReader remove(int hashId) {
        return socketReaderMap.remove(hashId);
    }

    public SocketReader get(int hashId) {
        return socketReaderMap.get(hashId);
    }

    public boolean isReadReady(int hashId) {
        return socketReaderMap.containsKey(hashId);
    }
}
