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
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http2.Http2Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2ClientChannel;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2TargetHandler;
import org.wso2.transport.http.netty.contractimpl.sender.http2.OutboundMsgHolder;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static org.wso2.transport.http.netty.contract.Constants.REMOTE_SERVER_CLOSED_WHILE_READING_INBOUND_RESPONSE_BODY;
import static org.wso2.transport.http.netty.contractimpl.common.states.Http2StateUtil.releaseContent;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.handleIncompleteInboundMessage;

/**
 * State between start and end of inbound response entity body read.
 *
 * @since 6.0.241
 */
public class ReceivingEntityBody implements SenderState {

    private static final Logger LOG = LoggerFactory.getLogger(ReceivingEntityBody.class);

    private final Http2TargetHandler http2TargetHandler;
    private final Http2ClientChannel http2ClientChannel;
    private final Http2TargetHandler.Http2RequestWriter http2RequestWriter;

    ReceivingEntityBody(Http2TargetHandler http2TargetHandler,
                        Http2TargetHandler.Http2RequestWriter http2RequestWriter) {
        this.http2TargetHandler = http2TargetHandler;
        this.http2RequestWriter = http2RequestWriter;
        this.http2ClientChannel = http2TargetHandler.getHttp2ClientChannel();
    }

    @Override
    public void writeOutboundRequestHeaders(ChannelHandlerContext ctx, HttpContent httpContent) {
        LOG.warn("writeOutboundRequestHeaders is not a dependant action of this state");
    }

    @Override
    public void writeOutboundRequestBody(ChannelHandlerContext ctx, HttpContent httpContent,
                                         Http2MessageStateContext http2MessageStateContext) throws Http2Exception {
        // In bidirectional streaming case, while sending the request data frames, server response data frames can
        // receive. In order to handle it. we need to change the states depending on the action.
        // This is temporary check. Remove the conditional check after reviewing message flow.
        if (http2RequestWriter != null) {
            http2MessageStateContext.setSenderState(new SendingEntityBody(http2TargetHandler, http2RequestWriter));
            http2MessageStateContext.getSenderState().writeOutboundRequestBody(ctx, httpContent,
                    http2MessageStateContext);
        } else {
            // Response is already receiving, if request writer does not exist the outgoing data frames need to be
            // released.
            releaseContent(httpContent);
        }
    }

    @Override
    public void readInboundResponseHeaders(ChannelHandlerContext ctx, Http2HeadersFrame http2HeadersFrame,
                                           OutboundMsgHolder outboundMsgHolder, boolean serverPush,
                                           Http2MessageStateContext http2MessageStateContext) throws Http2Exception {
        // When trailer headers are going to be received after receiving entity body of the response.
        http2MessageStateContext.setSenderState(new ReceivingHeaders(http2TargetHandler, http2RequestWriter));
        http2MessageStateContext.getSenderState().readInboundResponseHeaders(ctx, http2HeadersFrame, outboundMsgHolder,
                serverPush, http2MessageStateContext);
    }

    @Override
    public void readInboundResponseBody(ChannelHandlerContext ctx, Http2DataFrame http2DataFrame,
                                        OutboundMsgHolder outboundMsgHolder, boolean serverPush,
                                        Http2MessageStateContext http2MessageStateContext) {
        onDataRead(http2DataFrame, outboundMsgHolder, serverPush, http2MessageStateContext);
    }

    @Override
    public void readInboundPromise(ChannelHandlerContext ctx, Http2PushPromise http2PushPromise,
                                   OutboundMsgHolder outboundMsgHolder) {
        LOG.warn("readInboundPromise is not a dependant action of this state");
    }

    @Override
    public void handleStreamTimeout(OutboundMsgHolder outboundMsgHolder, boolean serverPush,
            ChannelHandlerContext ctx, int streamId) {
        //This is handled by {@link Http2ClientTimeoutHandler#handleIncompleteResponse(OutboundMsgHolder, boolean)}
        // method.
    }

    @Override
    public void handleConnectionClose(OutboundMsgHolder outboundMsgHolder) {
        handleIncompleteInboundMessage(outboundMsgHolder.getResponse(),
                                       REMOTE_SERVER_CLOSED_WHILE_READING_INBOUND_RESPONSE_BODY);
    }

    private void onDataRead(Http2DataFrame http2DataFrame, OutboundMsgHolder outboundMsgHolder, boolean serverPush,
                            Http2MessageStateContext http2MessageStateContext) {
        int streamId = http2DataFrame.getStreamId();
        ByteBuf data = http2DataFrame.getData();
        boolean endOfStream = http2DataFrame.isEndOfStream();

        if (serverPush) {
            onServerPushDataRead(outboundMsgHolder, streamId, endOfStream, data);
        } else {
            onResponseDataRead(outboundMsgHolder, streamId, endOfStream, data);
        }
        if (endOfStream) {
            http2MessageStateContext.setSenderState(new EntityBodyReceived(http2TargetHandler, http2RequestWriter));
        }
    }

    private void onServerPushDataRead(OutboundMsgHolder outboundMsgHolder, int streamId, boolean endOfStream,
                                      ByteBuf data) {
        HttpCarbonMessage responseMessage = outboundMsgHolder.getPushResponse(streamId);
        if (endOfStream) {
            responseMessage.addHttpContent(new DefaultLastHttpContent(data.retain()));
            http2ClientChannel.removePromisedMessage(streamId);
            responseMessage.setLastHttpContentArrived();
        } else {
            responseMessage.addHttpContent(new DefaultHttpContent(data.retain()));
        }
    }

    private void onResponseDataRead(OutboundMsgHolder outboundMsgHolder, int streamId, boolean endOfStream,
                                    ByteBuf data) {
        HttpCarbonMessage responseMessage = outboundMsgHolder.getResponse();
        if (endOfStream) {
            responseMessage.addHttpContent(new DefaultLastHttpContent(data.retain()));
            http2ClientChannel.removeInFlightMessage(streamId);
            responseMessage.setLastHttpContentArrived();
        } else {
            responseMessage.addHttpContent(new DefaultHttpContent(data.retain()));
        }
    }
}
