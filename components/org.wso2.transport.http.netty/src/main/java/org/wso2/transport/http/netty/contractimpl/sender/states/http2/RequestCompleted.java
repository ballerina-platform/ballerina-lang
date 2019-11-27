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
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.Http2Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.exceptions.EndpointTimeOutException;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2ClientChannel;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2TargetHandler;
import org.wso2.transport.http.netty.contractimpl.sender.http2.OutboundMsgHolder;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.Http2PushPromise;

import static org.wso2.transport.http.netty.contract.Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_RESPONSE;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_SERVER_CLOSED_BEFORE_INITIATING_INBOUND_RESPONSE;
import static org.wso2.transport.http.netty.contractimpl.common.states.Http2StateUtil.onPushPromiseRead;

/**
 * State between end of payload write and start of response headers read.
 *
 * @since 6.0.241
 */
public class RequestCompleted implements SenderState {

    private static final Logger LOG = LoggerFactory.getLogger(RequestCompleted.class);

    private final Http2TargetHandler http2TargetHandler;
    private final Http2ClientChannel http2ClientChannel;
    private final Http2TargetHandler.Http2RequestWriter http2RequestWriter;

    public RequestCompleted(Http2TargetHandler http2TargetHandler,
                            Http2TargetHandler.Http2RequestWriter http2RequestWriter) {
        this.http2RequestWriter = http2RequestWriter;
        this.http2TargetHandler = http2TargetHandler;
        this.http2ClientChannel = http2TargetHandler.getHttp2ClientChannel();
    }

    @Override
    public void writeOutboundRequestHeaders(ChannelHandlerContext ctx, HttpContent httpContent) {
        LOG.warn("writeOutboundRequestHeaders is not a dependant action of this state");
    }

    @Override
    public void writeOutboundRequestBody(ChannelHandlerContext ctx, HttpContent httpContent,
                                         Http2MessageStateContext http2MessageStateContext) {
        LOG.warn("writeOutboundRequestBody is not a dependant action of this state");
    }

    @Override
    public void readInboundResponseHeaders(ChannelHandlerContext ctx, Http2HeadersFrame http2HeadersFrame,
                                           OutboundMsgHolder outboundMsgHolder, boolean serverPush,
                                           Http2MessageStateContext http2MessageStateContext) throws Http2Exception {
        // When the initial frames of the response is to be received after sending the complete request.
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
        onPushPromiseRead(ctx, http2PushPromise, http2ClientChannel, outboundMsgHolder);
    }

    @Override
    public void handleStreamTimeout(OutboundMsgHolder outboundMsgHolder, boolean serverPush,
            ChannelHandlerContext ctx, int streamId) {
        if (!serverPush) {
            outboundMsgHolder.getResponseFuture().notifyHttpListener(new EndpointTimeOutException(
                    IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_RESPONSE,
                    HttpResponseStatus.GATEWAY_TIMEOUT.code()));
        }
    }

    @Override
    public void handleConnectionClose(OutboundMsgHolder outboundMsgHolder) {
        outboundMsgHolder.getResponseFuture().notifyHttpListener(
                new ServerConnectorException(REMOTE_SERVER_CLOSED_BEFORE_INITIATING_INBOUND_RESPONSE));
        LOG.error(REMOTE_SERVER_CLOSED_BEFORE_INITIATING_INBOUND_RESPONSE);
    }
}
