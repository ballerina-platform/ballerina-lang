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

package org.wso2.carbon.transport.http.netty.listener;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.net.InetSocketAddress;

/**
 * {@link SourceHandler} only handles the HTTP requests
 * This class handles all kinds of WebSocketFrames
 * after connection is upgraded from HTTP to WebSocket
 */
public class WebSocketSourceHandler extends SourceHandler {

    private static Logger log = LoggerFactory.getLogger(WebSocketSourceHandler.class);
    private final String uri;

    public WebSocketSourceHandler(ConnectionManager connectionManager,
                                  ListenerConfiguration listenerConfiguration,
                                  String uri) throws Exception {
        super(connectionManager, listenerConfiguration);
        this.uri = uri;
    }



    /**
     * Read the channel for incoming {@link io.netty.handler.codec.http.websocketx.WebSocketFrame}
     * @param ctx {@link ChannelHandlerContext} for the given channel
     * @param msg Incoming message object
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof WebSocketFrame) {
            WebSocketFrame webSocketFrame = (WebSocketFrame) msg;
            CarbonMessage carbonMessage = setupBasicCarbonMessage(ctx);
            carbonMessage.setProperty(Constants.IS_WEBSOCKET_FRAME, true);
            carbonMessage.setProperty(Constants.WEBSOCKET_FRAME, webSocketFrame);
            publishToMessageProcessor(carbonMessage);
        } else {
            log.error("Expecting WebSocketFrame. Unknown type.");
        }
    }


    /*
    Carbon Message is published to registered message processor
    Message Processor should return transport thread immediately
     */
    private void publishToMessageProcessor(CarbonMessage cMsg) {
        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor().executeAtSourceRequestReceiving(cMsg);
        }

        CarbonMessageProcessor carbonMessageProcessor = HTTPTransportContextHolder.getInstance()
                .getMessageProcessor();
        if (carbonMessageProcessor != null) {
            try {
                carbonMessageProcessor.receive(cMsg, new ResponseCallback(this.ctx));
            } catch (Exception e) {
                log.error("Error while submitting CarbonMessage to CarbonMessageProcessor", e);
            }
        } else {
            log.error("Cannot find registered MessageProcessor for forward the message");
        }

    }



    /*
     Extract all the necessary details from ChannelHandlerContext
     Add them into a CarbonMessage
     Note : This method only add details of ChannelHandlerContext to the CarbonMessage
     Adding other custom details should be done separately
     @return basic CarbonMessage with necessary details
     */
    private CarbonMessage setupBasicCarbonMessage(ChannelHandlerContext ctx) {

        CarbonMessage cMsg = new HTTPCarbonMessage();
        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor().executeAtSourceRequestReceiving(cMsg);
        }
        cMsg.setProperty(Constants.PORT, ((InetSocketAddress) ctx.channel().remoteAddress()).getPort());
        cMsg.setProperty(Constants.HOST, ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName());

        cMsg.setProperty(Constants.TO, this.uri);
        cMsg.setProperty(Constants.CHNL_HNDLR_CTX, ctx);
        cMsg.setProperty(Constants.SRC_HNDLR, this);

        cMsg.setProperty(org.wso2.carbon.messaging.Constants.LISTENER_PORT,
                         ((InetSocketAddress) ctx.channel().localAddress()).getPort());

        cMsg.setProperty(Constants.LOCAL_ADDRESS, ctx.channel().localAddress());
        cMsg.setProperty(Constants.LOCAL_NAME, ((InetSocketAddress) ctx.channel().localAddress()).getHostName());
        cMsg.setProperty(Constants.REMOTE_ADDRESS, ctx.channel().remoteAddress());
        cMsg.setProperty(Constants.REMOTE_HOST, ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName());
        cMsg.setProperty(Constants.REMOTE_PORT, ((InetSocketAddress) ctx.channel().remoteAddress()).getPort());
        ChannelHandler handler = ctx.handler();

        cMsg.setProperty(Constants.CHANNEL_ID, ((SourceHandler) handler).getListenerConfiguration().getId());
        return cMsg;
    }
}
