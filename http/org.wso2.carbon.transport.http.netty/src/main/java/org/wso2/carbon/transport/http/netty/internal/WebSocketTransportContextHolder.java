/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.transport.http.netty.internal;

import org.wso2.carbon.connector.framework.websocket.WebSocketObservable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Context holder for WebSocket transport.
 */
public class WebSocketTransportContextHolder {

    private static final WebSocketTransportContextHolder contextHolder = new WebSocketTransportContextHolder();

    // Map<ListenerPort, WebSocketObservable>
    private final Map<String, WebSocketObservable> observablesMap = new ConcurrentHashMap<>();

    private WebSocketTransportContextHolder() {
    }

    public WebSocketTransportContextHolder getInstance() {
        return contextHolder;
    }

    public WebSocketObservable getObservable(String listenerPort) {
        return observablesMap.get(listenerPort);
    }

    public void addObservable(String listenerPort, WebSocketObservable observable) {
        observablesMap.put(listenerPort, observable);
    }
}
