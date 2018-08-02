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

package org.wso2.transport.http.netty.listener.senderstates;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.HttpOutboundRespListener;
import org.wso2.transport.http.netty.exception.EndpointTimeOutException;
import org.wso2.transport.http.netty.listener.SourceHandler;
import org.wso2.transport.http.netty.listener.states.ListenerState;
import org.wso2.transport.http.netty.listener.states.ListenerStateContext;
import org.wso2.transport.http.netty.listener.states.ReceivingHeaders;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_RESPONSE;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_SERVER_CLOSED_BEFORE_INITIATING_INBOUND_RESPONSE;

/**
 * State of successfully written response
 */
public class EntityBodySent implements SenderState {

    private static Logger log = LoggerFactory.getLogger(EntityBodySent.class);

    @Override
    public void writeOutboundRequestHeaders() {

    }

    @Override
    public void writeOutboundRequestEntityBody() {

    }

    @Override
    public void readInboundResponseHeaders() {

    }

    @Override
    public void readInboundResponseEntityBody() {

    }

    @Override
    public void handleAbruptChannelClosure(ServerConnectorFuture serverConnectorFuture) {
        httpResponseFuture.notifyHttpListener(
                new ServerConnectorException(REMOTE_SERVER_CLOSED_BEFORE_INITIATING_INBOUND_RESPONSE));
        log.error(REMOTE_SERVER_CLOSED_BEFORE_INITIATING_INBOUND_RESPONSE);

    }

    @Override
    public ChannelFuture handleIdleTimeoutConnectionClosure(ServerConnectorFuture serverConnectorFuture,
                                                            ChannelHandlerContext ctx, IdleStateEvent evt) {
        httpResponseFuture.notifyHttpListener(new EndpointTimeOutException(channelID,
                                                                           IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_RESPONSE,
                                                                           HttpResponseStatus.GATEWAY_TIMEOUT.code()));
        log.error("Error in HTTP client: {}", IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_RESPONSE);

        return null;
    }
}
