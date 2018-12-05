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
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contractimpl.sender.http2.OutboundMsgHolder;

import java.util.List;

/**
 * {@code Http2PushPromise} represents a HTTP/2 PUSH_PROMISE frame.
 * <p>
 * This represents both HTTP/2 push promises received from a server as well as the push promises
 * suppose to be delivered to a client.
 * A PUSH_PROMISE should not be sent by a client.
 */
public class Http2PushPromise extends Http2Frame {

    private int promisedStreamId;
    private boolean rejected = false;
    private HttpRequest httpRequest;
    private OutboundMsgHolder outboundMsgHolder;

    /**
     * Constructor to create {@code Http2PushPromise} with initial {@code HttpRequest}.
     *
     * @param httpRequest the HttpRequest
     */
    public Http2PushPromise(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    /**
     * Constructor to create a new {@code Http2PushPromise} with uri and http method.
     *
     * @param method the http method
     * @param uri    the uri
     */
    public Http2PushPromise(String method, String uri) {
        httpRequest = new DefaultHttpRequest(new HttpVersion(Constants.HTTP_VERSION_2_0, true),
                HttpMethod.valueOf(method), uri);
    }

    /**
     * Gets the associated {@code HttpRequest} of the push promise.
     *
     * @return the associated {@code HttpRequest}
     */
    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    /**
     * Gets the http method associated with the push promise.
     *
     * @return the http method
     */
    public String getMethod() {
        return httpRequest.method().name();
    }

    /**
     * Gets the path associated with the push promise.
     *
     * @return the path
     */
    public String getPath() {
        return httpRequest.uri();
    }

    /**
     * Adds a header to the push promise.
     * This will not replace the value of an already exists header.
     *
     * @param name  the header name
     * @param value value of the header
     */
    public void addHeader(String name, String value) {
        httpRequest.headers().add(name, value);
    }

    /**
     * Remove all headers in for a given header name.
     *
     * @param name name of the header
     */
    public void removeHeader(String name) {
        httpRequest.headers().remove(name);
    }

    /**
     * Gets a header value for a given header name.
     * In case if a given name has multiple headers, the first one is returned.
     *
     * @param name name of the header
     * @return the value of the header
     */
    public String getHeader(String name) {
        return httpRequest.headers().get(name);
    }

    /**
     * Gets all header values for a given header name.
     *
     * @param name name of the header
     * @return array of header values
     */
    public String[] getHeaders(String name) {
        List<String> headerList = httpRequest.headers().getAll(name);
        return headerList.toArray(new String[0]);
    }

    /**
     * Sets a header value for a given header name.
     * This replaces the value of an already existing header.
     *
     * @param name  name of the header
     * @param value value of the header
     */
    public void setHeader(String name, String value) {
        httpRequest.headers().set(name, value);
    }

    /**
     * Removes all headers from the push promise.
     */
    public void removeAllHeaders() {
        httpRequest.headers().clear();
    }

    /**
     * Gets the promised stream id of the push promise.
     *
     * @return the promised stream id
     */
    public int getPromisedStreamId() {
        return promisedStreamId;
    }

    /**
     * Sets the promised stream id of the push promise.
     *
     * @param promisedStreamId promised stream id
     */
    public void setPromisedStreamId(int promisedStreamId) {
        this.promisedStreamId = promisedStreamId;
    }

    /**
     * Checks whether push promise is rejected.
     *
     * @return whether push promise is rejected
     */
    public boolean isRejected() {
        return rejected;
    }

    /**
     * Rejects the push promise.
     */
    public void reject() {
        this.rejected = true;
    }

    /**
     * Gets the {@code OutboundMsgHolder} associated with this {@code Http2PushPromise}.
     *
     * @return the {@code OutboundMsgHolder} associated with this {@code Http2PushPromise}
     */
    public OutboundMsgHolder getOutboundMsgHolder() {
        return outboundMsgHolder;
    }

    /**
     * Sets the {@code OutboundMsgHolder} associated with {@code Http2PushPromise}.
     *
     * @param outboundMsgHolder the {@code OutboundMsgHolder} associated with this {@code Http2PushPromise}
     */
    public void setOutboundMsgHolder(OutboundMsgHolder outboundMsgHolder) {
        this.outboundMsgHolder = outboundMsgHolder;
    }
}
