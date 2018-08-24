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

package org.wso2.transport.http.netty.listener.states.sender;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.sender.TargetHandler;

/**
 * SenderStates of target handler.
 */
public interface SenderState {

    void writeOutboundRequestHeaders(HttpCarbonMessage httpOutboundRequest, HttpContent httpContent) throws Exception;

    void writeOutboundRequestEntityBody(HttpCarbonMessage httpOutboundRequest, HttpContent httpContent)
            throws Exception;

    void readInboundResponseHeaders(TargetHandler targetHandler, HttpResponse httpInboundResponse);

    void readInboundResponseEntityBody(ChannelHandlerContext ctx, HttpContent httpContent,
                                       HttpCarbonMessage inboundResponseMsg) throws Exception;

    void handleAbruptChannelClosure(HttpResponseFuture httpResponseFuture);

    void handleIdleTimeoutConnectionClosure(HttpResponseFuture httpResponseFuture, String channelID);
}
