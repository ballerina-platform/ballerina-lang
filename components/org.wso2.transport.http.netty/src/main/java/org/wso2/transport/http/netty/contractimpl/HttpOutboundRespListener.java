/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.transport.http.netty.contractimpl;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.Util;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.listener.RequestDataHolder;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Get executed when the response is available.
 */
public class HttpOutboundRespListener implements HttpConnectorListener {

    private ChannelHandlerContext sourceContext;
    private RequestDataHolder requestDataHolder;
    private HandlerExecutor handlerExecutor;
    private HTTPCarbonMessage inboundRequestMsg;
    private ChunkConfig chunkConfig;
    private boolean isHeaderWritten = false;
    private int contentLength = 0;
    private List<HttpContent> contentList = new ArrayList<>();

    public HttpOutboundRespListener(ChannelHandlerContext channelHandlerContext, HTTPCarbonMessage requestMsg,
            ChunkConfig chunkConfig) {
        this.sourceContext = channelHandlerContext;
        this.requestDataHolder = new RequestDataHolder(requestMsg);
        this.inboundRequestMsg = requestMsg;
        this.handlerExecutor = HTTPTransportContextHolder.getInstance().getHandlerExecutor();
        this.chunkConfig = chunkConfig;
    }

    @Override
    public void onMessage(HTTPCarbonMessage outboundResponseMsg) {
        sourceContext.channel().eventLoop().execute(() -> {

            if (handlerExecutor != null) {
                handlerExecutor.executeAtSourceResponseReceiving(outboundResponseMsg);
            }

            boolean keepAlive = isKeepAlive(outboundResponseMsg);

            outboundResponseMsg.getHttpContentAsync().setMessageListener(httpContent ->
                    this.sourceContext.channel().eventLoop().execute(() -> {
                if (Util.isLastHttpContent(httpContent)) {
                    if (!isHeaderWritten) {
                        if ((chunkConfig == ChunkConfig.ALWAYS)
                                && Util.isVersionCompatibleForChunking(requestDataHolder.getHttpVersion())) {
                            Util.setupChunkedRequest(outboundResponseMsg);
                        } else {
                            contentLength += httpContent.content().readableBytes();
                            Util.setupContentLengthRequest(outboundResponseMsg, contentLength);
                        }
                        writeOutboundResponseHeaders(outboundResponseMsg, keepAlive);
                    }

                    if (chunkConfig == ChunkConfig.NEVER) {
                        for (HttpContent cachedHttpContent : contentList) {
                            sourceContext.writeAndFlush(cachedHttpContent);
                        }
                    }
                    ChannelFuture outboundChannelFuture = sourceContext.writeAndFlush(httpContent);
                    HttpResponseStatusFuture outboundRespStatusFuture =
                            inboundRequestMsg.getHttpOutboundRespStatusFuture();
                    outboundChannelFuture.addListener(genericFutureListener -> {
                        if (genericFutureListener.cause() != null) {
                            outboundRespStatusFuture.notifyHttpListener(genericFutureListener.cause());
                        } else {
                            outboundRespStatusFuture.notifyHttpListener(inboundRequestMsg);
                        }
                    });

                    if (!keepAlive) {
                        outboundChannelFuture.addListener(ChannelFutureListener.CLOSE);
                    }
                    if (handlerExecutor != null) {
                        handlerExecutor.executeAtSourceResponseSending(outboundResponseMsg);
                    }
                    outboundResponseMsg.removeHttpContentAsyncFuture();
                    contentList.clear();
                    contentLength = 0;
                } else {
                    if ((chunkConfig == ChunkConfig.ALWAYS || chunkConfig == ChunkConfig.AUTO)
                            && Util.isVersionCompatibleForChunking(requestDataHolder.getHttpVersion())) {
                        if (!isHeaderWritten) {
                            Util.setupChunkedRequest(outboundResponseMsg);
                            writeOutboundResponseHeaders(outboundResponseMsg, keepAlive);
                        }
                        sourceContext.writeAndFlush(httpContent);
                    } else {
                        this.contentList.add(httpContent);
                        contentLength += httpContent.content().readableBytes();
                    }
                }
            }));
        });
    }

    // Decides whether to close the connection after sending the response
    private boolean isKeepAlive(HTTPCarbonMessage responseMsg) {
        String responseConnectionHeader = responseMsg.getHeader(Constants.HTTP_CONNECTION);
        String requestConnectionHeader = requestDataHolder.getConnectionHeaderValue();
        if ((responseConnectionHeader != null &&
                Constants.CONNECTION_CLOSE.equalsIgnoreCase(responseConnectionHeader))
                || (requestConnectionHeader != null &&
                Constants.CONNECTION_CLOSE.equalsIgnoreCase(requestConnectionHeader))) {
            return false;
        }
        return true;
    }

    @Override
    public void onError(Throwable throwable) {

    }

    private void writeOutboundResponseHeaders(HTTPCarbonMessage httpOutboundRequest, boolean keepAlive) {
        HttpResponse response = Util.createHttpResponse(httpOutboundRequest, keepAlive);
        isHeaderWritten = true;
        sourceContext.write(response);
    }
}
