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
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.HttpOutboundRespListener;
import org.wso2.transport.http.netty.listener.SourceHandler;
import org.wso2.transport.http.netty.listener.states.StateContext;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static org.wso2.transport.http.netty.common.Constants
        .IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_100_CONTINUE_RESPONSE;
import static org.wso2.transport.http.netty.common.Constants
        .REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_100_CONTINUE_RESPONSE;

/**
 * Special state of receiving request with expect:100-continue header
 */
public class Expect100ContinueHeaderReceived implements ListenerState {

    private static Logger log = LoggerFactory.getLogger(Expect100ContinueHeaderReceived.class);
    private final StateContext stateContext;
    private final SourceHandler sourceHandler;

    Expect100ContinueHeaderReceived(StateContext stateContext, SourceHandler sourceHandler) {
        this.stateContext = stateContext;
        this.sourceHandler = sourceHandler;
    }

    @Override
    public void readInboundRequestHeaders(HttpCarbonMessage inboundRequestMsg, HttpRequest inboundRequestHeaders) {
        // Not a dependant action of this state.
    }

    @Override
    public void readInboundRequestEntityBody(Object inboundRequestEntityBody) throws ServerConnectorException {
        // Not a dependant action of this state.
    }

    @Override
    public void writeOutboundResponseHeaders(HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        // Not a dependant action of this state.
    }

    @Override
    public void writeOutboundResponseEntityBody(HttpOutboundRespListener outboundResponseListener,
                                                HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        stateContext.setListenerState(new Response100ContinueSent(outboundResponseListener, sourceHandler, stateContext));
        stateContext.getListenerState().writeOutboundResponseEntityBody(outboundResponseListener, outboundResponseMsg,
                                                                        httpContent);
    }

    @Override
    public void handleAbruptChannelClosure(ServerConnectorFuture serverConnectorFuture) {
        try {
            serverConnectorFuture.notifyErrorListener(
                    new ServerConnectorException(REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_100_CONTINUE_RESPONSE));
        } catch (ServerConnectorException e) {
            log.error("Error while notifying error state to server-connector listener");
        }
    }

    @Override
    public ChannelFuture handleIdleTimeoutConnectionClosure(ServerConnectorFuture serverConnectorFuture,
                                                            ChannelHandlerContext ctx, IdleStateEvent evt) {
        if (evt.state() != IdleState.READER_IDLE) {
            String responseValue = "Server time out";
            ChannelFuture outboundRespFuture = sendRequestTimeoutResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                                          copiedBuffer(responseValue,
                                                                                       CharsetUtil.UTF_8),
                                                                          responseValue.length());
            outboundRespFuture.addListener((ChannelFutureListener) channelFuture -> {
                Throwable cause = channelFuture.cause();
                if (cause != null) {
                    log.warn("Failed to send: {}", cause.getMessage());
                }
                sourceHandler.channelInactive(ctx);
            });
            return outboundRespFuture;
        }

        try {
            serverConnectorFuture.notifyErrorListener(
                    new ServerConnectorException(IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_100_CONTINUE_RESPONSE));
        } catch (ServerConnectorException e) {
            log.error("Error while notifying error state to server-connector listener");
        }
        return null;
    }

    private ChannelFuture sendRequestTimeoutResponse(ChannelHandlerContext ctx, HttpResponseStatus status,
                                                     ByteBuf content, int length) {
        HttpResponse outboundResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        outboundResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, length);
        outboundResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, Constants.TEXT_PLAIN);
        outboundResponse.headers().set(HttpHeaderNames.CONNECTION.toString(), Constants.CONNECTION_CLOSE);
        outboundResponse.headers().set(HttpHeaderNames.SERVER.toString(), sourceHandler.getServerName());
        return ctx.channel().writeAndFlush(outboundResponse);
    }
}
