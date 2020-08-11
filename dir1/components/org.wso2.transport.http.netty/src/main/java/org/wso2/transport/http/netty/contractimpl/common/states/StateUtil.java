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

package org.wso2.transport.http.netty.contractimpl.common.states;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.config.ChunkConfig;
import org.wso2.transport.http.netty.contract.config.KeepAliveConfig;
import org.wso2.transport.http.netty.contractimpl.HttpOutboundRespListener;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.contractimpl.listener.states.ListenerReqRespStateManager;
import org.wso2.transport.http.netty.contractimpl.listener.states.SendingHeaders;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility functions for states.
 */
public class StateUtil {

    private static final Logger LOG = LoggerFactory.getLogger(StateUtil.class);

    public static final String ILLEGAL_STATE_ERROR = "is not a valid action of this state";
    public static final String CONNECTOR_NOTIFYING_ERROR =
            "Error while notifying error state to server-connector listener";

    private StateUtil() {
        //Hides implicit public constructor.
    }

    public static boolean checkChunkingCompatibility(String httpVersion, ChunkConfig chunkConfig) {
        return Util.isVersionCompatibleForChunking(httpVersion) || Util
                .shouldEnforceChunkingforHttpOneZero(chunkConfig, httpVersion);
    }

    public static void notifyIfHeaderWriteFailure(HttpResponseFuture httpResponseStatusFuture,
                                                  ChannelFuture outboundHeaderFuture, String errorMsg) {
        outboundHeaderFuture.addListener(writeOperationPromise -> {
            Throwable throwable = writeOperationPromise.cause();
            if (throwable != null) {
                if (throwable instanceof ClosedChannelException) {
                    throwable = new IOException(errorMsg);
                }
                httpResponseStatusFuture.notifyHttpListener(throwable);
            }
        });
    }

    public static ChannelFuture sendRequestTimeoutResponse(ChannelHandlerContext ctx, HttpResponseStatus status,
                                                           ByteBuf content, int length, float httpVersion,
                                                           String serverName) {
        HttpResponse outboundResponse;
        if (httpVersion == Constants.HTTP_1_0) {
            outboundResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, status, content);
        } else {
            outboundResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        }

        outboundResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, length);
        if (length != 0) {
            outboundResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, Constants.TEXT_PLAIN);
        }
        outboundResponse.headers().set(HttpHeaderNames.CONNECTION.toString(), Constants.CONNECTION_CLOSE);
        outboundResponse.headers().set(HttpHeaderNames.SERVER.toString(), serverName);
        return ctx.channel().writeAndFlush(outboundResponse);
    }

    public static void handleIncompleteInboundMessage(HttpCarbonMessage inboundRequestMsg, String errorMessage) {
        LastHttpContent lastHttpContent = new DefaultLastHttpContent();
        DecoderException exception = new DecoderException(errorMessage);
        lastHttpContent.setDecoderResult(DecoderResult.failure(exception));
        if (inboundRequestMsg != null) {
            inboundRequestMsg.addHttpContent(lastHttpContent);
            inboundRequestMsg.notifyContentFailure(exception);
        }
        LOG.warn(errorMessage);
    }

    public static void respondToIncompleteRequest(Channel channel, HttpOutboundRespListener outboundResponseListener,
                                                  ListenerReqRespStateManager listenerReqRespStateManager,
                                                  HttpCarbonMessage outboundResponseMsg, HttpContent httpContent,
                                                  String errorMsg) {
        // Response is processing, but inbound request is not completed yet. So removing the read interest
        channel.config().setAutoRead(false);
        if (outboundResponseListener.getInboundRequestMsg().getIoException() == null) {
            handleIncompleteInboundMessage(outboundResponseListener.getInboundRequestMsg(), errorMsg);
        }

        // It is an application error. Therefore connection needs to be closed once the response is sent.
        outboundResponseListener.setKeepAliveConfig(KeepAliveConfig.NEVER);
        listenerReqRespStateManager.state
                = new SendingHeaders(listenerReqRespStateManager, outboundResponseListener);
        listenerReqRespStateManager.writeOutboundResponseHeaders(outboundResponseMsg, httpContent);
    }

    // This method will create and add the trailer header by looking at the trailers lies in HttpCarbonMessage
    // httpTrailerHeaders attribute. It does not consider seperately injected trailers to the LastHttpContent through
    // netty's API. Dev should use HttpCarbonMessage.getTrailerHeaders() API to manipulate trailer. Otherwise header
    // should be manually added to avoid any trailer miss.
    public static void addTrailerHeaderIfPresent(HttpCarbonMessage outboundResponseMsg) {
        if (outboundResponseMsg.getTrailerHeaders().isEmpty()) {
            return;
        }
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, String> header : outboundResponseMsg.getTrailerHeaders().entries()) {
            names.add(header.getKey());
        }
        String trailerHeaderValue = String.join(", ", names);
        outboundResponseMsg.setHeader(HttpHeaderNames.TRAILER.toString(), trailerHeaderValue);
    }

    /**
     * Populate inboound trailer of the response content to the HttpCarbonMessage and clear the trailer from the
     * HttpContent. Make sure dev adds the LHC to the content queue after this method invocation, to avoid concurrent
     * issues when accesssing trailers. Standard is to access to using HCM API.
     *
     * @param trailers    Represent the inbound trailing header
     * @param responseMsg Represent the newly created response message
     */
    public static void setInboundTrailersToNewMessage(HttpHeaders trailers, HttpCarbonMessage responseMsg) {
        if (trailers.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> header : trailers.entries()) {
            responseMsg.getTrailerHeaders().set(header.getKey(), header.getValue());
        }
        trailers.clear();
    }
}
