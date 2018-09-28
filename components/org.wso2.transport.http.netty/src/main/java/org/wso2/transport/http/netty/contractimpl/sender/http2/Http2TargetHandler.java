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

package org.wso2.transport.http.netty.contractimpl.sender.http2;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.sender.states.http2.RequestCompleted;
import org.wso2.transport.http.netty.contractimpl.sender.states.http2.SendingEntityBody;
import org.wso2.transport.http.netty.contractimpl.sender.states.http2.SendingHeaders;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.Http2Reset;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.Locale;

/**
 * {@code Http2TargetHandler} is responsible for sending and receiving HTTP/2 frames over an outbound connection.
 */
public class Http2TargetHandler extends ChannelDuplexHandler {

    private static final Logger LOG = LoggerFactory.getLogger(Http2TargetHandler.class);

    private Http2Connection connection;
    // Encoder associated with the HTTP2ConnectionHandler
    private Http2ConnectionEncoder encoder;
    private Http2ClientChannel http2ClientChannel;

    public Http2TargetHandler(Http2Connection connection, Http2ConnectionEncoder encoder) {
        this.connection = connection;
        this.encoder = encoder;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof OutboundMsgHolder) {
            OutboundMsgHolder outboundMsgHolder = (OutboundMsgHolder) msg;
            new Http2TargetHandler.Http2RequestWriter(outboundMsgHolder).writeContent(ctx);
        } else if (msg instanceof Http2Reset) {
            Http2Reset resetMsg = (Http2Reset) msg;
            resetStream(ctx, resetMsg.getStreamId(), resetMsg.getError());
        } else {
            ctx.write(msg, promise); // let other types of objects to pass this handler
        }
    }

    /**
     * Terminates a stream.
     *
     * @param streamId   stream to be terminated
     * @param http2Error cause for the termination
     */
    void resetStream(ChannelHandlerContext ctx, int streamId, Http2Error http2Error) {
        encoder.writeRstStream(ctx, streamId, http2Error.code(), ctx.newPromise());
        http2ClientChannel.getDataEventListeners()
                .forEach(dataEventListener -> dataEventListener.onStreamReset(streamId));
        ctx.flush();
    }

    /**
     * Gets the associated {@code Http2Connection}.
     *
     * @return the associated Http2Connection
     */
    public Http2Connection getConnection() {
        return connection;
    }

    /**
     * Sets the {@link Http2ClientChannel}.
     *
     * @param http2ClientChannel the client channel related to the handler
     */
    public void setHttp2ClientChannel(Http2ClientChannel http2ClientChannel) {
        this.http2ClientChannel = http2ClientChannel;
    }

    /**
     * Gets the {@link Http2ClientChannel}.
     *
     * @return the Http2ClientChannel
     */
    public Http2ClientChannel getHttp2ClientChannel() {
        return http2ClientChannel;
    }

    /**
     * Gets the {@link Http2ConnectionEncoder}.
     *
     * @return the Http2ConnectionEncoder
     */
    public Http2ConnectionEncoder getEncoder() {
        return encoder;
    }

    /**
     * {@code Http2RequestWriter} is used to write Http2 content to the connection.
     */
    public class Http2RequestWriter {

        HttpCarbonMessage httpOutboundRequest;
        OutboundMsgHolder outboundMsgHolder;
        Http2MessageStateContext http2MessageStateContext;
        int streamId;

        Http2RequestWriter(OutboundMsgHolder outboundMsgHolder) {
            this.outboundMsgHolder = outboundMsgHolder;
            this.httpOutboundRequest = outboundMsgHolder.getRequest();
            http2MessageStateContext = httpOutboundRequest.getHttp2MessageStateContext();
            if (http2MessageStateContext == null) {
                http2MessageStateContext = new Http2MessageStateContext();
                httpOutboundRequest.setHttp2MessageStateContext(http2MessageStateContext);
            }
        }

        void writeContent(ChannelHandlerContext ctx) {
            httpOutboundRequest.getHttp2MessageStateContext()
                    .setSenderState(new SendingHeaders(Http2TargetHandler.this, this));
            // Write Content
            httpOutboundRequest.getHttpContentAsync().setMessageListener((httpContent ->
                    http2ClientChannel.getChannel().eventLoop().execute(() -> {
                        try {
                            writeOutboundRequest(ctx, httpContent);
                        } catch (Exception ex) {
                            String errorMsg = "Failed to send the request : " +
                                    ex.getMessage().toLowerCase(Locale.ENGLISH);
                            LOG.error(errorMsg, ex);
                            outboundMsgHolder.getResponseFuture().notifyHttpListener(ex);
                        }
                    })));
        }

        private void writeOutboundRequest(ChannelHandlerContext ctx, HttpContent msg) throws Http2Exception {
            try {
                http2MessageStateContext.getSenderState().writeOutboundRequestBody(ctx, msg);
            } catch (RuntimeException ex) {
                httpOutboundRequest.getHttp2MessageStateContext()
                        .setSenderState(new SendingEntityBody(Http2TargetHandler.this, this));
                httpOutboundRequest.getHttp2MessageStateContext()
                        .getSenderState().writeOutboundRequestBody(ctx, new DefaultLastHttpContent());
            }
        }

        public Http2MessageStateContext getHttp2MessageStateContext() {
            return http2MessageStateContext;
        }

        public HttpCarbonMessage getHttpOutboundRequest() {
            return httpOutboundRequest;
        }

        public OutboundMsgHolder getOutboundMsgHolder() {
            return outboundMsgHolder;
        }

        public int getStreamId() {
            return streamId;
        }

        public void setStreamId(int streamId) {
            this.streamId = streamId;
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Http2HeadersFrame) {
            onHeaderRead(ctx, (Http2HeadersFrame) msg);
        } else if (msg instanceof Http2DataFrame) {
            onDataRead(ctx, (Http2DataFrame) msg);
        } else if (msg instanceof Http2PushPromise) {
            onPushPromiseRead((Http2PushPromise) msg);
        } else if (msg instanceof Http2Reset) {
            onResetRead((Http2Reset) msg);
        }
    }

    private void onHeaderRead(ChannelHandlerContext ctx, Http2HeadersFrame http2HeadersFrame) {
        int streamId = http2HeadersFrame.getStreamId();
        OutboundMsgHolder outboundMsgHolder = http2ClientChannel.getInFlightMessage(streamId);
        boolean isServerPush = false;
        if (outboundMsgHolder == null) {
            outboundMsgHolder = http2ClientChannel.getPromisedMessage(streamId);
            if (outboundMsgHolder != null) {
                isServerPush = true;
            } else {
                LOG.warn("Header Frame received on channel: {} with invalid stream id: {} ",
                        http2ClientChannel, streamId);
                return;
            }
        }
        Http2MessageStateContext http2MessageStateContext = initHttp2MessageContext(outboundMsgHolder);
        http2MessageStateContext.getSenderState().readInboundResponseHeaders(ctx, http2HeadersFrame,
                outboundMsgHolder, isServerPush, http2MessageStateContext);
    }

    private void onDataRead(ChannelHandlerContext ctx, Http2DataFrame http2DataFrame) {
        int streamId = http2DataFrame.getStreamId();
        OutboundMsgHolder outboundMsgHolder = http2ClientChannel.getInFlightMessage(streamId);
        boolean isServerPush = false;
        if (outboundMsgHolder == null) {
            outboundMsgHolder = http2ClientChannel.getPromisedMessage(streamId);
            if (outboundMsgHolder != null) {
                isServerPush = true;
            } else {
                LOG.warn("Data Frame received on channel: {} with invalid stream id: {}",
                        http2ClientChannel, streamId);
                return;
            }
        }
        Http2MessageStateContext http2MessageStateContext = getHttp2MessageContext(outboundMsgHolder);
        http2MessageStateContext.getSenderState().readInboundResponseBody(ctx, http2DataFrame,
                outboundMsgHolder, isServerPush, http2MessageStateContext);
    }

    private void onPushPromiseRead(Http2PushPromise http2PushPromise) {
        int streamId = http2PushPromise.getStreamId();
        OutboundMsgHolder outboundMsgHolder = http2ClientChannel.getInFlightMessage(streamId);
        Http2MessageStateContext http2MessageStateContext = initHttp2MessageContext(outboundMsgHolder);
        http2MessageStateContext.getSenderState().readInboundPromise(http2PushPromise, outboundMsgHolder);
    }

    private void onResetRead(Http2Reset http2Reset) {
        int streamId = http2Reset.getStreamId();
        OutboundMsgHolder outboundMsgHolder = http2ClientChannel.getInFlightMessage(streamId);
        if (outboundMsgHolder != null) {
            outboundMsgHolder.getResponseFuture()
                    .notifyHttpListener(new Exception("HTTP/2 stream " + streamId + " reset by the remote peer"));
        }
    }

    private Http2MessageStateContext initHttp2MessageContext(OutboundMsgHolder outboundMsgHolder) {
        Http2MessageStateContext http2MessageStateContext = getHttp2MessageContext(outboundMsgHolder);
        if (http2MessageStateContext == null) {
            http2MessageStateContext = new Http2MessageStateContext();
            http2MessageStateContext.setSenderState(new RequestCompleted(this));
            outboundMsgHolder.getRequest().setHttp2MessageStateContext(http2MessageStateContext);
        }
        return http2MessageStateContext;
    }

    private Http2MessageStateContext getHttp2MessageContext(OutboundMsgHolder outboundMsgHolder) {
        return outboundMsgHolder.getRequest().getHttp2MessageStateContext();
    }
}
