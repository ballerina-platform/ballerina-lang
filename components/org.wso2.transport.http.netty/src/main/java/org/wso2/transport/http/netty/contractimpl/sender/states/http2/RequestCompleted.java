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

/**
 * State between end of payload write and start of response headers read
 */
public class RequestCompleted implements SenderState {

    private static final Logger LOG = LoggerFactory.getLogger(RequestCompleted.class);

    private final Http2TargetHandler http2TargetHandler;

    RequestCompleted(Http2TargetHandler http2TargetHandler) {
        this.http2TargetHandler = http2TargetHandler;
    }

    @Override
    public void writeOutboundRequestHeaders(ChannelHandlerContext ctx, HttpContent httpContent) {
        LOG.warn("writeOutboundRequestHeaders is not a dependant action of this state");
    }

    @Override
    public void writeOutboundRequestEntity(
            ChannelHandlerContext ctx, HttpContent httpContent) {
        LOG.warn("writeOutboundRequestEntity is not a dependant action of this state");
    }

    @Override
    public void readInboundResponseHeaders(ChannelHandlerContext ctx, Object msg,
                                           OutboundMsgHolder outboundMsgHolder, boolean isServerPush,
                                           Http2MessageStateContext http2MessageStateContext) {
        // When the initial frames of the response is to be received after sending the complete request.
        http2MessageStateContext.setSenderState(new ReceivingHeaders(http2TargetHandler));
        http2MessageStateContext.getSenderState().readInboundResponseHeaders(ctx, msg, outboundMsgHolder,
                isServerPush, http2MessageStateContext);
    }

    @Override
    public void readInboundResponseEntityBody(ChannelHandlerContext ctx, Object msg,
                                              OutboundMsgHolder outboundMsgHolder, boolean isServerPush,
                                              Http2MessageStateContext http2MessageStateContext) {
        LOG.warn("readInboundResponseEntityBody is not a dependant action of this state");
    }
}
