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

package org.wso2.transport.http.netty.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.logging.LogLevel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import static io.netty.util.internal.StringUtil.NEWLINE;

/**
 * {@code FrameLogger} logs the HTTP/2 frames.
 */
public class FrameLogger extends Http2FrameLogger {

    private final InternalLogger logger;
    private LogLevel level;

    public FrameLogger(LogLevel level, String name) {
        super(level, name);
        logger = InternalLoggerFactory.getInstance(name);
        this.level = level;
    }

    public void logData(Http2FrameLogger.Direction direction, ChannelHandlerContext ctx, int streamId,
                        ByteBuf data, int padding, boolean endStream) {
        logger.log(level.toInternalLevel(), "{} {} DATA: streamId={} padding={} endStream={} length={} data={}",
                   ctx.channel(), direction.name(), streamId, padding, endStream, data.readableBytes(),
                   formatPayload(data));
    }

    private String formatPayload(ByteBuf msg) {
        int length = msg.readableBytes();
        if (length == 0) {
            return " 0B";
        } else {
            int rows = length / 16 + (length % 16 == 0 ? 0 : 1) + 4;
            StringBuilder stringBuilder = new StringBuilder(10 + 1 + 2 + rows * 80);
            stringBuilder.append(length).append('B').append(NEWLINE);
            try {
                appendPayload(stringBuilder, msg);
            } catch (CharacterCodingException e) {
                return "<< Payload could not be decoded >>";
            }
            return stringBuilder.toString();
        }
    }

    private void appendPayload(StringBuilder stringBuilder, ByteBuf content) throws CharacterCodingException {
        CharsetDecoder decoder = Charset.forName("UTF8").newDecoder();
        CharBuffer buffer = decoder.decode(content.nioBuffer());
        stringBuilder.append(buffer);
    }

}
