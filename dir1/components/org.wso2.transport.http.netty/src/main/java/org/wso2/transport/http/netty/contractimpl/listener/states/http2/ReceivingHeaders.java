/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.Http2OutboundRespListener;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2StateUtil;
import org.wso2.transport.http.netty.contractimpl.listener.http2.Http2SourceHandler;
import org.wso2.transport.http.netty.contractimpl.listener.http2.InboundMessageHolder;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static io.netty.handler.codec.http.HttpResponseStatus.REQUEST_TIMEOUT;
import static org.wso2.transport.http.netty.contract.Constants.HTTP2_METHOD;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST_HEADERS;
import static org.wso2.transport.http.netty.contractimpl.common.Util.is100ContinueRequest;
import static org.wso2.transport.http.netty.contractimpl.common.states.Http2StateUtil.notifyRequestListener;
import static org.wso2.transport.http.netty.contractimpl.common.states.Http2StateUtil.setupCarbonRequest;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.handleIncompleteInboundMessage;

/**
 * State between start and end of inbound request headers read.
 *
 * @since 6.0.241
 */
public class ReceivingHeaders implements ListenerState {

    private static final Logger LOG = LoggerFactory.getLogger(ReceivingHeaders.class);

    private final Http2SourceHandler http2SourceHandler;
    private final Http2MessageStateContext http2MessageStateContext;

    public ReceivingHeaders(Http2SourceHandler http2SourceHandler, Http2MessageStateContext http2MessageStateContext) {
        this.http2SourceHandler = http2SourceHandler;
        this.http2MessageStateContext = http2MessageStateContext;
    }

    @Override
    public void readInboundRequestHeaders(ChannelHandlerContext ctx, Http2HeadersFrame headersFrame)
            throws Http2Exception {
        int streamId = headersFrame.getStreamId();
        if (headersFrame.isEndOfStream()) {
            // Retrieve HTTP request and add last http content.
            InboundMessageHolder inboundMessageHolder = http2SourceHandler.getStreamIdRequestMap().get(streamId);
            HttpCarbonMessage sourceReqCMsg =
                    inboundMessageHolder != null ? inboundMessageHolder.getInboundMsg() : null;

            if (headersFrame.getHeaders().contains(HTTP2_METHOD)) {
                // if the header frame is an initial header frame and also it has endOfStream
                sourceReqCMsg = setupHttp2CarbonMsg(headersFrame.getHeaders(), streamId);

                //(https://httpwg.org/specs/rfc7231.html#header.expect)A client MUST NOT generate a 100-continue
                // expectation in a request that does not include a message body.
                //TODO:Handle how the server should react to this situation.

                // Add empty last http content if no data frames available in the http request
                sourceReqCMsg.addHttpContent(new DefaultLastHttpContent());
                initializeDataEventListeners(ctx, streamId, sourceReqCMsg);
                sourceReqCMsg.setHttp2MessageStateContext(http2MessageStateContext);
            }
            http2MessageStateContext.setListenerState(new EntityBodyReceived(http2MessageStateContext));
        } else {
            // Construct new HTTP Request
            HttpCarbonMessage sourceReqCMsg = setupHttp2CarbonMsg(headersFrame.getHeaders(), streamId);
            sourceReqCMsg.setHttp2MessageStateContext(http2MessageStateContext);
            initializeDataEventListeners(ctx, streamId, sourceReqCMsg);
            if (is100ContinueRequest(sourceReqCMsg)) {
                http2MessageStateContext.setListenerState(
                        new Expect100ContinueHeaderReceived(http2MessageStateContext));
            } else {
                http2MessageStateContext.setListenerState(new ReceivingEntityBody(http2MessageStateContext));
            }
        }
    }

    private void initializeDataEventListeners(ChannelHandlerContext ctx, int streamId,
                                              HttpCarbonMessage sourceReqCMsg) {
        InboundMessageHolder inboundMsgHolder = new InboundMessageHolder(sourceReqCMsg);
        // storing to add HttpContent later
        http2SourceHandler.getStreamIdRequestMap().put(streamId, inboundMsgHolder);
        http2SourceHandler.getHttp2ServerChannel().getDataEventListeners()
                .forEach(dataEventListener -> dataEventListener.onStreamInit(ctx, streamId));
        notifyRequestListener(http2SourceHandler, inboundMsgHolder, streamId);
    }

    @Override
    public void readInboundRequestBody(Http2SourceHandler http2SourceHandler, Http2DataFrame dataFrame) {
        LOG.warn("readInboundRequestBody is not a dependant action of this state");
    }

    @Override
    public void writeOutboundResponseHeaders(Http2OutboundRespListener http2OutboundRespListener,
                                             HttpCarbonMessage outboundResponseMsg, HttpContent httpContent,
                                             int streamId) {
        LOG.warn("writeOutboundResponseHeaders is not a dependant action of this state");
    }

    @Override
    public void writeOutboundResponseBody(Http2OutboundRespListener http2OutboundRespListener,
                                          HttpCarbonMessage outboundResponseMsg, HttpContent httpContent,
                                          int streamId) {
        LOG.warn("writeOutboundResponseBody is not a dependant action of this state");
    }

    @Override
    public void writeOutboundPromise(Http2OutboundRespListener http2OutboundRespListener,
                                     Http2PushPromise pushPromise) {
        LOG.warn("writeOutboundPromise is not a dependant action of this state");
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
        handleIncompleteInboundMessage(http2OutboundRespListener.getInboundRequestMsg(),
                                       REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST_HEADERS);
    }

    private HttpCarbonMessage setupHttp2CarbonMsg(Http2Headers http2Headers, int streamId) throws Http2Exception {
        return setupCarbonRequest(Util.createHttpRequestFromHttp2Headers(http2Headers, streamId), http2SourceHandler,
                                  streamId);
    }
}
