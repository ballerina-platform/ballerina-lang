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

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.Util;
import org.wso2.transport.http.netty.config.KeepAliveConfig;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.HttpOutboundRespListener;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.internal.HttpTransportContextHolder;
import org.wso2.transport.http.netty.listener.SourceHandler;
import org.wso2.transport.http.netty.listener.states.StateContext;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static io.netty.handler.codec.http.HttpResponseStatus.REQUEST_TIMEOUT;
import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_REQUEST_BODY;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST_BODY;
import static org.wso2.transport.http.netty.listener.states.StateUtil.handleIncompleteInboundMessage;
import static org.wso2.transport.http.netty.listener.states.StateUtil.sendRequestTimeoutResponse;

/**
 * State between start and end of payload read
 */
public class ReceivingEntityBody implements ListenerState {

    private static Logger log = LoggerFactory.getLogger(ReceivingEntityBody.class);
    private final HandlerExecutor handlerExecutor;
    private final ServerConnectorFuture serverConnectorFuture;
    private final StateContext stateContext;
    private final SourceHandler sourceHandler;
    private final HttpCarbonMessage inboundRequestMsg;
    private final float httpVersion;

    ReceivingEntityBody(StateContext stateContext, HttpCarbonMessage inboundRequestMsg, SourceHandler sourceHandler,
                        float httpVersion) {
        this.stateContext = stateContext;
        this.inboundRequestMsg = inboundRequestMsg;
        this.sourceHandler = sourceHandler;
        this.handlerExecutor = HttpTransportContextHolder.getInstance().getHandlerExecutor();
        this.serverConnectorFuture = sourceHandler.getServerConnectorFuture();
        this.httpVersion = httpVersion;
    }

    @Override
    public void readInboundRequestHeaders(HttpCarbonMessage inboundRequestMsg, HttpRequest inboundRequestHeaders) {
        // Not a dependant action of this state.
    }

    @Override
    public void readInboundRequestEntityBody(Object inboundRequestEntityBody) throws ServerConnectorException {
        if (inboundRequestEntityBody instanceof HttpContent) {
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
                    sourceHandler.resetInboundRequestMsg();
                    stateContext.setListenerState(new EntityBodyReceived(stateContext, sourceHandler, httpVersion));
                }
            } catch (RuntimeException ex) {
                httpContent.release();
                log.warn("Response already received before completing the inbound request {}", ex.getMessage());
            }
        }
    }

    @Override
    public void writeOutboundResponseHeaders(HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        // Not a dependant action of this state.
    }

    @Override
    public void writeOutboundResponseEntityBody(HttpOutboundRespListener outboundResponseListener,
                                                HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        // If this method is called, it is an application error. we need to close connection once response is sent.
        outboundResponseListener.setKeepAliveConfig(KeepAliveConfig.NEVER);
        stateContext.setListenerState(new SendingHeaders(outboundResponseListener, stateContext));
        stateContext.getListenerState().writeOutboundResponseHeaders(outboundResponseMsg, httpContent);
    }

    @Override
    public void handleAbruptChannelClosure(ServerConnectorFuture serverConnectorFuture) {
        handleIncompleteInboundMessage(inboundRequestMsg, REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST_BODY);
    }

    @Override
    public ChannelFuture handleIdleTimeoutConnectionClosure(ServerConnectorFuture serverConnectorFuture,
                                                            ChannelHandlerContext ctx) {
        ChannelFuture outboundRespFuture = sendRequestTimeoutResponse(ctx, REQUEST_TIMEOUT, Unpooled.EMPTY_BUFFER, 0,
                                                                      httpVersion, sourceHandler.getServerName());
        outboundRespFuture.addListener((ChannelFutureListener) channelFuture -> {
            Throwable cause = channelFuture.cause();
            if (cause != null) {
                log.warn("Failed to send: {}", cause.getMessage());
            }
            sourceHandler.channelInactive(ctx);
            handleIncompleteInboundMessage(inboundRequestMsg,
                                           IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_REQUEST_BODY);
        });
        return outboundRespFuture;
    }

    private boolean isDiffered(HttpCarbonMessage sourceReqCmsg) {
        //Http resource stored in the HTTPCarbonMessage means execution waits till payload.
        return sourceReqCmsg.getProperty(Constants.HTTP_RESOURCE) != null;
    }
}
