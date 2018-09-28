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

/**
 * HTTP/2 utility functions for states.
 */
public class Http2StateUtil {

    private static final Logger LOG = LoggerFactory.getLogger(Http2StateUtil.class);

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

    public static int initiateStream(ChannelHandlerContext ctx, Http2Connection connection,
                               Http2ClientChannel http2ClientChannel,
                               OutboundMsgHolder outboundMsgHolder) throws Http2Exception {
        int id = getNextStreamId(connection);
        http2ClientChannel.putInFlightMessage(id, outboundMsgHolder);
        http2ClientChannel.getDataEventListeners()
                .forEach(dataEventListener -> dataEventListener.onStreamInit(ctx, id));
        return id;
    }

    private static synchronized int getNextStreamId(Http2Connection connection) throws Http2Exception {
        int nextStreamId = connection.local().incrementAndGetNextStreamId();
        connection.local().createStream(nextStreamId, false);
        LOG.debug("Stream created streamId: {}", nextStreamId);
        return nextStreamId;
    }
}
