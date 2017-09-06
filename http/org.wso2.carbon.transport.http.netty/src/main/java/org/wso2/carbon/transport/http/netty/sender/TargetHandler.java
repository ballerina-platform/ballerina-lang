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
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.exceptions.MessagingException;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.sender.channel.TargetChannel;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * A class responsible for handling responses coming from BE.
 *
 * Timer tasks in IdleStateHandler (parent of ReadTimeoutHandler) is not working properly with overridden methods which
 * causes timeout issues when TargetHandler is re-used from the ConnectionManager.
 *
 */
public class TargetHandler extends ChannelInboundHandlerAdapter {
    protected static final Logger LOG = LoggerFactory.getLogger(TargetHandler.class);

    private HttpResponseFuture httpResponseFuture;
    private HTTPCarbonMessage cMsg;
    private ConnectionManager connectionManager;
    private TargetChannel targetChannel;
    private CarbonMessage incomingMsg;

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
        if (targetChannel.isRequestWritten()) {
            if (msg instanceof HttpResponse) {
                cMsg = setUpCarbonMessage(ctx, msg);
                if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
                    HTTPTransportContextHolder.getInstance().getHandlerExecutor().
                            executeAtTargetResponseReceiving(cMsg);
                }
                if (this.httpResponseFuture != null) {
                    try {
                        httpResponseFuture.notifyHttpListener(cMsg);
                    } catch (Exception e) {
                        LOG.error("Error while notifying response to listener ", e);
                    }
                } else {
                    LOG.error("Cannot correlate callback with request callback is null");
                }
            } else {
                if (cMsg != null) {
                    if (msg instanceof LastHttpContent) {
                        HttpContent httpContent = (LastHttpContent) msg;
                        cMsg.addHttpContent(httpContent);
                        cMsg.setEndOfMsgAdded(true);
                        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
                            HTTPTransportContextHolder.getInstance().getHandlerExecutor().
                                    executeAtTargetResponseSending(cMsg);
                        }
                        targetChannel.getChannel().pipeline().remove(Constants.IDLE_STATE_HANDLER);
                        connectionManager.returnChannel(targetChannel);
                    } else {
                        HttpContent httpContent = (DefaultHttpContent) msg;
                        cMsg.addHttpContent(httpContent);
                    }
                }
            }
        } else {
            if (msg instanceof HttpResponse) {
                LOG.warn("Received a response for an obsolete request");
            }
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.close();
        targetChannel.getChannel().pipeline().remove(Constants.IDLE_STATE_HANDLER);
        connectionManager.invalidateTargetChannel(targetChannel);

        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor()
                    .executeAtTargetConnectionTermination(Integer.toString(ctx.hashCode()));
        }
        LOG.debug("Target channel closed.");
    }

    public void setHttpResponseFuture(HttpResponseFuture httpResponseFuture) {
        this.httpResponseFuture = httpResponseFuture;
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

    private void sendBackTimeOutResponse() {
        String payload = "<errorMessage>" + "ReadTimeoutException occurred for endpoint " + targetChannel.
                getHttpRoute().toString() + "</errorMessage>";
        if (httpResponseFuture != null) {
            try {
                httpResponseFuture.notifyHttpListener(createErrorMessage(payload));
            } catch (Exception e) {
                LOG.error("Error while notifying response to listener ", e);
            }
        } else {
            LOG.error("Cannot correlate callback with request callback is null ");
        }
    }

    private HTTPCarbonMessage setUpCarbonMessage(ChannelHandlerContext ctx, Object msg) {
        cMsg = new HTTPCarbonMessage();
        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor().executeAtTargetResponseReceiving(cMsg);
        }

        cMsg.setProperty(org.wso2.carbon.messaging.Constants.DIRECTION,
                org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE);
        HttpResponse httpResponse = (HttpResponse) msg;

        cMsg.setProperty(Constants.HTTP_STATUS_CODE, httpResponse.getStatus().code());
        cMsg.setHeaders(Util.getHeaders(httpResponse).getAll());

        //copy required properties for service chaining from incoming carbon message to the response carbon message
        //copy shared worker pool
        cMsg.setProperty(Constants.EXECUTOR_WORKER_POOL, incomingMsg.getProperty(Constants.EXECUTOR_WORKER_POOL));
        //TODO copy mandatory properties from previous message if needed

        return cMsg;

    }

    private HTTPCarbonMessage createErrorMessage(String payload) {
        HTTPCarbonMessage response = new HTTPCarbonMessage();

        response.addMessageBody(ByteBuffer.wrap(payload.getBytes(Charset.defaultCharset())));
        response.setEndOfMsgAdded(true);
        byte[] errorMessageBytes = payload.getBytes(Charset.defaultCharset());

        Map<String, String> transportHeaders = new HashMap<>();
        transportHeaders.put(Constants.HTTP_CONTENT_TYPE, Constants.TEXT_XML);
        transportHeaders.put(Constants.HTTP_CONTENT_LENGTH, (String.valueOf(errorMessageBytes.length)));

        response.setHeaders(transportHeaders);

        response.setProperty(Constants.HTTP_STATUS_CODE, 504);
        response.setProperty(org.wso2.carbon.messaging.Constants.DIRECTION,
                org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE);
        MessagingException messagingException = new MessagingException("read timeout", 101504);
        response.setMessagingException(messagingException);
        return response;

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (ctx != null && ctx.channel().isActive()) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE || event.state() == IdleState.WRITER_IDLE) {
                targetChannel.getChannel().pipeline().remove(Constants.IDLE_STATE_HANDLER);
                targetChannel.setRequestWritten(false);
                sendBackTimeOutResponse();
            }
        }
    }

    public HttpResponseFuture getHttpResponseFuture() {
        return httpResponseFuture;
    }
}
