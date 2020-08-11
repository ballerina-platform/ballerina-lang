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

package org.wso2.transport.http.netty.contractimpl.sender.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2EventAdapter;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.Http2Reset;

import static org.wso2.transport.http.netty.contract.Constants.ZERO_READABLE_BYTES;

/**
 * {@code ClientFrameListener} listens to HTTP/2 Events received from the HTTP/2 backend service
 * and construct HTTP/2 frames.
 */
public class ClientFrameListener extends Http2EventAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(ClientFrameListener.class);

    private Http2ClientChannel http2ClientChannel;

    @Override
    public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Reading data on channel: {} with stream id: {}, isEndOfStream: {}", http2ClientChannel, streamId,
                    endOfStream);
        }

        for (Http2DataEventListener listener : http2ClientChannel.getDataEventListeners()) {
            if (!listener.onDataRead(ctx, streamId, data, endOfStream)) {
                return ZERO_READABLE_BYTES;
            }
        }

        Http2DataFrame dataFrame = new Http2DataFrame(streamId, data, endOfStream);
        ctx.fireChannelRead(dataFrame);
        return ZERO_READABLE_BYTES;
    }

    @Override
    public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers,
                              int streamDependency, short weight, boolean exclusive, int padding, boolean endStream) {
        this.onHeadersRead(ctx, streamId, headers, padding, endStream);
    }

    @Override
    public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding,
                              boolean endStream) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Reading Http2 headers on channel: {} with stream id: {}, isEndOfStream: {}", http2ClientChannel,
                    streamId, endStream);
        }

        for (Http2DataEventListener listener : http2ClientChannel.getDataEventListeners()) {
            if (!listener.onHeadersRead(ctx, streamId, headers, endStream)) {
                return;
            }
        }
        Http2HeadersFrame http2HeadersFrame = new Http2HeadersFrame(streamId, headers, endStream);
        ctx.fireChannelRead(http2HeadersFrame);
    }

    @Override
    public void onSettingsRead(ChannelHandlerContext ctx, Http2Settings settings)
            throws Http2Exception {
        LOG.debug("Http2FrameListenAdapter.onSettingRead()");
        ctx.fireChannelRead(settings);
        super.onSettingsRead(ctx, settings);
    }

    @Override
    public void onRstStreamRead(ChannelHandlerContext ctx, int streamId, long errorCode) {
        LOG.warn("RST received on channel: {} for streamId: {} errorCode: {}", http2ClientChannel, streamId, errorCode);
        Http2Reset http2Reset = new Http2Reset(streamId, Http2Error.valueOf(errorCode));
        ctx.fireChannelRead(http2Reset);
    }

    @Override
    public void onPushPromiseRead(ChannelHandlerContext ctx, int streamId, int promisedStreamId,
                                  Http2Headers headers, int padding) throws Http2Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Received a push promise on channel: {} over stream id: {}, promisedStreamId: {}",
                    http2ClientChannel, streamId, promisedStreamId);
        }
        for (Http2DataEventListener listener : http2ClientChannel.getDataEventListeners()) {
            if (!listener.onPushPromiseRead(ctx, streamId, headers, false)) {
                return;
            }
        }

        Http2PushPromise pushPromise = new Http2PushPromise(Util.createHttpRequestFromHttp2Headers(headers, streamId));
        pushPromise.setPromisedStreamId(promisedStreamId);
        pushPromise.setStreamId(streamId);
        ctx.fireChannelRead(pushPromise);
    }

    @Override
    public void onGoAwaySent(int lastStreamId, long errorCode, ByteBuf debugData) {
        http2ClientChannel.destroy();
    }

    @Override
    public void onGoAwayReceived(int lastStreamId, long errorCode, ByteBuf debugData) {
        http2ClientChannel.destroy();
    }

    /**
     * Sets the {@code TargetChannel} associated with the ClientInboundHandler.
     *
     * @param http2ClientChannel the associated TargetChannel
     */
    public void setHttp2ClientChannel(Http2ClientChannel http2ClientChannel) {
        this.http2ClientChannel = http2ClientChannel;
    }
}
