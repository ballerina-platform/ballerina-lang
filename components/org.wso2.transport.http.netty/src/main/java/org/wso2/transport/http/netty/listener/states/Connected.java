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

package org.wso2.transport.http.netty.listener.states;


import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import org.wso2.transport.http.netty.contractimpl.HttpOutboundRespListener;
import org.wso2.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.listener.SourceHandler;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Custom Http Content Compressor to handle the content-length and transfer encoding.
 */
public class Connected implements ListenerState {

    private final SourceHandler sourceHandler;
    private HandlerExecutor handlerExecutor;

    public Connected(SourceHandler sourceHandler) {
        this.sourceHandler = sourceHandler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        sourceHandler.getAllChannels().add(ctx.channel());
        handlerExecutor = HTTPTransportContextHolder.getInstance().getHandlerExecutor();
        sourceHandler.setHandlerExecutor(handlerExecutor);
        if (handlerExecutor != null) {
            handlerExecutor.executeAtSourceConnectionInitiation(Integer.toString(ctx.hashCode()));
        }
        sourceHandler.setRemoteAddress(ctx.channel().remoteAddress());
//        sourceErrorHandler.setState(CONNECTED);
    }

    @Override
    public void readInboundRequestHeaders(ChannelHandlerContext ctx, HttpRequest inboundRequestHeaders) {
        ListenerState state = new ReceivingHeaders(sourceHandler, handlerExecutor);
        state.readInboundRequestHeaders(ctx, inboundRequestHeaders);
    }

    @Override
    public void readInboundReqEntityBody(Object inboundRequestEntityBody) {

    }

    @Override
    public void writeOutboundResponseHeaders(HTTPCarbonMessage outboundResponseMsg, HttpContent httpContent) {

    }

    @Override
    public void writeOutboundResponse(HttpOutboundRespListener outboundResponseListener,
                                      HTTPCarbonMessage outboundResponseMsg, HttpContent httpContent) {

    }
}
