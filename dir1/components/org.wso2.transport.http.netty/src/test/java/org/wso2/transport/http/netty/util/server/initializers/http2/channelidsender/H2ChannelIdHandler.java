/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.util.server.initializers.http2.channelidsender;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2Flags;
import io.netty.handler.codec.http2.Http2FrameListener;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.wso2.transport.http.netty.util.Http2Util.http1HeadersToHttp2Headers;

/**
 * A simple handler that responds with the connection id.
 */
public final class H2ChannelIdHandler extends Http2ConnectionHandler implements Http2FrameListener {
    private static final Logger LOG = LoggerFactory.getLogger(H2ChannelIdHandler.class);

    H2ChannelIdHandler(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder,
                       Http2Settings initialSettings) {
        super(decoder, encoder, initialSettings);
    }

    /**
     * Handles the cleartext HTTP upgrade event. If an upgrade occurred, sends a simple response via HTTP/2 on stream 1
     * (the stream specifically reserved for cleartext HTTP upgrade).
     *
     * @param ctx represents the channel handler context
     * @param evt represent an event
     * @throws Exception if occurred during user event
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof HttpServerUpgradeHandler.UpgradeEvent) {
            HttpServerUpgradeHandler.UpgradeEvent upgradeEvent =
                    (HttpServerUpgradeHandler.UpgradeEvent) evt;
            onHeadersRead(ctx, 1, http1HeadersToHttp2Headers(upgradeEvent.upgradeRequest()), 0, true);
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("Exception occurred in H2ChannelIdHandler : {}", cause.getMessage());
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }

    /**
     * Sends DATA frame with the connection id as the payload to the client.
     *
     * @param ctx      represents the channel handler context
     * @param streamId represents the stream id that the response should be sent to
     */
    private void sendResponse(ChannelHandlerContext ctx, int streamId) {
        // Send a frame for the response status
        Http2Headers headers = new DefaultHttp2Headers().status(OK.codeAsText());
        encoder().writeHeaders(ctx, streamId, headers, 0, false, ctx.newPromise());
        ByteBuf content = Unpooled.wrappedBuffer(ctx.channel().id().asLongText().getBytes());
        encoder().writeData(ctx, streamId, content, 0, true, ctx.newPromise());
    }

    @Override
    public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream) {
        int processed = data.readableBytes() + padding;
        if (endOfStream) {
            sendResponse(ctx, streamId);
        }
        return processed + padding;
    }

    @Override
    public void onHeadersRead(ChannelHandlerContext ctx, int streamId,
                              Http2Headers headers, int padding, boolean endOfStream) {
        if (endOfStream) {
            sendResponse(ctx, streamId);
        }
    }

    @Override
    public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency,
                              short weight, boolean exclusive, int padding, boolean endOfStream) {
        onHeadersRead(ctx, streamId, headers, padding, endOfStream);
    }

    @Override
    public void onPriorityRead(ChannelHandlerContext ctx, int streamId, int streamDependency,
                               short weight, boolean exclusive) {
        LOG.debug("onPriorityRead {}", streamId);
    }

    @Override
    public void onRstStreamRead(ChannelHandlerContext ctx, int streamId, long errorCode) {
        LOG.debug("onRstStreamRead {}", streamId);
    }

    @Override
    public void onSettingsAckRead(ChannelHandlerContext ctx) {
        LOG.debug("onSettingsAckRead");
    }

    @Override
    public void onSettingsRead(ChannelHandlerContext ctx, Http2Settings settings) {
        LOG.debug("onSettingsRead");
    }

    @Override
    public void onPingRead(ChannelHandlerContext ctx, long data) {
        LOG.debug("onPingRead");
    }

    @Override
    public void onPingAckRead(ChannelHandlerContext ctx, long data) {
        LOG.debug("onPingAckRead");
    }

    @Override
    public void onPushPromiseRead(ChannelHandlerContext ctx, int streamId, int promisedStreamId,
                                  Http2Headers headers, int padding) {
        LOG.debug("onPushPromiseRead {}", streamId);
    }

    @Override
    public void onGoAwayRead(ChannelHandlerContext ctx, int lastStreamId, long errorCode, ByteBuf debugData) {
        LOG.debug("onGoAwayRead {}", lastStreamId);
    }

    @Override
    public void onWindowUpdateRead(ChannelHandlerContext ctx, int streamId, int windowSizeIncrement) {
        LOG.debug("onWindowUpdateRead {}", streamId);
    }

    @Override
    public void onUnknownFrame(ChannelHandlerContext ctx, byte frameType, int streamId,
                               Http2Flags flags, ByteBuf payload) {
        LOG.debug("onUnknownFrame {}", streamId);
    }
}
