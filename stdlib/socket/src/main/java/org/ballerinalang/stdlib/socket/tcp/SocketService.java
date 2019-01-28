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

package org.ballerinalang.stdlib.socket.tcp;

import org.ballerinalang.connector.api.Resource;

import java.nio.channels.SelectableChannel;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * This will hold the ServerSocketChannel or client SocketChannel
 * and the associate resources for the service that server or client belong.
 *
 * @since 0.985.0
 */
public class SocketService {

    private SelectableChannel socketChannel;
    private Map<String, Resource> resources;
    private Semaphore resourceLock = new Semaphore(1);

    public SocketService(SelectableChannel socketChannel, Map<String, Resource> resources) {
        this.socketChannel = socketChannel;
        this.resources = resources;
    }

    public SocketService(Map<String, Resource> resources) {
        this.resources = resources;
    }

    SelectableChannel getSocketChannel() {
        return socketChannel;
    }

    public Map<String, Resource> getResources() {
        return resources;
    }

    public Semaphore getResourceLock() {
        return resourceLock;
    }
}
