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

package org.wso2.transport.http.netty.contractimpl.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.transport.http.netty.contract.websocket.WebSocketControlSignal;
import org.wso2.transport.http.netty.contractimpl.websocket.message.DefaultWebSocketBinaryMessage;
import org.wso2.transport.http.netty.contractimpl.websocket.message.DefaultWebSocketControlMessage;
import org.wso2.transport.http.netty.contractimpl.websocket.message.DefaultWebSocketTextMessage;

import java.nio.ByteBuffer;

/**
 * Utility class for WebSocket client connector.
 */
public class WebSocketUtil {

    private WebSocketUtil() {
    }

    public static String getChannelId(ChannelHandlerContext ctx) {
        return ctx.channel().id().asLongText();
    }

    public static WebSocketControlMessage getWebSocketControlMessage(WebSocketFrame webSocketFrame,
                                                                     WebSocketControlSignal controlSignal) {
        ByteBuf content = webSocketFrame.content();
        ByteBuffer clonedContent = getClonedByteBuf(content);
        WebSocketControlMessage webSocketControlMessage = new DefaultWebSocketControlMessage(controlSignal,
                                                                                             clonedContent);
        webSocketFrame.release();
        return webSocketControlMessage;
    }

    public static DefaultWebSocketMessage getWebSocketMessage(WebSocketFrame frame, String text,
                                                              boolean isFinalFragment) {
        DefaultWebSocketMessage webSocketTextMessage = new DefaultWebSocketTextMessage(text, isFinalFragment);
        frame.release();
        return webSocketTextMessage;
    }

    public static DefaultWebSocketMessage getWebSocketMessage(WebSocketFrame webSocketFrame, ByteBuf content,
                                                              boolean finalFragment) {
        ByteBuffer clonedContent = getClonedByteBuf(content);
        DefaultWebSocketMessage webSocketBinaryMessage = new DefaultWebSocketBinaryMessage(clonedContent,
                                                                                           finalFragment);
        webSocketFrame.release();
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
