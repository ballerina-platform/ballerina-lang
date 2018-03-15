/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.transport.http.netty.sender;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contract.ClientConnectorException;

/**
 * A future to check the connection availability.
 */
public class ConnectionAvailabilityFuture {

    private ChannelFuture socketAvailabilityFuture;
    private boolean isSSLEnabled = false;
    private ConnectionAvailabilityListener listener = null;
    private String protocol;
    private boolean socketAvailable = false;
    private boolean isFailure;
    private Throwable throwable;

    public void setSocketAvailabilityFuture(ChannelFuture socketAvailabilityFuture) {
        this.socketAvailabilityFuture = socketAvailabilityFuture;
        socketAvailabilityFuture.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (isValidChannel(channelFuture)) {
                    socketAvailable = true;
                    if (listener != null && !isSSLEnabled) {
                        notifySuccess(Constants.HTTP_SCHEME);
                    }
                } else {
                    notifyFailure(channelFuture.cause());
                }
            }

            private boolean isValidChannel(ChannelFuture channelFuture) {
                return (channelFuture.isDone() && channelFuture.isSuccess());
            }
        });
    }

    void setSSLEnabled(boolean sslEnabled) {
        isSSLEnabled = sslEnabled;
    }

    void notifySuccess(String protocol) {
        this.protocol = protocol;
        if (listener != null) {
            listener.onSuccess(protocol, socketAvailabilityFuture);
        }
    }

    void notifyFailure(Throwable cause) {
        isFailure = true;
        throwable = cause;
        if (listener != null) {
            notifyErrorState(socketAvailabilityFuture, cause);
        }
    }

    public void setListener(ConnectionAvailabilityListener listener) {
        this.listener = listener;
        if (protocol != null) {
            notifySuccess(protocol);
        } else if (!isSSLEnabled && socketAvailable) {
            notifySuccess(Constants.HTTP_SCHEME);
        } else if (isFailure) {
            notifyFailure(throwable);
        }
    }

    private void notifyErrorState(ChannelFuture channelFuture, Throwable cause) {
        ClientConnectorException connectorException;
        String socketAddress = null;
        if (channelFuture.channel().remoteAddress() != null) {
            socketAddress = channelFuture.channel().remoteAddress().toString();
        }
        if (channelFuture.isDone() && channelFuture.isCancelled()) {
            connectorException = new ClientConnectorException("Request cancelled: " + socketAddress,
                    HttpResponseStatus.BAD_GATEWAY.code());
        } else if (!channelFuture.isDone() && !channelFuture.isSuccess() && !channelFuture.isCancelled() && (
                channelFuture.cause() == null)) {
            connectorException = new ClientConnectorException("Connection timeout: " + socketAddress,
                    HttpResponseStatus.BAD_GATEWAY.code());
        } else if (cause.toString().contains("javax.net.ssl")) {
            connectorException = new ClientConnectorException(cause.getMessage() + socketAddress,
                    HttpResponseStatus.BAD_GATEWAY.code());
        } else {
            connectorException = new ClientConnectorException(channelFuture.cause().getMessage(),
                    HttpResponseStatus.BAD_GATEWAY.code());
        }
        if (channelFuture.cause() != null) {
            connectorException.initCause(channelFuture.cause());
        }
        listener.onFailure(connectorException);
    }
}

