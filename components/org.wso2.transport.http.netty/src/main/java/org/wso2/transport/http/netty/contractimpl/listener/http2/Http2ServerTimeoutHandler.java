/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.contractimpl.listener.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2DataEventListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.wso2.transport.http.netty.contractimpl.common.Util.schedule;
import static org.wso2.transport.http.netty.contractimpl.common.Util.ticksInNanos;

/**
 * Timeout handler for HTTP/2 server. Timer applies to individual streams.
 */
public class Http2ServerTimeoutHandler implements Http2DataEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(Http2ServerTimeoutHandler.class);
    private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1);
    private long idleTimeNanos;
    private Http2ServerChannel http2ServerChannel;
    private Map<Integer, ScheduledFuture<?>> timerTasks;
    private ServerConnectorFuture serverConnectorFuture;

    Http2ServerTimeoutHandler(long idleTimeMills, Http2ServerChannel serverChannel,
                              ServerConnectorFuture serverConnectorFuture) {
        this.idleTimeNanos = Math.max(TimeUnit.MILLISECONDS.toNanos(idleTimeMills), MIN_TIMEOUT_NANOS);
        this.http2ServerChannel = serverChannel;
        this.serverConnectorFuture = serverConnectorFuture;
        timerTasks = new ConcurrentHashMap<>();
    }

    @Override
    public boolean onStreamInit(ChannelHandlerContext ctx, int streamId) {
        InboundMessageHolder inboundMsgHolder = http2ServerChannel.getInboundMessage(streamId);
        if (inboundMsgHolder != null) {
            inboundMsgHolder.setLastReadWriteTime(ticksInNanos());
            timerTasks.put(streamId,
                           schedule(ctx, new Http2ServerTimeoutHandler.IdleTimeoutTask(ctx, streamId), idleTimeNanos));
        }
        return true;
    }

    @Override
    public boolean onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, boolean endOfStream) {
        updateLastReadTime(streamId);
        return true;
    }

    @Override
    public boolean onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, boolean endOfStream) {
        updateLastReadTime(streamId);
        return true;
    }

    @Override
    public boolean onPushPromiseRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers,
                                     boolean endOfStream) {
        return true;
    }

    @Override
    public boolean onHeadersWrite(ChannelHandlerContext ctx, int streamId, Http2Headers headers, boolean endOfStream) {
        updateLastWriteTime(streamId, endOfStream);
        return true;
    }

    @Override
    public boolean onDataWrite(ChannelHandlerContext ctx, int streamId, ByteBuf data, boolean endOfStream) {
        updateLastWriteTime(streamId, endOfStream);
        return true;
    }

    @Override
    public void onStreamReset(int streamId) {
        onStreamClose(streamId);
    }

    @Override
    public void onStreamClose(int streamId) {
        ScheduledFuture timerTask = timerTasks.get(streamId);
        if (timerTask != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Server timer is removed for the stream : {}", streamId);
            }
            timerTask.cancel(false);
            timerTasks.remove(streamId);
        }
    }

    @Override
    public void destroy() {
        timerTasks.forEach((streamId, task) -> task.cancel(false));
        timerTasks.clear();
    }

    private class IdleTimeoutTask implements Runnable {
        private ChannelHandlerContext ctx;
        private int streamId;

        IdleTimeoutTask(ChannelHandlerContext ctx, int streamId) {
            this.ctx = ctx;
            this.streamId = streamId;
        }

        @Override
        public void run() {
            InboundMessageHolder msgHolder = http2ServerChannel.getInboundMessage(streamId);
            if (msgHolder != null) {
                runTimeOutLogic(msgHolder);
            }
        }

        private void runTimeOutLogic(InboundMessageHolder msgHolder) {
            long nextDelay = getNextDelay(msgHolder);
            if (nextDelay <= 0) {
                handleTimeout(msgHolder);
                closeStream(msgHolder, streamId, ctx);
            } else {
                // Read or write occurred before the timeout - set a new timeout with shorter delay.
                timerTasks.put(streamId, schedule(ctx, this, nextDelay));
            }
        }

        private long getNextDelay(InboundMessageHolder msgHolder) {
            return idleTimeNanos - (ticksInNanos() - msgHolder.getLastReadWriteTime());
        }

        private void handleTimeout(InboundMessageHolder msgHolder) {
            if (msgHolder.getInboundMsg() != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Timeout Occurred during {} state",
                              msgHolder.getInboundMsg().getHttp2MessageStateContext().getListenerState().toString());
                }
                msgHolder.getInboundMsg().getHttp2MessageStateContext().getListenerState()
                        .handleStreamTimeout(serverConnectorFuture, ctx, msgHolder.getHttp2OutboundRespListener(),
                                             streamId);
            }
        }

        private void closeStream(InboundMessageHolder msgHolder, int streamId, ChannelHandlerContext ctx) {
            try {
                msgHolder.getHttp2OutboundRespListener().resetStream(ctx, streamId, Http2Error.INTERNAL_ERROR);
                http2ServerChannel.getStreamIdRequestMap().remove(streamId);
            } catch (Http2Exception e) {
                LOG.error("Error sending RST_STREAM: ", e.getCause());
            }
        }
    }

    private void updateLastReadTime(int streamId) {
        InboundMessageHolder inboundMessage = http2ServerChannel.getInboundMessage(streamId);
        if (inboundMessage != null) {
            inboundMessage.setLastReadWriteTime(ticksInNanos());
        }
    }

    private void updateLastWriteTime(int streamId, boolean endOfStream) {
        InboundMessageHolder inboundMessage = http2ServerChannel.getInboundMessage(streamId);
        if (inboundMessage != null) {
            inboundMessage.setLastReadWriteTime(ticksInNanos());
        }
        if (endOfStream) {
            onStreamClose(streamId);
        }
    }
}
