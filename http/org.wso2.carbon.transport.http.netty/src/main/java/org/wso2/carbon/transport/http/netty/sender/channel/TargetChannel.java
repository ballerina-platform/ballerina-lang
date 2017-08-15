/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.transport.http.netty.sender.channel;


import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.timeout.IdleStateHandler;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.HttpRoute;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.listener.SourceHandler;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.sender.HTTPClientInitializer;
import org.wso2.carbon.transport.http.netty.sender.TargetHandler;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.util.concurrent.TimeUnit;

/**
 * A class that encapsulate channel and state.
 */
public class TargetChannel {

    private Channel channel;
    private TargetHandler targetHandler;
    private HTTPClientInitializer httpClientInitializer;
    private HttpRoute httpRoute;
    private SourceHandler correlatedSource;
    private boolean isRequestWritten = false;

    public Channel getChannel() {
        return channel;
    }

    public TargetChannel setChannel(Channel channel) {
        this.channel = channel;
        return this;
    }

    public TargetHandler getTargetHandler() {
        return targetHandler;
    }

    public void setTargetHandler(TargetHandler targetHandler) {
        this.targetHandler = targetHandler;
    }

    public HTTPClientInitializer getHTTPClientInitializer() {
        return httpClientInitializer;
    }

    public void setHTTPClientInitializer(HTTPClientInitializer clientInitializer) {
        this.httpClientInitializer = clientInitializer;
    }

    public HttpRoute getHttpRoute() {
        return httpRoute;
    }

    public void setHttpRoute(HttpRoute httpRoute) {
        this.httpRoute = httpRoute;
    }

    public SourceHandler getCorrelatedSource() {
        return correlatedSource;
    }

    public void setCorrelatedSource(SourceHandler correlatedSource) {
        this.correlatedSource = correlatedSource;
    }

    public boolean isRequestWritten() {
        return isRequestWritten;
    }

    public void setRequestWritten(boolean isRequestWritten) {
        this.isRequestWritten = isRequestWritten;
    }

    public void configure(HTTPCarbonMessage httpCarbonMessage, SourceHandler srcHandler) {
        this.setTargetHandler(this.getHTTPClientInitializer().getTargetHandler());
        this.setCorrelatedSource(srcHandler);
        TargetHandler targetHandler = this.getTargetHandler();
        //                    targetHandler.setHttpClientConnectorFuture(httpClientConnectorFuture);
        targetHandler.setListener(httpCarbonMessage.getResponseListener());
        targetHandler.setIncomingMsg(httpCarbonMessage);
        targetHandler.setTargetChannel(this);
    }

    public void setEndPointTimeout(int socketIdleTimeout) {
        this.getChannel().pipeline().addBefore(Constants.TARGET_HANDLER,
                Constants.IDLE_STATE_HANDLER, new IdleStateHandler(socketIdleTimeout, socketIdleTimeout, 0,
                        TimeUnit.MILLISECONDS));
    }

    public boolean writeContent(HTTPCarbonMessage carbonMessage) {
        HttpRequest httpRequest = Util.createHttpRequest(carbonMessage);
        this.setRequestWritten(true);
        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor().
                    executeAtTargetRequestReceiving(carbonMessage);
        }
        this.getChannel().write(httpRequest);

        while (true) {
            if (carbonMessage.isEndOfMsgAdded() && carbonMessage.isEmpty()) {
                this.getChannel().writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                break;
            }
            HttpContent httpContent = carbonMessage.getHttpContent();
            if (httpContent instanceof LastHttpContent) {
                this.getChannel().writeAndFlush(httpContent);
                if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
                    HTTPTransportContextHolder.getInstance().getHandlerExecutor().
                            executeAtTargetRequestSending(carbonMessage);
                }
                break;
            }
            if (httpContent != null) {
                this.getChannel().write(httpContent);
            }
        }

        return true;
    }
}
