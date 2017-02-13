/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.transport.http.netty.internal.websocket;


import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.websocket.CloseReason;
import javax.websocket.RemoteEndpoint;


/**
 * This is spec implementation of {@link javax.websocket.Session} which uses {@link WebSocketSessionAdapter}.
 */
public class WebSocketSessionImpl extends WebSocketSessionAdapter {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketSessionImpl.class);

    private final ChannelHandlerContext ctx;
    private final boolean isSecure;
    private final String requestedUri;

    public WebSocketSessionImpl(ChannelHandlerContext ctx, boolean isSecure,
                                String requestedUri) {
        this.ctx = ctx;
        this.isSecure = isSecure;
        this.requestedUri = requestedUri;
    }

    @Override
    public RemoteEndpoint.Basic getBasicRemote() {
        RemoteEndpoint.Basic basicRemoteEndpoint = new WebSocketBasicRemoteEndpoint(ctx);
        return basicRemoteEndpoint;
    }

    @Override
    public String getId() {
        return ctx.channel().toString();
    }

    @Override
    public void close() throws IOException {
        ctx.channel().close();
    }

    @Override
    public void close(CloseReason closeReason) throws IOException {
        ctx.channel().write(new CloseWebSocketFrame(closeReason.getCloseCode().getCode(),
                                                    closeReason.getReasonPhrase()));
    }

    @Override
    public URI getRequestURI() {
        try {
            return new URI(requestedUri);
        } catch (URISyntaxException e) {
            logger.error(e.toString());
            return null;
        }
    }

    @Override
    public boolean isSecure() {
        return isSecure;
    }
}
