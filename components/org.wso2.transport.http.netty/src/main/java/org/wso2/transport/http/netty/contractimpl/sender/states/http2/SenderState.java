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
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2TargetHandler;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2TargetHandler.Http2RequestWriter;
import org.wso2.transport.http.netty.contractimpl.sender.http2.OutboundMsgHolder;

/**
 * SenderStates of target handler.
 */
public interface SenderState {

    /**
     * Write headers of outbound request.
     *
     * @param requestWriter the request writer
     * @param ctx           the channel handler context
     * @param httpContent   the initial content of the entity body
     */
    void writeOutboundRequestHeaders(Http2RequestWriter requestWriter, ChannelHandlerContext ctx,
                                     HttpContent httpContent) throws Http2Exception;

    /**
     * Write entity body of outbound request.
     *
     * @param requestWriter {@link Http2RequestWriter} is used to write Http2 content to the connection
     * @param ctx           the channel handler context
     * @param httpContent   the content of the entity body
     */
    void writeOutboundRequestEntity(Http2RequestWriter requestWriter, ChannelHandlerContext ctx,
                                    HttpContent httpContent) throws Http2Exception;

    /**
     * Read headers of inbound response.
     *
     * @param targetHandler            the target handler
     * @param ctx                      the channel handler context
     * @param msg                      the HTTP/2 frame
     * @param http2MessageStateContext the message state context
     */
    void readInboundResponseHeaders(Http2TargetHandler targetHandler, ChannelHandlerContext ctx, Object msg,
                                    OutboundMsgHolder outboundMsgHolder, boolean isServerPush,
                                    Http2MessageStateContext http2MessageStateContext);

    /**
     * Write headers of outbound request.
     *
     * @param targetHandler            the target handler
     * @param ctx                      the channel handler context
     * @param msg                      the HTTP/2 frame
     * @param http2MessageStateContext the message state context
     */
    void readInboundResponseEntityBody(Http2TargetHandler targetHandler, ChannelHandlerContext ctx, Object msg,
                                       OutboundMsgHolder outboundMsgHolder, boolean isServerPush,
                                       Http2MessageStateContext http2MessageStateContext);
}
