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
 * {@code Http2PushPromise} represents a Htt2 Push Promise
 */
public class Http2PushPromise {

    int streamId;
    int promisedStreamId;
    private Http2Headers headers;

    public Http2PushPromise(Http2Headers headers) {
        this.headers = headers;
    }

    public int getPromisedStreamId() {
        return promisedStreamId;
    }

    public int getStreamId() {
        return streamId;
    }

    public Http2Headers getHeaders() {
        return headers;
    }

    public void setStreamId(int streamId) {
        this.streamId = streamId;
    }

    public void setPromisedStreamId(int promisedStreamId) {
        this.promisedStreamId = promisedStreamId;
    }
}
