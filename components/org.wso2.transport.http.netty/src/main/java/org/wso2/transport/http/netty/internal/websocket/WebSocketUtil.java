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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlSignal;
import org.wso2.transport.http.netty.contractimpl.websocket.WebSocketMessageImpl;
import org.wso2.transport.http.netty.contractimpl.websocket.message.WebSocketBinaryMessageImpl;
import org.wso2.transport.http.netty.contractimpl.websocket.message.WebSocketControlMessageImpl;
import org.wso2.transport.http.netty.contractimpl.websocket.message.WebSocketTextMessageImpl;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;

/**
 * Utility class for WebSocket client connector.
 */
public class WebSocketUtil {

    public static String getSessionID(ChannelHandlerContext ctx) {
        return ctx.channel().id().asLongText();
    }

    public static DefaultWebSocketSession getSession(ChannelHandlerContext ctx,
                                                     boolean isSecured, String uri) throws URISyntaxException {
        return new DefaultWebSocketSession(ctx, isSecured, uri, getSessionID(ctx));
    }

    public static WebSocketControlMessage getWebsocketControlMessage(WebSocketFrame webSocketFrame,
                                                                     WebSocketControlSignal controlSignal) {
        ByteBuf content = webSocketFrame.content();
        ByteBuffer clonedContent = getClonedByteBuf(content);
        WebSocketControlMessage webSocketControlMessage = new WebSocketControlMessageImpl(controlSignal, clonedContent);
        webSocketFrame.release();
        return webSocketControlMessage;
    }

    public static WebSocketMessageImpl getWebSocketMessage(TextWebSocketFrame textWebSocketFrame) {
        String text = textWebSocketFrame.text();
        boolean isFinalFragment = textWebSocketFrame.isFinalFragment();
        WebSocketMessageImpl webSocketTextMessage = new WebSocketTextMessageImpl(text, isFinalFragment);
        textWebSocketFrame.release();
        return webSocketTextMessage;
    }

    public static WebSocketMessageImpl getWebSocketMessage(BinaryWebSocketFrame binaryWebSocketFrame) {
        ByteBuf content = binaryWebSocketFrame.content();
        ByteBuffer clonedContent = getClonedByteBuf(content);
        boolean finalFragment = binaryWebSocketFrame.isFinalFragment();
        WebSocketMessageImpl webSocketBinaryMessage = new WebSocketBinaryMessageImpl(clonedContent, finalFragment);
        binaryWebSocketFrame.release();
        return webSocketBinaryMessage;
    }

    private static ByteBuffer getClonedByteBuf(ByteBuf buf) {
        ByteBuffer originalContent = buf.nioBuffer();
        ByteBuffer clonedContent = ByteBuffer.allocate(originalContent.capacity());
        originalContent.rewind();
        clonedContent.put(originalContent);
        originalContent.rewind();
        clonedContent.flip();
        return clonedContent;
    }
}
