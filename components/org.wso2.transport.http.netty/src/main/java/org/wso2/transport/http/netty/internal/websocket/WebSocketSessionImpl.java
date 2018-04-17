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

package org.wso2.transport.http.netty.internal.websocket;


import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.websocket.CloseReason;
import javax.websocket.RemoteEndpoint;


/**
 * This is spec implementation of {@link javax.websocket.Session} which uses {@link WebSocketSessionAdapter}.
 */
public class WebSocketSessionImpl extends WebSocketSessionAdapter {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketSessionImpl.class);

    private final ChannelHandlerContext ctx;
    private final boolean isSecure;
    private final URI requestedUri;
    private final String sessionId;
    private final WebSocketBasicRemoteEndpoint basicRemoteEndpoint;
    private final WebSocketAsyncRemoteEndpoint asyncRemoteEndpoint;
    private String negotiatedSubProtocol = null;
    private boolean isOpen = false;
    private Map<String, Object> userProperties = new HashMap<>();

    public WebSocketSessionImpl(ChannelHandlerContext ctx, boolean isSecure, String requestedUri,
                                String sessionId) throws URISyntaxException {
        this.ctx = ctx;
        this.isSecure = isSecure;
        this.isOpen = true;
        this.requestedUri = new URI(requestedUri);
        this.sessionId = sessionId;
        this.basicRemoteEndpoint = new WebSocketBasicRemoteEndpoint(ctx);
        this.asyncRemoteEndpoint = new WebSocketAsyncRemoteEndpoint(ctx);
    }

    @Override
    public RemoteEndpoint.Async getAsyncRemote() {
        return asyncRemoteEndpoint;
    }

    @Override
    public RemoteEndpoint.Basic getBasicRemote() {
        return basicRemoteEndpoint;
    }

    @Override
    public String getId() {
        return sessionId;
    }

    @Override
    public void close() throws IOException {
        ctx.channel().close();
        this.isOpen = false;
    }

    @Override
    public void close(CloseReason closeReason) {
        ctx.channel().writeAndFlush(new CloseWebSocketFrame(closeReason.getCloseCode().getCode(),
                                                    closeReason.getReasonPhrase()));
        ctx.channel().close();
        this.isOpen = false;
    }

    @Override
    public URI getRequestURI() {
        return requestedUri;
    }

    @Override
    public String getNegotiatedSubprotocol() {
        return negotiatedSubProtocol;
    }

    public void setNegotiatedSubProtocol(String negotiatedSubProtocol) {
        this.negotiatedSubProtocol = negotiatedSubProtocol;
    }

    @Override
    public boolean isSecure() {
        return isSecure;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return userProperties;
    }

    /**
     * Identify whether connection is still open.
     * @param isOpen true if the connection is still open.
     */
    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    /**
     * Add user property.
     * @param key key of the property.
     * @param value value of the property.
     */
    public void addUserProperty(String key, Object value) {
        userProperties.put(key, value);
    }


}
