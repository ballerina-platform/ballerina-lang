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

import io.netty.channel.ChannelFuture;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.WebSocketConstants;
import org.ballerinalang.net.http.WebSocketOpenConnectionInfo;
import org.ballerinalang.net.http.WebSocketUtil;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * {@code Get} is the GET action implementation of the HTTP Connector.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "externClose",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = WebSocketConstants.WEBSOCKET_CONNECTOR,
                             structPackage = "ballerina/http"),
        args = {
                @Argument(name = "wsConnector", type = TypeKind.OBJECT),
                @Argument(name = "statusCode", type = TypeKind.INT),
                @Argument(name = "reason", type = TypeKind.STRING),
                @Argument(name = "timeoutInSecs", type = TypeKind.INT)
        }
)
public class Close implements NativeCallableUnit {

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BMap<String, BValue> webSocketConnector = (BMap<String, BValue>) context.getRefArgument(0);
        int statusCode = (int) context.getIntArgument(0);
        String reason = context.getStringArgument(0);
        int timeoutInSecs = (int) context.getIntArgument(1);
        WebSocketOpenConnectionInfo connectionInfo = (WebSocketOpenConnectionInfo) webSocketConnector
                .getNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ChannelFuture closeFuture =
                initiateConnectionClosure(context, statusCode, reason, connectionInfo, countDownLatch);
        waitForTimeout(context, timeoutInSecs, countDownLatch);
        closeFuture.channel().close().addListener(future -> {
            WebSocketUtil.setListenerOpenField(connectionInfo);
            callback.notifySuccess();
        });

    }

    private ChannelFuture initiateConnectionClosure(Context context, int statusCode, String reason,
                                                    WebSocketOpenConnectionInfo connectionInfo, CountDownLatch latch) {
        WebSocketConnection webSocketConnection = connectionInfo.getWebSocketConnection();
        ChannelFuture closeFuture;
        if (statusCode < 0) {
            closeFuture = webSocketConnection.initiateConnectionClosure();
        } else {
            closeFuture = webSocketConnection.initiateConnectionClosure(statusCode, reason);
        }
        return closeFuture.addListener(future -> {
            Throwable cause = future.cause();
            if (!future.isSuccess() && cause != null) {
                context.setReturnValues(HttpUtil.getError(context, cause));
            } else {
                context.setReturnValues();
            }
            latch.countDown();
        });
    }

    private void waitForTimeout(Context context, int timeoutInSecs, CountDownLatch latch) {
        try {
            if (timeoutInSecs < 0) {
                latch.await();
            } else {
                boolean countDownReached = latch.await(timeoutInSecs, TimeUnit.SECONDS);
                if (!countDownReached) {
                    String errMsg = String.format(
                            "Could not receive a WebSocket close frame from remote endpoint within %d seconds",
                            timeoutInSecs);
                    context.setReturnValues(HttpUtil.getError(context, errMsg));
                }
            }
        } catch (InterruptedException err) {
            context.setReturnValues(HttpUtil.getError(context, "Connection interrupted while closing the connection"));
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
