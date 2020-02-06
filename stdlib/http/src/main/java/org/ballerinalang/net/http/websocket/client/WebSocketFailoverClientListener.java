/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.websocket.client;

import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketResourceDispatcher;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.ballerinalang.net.http.websocket.server.WebSocketConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;

import java.io.IOException;

/**
 * Failover client listener of the WebSocket.
 *
 * @since 1.2.0
 */
public class WebSocketFailoverClientListener extends WebSocketClientConnectorListener {

    private WebSocketConnectionInfo connectionInfo = null;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketFailoverClientListener.class);

    @Override
    public void setConnectionInfo(WebSocketConnectionInfo connectionInfo) {
        super.setConnectionInfo(connectionInfo);
        this.connectionInfo = connectionInfo;
    }

    @Override
    public void onMessage(WebSocketCloseMessage webSocketCloseMessage) {
        int statusCode = webSocketCloseMessage.getCloseCode();
        if (!(statusCode == WebSocketConstants.STATUS_CODE_ABNORMAL_CLOSURE &&
                WebSocketUtil.failover(connectionInfo))) {
            WebSocketUtil.dispatchOnClose(connectionInfo, webSocketCloseMessage);
        }
    }

    @Override
    public void onError(WebSocketConnection webSocketConnection, Throwable throwable) {
        // When the connection is lost, do the failover to the remaining server URLs
        if (!(throwable instanceof IOException && WebSocketUtil.failover(connectionInfo))) {
            logger.error(WebSocketConstants.ERROR_MESSAGE, throwable);
            WebSocketResourceDispatcher.dispatchOnError(connectionInfo, throwable);
        }
    }
}
