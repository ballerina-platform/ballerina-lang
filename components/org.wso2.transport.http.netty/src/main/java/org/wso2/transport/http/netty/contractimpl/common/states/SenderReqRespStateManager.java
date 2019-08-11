/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.contractimpl.common.states;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contractimpl.sender.TargetHandler;
import org.wso2.transport.http.netty.contractimpl.sender.states.SenderState;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * Context class to manipulate current state of the message.
 */
public class SenderReqRespStateManager {

    public SenderState senderState;

    public void writeOutboundRequestHeaders(HttpCarbonMessage httpOutboundRequest, HttpContent httpContent) {
        senderState.writeOutboundRequestHeaders(httpOutboundRequest, httpContent);
    }

    public void writeOutboundRequestEntity(HttpCarbonMessage httpOutboundRequest, HttpContent httpContent) {
        senderState.writeOutboundRequestEntity(httpOutboundRequest, httpContent);
    }

    public void readInboundResponseHeaders(TargetHandler targetHandler, HttpResponse httpInboundResponse) {
        senderState.readInboundResponseHeaders(targetHandler, httpInboundResponse);
    }

    public void readInboundResponseEntityBody(ChannelHandlerContext ctx, HttpContent httpContent,
                                              HttpCarbonMessage inboundResponseMsg) throws Exception {
        senderState.readInboundResponseEntityBody(ctx, httpContent, inboundResponseMsg);
    }

    public void handleAbruptChannelClosure(HttpResponseFuture httpResponseFuture) {
        senderState.handleAbruptChannelClosure(httpResponseFuture);
    }

    public void handleIdleTimeoutConnectionClosure(HttpResponseFuture httpResponseFuture, String channelID) {
        senderState.handleIdleTimeoutConnectionClosure(httpResponseFuture, channelID);
    }
}
