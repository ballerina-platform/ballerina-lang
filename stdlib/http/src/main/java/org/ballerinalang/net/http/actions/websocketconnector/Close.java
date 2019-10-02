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
import org.ballerinalang.net.http.WebSocketObservability;
import org.ballerinalang.net.http.WebSocketOpenConnectionInfo;
import org.ballerinalang.net.http.WebSocketUtil;
import org.ballerinalang.net.http.exception.WebSocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.ballerinalang.net.http.WebSocketConstants.ErrorCode;
import static org.ballerinalang.net.http.WebSocketObservability.WEBSOCKET_ERROR_TYPE_CLOSE;
import static org.ballerinalang.net.http.WebSocketObservability.WEBSOCKET_MESSAGE_RESULT_SUCCESS;
import static org.ballerinalang.net.http.WebSocketObservability.WEBSOCKET_MESSAGE_TYPE_CLOSE;

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
    private static final Logger log = LoggerFactory.getLogger(Close.class);

    public static Object externClose(Strand strand, ObjectValue wsConnection, long statusCode, String reason,
                                     long timeoutInSecs) {
        NonBlockingCallback callback = new NonBlockingCallback(strand);

        try {
            WebSocketOpenConnectionInfo connectionInfo = (WebSocketOpenConnectionInfo) wsConnection
                    .getNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO);
            CountDownLatch countDownLatch = new CountDownLatch(1);
            ChannelFuture closeFuture =
                    initiateConnectionClosure(callback, (int) statusCode, reason, connectionInfo, countDownLatch);
            waitForTimeout(callback, (int) timeoutInSecs, countDownLatch, connectionInfo);
            closeFuture.channel().close().addListener(future -> {
                WebSocketUtil.setListenerOpenField(connectionInfo);
                callback.notifySuccess();

                //Observe close message sent
                WebSocketObservability.observePush(WEBSOCKET_MESSAGE_TYPE_CLOSE, WEBSOCKET_MESSAGE_RESULT_SUCCESS,
                                                   connectionInfo);

            });
        } catch (Exception e) {
            log.error("Error occurred when closing the connections", e);
            callback.setReturnValues(new WebSocketException(ErrorCode.WsConnectionError, e.getMessage()));
            callback.notifySuccess();

            //Observe error when sending close message
            WebSocketObservability.observeError((WebSocketOpenConnectionInfo)
                                 wsConnection.getNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO),
                         WEBSOCKET_ERROR_TYPE_CLOSE);
        }
        return null;
    }

    private static ChannelFuture initiateConnectionClosure(NonBlockingCallback callback, int statusCode, String reason,
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
                callback.setReturnValues(
                        new WebSocketException(ErrorCode.WsConnectionClosureError, cause.getMessage()));

                //Observe connection closure error
                WebSocketObservability.observeError(connectionInfo, WEBSOCKET_ERROR_TYPE_CLOSE);

            } else {
                callback.setReturnValues(null);
            }
            latch.countDown();
        });
    }

    private static void waitForTimeout(NonBlockingCallback callback, int timeoutInSecs,
                                       CountDownLatch latch, WebSocketOpenConnectionInfo connectionInfo) {
        try {
            if (timeoutInSecs < 0) {
                latch.await();
            } else {
                boolean countDownReached = latch.await(timeoutInSecs, TimeUnit.SECONDS);
                if (!countDownReached) {
                    String errMsg = String.format(
                            "Could not receive a WebSocket close frame from remote endpoint within %d seconds",
                            timeoutInSecs);
                    callback.setReturnValues(new WebSocketException(ErrorCode.WsConnectionClosureError, errMsg));

                    //Observe connection closure error
                    WebSocketObservability.observeError(connectionInfo, WEBSOCKET_ERROR_TYPE_CLOSE);

                }
            }
        } catch (InterruptedException err) {
            callback.setReturnValues(new WebSocketException(ErrorCode.WsConnectionClosureError,
                                                            "Connection interrupted while closing the connection"));

            //Observe connection closure error
            WebSocketObservability.observeError(connectionInfo, WEBSOCKET_ERROR_TYPE_CLOSE);

            Thread.currentThread().interrupt();
        }
    }

    private Close() {
    }
}
