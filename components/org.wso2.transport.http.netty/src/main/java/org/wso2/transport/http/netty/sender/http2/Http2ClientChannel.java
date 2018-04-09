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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.HttpRoute;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@code Http2ClientChannel} encapsulates the Channel associated with a particular connection.
 * <p>
 * This shouldn't have anything related to a single message.
 */
public class Http2ClientChannel {

    private ConcurrentHashMap<Integer, OutboundMsgHolder> inFlightMessages, promisedMessages;
    private Channel channel;
    private Http2Connection connection;
    private ChannelFuture channelFuture;
    private HttpRoute httpRoute;
    private Http2ConnectionManager http2ConnectionManager;
    // Whether channel is operates with maximum number of allowed streams
    private AtomicBoolean isExhausted = new AtomicBoolean(false);
    // Number of active streams. Need to start from 1 to prevent someone stealing the connection from the creator
    private AtomicInteger activeStreams = new AtomicInteger(1);
    private boolean upgradedToHttp2 = false;
    private List<Http2DataEventListener> dataEventListeners;

    private static final Logger log = LoggerFactory.getLogger(Http2ClientChannel.class);

    public Http2ClientChannel(Http2ConnectionManager http2ConnectionManager, Http2Connection connection,
                              HttpRoute httpRoute, Channel channel) {
        this.http2ConnectionManager = http2ConnectionManager;
        this.channel = channel;
        this.connection = connection;
        this.httpRoute = httpRoute;
        this.connection.addListener(new StreamCloseListener(this));
        dataEventListeners = new ArrayList<>();
        inFlightMessages = new ConcurrentHashMap<>();
        promisedMessages = new ConcurrentHashMap<>();
    }

    /**
     * Gets the netty channel associated with this {@code Http2ClientChannel}.
     *
     * @return channel which represent the connection
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Sets the netty channel associated with this {@code Http2ClientChannel}.
     *
     * @param channel channel which represent the connection
     */
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    /**
     * Gets the HTTP/2 connection associated with this {@code Http2ClientChannel}.
     *
     * @return associated HTTP/2 connection
     */
    public Http2Connection getConnection() {
        return connection;
    }

    /**
     * Gets the {@code ChannelFuture} associated with this {@code Http2ClientChannel}.
     *
     * @return associated ChannelFuture
     */
    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    /**
     * Sets the {@code ChannelFuture} associated with this {@code Http2ClientChannel}.
     *
     * @param  channelFuture associated ChannelFuture
     */
    public void setChannelFuture(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
    }

    /**
     * Adds a in-flight message.
     *
     * @param streamId        stream id
     * @param inFlightMessage {@link OutboundMsgHolder} which holds the in-flight message
     */
    public void putInFlightMessage(int streamId, OutboundMsgHolder inFlightMessage) {
        if (log.isDebugEnabled()) {
            log.debug("TID: {} OID: {} In flight message added to stream id: {}",
                     Thread.currentThread().getId(), this.toString(), streamId);
        }
        inFlightMessages.put(streamId, inFlightMessage);
    }

    /**
     * Gets the in-flight message associated with the a particular stream id.
     *
     * @param streamId stream id
     * @return in-flight message associated with the a particular stream id
     */
    public OutboundMsgHolder getInFlightMessage(int streamId) {
        if (log.isDebugEnabled()) {
            log.debug("TID: {} OID: {} Getting in flight message for stream id: {}",
                      Thread.currentThread().getId(), this.toString(), streamId);
        }
        return inFlightMessages.get(streamId);
    }

    /**
     * Removes a in-flight message.
     *
     * @param streamId stream id
     */
    void removeInFlightMessage(int streamId) {
        if (log.isDebugEnabled()) {
            log.debug("TID: {} OID: {} In flight message for stream id: {} removed",
                      Thread.currentThread().getId(), this.toString(), streamId);
        }
        inFlightMessages.remove(streamId);
    }

    /**
     * Adds a promised message.
     *
     * @param streamId        stream id
     * @param promisedMessage {@code OutboundMsgHolder} which holds the promised message
     */
    void putPromisedMessage(int streamId, OutboundMsgHolder promisedMessage) {
        promisedMessages.put(streamId, promisedMessage);
    }

    /**
     * Gets the promised message associated with the a particular stream id.
     *
     * @param streamId stream id
     * @return promised message associated with the a particular stream id
     */
    OutboundMsgHolder getPromisedMessage(int streamId) {
        return promisedMessages.get(streamId);
    }

    /**
     * Checks whether associated connection is upgraded to http2.
     *
     * @return whether associated connection is upgraded to http2
     */
    public boolean isUpgradedToHttp2() {
        return upgradedToHttp2;
    }

    /**
     * Sets whether associated connection is upgraded to http2.
     *
     * @param upgradedToHttp2 whether upgraded to http2 or not
     */
    public void setUpgradedToHttp2(boolean upgradedToHttp2) {
        this.upgradedToHttp2 = upgradedToHttp2;
    }

    /**
     * Removes a promised message.
     *
     * @param streamId stream id
     */
    void removePromisedMessage(int streamId) {
        promisedMessages.remove(streamId);
    }

    /**
     * Increments and gets the active streams count.
     *
     * @return number of active streams count
     */
    int incrementActiveStreamCount() {
        return activeStreams.incrementAndGet();
    }

    /**
     * Marks the channel has reached the maximum number of active streams.
     */
    void markAsExhausted() {
        isExhausted.set(true);
    }


    public void addDataEventListener(Http2DataEventListener dataEventListener) {
        dataEventListeners.add(dataEventListener);
    }

    public List<Http2DataEventListener> getDataEventListeners() {
        return dataEventListeners;
    }

    /**
     * Listener which listen to the stream closure event.
     */
    private class StreamCloseListener extends Http2EventAdapter {

        private Http2ClientChannel http2ClientChannel;

        public StreamCloseListener(Http2ClientChannel http2ClientChannel) {
            this.http2ClientChannel = http2ClientChannel;
        }

        public void onStreamClosed(Http2Stream stream) {
            // Channel is no longer exhausted, so we can return it back to the pool
            activeStreams.decrementAndGet();
            http2ClientChannel.getDataEventListeners().
                    forEach(dataEventListener -> dataEventListener.onStreamClose(stream.id()));
            if (isExhausted.getAndSet(false)) {
                http2ConnectionManager.returnClientChannel(httpRoute, http2ClientChannel);
            }
        }
    }
}
