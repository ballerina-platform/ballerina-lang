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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2ClientChannel;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2TargetHandler;
import org.wso2.transport.http.netty.contractimpl.sender.http2.OutboundMsgHolder;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * State between start and end of inbound response entity body read.
 */
public class ReceivingEntityBody implements SenderState {

    private static final Logger LOG = LoggerFactory.getLogger(ReceivingEntityBody.class);

    private final Http2TargetHandler http2TargetHandler;
    private final Http2ClientChannel http2ClientChannel;

    ReceivingEntityBody(Http2TargetHandler http2TargetHandler) {
        this.http2TargetHandler = http2TargetHandler;
        this.http2ClientChannel = http2TargetHandler.getHttp2ClientChannel();
    }

    @Override
    public void writeOutboundRequestHeaders(ChannelHandlerContext ctx, HttpContent httpContent) {
        LOG.warn("writeOutboundRequestHeaders is not a dependant action of this state");
    }

    @Override
    public void writeOutboundRequestBody(ChannelHandlerContext ctx, HttpContent httpContent) {
        LOG.warn("writeOutboundRequestBody is not a dependant action of this state");
    }

    @Override
    public void readInboundResponseHeaders(ChannelHandlerContext ctx, Object msg, OutboundMsgHolder outboundMsgHolder,
                                           boolean isServerPush, Http2MessageStateContext http2MessageStateContext) {
        LOG.warn("readInboundResponseHeaders is not a dependant action of this state");
    }

    @Override
    public void readInboundResponseBody(ChannelHandlerContext ctx, Object msg, OutboundMsgHolder outboundMsgHolder,
                                        boolean isServerPush, Http2MessageStateContext http2MessageStateContext) {
        onDataRead((Http2DataFrame) msg, outboundMsgHolder, isServerPush, http2MessageStateContext);
    }

    private void onDataRead(Http2DataFrame http2DataFrame, OutboundMsgHolder outboundMsgHolder, boolean isServerPush,
                            Http2MessageStateContext http2MessageStateContext) {
        int streamId = http2DataFrame.getStreamId();
        ByteBuf data = http2DataFrame.getData();
        boolean endOfStream = http2DataFrame.isEndOfStream();

        if (isServerPush) {
            HttpCarbonMessage responseMessage = outboundMsgHolder.getPushResponse(streamId);
            if (endOfStream) {
                responseMessage.addHttpContent(new DefaultLastHttpContent(data.retain()));
                http2ClientChannel.removePromisedMessage(streamId);
            } else {
                responseMessage.addHttpContent(new DefaultHttpContent(data.retain()));
            }
        } else {
            HttpCarbonMessage responseMessage = outboundMsgHolder.getResponse();
            if (endOfStream) {
                responseMessage.addHttpContent(new DefaultLastHttpContent(data.retain()));
                http2ClientChannel.removeInFlightMessage(streamId);
            } else {
                responseMessage.addHttpContent(new DefaultHttpContent(data.retain()));
            }
        }
        if (endOfStream) {
            http2MessageStateContext.setSenderState(new EntityBodyReceived(http2TargetHandler));
        }
    }
}
