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

package org.wso2.transport.http.netty.contractimpl.listener.states.http2;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http2.Http2Exception;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.Http2OutboundRespListener;
import org.wso2.transport.http.netty.contractimpl.listener.http2.Http2SourceHandler;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * Listener states of HTTP/2 source handler.
 *
 * @since 6.0.241
 */
public interface ListenerState {

    /**
     * Reads headers of inbound request.
     *
     * @param ctx          channel handler context
     * @param headersFrame inbound header frame
     * @throws Http2Exception if an error occurs while reading
     */
    void readInboundRequestHeaders(ChannelHandlerContext ctx, Http2HeadersFrame headersFrame) throws Http2Exception;

    /**
     * Reads entity body of inbound request.
     *
     * @param http2SourceHandler HTTP2 source handler
     * @param dataFrame          inbound data frame
     * @throws Http2Exception if an error occurs while reading
     */
    void readInboundRequestBody(Http2SourceHandler http2SourceHandler, Http2DataFrame dataFrame) throws Http2Exception;

    /**
     * Writes headers of outbound response.
     *
     * @param http2OutboundRespListener outbound response listener of response future
     * @param outboundResponseMsg       outbound response message
     * @param httpContent               the initial content of the entity body
     * @param streamId                  the current stream id
     * @throws Http2Exception if an error occurs while writing
     */
    void writeOutboundResponseHeaders(Http2OutboundRespListener http2OutboundRespListener,
                                      HttpCarbonMessage outboundResponseMsg, HttpContent httpContent,
                                      int streamId) throws Http2Exception;

    /**
     * Writes entity body of outbound response.
     *
     * @param http2OutboundRespListener outbound response listener of response future
     * @param outboundResponseMsg       outbound response message
     * @param httpContent               the content of the entity body
     * @param streamId                  the current stream id
     * @throws Http2Exception if an error occurs while writing
     */
    void writeOutboundResponseBody(Http2OutboundRespListener http2OutboundRespListener,
                                   HttpCarbonMessage outboundResponseMsg, HttpContent httpContent,
                                   int streamId) throws Http2Exception;

    /**
     * Writes the outbound promise message.
     *
     * @param http2OutboundRespListener outbound response listener of response future
     * @param pushPromise               outbound promise message
     * @throws Http2Exception if an error occurs while writing
     */
    void writeOutboundPromise(Http2OutboundRespListener http2OutboundRespListener, Http2PushPromise pushPromise)
            throws Http2Exception;

    /**
     * Handles the stream timeout.
     *
     * @param serverConnectorFuture     the sever connector future
     * @param ctx                       the channel handler context
     * @param http2OutboundRespListener the http/2 outbound response listener
     * @param streamId                  the stream id
     */
    void handleStreamTimeout(ServerConnectorFuture serverConnectorFuture, ChannelHandlerContext ctx,
                             Http2OutboundRespListener http2OutboundRespListener, int streamId);

    /**
     * Handles the abrupt channel closure.
     *
     * @param serverConnectorFuture     the sever connector future
     * @param ctx                       the channel handler context
     * @param http2OutboundRespListener the http/2 outbound response listener
     * @param streamId                  the stream id
     */
    void handleAbruptChannelClosure(ServerConnectorFuture serverConnectorFuture, ChannelHandlerContext ctx,
                                    Http2OutboundRespListener http2OutboundRespListener, int streamId);
}
