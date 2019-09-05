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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.HttpOutboundRespListener;
import org.wso2.transport.http.netty.contractimpl.listener.SourceHandler;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.internal.HttpTransportContextHolder;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static io.netty.buffer.Unpooled.EMPTY_BUFFER;
import static io.netty.handler.codec.http.HttpResponseStatus.REQUEST_TIMEOUT;
import static org.wso2.transport.http.netty.contract.Constants
        .IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_REQUEST_HEADERS;
import static org.wso2.transport.http.netty.contract.Constants
        .REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST_HEADERS;
import static org.wso2.transport.http.netty.contractimpl.common.Util.is100ContinueRequest;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.ILLEGAL_STATE_ERROR;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.handleIncompleteInboundMessage;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.respondToIncompleteRequest;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.sendRequestTimeoutResponse;

/**
 * State between start and end of inbound request headers read.
 */
public class ReceivingHeaders implements ListenerState {

    private static final Logger LOG = LoggerFactory.getLogger(ReceivingHeaders.class);

    private final SourceHandler sourceHandler;
    private final HandlerExecutor handlerExecutor;
    private final ListenerReqRespStateManager listenerReqRespStateManager;
    private HttpCarbonMessage inboundRequestMsg;
    private float httpVersion;

    public ReceivingHeaders(ListenerReqRespStateManager listenerReqRespStateManager, SourceHandler sourceHandler) {
        this.listenerReqRespStateManager = listenerReqRespStateManager;
        this.sourceHandler = sourceHandler;
        this.handlerExecutor = HttpTransportContextHolder.getInstance().getHandlerExecutor();
    }

    @Override
    public void readInboundRequestHeaders(HttpCarbonMessage inboundRequestMsg, HttpRequest inboundRequestHeaders) {
        this.inboundRequestMsg = inboundRequestMsg;
        this.httpVersion = Float.parseFloat(inboundRequestMsg.getHttpVersion());
        boolean continueRequest = is100ContinueRequest(inboundRequestMsg);
        if (continueRequest) {
            listenerReqRespStateManager.state =
                    new Expect100ContinueHeaderReceived(listenerReqRespStateManager, sourceHandler,
                                                        inboundRequestMsg, httpVersion);
        }
        notifyRequestListener(inboundRequestMsg);

        if (inboundRequestHeaders.decoderResult().isFailure()) {
            LOG.debug(inboundRequestHeaders.decoderResult().cause().getMessage());
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
                httpRequestMsg.setSourceContext(sourceHandler.getInboundChannelContext());
                sourceHandler.getServerConnectorFuture().notifyHttpListener(httpRequestMsg);
            } catch (Exception e) {
                LOG.error("Error while notifying listeners", e);
            }
        } else {
            LOG.error("Cannot find registered listener to forward the message");
        }
    }

    @Override
    public void readInboundRequestBody(Object inboundRequestEntityBody) throws ServerConnectorException {
        listenerReqRespStateManager.state
                = new ReceivingEntityBody(listenerReqRespStateManager, inboundRequestMsg, sourceHandler, httpVersion);
        listenerReqRespStateManager.readInboundRequestBody(inboundRequestEntityBody);
    }

    @Override
    public void writeOutboundResponseHeaders(HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        LOG.warn("writeOutboundResponseHeaders {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void writeOutboundResponseBody(HttpOutboundRespListener outboundResponseListener,
                                          HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        // If this method is called, it's an application error. Connection needs to be closed once the response is sent.
        respondToIncompleteRequest(sourceHandler.getInboundChannelContext().channel(), outboundResponseListener,
                                   listenerReqRespStateManager, outboundResponseMsg, httpContent,
                                   REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST_HEADERS);
    }

    @Override
    public void handleAbruptChannelClosure(ServerConnectorFuture serverConnectorFuture) {
        handleIncompleteInboundMessage(inboundRequestMsg, REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST_HEADERS);
    }

    @Override
    public ChannelFuture handleIdleTimeoutConnectionClosure(ServerConnectorFuture serverConnectorFuture,
                                                            ChannelHandlerContext ctx) {
        ChannelFuture outboundRespFuture = sendRequestTimeoutResponse(ctx, REQUEST_TIMEOUT, EMPTY_BUFFER, 0,
                                                                      httpVersion, sourceHandler.getServerName());
        outboundRespFuture.addListener((ChannelFutureListener) channelFuture -> {
            Throwable cause = channelFuture.cause();
            if (cause != null) {
                LOG.warn("Failed to send: {}", cause.getMessage());
            }
            ctx.close();
            handleIncompleteInboundMessage(inboundRequestMsg,
                                           IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_REQUEST_HEADERS);
        });
        listenerReqRespStateManager.state
                = new ResponseCompleted(listenerReqRespStateManager, sourceHandler, inboundRequestMsg);
        return outboundRespFuture;
    }
}
