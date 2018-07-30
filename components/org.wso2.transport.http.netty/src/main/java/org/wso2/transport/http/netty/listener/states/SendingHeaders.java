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
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.HttpOutboundRespListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

import static org.wso2.transport.http.netty.common.Constants.CHUNKING_CONFIG;
import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_RESPONSE;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_OUTBOUND_RESPONSE;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_CLOSED_WHILE_WRITING_OUTBOUND_RESPONSE;
import static org.wso2.transport.http.netty.common.Util.createHttpResponse;
import static org.wso2.transport.http.netty.common.Util.isLastHttpContent;
import static org.wso2.transport.http.netty.common.Util.isVersionCompatibleForChunking;
import static org.wso2.transport.http.netty.common.Util.setupChunkedRequest;
import static org.wso2.transport.http.netty.common.Util.shouldEnforceChunkingforHttpOneZero;

/**
 * State between start and end of outbound response headers write
 */
public class SendingHeaders implements ListenerState {

    private static Logger log = LoggerFactory.getLogger(SendingHeaders.class);
    private final HttpOutboundRespListener outboundResponseListener;
    boolean keepAlive;
    private final ListenerStateContext stateContext;
    ChunkConfig chunkConfig;
    HttpResponseFuture outboundRespStatusFuture;

    public SendingHeaders(HttpOutboundRespListener outboundResponseListener,
                          ListenerStateContext stateContext) {
        this.outboundResponseListener = outboundResponseListener;
        this.stateContext = stateContext;
        this.chunkConfig = outboundResponseListener.getChunkConfig();
        this.keepAlive = outboundResponseListener.isKeepAlive();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // Not a dependant action of this state.
    }

    @Override
    public void readInboundRequestHeaders(HttpCarbonMessage inboundRequestMsg, HttpRequest inboundRequestHeaders) {
        // Not a dependant action of this state.
    }

    @Override
    public void readInboundRequestEntityBody(Object inboundRequestEntityBody) throws ServerConnectorException {
        // Not a dependant action of this state.
    }

    @Override
    public void writeOutboundResponseEntityBody(HttpOutboundRespListener outboundResponseListener,
                                                HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        // Not a dependant action of this state.
    }

    @Override
    public void handleAbruptChannelClosure(ServerConnectorFuture serverConnectorFuture) {
        // OutboundResponseStatusFuture will be notified asynchronously via OutboundResponseListener.
        log.error(REMOTE_CLIENT_CLOSED_WHILE_WRITING_OUTBOUND_RESPONSE);
    }

    @Override
    public ChannelFuture handleIdleTimeoutConnectionClosure(ServerConnectorFuture serverConnectorFuture,
                                                            ChannelHandlerContext ctx,
                                                            IdleStateEvent evt) {
        // OutboundResponseStatusFuture will be notified asynchronously via OutboundResponseListener.
        log.error(IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_RESPONSE);
        return null;
    }

    @Override
    public void writeOutboundResponseHeaders(HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        ChunkConfig responseChunkConfig = outboundResponseMsg.getProperty(CHUNKING_CONFIG) != null ?
                (ChunkConfig) outboundResponseMsg.getProperty(CHUNKING_CONFIG) : null;
        if (responseChunkConfig != null) {
            this.setChunkConfig(responseChunkConfig);
        }
        outboundRespStatusFuture = outboundResponseListener.getInboundRequestMsg().getHttpOutboundRespStatusFuture();
        if (isLastHttpContent(httpContent)) {
            if (chunkConfig == ChunkConfig.ALWAYS && checkChunkingCompatibility(outboundResponseListener)) {
                writeHeaders(outboundResponseMsg, keepAlive, outboundRespStatusFuture);
                writeResponse(outboundResponseMsg, httpContent, true);
                return;
            }
        } else {
            if ((chunkConfig == ChunkConfig.ALWAYS || chunkConfig == ChunkConfig.AUTO) && (
                    checkChunkingCompatibility(outboundResponseListener))) {
                writeHeaders(outboundResponseMsg, keepAlive, outboundRespStatusFuture);
                writeResponse(outboundResponseMsg, httpContent, true);
                return;
            }
        }
        writeResponse(outboundResponseMsg, httpContent, false);
    }

    private void writeResponse(HttpCarbonMessage outboundResponseMsg, HttpContent httpContent, boolean headersWritten) {
        stateContext.setState(new SendingEntityBody(stateContext, chunkConfig, outboundRespStatusFuture, headersWritten));
        stateContext.getState().writeOutboundResponseEntityBody(outboundResponseListener, outboundResponseMsg, httpContent);
    }

    boolean checkChunkingCompatibility(HttpOutboundRespListener outboundResponseListener) {
        return isVersionCompatibleForChunking(outboundResponseListener.getRequestDataHolder().getHttpVersion()) ||
                shouldEnforceChunkingforHttpOneZero(chunkConfig, outboundResponseListener.getRequestDataHolder().getHttpVersion());
    }

    private void writeHeaders(HttpCarbonMessage outboundResponseMsg, boolean keepAlive,
                              HttpResponseFuture outboundRespStatusFuture) {
        setupChunkedRequest(outboundResponseMsg);
        ChannelFuture outboundHeaderFuture = writeResponseHeaders(outboundResponseMsg, keepAlive);
        addResponseWriteFailureListener(outboundRespStatusFuture, outboundHeaderFuture);
    }

    void setChunkConfig(ChunkConfig chunkConfig) {
        this.chunkConfig = chunkConfig;
    }

    ChannelFuture writeResponseHeaders(HttpCarbonMessage outboundResponseMsg, boolean keepAlive) {
        HttpResponse response = createHttpResponse(outboundResponseMsg, outboundResponseListener.getRequestDataHolder().getHttpVersion(),
                                                   outboundResponseListener.getServerName(), keepAlive);
        return outboundResponseListener.getSourceContext().write(response);
    }

    void addResponseWriteFailureListener(HttpResponseFuture outboundRespStatusFuture,
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
