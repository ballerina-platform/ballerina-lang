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

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.websocket.CloseReason;
import javax.websocket.Extension;
import javax.websocket.MessageHandler;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 * This is the adapter for {@link Session} implementation.
 * This is the implementation class of {@link Session} interface.
 * In here all overridden methods are unsupported. Then user needs to extend this adapter class
 * and override the methods of this as needed. So only the needed methods from this
 * class are implemented and other methods are automatically unsupported.
 */
public class WebSocketSessionAdapter implements Session {

    @Override
    public WebSocketContainer getContainer() {
        throw new UnsupportedOperationException("Method is not supported");
    }


    @Override
    public void addMessageHandler(MessageHandler handler) throws IllegalStateException {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public <T> void addMessageHandler(Class<T> clazz, MessageHandler.Whole<T> handler) {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public <T> void addMessageHandler(Class<T> clazz, MessageHandler.Partial<T> handler) {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public Set<MessageHandler> getMessageHandlers() {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public void removeMessageHandler(MessageHandler handler) {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public String getProtocolVersion() {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public String getNegotiatedSubprotocol() {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public List<Extension> getNegotiatedExtensions() {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public boolean isSecure() {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public boolean isOpen() {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public long getMaxIdleTimeout() {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public void setMaxIdleTimeout(long milliseconds) {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public void setMaxBinaryMessageBufferSize(int length) {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public int getMaxBinaryMessageBufferSize() {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public void setMaxTextMessageBufferSize(int length) {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public int getMaxTextMessageBufferSize() {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public RemoteEndpoint.Async getAsyncRemote() {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public RemoteEndpoint.Basic getBasicRemote() {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public String getId() {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public void close(CloseReason closeReason) throws IOException {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public URI getRequestURI() {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public Map<String, List<String>> getRequestParameterMap() {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public String getQueryString() {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public Map<String, String> getPathParameters() {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public Map<String, Object> getUserProperties() {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public Principal getUserPrincipal() {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public Set<Session> getOpenSessions() {
        throw new UnsupportedOperationException("Method is not supported");
    }
}
