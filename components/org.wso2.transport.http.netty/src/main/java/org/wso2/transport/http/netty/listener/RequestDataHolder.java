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

package org.wso2.transport.http.netty.listener;

import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

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

    public RequestDataHolder(HTTPCarbonMessage requestMessage) {
        acceptEncodingHeaderValue = requestMessage.getHeader(Constants.ACCEPT_ENCODING);
        connectionHeaderValue = requestMessage.getHeader(Constants.HTTP_CONNECTION);
        contentTypeHeaderValue = requestMessage.getHeader(Constants.HTTP_CONTENT_TYPE);
        transferEncodingHeaderValue = requestMessage.getHeader(Constants.HTTP_TRANSFER_ENCODING);
        contentLengthHeaderValue = requestMessage.getHeader(Constants.HTTP_CONTENT_LENGTH);
        httpMethod = (String) requestMessage.getProperty(Constants.HTTP_METHOD);
    }

    /**
     * Get the value of the Accept-Encoding header
     *
     * @return value of the Accept-Encoding header
     */
    public String getAcceptEncodingHeaderValue() {
        return acceptEncodingHeaderValue;
    }

    /**
     * Get the value of the Connection header
     *
     * @return value of the Connection header
     */
    public String getConnectionHeaderValue() {
        return connectionHeaderValue;
    }

    /**
     * Get the value of the Content-Type header
     *
     * @return value of the Content-Type header
     */
    public String getContentTypeHeaderValue() {
        return contentTypeHeaderValue;
    }

    /**
     * Get the value of the Transfer-Encoding header
     *
     * @return  value of the Transfer-Encoding header
     */
    public String getTransferEncodingHeaderValue() {
        return transferEncodingHeaderValue;
    }

    /**
     * Get the value of the Content-Length header
     *
     * @return value of the Content-Length header
     */
    public String getContentLengthHeaderValue() {
        return contentLengthHeaderValue;
    }

    /**
     * Get the http method
     *
     * @return http method
     */
    public String getHttpMethod() {
        return httpMethod;
    }
}
