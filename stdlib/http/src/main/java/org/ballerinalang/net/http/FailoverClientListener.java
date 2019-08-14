/*
 * Copyright (c) 2019, WSO2 Inc. (http:www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http:www.apache.orglicensesLICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specif ic language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http;

import org.ballerinalang.jvm.values.ObjectValue;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;

import java.io.IOException;
import java.io.PrintStream;

import static org.ballerinalang.net.http.WebSocketConstants.STATUS_CODE_ABNORMAL_CLOSURE;
import static org.ballerinalang.net.http.WebSocketUtil.doAction;
import static org.ballerinalang.net.http.WebSocketUtil.hasRetryConfig;
import static org.ballerinalang.net.http.WebSocketUtil.setCloseMessage;

/**
 * Failover client connector listener for WebSocket.
 */
public class FailoverClientListener extends WebSocketClientConnectorListener {

    private WebSocketOpenConnectionInfo connectionInfo;
    private static final PrintStream console = System.out;

    public void setConnectionInfo(WebSocketOpenConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    @Override
    public void onMessage(WebSocketCloseMessage webSocketCloseMessage) {
        ObjectValue webSocketClient = connectionInfo.getWebSocketEndpoint();
        int statusCode = webSocketCloseMessage.getCloseCode();
        if (statusCode == STATUS_CODE_ABNORMAL_CLOSURE) {
            doAction(connectionInfo, null, webSocketCloseMessage);
        } else {
            if (hasRetryConfig(webSocketClient)) {
                console.println("Couldn't connect to the server because the connected server " +
                        "has sent the close request to the client.");
            }
            setCloseMessage(connectionInfo, webSocketCloseMessage);
        }
    }

    @Override
    public void onError(WebSocketConnection webSocketConnection, Throwable throwable) {
        if (throwable instanceof IOException || throwable.getMessage().contains("Unexpected error")) {
            doAction(connectionInfo, throwable, null);
        } else {
            console.println("Unable do the retry because the connection has some issue that needs to fix.");
            WebSocketDispatcher.dispatchError(connectionInfo, throwable);
        }
    }
}
