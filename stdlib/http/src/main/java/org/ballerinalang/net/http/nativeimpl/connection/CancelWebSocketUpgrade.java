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

import io.ballerina.runtime.api.BalEnv;
import io.ballerina.runtime.api.BalFuture;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.ballerinalang.net.http.websocket.WebSocketConstants;
import org.ballerinalang.net.http.websocket.WebSocketUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.websocket.WebSocketHandshaker;

/**
 * {@code CancelWebSocketUpgrade} is the action to cancel a WebSocket upgrade.
 *
 * @since 0.970
 */
public class CancelWebSocketUpgrade {
    private static final Logger log = LoggerFactory.getLogger(CancelWebSocketUpgrade.class);

    public static Object cancelWebSocketUpgrade(BalEnv env, BObject connectionObj, long statusCode, BString reason) {
        BalFuture balFuture = env.markAsync();
        try {
            WebSocketHandshaker webSocketHandshaker =
                    (WebSocketHandshaker) connectionObj.getNativeData(WebSocketConstants.WEBSOCKET_HANDSHAKER);
            if (webSocketHandshaker == null) {
                WebSocketUtil.setNotifyFailure("Not a WebSocket upgrade request. " +
                                               "Cannot cancel the request", balFuture);
                return null;
            }
            ChannelFuture future = webSocketHandshaker.cancelHandshake((int) statusCode, reason.getValue());
            future.addListener((ChannelFutureListener) channelFuture -> {
                Throwable cause = future.cause();
                if (channelFuture.channel().isOpen()) {
                    channelFuture.channel().close();
                }
                if (!future.isSuccess() && cause != null) {
                    balFuture.complete(WebSocketUtil.createErrorByType(cause));
                } else {
                    balFuture.complete(null);
                }
            });
        } catch (Exception e) {
            log.error("Error when cancelling WebsSocket upgrade request", e);
            balFuture.complete(WebSocketUtil.createErrorByType(e));
        }
        return null;
    }

    private CancelWebSocketUpgrade() {
    }
}
