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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HttpCarbonResponse;
import org.wso2.carbon.transport.http.netty.sender.channel.TargetChannel;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class responsible for handling responses coming from BE.
 *
 * Timer tasks in IdleStateHandler (parent of ReadTimeoutHandler) is not working properly with overridden methods which
 * causes timeout issues when TargetHandler is re-used from the ConnectionManager.
 *
 */
public class TargetHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(TargetHandler.class);

    private HttpResponseFuture httpResponseFuture;
    private HTTPCarbonMessage targetRespMsg;
    private ConnectionManager connectionManager;
    private TargetChannel targetChannel;
    private HTTPCarbonMessage incomingMsg;

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
                targetRespMsg = setUpCarbonMessage(ctx, msg);
                // TODO: Revisit all of these after the refactor
                if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
                    HTTPTransportContextHolder.getInstance().getHandlerExecutor().
                            executeAtTargetResponseReceiving(targetRespMsg);
                }
                if (this.httpResponseFuture != null) {
                    try {
                        httpResponseFuture.notifyHttpListener(targetRespMsg);
                    } catch (Exception e) {
                        LOG.error("Error while notifying response to listener ", e);
                    }
                } else {
                    LOG.error("Cannot correlate callback with request callback is null");
                }
            } else {
                if (targetRespMsg != null) {
                    HttpContent httpContent = (HttpContent) msg;
                    targetRespMsg.addHttpContent(httpContent);
                    if (msg instanceof LastHttpContent) {
                        targetRespMsg.setEndOfMsgAdded(true);
                        targetRespMsg = null;
                        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
                            HTTPTransportContextHolder.getInstance().getHandlerExecutor().
                                    executeAtTargetResponseSending(targetRespMsg);
                        }

                        AtomicInteger redirectCount = ctx.channel().attr(Constants.REDIRECT_COUNT).get();
                        if (redirectCount != null) {
                            redirectCount.set(0);
                            ctx.channel().attr(Constants.REDIRECT_COUNT).set(redirectCount);
                        }
                        if (ctx.channel().attr(Constants.ORIGINAL_REQUEST).get() != null) {
                            ctx.channel().attr(Constants.ORIGINAL_REQUEST).set(null);
                        }

                        targetChannel.getChannel().pipeline().remove(Constants.IDLE_STATE_HANDLER);
                        connectionManager.returnChannel(targetChannel);
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

    private HTTPCarbonMessage setUpCarbonMessage(ChannelHandlerContext ctx, Object msg) {
        targetRespMsg = new HttpCarbonResponse((HttpResponse) msg);

        targetRespMsg.setProperty(org.wso2.carbon.messaging.Constants.DIRECTION,
                org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE);
        HttpResponse httpResponse = (HttpResponse) msg;
        targetRespMsg.setProperty(Constants.HTTP_STATUS_CODE, httpResponse.getStatus().code());

        //copy required properties for service chaining from incoming carbon message to the response carbon message
        //copy shared worker pool
        targetRespMsg.setProperty(Constants.EXECUTOR_WORKER_POOL, incomingMsg
                .getProperty(Constants.EXECUTOR_WORKER_POOL));
        //TODO copy mandatory properties from previous message if needed

        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor()
                    .executeAtTargetResponseReceiving(targetRespMsg);
        }

        return targetRespMsg;
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

    public void setHttpResponseFuture(HttpResponseFuture httpResponseFuture) {
        this.httpResponseFuture = httpResponseFuture;
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void setIncomingMsg(HTTPCarbonMessage incomingMsg) {
        this.incomingMsg = incomingMsg;
    }

    public void setTargetChannel(TargetChannel targetChannel) {
        this.targetChannel = targetChannel;
    }

    public HttpResponseFuture getHttpResponseFuture() {
        return httpResponseFuture;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        httpResponseFuture.notifyHttpListener(cause);
        if (ctx != null && ctx.channel().isActive()) {
            ctx.close();
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

    private void sendBackTimeOutResponse() {
        String payload = "<errorMessage>" + "ReadTimeoutException occurred for endpoint " + targetChannel.
                getHttpRoute().toString() + "</errorMessage>";
        if (httpResponseFuture != null) {
            try {
                httpResponseFuture.notifyHttpListener(Util.createErrorMessage(payload));
            } catch (Exception e) {
                LOG.error("Error while notifying response to listener ", e);
            }
        } else {
            LOG.error("Cannot correlate callback with request callback is null ");
        }
    }
}
