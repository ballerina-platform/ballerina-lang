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

package org.wso2.transport.http.netty.contractimpl.sender;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.exceptions.ClientConnectorException;
import org.wso2.transport.http.netty.contract.exceptions.ConnectionTimedOutException;
import org.wso2.transport.http.netty.contract.exceptions.InvalidProtocolException;
import org.wso2.transport.http.netty.contract.exceptions.RequestCancelledException;
import org.wso2.transport.http.netty.contract.exceptions.SslException;
import org.wso2.transport.http.netty.contract.exceptions.UnresolvedHostException;

import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;

import static org.wso2.transport.http.netty.contract.Constants.COLON;
import static org.wso2.transport.http.netty.contract.Constants.ERROR_COULD_NOT_RESOLVE_HOST;
import static org.wso2.transport.http.netty.contract.Constants.SECURITY;
import static org.wso2.transport.http.netty.contract.Constants.SSL;
import static org.wso2.transport.http.netty.contract.Constants.SSL_CONNECTION_ERROR;

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
    private boolean forceHttp2 = false;

    public void setSocketAvailabilityFuture(ChannelFuture socketAvailabilityFuture) {
        this.socketAvailabilityFuture = socketAvailabilityFuture;
        socketAvailabilityFuture.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (isValidChannel(channelFuture)) {
                    socketAvailable = true;
                    if (listener != null && !isSSLEnabled) {
                        if (forceHttp2) {
                            notifySuccess(Constants.HTTP2_CLEARTEXT_PROTOCOL);
                        } else {
                            notifySuccess(Constants.HTTP_SCHEME);
                        }
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

    public void setForceHttp2(boolean forceHttp2) {
        this.forceHttp2 = forceHttp2;
    }

    void notifySuccess(String protocol) {
        this.protocol = protocol;
        if (listener != null) {
            if (forceHttp2 && !(protocol.equalsIgnoreCase(Constants.HTTP2_CLEARTEXT_PROTOCOL) ||
                    protocol.equalsIgnoreCase(Constants.HTTP2_TLS_PROTOCOL))) {
                ClientConnectorException connectorException =
                        new InvalidProtocolException("Protocol must be HTTP/2",
                                HttpResponseStatus.HTTP_VERSION_NOT_SUPPORTED.code());
                listener.onFailure(connectorException);
            } else {
                listener.onSuccess(protocol, socketAvailabilityFuture);
            }
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
            if (forceHttp2) {
                notifySuccess(Constants.HTTP2_CLEARTEXT_PROTOCOL);
            } else {
                notifySuccess(Constants.HTTP_SCHEME);
            }
        } else if (isFailure) {
            notifyFailure(throwable);
        }
    }

    private void notifyErrorState(ChannelFuture channelFuture, Throwable cause) {
        String socketAddress = null;
        if (channelFuture.channel().remoteAddress() != null) {
            socketAddress = channelFuture.channel().remoteAddress().toString();
        }
        ClientConnectorException connectorException =
                createSpecificExceptionFromGeneric(channelFuture, cause, socketAddress);

        listener.onFailure(connectorException);
    }

    private ClientConnectorException createSpecificExceptionFromGeneric(ChannelFuture channelFuture, Throwable cause,
                                                                        String socketAddress) {
        ClientConnectorException connectorException;
        if (isRequestCancelled(channelFuture)) {
            connectorException = new RequestCancelledException("Request cancelled: " + socketAddress,
                                                               HttpResponseStatus.BAD_GATEWAY.code());
        } else if (isConnectionTimeout(channelFuture)) {
            connectorException = new ConnectionTimedOutException("Connection timeout: " + socketAddress,
                                                                 HttpResponseStatus.BAD_GATEWAY.code());
        } else if (isSslException(cause)) {
            connectorException = new SslException(SSL_CONNECTION_ERROR + COLON + cause.getMessage()
                                                          + " " + socketAddress, HttpResponseStatus.BAD_GATEWAY.code());
        } else if (cause instanceof UnknownHostException) {
            connectorException = new UnresolvedHostException(ERROR_COULD_NOT_RESOLVE_HOST + COLON +
                    cause.getMessage(), HttpResponseStatus.BAD_GATEWAY.code());
        } else if (cause instanceof ClosedChannelException) {
            connectorException = new ClientConnectorException("Remote host: " + socketAddress
                    + " closed the connection while SSL handshake", HttpResponseStatus.BAD_GATEWAY.code());
        } else {
            connectorException = handleInGenericWay(channelFuture);
        }

        if (channelFuture.cause() != null) {
            connectorException.initCause(channelFuture.cause());
        }
        return connectorException;
    }

    private boolean isRequestCancelled(ChannelFuture channelFuture) {
        return channelFuture.isDone() && channelFuture.isCancelled();
    }

    private ClientConnectorException handleInGenericWay(ChannelFuture channelFuture) {
        ClientConnectorException connectorException;
        if (channelFuture.cause() != null) {
            connectorException = new ClientConnectorException(channelFuture.cause().getMessage(),
                                                              HttpResponseStatus.BAD_GATEWAY.code());
        } else {
            connectorException = new ClientConnectorException("Generic client error",
                    HttpResponseStatus.BAD_GATEWAY.code());
        }
        return connectorException;
    }

    private boolean isSslException(Throwable cause) {
        return cause.toString().contains(SSL) || cause.toString().contains(SECURITY);
    }

    private boolean isConnectionTimeout(ChannelFuture channelFuture) {
        return !channelFuture.isDone() && !channelFuture.isSuccess() && !channelFuture.isCancelled() && (
                channelFuture.cause() == null);
    }
}
