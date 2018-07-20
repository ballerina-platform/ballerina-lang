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

/**
 * {@code Http2Frame} is the base for all HTTP/2 frames.
 */
public abstract class Http2Frame {

    private int streamId;

    private boolean endOfStream;

    /**
     * Gets the id of the stream that the {@code Http2Frame} belongs to.
     *
     * @return id of the stream
     */
    public int getStreamId() {
        return streamId;
    }

    /**
     * Sets the id of the stream that the {@code Http2Frame} belongs to.
     *
     * @param streamId id of the stream
     */
    public void setStreamId(int streamId) {
        this.streamId = streamId;
    }

    /**
     * Checks whether this frame has the END_STREAM flag.
     *
     * @return {@code true} if the END_STREAM flag is set, else {@code false}
     */
    public boolean isEndOfStream() {
        return endOfStream;
    }

    /**
     * Sets whether this frame marks the end of the stream.
     *
     * @param endOfStream whether to set END_STREAM flag
     */
    public void setEndOfStream(boolean endOfStream) {
        this.endOfStream = endOfStream;
    }
}
