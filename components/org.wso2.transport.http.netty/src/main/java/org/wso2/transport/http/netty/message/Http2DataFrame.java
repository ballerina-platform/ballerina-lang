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

package org.wso2.transport.http.netty.message;

import io.netty.buffer.ByteBuf;

/**
 * {@code Http2DataFrame} represents an HTTP/2 data frame.
 */
public class Http2DataFrame extends Http2Frame {

    private ByteBuf data;

    /**
     * Constructs an {@code Http2DataFrame} with a given {@link ByteBuf} that holds data.
     *
     * @param streamId    id of the stream
     * @param data        the {@code ByteBuf} that holds data
     * @param endOfStream whether this frame marks the end of the stream
     */
    public Http2DataFrame(int streamId, ByteBuf data, boolean endOfStream) {
        setStreamId(streamId);
        setEndOfStream(endOfStream);
        this.data = data;
    }

    /**
     * Gets the {@code ByteBuf} that holds data.
     *
     * @return the {@code ByteBuf} that holds data
     */
    public ByteBuf getData() {
        return data;
    }
}
