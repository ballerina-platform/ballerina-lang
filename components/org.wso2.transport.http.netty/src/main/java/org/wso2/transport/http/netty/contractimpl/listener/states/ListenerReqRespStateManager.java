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

package org.wso2.transport.http.netty.contractimpl.listener.states;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.HttpOutboundRespListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * Context class to manipulate current state of the listener message.
 */
public class ListenerReqRespStateManager {

    public ListenerState state;

    public void readInboundRequestHeaders(HttpCarbonMessage inboundRequestMsg, HttpRequest inboundRequestHeaders) {
        state.readInboundRequestHeaders(inboundRequestMsg, inboundRequestHeaders);
    }

    public void readInboundRequestBody(Object inboundRequestEntityBody) throws ServerConnectorException {
        state.readInboundRequestBody(inboundRequestEntityBody);
    }

    public void writeOutboundResponseHeaders(HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        state.writeOutboundResponseHeaders(outboundResponseMsg, httpContent);
    }

    public void writeOutboundResponseBody(HttpOutboundRespListener outboundResponseListener,
                                          HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        state.writeOutboundResponseBody(outboundResponseListener, outboundResponseMsg, httpContent);
    }

    public void handleAbruptChannelClosure(ServerConnectorFuture serverConnectorFuture) {
        state.handleAbruptChannelClosure(serverConnectorFuture);
    }

    public ChannelFuture handleIdleTimeoutConnectionClosure(ServerConnectorFuture serverConnectorFuture,
                                                            ChannelHandlerContext ctx) {
        return state.handleIdleTimeoutConnectionClosure(serverConnectorFuture, ctx);
    }
}
