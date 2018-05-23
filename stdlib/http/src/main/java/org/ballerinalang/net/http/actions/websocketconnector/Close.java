/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.actions.websocketconnector;

import io.netty.channel.ChannelFutureListener;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.WebSocketConstants;
import org.ballerinalang.net.http.WebSocketOpenConnectionInfo;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;

/**
 * {@code Get} is the GET action implementation of the HTTP Connector.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "close",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = WebSocketConstants.WEBSOCKET_CONNECTOR,
                             structPackage = "ballerina.http"),
        args = {
                @Argument(name = "wsConnector", type = TypeKind.STRUCT),
                @Argument(name = "statusCode", type = TypeKind.INT),
                @Argument(name = "reason", type = TypeKind.STRING),
                @Argument(name = "timeoutInSecs", type = TypeKind.INT)
        }
)
public class Close implements NativeCallableUnit {

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        try {
            BStruct webSocketConnector = (BStruct) context.getRefArgument(0);
            int statusCode = (int) context.getIntArgument(0);
            String reason = context.getStringArgument(0);
            int timeoutInSecs = (int) context.getIntArgument(1);
            WebSocketOpenConnectionInfo connectionInfo = (WebSocketOpenConnectionInfo) webSocketConnector
                    .getNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO);
            WebSocketConnection webSocketConnection = connectionInfo.getWebSocketConnection();
            webSocketConnection.initiateConnectionClosure(statusCode, reason, timeoutInSecs)
                    .addListener((ChannelFutureListener) future -> {
                Throwable cause = future.cause();
                if (!future.isSuccess() && cause != null) {
                    context.setReturnValues(HttpUtil.getError(context, cause));
                } else {
                    connectionInfo.setCloseStatusCode(statusCode);
                    connectionInfo.getWebSocketEndpoint().setBooleanField(WebSocketConstants.LISTENER_IS_OPEN_INDEX, 0);
                    context.setReturnValues();
                }
                callback.notifySuccess();
            });
        } catch (Throwable throwable) {
            context.setReturnValues(HttpUtil.getError(context, throwable));
            callback.notifySuccess();
        }
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
