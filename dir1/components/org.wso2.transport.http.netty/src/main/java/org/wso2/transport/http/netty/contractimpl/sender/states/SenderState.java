/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.contractimpl.sender.states;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contractimpl.sender.TargetHandler;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * SenderStates of target handler.
 */
public interface SenderState {

    /**
     * Write headers of outbound request.
     *
     * @param httpOutboundRequest {@link HttpCarbonMessage} which represents the outbound message
     */
    void writeOutboundRequestHeaders(HttpCarbonMessage httpOutboundRequest);

    /**
     * Write entity body of outbound request.
     *
     * @param httpOutboundRequest {@link HttpCarbonMessage} which represents the outbound message
     * @param httpContent         the content of the entity body
     */
    void writeOutboundRequestEntity(HttpCarbonMessage httpOutboundRequest, HttpContent httpContent);

    /**
     * Read headers of inbound response.
     *
     * @param targetHandler       the target handler
     * @param httpInboundResponse {@link HttpResponse} which is received at target handler
     */
    void readInboundResponseHeaders(TargetHandler targetHandler, HttpResponse httpInboundResponse);

    /**
     * Write headers of outbound request.
     *
     * @param ctx                the channel handler context
     * @param httpContent        the initial content of the entity body
     * @param inboundResponseMsg {@link HttpCarbonMessage} which represents the inbound message
     * @throws Exception if an error occurs while reading response
     */
    void readInboundResponseEntityBody(ChannelHandlerContext ctx, HttpContent httpContent,
                                       HttpCarbonMessage inboundResponseMsg) throws Exception;

    /**
     * Handle channel closure occurred due to abrupt connection failures.
     *
     * @param targetHandler       the target handler
     * @param httpResponseFuture to notify the closure
     */
    void handleAbruptChannelClosure(TargetHandler targetHandler, HttpResponseFuture httpResponseFuture);

    /**
     * Handle channel closure occurred due to idle timeout.
     *
     * @param targetHandler      the target handler
     * @param httpResponseFuture to notify the closure
     * @param channelID          the channel id
     */
    void handleIdleTimeoutConnectionClosure(TargetHandler targetHandler,
                                            HttpResponseFuture httpResponseFuture, String channelID);
}
