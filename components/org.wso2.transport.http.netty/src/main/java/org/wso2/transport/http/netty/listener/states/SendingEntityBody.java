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
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.HttpOutboundRespListener;
import org.wso2.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_TO_HOST_CONNECTION_CLOSED;
import static org.wso2.transport.http.netty.common.SourceInteractiveState.ENTITY_BODY_SENT;
import static org.wso2.transport.http.netty.common.Util.createFullHttpResponse;
import static org.wso2.transport.http.netty.common.Util.isLastHttpContent;
import static org.wso2.transport.http.netty.common.Util.isVersionCompatibleForChunking;
import static org.wso2.transport.http.netty.common.Util.setupChunkedRequest;
import static org.wso2.transport.http.netty.common.Util.setupContentLengthRequest;
import static org.wso2.transport.http.netty.common.Util.shouldEnforceChunkingforHttpOneZero;

/**
 * Custom Http Content Compressor to handle the content-length and transfer encoding.
 */
public class SendingEntityBody implements ListenerState {

    private static Logger log = LoggerFactory.getLogger(SendingEntityBody.class);
    private final HandlerExecutor handlerExecutor;
    private final HttpResponseFuture outboundRespStatusFuture;
    private final ListenerStateContext stateContext;
    private ChunkConfig chunkConfig;
    private long contentLength = 0;
    private boolean headRequest;
    private List<HttpContent> contentList = new ArrayList<>();
    private HTTPCarbonMessage inboundRequestMsg;
    private ChannelHandlerContext sourceContext;


    public SendingEntityBody(ListenerStateContext stateContext, ChunkConfig chunkConfig, HttpResponseFuture outboundRespStatusFuture) {
        this.stateContext = stateContext;
        this.chunkConfig = chunkConfig;
        this.outboundRespStatusFuture = outboundRespStatusFuture;
        handlerExecutor = HTTPTransportContextHolder.getInstance().getHandlerExecutor();
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
    public void writeOutboundResponseHeaders(HTTPCarbonMessage outboundResponseMsg, HttpContent httpContent) {

    }

    @Override
    public void writeOutboundResponse(HttpOutboundRespListener outboundRespListener,
                                      HTTPCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        if (outboundRespListener.isContinueRequest()) {
//            sourceErrorHandler.setState(RESPONSE_100_CONTINUE_SENT);
            outboundRespListener.setContinueRequest(false);
        } else {
//            if (sourceErrorHandler.getState().equals(SourceInteractiveState.RECEIVING_ENTITY_BODY)) {
                // Response is being sent before finish reading the inbound request,
                // hence close the connection once the response is sent.
//                keepAlive = false;
//            }
//            sourceErrorHandler.setState(SENDING_ENTITY_BODY);
        }

        headRequest = outboundRespListener.getRequestDataHolder().getHttpMethod().equalsIgnoreCase(Constants.HTTP_HEAD_METHOD);
        inboundRequestMsg = outboundRespListener.getInboundRequestMsg();
        sourceContext = outboundRespListener.getSourceContext();

        ChannelFuture outboundChannelFuture;
        if (isLastHttpContent(httpContent)) {
            if (chunkConfig == ChunkConfig.ALWAYS && checkChunkingCompatibility(outboundRespListener)) {
                outboundChannelFuture = checkHeadRequestAndWriteOutboundResponseBody(httpContent);

            } else {
                contentLength += httpContent.content().readableBytes();
                setupContentLengthRequest(outboundResponseMsg, contentLength);
                outboundChannelFuture = writeOutboundResponseHeaderAndBody(outboundRespListener, outboundResponseMsg,
                                                                           (LastHttpContent) httpContent);
            }

            if (!outboundRespListener.isKeepAlive()) {
                outboundChannelFuture.addListener(ChannelFutureListener.CLOSE);
            }
            if (handlerExecutor != null) {
                handlerExecutor.executeAtSourceResponseSending(outboundResponseMsg);
            }
        } else {
            if ((chunkConfig == ChunkConfig.ALWAYS || chunkConfig == ChunkConfig.AUTO) && (
                    checkChunkingCompatibility(outboundRespListener))) {
                if (headRequest) {
                    httpContent.release();
                    return;
                }
                outboundRespListener.getSourceContext().writeAndFlush(httpContent);
            } else {
                this.contentList.add(httpContent);
                contentLength += httpContent.content().readableBytes();
            }
        }
    }

    private boolean checkChunkingCompatibility(HttpOutboundRespListener outboundResponseListener) {
        return isVersionCompatibleForChunking(outboundResponseListener.getRequestDataHolder().getHttpVersion()) ||
                shouldEnforceChunkingforHttpOneZero(chunkConfig, outboundResponseListener.getRequestDataHolder().getHttpVersion());
    }

    private ChannelFuture checkHeadRequestAndWriteOutboundResponseBody(HttpContent httpContent) {
        ChannelFuture outboundChannelFuture;
        if (headRequest) {
            httpContent.release();
            outboundChannelFuture = writeOutboundResponseBody(new DefaultLastHttpContent());
        } else {
            outboundChannelFuture = writeOutboundResponseBody(httpContent);
        }
        return outboundChannelFuture;
    }

    private ChannelFuture writeOutboundResponseHeaderAndBody(HttpOutboundRespListener outboundRespListener,
                                                             HTTPCarbonMessage outboundResponseMsg, LastHttpContent lastHttpContent) {
        CompositeByteBuf allContent = Unpooled.compositeBuffer();
        for (HttpContent cachedHttpContent : contentList) {
            allContent.addComponent(true, cachedHttpContent.content());
        }
        allContent.addComponent(true, lastHttpContent.content());

        if (headRequest) {
            allContent.release();
            allContent = Unpooled.compositeBuffer();
            allContent.addComponent(true, new DefaultLastHttpContent().content());
        }

        HttpResponse fullOutboundResponse = createFullHttpResponse(outboundResponseMsg,
                                                                   outboundRespListener.getRequestDataHolder()
                                                                           .getHttpVersion(),
                                                                   outboundRespListener.getServerName(),
                                                                   outboundRespListener.isKeepAlive(), allContent);

        ChannelFuture outboundChannelFuture = sourceContext.writeAndFlush(fullOutboundResponse);
        checkForResponseWriteStatus(inboundRequestMsg, outboundRespStatusFuture, outboundChannelFuture);
        return outboundChannelFuture;
    }

    private ChannelFuture writeOutboundResponseBody(HttpContent lastHttpContent) {
        ChannelFuture outboundChannelFuture = sourceContext.writeAndFlush(lastHttpContent);
        checkForResponseWriteStatus(inboundRequestMsg, outboundRespStatusFuture, outboundChannelFuture);
        return outboundChannelFuture;
    }

    private void checkForResponseWriteStatus(HTTPCarbonMessage inboundRequestMsg,
                                            HttpResponseFuture outboundRespStatusFuture, ChannelFuture channelFuture) {
        channelFuture.addListener(writeOperationPromise -> {
            Throwable throwable = writeOperationPromise.cause();
            if (throwable != null) {
                if (throwable instanceof ClosedChannelException) {
                    throwable = new IOException(REMOTE_CLIENT_TO_HOST_CONNECTION_CLOSED);
                }
                outboundRespStatusFuture.notifyHttpListener(throwable);
            } else {
                outboundRespStatusFuture.notifyHttpListener(inboundRequestMsg);
//                this.setState(ENTITY_BODY_SENT);
            }
        });
    }
}
