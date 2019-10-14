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
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.Http2OutboundRespListener;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2StateUtil;
import org.wso2.transport.http.netty.contractimpl.listener.http2.Http2SourceHandler;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.REQUEST_TIMEOUT;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_CLIENT_CLOSED_WHILE_WRITING_100_CONTINUE_RESPONSE;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.ILLEGAL_STATE_ERROR;

/**
 * Writes 100-continue response to the caller.
 */
public class Response100ContinueSent implements ListenerState {
    private static final Logger LOG = LoggerFactory.getLogger(
            Response100ContinueSent.class);
    private final Http2MessageStateContext http2MessageStateContext;

    public Response100ContinueSent(Http2MessageStateContext http2MessageStateContext) {
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
                                             int streamId) throws Http2Exception {
        LOG.warn("writeOutboundResponseHeaders {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void writeOutboundResponseBody(Http2OutboundRespListener http2OutboundRespListener,
                                          HttpCarbonMessage outboundResponseMsg, HttpContent httpContent, int streamId)
            throws Http2Exception {
        // Send a '100 Continue' response.
        ChannelHandlerContext ctx = http2OutboundRespListener.getChannelHandlerContext();
        ChannelFuture channelFuture = http2OutboundRespListener.getEncoder().writeHeaders(
                ctx, streamId,
                new DefaultHttp2Headers(false).status(CONTINUE.codeAsText()), 0, false, ctx.voidPromise());

        http2OutboundRespListener.getEncoder().flowController().writePendingBytes();
        ctx.flush();
        http2MessageStateContext.setListenerState(
                new SendingHeaders(http2OutboundRespListener, http2MessageStateContext));

        addResponseWriteFailureListener(http2OutboundRespListener.getOutboundRespStatusFuture(), channelFuture,
                                        http2OutboundRespListener);
    }

    @Override
    public void writeOutboundPromise(Http2OutboundRespListener http2OutboundRespListener, Http2PushPromise pushPromise)
            throws Http2Exception {
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
        LOG.error(REMOTE_CLIENT_CLOSED_WHILE_WRITING_100_CONTINUE_RESPONSE);
    }

    private static void addResponseWriteFailureListener(HttpResponseFuture outboundRespStatusFuture,
                                                        ChannelFuture channelFuture,
                                                        Http2OutboundRespListener http2OutboundRespListener) {
        channelFuture.addListener(writeOperationPromise -> {
            Throwable throwable = writeOperationPromise.cause();
            if (throwable != null) {
                if (throwable instanceof ClosedChannelException) {
                    throwable = new IOException(REMOTE_CLIENT_CLOSED_WHILE_WRITING_100_CONTINUE_RESPONSE);
                }
                http2OutboundRespListener.removeDefaultResponseWriter();
                outboundRespStatusFuture.notifyHttpListener(throwable);
            } else {
                outboundRespStatusFuture.notifyHttpListener(http2OutboundRespListener.getInboundRequestMsg());
            }
        });
    }
}
