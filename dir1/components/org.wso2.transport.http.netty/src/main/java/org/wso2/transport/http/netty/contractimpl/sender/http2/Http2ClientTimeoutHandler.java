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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.exceptions.EndpointTimeOutException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.wso2.transport.http.netty.contract.Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_PUSH_RESPONSE;
import static org.wso2.transport.http.netty.contract.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_RESPONSE_BODY;
import static org.wso2.transport.http.netty.contract.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_READING_PUSH_RESPONSE_BODY;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST_BODY;
import static org.wso2.transport.http.netty.contractimpl.common.Util.schedule;
import static org.wso2.transport.http.netty.contractimpl.common.Util.ticksInNanos;

/**
 * {@code Http2ClientTimeoutHandler} handles the Read/Write Timeout of HTTP/2 streams.
 */
public class Http2ClientTimeoutHandler implements Http2DataEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(Http2ClientTimeoutHandler.class);
    private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1);

    private long idleTimeNanos;
    private Http2ClientChannel http2ClientChannel;
    private Map<Integer, ScheduledFuture<?>> timerTasks;

    public Http2ClientTimeoutHandler(long idleTimeMills, Http2ClientChannel http2ClientChannel) {
        this.idleTimeNanos = Math.max(TimeUnit.MILLISECONDS.toNanos(idleTimeMills), MIN_TIMEOUT_NANOS);
        this.http2ClientChannel = http2ClientChannel;
        timerTasks = new ConcurrentHashMap<>();
    }

    @Override
    public boolean onStreamInit(ChannelHandlerContext ctx, int streamId) {
        OutboundMsgHolder outboundMsgHolder = http2ClientChannel.getInFlightMessage(streamId);
        if (outboundMsgHolder == null) {
            outboundMsgHolder = http2ClientChannel.getPromisedMessage(streamId);
        }
        setTimerTask(ctx, streamId, outboundMsgHolder);
        return true;
    }

    private void setTimerTask(ChannelHandlerContext ctx, int streamId, OutboundMsgHolder outboundMsgHolder) {
        if (outboundMsgHolder != null) {
            outboundMsgHolder.setLastReadWriteTime(ticksInNanos());
            timerTasks.put(streamId,
                           schedule(ctx, new IdleTimeoutTask(ctx, streamId, false), idleTimeNanos));
        }
    }

    public void createTimerTask(ChannelHandlerContext ctx, int streamId, long timeOut, boolean expectContinue) {
        this.idleTimeNanos = timeOut;
        timerTasks.put(streamId, schedule(ctx, new IdleTimeoutTask(ctx, streamId, expectContinue),
                TimeUnit.MILLISECONDS.toNanos(timeOut)));
    }

    @Override
    public boolean onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, boolean endOfStream) {
        updateLastReadTime(streamId, endOfStream);
        return true;
    }

    @Override
    public boolean onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, boolean endOfStream) {
        updateLastReadTime(streamId, endOfStream);
        return true;
    }

    @Override
    public boolean onPushPromiseRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers,
                                     boolean endOfStream) {
        updateLastReadTime(streamId, endOfStream);
        return true;
    }

    @Override
    public boolean onHeadersWrite(ChannelHandlerContext ctx, int streamId, Http2Headers headers, boolean endOfStream) {
        updateLastWriteTime(streamId);
        return true;
    }

    @Override
    public boolean onDataWrite(ChannelHandlerContext ctx, int streamId, ByteBuf data, boolean endOfStream) {
        updateLastWriteTime(streamId);
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
            timerTask.cancel(false);
            timerTasks.remove(streamId);
        }
    }

    @Override
    public void destroy() {
        timerTasks.forEach((streamId, task) -> task.cancel(false));
        timerTasks.clear();
    }

    private void updateLastReadTime(int streamId, boolean endOfStream) {
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

    private void updateLastWriteTime(int streamId) {
        OutboundMsgHolder msgHolder = http2ClientChannel.getInFlightMessage(streamId);
        if (msgHolder != null) {
            msgHolder.setLastReadWriteTime(ticksInNanos());
        } else {
            LOG.debug("OutboundMsgHolder may have already been removed for streamId: {}", streamId);
        }
    }

    /**
     * This class is for creating a IdleTimeoutTask.
     */
    public class IdleTimeoutTask implements Runnable {

        private ChannelHandlerContext ctx;
        private int streamId;
        private boolean expectContinue;

        IdleTimeoutTask(ChannelHandlerContext ctx, int streamId, boolean expectContinue) {
            this.ctx = ctx;
            this.streamId = streamId;
            this.expectContinue = expectContinue;
        }

        @Override
        public void run() {
            OutboundMsgHolder msgHolder = http2ClientChannel.getInFlightMessage(streamId);
            OutboundMsgHolder promiseHolder = http2ClientChannel.getPromisedMessage(streamId);

            if (msgHolder != null) {
                runTimeOutLogic(msgHolder, true);
            } else if (promiseHolder != null) {
                runTimeOutLogic(promiseHolder, false);
            }
        }

        private void runTimeOutLogic(OutboundMsgHolder msgHolder, boolean primary) {
            long nextDelay = getNextDelay(msgHolder);
            if (nextDelay <= 0) {
                if (!expectContinue) {
                    closeStream(streamId, ctx);
                }
                if (primary) {
                    handlePrimaryResponseTimeout(msgHolder);
                } else {
                    handlePushResponseTimeout(msgHolder);
                }
            } else {
                // Write occurred before the timeout - set a new timeout with shorter delay.
                timerTasks.put(streamId, schedule(ctx, this, nextDelay));
            }
        }

        private void handlePrimaryResponseTimeout(OutboundMsgHolder msgHolder) {
            if (msgHolder.getResponse() != null) {
                handleIncompleteResponse(msgHolder, true);
            } else {
                notifyTimeoutError(msgHolder, true);
            }
            if (!expectContinue) {
                http2ClientChannel.removeInFlightMessage(streamId);
            }
        }

        private void handlePushResponseTimeout(OutboundMsgHolder promiseHolder) {
            if (promiseHolder.getPushResponse(streamId) != null) {
                handleIncompleteResponse(promiseHolder, false);
            } else {
                notifyTimeoutError(promiseHolder, false);
            }
            http2ClientChannel.removePromisedMessage(streamId);
        }

        private void handleIncompleteResponse(OutboundMsgHolder msgHolder, boolean primary) {
            LastHttpContent lastHttpContent = new DefaultLastHttpContent();
            lastHttpContent.setDecoderResult(DecoderResult.failure(new DecoderException(getErrorMessage(primary))));
            msgHolder.getResponse().addHttpContent(lastHttpContent);
            LOG.warn(getErrorMessage(primary));
        }

        private String getErrorMessage(boolean primary) {
            return primary ? IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_RESPONSE_BODY :
                    IDLE_TIMEOUT_TRIGGERED_WHILE_READING_PUSH_RESPONSE_BODY;
        }

        private void closeStream(int streamId, ChannelHandlerContext ctx) {
            Http2TargetHandler clientOutboundHandler =
                    (Http2TargetHandler) ctx.pipeline().get(Constants.HTTP2_TARGET_HANDLER);
            clientOutboundHandler.resetStream(ctx, streamId, Http2Error.STREAM_CLOSED);
        }

        private void notifyTimeoutError(OutboundMsgHolder msgHolder, boolean primary) {
            if (primary) {
                try {
                    msgHolder.getRequest().getHttp2MessageStateContext().getSenderState()
                            .handleStreamTimeout(msgHolder, false, ctx, streamId);
                } catch (Http2Exception e) {
                    msgHolder.getResponseFuture().notifyHttpListener(new EndpointTimeOutException(
                            REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST_BODY,
                            HttpResponseStatus.GATEWAY_TIMEOUT.code()));
                }

            } else {
                msgHolder.getResponseFuture().notifyPushResponse(streamId, new EndpointTimeOutException(
                        IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_PUSH_RESPONSE,
                        HttpResponseStatus.GATEWAY_TIMEOUT.code()));
            }
        }

        private long getNextDelay(OutboundMsgHolder msgHolder) {
            return idleTimeNanos - (ticksInNanos() - msgHolder.getLastReadWriteTime());
        }
    }

    public Map<Integer, ScheduledFuture<?>> getTimerTasks() {
        return timerTasks;
    }
}
