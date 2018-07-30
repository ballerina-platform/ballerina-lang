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

package org.ballerinalang.stdlib.io.socket.client;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the Socket connect callback queue where, ballerina socket connect function register a callback to execute
 * once the socket successfully connected to remote server.
 *
 * @since 0.980.1
 */
public class SocketConnectCallbackRegistry {

    private static SocketConnectCallbackRegistry instance = new SocketConnectCallbackRegistry();
    private final Object keyLock = new Object();

    private SocketConnectCallbackRegistry() {
    }

    public static SocketConnectCallbackRegistry getInstance() {
        return instance;
    }

    private Map<Integer, SocketConnectCallback> callbackRegistry = new HashMap<>();

    public void registerSocketConnectCallback(int socketHashcode, SocketConnectCallback socketConnectCallback) {
        synchronized (keyLock) {
            callbackRegistry.put(socketHashcode, socketConnectCallback);
        }
    }

    public SocketConnectCallback getCallback(int socketHashcode) {
        synchronized (keyLock) {
            return callbackRegistry.get(socketHashcode);
        }
    }
}
