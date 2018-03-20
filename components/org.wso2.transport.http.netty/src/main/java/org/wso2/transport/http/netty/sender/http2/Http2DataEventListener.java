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

import io.netty.channel.ChannelHandlerContext;

/**
 * {@code Http2DataEventListener} represents a data listener for http2 data transfer events.
 */
public interface Http2DataEventListener {

    /**
     * Gets notified for an event on a stream initialization.
     *
     * @param streamId the related stream id
     * @param ctx      the channel handler context
     */
    void onStreamInit(int streamId, ChannelHandlerContext ctx);

    /**
     * Gets notified for an event on a data read on a particular stream.
     *
     * @param streamId    the related stream id
     * @param ctx         the channel handler context
     * @param endOfStream whether stream terminate with this data read operation
     */
    void onDataRead(int streamId, ChannelHandlerContext ctx, boolean endOfStream);

    /**
     * Gets notified for an event on a data write on a particular stream.
     *
     * @param streamId    the related stream id
     * @param ctx         the channel handler context
     * @param endOfStream whether stream terminate with this data read operation
     */
    void onDataWrite(int streamId, ChannelHandlerContext ctx, boolean endOfStream);

    /**
     * Gets notified on  a stream reset.
     *
     * @param streamId the stream id
     */
    void onStreamReset(int streamId);

    /**
     * Gets notified on a stream close.
     *
     * @param streamId the related stream id
     */
    void onStreamClose(int streamId);

    /**
     * Destroy this {@code Http2DataEventListener}.
     */
    void destroy();
}
