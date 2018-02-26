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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@code TargetChannel} encapsulate the Channel associate with a particular connection.
 * <p>
 * This shouldn't have anything related to a single message.
 */
public class TargetChannel {

    private static ConcurrentHashMap<Integer, OutboundMsgHolder> inFlightMessages = new ConcurrentHashMap<>();
    private Channel channel;
    private Http2Connection connection;
    private ChannelFuture channelFuture;
    private UpgradeState upgradeState = UpgradeState.UPGRADE_NOT_ISSUED;
    // List which holds the pending message during the connection upgrade
    private ConcurrentLinkedQueue<OutboundMsgHolder> pendingMessages;
    private HttpRoute httpRoute;
    private ConnectionManager connectionManager = ConnectionManager.getInstance();

    // Whether channel is operates with maximum number of allowed streams
    private AtomicBoolean isExhausted = new AtomicBoolean(false);

    // Number of active streams. Need to start from 1 to prevent someone stealing the connection from the creator
    private AtomicInteger activeStreams = new AtomicInteger(1);

    public TargetChannel(Http2Connection connection, HttpRoute httpRoute, ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
        channel = channelFuture.channel();
        this.connection = connection;
        this.httpRoute = httpRoute;
        pendingMessages = new ConcurrentLinkedQueue<>();
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
     * Store a message until connection upgrade is done.
     * When a connection upgrade is in progress, subsequent messages should kept on-hold until the process is over
     *
     * @param pendingMessage a message to be hold until connection upgrade is done
     */
    void addPendingMessage(OutboundMsgHolder pendingMessage) {
        pendingMessages.add(pendingMessage);
    }

    /**
     * Get all messages was on-hold due to the connection upgrade process
     *
     * @return queue of pending messages to be delivered to the backend service
     */
    ConcurrentLinkedQueue<OutboundMsgHolder> getPendingMessages() {
        return pendingMessages;
    }

    /**
     * Update the connection upgrade status
     *
     * @param upgradeState status of the connection upgrade
     */
    void updateUpgradeState(UpgradeState upgradeState) {
        this.upgradeState = upgradeState;
    }

    /**
     * Get the current state of the connection upgrade process
     *
     * @return state of the connection upgrade process
     */
    UpgradeState getUpgradeState() {
        return upgradeState;
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
     * Lifecycle states of the Target Channel related to connection upgrade
     */
    public enum UpgradeState {
        UPGRADE_NOT_ISSUED, UPGRADE_ISSUED, UPGRADED
    }

    /**
     * Listener which listen to the stream closure event
     */
    private class StreamCloseListener extends Http2EventAdapter {

        private TargetChannel targetChannel;

        public StreamCloseListener(TargetChannel targetChannel) {
            this.targetChannel = targetChannel;
        }

        public void onStreamClosed(Http2Stream stream) {
            // TargetChannel is no longer exhausted, so we can return it back to the pool
            activeStreams.decrementAndGet();
            if (isExhausted.getAndSet(false)) {
                connectionManager.returnTargetChannel(httpRoute, targetChannel);
            }
        }
    }

}
