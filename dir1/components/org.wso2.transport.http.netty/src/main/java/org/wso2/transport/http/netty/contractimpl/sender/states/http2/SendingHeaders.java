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
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.exceptions.EndpointTimeOutException;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2ClientChannel;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2TargetHandler;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2TargetHandler.Http2RequestWriter;
import org.wso2.transport.http.netty.contractimpl.sender.http2.OutboundMsgHolder;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.IOException;

import static org.wso2.transport.http.netty.contract.Constants.HTTP_SCHEME;
import static org.wso2.transport.http.netty.contract.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_REQUEST_HEADERS;
import static org.wso2.transport.http.netty.contract.Constants.INBOUND_RESPONSE_ALREADY_RECEIVED;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST_HEADERS;
import static org.wso2.transport.http.netty.contractimpl.common.states.Http2StateUtil.initiateStream;
import static org.wso2.transport.http.netty.contractimpl.common.states.Http2StateUtil.writeHttp2Headers;

/**
 * State between start and end of outbound request header write.
 *
 * @since 6.0.241
 */
public class SendingHeaders implements SenderState {

    private static final Logger LOG = LoggerFactory.getLogger(SendingHeaders.class);

    private final Http2TargetHandler http2TargetHandler;
    private final Http2RequestWriter http2RequestWriter;
    private final Http2MessageStateContext http2MessageStateContext;
    private final HttpCarbonMessage httpOutboundRequest;
    private final OutboundMsgHolder outboundMsgHolder;
    private final Http2Connection connection;
    private final Http2ConnectionEncoder encoder;
    private final Http2ClientChannel http2ClientChannel;
    private int streamId;
    private boolean continueHeader;

    public SendingHeaders(Http2TargetHandler http2TargetHandler, Http2RequestWriter http2RequestWriter) {
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
    public void writeOutboundRequestBody(ChannelHandlerContext ctx, HttpContent httpContent,
                                         Http2MessageStateContext http2MessageStateContext) throws Http2Exception {
        writeOutboundRequestHeaders(ctx, httpContent);
    }

    @Override
    public void readInboundResponseHeaders(ChannelHandlerContext ctx, Http2HeadersFrame http2HeadersFrame,
                                           OutboundMsgHolder outboundMsgHolder, boolean serverPush,
                                           Http2MessageStateContext http2MessageStateContext) throws Http2Exception {
        // This is an action due to an application error. When the initial frames of the response is being received
        // before sending the complete request.
        outboundMsgHolder.getRequest().setIoException(new IOException(INBOUND_RESPONSE_ALREADY_RECEIVED));
        http2MessageStateContext.setSenderState(new ReceivingHeaders(http2TargetHandler, http2RequestWriter));
        http2MessageStateContext.getSenderState().readInboundResponseHeaders(ctx, http2HeadersFrame, outboundMsgHolder,
                serverPush, http2MessageStateContext);
    }

    @Override
    public void readInboundResponseBody(ChannelHandlerContext ctx, Http2DataFrame http2DataFrame,
                                        OutboundMsgHolder outboundMsgHolder, boolean serverPush,
                                        Http2MessageStateContext http2MessageStateContext) {
        LOG.warn("readInboundResponseEntityBody is not a dependant action of this state");
    }

    @Override
    public void readInboundPromise(ChannelHandlerContext ctx, Http2PushPromise http2PushPromise,
                                   OutboundMsgHolder outboundMsgHolder) {
        LOG.warn("readInboundPromise is not a dependant action of this state");
    }

    @Override
    public void handleStreamTimeout(OutboundMsgHolder outboundMsgHolder, boolean serverPush,
            ChannelHandlerContext ctx, int streamId) {
        if (!serverPush) {
            outboundMsgHolder.getResponseFuture().notifyHttpListener(
                    new EndpointTimeOutException(IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_REQUEST_HEADERS,
                            HttpResponseStatus.GATEWAY_TIMEOUT.code()));
        }
    }

    @Override
    public void handleConnectionClose(OutboundMsgHolder outboundMsgHolder) {
        outboundMsgHolder.getResponseFuture().notifyHttpListener(new EndpointTimeOutException(
                REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST_HEADERS,
                HttpResponseStatus.GATEWAY_TIMEOUT.code()));
    }

    private void writeHeaders(ChannelHandlerContext ctx, HttpContent msg) throws Http2Exception {
        // Initiate the stream
        boolean endStream = false;
        this.streamId = initiateStream(ctx, connection, http2ClientChannel, outboundMsgHolder);
        http2RequestWriter.setStreamId(streamId);
        HttpRequest httpRequest = Util.createHttpRequest(httpOutboundRequest);

        if (msg instanceof LastHttpContent && msg.content().capacity() == 0) {
            endStream = true;
        }
        // Write Headers
        writeOutboundRequestHeaders(ctx, httpRequest, endStream);
        if (endStream) {
            http2MessageStateContext.setSenderState(new RequestCompleted(http2TargetHandler, http2RequestWriter));
        } else {
            String expectHeader = httpOutboundRequest.getHeader(HttpHeaderNames.EXPECT.toString());
            if (expectHeader != null) {
                http2MessageStateContext.setSenderState(
                        new WaitingFor100Continue(http2TargetHandler, http2RequestWriter, msg, ctx, streamId,
                                http2MessageStateContext));
            } else {
                http2MessageStateContext.setSenderState(new SendingEntityBody(http2TargetHandler, http2RequestWriter));
                http2MessageStateContext.getSenderState().writeOutboundRequestBody(ctx, msg, http2MessageStateContext);
            }
        }
    }

    private void writeOutboundRequestHeaders(ChannelHandlerContext ctx, HttpMessage httpMsg, boolean endStream)
            throws Http2Exception {
        // Convert and write the headers.
        httpMsg.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), HTTP_SCHEME);
        Http2Headers http2Headers = HttpConversionUtil.toHttp2Headers(httpMsg, true);
        writeHttp2Headers(ctx, outboundMsgHolder, http2ClientChannel, encoder, streamId, httpMsg.headers(),
                http2Headers, endStream);
    }
}
