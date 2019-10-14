/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.contractimpl.listener.states.http2;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.Http2Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.Http2OutboundRespListener;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2StateUtil;
import org.wso2.transport.http.netty.contractimpl.listener.http2.Http2SourceHandler;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static io.netty.handler.codec.http.HttpResponseStatus.REQUEST_TIMEOUT;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_100_CONTINUE_RESPONSE;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.CONNECTOR_NOTIFYING_ERROR;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.ILLEGAL_STATE_ERROR;

/**
 * Listener goes into {@code Expect100ContinueHeaderReceived} state when the expect header is detected.
 */
public class Expect100ContinueHeaderReceived implements ListenerState {
    private static final Logger LOG = LoggerFactory.getLogger(Expect100ContinueHeaderReceived.class);

    private final Http2MessageStateContext http2MessageStateContext;

    Expect100ContinueHeaderReceived(Http2MessageStateContext http2MessageStateContext) {
        this.http2MessageStateContext = http2MessageStateContext;
    }

    @Override
    public void readInboundRequestHeaders(ChannelHandlerContext ctx, Http2HeadersFrame headersFrame) {
        LOG.warn("readInboundRequestHeaders {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void readInboundRequestBody(Http2SourceHandler http2SourceHandler, Http2DataFrame dataFrame)
            throws Http2Exception {
        //Client might start sending request body without waiting for the 100-continue response.
        http2MessageStateContext.setListenerState(new ReceivingEntityBody(http2MessageStateContext));
        http2MessageStateContext.getListenerState().readInboundRequestBody(http2SourceHandler, dataFrame);
    }

    @Override
    public void writeOutboundResponseHeaders(Http2OutboundRespListener http2OutboundRespListener,
                                             HttpCarbonMessage outboundResponseMsg, HttpContent httpContent,
                                             int streamId) {
        LOG.warn("writeOutboundResponseHeaders {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void writeOutboundResponseBody(Http2OutboundRespListener http2OutboundRespListener,
                                          HttpCarbonMessage outboundResponseMsg, HttpContent httpContent, int streamId)
            throws Http2Exception {
        if (Util.getHttpResponseStatus(outboundResponseMsg).code() == HttpResponseStatus.CONTINUE.code()) {
            http2MessageStateContext.setListenerState(new Response100ContinueSent(http2MessageStateContext));
        } else {
            http2MessageStateContext.setListenerState(new EntityBodyReceived(http2MessageStateContext));
        }
        http2MessageStateContext.getListenerState().writeOutboundResponseBody(http2OutboundRespListener,
                                                                              outboundResponseMsg,
                                                                              httpContent, streamId);
    }

    @Override
    public void writeOutboundPromise(Http2OutboundRespListener http2OutboundRespListener,
                                     Http2PushPromise pushPromise) {
        LOG.warn("writeOutboundPromise {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void handleStreamTimeout(ServerConnectorFuture serverConnectorFuture, ChannelHandlerContext ctx,
                                    Http2OutboundRespListener http2OutboundRespListener, int streamId) {
        Http2StateUtil.sendRequestTimeoutResponse(ctx, http2OutboundRespListener, streamId, REQUEST_TIMEOUT,
                                                  Unpooled.EMPTY_BUFFER, true, true);
    }

    @Override
    public void handleAbruptChannelClosure(ServerConnectorFuture serverConnectorFuture, ChannelHandlerContext ctx,
                                           Http2OutboundRespListener http2OutboundRespListener, int streamId) {
        try {
            serverConnectorFuture.notifyErrorListener(
                    new ServerConnectorException(REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_100_CONTINUE_RESPONSE));
        } catch (ServerConnectorException e) {
            LOG.error(CONNECTOR_NOTIFYING_ERROR, e);
        }
    }
}
