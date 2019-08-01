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
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.WebSocketConstants;
import org.ballerinalang.net.http.WebSocketOpenConnectionInfo;
import org.ballerinalang.net.http.WebSocketUtil;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.ballerinalang.net.http.WebSocketConstants.ErrorCode.WsConnectionClosureError;
import static org.ballerinalang.net.http.WebSocketConstants.ErrorCode.WsConnectionError;
import static org.ballerinalang.net.http.WebSocketUtil.createWebSocketError;

/**
 * {@code Get} is the GET action implementation of the HTTP Connector.
 */
@BallerinaFunction(
        orgName = WebSocketConstants.BALLERINA_ORG,
        packageName = WebSocketConstants.PACKAGE_HTTP,
        functionName = "externClose",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = WebSocketConstants.WEBSOCKET_CONNECTOR,
                structPackage = WebSocketConstants.FULL_PACKAGE_HTTP
        )
)
public class Close {

    public static Object externClose(Strand strand, ObjectValue wsConnection, long statusCode, String reason,
                                     long timeoutInSecs) {
        //TODO : NonBlockingCallback is temporary fix to handle non blocking call
        NonBlockingCallback callback = new NonBlockingCallback(strand);
        try {
            WebSocketOpenConnectionInfo connectionInfo = (WebSocketOpenConnectionInfo) wsConnection
                    .getNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO);
            CountDownLatch countDownLatch = new CountDownLatch(1);
            ChannelFuture closeFuture =
                    initiateConnectionClosure(strand, callback, (int) statusCode, reason, connectionInfo,
                            countDownLatch);
            waitForTimeout(callback, (int) timeoutInSecs, countDownLatch);
            closeFuture.channel().close().addListener(future -> {
                WebSocketUtil.setListenerOpenField(connectionInfo);
                callback.notifySuccess();
            });
        } catch (Exception e) {
            //TODO remove this call back
            callback.setReturnValues(createWebSocketError(WsConnectionError, e.getMessage()));
            callback.notifySuccess();
        }
        return null;
    }

    private static ChannelFuture initiateConnectionClosure(Strand strand,
                                                           NonBlockingCallback callback,
                                                           int statusCode, String reason,
                                                           WebSocketOpenConnectionInfo connectionInfo,
                                                           CountDownLatch latch)
            throws IllegalAccessException {
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
                strand.setReturnValues(createWebSocketError(WsConnectionClosureError, cause.getMessage()));
                //TODO remove this call back
                callback.setReturnValues(createWebSocketError(WsConnectionClosureError, cause.getMessage()));
            } else {
                strand.setReturnValues(null);
                //TODO remove this call back
                callback.setReturnValues(null);
            }
            latch.countDown();
        });
    }

    private static void waitForTimeout(NonBlockingCallback callback, int timeoutInSecs,
                                       CountDownLatch latch) {
        try {
            if (timeoutInSecs < 0) {
                latch.await();
            } else {
                boolean countDownReached = latch.await(timeoutInSecs, TimeUnit.SECONDS);
                if (!countDownReached) {
                    String errMsg = String.format(
                            "Could not receive a WebSocket close frame from remote endpoint within %d seconds",
                            timeoutInSecs);
                    //TODO remove this call back
                    callback.setReturnValues(createWebSocketError(WsConnectionClosureError, errMsg));
                }
            }
        } catch (InterruptedException err) {
            //TODO remove this call back
            callback.setReturnValues(createWebSocketError(WsConnectionClosureError,
                    "Connection interrupted while closing the connection"));
            Thread.currentThread().interrupt();
        }
    }

    private Close() {
    }
}
