/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.contractimpl.listener;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * {@code RequestDataHolder} holds data fields of the request which might be useful later in the message flow.
 * ex. at the point where response is send back to the client.
 */
public class RequestDataHolder {

    private String acceptEncodingHeaderValue;
    private String connectionHeaderValue;
    private String contentTypeHeaderValue;
    private String transferEncodingHeaderValue;
    private String contentLengthHeaderValue;
    private String httpMethod;
    private String httpVersion;

    public RequestDataHolder(HttpCarbonMessage requestMessage) {
        acceptEncodingHeaderValue = requestMessage.getHeader(HttpHeaderNames.ACCEPT_ENCODING.toString());
        connectionHeaderValue = requestMessage.getHeader(HttpHeaderNames.CONNECTION.toString());
        contentTypeHeaderValue = requestMessage.getHeader(HttpHeaderNames.CONTENT_TYPE.toString());
        transferEncodingHeaderValue = requestMessage.getHeader(HttpHeaderNames.TRANSFER_ENCODING.toString());
        contentLengthHeaderValue = requestMessage.getHeader(HttpHeaderNames.CONTENT_LENGTH.toString());
        httpMethod = requestMessage.getHttpMethod();
        httpVersion = requestMessage.getHttpVersion();
    }

    /**
     * Get the value of the Accept-Encoding header.
     *
     * @return value of the Accept-Encoding header
     */
    public String getAcceptEncodingHeaderValue() {
        return acceptEncodingHeaderValue;
    }

    /**
     * Get the value of the Connection header.
     *
     * @return value of the Connection header
     */
    public String getConnectionHeaderValue() {
        return connectionHeaderValue;
    }

    /**
     * Get the value of the Content-Type header.
     *
     * @return value of the Content-Type header
     */
    public String getContentTypeHeaderValue() {
        return contentTypeHeaderValue;
    }

    /**
     * Get the value of the Transfer-Encoding header.
     *
     * @return  value of the Transfer-Encoding header
     */
    public String getTransferEncodingHeaderValue() {
        return transferEncodingHeaderValue;
    }

    /**
     * Get the value of the Content-Length header.
     *
     * @return value of the Content-Length header
     */
    public String getContentLengthHeaderValue() {
        return contentLengthHeaderValue;
    }

    /**
     * Get the http method.
     *
     * @return http method
     */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * Get the http version.
     *
     * @return http version
     */
    public String getHttpVersion() {
        return httpVersion;
    }
}
