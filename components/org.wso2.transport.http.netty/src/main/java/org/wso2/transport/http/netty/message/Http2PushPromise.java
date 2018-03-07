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

import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.wso2.transport.http.netty.common.Constants;

/**
 * {@code Http2PushPromise} represents a Htt2 Push Promise
 */
public class Http2PushPromise {

    private int streamId;
    private int promisedStreamId;
    private boolean rejected = false;
    private HttpRequest httpRequest;

    public Http2PushPromise(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    public Http2PushPromise(String method, String uri) {
        httpRequest = new DefaultHttpRequest(new HttpVersion(Constants.HTTP_VERSION_2_0, true),
                                             HttpMethod.valueOf(method), uri);
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public String getMethod() {
        return httpRequest.method().name();
    }

    public String getPath() {
        return httpRequest.uri();
    }

    public void addHeader(String name, String value) {
        httpRequest.headers().add(name, value);
    }

    public void removeHeader(String name) {
        httpRequest.headers().remove(name);
    }

    public String getHeader(String name) {
        return httpRequest.headers().get(name);
    }

    public int getPromisedStreamId() {
        return promisedStreamId;
    }

    public int getStreamId() {
        return streamId;
    }

    public void setStreamId(int streamId) {
        this.streamId = streamId;
    }

    public void setPromisedStreamId(int promisedStreamId) {
        this.promisedStreamId = promisedStreamId;
    }

    public boolean isRejected() {
        return rejected;
    }

    public void reject() {
        this.rejected = true;
    }
}
