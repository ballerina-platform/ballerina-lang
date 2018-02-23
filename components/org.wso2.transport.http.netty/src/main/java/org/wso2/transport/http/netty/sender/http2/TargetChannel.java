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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class encapsulate the Channel associate with a particular connection.
 * This shouldn't have anything related to a single message.
 */
public class TargetChannel {

    private static ConcurrentHashMap<Integer, OutboundHttp2MessageHolder> inFlightMessages = new ConcurrentHashMap<>();
    private Channel channel;
    private Http2ClientHandler clientHandler;
    private Http2ClientInitializer http2ClientInitializer;
    private Http2Connection connection;
    private ChannelFuture channelFuture;
    private UpgradeState upgradeState = UpgradeState.UPGRADE_NOT_ISSUED;
    /* List which holds the pending message during the connection upgrade */
    private ConcurrentLinkedQueue<OutboundHttp2MessageHolder> pendingMessages;


    public TargetChannel(Http2ClientInitializer http2ClientInitializer, ChannelFuture channelFuture) {
        this.http2ClientInitializer = http2ClientInitializer;
        this.channelFuture = channelFuture;
        channel = channelFuture.channel();
        connection = http2ClientInitializer.getHttp2ClientHandler().getConnection();
        pendingMessages = new ConcurrentLinkedQueue<>();
    }

    public Channel getChannel() {
        return channel;
    }

    public Http2ClientHandler getClientHandler() {
        return clientHandler;
    }

    public void setClientHandler(Http2ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    public Http2ClientInitializer getHttp2ClientInitializer() {
        return http2ClientInitializer;
    }

    public Http2Connection getConnection() {
        return connection;
    }

    public void setConnection(Http2Connection connection) {
        this.connection = connection;
    }

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public void putInFlightMessage(int streamId, OutboundHttp2MessageHolder inFlightMessage) {
        inFlightMessages.put(streamId, inFlightMessage);
    }

    public void addPendingMessage(OutboundHttp2MessageHolder pendingMessage) {
        pendingMessages.add(pendingMessage);
    }

    public ConcurrentLinkedQueue<OutboundHttp2MessageHolder> getPendingMessages() {
        return pendingMessages;
    }

    public OutboundHttp2MessageHolder getInFlightMessage(int streamId) {
        return inFlightMessages.get(streamId);
    }

    /**
     * Lifecycle states of the Target Channel related to connection upgrade
     */
    public enum UpgradeState {
        UPGRADE_NOT_ISSUED, UPGRADE_ISSUED, UPGRADED
    }

    public void updateUpgradeState(UpgradeState upgradeState) {
        this.upgradeState = upgradeState;
    }

    public UpgradeState getUpgradeState() {
        return upgradeState;
    }
}
