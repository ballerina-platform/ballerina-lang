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
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http2.Http2Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2TargetHandler;
import org.wso2.transport.http.netty.contractimpl.sender.http2.OutboundMsgHolder;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.Http2PushPromise;

import java.io.IOException;

import static org.wso2.transport.http.netty.contract.Constants.INBOUND_RESPONSE_ALREADY_RECEIVED;
import static org.wso2.transport.http.netty.contractimpl.common.states.Http2StateUtil.releaseContent;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.ILLEGAL_STATE_ERROR;

/**
 * State of successfully read response.
 *
 * @since 6.0.241
 */
public class EntityBodyReceived implements SenderState {

    private static final Logger LOG = LoggerFactory.getLogger(EntityBodyReceived.class);

    private final Http2TargetHandler http2TargetHandler;
    private Http2TargetHandler.Http2RequestWriter http2RequestWriter;
    private OutboundMsgHolder outboundMsgHolder;

    EntityBodyReceived(Http2TargetHandler http2TargetHandler,
                       Http2TargetHandler.Http2RequestWriter http2RequestWriter) {
        this.http2TargetHandler = http2TargetHandler;
        if (http2RequestWriter != null) {
            this.http2RequestWriter = http2RequestWriter;
            this.outboundMsgHolder = http2RequestWriter.getOutboundMsgHolder();
        }
    }

    @Override
    public void writeOutboundRequestHeaders(ChannelHandlerContext ctx, HttpContent httpContent) {
        LOG.warn("writeOutboundRequestHeaders is not a dependant action of this state");
    }

    @Override
    public void writeOutboundRequestBody(ChannelHandlerContext ctx, HttpContent httpContent,
                                         Http2MessageStateContext http2MessageStateContext) throws Http2Exception {
        // Response is already received, hence the outgoing data frames need to be released and send last http
        // content frame to the server.
        // http2RequestWriter can be null in http upgrade path. If http2RequestWriter is null, we don't need to send
        // the content to server.
        if (http2RequestWriter != null && outboundMsgHolder != null) {
            outboundMsgHolder.getRequest().setIoException(new IOException(INBOUND_RESPONSE_ALREADY_RECEIVED));
            http2MessageStateContext.setSenderState(new SendingEntityBody(http2TargetHandler, http2RequestWriter));
            http2MessageStateContext.getSenderState().writeOutboundRequestBody(ctx, new DefaultLastHttpContent(),
                    http2MessageStateContext);
            // Move back to EntityBodyReceived state to ensure proper state transition.
            http2MessageStateContext.setSenderState(new EntityBodyReceived(http2TargetHandler, http2RequestWriter));
        }
        releaseContent(httpContent);
    }

    @Override
    public void readInboundResponseHeaders(ChannelHandlerContext ctx, Http2HeadersFrame http2HeadersFrame,
                                           OutboundMsgHolder outboundMsgHolder, boolean serverPush,
                                           Http2MessageStateContext http2MessageStateContext) throws Http2Exception {
        // When promised response message is going to be received after the original response or previous promised
        // responses has been received.
        http2MessageStateContext.setSenderState(new ReceivingHeaders(http2TargetHandler, http2RequestWriter));
        http2MessageStateContext.getSenderState().readInboundResponseHeaders(ctx, http2HeadersFrame, outboundMsgHolder,
                serverPush, http2MessageStateContext);
    }

    @Override
    public void readInboundResponseBody(ChannelHandlerContext ctx, Http2DataFrame http2DataFrame,
                                        OutboundMsgHolder outboundMsgHolder, boolean serverPush,
                                        Http2MessageStateContext http2MessageStateContext) {
        LOG.warn("readInboundResponseBody is not a dependant action of this state");
    }

    @Override
    public void readInboundPromise(ChannelHandlerContext ctx, Http2PushPromise http2PushPromise,
                                   OutboundMsgHolder outboundMsgHolder) {
        LOG.warn("readInboundPromise is not a dependant action of this state");
    }

    @Override
    public void handleStreamTimeout(OutboundMsgHolder outboundMsgHolder, boolean serverPush,
            ChannelHandlerContext ctx, int streamId) {
        LOG.warn("handleStreamTimeout {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void handleConnectionClose(OutboundMsgHolder outboundMsgHolder) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Channel is closed");
        }
    }
}
