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
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.HttpOutboundRespListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * ListenerStates of source handler.
 */
public interface ListenerState {

    /**
     * Read headers of inbound request.
     *
     * @param inboundRequestMsg     {@link HttpCarbonMessage} which represents the inbound message
     * @param inboundRequestHeaders {@link HttpRequest} which is received at source handler
     */
    void readInboundRequestHeaders(HttpCarbonMessage inboundRequestMsg, HttpRequest inboundRequestHeaders);

    /**
     * Read entity body of inbound request.
     *
     * @param inboundRequestEntityBody which represents the inbound Http content
     * @throws ServerConnectorException if an error occurs while notifying to server connector future
     */
    void readInboundRequestBody(Object inboundRequestEntityBody) throws ServerConnectorException;

    /**
     * Write headers of outbound response.
     *
     * @param outboundResponseMsg {@link HttpCarbonMessage} which represents the outbound message
     * @param httpContent         the initial content of the entity body
     */
    void writeOutboundResponseHeaders(HttpCarbonMessage outboundResponseMsg, HttpContent httpContent);

    /**
     * Write entity body of outbound response.
     *
     * @param outboundResponseListener outbound response listener of response future
     * @param outboundResponseMsg      {@link HttpCarbonMessage} which represents the outbound message
     * @param httpContent              the content of the entity body
     */
    void writeOutboundResponseBody(HttpOutboundRespListener outboundResponseListener,
                                   HttpCarbonMessage outboundResponseMsg, HttpContent httpContent);

    /**
     * Handle channel closure occurred due to abrupt connection failures.
     *
     * @param serverConnectorFuture to notify the closure
     */
    void handleAbruptChannelClosure(ServerConnectorFuture serverConnectorFuture);

    /**
     * Handle channel closure occurred due to idle timeout.
     *
     * @param serverConnectorFuture to notify the closure
     * @param ctx                   the channel handler context
     * @return the channel future related to response write operation
     */
    ChannelFuture handleIdleTimeoutConnectionClosure(ServerConnectorFuture serverConnectorFuture,
                                                     ChannelHandlerContext ctx);
}
