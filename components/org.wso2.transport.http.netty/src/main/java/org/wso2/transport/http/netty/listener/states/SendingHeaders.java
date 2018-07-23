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

import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.SourceInteractiveState;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.HttpOutboundRespListener;
import org.wso2.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.wso2.transport.http.netty.common.Constants.CHUNKING_CONFIG;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_OUTBOUND_RESPONSE;
import static org.wso2.transport.http.netty.common.SourceInteractiveState.RESPONSE_100_CONTINUE_SENT;
import static org.wso2.transport.http.netty.common.SourceInteractiveState.SENDING_ENTITY_BODY;
import static org.wso2.transport.http.netty.common.Util.createFullHttpResponse;
import static org.wso2.transport.http.netty.common.Util.createHttpResponse;
import static org.wso2.transport.http.netty.common.Util.isLastHttpContent;
import static org.wso2.transport.http.netty.common.Util.isVersionCompatibleForChunking;
import static org.wso2.transport.http.netty.common.Util.setupChunkedRequest;
import static org.wso2.transport.http.netty.common.Util.setupContentLengthRequest;
import static org.wso2.transport.http.netty.common.Util.shouldEnforceChunkingforHttpOneZero;

/**
 * Custom Http Content Compressor to handle the content-length and transfer encoding.
 */
public class SendingHeaders implements ListenerState {

    private static Logger log = LoggerFactory.getLogger(SendingHeaders.class);
    private final HttpOutboundRespListener outboundResponseListener;
    private final boolean keepAlive;
    private final ListenerStateContext stateContext;
    private ChunkConfig chunkConfig;
    private HttpResponseFuture outboundRespStatusFuture;

    public SendingHeaders(HttpOutboundRespListener outboundResponseListener,
                          ListenerStateContext stateContext) {
        this.outboundResponseListener = outboundResponseListener;
        this.stateContext = stateContext;
        this.chunkConfig = outboundResponseListener.getChunkConfig();
        this.keepAlive = outboundResponseListener.isKeepAlive();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

    }

    @Override
    public void readInboundRequestHeaders(ChannelHandlerContext ctx, HttpRequest inboundRequestHeaders) {

    }

    @Override
    public void readInboundReqEntityBody(Object inboundRequestEntityBody) throws ServerConnectorException {

    }

    @Override
    public void writeOutboundResponse(HttpOutboundRespListener outboundResponseListener,
                                      HTTPCarbonMessage outboundResponseMsg, HttpContent httpContent) {

    }

    @Override
    public void writeOutboundResponseHeaders(HTTPCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        ChunkConfig responseChunkConfig = outboundResponseMsg.getProperty(CHUNKING_CONFIG) != null ?
                (ChunkConfig) outboundResponseMsg.getProperty(CHUNKING_CONFIG) : null;
        if (responseChunkConfig != null) {
            this.setChunkConfig(responseChunkConfig);
        }
        if (outboundResponseListener.isContinueRequest()) {
//            sourceErrorHandler.setState(RESPONSE_100_CONTINUE_SENT);
            outboundResponseListener.setContinueRequest(false);
        } else {
//            if (sourceErrorHandler.getState().equals(SourceInteractiveState.RECEIVING_ENTITY_BODY)) {
                // Response is being sent before finish reading the inbound request,
                // hence close the connection once the response is sent.
//                keepAlive = false;
//            }
//            sourceErrorHandler.setState(SENDING_ENTITY_BODY);
        }
        outboundRespStatusFuture = outboundResponseListener.getInboundRequestMsg().getHttpOutboundRespStatusFuture();
        if (isLastHttpContent(httpContent)) {
            if (chunkConfig == ChunkConfig.ALWAYS && checkChunkingCompatibility(outboundResponseListener)) {
                writeHeaders(outboundResponseMsg, keepAlive, outboundRespStatusFuture);
                writeResponse(outboundResponseMsg, httpContent);
            } else {
                writeResponse(outboundResponseMsg, httpContent);
            }
        } else {
            if ((chunkConfig == ChunkConfig.ALWAYS || chunkConfig == ChunkConfig.AUTO) && (
                    checkChunkingCompatibility(outboundResponseListener))) {
                writeHeaders(outboundResponseMsg, keepAlive, outboundRespStatusFuture);

                writeResponse(outboundResponseMsg, httpContent);

            } else {
                writeResponse(outboundResponseMsg, httpContent);
            }

        }
    }

    private void writeResponse(HTTPCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        stateContext.setState(new SendingEntityBody(stateContext, chunkConfig, outboundRespStatusFuture));
        stateContext.getState().writeOutboundResponse(outboundResponseListener, outboundResponseMsg, httpContent);
    }

    private boolean checkChunkingCompatibility(HttpOutboundRespListener outboundResponseListener) {
        return isVersionCompatibleForChunking(outboundResponseListener.getRequestDataHolder().getHttpVersion()) ||
                shouldEnforceChunkingforHttpOneZero(chunkConfig, outboundResponseListener.getRequestDataHolder().getHttpVersion());
    }

    private void writeHeaders(HTTPCarbonMessage outboundResponseMsg, boolean keepAlive,
                              HttpResponseFuture outboundRespStatusFuture) {
        setupChunkedRequest(outboundResponseMsg);
        ChannelFuture outboundHeaderFuture = writeResponseHeaders(outboundResponseMsg, keepAlive);
        addResponseWriteFailureListener(outboundRespStatusFuture, outboundHeaderFuture);
    }

    private void setChunkConfig(ChunkConfig chunkConfig) {
        this.chunkConfig = chunkConfig;
    }

    private ChannelFuture writeResponseHeaders(HTTPCarbonMessage outboundResponseMsg, boolean keepAlive) {
        HttpResponse response = createHttpResponse(outboundResponseMsg, outboundResponseListener.getRequestDataHolder().getHttpVersion(),
                                                   outboundResponseListener.getServerName(), keepAlive);
        return outboundResponseListener.getSourceContext().write(response);
    }

    private void addResponseWriteFailureListener(HttpResponseFuture outboundRespStatusFuture,
                                                ChannelFuture channelFuture) {
        channelFuture.addListener(writeOperationPromise -> {
            Throwable throwable = writeOperationPromise.cause();
            if (throwable != null) {
                if (throwable instanceof ClosedChannelException) {
                    throwable = new IOException(REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_OUTBOUND_RESPONSE);
                }
                outboundRespStatusFuture.notifyHttpListener(throwable);
            }
        });
    }
}
