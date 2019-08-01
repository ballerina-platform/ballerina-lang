/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.net.http.nativeimpl.connection;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.WebSocketConnectionManager;
import org.ballerinalang.net.http.WebSocketConstants;
import org.ballerinalang.net.http.WebSocketService;
import org.ballerinalang.net.http.WebSocketUtil;
import org.ballerinalang.net.http.exception.WebSocketException;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;

/**
 * {@code AcceptWebSocketUpgrade} is the AcceptWebSocketUpgrade action implementation of the HTTP Connector.
 */
@BallerinaFunction(
        orgName = WebSocketConstants.BALLERINA_ORG,
        packageName = WebSocketConstants.PACKAGE_HTTP,
        functionName = "acceptWebSocketUpgrade",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = HttpConstants.CALLER,
                structPackage = WebSocketConstants.FULL_PACKAGE_HTTP
        )
)
public class AcceptWebSocketUpgrade {

    public static ObjectValue acceptWebSocketUpgrade(Strand strand, ObjectValue connectionObj,
                                                     MapValue<String, String> headers) {
        WebSocketHandshaker webSocketHandshaker =
                (WebSocketHandshaker) connectionObj.getNativeData(WebSocketConstants.WEBSOCKET_MESSAGE);
        WebSocketConnectionManager connectionManager = (WebSocketConnectionManager) connectionObj
                .getNativeData(HttpConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_MANAGER);
        if (webSocketHandshaker == null) {
            throw new WebSocketException("Not a WebSocket upgrade request. Cannot upgrade from HTTP to WS");
        }
        if (connectionManager == null) {
            throw new WebSocketException("Cannot accept a WebSocket upgrade without WebSocket " +
                    "connection manager");
        }

        WebSocketService webSocketService = (WebSocketService) connectionObj.getNativeData(
                WebSocketConstants.WEBSOCKET_SERVICE);

        DefaultHttpHeaders httpHeaders = new DefaultHttpHeaders();
        Object[] keys = headers.getKeys();
        for (Object key : keys) {
            httpHeaders.add(key.toString(), headers.get(key.toString()));
        }

        WebSocketUtil.handleHandshake(webSocketService, connectionManager, httpHeaders, webSocketHandshaker,
                                      new NonBlockingCallback(strand));
        return null;
    }

    private AcceptWebSocketUpgrade() {
    }
}
