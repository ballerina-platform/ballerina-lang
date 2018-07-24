/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.listener.states;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.Util;
import org.wso2.transport.http.netty.config.KeepAliveConfig;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.HttpOutboundRespListener;
import org.wso2.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.listener.SourceHandler;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST;

/**
 * State between start and end of payload read
 */
public class ReceivingEntityBody implements ListenerState {

    private static Logger log = LoggerFactory.getLogger(ReceivingEntityBody.class);
    private final HandlerExecutor handlerExecutor;
    private final ServerConnectorFuture serverConnectorFuture;
    private final ListenerStateContext stateContext;
    private final SourceHandler sourceHandler;
    private HTTPCarbonMessage inboundRequestMsg;

    public ReceivingEntityBody(ListenerStateContext stateContext,
                               HTTPCarbonMessage inboundRequestMsg, SourceHandler sourceHandler) {
        this.stateContext = stateContext;
        this.inboundRequestMsg = inboundRequestMsg;
        this.sourceHandler = sourceHandler;
        this.handlerExecutor = HTTPTransportContextHolder.getInstance().getHandlerExecutor();
        this.serverConnectorFuture = sourceHandler.getServerConnectorFuture();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // Not a dependant action of this state.
    }

    @Override
    public void readInboundRequestHeaders(ChannelHandlerContext ctx, HttpRequest inboundRequestHeaders) {
        // Not a dependant action of this state.
    }

    @Override
    public void readInboundReqEntityBody(Object inboundRequestEntityBody) throws ServerConnectorException {
        if (inboundRequestMsg != null) {
            if (inboundRequestEntityBody instanceof HttpContent) {
//                sourceErrorHandler.setState(RECEIVING_ENTITY_BODY);
                HttpContent httpContent = (HttpContent) inboundRequestEntityBody;
                try {
                    inboundRequestMsg.addHttpContent(httpContent);
                    if (Util.isLastHttpContent(httpContent)) {
                        if (handlerExecutor != null) {
                            handlerExecutor.executeAtSourceRequestSending(inboundRequestMsg);
                        }
                        if (isDiffered(inboundRequestMsg)) {
                            serverConnectorFuture.notifyHttpListener(inboundRequestMsg);
                        }
                        inboundRequestMsg = null;
                        stateContext.setState(new EntityBodyReceived(stateContext));
//                        sourceErrorHandler.setState(ENTITY_BODY_RECEIVED);
                    }
                } catch (RuntimeException ex) {
                    httpContent.release();
                    log.warn("Response already received before completing the inbound request" + ex.getMessage());
                }
            }
        } else {
            log.warn("Inconsistent state detected : inboundRequestMsg is null for channel read event");
        }
    }

    @Override
    public void writeOutboundResponseHeaders(HTTPCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        // Not a dependant action of this state.
    }

    @Override
    public void writeOutboundResponse(HttpOutboundRespListener outboundResponseListener,
                                      HTTPCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        // If this method is called, it is an application error. we need to close connection once response is sent.
        outboundResponseListener.setKeepAliveConfig(KeepAliveConfig.NEVER);
        stateContext.setState(new SendingHeaders(outboundResponseListener, stateContext));
        stateContext.getState().writeOutboundResponseHeaders(outboundResponseMsg, httpContent);
    }

    @Override
    public void handleAbruptChannelClosure(ServerConnectorFuture serverConnectorFuture) {
        handleIncompleteInboundRequest(REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST);
    }

    @Override
    public ChannelFuture handleIdleTimeoutConnectionClosure(ServerConnectorFuture serverConnectorFuture,
                                                            ChannelHandlerContext ctx,
                                                            IdleStateEvent evt) {
        ChannelFuture outboundRespFuture = sendRequestTimeoutResponse(ctx, HttpResponseStatus.REQUEST_TIMEOUT, Unpooled.EMPTY_BUFFER, 0);

        outboundRespFuture.addListener((ChannelFutureListener) channelFuture -> {
            Throwable cause = channelFuture.cause();
            if (cause != null) {
                log.warn("Failed to send: " + cause.getMessage());
            }
            sourceHandler.channelInactive(ctx);
            handleIncompleteInboundRequest(IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_REQUEST);
        });
        return null;
    }

    private boolean isDiffered(HTTPCarbonMessage sourceReqCmsg) {
        //Http resource stored in the HTTPCarbonMessage means execution waits till payload.
        return sourceReqCmsg.getProperty(Constants.HTTP_RESOURCE) != null;
    }

    private ChannelFuture sendRequestTimeoutResponse(ChannelHandlerContext ctx, HttpResponseStatus status,
                                                     ByteBuf content, int length) {
        HttpResponse outboundResponse;
        if (inboundRequestMsg != null) {
            float httpVersion = Float.parseFloat((String) inboundRequestMsg.getProperty(Constants.HTTP_VERSION));
            if (httpVersion == Constants.HTTP_1_0) {
                outboundResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, status, content);
            } else {
                outboundResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
            }
        } else {
            outboundResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        }
        outboundResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, length);
        outboundResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, Constants.TEXT_PLAIN);
        outboundResponse.headers().set(HttpHeaderNames.CONNECTION.toString(), Constants.CONNECTION_CLOSE);
        outboundResponse.headers().set(HttpHeaderNames.SERVER.toString(), sourceHandler.getServerName());
        return ctx.channel().writeAndFlush(outboundResponse);
    }

    private void handleIncompleteInboundRequest(String errorMessage) {
        LastHttpContent lastHttpContent = new DefaultLastHttpContent();
        lastHttpContent.setDecoderResult(DecoderResult.failure(new DecoderException(errorMessage)));
        this.inboundRequestMsg.addHttpContent(lastHttpContent);
        log.warn(errorMessage);
    }
}
