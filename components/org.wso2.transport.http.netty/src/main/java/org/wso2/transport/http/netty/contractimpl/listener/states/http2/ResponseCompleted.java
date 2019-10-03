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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.Http2OutboundRespListener;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.listener.http2.Http2SourceHandler;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static org.wso2.transport.http.netty.contractimpl.common.states.Http2StateUtil.releaseDataFrame;
import static org.wso2.transport.http.netty.contractimpl.common.states.Http2StateUtil.sendRstFrame;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.ILLEGAL_STATE_ERROR;

/**
 * State of successfully written outbound response or push response.
 *
 * @since 6.0.241
 */
public class ResponseCompleted implements ListenerState {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseCompleted.class);

    private final Http2MessageStateContext http2MessageStateContext;
    private final ChannelHandlerContext ctx;
    private final Http2ConnectionEncoder encoder;
    private final int originalStreamId;

    ResponseCompleted(Http2OutboundRespListener http2OutboundRespListener,
                      Http2MessageStateContext http2MessageStateContext) {
        this.http2MessageStateContext = http2MessageStateContext;
        this.ctx = http2OutboundRespListener.getChannelHandlerContext();
        this.encoder = http2OutboundRespListener.getEncoder();
        this.originalStreamId = http2OutboundRespListener.getOriginalStreamId();
    }

    @Override
    public void readInboundRequestHeaders(ChannelHandlerContext ctx, Http2HeadersFrame headersFrame) {
        LOG.warn("readInboundRequestHeaders is not a dependant action of this state");
    }

    @Override
    public void readInboundRequestBody(Http2SourceHandler http2SourceHandler, Http2DataFrame dataFrame)
            throws Http2Exception {
        // Response is already sent, hence the incoming data frames need to be released.
        releaseDataFrame(http2SourceHandler, dataFrame);
        sendRstFrame(ctx, encoder, originalStreamId);
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
                                          int streamId) throws Http2Exception {
        // When promised response message is going to be sent after the original response or previous promised responses
        // has been sent.
        http2MessageStateContext.setListenerState(
                new SendingHeaders(http2OutboundRespListener, http2MessageStateContext));
        http2MessageStateContext.getListenerState()
                .writeOutboundResponseHeaders(http2OutboundRespListener, outboundResponseMsg, httpContent, streamId);
    }

    @Override
    public void writeOutboundPromise(Http2OutboundRespListener http2OutboundRespListener,
                                     Http2PushPromise pushPromise) throws Http2Exception {
        LOG.warn("writeOutboundPromise is not a dependant action of this state");
        throw new Http2Exception(Http2Error.PROTOCOL_ERROR,
                "WriteOutboundPromise is not a dependant action of ResponseCompleted state");
    }

    @Override
    public void handleStreamTimeout(ServerConnectorFuture serverConnectorFuture, ChannelHandlerContext ctx,
                                    Http2OutboundRespListener http2OutboundRespListener, int streamId) {
        LOG.warn("handleStreamTimeout {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void handleAbruptChannelClosure(ServerConnectorFuture serverConnectorFuture, ChannelHandlerContext ctx,
                                           Http2OutboundRespListener http2OutboundRespListener, int streamId) {
        LOG.warn("handleAbruptChannelClosure {}", ILLEGAL_STATE_ERROR);
    }
}
