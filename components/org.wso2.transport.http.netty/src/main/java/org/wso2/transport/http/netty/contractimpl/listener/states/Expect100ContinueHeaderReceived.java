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

package org.wso2.transport.http.netty.contractimpl.listener.states;

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
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.HttpOutboundRespListener;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.contractimpl.listener.SourceHandler;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static org.wso2.transport.http.netty.contract.Constants
        .IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_100_CONTINUE_RESPONSE;
import static org.wso2.transport.http.netty.contract.Constants
        .REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_100_CONTINUE_RESPONSE;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.CONNECTOR_NOTIFYING_ERROR;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.ILLEGAL_STATE_ERROR;

/**
 * Special state of receiving request with expect:100-continue header.
 */
public class Expect100ContinueHeaderReceived implements ListenerState {

    private static final Logger LOG = LoggerFactory.getLogger(Expect100ContinueHeaderReceived.class);

    private final ListenerReqRespStateManager listenerReqRespStateManager;
    private final SourceHandler sourceHandler;
    private final HttpCarbonMessage inboundRequestMsg;
    private final float httpVersion;

    Expect100ContinueHeaderReceived(ListenerReqRespStateManager listenerReqRespStateManager,
                                  SourceHandler sourceHandler, HttpCarbonMessage inboundRequestMsg, float httpVersion) {
        this.listenerReqRespStateManager = listenerReqRespStateManager;
        this.sourceHandler = sourceHandler;
        this.inboundRequestMsg = inboundRequestMsg;
        this.httpVersion = httpVersion;
    }

    @Override
    public void readInboundRequestHeaders(HttpCarbonMessage inboundRequestMsg, HttpRequest inboundRequestHeaders) {
        LOG.warn("readInboundRequestHeaders {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void readInboundRequestBody(Object inboundRequestEntityBody) throws ServerConnectorException {
        // Client may send request body without/after waiting for the 100-continue response.
        listenerReqRespStateManager.state = new ReceivingEntityBody(listenerReqRespStateManager,
                                                                    inboundRequestMsg, sourceHandler, httpVersion);
        listenerReqRespStateManager.readInboundRequestBody(inboundRequestEntityBody);
    }

    @Override
    public void writeOutboundResponseHeaders(HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        LOG.warn("writeOutboundResponseHeaders {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void writeOutboundResponseBody(HttpOutboundRespListener outboundResponseListener,
                                          HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        if (Util.getHttpResponseStatus(outboundResponseMsg).code() == HttpResponseStatus.CONTINUE.code()) {
            listenerReqRespStateManager.state =
                    new Response100ContinueSent(listenerReqRespStateManager, sourceHandler, outboundResponseListener);
        } else {
            listenerReqRespStateManager.state =
                    new EntityBodyReceived(listenerReqRespStateManager, sourceHandler, httpVersion);
        }
        listenerReqRespStateManager.writeOutboundResponseBody(outboundResponseListener, outboundResponseMsg,
                                                                         httpContent);
    }

    @Override
    public void handleAbruptChannelClosure(ServerConnectorFuture serverConnectorFuture) {
        try {
            serverConnectorFuture.notifyErrorListener(
                    new ServerConnectorException(REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_100_CONTINUE_RESPONSE));
        } catch (ServerConnectorException e) {
            LOG.error(CONNECTOR_NOTIFYING_ERROR, e);
        }
    }

    @Override
    public ChannelFuture handleIdleTimeoutConnectionClosure(ServerConnectorFuture serverConnectorFuture,
                                                            ChannelHandlerContext ctx) {
        try {
            serverConnectorFuture.notifyErrorListener(
                    new ServerConnectorException(IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_100_CONTINUE_RESPONSE));
        } catch (ServerConnectorException e) {
            LOG.error(CONNECTOR_NOTIFYING_ERROR, e);
        }
        String responseValue = "Server time out";
        ChannelFuture outboundRespFuture =
                sendRequestTimeoutResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                           copiedBuffer(responseValue, CharsetUtil.UTF_8), responseValue.length());
        outboundRespFuture.addListener((ChannelFutureListener) channelFuture -> {
            Throwable cause = channelFuture.cause();
            if (cause != null) {
                LOG.warn("Failed to send: {}", cause.getMessage());
            }
            ctx.close();
        });
        return outboundRespFuture;
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
