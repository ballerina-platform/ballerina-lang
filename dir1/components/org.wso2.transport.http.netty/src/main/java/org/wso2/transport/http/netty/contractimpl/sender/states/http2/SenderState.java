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

package org.wso2.transport.http.netty.contractimpl.sender.states.http2;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http2.Http2Exception;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.sender.http2.OutboundMsgHolder;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.Http2PushPromise;

/**
 * Sender states of target handler.
 *
 * @since 6.0.241
 */
public interface SenderState {

    /**
     * Writes headers of outbound request.
     *
     * @param ctx         the channel handler context
     * @param httpContent the initial content of the entity body
     * @throws Http2Exception if a protocol or connection related error occurred
     */
    void writeOutboundRequestHeaders(ChannelHandlerContext ctx, HttpContent httpContent) throws Http2Exception;

    /**
     * Writes entity body of outbound request.
     *
     * @param ctx                      the channel handler context
     * @param httpContent              the content of the entity body
     * @param http2MessageStateContext the message state context
     * @throws Http2Exception if a protocol or connection related error occurred
     */
    void writeOutboundRequestBody(ChannelHandlerContext ctx, HttpContent httpContent,
                                  Http2MessageStateContext http2MessageStateContext) throws Http2Exception;

    /**
     * Reads headers of inbound response.
     *
     * @param ctx                      the channel handler context
     * @param http2HeadersFrame        the HTTP/2 header frame
     * @param outboundMsgHolder        the outbound message holder
     * @param serverPush               is this a server push response or not
     * @param http2MessageStateContext the message state context
     */
    void readInboundResponseHeaders(ChannelHandlerContext ctx, Http2HeadersFrame http2HeadersFrame,
                                    OutboundMsgHolder outboundMsgHolder, boolean serverPush,
                                    Http2MessageStateContext http2MessageStateContext) throws Http2Exception;

    /**
     * Writes headers of outbound request.
     *
     * @param ctx                      the channel handler context
     * @param http2DataFrame           the HTTP/2 data frame
     * @param outboundMsgHolder        the outbound message holder
     * @param serverPush               is this a server push response or not
     * @param http2MessageStateContext the message state context
     */
    void readInboundResponseBody(ChannelHandlerContext ctx, Http2DataFrame http2DataFrame,
                                 OutboundMsgHolder outboundMsgHolder, boolean serverPush,
                                 Http2MessageStateContext http2MessageStateContext);

    /**
     * Reads inbound promise.
     *
     * @param ctx               the channel handler context
     * @param http2PushPromise  the HTTP/2 promise frame
     * @param outboundMsgHolder the outbound message holder
     */
    void readInboundPromise(ChannelHandlerContext ctx, Http2PushPromise http2PushPromise,
                            OutboundMsgHolder outboundMsgHolder);

    /**
     * Handles stream timeout.
     *
     * @param outboundMsgHolder the outbound message holder
     * @param serverPush        indicates the response type
     */
    void handleStreamTimeout(OutboundMsgHolder outboundMsgHolder, boolean serverPush, ChannelHandlerContext ctx,
            int streamId) throws Http2Exception;

    /**
     * Handles the connection close event.
     */
    void handleConnectionClose(OutboundMsgHolder outboundMsgHolder);
}
