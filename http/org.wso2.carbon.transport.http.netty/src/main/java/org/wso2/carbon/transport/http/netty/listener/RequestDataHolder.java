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

package org.wso2.carbon.transport.http.netty.listener;

import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;

/**
 * {@code RequestDataHolder} holds data fields of the request which might be useful later in the message flow.
 * ex. at the point where response is send back to the client.
 */
public class RequestDataHolder {

    private String acceptEncodingHeader;
    private String connectionHeader;
    private String contentType;

    public RequestDataHolder(CarbonMessage requestMessage) {
        acceptEncodingHeader = requestMessage.getHeader(Constants.ACCEPT_ENCODING);
        connectionHeader = requestMessage.getHeader(Constants.HTTP_CONNECTION);
        contentType = requestMessage.getHeader(Constants.HTTP_CONTENT_TYPE);
    }

    /**
     * Get the value of the Accept-Encoding header
     *
     * @return value of the Accept-Encoding header
     */
    public String getAcceptEncodingHeader() {
        return acceptEncodingHeader;
    }

    /**
     * Get the value of the Connection header
     *
     * @return value of the Connection header
     */
    public String getConnectionHeader() {
        return connectionHeader;
    }

    /**
     * Get the value of the Content-Type header
     *
     * @return value of the Content-Type header
     */
    public String getContentType() {
        return contentType;
    }
}
