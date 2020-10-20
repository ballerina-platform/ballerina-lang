/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.ballerinalang.net.http.websocket.server;

import io.ballerina.runtime.api.values.BObject;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketService;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;

/**
 * This class has WebSocket connection info for both the client and the server. Includes details
 * needed to dispatch a resource after a successful handshake.
 */
public class WebSocketConnectionInfo {

    private final WebSocketService webSocketService;
    private final BObject webSocketEndpoint;
    private final WebSocketConnection webSocketConnection;
    private StringAggregator stringAggregator = null;

    /**
     * @param webSocketService    can be the WebSocketServerService or WebSocketService
     * @param webSocketConnection can be the client or server connection or null if the connection hasn't been made.
     * @param webSocketEndpoint   can be the WebSocketCaller or the WebSocketClient
     */
    public WebSocketConnectionInfo(WebSocketService webSocketService, WebSocketConnection webSocketConnection,
                                   BObject webSocketEndpoint) {
        this.webSocketService = webSocketService;
        this.webSocketConnection = webSocketConnection;
        this.webSocketEndpoint = webSocketEndpoint;
    }

    public WebSocketService getService() {
        return webSocketService;
    }

    public BObject getWebSocketEndpoint() {
        return webSocketEndpoint;
    }

    public WebSocketConnection getWebSocketConnection() throws IllegalAccessException {
        if (webSocketConnection != null) {
            return webSocketConnection;
        } else {
            throw new IllegalAccessException(WebSocketConstants.THE_WEBSOCKET_CONNECTION_HAS_NOT_BEEN_MADE);
        }
    }

    public StringAggregator createIfNullAndGetStringAggregator() {
        if (stringAggregator == null) {
            stringAggregator = new StringAggregator();
        }
        return stringAggregator;
    }

    /**
     * A string aggregator to handle string aggregation for data binding during onText resource dispatching. The
     * aggregation is done in the ConnectionInfo class because the strings specific to a particular connection needs to
     * be aggregated.
     */
    public static class StringAggregator {
        private StringAggregator() {

        }

        private String aggregateString = "";

        public String getAggregateString() {
            return aggregateString;
        }

        public void appendAggregateString(String aggregateString) {
            this.aggregateString += aggregateString;
        }

        public void resetAggregateString() {
            this.aggregateString = "";
        }
    }
}
