/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.transport.http.netty.sender.channel;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.common.HttpRoute;
import org.wso2.carbon.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import java.net.ConnectException;

/**
 * A class that get executed when the connection is created.
 */
public class TargetChannelListener implements ChannelFutureListener {

    private static final Logger log = LoggerFactory.getLogger(TargetChannelListener.class);

    private TargetChannel targetChannel;
    private HTTPCarbonMessage httpCarbonRequest;
    private HttpRoute httpRoute;
    private int socketIdleTimeout;
    private HttpResponseFuture httpResponseFuture;

    public TargetChannelListener (TargetChannel targetChannel, HTTPCarbonMessage httpCarbonRequest,
            int socketIdleTimeout, HttpResponseFuture httpResponseFuture) {
        this.targetChannel = targetChannel;
        this.httpCarbonRequest = httpCarbonRequest;
        this.httpRoute = targetChannel.getHttpRoute();
        this.socketIdleTimeout = socketIdleTimeout;
        this.httpResponseFuture = httpResponseFuture;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        if (isValidateChannel(channelFuture)) {
            targetChannel.setChannel(channelFuture.channel());

            targetChannel.configTargetHandler(httpCarbonRequest, httpResponseFuture);
            targetChannel.setEndPointTimeout(socketIdleTimeout);
            targetChannel.setCorrelationIdForLogging();

            targetChannel.setRequestWritten(true);
            targetChannel.writeContent(httpCarbonRequest);
        } else {
            notifyErrorState(channelFuture);
        }
    }

    private boolean isValidateChannel(ChannelFuture channelFuture) throws Exception {
        if (channelFuture.isDone() && channelFuture.isSuccess()) {
            if (log.isDebugEnabled()) {
                log.debug("Created the connection to address: {}", httpRoute.toString());
            }
            return true;
        }
        return false;
    }

    private void notifyErrorState(ChannelFuture channelFuture) {
        if (channelFuture.isDone() && channelFuture.isCancelled()) {
            ConnectException cause = new ConnectException("Request Cancelled, " + httpRoute.toString());
            if (channelFuture.cause() != null) {
                cause.initCause(channelFuture.cause());
            }
            this.targetChannel.getTargetHandler().getHttpResponseFuture().notifyHttpListener(cause);
        } else if (!channelFuture.isDone() && !channelFuture.isSuccess() &&
                !channelFuture.isCancelled() && (channelFuture.cause() == null)) {
            ConnectException cause = new ConnectException("Connection timeout, " + httpRoute.toString());
            this.targetChannel.getTargetHandler().getHttpResponseFuture().notifyHttpListener(cause);
        } else {
            ConnectException cause = new ConnectException("Connection refused, " + httpRoute.toString());
            if (channelFuture.cause() != null) {
                cause.initCause(channelFuture.cause());
            }
            this.targetChannel.getTargetHandler().getHttpResponseFuture().notifyHttpListener(cause);
        }
    }
}
