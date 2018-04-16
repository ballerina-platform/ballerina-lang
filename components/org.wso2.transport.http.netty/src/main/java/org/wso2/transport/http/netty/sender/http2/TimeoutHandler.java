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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.exception.EndpointTimeOutException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * {@code TimeoutHandler} handles the Read/Write Timeout of HTTP/2 streams.
 */
public class TimeoutHandler  implements Http2DataEventListener {

    private static final Logger log = LoggerFactory.getLogger(TimeoutHandler.class);
    private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1);

    private long idleTimeNanos;
    private Http2ClientChannel http2ClientChannel;
    private Map<Integer, ScheduledFuture<?>> timerTasks;

    public TimeoutHandler(long idleTimeMills, Http2ClientChannel http2ClientChannel) {
        this.idleTimeNanos = Math.max(TimeUnit.MILLISECONDS.toNanos(idleTimeMills), MIN_TIMEOUT_NANOS);
        this.http2ClientChannel = http2ClientChannel;
        timerTasks = new ConcurrentHashMap<>();
    }

    @Override
    public boolean onStreamInit(int streamId, ChannelHandlerContext ctx) {
        OutboundMsgHolder outboundMsgHolder = http2ClientChannel.getInFlightMessage(streamId);
        if (outboundMsgHolder == null) {
            outboundMsgHolder = http2ClientChannel.getPromisedMessage(streamId);
        }
        if (outboundMsgHolder != null) {
            outboundMsgHolder.setLastReadWriteTime(ticksInNanos());
            timerTasks.put(streamId,
                           schedule(ctx, new IdleTimeoutTask(ctx, streamId), idleTimeNanos, TimeUnit.NANOSECONDS));
        }
        return true;
    }

    @Override
    public boolean onHeadersRead(int streamId, ChannelHandlerContext ctx, Http2Headers headers, boolean endOfStream) {
        updateReadWriteTime(streamId, endOfStream);
        return true;
    }

    @Override
    public boolean onDataRead(int streamId, ChannelHandlerContext ctx, ByteBuf data, boolean endOfStream) {
        updateReadWriteTime(streamId, endOfStream);
        return true;
    }

    @Override
    public boolean onDataWrite(int streamId, ChannelHandlerContext ctx, boolean endOfStream) {
        OutboundMsgHolder msgHolder = http2ClientChannel.getInFlightMessage(streamId);
        msgHolder.setLastReadWriteTime(ticksInNanos());
        return true;
    }

    @Override
    public boolean onStreamReset(int streamId) {
        onStreamClose(streamId);
        return true;
    }

    @Override
    public boolean onStreamClose(int streamId) {
        ScheduledFuture timerTask = timerTasks.get(streamId);
        if (timerTask != null) {
            timerTask.cancel(false);
            timerTasks.remove(streamId);
        }
        return true;
    }

    @Override
    public void destroy() {
        timerTasks.forEach((streamId, task) -> task.cancel(false));
        timerTasks.clear();
    }

    private void updateReadWriteTime(int streamId, boolean endOfStream) {
        OutboundMsgHolder outboundMsgHolder = http2ClientChannel.getInFlightMessage(streamId);
        if (outboundMsgHolder == null) {
            outboundMsgHolder = http2ClientChannel.getPromisedMessage(streamId);
        }
        if (outboundMsgHolder != null) {
            outboundMsgHolder.setLastReadWriteTime(ticksInNanos());
        }
        if (endOfStream) {
            onStreamClose(streamId);
        }
    }

    private class IdleTimeoutTask implements Runnable {

        private ChannelHandlerContext ctx;
        private OutboundMsgHolder msgHolder;
        private int streamId;

        public IdleTimeoutTask(ChannelHandlerContext ctx, int streamId) {
            this.ctx = ctx;
            this.streamId = streamId;
            this.msgHolder = http2ClientChannel.getInFlightMessage(streamId);
        }

        @Override
        public void run() {
            long nextDelay = idleTimeNanos - (ticksInNanos() - msgHolder.getLastReadWriteTime());
            if (nextDelay <= 0) {
                closeStream(streamId, ctx);
                if (msgHolder.getResponse() != null) {
                    handleIncompleteInboundResponse(Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_RESPONSE);
                } else if (msgHolder.isRequestWritten()) {
                    msgHolder.getResponseFuture().notifyHttpListener(
                            new EndpointTimeOutException(
                                    Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_READING_INBOUND_RESPONSE,
                                    HttpResponseStatus.GATEWAY_TIMEOUT.code()));
                } else {
                    msgHolder.getResponseFuture().notifyHttpListener(
                            new EndpointTimeOutException(
                                    Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_WRITING_OUTBOUND_RESPONSE,
                                    HttpResponseStatus.GATEWAY_TIMEOUT.code()));
                }
            } else {
                // Write occurred before the timeout - set a new timeout with shorter delay.
                timerTasks.put(streamId, schedule(ctx, this, nextDelay, TimeUnit.NANOSECONDS));
            }
        }

        private void handleIncompleteInboundResponse(String errorMessage) {
            LastHttpContent lastHttpContent = new DefaultLastHttpContent();
            lastHttpContent.setDecoderResult(DecoderResult.failure(new DecoderException(errorMessage)));
            msgHolder.getResponse().addHttpContent(lastHttpContent);
            log.warn(errorMessage);
        }
    }

    private long ticksInNanos() {
        return System.nanoTime();
    }

    private void closeStream(int streamId, ChannelHandlerContext ctx) {
        ClientOutboundHandler clientOutboundHandler =
                (ClientOutboundHandler) ctx.pipeline().get(Constants.OUTBOUND_HANDLER);
        clientOutboundHandler.resetStream(ctx, streamId, Http2Error.STREAM_CLOSED);
    }

    private ScheduledFuture<?> schedule(ChannelHandlerContext ctx, Runnable task, long delay, TimeUnit unit) {
        return ctx.executor().schedule(task, delay, unit);
    }
}
