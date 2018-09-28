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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2TargetHandler;
import org.wso2.transport.http.netty.contractimpl.sender.http2.OutboundMsgHolder;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.Http2PushPromise;

/**
 * State of successfully read response
 */
public class EntityBodyReceived implements SenderState {

    private static final Logger LOG = LoggerFactory.getLogger(EntityBodyReceived.class);

    private final Http2TargetHandler http2TargetHandler;

    EntityBodyReceived(Http2TargetHandler http2TargetHandler) {
        this.http2TargetHandler = http2TargetHandler;
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
    public void readInboundResponseHeaders(ChannelHandlerContext ctx, Http2HeadersFrame http2HeadersFrame,
                                           OutboundMsgHolder outboundMsgHolder, boolean isServerPush,
                                           Http2MessageStateContext http2MessageStateContext) {
        // When promised response message is going to be sent after the original response or previous promised responses
        // has been sent.
        http2MessageStateContext.setSenderState(new ReceivingHeaders(http2TargetHandler));
        http2MessageStateContext.getSenderState().readInboundResponseHeaders(ctx, http2HeadersFrame, outboundMsgHolder,
                isServerPush, http2MessageStateContext);
    }

    @Override
    public void readInboundResponseBody(ChannelHandlerContext ctx, Http2DataFrame http2DataFrame,
                                        OutboundMsgHolder outboundMsgHolder, boolean isServerPush,
                                        Http2MessageStateContext http2MessageStateContext) {
        LOG.warn("readInboundResponseBody is not a dependant action of this state");
    }

    @Override
    public void readInboundPromise(Http2PushPromise http2PushPromise, OutboundMsgHolder outboundMsgHolder) {
        LOG.warn("readInboundPromise is not a dependant action of this state");
    }
}
