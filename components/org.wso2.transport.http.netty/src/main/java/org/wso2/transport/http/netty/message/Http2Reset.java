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

import io.netty.handler.codec.http2.Http2Error;

/**
 * {@code Http2Reset} represents a HTTP/2 RST_STREAM frame.
 */
public class Http2Reset extends Http2Frame {

    private Http2Error error;

    /**
     * Constructor to create a {@code Http2Reset} with stream id need to reset and the error.
     *
     * @param streamId id of the stream need to be reset
     * @param error    error to be written as the cause for reset
     */
    public Http2Reset(int streamId, Http2Error error) {
        setStreamId(streamId);
        this.error = error;
    }

    /**
     * Constructor to create {@code Http2Reset} with stream id.
     * REFUSED_STREAM is used as the error code.
     *
     * @param streamId id of the stream need to be reset
     */
    public Http2Reset(int streamId) {
        setStreamId(streamId);
        this.error = Http2Error.REFUSED_STREAM;
    }

    /**
     * Gets the cause of the stream reset.
     *
     * @return the cause of the stream reset
     */
    public Http2Error getError() {
        return error;
    }
}
