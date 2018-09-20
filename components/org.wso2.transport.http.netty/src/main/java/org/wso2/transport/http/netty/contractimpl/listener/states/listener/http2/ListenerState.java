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

package org.wso2.transport.http.netty.contractimpl.listener.states.listener.http2;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http2.Http2Exception;
import org.wso2.transport.http.netty.contractimpl.Http2OutboundRespListener;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * ListenerStates of HTTP/2 source handler.
 */
public interface ListenerState {

    /**
     * Read headers of inbound request.
     *
     * @param headersFrame inbound header frame
     * @throws Http2Exception if an error occurs while reading
     */
    void readInboundRequestHeaders(Http2HeadersFrame headersFrame) throws Http2Exception;

    /**
     * Read entity body of inbound request.
     *
     * @param dataFrame inbound data frame
     * @throws Http2Exception if an error occurs while reading
     */
    void readInboundRequestBody(Http2DataFrame dataFrame) throws Http2Exception;

    /**
     * Write headers of outbound response.
     *
     * @param http2OutboundRespListener outbound response listener of response future
     * @param outboundResponseMsg       outbound response message
     * @param httpContent               the initial content of the entity body
     * @param streamId                  the current stream id
     * @throws Http2Exception if an error occurs while writing
     */
    void writeOutboundResponseHeaders(Http2OutboundRespListener http2OutboundRespListener,
                                      HttpCarbonMessage outboundResponseMsg, HttpContent httpContent, int streamId)
            throws Http2Exception;

    /**
     * Write entity body of outbound response.
     *
     * @param http2OutboundRespListener outbound response listener of response future
     * @param outboundResponseMsg       outbound response message
     * @param httpContent               the content of the entity body
     * @param streamId                  the current stream id
     * @throws Http2Exception if an error occurs while writing
     */
    void writeOutboundResponseBody(Http2OutboundRespListener http2OutboundRespListener,
                                   HttpCarbonMessage outboundResponseMsg, HttpContent httpContent, int streamId)
            throws Http2Exception;

    /**
     * Write the outbound promise message.
     *
     * @param http2OutboundRespListener outbound response listener of response future
     * @param pushPromise               outbound promise message
     * @throws Http2Exception if an error occurs while writing
     */
    void writeOutboundPromise(Http2OutboundRespListener http2OutboundRespListener, Http2PushPromise pushPromise)
            throws Http2Exception;
}
