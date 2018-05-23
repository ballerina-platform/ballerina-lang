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
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.WebSocketConnectionManager;
import org.ballerinalang.net.http.WebSocketConstants;
import org.ballerinalang.net.http.WebSocketService;
import org.ballerinalang.net.http.WebSocketUtil;
import org.wso2.transport.http.netty.contract.websocket.WebSocketInitMessage;

import java.util.Set;

/**
 * {@code Get} is the GET action implementation of the HTTP Connector.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "acceptWebSocketUpgrade",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = HttpConstants.CONNECTION,
                             structPackage = "ballerina.http"),
        args = {
                @Argument(name = "headers", type = TypeKind.MAP)
        },
        isPublic = true
)
public class AcceptWebSocketUpgrade implements NativeCallableUnit {

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BStruct httpConnection = (BStruct) context.getRefArgument(0);

        WebSocketInitMessage initMessage =
                (WebSocketInitMessage) httpConnection.getNativeData(WebSocketConstants.WEBSOCKET_MESSAGE);
        WebSocketConnectionManager connectionManager = (WebSocketConnectionManager) httpConnection
                .getNativeData(HttpConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_MANAGER);
        if (initMessage == null) {
            throw new BallerinaConnectorException("Not a WebSocket upgrade request. Cannot upgrade from HTTP to WS");
        }
        if (connectionManager == null) {
            throw new BallerinaConnectorException("Cannot accept a WebSocket upgrade without WebSocket " +
                                                          "connection manager");
        }

        WebSocketService webSocketService = (WebSocketService) httpConnection.getNativeData(
                WebSocketConstants.WEBSOCKET_SERVICE);

        BMap<String, BString> headers = (BMap<String, BString>) context.getRefArgument(1);
        DefaultHttpHeaders httpHeaders = new DefaultHttpHeaders();
        Set<String> keys = headers.keySet();
        for (String key : keys) {
            httpHeaders.add(key, headers.get(key));
        }

        WebSocketUtil.handleHandshake(webSocketService, connectionManager, httpHeaders, initMessage, context, callback);
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
