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

package org.wso2.transport.http.netty.contractimpl.listener.states;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.HttpOutboundRespListener;
import org.wso2.transport.http.netty.contractimpl.listener.SourceHandler;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.ILLEGAL_STATE_ERROR;

/**
 * State of successfully written response.
 */
public class ResponseCompleted implements ListenerState {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseCompleted.class);

    private final SourceHandler sourceHandler;
    private final ListenerReqRespStateManager listenerReqRespStateManager;

    ResponseCompleted(ListenerReqRespStateManager listenerReqRespStateManager, SourceHandler sourceHandler,
                      HttpCarbonMessage inboundRequestMsg) {
        this.listenerReqRespStateManager = listenerReqRespStateManager;
        this.sourceHandler = sourceHandler;
        cleanupSourceHandler(inboundRequestMsg);
    }

    @Override
    public void readInboundRequestHeaders(HttpCarbonMessage inboundRequestMsg, HttpRequest inboundRequestHeaders) {
        listenerReqRespStateManager.state = new ReceivingHeaders(listenerReqRespStateManager, sourceHandler);
        listenerReqRespStateManager.readInboundRequestHeaders(inboundRequestMsg, inboundRequestHeaders);
    }

    @Override
    public void readInboundRequestBody(Object inboundRequestEntityBody) throws ServerConnectorException {
        LOG.warn("readInboundRequestBody {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void writeOutboundResponseHeaders(HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        LOG.warn("writeOutboundResponseHeaders {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void writeOutboundResponseBody(HttpOutboundRespListener outboundRespListener,
                                          HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        LOG.warn("writeOutboundResponseBody {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void handleAbruptChannelClosure(ServerConnectorFuture serverConnectorFuture) {
        LOG.warn("handleAbruptChannelClosure {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public ChannelFuture handleIdleTimeoutConnectionClosure(ServerConnectorFuture serverConnectorFuture,
                                                            ChannelHandlerContext ctx) {
        LOG.warn("handleIdleTimeoutConnectionClosure {}", ILLEGAL_STATE_ERROR);
        return null;
    }

    private void cleanupSourceHandler(HttpCarbonMessage inboundRequestMsg) {
        inboundRequestMsg.waitAndReleaseAllEntities();
        sourceHandler.removeRequestEntry(inboundRequestMsg);
    }
}
