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
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2NoMoreStreamIdsException;
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
        if (msg instanceof Http2Content) {
            Http2Content http2Content = (Http2Content) msg;
            try {
                new Http2TargetHandler.Http2RequestWriter(http2Content.getOutboundMsgHolder()).
                    writeContent(ctx, http2Content.getHttpContent());
            } catch (Http2NoMoreStreamIdsException ex) {
                //Remove connection from the pool
                http2ClientChannel.removeFromConnectionPool();
                LOG.warn("Channel is removed from the connection pool : {}", ex.getMessage(), ex);
                http2Content.getOutboundMsgHolder().getResponseFuture().notifyHttpListener(ex);
            } catch (Http2Exception ex) {
                LOG.error("Failed to send the request : {}", ex.getMessage(), ex);
                http2Content.getOutboundMsgHolder().getResponseFuture().notifyHttpListener(ex);
            }
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

        void writeContent(ChannelHandlerContext ctx, HttpContent msg) throws Http2Exception {
            try {
                if (!outboundMsgHolder.isFirstContentWritten()) {
                    httpOutboundRequest.getHttp2MessageStateContext()
                        .setSenderState(new SendingHeaders(Http2TargetHandler.this, this));
                    outboundMsgHolder.setFirstContentWritten(true);
                }
                http2MessageStateContext.getSenderState().writeOutboundRequestBody(ctx, msg, http2MessageStateContext);
            } catch (RuntimeException ex) {
                httpOutboundRequest.getHttp2MessageStateContext()
                    .setSenderState(new SendingEntityBody(Http2TargetHandler.this, this));
                httpOutboundRequest.getHttp2MessageStateContext()
                    .getSenderState().writeOutboundRequestBody(ctx, new DefaultLastHttpContent(),
                                                               http2MessageStateContext);
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
            onPushPromiseRead(ctx, (Http2PushPromise) msg);
        } else if (msg instanceof Http2Reset) {
            onResetRead((Http2Reset) msg);
        }
    }

    private void onHeaderRead(ChannelHandlerContext ctx, Http2HeadersFrame http2HeadersFrame) {
        int streamId = http2HeadersFrame.getStreamId();
        OutboundMsgHolder outboundMsgHolder = http2ClientChannel.getInFlightMessage(streamId);
        boolean serverPush = false;
        if (outboundMsgHolder == null) {
            outboundMsgHolder = http2ClientChannel.getPromisedMessage(streamId);
            if (outboundMsgHolder != null) {
                serverPush = true;
            } else {
                LOG.warn("Header Frame received on channel: {} with invalid stream id: {} ",
                         http2ClientChannel, streamId);
                return;
            }
        } else if (isUnexpected100ContinueResponse(http2HeadersFrame.getHeaders(), outboundMsgHolder.getRequest())) {
            LOG.warn("Received an unexpected 100-continue response");
            return;
        }
        Http2MessageStateContext http2MessageStateContext = initHttp2MessageContext(outboundMsgHolder);
        try {
            http2MessageStateContext.getSenderState()
                    .readInboundResponseHeaders(ctx, http2HeadersFrame, outboundMsgHolder, serverPush,
                            http2MessageStateContext);
        } catch (Http2Exception e) {
            String errorMsg = "Failed to read the inbound headers from the response : " + e.getMessage()
                    .toLowerCase(Locale.ENGLISH);
            LOG.error(errorMsg, e);
            outboundMsgHolder.getResponseFuture().notifyHttpListener(e);
        }
    }

    private void onDataRead(ChannelHandlerContext ctx, Http2DataFrame http2DataFrame) {
        int streamId = http2DataFrame.getStreamId();
        OutboundMsgHolder outboundMsgHolder = http2ClientChannel.getInFlightMessage(streamId);
        boolean serverPush = false;
        if (outboundMsgHolder == null) {
            outboundMsgHolder = http2ClientChannel.getPromisedMessage(streamId);
            if (outboundMsgHolder != null) {
                serverPush = true;
            } else {
                LOG.warn("Data Frame received on channel: {} with invalid stream id: {}",
                        http2ClientChannel, streamId);
                return;
            }
        }
        Http2MessageStateContext http2MessageStateContext = getHttp2MessageContext(outboundMsgHolder);
        http2MessageStateContext.getSenderState().readInboundResponseBody(ctx, http2DataFrame,
                outboundMsgHolder, serverPush, http2MessageStateContext);
    }

    private void onPushPromiseRead(ChannelHandlerContext ctx, Http2PushPromise http2PushPromise) {
        int streamId = http2PushPromise.getStreamId();
        OutboundMsgHolder outboundMsgHolder = http2ClientChannel.getInFlightMessage(streamId);
        Http2MessageStateContext http2MessageStateContext = initHttp2MessageContext(outboundMsgHolder);
        http2MessageStateContext.getSenderState().readInboundPromise(ctx, http2PushPromise, outboundMsgHolder);
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
            http2MessageStateContext.setSenderState(new RequestCompleted(this, null));
            outboundMsgHolder.getRequest().setHttp2MessageStateContext(http2MessageStateContext);
        } else if (http2MessageStateContext.getSenderState() == null) {
            http2MessageStateContext.setSenderState(new RequestCompleted(this, null));
        }
        return http2MessageStateContext;
    }

    private Http2MessageStateContext getHttp2MessageContext(OutboundMsgHolder outboundMsgHolder) {
        return outboundMsgHolder.getRequest().getHttp2MessageStateContext();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Channel is inactive");
        }
        http2ClientChannel.destroy();
    }

    private boolean isUnexpected100ContinueResponse(Http2Headers http2Headers, HttpCarbonMessage inboundReq) {
        return HttpResponseStatus.CONTINUE.codeAsText().contentEquals(http2Headers.status()) &&
                !inboundReq.is100ContinueExpected();
    }
}
