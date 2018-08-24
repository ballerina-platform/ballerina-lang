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

package org.wso2.transport.http.netty.listener.states.listener;

import io.netty.buffer.ByteBuf;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.HttpOutboundRespListener;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.internal.HttpTransportContextHolder;
import org.wso2.transport.http.netty.listener.SourceHandler;
import org.wso2.transport.http.netty.listener.states.StateContext;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.PriorityQueue;
import java.util.Queue;

import static io.netty.buffer.Unpooled.EMPTY_BUFFER;
import static io.netty.handler.codec.http.HttpResponseStatus.REQUEST_TIMEOUT;
import static org.wso2.transport.http.netty.common.Constants.EXPECTED_SEQUENCE_NUMBER;
import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_REQUEST_HEADERS;
import static org.wso2.transport.http.netty.common.Constants.NUMBER_OF_INITIAL_EVENTS_HELD;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST_HEADERS;
import static org.wso2.transport.http.netty.common.Util.is100ContinueRequest;

/**
 * State between start and end of inbound request headers read.
 */
public class ReceivingHeaders implements ListenerState {

    private static Logger log = LoggerFactory.getLogger(ReceivingHeaders.class);
    private final SourceHandler sourceHandler;
    private final HandlerExecutor handlerExecutor;
    private final StateContext stateContext;
    private HttpCarbonMessage inboundRequestMsg;

    private final int maximumEvents = 3; //TODO: We should let the user configure this
    private int sequenceId = 1; //Keep track of the request order for http 1.1 pipelining
    private final Queue holdingQueue = new PriorityQueue<>(NUMBER_OF_INITIAL_EVENTS_HELD);

    public ReceivingHeaders(SourceHandler sourceHandler, StateContext stateContext) {
        this.sourceHandler = sourceHandler;
        this.handlerExecutor = HttpTransportContextHolder.getInstance().getHandlerExecutor();
        this.stateContext = stateContext;
    }

    @Override
    public void readInboundRequestHeaders(HttpCarbonMessage inboundRequestMsg, HttpRequest inboundRequestHeaders) {
        this.inboundRequestMsg = inboundRequestMsg;
        boolean continueRequest = is100ContinueRequest(inboundRequestMsg);
        if (continueRequest) {
            stateContext.setListenerState(new Expect100ContinueHeaderReceived(stateContext, sourceHandler));
        }
        notifyRequestListener(inboundRequestMsg);

        if (inboundRequestHeaders.decoderResult().isFailure()) {
            log.warn(inboundRequestHeaders.decoderResult().cause().getMessage());
        }
        if (handlerExecutor != null) {
            handlerExecutor.executeAtSourceRequestReceiving(inboundRequestMsg);
        }
    }

    private void notifyRequestListener(HttpCarbonMessage httpRequestMsg) {
        if (handlerExecutor != null) {
            handlerExecutor.executeAtSourceRequestReceiving(httpRequestMsg);
        }

        if (sourceHandler.getServerConnectorFuture() != null) {
            try {
                ServerConnectorFuture outboundRespFuture = httpRequestMsg.getHttpResponseFuture();
                outboundRespFuture.setHttpConnectorListener(
                        new HttpOutboundRespListener(httpRequestMsg, sourceHandler));

                //Set the pipelining properties just before notifying the listener about the request for the first time
                //because in case the response got ready before receiving the last HTTP content there's a possibility
                //of seeing an incorrect sequence number
                setPipeliningProperties();
                httpRequestMsg.setSourceContext(sourceHandler.getInboundChannelContext());
                sourceHandler.getServerConnectorFuture().notifyHttpListener(httpRequestMsg);
            } catch (Exception e) {
                log.error("Error while notifying listeners", e);
            }
        } else {
            log.error("Cannot find registered listener to forward the message");
        }
    }

    @Override
    public void readInboundRequestEntityBody(Object inboundRequestEntityBody) throws ServerConnectorException {
        stateContext.setListenerState(new ReceivingEntityBody(stateContext, inboundRequestMsg, sourceHandler));
        stateContext.getListenerState().readInboundRequestEntityBody(inboundRequestEntityBody);
    }

    @Override
    public void writeOutboundResponseHeaders(HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        // Not a dependant action of this state.
    }

    @Override
    public void writeOutboundResponseEntityBody(HttpOutboundRespListener outboundResponseListener,
                                                HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        // Not a dependant action of this state.
    }

    @Override
    public void handleAbruptChannelClosure(ServerConnectorFuture serverConnectorFuture) {
        handleIncompleteInboundRequest(REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST_HEADERS);
    }

    @Override
    public ChannelFuture handleIdleTimeoutConnectionClosure(ServerConnectorFuture serverConnectorFuture,
                                                            ChannelHandlerContext ctx) {
        ChannelFuture outboundRespFuture = sendRequestTimeoutResponse(ctx, REQUEST_TIMEOUT, EMPTY_BUFFER);
        outboundRespFuture.addListener((ChannelFutureListener) channelFuture -> {
            Throwable cause = channelFuture.cause();
            if (cause != null) {
                log.warn("Failed to send: {}", cause.getMessage());
            }
            sourceHandler.channelInactive(ctx);
            handleIncompleteInboundRequest(IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_REQUEST_HEADERS);
        });
        return outboundRespFuture;
    }

    private ChannelFuture sendRequestTimeoutResponse(ChannelHandlerContext ctx, HttpResponseStatus status,
                                                     ByteBuf content) {
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
        outboundResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
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

    //Set pipeline related properties. These should be set only once per connection.
    private void setPipeliningProperties() {
        ChannelHandlerContext inboundChannelContext = sourceHandler.getInboundChannelContext();
        inboundRequestMsg.setSequenceId(sequenceId);
        sequenceId++;
        if (inboundChannelContext.channel().attr(Constants.NEXT_SEQUENCE_NUMBER).get() == null) {
            inboundChannelContext.channel().attr(Constants.MAX_RESPONSES_ALLOWED_TO_BE_QUEUED).set(maximumEvents);
        }
        if (inboundChannelContext.channel().attr(Constants.RESPONSE_QUEUE).get() == null) {
            inboundChannelContext.channel().attr(Constants.RESPONSE_QUEUE).set(holdingQueue);
        }
        if (inboundChannelContext.channel().attr(Constants.NEXT_SEQUENCE_NUMBER).get() == null) {
            inboundChannelContext.channel().attr(Constants.NEXT_SEQUENCE_NUMBER).set(EXPECTED_SEQUENCE_NUMBER);
        }
    }
}
