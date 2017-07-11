/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.transport.http.netty.sender;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.exceptions.MessagingException;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.sender.channel.TargetChannel;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * A class responsible for handling responses coming from BE.
 *
 * TODO: Need to redesign this. This has an incorrect usage of ReadTimeoutHandler.
 * Timer tasks in IdleStateHandler (parent of ReadTimeoutHandler) is not working properly with overridden methods which
 * causes timeout issues when TargetHandler is re-used from the ConnectionManager.
 *
 */
public class TargetHandler extends ChannelInboundHandlerAdapter {
    protected static final Logger LOG = LoggerFactory.getLogger(TargetHandler.class);

    protected CarbonCallback callback;
    protected CarbonMessage cMsg;
    protected ConnectionManager connectionManager;
    protected TargetChannel targetChannel;
    protected CarbonMessage incomingMsg;

    public TargetHandler() {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor()
                    .executeAtTargetConnectionInitiation(Integer.toString(ctx.hashCode()));
        }

        super.channelActive(ctx);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpResponse) {

            cMsg = setUpCarbonMessage(ctx, msg);
            if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
                HTTPTransportContextHolder.getInstance().getHandlerExecutor().
                        executeAtTargetResponseReceiving(cMsg);
            }
            CarbonMessageProcessor carbonMessageProcessor = HTTPTransportContextHolder.getInstance()
                    .getMessageProcessor((String) incomingMsg.getProperty(Constants.MESSAGE_PROCESSOR_ID));
            if (carbonMessageProcessor != null) {
                try {
                    HTTPTransportContextHolder.getInstance().getMessageProcessor((String) incomingMsg
                            .getProperty(Constants.MESSAGE_PROCESSOR_ID)).receive(cMsg, callback);
                } catch (Exception e) {
                    LOG.error("Error while handover response to MessageProcessor ", e);
                }
            } else {
                LOG.error("Cannot correlate callback with request callback is null ");
            }

        } else {
            if (cMsg != null) {
                if (msg instanceof LastHttpContent) {
                    HttpContent httpContent = (LastHttpContent) msg;
                    ((HTTPCarbonMessage) cMsg).addHttpContent(httpContent);
                    cMsg.setEndOfMsgAdded(true);
                    if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
                        HTTPTransportContextHolder.getInstance().getHandlerExecutor().
                                executeAtTargetResponseSending(cMsg);
                    }
                    targetChannel.setRequestWritten(false);
                    connectionManager.returnChannel(targetChannel);
                } else {
                    HttpContent httpContent = (DefaultHttpContent) msg;
                    ((HTTPCarbonMessage) cMsg).addHttpContent(httpContent);
                }
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.close();
        connectionManager.invalidateTargetChannel(targetChannel);

        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor()
                    .executeAtTargetConnectionTermination(Integer.toString(ctx.hashCode()));
        }
        LOG.debug("Target channel closed.");
    }

    public void setCallback(CarbonCallback callback) {
        this.callback = callback;
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void setIncomingMsg(CarbonMessage incomingMsg) {
        this.incomingMsg = incomingMsg;
    }

    public void setTargetChannel(TargetChannel targetChannel) {
        this.targetChannel = targetChannel;
    }

    // TODO: This cannot be done as long as we use apache pooling.
    protected void readTimedOut(ChannelHandlerContext ctx) {

        ctx.channel().close();

        if (targetChannel.isRequestWritten()) {
            String payload = "<errorMessage>" + "ReadTimeoutException occurred for endpoint " + targetChannel.
                    getHttpRoute().toString() + "</errorMessage>";

            CarbonMessageProcessor carbonMessageProcessor = HTTPTransportContextHolder.getInstance()
                    .getMessageProcessor((String) incomingMsg.getProperty(Constants.MESSAGE_PROCESSOR_ID));

            if (carbonMessageProcessor != null) {
                try {
                    HTTPTransportContextHolder.getInstance().getMessageProcessor((String) incomingMsg
                            .getProperty(Constants.MESSAGE_PROCESSOR_ID))
                            .receive(createErrorMessage(payload), callback);
                } catch (Exception e) {
                    LOG.error("Error while handover response to MessageProcessor ", e);
                }
            } else {
                LOG.error("Cannot correlate callback with request callback is null ");
            }
        }

    }

    protected CarbonMessage setUpCarbonMessage(ChannelHandlerContext ctx, Object msg) {
        cMsg = new HTTPCarbonMessage();
        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor().executeAtTargetResponseReceiving(cMsg);
        }

        cMsg.setProperty(org.wso2.carbon.messaging.Constants.DIRECTION,
                org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE);
        cMsg.setProperty(org.wso2.carbon.messaging.Constants.CALL_BACK, callback);
        HttpResponse httpResponse = (HttpResponse) msg;

        cMsg.setProperty(Constants.HTTP_STATUS_CODE, httpResponse.getStatus().code());
        cMsg.setHeaders(Util.getHeaders(httpResponse).getAll());

        //copy required properties for service chaining from incoming carbon message to the response carbon message
        //copy shared worker pool
        cMsg.setProperty(Constants.EXECUTOR_WORKER_POOL, incomingMsg.getProperty(Constants.EXECUTOR_WORKER_POOL));
        //TODO copy mandatory properties from previous message if needed

        return cMsg;

    }

    protected CarbonMessage createErrorMessage(String payload) {
        DefaultCarbonMessage response = new DefaultCarbonMessage();

        response.setStringMessageBody(payload);
        byte[] errorMessageBytes = payload.getBytes(Charset.defaultCharset());

        Map<String, String> transportHeaders = new HashMap<>();
        transportHeaders.put(Constants.HTTP_CONTENT_TYPE, Constants.TEXT_XML);
        transportHeaders.put(Constants.HTTP_CONTENT_LENGTH, (String.valueOf(errorMessageBytes.length)));

        response.setHeaders(transportHeaders);

        response.setProperty(Constants.HTTP_STATUS_CODE, 504);
        response.setProperty(org.wso2.carbon.messaging.Constants.DIRECTION,
                org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE);
        response.setProperty(org.wso2.carbon.messaging.Constants.CALL_BACK, callback);
        MessagingException messagingException = new MessagingException("Read Timeout", 101504);
        response.setMessagingException(messagingException);
        return response;

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (ctx != null && ctx.channel().isActive()) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
