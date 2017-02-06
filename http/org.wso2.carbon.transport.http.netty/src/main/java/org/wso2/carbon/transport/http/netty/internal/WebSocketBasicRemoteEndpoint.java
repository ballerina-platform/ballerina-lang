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

package org.wso2.carbon.transport.http.netty.internal;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import javax.websocket.EncodeException;
import javax.websocket.RemoteEndpoint;

/**
 * This is {@link Basic} implementation for WebSocket Connection.
 *
 * @since 1.0.0
 */
public class WebSocketBasicRemoteEndpoint implements RemoteEndpoint.Basic {

    private final ChannelHandlerContext ctx;

    public WebSocketBasicRemoteEndpoint(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Send a text message, blocking until all of the message has been transmitted.
     *
     * @param text the message to be sent.
     * @throws IOException              if there is a problem delivering the message.
     * @throws IllegalArgumentException if the text is {@code null}.
     */
    @Override
    public void sendText(String text) throws IOException {
        ctx.channel().write(new TextWebSocketFrame(text));
        ctx.channel().flush();
    }

    /**
     * Send a binary message, returning when all of the message has been transmitted.
     *
     * @param data the message to be sent.
     * @throws IOException              if there is a problem delivering the message.
     * @throws IllegalArgumentException if the data is {@code null}.
     */
    @Override
    public void sendBinary(ByteBuffer data) throws IOException {
        byte[] bytes = data.array();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
        ctx.channel().write(new BinaryWebSocketFrame(byteBuf));
        ctx.channel().flush();
    }

    /**
     * Send a text message in parts, blocking until all of the message has been transmitted. The runtime
     * reads the message in order. Non-final parts of the message are sent with isLast set to false. The final part
     * must be sent with isLast set to true.
     *
     * @param partialMessage the parts of the message being sent.
     * @param isLast         Whether the partial message being sent is the last part of the message.
     * @throws IOException              if there is a problem delivering the message fragment.
     * @throws IllegalArgumentException if the partialMessage is {@code null}.
     */
    @Override
    @Deprecated
    public void sendText(String partialMessage, boolean isLast) throws IOException {
    }

    /**
     * Send a binary message in parts, blocking until all of the message has been transmitted. The runtime
     * reads the message in order. Non-final parts are sent with isLast set to false. The final piece
     * must be sent with isLast set to true.
     *
     * @param partialByte the part of the message being sent.
     * @param isLast      Whether the partial message being sent is the last part of the message.
     * @throws IOException              if there is a problem delivering the partial message.
     * @throws IllegalArgumentException if the partialByte is {@code null}.
     */
    @Override
    public void sendBinary(ByteBuffer partialByte, boolean isLast) throws IOException {
        byte[] bytes = partialByte.array();
        ByteBuf partialByteBuf = Unpooled.wrappedBuffer(bytes);
        ctx.channel().write(new BinaryWebSocketFrame(isLast, 0, partialByteBuf));
        ctx.channel().flush();
    }

    /**
     * Opens an output stream on which a binary message may be sent. The developer must close the output stream in order
     * to indicate that the complete message has been placed into the output stream.
     *
     * @return the output stream to which the message will be written.
     * @throws IOException if there is a problem obtaining the OutputStream to write the binary message.
     */
    @Override
    public OutputStream getSendStream() throws IOException {
        return null;
    }

    /**
     * Opens an character stream on which a text message may be sent. The developer must close the writer in order
     * to indicate that the complete message has been placed into the character stream.
     *
     * @return the writer to which the message will be written.
     * @throws IOException if there is a problem obtaining the Writer to write the text message.
     */
    @Override
    public Writer getSendWriter() throws IOException {
        return null;
    }

    /**
     * Sends a custom developer object, blocking until it has been transmitted.
     * Containers will by default be able to encode java primitive types and
     * their object equivalents, otherwise the developer must have provided an encoder
     * for the object type in the endpoint configuration. A developer-provided
     * encoder for a Java primitive type overrides the container default
     * encoder.
     *
     * @param data the object to be sent.
     * @throws IOException              if there is a communication error sending the message object.
     * @throws EncodeException          if there was a problem encoding the message object into the form of a native
     *                                  websocket message.
     * @throws IllegalArgumentException if the data parameter is {@code null}
     */
    @Override
    public void sendObject(Object data) throws IOException, EncodeException {

    }

    /**
     * Indicate to the implementation that it is allowed to batch outgoing messages
     * before sending. Not all implementations support batching of outgoing messages.
     * The default mode for RemoteEndpoints is false. If the developer
     * has indicated that batching of outgoing
     * messages is permitted, then the developer must call flushBatch() in order to be
     * sure that all the messages passed into the send methods of this RemoteEndpoint
     * are sent.
     * When batching is allowed, the implementations send operations are considered
     * to have completed if the message has been written to the local batch, in
     * the case when there is still room in the batch for the message, and are considered
     * to have completed if the batch has been send to the peer and the remainder
     * written to the new batch, in the case when
     * writing the message causes the batch to need to be sent. The blocking
     * and asynchronous send methods use this notion of completion in order
     * to complete blocking calls, notify SendHandlers and complete Futures respectively.
     * When batching is allowed, if the developer has called send methods
     * on this RemoteEndpoint without calling flushBatch(), then the implementation
     * may not have sent all the messages the developer has asked to be sent. If
     * the parameter value is false and the implementation has a batch of unsent messages,
     * then the implementation must immediately send the batch of unsent messages.
     *
     * @param allowed whether the implementation is allowed to batch messages.
     * @throws IOException if batching is being disabled and there are unsent messages this error may be thrown as the
     *                     implementation sends the batch of unsent messages if there is a problem.
     */
    @Override
    public void setBatchingAllowed(boolean allowed) throws IOException {

    }

    /**
     * Return whether the implementation is allowed to batch outgoing messages
     * before sending. The default mode for RemoteEndpoints is false. The value
     * may be changed by calling {@link #setBatchingAllowed(boolean) setBatchingAllowed}.
     */
    @Override
    public boolean getBatchingAllowed() {
        return false;
    }

    /**
     * This method is only used when batching is allowed for this RemoteEndpint. Calling
     * this method forces the implementation to send any unsent messages it has been batching.
     */
    @Override
    public void flushBatch() throws IOException {

    }

    /**
     * Send a Ping message containing the given application data to the remote endpoint. The corresponding Pong message
     * may be picked up using the MessageHandler.Pong handler.
     *
     * @param applicationData the data to be carried in the ping request.
     * @throws IOException              if the ping failed to be sent
     * @throws IllegalArgumentException if the applicationData exceeds the maximum allowed payload of 125 bytes
     */
    @Override
    public void sendPing(ByteBuffer applicationData) throws IOException, IllegalArgumentException {
        ByteBuf applicationDataBuf = Unpooled.wrappedBuffer(applicationData.array());
        ctx.channel().write(new PingWebSocketFrame(applicationDataBuf));
        ctx.channel().flush();
    }

    /**
     * Allows the developer to send an unsolicited Pong message containing the given application
     * data in order to serve as a unidirectional
     * heartbeat for the session.
     *
     * @param applicationData the application data to be carried in the pong response.
     * @throws IOException              if the pong failed to be sent
     * @throws IllegalArgumentException if the applicationData exceeds the maximum allowed payload of 125 bytes
     */
    @Override
    public void sendPong(ByteBuffer applicationData) throws IOException, IllegalArgumentException {
        ByteBuf applicationDataBuf = Unpooled.wrappedBuffer(applicationData.array());
        ctx.channel().write(new PongWebSocketFrame(applicationDataBuf));
        ctx.channel().flush();
    }
}
