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
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.HttpOutboundRespListener;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.listener.SourceHandler;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import static org.wso2.transport.http.netty.common.Util.createInboundReqCarbonMsg;
import static org.wso2.transport.http.netty.common.Util.is100ContinueRequest;

/**
 * Custom Http Content Compressor to handle the content-length and transfer encoding.
 */
public class ReceivingHeaders implements ListenerState {

    private static Logger log = LoggerFactory.getLogger(ReceivingHeaders.class);
    private final SourceHandler sourceHandler;
    private final HandlerExecutor handlerExecutor;
    private HTTPCarbonMessage inboundRequestMsg;
    private boolean continueRequest;

    public ReceivingHeaders(SourceHandler sourceHandler,
                            HandlerExecutor handlerExecutor) {
        this.sourceHandler = sourceHandler;
        this.handlerExecutor = handlerExecutor;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

    }

    @Override
    public void readInboundRequestHeaders(ChannelHandlerContext ctx, HttpRequest inboundRequestHeaders) {
//        sourceErrorHandler.setState(CONNECTED);
        inboundRequestMsg = createInboundReqCarbonMsg(inboundRequestHeaders, ctx, sourceHandler);
        continueRequest = is100ContinueRequest(inboundRequestMsg);
        if (continueRequest) {
//            sourceErrorHandler.setState(EXPECT_100_CONTINUE_HEADER_RECEIVED);
        }
        notifyRequestListener(inboundRequestMsg);

        if (inboundRequestHeaders.decoderResult().isFailure()) {
            log.warn(inboundRequestHeaders.decoderResult().cause().getMessage());
        }
        if (handlerExecutor != null) {
            handlerExecutor.executeAtSourceRequestReceiving(inboundRequestMsg);
        }
    }

    @Override
    public void readInboundReqEntityBody(Object inboundRequestEntityBody) throws ServerConnectorException {
        ListenerState state = new ReceivingEntityBody(inboundRequestMsg, handlerExecutor,
                                                      sourceHandler.getServerConnectorFuture());
        state.readInboundReqEntityBody(inboundRequestEntityBody);

    }

    @Override
    public void writeOutboundResponseHeaders(HTTPCarbonMessage outboundResponseMsg, HttpContent httpContent) {

    }

    @Override
    public void writeOutboundResponse(HttpOutboundRespListener outboundResponseMsg, HTTPCarbonMessage keepAlive,
                                      HttpContent httpContent) {

    }

    private void notifyRequestListener(HTTPCarbonMessage httpRequestMsg) {
        if (handlerExecutor != null) {
            handlerExecutor.executeAtSourceRequestReceiving(httpRequestMsg);
        }

        if (sourceHandler.getServerConnectorFuture() != null) {
            try {
                ServerConnectorFuture outboundRespFuture = httpRequestMsg.getHttpResponseFuture();
                outboundRespFuture.setHttpConnectorListener(
                        new HttpOutboundRespListener(httpRequestMsg, sourceHandler, continueRequest));
                sourceHandler.getServerConnectorFuture().notifyHttpListener(httpRequestMsg);
            } catch (Exception e) {
                log.error("Error while notifying listeners", e);
            }
        } else {
            log.error("Cannot find registered listener to forward the message");
        }
    }
}
