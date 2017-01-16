/*
 *   Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.transport.http.netty.internal;


import io.netty.channel.ChannelHandlerContext;

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
 * This is spec implementation of {@link Session}
 *
 * @since 1.0.0
 */
public class WebSocketSessionImpl implements Session {

    private final ChannelHandlerContext ctx;

    public WebSocketSessionImpl(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Return the container that this session is part of.
     *
     * @return the container.
     */
    @Override
    public WebSocketContainer getContainer() {
        return null;
    }

    /**
     * Register to handle to incoming messages in this conversation. A maximum of one message handler per
     * native websocket message type (text, binary, pong) may be added to each Session. I.e. a maximum
     * of one message handler to handle incoming text messages a maximum of one message handler for
     * handling incoming binary messages, and a maximum of one for handling incoming pong
     * messages. For further details of which message handlers handle which of the native websocket
     * message types please see {@link MessageHandler.Whole} and {@link MessageHandler.Partial}.
     * Adding more than one of any one type will result in a runtime exception.
     * <p>
     * This method is not safe to use unless you are providing an anonymous class derived directly
     * from {@link MessageHandler.Whole} or {@link MessageHandler.Partial}.
     * In all other cases (Lambda Expressions, more complex inheritance or generic type arrangements),
     * one of the following methods have to be used:
     * {@link #addMessageHandler(Class, MessageHandler.Whole)} or
     * {@link #addMessageHandler(Class, MessageHandler.Partial)}.
     *
     * @param handler the MessageHandler to be added.
     * @throws IllegalStateException if there is already a MessageHandler registered for the same native websocket
     *                               message type as this handler.
     */
    @Override
    public void addMessageHandler(MessageHandler handler) throws IllegalStateException {
        //TODO : Implementation when needed
    }

    /**
     * Register to handle to incoming messages in this conversation. A maximum of one message handler per
     * native websocket message type (text, binary, pong) may be added to each Session. I.e. a maximum
     * of one message handler to handle incoming text messages a maximum of one message handler for
     * handling incoming binary messages, and a maximum of one for handling incoming pong
     * messages. For further details of which message handlers handle which of the native websocket
     * message types please see {@link MessageHandler.Whole} and {@link MessageHandler.Partial}.
     * Adding more than one of any one type will result in a runtime exception.
     *
     * @param clazz   type of the message processed by message handler to be registered.
     * @param handler whole message handler to be added.
     * @throws IllegalStateException if there is already a MessageHandler registered for the same native websocket
     *                               message type as this handler.
     * @since 1.1
     */
    @Override
    public <T> void addMessageHandler(Class<T> clazz, MessageHandler.Whole<T> handler) {
        //TODO : Implementation when needed
    }

    /**
     * Register to handle to incoming messages in this conversation. A maximum of one message handler per
     * native websocket message type (text, binary, pong) may be added to each Session. I.e. a maximum
     * of one message handler to handle incoming text messages a maximum of one message handler for
     * handling incoming binary messages, and a maximum of one for handling incoming pong
     * messages. For further details of which message handlers handle which of the native websocket
     * message types please see {@link MessageHandler.Whole} and {@link MessageHandler.Partial}.
     * Adding more than one of any one type will result in a runtime exception.
     *
     * @param clazz   type of the message processed by message handler to be registered.
     * @param handler partial message handler to be added.
     * @throws IllegalStateException if there is already a MessageHandler registered for the same native websocket
     *                               message type as this handler.
     * @since 1.1
     */
    @Override
    public <T> void addMessageHandler(Class<T> clazz, MessageHandler.Partial<T> handler) {
        //TODO : Implementation when needed
    }

    /**
     * Return an unmodifiable copy of the set of MessageHandlers for this Session.
     *
     * @return the set of message handlers.
     */
    @Override
    public Set<MessageHandler> getMessageHandlers() {
        //TODO : Implementation when needed
        return null;
    }

    /**
     * Remove the given MessageHandler from the set belonging to this session. This method may block
     * if the given handler is processing a message until it is no longer in use.
     *
     * @param handler the handler to be removed.
     */
    @Override
    public void removeMessageHandler(MessageHandler handler) {
        //TODO : Implementation when needed
    }

    /**
     * Returns the version of the websocket protocol currently being used. This is taken
     * as the value of the Sec-WebSocket-Version header used in the opening handshake. i.e. "13".
     *
     * @return the protocol version.
     */
    @Override
    public String getProtocolVersion() {
        //TODO : Implementation when needed
        return null;
    }

    /**
     * Return the sub protocol agreed during the websocket handshake for this conversation.
     *
     * @return the negotiated subprotocol, or the empty string if there isn't one.
     */
    @Override
    public String getNegotiatedSubprotocol() {
        //TODO : Implementation when needed
        return null;
    }

    /**
     * Return the list of extensions currently in use for this conversation.
     *
     * @return the negotiated extensions.
     */
    @Override
    public List<Extension> getNegotiatedExtensions() {
        //TODO : Implementation when needed
        return null;
    }

    /**
     * Return true if and only if the underlying socket is using a secure transport.
     *
     * @return whether its using a secure transport.
     */
    @Override
    public boolean isSecure() {
        //TODO : Implementation when needed
        return false;
    }

    /**
     * Return true if and only if the underlying socket is open.
     *
     * @return whether the session is active.
     */
    @Override
    public boolean isOpen() {
        return true;
    }

    /**
     * Return the number of milliseconds before this conversation may be closed by the
     * container if it is inactive, i.e. no messages are either sent or received in that time.
     *
     * @return the timeout in milliseconds.
     */
    @Override
    public long getMaxIdleTimeout() {
        //TODO : Implementation when needed
        return 0;
    }

    /**
     * Set the non-zero number of milliseconds before this session will be closed by the
     * container if it is inactive, ie no messages are either sent or received. A value that is
     * 0 or negative indicates the session will never timeout due to inactivity.
     *
     * @param milliseconds the number of milliseconds.
     */
    @Override
    public void setMaxIdleTimeout(long milliseconds) {
        //TODO : Implementation when needed
    }

    /**
     * Sets the maximum length of incoming binary messages that this Session can buffer.
     *
     * @param length the maximum length.
     */
    @Override
    public void setMaxBinaryMessageBufferSize(int length) {
        //TODO : Implementation when needed
    }

    /**
     * The maximum length of incoming binary messages that this Session can buffer. If
     * the implementation receives a binary message that it cannot buffer because it
     * is too large, it must close the session with a close code of {@link CloseReason.CloseCodes#TOO_BIG}.
     *
     * @return the maximum binary message size that can be buffered.
     */
    @Override
    public int getMaxBinaryMessageBufferSize() {
        //TODO : Implementation when needed
        return 0;
    }

    /**
     * Sets the maximum length of incoming text messages that this Session can buffer.
     *
     * @param length the maximum length.
     */
    @Override
    public void setMaxTextMessageBufferSize(int length) {
        //TODO : Implementation when needed
    }

    /**
     * The maximum length of incoming text messages that this Session can buffer. If
     * the implementation receives a text message that it cannot buffer because it
     * is too large, it must close the session with a close code of {@link CloseReason.CloseCodes#TOO_BIG}.
     *
     * @return the maximum text message size that can be buffered.
     */
    @Override
    public int getMaxTextMessageBufferSize() {
        //TODO : Implementation when needed
        return 0;
    }

    /**
     * Return a reference a RemoteEndpoint object representing the peer of this conversation
     * that is able to send messages asynchronously to the peer.
     *
     * @return the remote endpoint.
     */
    @Override
    public RemoteEndpoint.Async getAsyncRemote() {
        //TODO : Implementation when needed
        return null;
    }

    /**
     * Return a reference a RemoteEndpoint object representing the peer of this conversation
     * that is able to send messages synchronously to the peer.
     *
     * @return the remote endpoint.
     */
    @Override
    public RemoteEndpoint.Basic getBasicRemote() {
        RemoteEndpoint.Basic basicRemoteEndpoint = new WebSocketBasicRemoteEndpoint(ctx);
        return basicRemoteEndpoint;
    }

    /**
     * Returns a string containing the unique identifier assigned to this session.
     * The identifier is assigned by the web socket implementation and is implementation dependent.
     *
     * @return the unique identifier for this session instance.
     */
    @Override
    public String getId() {
        return ctx.channel().toString();
    }

    /**
     * Close the current conversation with a normal status code and no reason phrase.
     *
     * @throws IOException if there was a connection error closing the connection.
     */
    @Override
    public void close() throws IOException {
    }

    /**
     * Close the current conversation, giving a reason for the closure. The close
     * call causes the implementation to attempt notify the client of the close as
     * soon as it can. This may cause the sending of unsent messages immediately
     * prior to the close notification. After the close notification has been sent
     * the implementation notifies the endpoint's onClose method. Note the websocket
     * specification defines the
     * acceptable uses of status codes and reason phrases. If the application cannot
     * determine a suitable close code to use for the closeReason, it is recommended
     * to use {@link CloseReason.CloseCodes#NO_STATUS_CODE}.
     *
     * @param closeReason the reason for the closure.
     * @throws IOException if there was a connection error closing the connection
     */
    @Override
    public void close(CloseReason closeReason) throws IOException {

    }

    /**
     * Return the URI under which this session was opened, including
     * the query string if there is one.
     *
     * @return the request URI.
     */
    @Override
    public URI getRequestURI() {
        return null;
    }

    /**
     * Return the request parameters associated with the request this session
     * was opened under.
     *
     * @return the unmodifiable map of the request parameters.
     */
    @Override
    public Map<String, List<String>> getRequestParameterMap() {
        return null;
    }

    /**
     * Return the query string associated with the request this session
     * was opened under.
     *
     * @return the query string
     */
    @Override
    public String getQueryString() {
        return null;
    }

    /**
     * Return a map of the path parameter names and values used associated with the
     * request this session was opened under.
     *
     * @return the unmodifiable map of path parameters. The key of the map is the parameter name, the values in the map
     * are the parameter values.
     */
    @Override
    public Map<String, String> getPathParameters() {
        return null;
    }

    /**
     * While the session is open, this method returns a Map that the developer may
     * use to store application specific information relating to this session
     * instance. The developer may retrieve information from this Map at any time
     * between the opening of the session and during the onClose() method. But outside
     * that time, any information stored using this Map may no longer be kept by the
     * container. Web socket applications running on distributed implementations of
     * the web container should make any application specific objects stored here
     * java.io.Serializable, or the object may not be recreated after a failover.
     *
     * @return an editable Map of application data.
     */
    @Override
    public Map<String, Object> getUserProperties() {
        return null;
    }

    /**
     * Return the authenticated user for this Session or {@code null} if no user is authenticated for this session.
     *
     * @return the user principal.
     */
    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    /**
     * Return a copy of the Set of all the open web socket sessions that represent
     * connections to the same endpoint to which this session represents a connection.
     * The Set includes the session this method is called on. These
     * sessions may not still be open at any point after the return of this method. For
     * example, iterating over the set at a later time may yield one or more closed sessions. Developers
     * should use session.isOpen() to check.
     *
     * @return the set of sessions, open at the time of return.
     */
    @Override
    public Set<Session> getOpenSessions() {
        return null;
    }
}
