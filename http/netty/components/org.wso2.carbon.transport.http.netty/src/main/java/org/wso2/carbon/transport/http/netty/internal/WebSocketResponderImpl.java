/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.wso2.carbon.messaging.websocket.BinaryWebSocketCarbonMessage;
import org.wso2.carbon.messaging.websocket.CloseWebSocketCarbonMessage;
import org.wso2.carbon.messaging.websocket.TextWebSocketCarbonMessage;
import org.wso2.carbon.messaging.websocket.WebSocketCarbonMessage;
import org.wso2.carbon.messaging.websocket.WebSocketResponder;

/**
 * This class is responsible for sending server-side responses to a given client
 */
public class WebSocketResponderImpl implements WebSocketResponder {

    private final ChannelHandlerContext ctx;
    private int statusCodeLowerLimit = 1000;

    public WebSocketResponderImpl(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void pushToClient(WebSocketCarbonMessage webSocketCarbonMessage) {

        WebSocketFrame webSocketFrame = null;

        if (webSocketCarbonMessage instanceof TextWebSocketCarbonMessage) {
            TextWebSocketCarbonMessage textWebSocketCarbonMessage =
                    (TextWebSocketCarbonMessage) webSocketCarbonMessage;
            webSocketFrame = new TextWebSocketFrame(textWebSocketCarbonMessage.getText());

        } else if (webSocketCarbonMessage instanceof BinaryWebSocketCarbonMessage) {
            BinaryWebSocketCarbonMessage binaryWebSocketCarbonMessage =
                    (BinaryWebSocketCarbonMessage) webSocketCarbonMessage;

            byte[] bytes = binaryWebSocketCarbonMessage.readBytes().array();
            webSocketFrame = new BinaryWebSocketFrame(Unpooled.wrappedBuffer(bytes));

        } else if (webSocketCarbonMessage instanceof CloseWebSocketCarbonMessage) {
            CloseWebSocketCarbonMessage closeWebSocketCarbonMessage =
                    (CloseWebSocketCarbonMessage) webSocketCarbonMessage;

            String reasonText = closeWebSocketCarbonMessage.getReasonText();
            int statusCode = closeWebSocketCarbonMessage.getStatusCode();

            if (statusCode >= statusCodeLowerLimit && reasonText != null) {
                webSocketFrame = new CloseWebSocketFrame(statusCode, reasonText);
            } else if (statusCode >= statusCodeLowerLimit && reasonText == null) {
                webSocketFrame = new CloseWebSocketFrame(statusCode, null);
            } else {
                webSocketFrame = new CloseWebSocketFrame();
            }

        }

        ctx.channel().write(webSocketFrame);
        ctx.channel().flush();
    }
}
