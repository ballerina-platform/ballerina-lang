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
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_INBOUND_REQUEST;

/**
 * State between connection creation and start of inbound request header read
 */
public class Connected implements ListenerState {

    private static Logger log = LoggerFactory.getLogger(Connected.class);
    private final SourceHandler sourceHandler;
    private final ListenerStateContext stateContext;

    public Connected(SourceHandler sourceHandler, ListenerStateContext stateContext) {
        this.sourceHandler = sourceHandler;
        this.stateContext = stateContext;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

    }

    @Override
    public void readInboundRequestHeaders(HttpCarbonMessage inboundRequestMsg, HttpRequest inboundRequestHeaders) {
        stateContext.setState(new ReceivingHeaders(sourceHandler, stateContext));
        stateContext.getState().readInboundRequestHeaders(inboundRequestMsg, inboundRequestHeaders);
    }

    @Override
    public void readInboundRequestEntityBody(Object inboundRequestEntityBody) {
        // Not a dependant action of this state.
    }

    @Override
    public void writeOutboundResponseHeaders(HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        // Not a dependant action of this state.
    }

    @Override
    public void writeOutboundResponseEntityBody(HttpOutboundRespListener outboundResponseListener,
                                                HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        // Not a dependant action of this state.
    }

    @Override
    public void handleAbruptChannelClosure(ServerConnectorFuture serverConnectorFuture) {
        try {
            serverConnectorFuture.notifyErrorListener(
                    new ServerConnectorException(REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_INBOUND_REQUEST));
            // Error is notified to server connector. Debug log is to make transport layer aware
            log.debug(REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_INBOUND_REQUEST);
        } catch (ServerConnectorException e) {
            log.error("Error while notifying error state to server-connector listener");
        }
    }

    @Override
    public ChannelFuture handleIdleTimeoutConnectionClosure(ServerConnectorFuture serverConnectorFuture,
                                                            ChannelHandlerContext ctx, IdleStateEvent evt) {
        try {
            serverConnectorFuture.notifyErrorListener(
                    new ServerConnectorException(IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_REQUEST));
            // Error is notified to server connector. Debug log is to make transport layer aware
            log.debug(IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_REQUEST);
        } catch (ServerConnectorException e) {
            log.error("Error while notifying error state to server-connector listener");
        }
        return null;
    }
}
