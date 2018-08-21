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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.listener.states.StateContext;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.sender.TargetHandler;

/**
 * State between start and end of inbound request headers read.
 */
public class EntityBodyReceived implements SenderState {

    private static Logger log = LoggerFactory.getLogger(EntityBodyReceived.class);
    private final StateContext stateContext;

    public EntityBodyReceived(StateContext stateContext) {
        this.stateContext = stateContext;
    }

    @Override
    public void writeOutboundRequestHeaders(HttpCarbonMessage httpOutboundRequest, HttpContent httpContent) {
    }

    @Override
    public void writeOutboundRequestEntityBody(HttpCarbonMessage httpOutboundRequest, HttpContent httpContent) {

    }

    @Override
    public void readInboundResponseHeaders(TargetHandler targetHandler,
                                           HttpResponse httpInboundResponse) {

    }

    @Override
    public void readInboundResponseEntityBody(ChannelHandlerContext ctx,
                                              HttpContent httpContent,
                                              HttpCarbonMessage inboundResponseMsg) {

    }

    @Override
    public void handleAbruptChannelClosure(HttpResponseFuture httpResponseFuture) {
    }

    @Override
    public void handleIdleTimeoutConnectionClosure(HttpResponseFuture httpResponseFuture, String channelID) {

    }
}
