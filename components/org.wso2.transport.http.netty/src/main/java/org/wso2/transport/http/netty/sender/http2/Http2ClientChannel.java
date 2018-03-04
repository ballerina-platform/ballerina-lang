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

package org.wso2.transport.http.netty.sender.http2;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2EventAdapter;
import io.netty.handler.codec.http2.Http2Stream;
import org.wso2.transport.http.netty.common.HttpRoute;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@code TargetChannel} encapsulate the Channel associate with a particular connection.
 * <p>
 * This shouldn't have anything related to a single message.
 */
public class Http2ClientChannel {

    private static ConcurrentHashMap<Integer, OutboundMsgHolder> inFlightMessages = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Integer, OutboundMsgHolder> promisedMessages = new ConcurrentHashMap<>();
    private Channel channel;
    private Http2Connection connection;
    private ChannelFuture channelFuture;
    private HttpRoute httpRoute;
    private Http2ConnectionManager http2ConnectionManager;
    // Whether channel is operates with maximum number of allowed streams
    private AtomicBoolean isExhausted = new AtomicBoolean(false);

    // Number of active streams. Need to start from 1 to prevent someone stealing the connection from the creator
    private AtomicInteger activeStreams = new AtomicInteger(1);

    public Http2ClientChannel(Http2ConnectionManager http2ConnectionManager, Http2Connection connection,
                              HttpRoute httpRoute, Channel channel) {
        this.http2ConnectionManager = http2ConnectionManager;
        this.channel = channel;
        this.connection = connection;
        this.httpRoute = httpRoute;
        this.connection.addListener(new StreamCloseListener(this));
    }

    /**
     * Get the netty channel associated with this TargetChannel
     *
     * @return channel which represent the connection
     */
    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    /**
     * Get the HTTP/2 connection associated with this TargetChannel
     *
     * @return associated HTTP/2 connection
     */
    public Http2Connection getConnection() {
        return connection;
    }

    /**
     * Get the {@code ChannelFuture} associated with this TargetChannel
     *
     * @return associated ChannelFuture
     */
    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public void setChannelFuture(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
    }

    /**
     * Add a in-flight message
     *
     * @param streamId        stream id
     * @param inFlightMessage {@code OutboundMsgHolder} which holds the in-flight message
     */
    public void putInFlightMessage(int streamId, OutboundMsgHolder inFlightMessage) {
        inFlightMessages.put(streamId, inFlightMessage);
    }

    /**
     * Get the in-flight message associated with the a particular stream id
     *
     * @param streamId stream id
     * @return in-flight message associated with the a particular stream id
     */
    public OutboundMsgHolder getInFlightMessage(int streamId) {
        return inFlightMessages.get(streamId);
    }

    /**
     * Remove in-flight message
     *
     * @param streamId stream id
     */
    void removeInFlightMessage(int streamId) {
        inFlightMessages.remove(streamId);
    }

    /**
     * Add a promised message
     *
     * @param streamId        stream id
     * @param promisedMessage {@code OutboundMsgHolder} which holds the promised message
     */
    public void putPromisedMessage(int streamId, OutboundMsgHolder promisedMessage) {
        promisedMessages.put(streamId, promisedMessage);
    }

    /**
     * Get the promised message associated with the a particular stream id
     *
     * @param streamId stream id
     * @return promised message associated with the a particular stream id
     */
    public OutboundMsgHolder getPromisedMessage(int streamId) {
        return promisedMessages.get(streamId);
    }

    /**
     * Remove promised message
     *
     * @param streamId stream id
     */
    void removePromisedMessage(int streamId) {
        promisedMessages.remove(streamId);
    }

    /**
     * Increment and get the active streams count
     *
     * @return number of active streams count
     */
    int incrementActiveStreamCount() {
        return activeStreams.incrementAndGet();
    }

    /**
     * Mark the TargetChannel has reached the maximum number of active streams
     */
    void markAsExhausted() {
        isExhausted.set(true);
    }

    /**
     * Listener which listen to the stream closure event
     */
    private class StreamCloseListener extends Http2EventAdapter {

        private Http2ClientChannel http2ClientChannel;

        public StreamCloseListener(Http2ClientChannel http2ClientChannel) {
            this.http2ClientChannel = http2ClientChannel;
        }

        public void onStreamClosed(Http2Stream stream) {
            // TargetChannel is no longer exhausted, so we can return it back to the pool
            activeStreams.decrementAndGet();
            if (isExhausted.getAndSet(false)) {
                http2ConnectionManager.returnTargetChannel(httpRoute, http2ClientChannel);
            }
        }
    }
}
