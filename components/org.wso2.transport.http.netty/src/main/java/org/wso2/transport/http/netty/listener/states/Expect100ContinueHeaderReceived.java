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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.HttpOutboundRespListener;
import org.wso2.transport.http.netty.listener.SourceHandler;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import static org.wso2.transport.http.netty.common.Constants
        .REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_100_CONTINUE_RESPONSE;

/**
 * Special state of receiving request with expect:100-continue header
 */
public class Expect100ContinueHeaderReceived implements ListenerState {

    private static Logger log = LoggerFactory.getLogger(Expect100ContinueHeaderReceived.class);
    private final ListenerStateContext stateContext;
    private final SourceHandler sourceHandler;

    public Expect100ContinueHeaderReceived(
            ListenerStateContext stateContext, SourceHandler sourceHandler) {
        this.stateContext = stateContext;
        this.sourceHandler = sourceHandler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // Not a dependant action of this state.
    }

    @Override
    public void readInboundRequestHeaders(HTTPCarbonMessage inboundRequestMsg, HttpRequest inboundRequestHeaders) {
        // Not a dependant action of this state.
    }

    @Override
    public void readInboundRequestEntityBody(Object inboundRequestEntityBody) throws ServerConnectorException {
        // Not a dependant action of this state.
    }

    @Override
    public void writeOutboundResponseHeaders(HTTPCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        // Not a dependant action of this state.
    }

    @Override
    public void writeOutboundResponseEntityBody(HttpOutboundRespListener outboundResponseListener,
                                                HTTPCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        stateContext.setState(new Response100ContinueSent(outboundResponseListener, sourceHandler, stateContext));
        stateContext.getState().writeOutboundResponseEntityBody(outboundResponseListener, outboundResponseMsg, httpContent);
    }

    @Override
    public void handleAbruptChannelClosure(ServerConnectorFuture serverConnectorFuture) {
        try {
            serverConnectorFuture.notifyErrorListener(
                    new ServerConnectorException(REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_100_CONTINUE_RESPONSE));
        } catch (ServerConnectorException e) {
            log.error("Error while notifying error state to server-connector listener");
        }
    }

    @Override
    public ChannelFuture handleIdleTimeoutConnectionClosure(ServerConnectorFuture serverConnectorFuture,
                                                            ChannelHandlerContext ctx, IdleStateEvent evt) {
        return null;
    }
}
