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

package org.wso2.transport.http.netty.sender.http2;

import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

/**
 * {@code OutboundMsgHolder} holds data related to a single outbound invocation
 */
public class OutboundMsgHolder {

    // Outbound request HTTPCarbonMessage
    private HTTPCarbonMessage requestCarbonMessage;
    // Intended response received for the request
    private HTTPCarbonMessage responseCarbonMessage;
    // Future which is used to notify the response listener upon response receive
    private HttpResponseFuture responseFuture;

    /**
     * @param httpCarbonMessage outbound request message
     * @param responseFuture    the Future used to notify the response listener
     */
    public OutboundMsgHolder(HTTPCarbonMessage httpCarbonMessage,
                             HttpResponseFuture responseFuture) {
        this.requestCarbonMessage = httpCarbonMessage;
        this.responseFuture = responseFuture;
    }

    /**
     * Get Outbound request HTTPCarbonMessage
     *
     * @return request HTTPCarbonMessage
     */
    public HTTPCarbonMessage getRequest() {
        return requestCarbonMessage;
    }

    /**
     * Get the Future which is used to notify the response listener upon response receive
     *
     * @return the Future used to notify the response listener
     */
    public HttpResponseFuture getResponseFuture() {
        return responseFuture;
    }


    /**
     * Get the intended response received for the request
     *
     * @return response message
     */
    public HTTPCarbonMessage getResponse() {
        return responseCarbonMessage;
    }

    /**
     * Set the intended response for the request
     *
     * @param responseCarbonMessage response Carbon Message
     */
    void setResponseCarbonMessage(HTTPCarbonMessage responseCarbonMessage) {
        this.responseCarbonMessage = responseCarbonMessage;
    }
}
