/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.test.util.http2;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.internal.PlatformDependent;
import org.ballerinalang.test.util.TestConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Process {@link io.netty.handler.codec.http.FullHttpResponse} translated from HTTP/2 frames
 */
public class HTTP2ResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private static final Logger log = LoggerFactory.getLogger(HTTP2ResponseHandler.class);
    private Map<Integer, Entry<ChannelFuture, ChannelPromise>> streamIdPromiseMap;
    private Map<Integer, FullHttpResponse> streamIdResponseMap;

    public HTTP2ResponseHandler() {
        // Use a concurrent map because we add and iterate from the main thread
        streamIdPromiseMap = PlatformDependent.newConcurrentHashMap();
        streamIdResponseMap = PlatformDependent.newConcurrentHashMap();
    }

    /**
     * Create an association between an anticipated response stream id and a {@link io.netty.channel.ChannelPromise}
     *
     * @param streamId    The stream for which a response is expected
     * @param writeFuture A future that represent the request write operation
     * @param promise     The promise object that will be used to wait/notify events
     * @return The previous object associated with {@code streamId}
     */
    public Entry<ChannelFuture, ChannelPromise> put(int streamId, ChannelFuture writeFuture, ChannelPromise promise) {
        return streamIdPromiseMap.put(streamId, new SimpleEntry<ChannelFuture, ChannelPromise>(writeFuture, promise));
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
        Integer streamId = msg.headers().getInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());
        if (streamId == null) {
            log.error("HTTP2ResponseHandler unexpected message received: " + msg);
            return;
        }
        Entry<ChannelFuture, ChannelPromise> entry = streamIdPromiseMap.get(streamId);
        if (entry == null) {
            if (streamId == 1) {
                log.error("HTTP2 Upgrade request has received from  stream : " + streamId);
            }
        } else {
            streamIdResponseMap.put(streamId, msg.copy());
            entry.getValue().setSuccess();
        }
    }

    /**
     * Provide asynchronous response to HTTP2 request
     *
     * @param streamId StreamID
     * @return Response string
     */
    public FullHttpResponse getResponse(int streamId) {

        FullHttpResponse message = streamIdResponseMap.get(streamId);
        if (message != null) {
            return message;
        } else {
            Entry<ChannelFuture, ChannelPromise> channelFutureChannelPromiseEntry = streamIdPromiseMap.get(streamId);
            if (channelFutureChannelPromiseEntry != null) {
                ChannelFuture writeFuture = channelFutureChannelPromiseEntry.getKey();
                if (!writeFuture.awaitUninterruptibly(TestConstant.HTTP2_RESPONSE_TIME_OUT, TestConstant
                        .HTTP2_RESPONSE_TIME_UNIT)) {
                    streamIdPromiseMap.remove(streamId);
                    throw new IllegalStateException("Timed out waiting to write for stream id " + streamId);
                }
                if (!writeFuture.isSuccess()) {
                    streamIdPromiseMap.remove(streamId);
                    throw new RuntimeException(writeFuture.cause());
                }
                ChannelPromise promise = channelFutureChannelPromiseEntry.getValue();
                if (!promise.awaitUninterruptibly(TestConstant.HTTP2_RESPONSE_TIME_OUT, TestConstant
                        .HTTP2_RESPONSE_TIME_UNIT)) {
                    streamIdPromiseMap.remove(streamId);
                    throw new IllegalStateException("Timed out waiting for response on stream id " + streamId);
                }
                if (!promise.isSuccess()) {
                    streamIdPromiseMap.remove(streamId);
                    throw new RuntimeException(promise.cause());
                }
            }
        }
        return streamIdResponseMap.get(streamId);
    }
}
