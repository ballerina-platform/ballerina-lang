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

package org.wso2.transport.http.netty.internal;

import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * Interface for MessagingHandler.
 */
public interface MessagingHandler {

    /**
     * Invoked when source connection is initiated.
     *
     * @param metadata unique string key to identify the connection
     */
    void invokeAtSourceConnectionInitiation(String metadata);

    /**
     * Invoked when source connection is terminated.
     *
     * @param metadata unique string key to identify the connection
     */
    void invokeAtSourceConnectionTermination(String metadata);

    /**
     * Invoked when target connection is initiated.
     *
     * @param metadata unique string key to identify the connection
     */
    void invokeAtTargetConnectionInitiation(String metadata);

    /**
     * Invoked when target connection is terminated.
     *
     * @param metadata unique string key to identify the connection
     */
    void invokeAtTargetConnectionTermination(String metadata);

    /**
     * Invoked when source request is started receiving at source handler.
     *
     * @param carbonMessage newly created carbon message.
     */
    void invokeAtSourceRequestReceiving(HttpCarbonMessage carbonMessage);

    /**
     * Invoked when source request is started sending to the message processor.
     *
     * @param carbonMessage client request (i.e headers, property and message body)
     */
    void invokeAtSourceRequestSending(HttpCarbonMessage carbonMessage);

    /**
     * Invoked when the request is received again to the transport level after being processed at message processor.
     *
     * @param carbonMessage processed (or mediated) request (i.e headers, properties and message body)
     */
    void invokeAtTargetRequestReceiving(HttpCarbonMessage carbonMessage);

    /**
     * Invoked when the request is started sending to the backend.
     *
     * @param carbonMessage sent request (i.e the message is already had started to send to backend)
     *                      So no message body will be available. Even though the headers and properties are available,
     *                      manipulating them won't change the request send to the back end (because the headers are
     *                      already been send to the backend)
     */
    void invokeAtTargetRequestSending(HttpCarbonMessage carbonMessage);

    /**
     * Invoked when target response is started receiving at target handler.
     *
     * @param carbonMessage newly created carbon message.
     */
    void invokeAtTargetResponseReceiving(HttpCarbonMessage carbonMessage);

    /**
     * Invoked when target response is started sending to the message processor.
     *
     * @param carbonMessage target response (i.e headers, property and message body)
     */
    void invokeAtTargetResponseSending(HttpCarbonMessage carbonMessage);

    /**
     * Invoked when the response is received again to the transport level after being processed at message processor.
     *
     * @param carbonMessage processed (or mediated) response (i.e headers, properties and message body)
     */
    void invokeAtSourceResponseReceiving(HttpCarbonMessage carbonMessage);

    /**
     * Invoked when the response is started sending to the client.
     *
     * @param carbonMessage sent response (i.e with empty message body.
     *                      similar carbon message to}
     */
    void invokeAtSourceResponseSending(HttpCarbonMessage carbonMessage);

    /**
     * Gives handler name.
     *
     * @return handler name
     */
    String handlerName();
}
