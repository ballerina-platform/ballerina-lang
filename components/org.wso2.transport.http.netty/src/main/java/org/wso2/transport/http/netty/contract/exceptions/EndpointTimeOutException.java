/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.contract.exceptions;

/**
 * A class that represent EndpointTimeout Exception.
 */
public class EndpointTimeOutException extends ClientConnectorException {

    /**
     * Constructs a new EndpointTimeOutException with the specified outboundChannelID and detail message.
     *
     * @param outboundChannelID  the unique identifier of out bound channel.
     * @param message the detail message.
     */
    public EndpointTimeOutException(String outboundChannelID, String message) {
        super(outboundChannelID, message);
    }

    /**
     * Constructs a new EndpointTimeOutException with the specified detail message and HTTP Status code.
     *
     * @param message the detail message.
     * @param httpStatusCode HTTP status code to be set to the EndpointTimeOutException.
     */
    public EndpointTimeOutException(String message, int httpStatusCode) {
        super(message, httpStatusCode);
    }

    /**
     * Constructs a new EndpointTimeOutException with the specified outboundChannelID and detail message and
     * HTTP Status code.
     *
     * @param outboundChannelID the unique identifier of out bound channel.
     * @param message the detail message.
     * @param httpStatusCode HTTP status code to be set to the EndpointTimeOutException.
     */
    public EndpointTimeOutException(String outboundChannelID, String message, int httpStatusCode) {
        super(outboundChannelID, message, httpStatusCode);
    }

}
