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

package org.wso2.transport.http.netty.contractimpl.common.states;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2ClientChannel;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2DataEventListener;
import org.wso2.transport.http.netty.contractimpl.sender.http2.OutboundMsgHolder;
import org.wso2.transport.http.netty.message.Http2PushPromise;

/**
 * HTTP/2 utility functions for states.
 */
public class Http2StateUtil {

    private static final Logger LOG = LoggerFactory.getLogger(Http2StateUtil.class);

    /**
     * Write HTTP2 headers.
     *
     * @param ctx                the channel handler context
     * @param outboundMsgHolder  the outbound message holder
     * @param http2ClientChannel the client channel related to the handler
     * @param encoder            the HTTP2 connection encoder
     * @param streamId           the id of the stream
     * @param headers            the HTTP headers
     * @param http2Headers       the HTTP2 headers
     * @param endStream          is this the end of stream
     * @throws Http2Exception if a protocol-related error occurred
     */
    public static void writeHttp2Headers(ChannelHandlerContext ctx, OutboundMsgHolder outboundMsgHolder,
                                         Http2ClientChannel http2ClientChannel, Http2ConnectionEncoder encoder,
                                         int streamId, HttpHeaders headers, Http2Headers http2Headers,
                                         boolean endStream) throws Http2Exception {
        int dependencyId = headers.getInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_DEPENDENCY_ID.text(), 0);
        short weight = headers.getShort(HttpConversionUtil.ExtensionHeaderNames.STREAM_WEIGHT.text(),
                Http2CodecUtil.DEFAULT_PRIORITY_WEIGHT);
        for (Http2DataEventListener dataEventListener : http2ClientChannel.getDataEventListeners()) {
            if (!dataEventListener.onHeadersWrite(ctx, streamId, http2Headers, endStream)) {
                return;
            }
        }

        encoder.writeHeaders(ctx, streamId, http2Headers, dependencyId, weight, false, 0, endStream, ctx.newPromise());
        encoder.flowController().writePendingBytes();
        ctx.flush();

        if (endStream) {
            outboundMsgHolder.setRequestWritten(true);
        }
    }

    /**
     * Initiate HTTP2 stream.
     *
     * @param ctx                the channel handler context
     * @param connection         the HTTP2 connection
     * @param http2ClientChannel the client channel related to the handler
     * @param outboundMsgHolder  the outbound message holder
     * @return stream id of next stream
     * @throws Http2Exception if a protocol-related error occurred
     */
    public static int initiateStream(ChannelHandlerContext ctx, Http2Connection connection,
                                     Http2ClientChannel http2ClientChannel,
                                     OutboundMsgHolder outboundMsgHolder) throws Http2Exception {
        int streamId = getNextStreamId(connection);
        createStream(connection, streamId);
        http2ClientChannel.putInFlightMessage(streamId, outboundMsgHolder);
        http2ClientChannel.getDataEventListeners()
                .forEach(dataEventListener -> dataEventListener.onStreamInit(ctx, streamId));
        return streamId;
    }

    /**
     * Return the stream id of next stream.
     *
     * @param conn the HTTP2 connection
     * @return the next stream id
     */
    private static synchronized int getNextStreamId(Http2Connection conn) {
        return conn.local().incrementAndGetNextStreamId();
    }

    /**
     * Create stream with given stream id.
     *
     * @param conn     the HTTP2 connection
     * @param streamId the id of the stream
     * @throws Http2Exception if a protocol-related error occurred
     */
    private static synchronized void createStream(Http2Connection conn, int streamId) throws Http2Exception {
        conn.local().createStream(streamId, false);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Stream created streamId: {}", streamId);
        }
    }

    /**
     * Add push promise message.
     *
     * @param http2PushPromise   the HTTP2 push promise
     * @param http2ClientChannel the client channel related to the handler
     * @param outboundMsgHolder  the outbound message holder
     */
    public static void onPushPromiseRead(Http2PushPromise http2PushPromise, Http2ClientChannel http2ClientChannel,
                                         OutboundMsgHolder outboundMsgHolder) {
        int streamId = http2PushPromise.getStreamId();
        int promisedStreamId = http2PushPromise.getPromisedStreamId();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Received a push promise on channel: {} over stream id: {}, promisedStreamId: {}",
                    http2ClientChannel, streamId, promisedStreamId);
        }

        if (outboundMsgHolder == null) {
            LOG.warn("Push promise received in channel: {} over invalid stream id : {}", http2ClientChannel, streamId);
            return;
        }
        http2ClientChannel.putPromisedMessage(promisedStreamId, outboundMsgHolder);
        http2PushPromise.setOutboundMsgHolder(outboundMsgHolder);
        outboundMsgHolder.addPromise(http2PushPromise);
    }
}
