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

import java.nio.channels.SelectionKey;

/**
 * This will hold the {@link SocketService} and the selector's selection key to assist the read action.
 * Selection key is required to control the read interest.
 *
 * @since 0.995.0
 */
public class SocketReader {

    private SocketService socketService;
    private SelectionKey selectionKey;

    public SocketReader(SocketService socketService, SelectionKey selectionKey) {
        this.socketService = socketService;
        this.selectionKey = selectionKey;
    }

    public SocketService getSocketService() {
        return socketService;
    }

    public SelectionKey getSelectionKey() {
        return selectionKey;
    }
}
