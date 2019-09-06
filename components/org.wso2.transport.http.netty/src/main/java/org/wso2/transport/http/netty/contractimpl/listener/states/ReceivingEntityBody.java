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
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.HttpOutboundRespListener;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.contractimpl.listener.SourceHandler;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.internal.HttpTransportContextHolder;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpResponseStatus.REQUEST_TIMEOUT;
import static org.wso2.transport.http.netty.contract.Constants
        .IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_REQUEST_BODY;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST_BODY;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.ILLEGAL_STATE_ERROR;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.handleIncompleteInboundMessage;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.respondToIncompleteRequest;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.sendRequestTimeoutResponse;

/**
 * State between start and end of payload read.
 */
public class ReceivingEntityBody implements ListenerState {

    private static final Logger LOG = LoggerFactory.getLogger(ReceivingEntityBody.class);

    private final HandlerExecutor handlerExecutor;
    private final ServerConnectorFuture serverConnectorFuture;
    private final ListenerReqRespStateManager listenerReqRespStateManager;
    private final SourceHandler sourceHandler;
    private final HttpCarbonMessage inboundRequestMsg;
    private final float httpVersion;

    ReceivingEntityBody(ListenerReqRespStateManager listenerReqRespStateManager, HttpCarbonMessage inboundRequestMsg,
                        SourceHandler sourceHandler, float httpVersion) {
        this.listenerReqRespStateManager = listenerReqRespStateManager;
        this.inboundRequestMsg = inboundRequestMsg;
        this.sourceHandler = sourceHandler;
        this.handlerExecutor = HttpTransportContextHolder.getInstance().getHandlerExecutor();
        this.serverConnectorFuture = sourceHandler.getServerConnectorFuture();
        this.httpVersion = httpVersion;
    }

    @Override
    public void readInboundRequestHeaders(HttpCarbonMessage inboundRequestMsg, HttpRequest inboundRequestHeaders) {
        LOG.warn("readInboundRequestHeaders {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void readInboundRequestBody(Object inboundRequestEntityBody) throws ServerConnectorException {
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
                    inboundRequestMsg.setLastHttpContentArrived();
                    sourceHandler.resetInboundRequestMsg();
                    listenerReqRespStateManager.state
                            = new EntityBodyReceived(listenerReqRespStateManager, sourceHandler, httpVersion);
                }
            } catch (RuntimeException ex) {
                httpContent.release();
                inboundRequestMsg.notifyContentFailure(ex);
                LOG.warn("Response already received before completing the inbound request {}", ex.getMessage());
            }
        }
    }

    @Override
    public void writeOutboundResponseHeaders(HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        LOG.warn("writeOutboundResponseHeaders {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void writeOutboundResponseBody(HttpOutboundRespListener outboundResponseListener,
                                          HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        // If this method is called, it's an application error. Connection needs to be closed once the response is sent.
        if (Util.getHttpResponseStatus(outboundResponseMsg).code() != HttpResponseStatus.CONTINUE.code()) {
            respondToIncompleteRequest(sourceHandler.getInboundChannelContext().channel(), outboundResponseListener,
                                       listenerReqRespStateManager, outboundResponseMsg, httpContent,
                                       REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST_BODY);
        }
    }

    @Override
    public void handleAbruptChannelClosure(ServerConnectorFuture serverConnectorFuture) {
        handleIncompleteInboundMessage(inboundRequestMsg, REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST_BODY);
    }

    @Override
    public ChannelFuture handleIdleTimeoutConnectionClosure(ServerConnectorFuture serverConnectorFuture,
                                                            ChannelHandlerContext ctx) {

        ByteBuf responseBody =
                copiedBuffer(IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_REQUEST_BODY, CharsetUtil.UTF_8);
        ChannelFuture outboundRespFuture = sendRequestTimeoutResponse(ctx, REQUEST_TIMEOUT,
                                                                      responseBody, responseBody.readableBytes(),
                                                                      httpVersion, sourceHandler.getServerName());
        outboundRespFuture.addListener((ChannelFutureListener) channelFuture -> {
            Throwable cause = channelFuture.cause();
            if (cause != null) {
                LOG.warn("Failed to send: {}", cause.getMessage());
            }
            ctx.close();
            handleIncompleteInboundMessage(inboundRequestMsg,
                                           IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_REQUEST_BODY);
        });
        listenerReqRespStateManager.state
                = new ResponseCompleted(listenerReqRespStateManager, sourceHandler, inboundRequestMsg);
        return outboundRespFuture;
    }

    private boolean isDiffered(HttpCarbonMessage sourceReqCmsg) {
        //Http resource stored in the HTTPCarbonMessage means execution waits till payload.
        return sourceReqCmsg.getProperty(Constants.HTTP_RESOURCE) != null;
    }
}
