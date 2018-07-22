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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.Util;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.HttpOutboundRespListener;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Custom Http Content Compressor to handle the content-length and transfer encoding.
 */
public class ReceivingEntityBody implements ListenerState {

    private static Logger log = LoggerFactory.getLogger(ReceivingEntityBody.class);
    private final HandlerExecutor handlerExecutor;
    private final ServerConnectorFuture serverConnectorFuture;
    private HTTPCarbonMessage inboundRequestMsg;

    public ReceivingEntityBody(HTTPCarbonMessage inboundRequestMsg, HandlerExecutor handlerExecutor,
                               ServerConnectorFuture serverConnectorFuture) {
        this.inboundRequestMsg = inboundRequestMsg;
        this.handlerExecutor = handlerExecutor;
        this.serverConnectorFuture = serverConnectorFuture;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

    }

    @Override
    public void readInboundRequestHeaders(ChannelHandlerContext ctx, HttpRequest inboundRequestHeaders) {

    }

    @Override
    public void readInboundReqEntityBody(Object inboundRequestEntityBody) throws ServerConnectorException {
        if (inboundRequestMsg != null) {
            if (inboundRequestEntityBody instanceof HttpContent) {
//                sourceErrorHandler.setState(RECEIVING_ENTITY_BODY);
                HttpContent httpContent = (HttpContent) inboundRequestEntityBody;
                try {
                    inboundRequestMsg.addHttpContent(httpContent);
                    if (Util.isLastHttpContent(httpContent)) {
                        if (handlerExecutor != null) {
                            handlerExecutor.executeAtSourceRequestSending(inboundRequestMsg);
                        }
                        if (isDiffered(inboundRequestMsg)) {
                            serverConnectorFuture.notifyHttpListener(inboundRequestMsg);
                        }
                        inboundRequestMsg = null;
//                        sourceErrorHandler.setState(ENTITY_BODY_RECEIVED);
                    }
                } catch (RuntimeException ex) {
                    httpContent.release();
                    log.warn("Response already received before completing the inbound request" + ex.getMessage());
                }
            }
        } else {
            log.warn("Inconsistent state detected : inboundRequestMsg is null for channel read event");
        }
    }

    @Override
    public void writeOutboundResponseHeaders(HTTPCarbonMessage outboundResponseMsg, HttpContent httpContent) {

    }

    @Override
    public void writeOutboundResponse(HttpOutboundRespListener outboundResponseListener,
                                      HTTPCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        ListenerState state = new SendingHeaders(outboundResponseListener);
        state.writeOutboundResponseHeaders(outboundResponseMsg, httpContent);
    }

    private boolean isDiffered(HTTPCarbonMessage sourceReqCmsg) {
        //Http resource stored in the HTTPCarbonMessage means execution waits till payload.
        return sourceReqCmsg.getProperty(Constants.HTTP_RESOURCE) != null;
    }
}
