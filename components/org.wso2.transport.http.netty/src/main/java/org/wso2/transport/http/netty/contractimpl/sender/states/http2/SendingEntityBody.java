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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.exceptions.EndpointTimeOutException;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2ClientChannel;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2DataEventListener;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2TargetHandler;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2TargetHandler.Http2RequestWriter;
import org.wso2.transport.http.netty.contractimpl.sender.http2.OutboundMsgHolder;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.Http2PushPromise;

import java.io.IOException;

import static org.wso2.transport.http.netty.contract.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_REQUEST_BODY;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST_BODY;
import static org.wso2.transport.http.netty.contractimpl.common.states.Http2StateUtil.onPushPromiseRead;

/**
 * State between start and end of outbound request entity body write.
 *
 * @since 6.0.241
 */
public class SendingEntityBody implements SenderState {

    private static final Logger LOG = LoggerFactory.getLogger(SendingEntityBody.class);

    private final Http2TargetHandler http2TargetHandler;
    private final Http2MessageStateContext http2MessageStateContext;
    private final OutboundMsgHolder outboundMsgHolder;
    private final Http2ConnectionEncoder encoder;
    private final Http2ClientChannel http2ClientChannel;
    private final int streamId;
    private final Http2RequestWriter http2RequestWriter;

    public SendingEntityBody(Http2TargetHandler http2TargetHandler, Http2RequestWriter http2RequestWriter) {
        this.http2TargetHandler = http2TargetHandler;
        this.http2MessageStateContext = http2RequestWriter.getHttp2MessageStateContext();
        this.outboundMsgHolder = http2RequestWriter.getOutboundMsgHolder();
        this.encoder = http2TargetHandler.getEncoder();
        this.http2ClientChannel = http2TargetHandler.getHttp2ClientChannel();
        this.streamId = http2RequestWriter.getStreamId();
        this.http2RequestWriter = http2RequestWriter;
    }

    @Override
    public void writeOutboundRequestHeaders(ChannelHandlerContext ctx, HttpContent httpContent) {
        LOG.warn("writeOutboundRequestHeaders is not a dependant action of this state");
    }

    @Override
    public void writeOutboundRequestBody(ChannelHandlerContext ctx, HttpContent httpContent,
                                         Http2MessageStateContext http2MessageStateContext) throws Http2Exception {
        writeContent(ctx, httpContent);
    }

    @Override
    public void readInboundResponseHeaders(ChannelHandlerContext ctx, Http2HeadersFrame http2HeadersFrame,
                                           OutboundMsgHolder outboundMsgHolder, boolean serverPush,
                                           Http2MessageStateContext http2MessageStateContext) throws Http2Exception {
        // In bidirectional streaming case, while sending the request data frames, server response data frames can
        // receive. In order to handle it. we need to change the states depending on the action.
        http2MessageStateContext.setSenderState(new ReceivingHeaders(http2TargetHandler, http2RequestWriter));
        http2MessageStateContext.getSenderState().readInboundResponseHeaders(ctx, http2HeadersFrame, outboundMsgHolder,
                serverPush, http2MessageStateContext);
    }

    @Override
    public void readInboundResponseBody(ChannelHandlerContext ctx, Http2DataFrame http2DataFrame,
                                        OutboundMsgHolder outboundMsgHolder, boolean serverPush,
                                        Http2MessageStateContext http2MessageStateContext) {
        // In bidirectional streaming case, while sending the request data frames, server response data frames can
        // receive. In order to handle it. we need to change the states depending on the action.
        http2MessageStateContext.setSenderState(new ReceivingEntityBody(http2TargetHandler, http2RequestWriter));
        http2MessageStateContext.getSenderState().readInboundResponseBody(ctx, http2DataFrame, outboundMsgHolder,
                serverPush, http2MessageStateContext);
    }

    @Override
    public void readInboundPromise(ChannelHandlerContext ctx, Http2PushPromise http2PushPromise,
                                   OutboundMsgHolder outboundMsgHolder) {
        onPushPromiseRead(ctx, http2PushPromise, http2ClientChannel, outboundMsgHolder);
    }

    @Override
    public void handleStreamTimeout(OutboundMsgHolder outboundMsgHolder, boolean serverPush,
            ChannelHandlerContext ctx, int streamId) {
        if (!serverPush) {
            outboundMsgHolder.getResponseFuture().notifyHttpListener(new EndpointTimeOutException(
                    IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_REQUEST_BODY,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR.code()));
        }
    }

    @Override
    public void handleConnectionClose(OutboundMsgHolder outboundMsgHolder) {
        outboundMsgHolder.getRequest().setIoException(
                new IOException(REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST_BODY));
        outboundMsgHolder.getResponseFuture().notifyHttpListener(new EndpointTimeOutException(
                REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST_BODY,
                HttpResponseStatus.GATEWAY_TIMEOUT.code()));
    }

    private void writeContent(ChannelHandlerContext ctx, HttpContent msg) throws Http2Exception {
        boolean release = true;
        try {
            boolean endStream = msg instanceof LastHttpContent;
            // Write the data
            final ByteBuf content = msg.content();
            release = false;
            for (Http2DataEventListener dataEventListener : http2ClientChannel.getDataEventListeners()) {
                if (!dataEventListener.onDataWrite(ctx, streamId, content, endStream)) {
                    return;
                }
            }
            encoder.writeData(ctx, streamId, content, 0, endStream, ctx.newPromise());
            encoder.flowController().writePendingBytes();
            ctx.flush();
            if (endStream) {
                outboundMsgHolder.setRequestWritten(true);
                http2MessageStateContext.setSenderState(new RequestCompleted(http2TargetHandler, http2RequestWriter));
            }
        } finally {
            if (release) {
                ReferenceCountUtil.release(msg);
            }
        }
    }
}
