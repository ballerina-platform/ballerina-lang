/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.transport.http.netty.util.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import java.net.URI;

/**
 * A class which represents the request details
 */
public class Request {

    private HttpVersion httpVersion = HttpVersion.HTTP_1_1;
    private HttpMethod httpMethod = HttpMethod.GET;

    private URI uri;

    private String message;
    private String contentType;

    private String connection = HttpHeaders.Values.KEEP_ALIVE;
    private String acceptEncoding = HttpHeaders.Values.GZIP;
    private int contentLength;
    private String host;

    private ByteBuf content;

    public Request(HttpVersion httpVersion, HttpMethod httpMethod, URI uri) {
        this.httpVersion = httpVersion;
        this.httpMethod = httpMethod;
        this.uri = uri;
        host = uri.getHost() == null ? "localhost" : uri.getHost();
    }

    public void setMessageBody(String message, String contentType) {
        this.message = message;
        content = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer(message, CharsetUtil.US_ASCII));
        this.contentType = contentType;
        this.contentLength = content.readableBytes();
    }

    private void addHeaders(HttpRequest httpRequest) {
        httpRequest.headers().set(HttpHeaders.Names.CONNECTION, connection);
        if (message != null) {
            httpRequest.headers().set(HttpHeaders.Names.CONTENT_LENGTH, contentLength);
            httpRequest.headers().set(HttpHeaders.Names.CONTENT_TYPE, contentType);
        }
        httpRequest.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, acceptEncoding);
        httpRequest.headers().set(HttpHeaders.Names.HOST, host);
    }

    public void setAcceptEncoding(String acceptEncoding) {
        this.acceptEncoding = acceptEncoding;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public void setHttpVersion(HttpVersion httpVersion) {
        this.httpVersion = httpVersion;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public HttpRequest getHTTPRequest() {
        DefaultFullHttpRequest request = null;
        if (message != null) {
            request = new DefaultFullHttpRequest(httpVersion, httpMethod, uri.getRawPath(), content);
        } else {
            request = new DefaultFullHttpRequest(httpVersion, httpMethod, uri.getRawPath());
        }
        addHeaders(request);

        return request;
    }

    public URI getUri() {
        return uri;
    }
}
