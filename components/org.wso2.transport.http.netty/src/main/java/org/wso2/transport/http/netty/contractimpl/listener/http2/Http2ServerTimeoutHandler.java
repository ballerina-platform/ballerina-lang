package org.wso2.transport.http.netty.contractimpl.listener.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2DataEventListener;
import org.wso2.transport.http.netty.contractimpl.sender.http2.OutboundMsgHolder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.wso2.transport.http.netty.contractimpl.common.Util.schedule;
import static org.wso2.transport.http.netty.contractimpl.common.Util.ticksInNanos;

public class Http2ServerTimeoutHandler implements Http2DataEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(Http2ServerTimeoutHandler.class);
    private static final long MIN_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(1);
    private long idleTimeNanos;
    private Http2ServerChannel http2ServerChannel;
    private Map<Integer, ScheduledFuture<?>> timerTasks;

    public Http2ServerTimeoutHandler(long idleTimeMills, Http2ServerChannel serverChannel) {
        this.idleTimeNanos = Math.max(TimeUnit.MILLISECONDS.toNanos(idleTimeMills), MIN_TIMEOUT_NANOS);
        this.http2ServerChannel = serverChannel;
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
        updateLastReadTime(streamId, endOfStream);
        return true;
    }

    @Override
    public boolean onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, boolean endOfStream) {
        return false;
    }

    @Override
    public boolean onPushPromiseRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers,
                                     boolean endOfStream) {
        return false;
    }

    @Override
    public boolean onHeadersWrite(ChannelHandlerContext ctx, int streamId, Http2Headers headers, boolean endOfStream) {

        return true;
    }

    @Override
    public boolean onDataWrite(ChannelHandlerContext ctx, int streamId, ByteBuf data, boolean endOfStream) {

        return true;
    }

    @Override
    public void onStreamReset(int streamId) {

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
//                closeStream(streamId, ctx);
                handlePrimaryResponseTimeout(msgHolder);
            } else {
                // Write occurred before the timeout - set a new timeout with shorter delay.
                timerTasks.put(streamId, schedule(ctx, this, nextDelay));
            }
        }

        private long getNextDelay(InboundMessageHolder msgHolder) {
            return idleTimeNanos - (ticksInNanos() - msgHolder.getLastReadWriteTime());
        }

        private void handlePrimaryResponseTimeout(InboundMessageHolder msgHolder) {
//            if (msgHolder.getResponse() != null) {
//                handleIncompleteResponse(msgHolder, true);
//            } else {
//                notifyTimeoutError(msgHolder, true);
//            }
//            http2ServerChannel.removeInFlightMessage(streamId);
        }

    }

    private void updateLastReadTime(int streamId, boolean endOfStream) {
        InboundMessageHolder inboundMessage = http2ServerChannel.getInboundMessage(streamId);
//        if (outboundMsgHolder == null) {
//            outboundMsgHolder = http2ClientChannel.getPromisedMessage(streamId);
//        }
        if (inboundMessage != null) {
            inboundMessage.setLastReadWriteTime(ticksInNanos());
        }
        if (endOfStream) {
            onStreamClose(streamId);
        }
    }
}
