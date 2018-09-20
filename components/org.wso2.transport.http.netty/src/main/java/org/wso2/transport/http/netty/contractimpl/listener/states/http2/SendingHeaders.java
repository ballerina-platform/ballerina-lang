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
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contractimpl.Http2OutboundRespListener;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static org.wso2.transport.http.netty.contractimpl.common.states.Http2StateUtil.releaseDataFrame;
import static org.wso2.transport.http.netty.contractimpl.common.states.Http2StateUtil.validatePromisedStreamState;
import static org.wso2.transport.http.netty.contractimpl.common.states.Http2StateUtil.writeHttp2Headers;

/**
 * State between start and end of outbound response headers write.
 */
public class SendingHeaders implements ListenerState {

    private static final Logger LOG = LoggerFactory.getLogger(SendingHeaders.class);

    private final Http2MessageStateContext http2MessageStateContext;
    private final ChannelHandlerContext ctx;
    private final Http2Connection conn;
    private final Http2ConnectionEncoder encoder;
    private final HttpResponseFuture outboundRespStatusFuture;
    private final HttpCarbonMessage inboundRequestMsg;
    private final int originalStreamId;
    private final String serverName;

    public SendingHeaders(Http2OutboundRespListener http2OutboundRespListener,
                          Http2MessageStateContext http2MessageStateContext) {
        this.http2MessageStateContext = http2MessageStateContext;
        this.ctx = http2OutboundRespListener.getChannelHandlerContext();
        this.conn = http2OutboundRespListener.getConnection();
        this.encoder = http2OutboundRespListener.getEncoder();
        this.outboundRespStatusFuture = http2OutboundRespListener.getOutboundRespStatusFuture();
        this.inboundRequestMsg = http2OutboundRespListener.getInboundRequestMsg();
        this.serverName = http2OutboundRespListener.getServerName();
        this.originalStreamId = http2OutboundRespListener.getOriginalStreamId();
    }

    @Override
    public void readInboundRequestHeaders(Http2HeadersFrame headersFrame) {
        LOG.warn("readInboundRequestHeaders is not a dependant action of this state");
    }

    @Override
    public void readInboundRequestBody(Http2DataFrame dataFrame) {
        // Response is already started to send, hence the incoming data frames need to be released.
        releaseDataFrame(dataFrame);
    }

    @Override
    public void writeOutboundResponseHeaders(Http2OutboundRespListener http2OutboundRespListener,
                                             HttpCarbonMessage outboundResponseMsg, HttpContent httpContent,
                                             int streamId) throws Http2Exception {
        writeHeaders(outboundResponseMsg, streamId);
        http2MessageStateContext.setListenerState(
                new SendingEntityBody(http2OutboundRespListener, http2MessageStateContext));
        http2MessageStateContext.getListenerState()
                .writeOutboundResponseBody(http2OutboundRespListener, outboundResponseMsg, httpContent, streamId);
    }

    @Override
    public void writeOutboundResponseBody(Http2OutboundRespListener http2OutboundRespListener,
                                          HttpCarbonMessage outboundResponseMsg, HttpContent httpContent,
                                          int streamId) throws Http2Exception {
        // When the initial frames of the response for the upgraded request is to be sent.
        writeOutboundResponseHeaders(http2OutboundRespListener, outboundResponseMsg, httpContent, streamId);
    }

    @Override
    public void writeOutboundPromise(Http2OutboundRespListener http2OutboundRespListener,
                                     Http2PushPromise pushPromise) {
        LOG.warn("writeOutboundPromise is not a dependant action of this state");
    }

    private void writeHeaders(HttpCarbonMessage outboundResponseMsg, int streamId) throws Http2Exception {
        outboundResponseMsg.getHeaders().
                add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), Constants.HTTP_SCHEME);
        HttpMessage httpMessage =
                Util.createHttpResponse(outboundResponseMsg, Constants.HTTP2_VERSION, serverName, true);
        // Construct Http2 headers
        Http2Headers http2Headers = HttpConversionUtil.toHttp2Headers(httpMessage, true);
        validatePromisedStreamState(originalStreamId, streamId, conn, inboundRequestMsg);
        writeHttp2Headers(ctx, encoder, outboundRespStatusFuture, streamId, http2Headers, false);
    }
}
