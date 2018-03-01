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

import org.wso2.transport.http.netty.contract.Http2ResponseFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.Http2Response;

/**
 * {@code OutboundMsgHolder} holds data related to a single outbound invocation
 */
public class OutboundMsgHolder {

    // Outbound request HTTPCarbonMessage
    private HTTPCarbonMessage requestCarbonMessage;
    // Intended response received for the request
    private Http2Response http2Response;
    // Future which is used to notify the response listener upon response receive
    private Http2ResponseFuture responseFuture;
    // Whether the response listener is notified
    private boolean responseListenerNotified = false;

    /**
     * @param httpCarbonMessage outbound request message
     * @param responseFuture    the Future used to notify the response listener
     */
    public OutboundMsgHolder(HTTPCarbonMessage httpCarbonMessage,
                             Http2ResponseFuture responseFuture) {
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
    public Http2ResponseFuture getResponseFuture() {
        return responseFuture;
    }


    /**
     * Get the intended response received for the request
     *
     * @return response message
     */
    public Http2Response getResponse() {
        return http2Response;
    }

    /**
     * Set the intended response for the request
     *
     * @param http2Response response Carbon Message
     */
    void setHttp2Response(Http2Response http2Response) {
        this.http2Response = http2Response;
    }

    /**
     * Check whether response listener has been notified
     *
     * @return whether response listener has been notified
     */
    public boolean isResponseListenerNotified() {
        return responseListenerNotified;
    }

    /**
     * Set whether response listener is notified
     *
     * @param responseListenerNotified  whether response listener is notified
     */
    public void setResponseListenerNotified(boolean responseListenerNotified) {
        this.responseListenerNotified = responseListenerNotified;
    }
}
