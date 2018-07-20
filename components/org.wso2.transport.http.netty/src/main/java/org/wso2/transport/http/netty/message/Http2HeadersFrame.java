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

import io.netty.handler.codec.http2.Http2Headers;

/**
 * {@code Http2Headers} represents an HTTP2 header frame.
 */
public class Http2HeadersFrame extends Http2Frame {

    private Http2Headers headers;

    /**
     * Constructs an HTTP/2 header frame with the given HTTP/2 headers.
     *
     * @param streamId    id of the stream
     * @param headers     the http2 headers
     * @param endOfStream whether this frame marks the end of the stream
     */
    public Http2HeadersFrame(int streamId, Http2Headers headers, boolean endOfStream) {
        setStreamId(streamId);
        setEndOfStream(endOfStream);
        this.headers = headers;
    }

    /**
     * Gets the {@code Http2Headers} belongs to this header frame.
     *
     * @return the {@code Http2Headers} belongs to this header frame
     */
    public Http2Headers getHeaders() {
        return headers;
    }
}
