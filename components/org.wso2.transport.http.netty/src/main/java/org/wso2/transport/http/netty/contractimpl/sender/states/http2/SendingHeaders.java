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
package org.wso2.transport.http.netty.contractimpl.sender.states.http2;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2StateUtil;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2ClientChannel;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2TargetHandler;
import org.wso2.transport.http.netty.contractimpl.sender.http2.OutboundMsgHolder;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * State between start and end of outbound request header write
 */
public class SendingHeaders implements SenderState {

    private static final Logger LOG = LoggerFactory.getLogger(SendingHeaders.class);

    private final Http2TargetHandler http2TargetHandler;
    private final Http2TargetHandler.Http2RequestWriter http2RequestWriter;
    private final Http2MessageStateContext http2MessageStateContext;
    private final HttpCarbonMessage httpOutboundRequest;
    private final OutboundMsgHolder outboundMsgHolder;
    private final Http2Connection connection;
    private final Http2ConnectionEncoder encoder;
    private final Http2ClientChannel http2ClientChannel;
    private int streamId;

    public SendingHeaders(Http2TargetHandler http2TargetHandler,
                          Http2TargetHandler.Http2RequestWriter http2RequestWriter) {
        this.http2TargetHandler = http2TargetHandler;
        this.http2RequestWriter = http2RequestWriter;
        this.http2MessageStateContext = http2RequestWriter.getHttp2MessageStateContext();
        this.httpOutboundRequest = http2RequestWriter.getHttpOutboundRequest();
        this.outboundMsgHolder = http2RequestWriter.getOutboundMsgHolder();
        this.connection = http2TargetHandler.getConnection();
        this.encoder = http2TargetHandler.getEncoder();
        this.http2ClientChannel = http2TargetHandler.getHttp2ClientChannel();
    }

    @Override
    public void writeOutboundRequestHeaders(ChannelHandlerContext ctx, HttpContent httpContent) throws Http2Exception {
        writeHeaders(ctx, httpContent);
    }

    @Override
    public void writeOutboundRequestBody(ChannelHandlerContext ctx, HttpContent httpContent) throws Http2Exception {
        writeOutboundRequestHeaders(ctx, httpContent);
    }

    @Override
    public void readInboundResponseHeaders(ChannelHandlerContext ctx, Object msg, OutboundMsgHolder outboundMsgHolder,
                                           boolean isServerPush, Http2MessageStateContext http2MessageStateContext) {
        LOG.warn("readInboundResponseHeaders is not a dependant action of this state");
    }

    @Override
    public void readInboundResponseBody(ChannelHandlerContext ctx, Object msg, OutboundMsgHolder outboundMsgHolder,
                                        boolean isServerPush, Http2MessageStateContext http2MessageStateContext) {
        LOG.warn("readInboundResponseEntityBody is not a dependant action of this state");
    }

    @Override
    public void readInboundPromise(Http2PushPromise http2PushPromise, OutboundMsgHolder outboundMsgHolder) {
        LOG.warn("readInboundPromise is not a dependant action of this state");
    }

    private void writeHeaders(ChannelHandlerContext ctx, HttpContent msg) throws Http2Exception {
        // Initiate the stream
        boolean endStream = false;
        this.streamId = Http2StateUtil.initiateStream(ctx, connection, http2ClientChannel, outboundMsgHolder);
        http2RequestWriter.setStreamId(streamId);
        HttpRequest httpRequest = Util.createHttpRequest(httpOutboundRequest);

        if (msg instanceof LastHttpContent && msg.content().capacity() == 0) {
            endStream = true;
        }
        // Write Headers
        writeOutboundRequestHeaders(ctx, httpRequest, endStream);
        if (endStream) {
            http2MessageStateContext.setSenderState(new RequestCompleted(http2TargetHandler));
        } else {
            http2MessageStateContext.setSenderState(new SendingEntityBody(http2TargetHandler, http2RequestWriter));
            http2MessageStateContext.getSenderState().writeOutboundRequestBody(ctx, msg);
        }
    }

    private void writeOutboundRequestHeaders(ChannelHandlerContext ctx, HttpMessage httpMsg, boolean endStream)
            throws Http2Exception {
        // Convert and write the headers.
        httpMsg.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), Constants.HTTP_SCHEME);
        Http2Headers http2Headers = HttpConversionUtil.toHttp2Headers(httpMsg, true);
        Http2StateUtil.writeHttp2Headers(ctx, outboundMsgHolder, http2ClientChannel, encoder, streamId,
                httpMsg.headers(), http2Headers, endStream);
    }
}
