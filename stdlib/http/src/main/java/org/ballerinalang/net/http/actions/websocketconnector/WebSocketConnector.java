/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.runtime.api.BalEnv;
import io.ballerina.runtime.api.BalFuture;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.scheduling.Strand;
import io.netty.channel.ChannelFuture;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.ballerinalang.net.http.websocket.observability.WebSocketObservabilityConstants;
import org.ballerinalang.net.http.websocket.observability.WebSocketObservabilityUtil;
import org.ballerinalang.net.http.websocket.server.WebSocketConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Utilities related to websocket connector actions.
 */
public class WebSocketConnector {
    private static final Logger log = LoggerFactory.getLogger(WebSocketConnector.class);

    public static Object externPushText(BalEnv env, BObject wsConnection, BString text, boolean finalFrame) {
        Strand strand = Scheduler.getStrand();
        BalFuture balFuture = env.markAsync();
        WebSocketConnectionInfo connectionInfo = (WebSocketConnectionInfo) wsConnection
                .getNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO);
        WebSocketObservabilityUtil.observeResourceInvocation(strand, connectionInfo,
                                                             WebSocketConstants.RESOURCE_NAME_PUSH_TEXT);
        try {
            ChannelFuture future = connectionInfo.getWebSocketConnection().pushText(text.getValue(), finalFrame);
            WebSocketUtil.handleWebSocketCallback(env.markAsync(), future, log, connectionInfo);
            WebSocketObservabilityUtil.observeSend(WebSocketObservabilityConstants.MESSAGE_TYPE_TEXT,
                                                   connectionInfo);
        } catch (Exception e) {
            log.error("Error occurred when pushing text data", e);
            WebSocketObservabilityUtil.observeError(WebSocketObservabilityUtil.getConnectionInfo(wsConnection),
                                                    WebSocketObservabilityConstants.ERROR_TYPE_MESSAGE_SENT,
                                                    WebSocketObservabilityConstants.MESSAGE_TYPE_TEXT,
                                                    e.getMessage());
            WebSocketUtil.setCallbackFunctionBehaviour(connectionInfo, balFuture, e);
        }
        return null;
    }

    public static Object pushBinary(BalEnv env, BObject wsConnection, BArray binaryData,
                                    boolean finalFrame) {
        Strand strand = Scheduler.getStrand();
        BalFuture future = env.markAsync();
        WebSocketConnectionInfo connectionInfo = (WebSocketConnectionInfo) wsConnection
                .getNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO);
        WebSocketObservabilityUtil.observeResourceInvocation(strand, connectionInfo,
                                                             WebSocketConstants.RESOURCE_NAME_PUSH_BINARY);
        try {
            ChannelFuture webSocketChannelFuture = connectionInfo.getWebSocketConnection().pushBinary(
                    ByteBuffer.wrap(binaryData.getBytes()), finalFrame);
            WebSocketUtil.handleWebSocketCallback(future, webSocketChannelFuture, log, connectionInfo);
            WebSocketObservabilityUtil.observeSend(WebSocketObservabilityConstants.MESSAGE_TYPE_BINARY,
                                                   connectionInfo);
        } catch (Exception e) {
            log.error("Error occurred when pushing binary data", e);
            WebSocketObservabilityUtil.observeError(WebSocketObservabilityUtil.getConnectionInfo(wsConnection),
                                                    WebSocketObservabilityConstants.ERROR_TYPE_MESSAGE_SENT,
                                                    WebSocketObservabilityConstants.MESSAGE_TYPE_BINARY,
                                                    e.getMessage());
            WebSocketUtil.setCallbackFunctionBehaviour(connectionInfo, future, e);
        }
        return null;
    }

    public static Object ping(BalEnv env, BObject wsConnection, BArray binaryData) {
        Strand strand = Scheduler.getStrand();
        BalFuture balFuture = env.markAsync();
        WebSocketConnectionInfo connectionInfo = (WebSocketConnectionInfo) wsConnection
                .getNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO);
        WebSocketObservabilityUtil.observeResourceInvocation(strand, connectionInfo,
                                                             WebSocketConstants.RESOURCE_NAME_PING);
        try {
            ChannelFuture future = connectionInfo.getWebSocketConnection().ping(ByteBuffer.wrap(binaryData.getBytes()));
            WebSocketUtil.handleWebSocketCallback(balFuture, future, log, connectionInfo);
            WebSocketObservabilityUtil.observeSend(WebSocketObservabilityConstants.MESSAGE_TYPE_PING,
                                                   connectionInfo);
        } catch (Exception e) {
            log.error("Error occurred when pinging", e);
            WebSocketObservabilityUtil.observeError(WebSocketObservabilityUtil.getConnectionInfo(wsConnection),
                                                    WebSocketObservabilityConstants.ERROR_TYPE_MESSAGE_SENT,
                                                    WebSocketObservabilityConstants.MESSAGE_TYPE_PING,
                                                    e.getMessage());
            WebSocketUtil.setCallbackFunctionBehaviour(connectionInfo, balFuture, e);
        }
        return null;
    }

    public static Object pong(BalEnv env, BObject wsConnection, BArray binaryData) {
        Strand strand = Scheduler.getStrand();
        BalFuture balFuture = env.markAsync();
        WebSocketConnectionInfo connectionInfo = (WebSocketConnectionInfo) wsConnection
                .getNativeData(WebSocketConstants.NATIVE_DATA_WEBSOCKET_CONNECTION_INFO);
        WebSocketObservabilityUtil.observeResourceInvocation(strand, connectionInfo,
                                                             WebSocketConstants.RESOURCE_NAME_PONG);
        try {
            ChannelFuture future = connectionInfo.getWebSocketConnection().pong(ByteBuffer.wrap(binaryData.getBytes()));
            WebSocketUtil.handleWebSocketCallback(balFuture, future, log, connectionInfo);
            WebSocketObservabilityUtil.observeSend(WebSocketObservabilityConstants.MESSAGE_TYPE_PONG,
                                                   connectionInfo);
        } catch (Exception e) {
            log.error("Error occurred when ponging", e);
            WebSocketObservabilityUtil.observeError(WebSocketObservabilityUtil.getConnectionInfo(wsConnection),
                                                    WebSocketObservabilityConstants.ERROR_TYPE_MESSAGE_SENT,
                                                    WebSocketObservabilityConstants.MESSAGE_TYPE_PONG,
                                                    e.getMessage());
            WebSocketUtil.setCallbackFunctionBehaviour(connectionInfo, balFuture, e);
        }
        return null;
    }
}
