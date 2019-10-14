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

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2EventAdapter;
import io.netty.handler.codec.http2.Http2Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contractimpl.common.HttpRoute;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2MessageStateContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@code Http2ClientChannel} encapsulates the Channel associated with a particular connection.
 * <p>
 * This shouldn't have anything related to a single message.
 */
public class Http2ClientChannel {

    private static final Logger LOG = LoggerFactory.getLogger(Http2ClientChannel.class);

    private ConcurrentHashMap<Integer, OutboundMsgHolder> inFlightMessages;
    private ConcurrentHashMap<Integer, OutboundMsgHolder> promisedMessages;
    private Channel channel;
    private Http2Connection connection;
    private ChannelFuture channelFuture;
    private HttpRoute httpRoute;
    private Http2ConnectionManager http2ConnectionManager;
    // Whether channel is operates with maximum number of allowed streams
    private AtomicBoolean isExhausted = new AtomicBoolean(false);
    // Number of active streams. Need to start from 1 to prevent someone stealing the connection from the creator
    private AtomicInteger activeStreams = new AtomicInteger(1);
    private int socketIdleTimeout = Constants.ENDPOINT_TIMEOUT;
    private Map<String, Http2DataEventListener> dataEventListeners;
    private StreamCloseListener streamCloseListener;

    public Http2ClientChannel(Http2ConnectionManager http2ConnectionManager, Http2Connection connection,
                              HttpRoute httpRoute, Channel channel) {
        this.http2ConnectionManager = http2ConnectionManager;
        this.channel = channel;
        this.connection = connection;
        this.httpRoute = httpRoute;
        streamCloseListener = new StreamCloseListener(this);
        this.connection.addListener(streamCloseListener);
        dataEventListeners = new HashMap<>();
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
     * @param channelFuture associated ChannelFuture
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("In flight message added to channel: {} with stream id: {}  ", this, streamId);
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting in flight message for stream id: {} from channel: {}", streamId, this);
        }
        return inFlightMessages.get(streamId);
    }

    /**
     * Removes a in-flight message.
     *
     * @param streamId stream id
     */
    public void removeInFlightMessage(int streamId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("In flight message for stream id: {} removed from channel: {}", streamId, this);
        }
        inFlightMessages.remove(streamId);
    }

    /**
     * Adds a promised message.
     *
     * @param streamId        stream id
     * @param promisedMessage {@code OutboundMsgHolder} which holds the promised message
     */
    public void putPromisedMessage(int streamId, OutboundMsgHolder promisedMessage) {
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
     * Removes a promised message.
     *
     * @param streamId stream id
     */
    public void removePromisedMessage(int streamId) {
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

    /**
     * Adds a listener which listen for HTTP/2 data events.
     *
     * @param name              name of the listener
     * @param dataEventListener the data event listener
     */
    public void addDataEventListener(String name, Http2DataEventListener dataEventListener) {
        dataEventListeners.put(name, dataEventListener);
    }

    /**
     * Gets the list of listeners which listen for HTTP/2 data events.
     *
     * @return list of data event listeners
     */
    public List<Http2DataEventListener> getDataEventListeners() {
        return new ArrayList<>(dataEventListeners.values());
    }

    /**
     * Gets the socket idle timeout of the channel.
     *
     * @return the socket idle timeout of the channel
     */
    public int getSocketIdleTimeout() {
        return socketIdleTimeout;
    }

    /**
     * Sets the socket idle timeout of the channel.
     *
     * @param socketIdleTimeout the socket idle timeout
     */
    public void setSocketIdleTimeout(int socketIdleTimeout) {
        this.socketIdleTimeout = socketIdleTimeout;
    }

    /**
     * Destroys the Http2 client channel.
     */
    void destroy() {
        inFlightMessages.forEach((streamId, outboundMsgHolder) -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Remove back pressure listener from stream {} ", streamId);
            }
            outboundMsgHolder.getBackPressureObservable().removeListener();
        });
        handleConnectionClose();
        this.connection.removeListener(streamCloseListener);
        inFlightMessages.clear();
        promisedMessages.clear();
        http2ConnectionManager.removeClientChannel(httpRoute, this);
    }

    /**
     * Notify all the streams in the closed channel.
     */
    private void handleConnectionClose() {
        if (!inFlightMessages.isEmpty()) {
            inFlightMessages.values().forEach(outBoundMsgHolder -> {
                Http2MessageStateContext messageStateContext =
                        outBoundMsgHolder.getRequest().getHttp2MessageStateContext();
                if (messageStateContext != null) {
                    messageStateContext.getSenderState().handleConnectionClose(outBoundMsgHolder);
                }
            });
        }
    }

    /**
     * Listener which listen to the stream closure event.
     */
    private class StreamCloseListener extends Http2EventAdapter {

        private Http2ClientChannel http2ClientChannel;

        StreamCloseListener(Http2ClientChannel http2ClientChannel) {
            this.http2ClientChannel = http2ClientChannel;
        }

        @Override
        public void onStreamClosed(Http2Stream stream) {
            // Channel is no longer exhausted, so we can return it back to the pool
            http2ClientChannel.removeInFlightMessage(stream.id());
            activeStreams.decrementAndGet();
            http2ClientChannel.getDataEventListeners().
                    forEach(dataEventListener -> dataEventListener.onStreamClose(stream.id()));
            if (isExhausted.getAndSet(false)) {
                http2ConnectionManager.returnClientChannel(httpRoute, http2ClientChannel);
            }
        }
    }

    void removeFromConnectionPool() {
        http2ConnectionManager.removeClientChannel(httpRoute, this);
    }
}
