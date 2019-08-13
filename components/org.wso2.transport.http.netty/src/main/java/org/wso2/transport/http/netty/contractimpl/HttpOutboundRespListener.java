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

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.config.ChunkConfig;
import org.wso2.transport.http.netty.contract.config.KeepAliveConfig;
import org.wso2.transport.http.netty.contractimpl.common.BackPressureHandler;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.contractimpl.listener.RequestDataHolder;
import org.wso2.transport.http.netty.contractimpl.listener.SourceHandler;
import org.wso2.transport.http.netty.contractimpl.listener.states.ListenerReqRespStateManager;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.internal.HttpTransportContextHolder;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.Locale;

import static org.wso2.transport.http.netty.contractimpl.common.Util.isKeepAliveConnection;

/**
 * Get executed when the response is available.
 */
public class HttpOutboundRespListener implements HttpConnectorListener {

    private static final Logger LOG = LoggerFactory.getLogger(HttpOutboundRespListener.class);
    private final SourceHandler sourceHandler;
    private final ListenerReqRespStateManager listenerReqRespStateManager;

    private ChannelHandlerContext sourceContext;
    private RequestDataHolder requestDataHolder;
    private HandlerExecutor handlerExecutor;
    private HttpCarbonMessage inboundRequestMsg;
    private ChunkConfig chunkConfig;
    private KeepAliveConfig keepAliveConfig;
    private String serverName;

    public HttpOutboundRespListener(HttpCarbonMessage requestMsg, SourceHandler sourceHandler) {
        this.requestDataHolder = new RequestDataHolder(requestMsg);
        this.inboundRequestMsg = requestMsg;
        this.sourceHandler = sourceHandler;
        this.sourceContext = sourceHandler.getInboundChannelContext();
        this.chunkConfig = sourceHandler.getChunkConfig();
        this.keepAliveConfig = sourceHandler.getKeepAliveConfig();
        this.handlerExecutor = HttpTransportContextHolder.getInstance().getHandlerExecutor();
        this.serverName = sourceHandler.getServerName();
        this.listenerReqRespStateManager = requestMsg.listenerReqRespStateManager;
    }

    @Override
    public void onMessage(HttpCarbonMessage outboundResponseMsg) {
        if (handlerExecutor != null) {
            handlerExecutor.executeAtSourceResponseReceiving(outboundResponseMsg);
        }

        BackPressureHandler backpressureHandler = Util.getBackPressureHandler(sourceContext);
        Util.setBackPressureListener(outboundResponseMsg, backpressureHandler, outboundResponseMsg.getTargetContext());

        outboundResponseMsg.getHttpContentAsync().setMessageListener(httpContent -> {
            Util.checkUnWritabilityAndNotify(sourceContext, backpressureHandler);
            this.sourceContext.channel().eventLoop().execute(() -> {
                try {
                    listenerReqRespStateManager.writeOutboundResponseBody(this, outboundResponseMsg, httpContent);
                } catch (Exception exception) {
                    String errorMsg = "Failed to send the outbound response : "
                            + exception.getMessage().toLowerCase(Locale.ENGLISH);
                    LOG.error(errorMsg, exception);
                    inboundRequestMsg.getHttpOutboundRespStatusFuture().notifyHttpListener(exception);
                }
            });
        });
    }

    @Override
    public void onPushPromise(Http2PushPromise pushPromise) {
        inboundRequestMsg.getHttpOutboundRespStatusFuture().notifyHttpListener(new UnsupportedOperationException(
                "Sending a PUSH_PROMISE is not supported for HTTP/1.x connections"));
    }

    @Override
    public void onPushResponse(int promiseId, HttpCarbonMessage httpMessage) {
        inboundRequestMsg.getHttpOutboundRespStatusFuture().notifyHttpListener(new UnsupportedOperationException(
                "Sending Server Push messages is not supported for HTTP/1.x connections"));
    }

    // Decides whether to close the connection after sending the response
    public boolean isKeepAlive() {
        return isKeepAliveConnection(keepAliveConfig, requestDataHolder.getConnectionHeaderValue(),
                requestDataHolder.getHttpVersion());
    }

    @Override
    public void onError(Throwable throwable) {
        LOG.error("Couldn't send the outbound response", throwable);
    }

    public ChunkConfig getChunkConfig() {
        return chunkConfig;
    }

    public HttpCarbonMessage getInboundRequestMsg() {
        return inboundRequestMsg;
    }

    public RequestDataHolder getRequestDataHolder() {
        return requestDataHolder;
    }

    public ChannelHandlerContext getSourceContext() {
        return sourceContext;
    }

    public String getServerName() {
        return serverName;
    }

    public void setKeepAliveConfig(KeepAliveConfig config) {
        this.keepAliveConfig = config;
    }

    public SourceHandler getSourceHandler() {
        return sourceHandler;
    }
}
