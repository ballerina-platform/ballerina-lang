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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.EmptyHttp2Headers;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.message.DefaultListener;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.Http2Reset;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;
import org.wso2.transport.http.netty.message.PooledDataStreamerFactory;

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
     * {@code Http2RequestWriter} is used to write Http2 content to the connection.
     */
    private class Http2RequestWriter {

        // whether headers are written already
        boolean isHeadersWritten = false;
        HttpCarbonMessage httpOutboundRequest;
        OutboundMsgHolder outboundMsgHolder;
        int streamId;

        Http2RequestWriter(OutboundMsgHolder outboundMsgHolder) {
            this.outboundMsgHolder = outboundMsgHolder;
            httpOutboundRequest = outboundMsgHolder.getRequest();
        }

        void writeContent(ChannelHandlerContext ctx) {
            // Write Content
            httpOutboundRequest.getHttpContentAsync().
                    setMessageListener((httpContent ->
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

            boolean endStream = false;
            if (!isHeadersWritten) {
                // Initiate the stream
                streamId = initiateStream(ctx);
                HttpRequest httpRequest = Util.createHttpRequest(httpOutboundRequest);

                if (msg instanceof LastHttpContent && msg.content().capacity() == 0) {
                    endStream = true;
                }
                // Write Headers
                writeOutboundRequestHeaders(ctx, httpRequest, streamId, endStream);
                isHeadersWritten = true;
                if (endStream) {
                    return;
                }
            }

            boolean release = true;
            try {
                boolean isLastContent = false;
                HttpHeaders trailers = EmptyHttpHeaders.INSTANCE;
                Http2Headers http2Trailers = EmptyHttp2Headers.INSTANCE;
                if (msg instanceof LastHttpContent) {
                    isLastContent = true;
                    // Convert any trailing headers.
                    final LastHttpContent lastContent = (LastHttpContent) msg;
                    trailers = lastContent.trailingHeaders();
                    http2Trailers = HttpConversionUtil.toHttp2Headers(trailers, true);
                }

                // Write the data
                final ByteBuf content = msg.content();
                endStream = isLastContent && trailers.isEmpty();
                release = false;
                for (Http2DataEventListener dataEventListener : http2ClientChannel.getDataEventListeners()) {
                    if (!dataEventListener.onDataWrite(ctx, streamId, content, endStream)) {
                        return;
                    }
                }
                encoder.writeData(ctx, streamId, content, 0, endStream, ctx.newPromise());
                encoder.flowController().writePendingBytes();
                ctx.flush();
                if (!trailers.isEmpty()) {
                    // Write trailing headers.
                    writeHttp2Headers(ctx, streamId, trailers, http2Trailers, true);
                }
                if (endStream) {
                    outboundMsgHolder.setRequestWritten(true);
                }
            } catch (Exception ex) {
                LOG.error("Error while writing request", ex);
            } finally {
                if (release) {
                    ReferenceCountUtil.release(msg);
                }
            }
        }

        private int initiateStream(ChannelHandlerContext ctx) throws Http2Exception {
            int id = getNextStreamId();
            http2ClientChannel.putInFlightMessage(id, outboundMsgHolder);
            http2ClientChannel.getDataEventListeners().
                    forEach(dataEventListener -> dataEventListener.onStreamInit(ctx, id));
            return id;
        }

        /**
         * Gets the next available stream id in the connection.
         *
         * @return next available stream id
         */
        private synchronized int getNextStreamId() throws Http2Exception {
            int nextStreamId = connection.local().incrementAndGetNextStreamId();
            connection.local().createStream(nextStreamId, false);
            LOG.debug("Stream created streamId: {}", nextStreamId);
            return nextStreamId;
        }

        private void writeOutboundRequestHeaders(ChannelHandlerContext ctx, HttpMessage httpMsg, int streamId,
                                                 boolean endStream) throws Http2Exception {
            // Convert and write the headers.
            httpMsg.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), Constants.HTTP_SCHEME);
            Http2Headers http2Headers = HttpConversionUtil.toHttp2Headers(httpMsg, true);
            writeHttp2Headers(ctx, streamId, httpMsg.headers(), http2Headers, endStream);
        }

        private void writeHttp2Headers(ChannelHandlerContext ctx, int streamId, HttpHeaders headers,
                                       Http2Headers http2Headers, boolean endStream) throws Http2Exception {
            int dependencyId = headers.getInt(
                    HttpConversionUtil.ExtensionHeaderNames.STREAM_DEPENDENCY_ID.text(), 0);
            short weight = headers.getShort(HttpConversionUtil.ExtensionHeaderNames.STREAM_WEIGHT.text(),
                    Http2CodecUtil.DEFAULT_PRIORITY_WEIGHT);
            for (Http2DataEventListener dataEventListener : http2ClientChannel.getDataEventListeners()) {
                if (!dataEventListener.onHeadersWrite(ctx, streamId, http2Headers, endStream)) {
                    return;
                }
            }
            encoder.writeHeaders(
                    ctx, streamId, http2Headers, dependencyId, weight, false, 0, endStream, ctx.newPromise());
            encoder.flowController().writePendingBytes();

            ctx.flush();
            if (endStream) {
                outboundMsgHolder.setRequestWritten(true);
            }
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
        http2ClientChannel.getDataEventListeners().
                forEach(dataEventListener -> dataEventListener.onStreamReset(streamId));
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Http2HeadersFrame) {
            onHeadersRead(ctx, (Http2HeadersFrame) msg);
        } else if (msg instanceof Http2DataFrame) {
            onDataRead((Http2DataFrame) msg);
        } else if (msg instanceof Http2PushPromise) {
            onPushPromiseRead((Http2PushPromise) msg);
        } else if (msg instanceof Http2Reset) {
            onResetRead((Http2Reset) msg);
        }
    }

    private void onHeadersRead(ChannelHandlerContext ctx, Http2HeadersFrame http2HeadersFrame) {
        int streamId = http2HeadersFrame.getStreamId();
        boolean endOfStream = http2HeadersFrame.isEndOfStream();

        OutboundMsgHolder outboundMsgHolder = http2ClientChannel.getInFlightMessage(streamId);
        boolean isServerPush = false;
        if (outboundMsgHolder == null) {
            outboundMsgHolder = http2ClientChannel.getPromisedMessage(streamId);
            if (outboundMsgHolder != null) {
                isServerPush = true;
            } else {
                LOG.warn("Header Frame received on channel: {} with invalid stream id: {} ", http2ClientChannel,
                        streamId);
                return;
            }
        }

        if (isServerPush) {
            if (endOfStream) {
                // Retrieve response message.
                HttpCarbonResponse responseMessage = outboundMsgHolder.getPushResponse(streamId);
                if (responseMessage != null) {
                    onTrailersRead(streamId, http2HeadersFrame.getHeaders(), outboundMsgHolder, responseMessage);
                } else if (http2HeadersFrame.getHeaders().contains(Constants.HTTP2_METHOD)) {
                    // if the header frame is an initial header frame and also it has endOfStream
                    responseMessage = setupResponseCarbonMessage(ctx, streamId, http2HeadersFrame.getHeaders(),
                            outboundMsgHolder);
                    responseMessage.addHttpContent(new DefaultLastHttpContent());
                    outboundMsgHolder.addPushResponse(streamId, responseMessage);
                }
                http2ClientChannel.removePromisedMessage(streamId);
            } else {
                // Create response carbon message.
                HttpCarbonResponse responseMessage = setupResponseCarbonMessage(ctx, streamId,
                        http2HeadersFrame.getHeaders(), outboundMsgHolder);
                outboundMsgHolder.addPushResponse(streamId, responseMessage);
            }
        } else {
            if (endOfStream) {
                // Retrieve response message.
                HttpCarbonResponse responseMessage = outboundMsgHolder.getResponse();
                if (responseMessage != null) {
                    onTrailersRead(streamId, http2HeadersFrame.getHeaders(), outboundMsgHolder, responseMessage);
                } else if (http2HeadersFrame.getHeaders().contains(Constants.HTTP2_METHOD)) {
                    // if the header frame is an initial header frame and also it has endOfStream
                    responseMessage = setupResponseCarbonMessage(ctx, streamId, http2HeadersFrame.getHeaders(),
                            outboundMsgHolder);
                    responseMessage.addHttpContent(new DefaultLastHttpContent());
                    outboundMsgHolder.setResponse(responseMessage);
                }
                http2ClientChannel.removeInFlightMessage(streamId);
            } else {
                // Create response carbon message.
                HttpCarbonResponse responseMessage = setupResponseCarbonMessage(ctx, streamId,
                        http2HeadersFrame.getHeaders(), outboundMsgHolder);
                outboundMsgHolder.setResponse(responseMessage);
            }
        }
    }

    private void onTrailersRead(int streamId, Http2Headers headers, OutboundMsgHolder outboundMsgHolder,
                                HttpCarbonMessage responseMessage) {

        HttpVersion version = new HttpVersion(Constants.HTTP_VERSION_2_0, true);
        LastHttpContent lastHttpContent = new DefaultLastHttpContent();
        HttpHeaders trailers = lastHttpContent.trailingHeaders();

        try {
            HttpConversionUtil.addHttp2ToHttpHeaders(streamId, headers, trailers, version, true, false);
        } catch (Http2Exception e) {
            outboundMsgHolder.getResponseFuture().
                    notifyHttpListener(new Exception("Error while setting http headers", e));
        }
        responseMessage.addHttpContent(lastHttpContent);
    }

    private void onDataRead(Http2DataFrame dataFrame) {
        int streamId = dataFrame.getStreamId();
        ByteBuf data = dataFrame.getData();
        boolean endOfStream = dataFrame.isEndOfStream();

        OutboundMsgHolder outboundMsgHolder = http2ClientChannel.getInFlightMessage(streamId);
        boolean isServerPush = false;
        if (outboundMsgHolder == null) {
            outboundMsgHolder = http2ClientChannel.getPromisedMessage(streamId);
            if (outboundMsgHolder != null) {
                isServerPush = true;
            } else {
                LOG.warn("Data Frame received on channel: {} with invalid stream id: {}", http2ClientChannel, streamId);
                return;
            }
        }
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
    }

    private void onPushPromiseRead(Http2PushPromise pushPromise) {
        int streamId = pushPromise.getStreamId();
        int promisedStreamId = pushPromise.getPromisedStreamId();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Received a push promise on channel: {} over stream id: {}, promisedStreamId: {}",
                    http2ClientChannel, streamId, promisedStreamId);
        }

        OutboundMsgHolder outboundMsgHolder = http2ClientChannel.getInFlightMessage(streamId);
        if (outboundMsgHolder == null) {
            LOG.warn("Push promise received in channel: {} over invalid stream id : {}", http2ClientChannel, streamId);
            return;
        }
        http2ClientChannel.putPromisedMessage(promisedStreamId, outboundMsgHolder);
        pushPromise.setOutboundMsgHolder(outboundMsgHolder);
        outboundMsgHolder.addPromise(pushPromise);
    }

    private void onResetRead(Http2Reset http2Reset) {
        int streamId = http2Reset.getStreamId();
        OutboundMsgHolder outboundMsgHolder = http2ClientChannel.getInFlightMessage(streamId);
        if (outboundMsgHolder != null) {
            outboundMsgHolder.getResponseFuture().
                    notifyHttpListener(new Exception("HTTP/2 stream " + streamId + " reset by the remote peer"));
        }
    }

    private HttpCarbonResponse setupResponseCarbonMessage(ChannelHandlerContext ctx, int streamId,
                                                          Http2Headers http2Headers,
                                                          OutboundMsgHolder outboundMsgHolder) {
        // Create HTTP Response
        CharSequence status = http2Headers.status();
        HttpResponseStatus responseStatus;
        try {
            responseStatus = HttpConversionUtil.parseStatus(status);
        } catch (Http2Exception e) {
            responseStatus = HttpResponseStatus.BAD_GATEWAY;
        }
        HttpVersion version = new HttpVersion(Constants.HTTP_VERSION_2_0, true);
        HttpResponse httpResponse = new DefaultHttpResponse(version, responseStatus);

        // Set headers
        try {
            HttpConversionUtil.addHttp2ToHttpHeaders(
                    streamId, http2Headers, httpResponse.headers(), version, false, false);
        } catch (Http2Exception e) {
            outboundMsgHolder.getResponseFuture().
                    notifyHttpListener(new Exception("Error while setting http headers", e));
        }
        // Create HTTP Carbon Response
        HttpCarbonResponse responseCarbonMsg = new HttpCarbonResponse(httpResponse, new DefaultListener(ctx));

        // Setting properties of the HTTP Carbon Response
        responseCarbonMsg.setProperty(Constants.POOLED_BYTE_BUFFER_FACTORY, new PooledDataStreamerFactory(ctx.alloc()));
        responseCarbonMsg.setProperty(Constants.DIRECTION, Constants.DIRECTION_RESPONSE);
        responseCarbonMsg.setProperty(Constants.HTTP_STATUS_CODE, httpResponse.status().code());

        /* copy required properties for service chaining from incoming carbon message to the response carbon message
        copy shared worker pool */
        responseCarbonMsg.setProperty(Constants.EXECUTOR_WORKER_POOL,
                outboundMsgHolder.getRequest().getProperty(Constants.EXECUTOR_WORKER_POOL));
        return responseCarbonMsg;
    }
}
