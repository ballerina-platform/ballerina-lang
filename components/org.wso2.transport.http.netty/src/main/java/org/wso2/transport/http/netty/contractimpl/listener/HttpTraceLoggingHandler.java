/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.contractimpl.listener;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.SocketAddress;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import static io.netty.util.internal.StringUtil.NEWLINE;

/**
 * A custom LoggingHandler for the HTTP wire logs.
 */
public class HttpTraceLoggingHandler extends LoggingHandler {

    private static final LogLevel LOG_LEVEL = LogLevel.TRACE;
    private static final String EVENT_REGISTERED = "REGISTERED";
    private static final String EVENT_CONNECT = "CONNECT";
    private static final String EVENT_INBOUND = "INBOUND";
    private static final String EVENT_OUTBOUND = "OUTBOUND";
    private static final String ID_0X = "[id: 0x";

    private String correlatedSourceId;

    public HttpTraceLoggingHandler(LogLevel level) {
        super(level);
        correlatedSourceId = "n/a";
    }

    public HttpTraceLoggingHandler(Class<?> clazz) {
        super(clazz);
        correlatedSourceId = "n/a";
    }

    public HttpTraceLoggingHandler(Class<?> clazz, LogLevel level) {
        super(clazz, level);
        correlatedSourceId = "n/a";
    }

    public HttpTraceLoggingHandler(String name) {
        super(name, LOG_LEVEL);
        correlatedSourceId = "n/a";
    }

    public HttpTraceLoggingHandler(String name, LogLevel level) {
        super(name, level);
        correlatedSourceId = "n/a";
    }

    public void setCorrelatedSourceId(String correlatedSourceId) {
        this.correlatedSourceId = "0x" + correlatedSourceId;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, EVENT_INBOUND, msg));
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (logger.isEnabled(internalLevel)) {
            logger.log(internalLevel, format(ctx, EVENT_OUTBOUND, msg));
        }
        ctx.write(msg, promise);
    }

    @Override
    protected String format(ChannelHandlerContext ctx, String eventName, Object arg1, Object arg2) {
        String arg1Str = String.valueOf(arg1);
        String arg2Str = String.valueOf(arg2);
        StringBuilder stringBuilder = new StringBuilder(arg1Str.length() + 2 + arg2Str.length());

        return format(ctx, eventName, stringBuilder.append(arg1Str).append(", ").append(arg2Str));
    }

    @Override
    protected String format(ChannelHandlerContext ctx, String eventName) {
        String channelId = ctx.channel().id().asShortText();
        String socketInfo = buildSocketInfo(ctx.channel().localAddress(), ctx.channel().remoteAddress());

        StringBuilder stringBuilder = new StringBuilder(
                7 + channelId.length() + 14 + correlatedSourceId.length() + socketInfo.length() +
                        2 + eventName.length());

        if (EVENT_REGISTERED.equals(eventName) || EVENT_CONNECT.equals(eventName)) {
            return stringBuilder.append(ID_0X).append(channelId).append("] ").append(eventName).toString();
        } else {
            return stringBuilder.append(ID_0X).append(channelId).append(", correlatedSource: ")
                    .append(correlatedSourceId).append(socketInfo).append("] ").append(eventName).toString();
        }
    }

    @Override
    protected String format(ChannelHandlerContext ctx, String eventName, Object msg) {
        String channelId = ctx.channel().id().asShortText();
        String socketInfo = buildSocketInfo(ctx.channel().localAddress(), ctx.channel().remoteAddress());
        String msgStr;

        try {
            if (msg instanceof ByteBuf) {
                msgStr = formatPayload((ByteBuf) msg);
            } else if (msg instanceof ByteBufHolder) {
                msgStr = formatPayload((ByteBufHolder) msg);
            } else {
                msgStr = String.valueOf(msg);
            }
        } catch (CharacterCodingException e) {
            msgStr = "<< Payload could not be decoded >>";
        }

        StringBuilder stringBuilder = new StringBuilder(
                7 + channelId.length() + 14 + correlatedSourceId.length() + socketInfo.length() + 2 +
                        eventName.length() + 2 + msgStr.length());

        if (EVENT_REGISTERED.equals(eventName) || EVENT_CONNECT.equals(eventName)) {
            return stringBuilder.append(ID_0X).append(channelId).append("] ").append(eventName)
                    .append(": ").append(msgStr).toString();
        } else {
            return stringBuilder.append(ID_0X).append(channelId).append(", correlatedSource: ")
                    .append(correlatedSourceId).append(socketInfo).append("] ").append(eventName)
                    .append(": ").append(msgStr).toString();
        }
    }

    private static String formatPayload(ByteBuf msg) throws CharacterCodingException {
        int length = msg.readableBytes();
        if (length == 0) {
            return " 0B";
        } else {
            int rows = length / 16 + (length % 16 == 0 ? 0 : 1) + 4;
            StringBuilder stringBuilder = new StringBuilder(10 + 1 + 2 + rows * 80);

            stringBuilder.append(length).append('B').append(NEWLINE);
            appendPayload(stringBuilder, msg);
            
            return stringBuilder.toString();
        }
    }

    private static String formatPayload(ByteBufHolder msg) throws CharacterCodingException {
        String msgStr = msg.toString();
        ByteBuf content = msg.content();
        int length = content.readableBytes();
        if (length == 0) {
            return msgStr + ", 0B";
        } else {
            int rows = length / 16 + (length % 16 == 0 ? 0 : 1) + 4;
            StringBuilder stringBuilder = new StringBuilder(2 + msgStr.length() + 2 + 10 + 1 + 2 + rows * 80);

            stringBuilder.append(msgStr).append(", ").append(length).append('B').append(NEWLINE);
            appendPayload(stringBuilder, content);

            return stringBuilder.toString();
        }
    }

    private static String buildSocketInfo(SocketAddress local, SocketAddress remote) {
        StringBuilder stringBuilder = new StringBuilder();

        if (local != null) {
            stringBuilder.append(", host:").append(local.toString()).append(" - ");
        }
        if (remote != null) {
            stringBuilder.append("remote:").append(remote.toString());
        }

        return stringBuilder.toString();
    }

    private static void appendPayload(StringBuilder stringBuilder, ByteBuf content) throws CharacterCodingException {
        CharsetDecoder decoder = Charset.forName("UTF8").newDecoder();
        CharBuffer buffer = decoder.decode(content.nioBuffer());
        stringBuilder.append(buffer);
    }
}
