/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.transport.http.netty.sender.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpClientUpgradeHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.EmptyHttp2Headers;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.Util;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Sending requests and receiving responses from a backend server
 */
public class Http2ClientHandler extends ChannelDuplexHandler {

    private static final Log log = LogFactory.getLog(Http2ClientHandler.class);
    private ChannelHandlerContext channelHandlerContext;
    private Http2Connection connection;
    private Http2ConnectionEncoder encoder;
    private boolean upgradedToHttp2 = false;
    private TargetChannel targetChannel;

    public Http2ClientHandler(Http2Connection connection, Http2ConnectionEncoder encoder) {
        this.connection = connection;
        this.encoder = encoder;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("ClientHandler Channel read fired");
    }

    public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
        this.channelHandlerContext = channelHandlerContext;
    }

    public Http2Connection getConnection() {
        return connection;
    }

    public void setTargetChannel(TargetChannel targetChannel) {
        this.targetChannel = targetChannel;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        ctx.fireUserEventTriggered(evt);

        if (evt instanceof HttpClientUpgradeHandler.UpgradeEvent) {
            HttpClientUpgradeHandler.UpgradeEvent upgradeEvent = (HttpClientUpgradeHandler.UpgradeEvent) evt;
            if (HttpClientUpgradeHandler.UpgradeEvent.UPGRADE_SUCCESSFUL.name().equals(upgradeEvent.name())) {
                upgradedToHttp2 = true;
            }
        }
    }

    public void writeRequest(OutboundHttpRequestHolder outboundHttpRequestHolder) {

        HTTPCarbonMessage httpOutboundRequest = outboundHttpRequestHolder.getHttpCarbonMessage();

        if (upgradedToHttp2) {
            int streamId = getStreamId();
            targetChannel.putInFlightMessage(streamId, outboundHttpRequestHolder);

            HttpRequest httpRequest = Util.createHttpRequest(httpOutboundRequest);


            // Write Headers
            writeHttp2Headers(httpRequest, streamId, true);
            // Write Content
            httpOutboundRequest.getHttpContentAsync().
                    setMessageListener((httpContent ->
                                                this.targetChannel.getChannel().eventLoop().execute(() -> {
                                                    try {
                                                        writeHttp2Content(httpContent, streamId, true);
                                                    } catch (Exception exception) {
                                                        String errorMsg = "Failed to send the request : " +
                                                                          exception.getMessage().
                                                                                  toLowerCase(Locale.ENGLISH);
                                                        log.error(errorMsg, exception);
                                                    }
                                                })));
        } else {
            new UpgradeRequestWriter(outboundHttpRequestHolder).writeContent();
        }
    }

    private synchronized int getStreamId() {
        return connection.local().incrementAndGetNextStreamId();
    }

    private void writeHttp2Headers(HttpMessage httpMsg, int streamId, boolean validateHeaders) {
        // Convert and write the headers.
        httpMsg.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), "HTTP");

        Http2Headers http2Headers = HttpConversionUtil.toHttp2Headers(httpMsg, validateHeaders);
        boolean endStream = httpMsg instanceof FullHttpMessage && !((FullHttpMessage) httpMsg).content().isReadable();
        writeHeaders(streamId, httpMsg.headers(), http2Headers, endStream);
    }

    private void writeHttp2Content(HttpContent msg, int streamId, boolean validateHeaders) {

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
                http2Trailers = HttpConversionUtil.toHttp2Headers(trailers, validateHeaders);
            }

            // Write the data
            final ByteBuf content = ((HttpContent) msg).content();
            boolean endStream = isLastContent && trailers.isEmpty();
            release = false;
            encoder.writeData(channelHandlerContext, streamId, content, 0, endStream,
                              channelHandlerContext.newPromise());
            channelHandlerContext.flush();

            if (!trailers.isEmpty()) {
                // Write trailing headers.
                writeHeaders(streamId, trailers, http2Trailers, true);
            }
        } finally {
            if (release) {
                ReferenceCountUtil.release(msg);
            }
        }
    }

    private void writeHeaders(int streamId, HttpHeaders headers, Http2Headers http2Headers, boolean endStream) {
        int dependencyId = headers.getInt(
                HttpConversionUtil.ExtensionHeaderNames.STREAM_DEPENDENCY_ID.text(), 0);
        short weight = headers.getShort(
                HttpConversionUtil.ExtensionHeaderNames.STREAM_WEIGHT.text(), Http2CodecUtil.DEFAULT_PRIORITY_WEIGHT);
        encoder.writeHeaders(channelHandlerContext, streamId, http2Headers, dependencyId, weight, false,
                             0, endStream, channelHandlerContext.newPromise());
        channelHandlerContext.flush();
    }

    private class UpgradeRequestWriter {

        List<HttpContent> contentList = new ArrayList<>();
        int contentLength = 0;

        boolean isRequestWritten = false;
        String httpVersion = "1.1";
        ChunkConfig chunkConfig = ChunkConfig.AUTO;
        HTTPCarbonMessage httpOutboundRequest;
        HttpResponseFuture responseFuture;

        public UpgradeRequestWriter(OutboundHttpRequestHolder outboundHttpRequestHolder) {
            httpOutboundRequest = outboundHttpRequestHolder.getHttpCarbonMessage();
            responseFuture = outboundHttpRequestHolder.getHttpInboundResponseFuture();

        }

        public void writeContent() {

            httpOutboundRequest.getHttpContentAsync().
                    setMessageListener((httpContent -> targetChannel.getChannel().eventLoop().execute(() -> {
                        try {
                            writeOutboundRequest(httpContent);
                        } catch (Exception exception) {
                            String errorMsg = "Failed to send the request : "
                                              + exception.getMessage().toLowerCase(Locale.ENGLISH);
                            log.error(errorMsg, exception);
                            responseFuture.notifyHttpListener(exception);
                        }
                    })));
        }

        private void writeOutboundRequest(HttpContent httpContent) throws Exception {
            if (Util.isLastHttpContent(httpContent)) {
                if (!this.isRequestWritten) {
                    // this means we need to send an empty payload
                    // depending on the http verb
                    if (Util.isEntityBodyAllowed(getHttpMethod(httpOutboundRequest))) {
                        if (chunkConfig == ChunkConfig.ALWAYS && Util.isVersionCompatibleForChunking(httpVersion)) {
                            Util.setupChunkedRequest(httpOutboundRequest);
                        } else {
                            contentLength += httpContent.content().readableBytes();
                            Util.setupContentLengthRequest(httpOutboundRequest, contentLength);
                        }
                    }
                    writeOutboundRequestHeaders(httpOutboundRequest);
                }

                writeOutboundRequestBody(httpContent);
            } else {
                if ((chunkConfig == ChunkConfig.ALWAYS || chunkConfig == ChunkConfig.AUTO)
                    && Util.isVersionCompatibleForChunking(httpVersion)) {
                    if (!this.isRequestWritten) {
                        Util.setupChunkedRequest(httpOutboundRequest);
                        writeOutboundRequestHeaders(httpOutboundRequest);
                    }
                    ChannelFuture outboundRequestChannelFuture =
                            channelHandlerContext.channel().writeAndFlush(httpContent);
                    notifyIfFailure(outboundRequestChannelFuture);
                } else {
                    this.contentList.add(httpContent);
                    contentLength += httpContent.content().readableBytes();
                }
            }
        }

        private void writeOutboundRequestHeaders(HTTPCarbonMessage httpOutboundRequest) {
            httpOutboundRequest.setProperty(Constants.HTTP_VERSION, httpVersion);
            HttpRequest httpRequest = Util.createHttpRequest(httpOutboundRequest);
            isRequestWritten = true;
            targetChannel.getChannel().write(httpRequest);
        }

        private void writeOutboundRequestBody(HttpContent lastHttpContent) {
            if (chunkConfig == ChunkConfig.NEVER || !Util.isVersionCompatibleForChunking(httpVersion)) {
                for (HttpContent cachedHttpContent : contentList) {
                    ChannelFuture outboundRequestChannelFuture =
                            channelHandlerContext.channel().writeAndFlush(cachedHttpContent);
                    notifyIfFailure(outboundRequestChannelFuture);
                }
            }
            ChannelFuture outboundRequestChannelFuture = targetChannel.getChannel().writeAndFlush(lastHttpContent);
            notifyIfFailure(outboundRequestChannelFuture);
        }

        private void notifyIfFailure(ChannelFuture outboundRequestChannelFuture) {
            outboundRequestChannelFuture.addListener(writeOperationPromise -> {
                if (writeOperationPromise.cause() != null) {
                    Throwable throwable = writeOperationPromise.cause();
                    if (throwable instanceof ClosedChannelException) {
                        throwable = new IOException(Constants.REMOTE_SERVER_ABRUPTLY_CLOSE_REQUEST_CONNECTION);
                    }
                    log.error(Constants.REMOTE_SERVER_ABRUPTLY_CLOSE_REQUEST_CONNECTION, throwable);
                    responseFuture.notifyHttpListener(throwable);
                }
            });
        }

        private String getHttpMethod(HTTPCarbonMessage httpOutboundRequest) throws Exception {
            String httpMethod = (String) httpOutboundRequest.getProperty(Constants.HTTP_METHOD);
            if (httpMethod == null) {
                throw new Exception("Couldn't get the HTTP method from the outbound request");
            }
            return httpMethod;
        }

    }

}
