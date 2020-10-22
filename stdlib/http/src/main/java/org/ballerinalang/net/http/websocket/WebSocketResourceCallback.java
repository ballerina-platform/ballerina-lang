/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.http.websocket;

import io.ballerina.runtime.api.connector.CallableUnitCallback;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.services.ErrorHandlerUtils;
import org.ballerinalang.net.http.websocket.observability.WebSocketObservabilityConstants;
import org.ballerinalang.net.http.websocket.observability.WebSocketObservabilityUtil;
import org.ballerinalang.net.http.websocket.server.WebSocketConnectionInfo;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;

/**
 * Callback impl for web socket.
 */
public class WebSocketResourceCallback implements CallableUnitCallback {

    private final WebSocketConnection webSocketConnection;
    private final WebSocketConnectionInfo connectionInfo;
    private final String resource;

    WebSocketResourceCallback(WebSocketConnectionInfo webSocketConnectionInfo, String resource)
            throws IllegalAccessException {
        this.connectionInfo = webSocketConnectionInfo;
        this.webSocketConnection = connectionInfo.getWebSocketConnection();
        this.resource = resource;
    }

    @Override
    public void notifySuccess() {
        webSocketConnection.readNextFrame();
    }

    @Override
    public void notifyFailure(BError error) {
        ErrorHandlerUtils.printError(error.getPrintableStackTrace());
        WebSocketUtil.closeDuringUnexpectedCondition(webSocketConnection);
        //Observe error
        WebSocketObservabilityUtil.observeError(connectionInfo,
                                                WebSocketObservabilityConstants.ERROR_TYPE_RESOURCE_INVOCATION,
                                                resource, error.getMessage());
    }
}
