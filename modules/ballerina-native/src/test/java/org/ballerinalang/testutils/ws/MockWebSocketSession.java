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
 */

package org.ballerinalang.testutils.ws;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
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
 * Mock session implementation for WebSocket tests.
 */
public class MockWebSocketSession implements Session {

    private final String id;
    private final MockBasicRemoteEndpoint remoteEndpoint;
    private CloseReason closeReason;
    private boolean isOpen = true;

    public MockWebSocketSession(String id) {
        this.id = id;
        remoteEndpoint = new MockBasicRemoteEndpoint();
        isOpen = true;
    }

    public String getTextReceived() {
        return remoteEndpoint.getTextReceived();
    }

    public ByteBuffer getBufferReceived() {
        return remoteEndpoint.getBufferReceived();
    }

    @Override
    public WebSocketContainer getContainer() {
        return null;
    }

    @Override
    public void addMessageHandler(MessageHandler handler) throws IllegalStateException {

    }

    @Override
    public <T> void addMessageHandler(Class<T> clazz, MessageHandler.Whole<T> handler) {

    }

    @Override
    public <T> void addMessageHandler(Class<T> clazz, MessageHandler.Partial<T> handler) {

    }

    @Override
    public Set<MessageHandler> getMessageHandlers() {
        return null;
    }

    @Override
    public void removeMessageHandler(MessageHandler handler) {

    }

    @Override
    public String getProtocolVersion() {
        return null;
    }

    @Override
    public String getNegotiatedSubprotocol() {
        return null;
    }

    @Override
    public List<Extension> getNegotiatedExtensions() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public long getMaxIdleTimeout() {
        return 0;
    }

    @Override
    public void setMaxIdleTimeout(long milliseconds) {

    }

    @Override
    public void setMaxBinaryMessageBufferSize(int length) {

    }

    @Override
    public int getMaxBinaryMessageBufferSize() {
        return 0;
    }

    @Override
    public void setMaxTextMessageBufferSize(int length) {

    }

    @Override
    public int getMaxTextMessageBufferSize() {
        return 0;
    }

    @Override
    public RemoteEndpoint.Async getAsyncRemote() {
        return null;
    }

    @Override
    public RemoteEndpoint.Basic getBasicRemote() {
        return remoteEndpoint;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void close() throws IOException {
        this.isOpen = false;
    }

    @Override
    public void close(CloseReason closeReason) throws IOException {
        this.closeReason = closeReason;
        this.isOpen = false;
    }

    @Override
    public URI getRequestURI() {
        return null;
    }

    @Override
    public Map<String, List<String>> getRequestParameterMap() {
        return null;
    }

    @Override
    public String getQueryString() {
        return null;
    }

    @Override
    public Map<String, String> getPathParameters() {
        return null;
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return null;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public Set<Session> getOpenSessions() {
        return null;
    }
}
