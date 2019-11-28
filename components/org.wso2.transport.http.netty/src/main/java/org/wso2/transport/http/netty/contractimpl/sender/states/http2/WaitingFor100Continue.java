/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import io.netty.handler.codec.http2.Http2Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2ClientChannel;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2ClientTimeoutHandler;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2DataEventListener;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2TargetHandler;
import org.wso2.transport.http.netty.contractimpl.sender.http2.OutboundMsgHolder;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.Http2PushPromise;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import static org.wso2.transport.http.netty.contract.Constants.REMOTE_SERVER_CLOSED_WHILE_READING_INBOUND_RESPONSE_HEADERS;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.handleIncompleteInboundMessage;

/**
 * A state for handling 100-continue requests.
 */
public class WaitingFor100Continue implements SenderState {
    private static final Logger LOG = LoggerFactory.getLogger(ReceivingHeaders.class);
    private final Http2MessageStateContext http2MessageStateContext;

    private final Http2TargetHandler http2TargetHandler;
    private final Http2ClientChannel http2ClientChannel;
    private final Http2TargetHandler.Http2RequestWriter http2RequestWriter;
    private final HttpContent httpContent;
    private List<HttpContent> cachedContent = new ArrayList<>();

    WaitingFor100Continue(Http2TargetHandler http2TargetHandler,
            Http2TargetHandler.Http2RequestWriter http2RequestWriter, HttpContent httpContent,
            ChannelHandlerContext ctx, int streamId, Http2MessageStateContext http2MessageStateContext) {
        this.http2TargetHandler = http2TargetHandler;
        this.http2RequestWriter = http2RequestWriter;
        this.http2ClientChannel = http2TargetHandler.getHttp2ClientChannel();
        this.httpContent = httpContent;
        this.http2MessageStateContext = http2MessageStateContext;
        configTimeOut(ctx, streamId, true);
    }

    @Override
    public void writeOutboundRequestHeaders(ChannelHandlerContext ctx, HttpContent httpContent)
            throws Http2Exception {
        LOG.warn("writeOutboundRequestHeaders is not a dependant action of this state");
    }

    @Override
    public void writeOutboundRequestBody(ChannelHandlerContext ctx, HttpContent httpContent,
            Http2MessageStateContext http2MessageStateContext) throws Http2Exception {
        cachedContent.add(httpContent);
    }

    @Override
    public void readInboundResponseHeaders(ChannelHandlerContext ctx, Http2HeadersFrame http2HeadersFrame,
            OutboundMsgHolder outboundMsgHolder, boolean serverPush,
            Http2MessageStateContext http2MessageStateContext) throws Http2Exception {
        configTimeOut(ctx, http2HeadersFrame.getStreamId(), false);
        Http2Headers http2Headers = http2HeadersFrame.getHeaders();

        if (HttpResponseStatus.CONTINUE.codeAsText().contentEquals(http2Headers.status())) {
            http2MessageStateContext.setSenderState(new SendingEntityBody(http2TargetHandler, http2RequestWriter));
            http2MessageStateContext.getSenderState()
                    .writeOutboundRequestBody(ctx, httpContent, http2MessageStateContext);
            for (HttpContent cachedHttpContent : cachedContent) {
                http2MessageStateContext.getSenderState()
                        .writeOutboundRequestBody(ctx, cachedHttpContent, http2MessageStateContext);
            }
        } else {
            for (HttpContent cachedHttpContent : cachedContent) {
                cachedHttpContent.release();
            }
            http2MessageStateContext.setSenderState(new ReceivingHeaders(http2TargetHandler, http2RequestWriter));
            http2MessageStateContext.getSenderState()
                    .readInboundResponseHeaders(ctx, http2HeadersFrame, outboundMsgHolder, serverPush,
                            http2MessageStateContext);
        }
    }

    private void configTimeOut(ChannelHandlerContext ctx, int streamId, boolean expectContinue) {
        List<Http2DataEventListener> eventListeners = http2ClientChannel.getDataEventListeners();
        Http2ClientTimeoutHandler timeoutHandler = (Http2ClientTimeoutHandler) eventListeners.get(0);
        ScheduledFuture<?> timerTask = timeoutHandler.getTimerTasks().get(streamId);
        if (timerTask != null) {
            timerTask.cancel(true);
        }
        if (expectContinue) {
            timeoutHandler.createTimerTask(ctx, streamId, http2ClientChannel.getSocketIdleTimeout() / 5, true);
        } else {
            timeoutHandler.createTimerTask(ctx, streamId, http2ClientChannel.getSocketIdleTimeout(), false);
        }
    }

    @Override
    public void readInboundResponseBody(ChannelHandlerContext ctx, Http2DataFrame http2DataFrame,
            OutboundMsgHolder outboundMsgHolder, boolean serverPush,
            Http2MessageStateContext http2MessageStateContext) {
        http2MessageStateContext.setSenderState(new ReceivingEntityBody(http2TargetHandler, http2RequestWriter));
        http2MessageStateContext.getSenderState()
                .readInboundResponseBody(ctx, http2DataFrame, outboundMsgHolder, serverPush, http2MessageStateContext);
    }

    @Override
    public void readInboundPromise(ChannelHandlerContext ctx, Http2PushPromise http2PushPromise,
            OutboundMsgHolder outboundMsgHolder) {
        LOG.warn("readInboundPromise is not a dependant action of this state");
    }

    @Override
    public void handleStreamTimeout(OutboundMsgHolder outboundMsgHolder, boolean serverPush,
            ChannelHandlerContext context, int streamId) throws Http2Exception {
        configTimeOut(context, streamId, false);
        http2MessageStateContext.setSenderState(new SendingEntityBody(http2TargetHandler, http2RequestWriter));
        http2MessageStateContext.getSenderState()
                .writeOutboundRequestBody(context, httpContent, http2MessageStateContext);
        for (HttpContent cachedHttpContent : cachedContent) {
            http2MessageStateContext.getSenderState()
                    .writeOutboundRequestBody(context, cachedHttpContent, http2MessageStateContext);
        }
    }

    @Override
    public void handleConnectionClose(OutboundMsgHolder outboundMsgHolder) {
        handleIncompleteInboundMessage(outboundMsgHolder.getResponse(),
                REMOTE_SERVER_CLOSED_WHILE_READING_INBOUND_RESPONSE_HEADERS);
    }
}
