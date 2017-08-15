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

package org.wso2.carbon.transport.http.netty.contractimpl;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.messaging.exceptions.MessagingException;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.HttpRoute;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.common.ssl.SSLConfig;
import org.wso2.carbon.transport.http.netty.contract.HTTPClientConnector;
import org.wso2.carbon.transport.http.netty.contract.HTTPClientConnectorFuture;
import org.wso2.carbon.transport.http.netty.listener.SourceHandler;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.sender.TargetHandler;
import org.wso2.carbon.transport.http.netty.sender.channel.ChannelUtils;
import org.wso2.carbon.transport.http.netty.sender.channel.TargetChannel;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of the client connector.
 */
public class HTTPClientConnectorImpl implements HTTPClientConnector {

    private static final Logger log = LoggerFactory.getLogger(HTTPClientConnector.class);

    private ConnectionManager connectionManager;
    private SSLConfig sslConfig;
    private int socketIdleTimeout;

    public HTTPClientConnectorImpl(ConnectionManager connectionManager, SSLConfig sslConfig, int socketIdleTimeout) {
        this.connectionManager = connectionManager;
        this.sslConfig = sslConfig;
        this.socketIdleTimeout = socketIdleTimeout;
    }

    @Override
    public HTTPClientConnectorFuture connect() {
        return null;
    }

    @Override
    public HTTPClientConnectorFuture send(HTTPCarbonMessage httpCarbonMessage) throws Exception {
        HTTPClientConnectorFuture httpClientConnectorFuture = new HTTPClientConnectorFutureImpl();

        // Fetch Host
        String host;
        Object hostProperty = httpCarbonMessage.getProperty(Constants.HOST);
        if (hostProperty != null && hostProperty instanceof String) {
            host = (String) hostProperty;
        } else {
            host = Constants.LOCALHOST;
            httpCarbonMessage.setProperty(Constants.HOST, Constants.LOCALHOST);
            log.debug("Cannot find property HOST of type string, hence using localhost as the host");
        }

        // Fetch Port
        int port;
        Object intProperty = httpCarbonMessage.getProperty(Constants.PORT);
        if (intProperty != null && intProperty instanceof Integer) {
            port = (int) intProperty;
        } else {
            port = sslConfig != null ?
                    Constants.DEFAULT_HTTPS_PORT : Constants.DEFAULT_HTTP_PORT;
            httpCarbonMessage.setProperty(Constants.PORT, port);
            log.debug("Cannot find property PORT of type integer, hence using " + port);
        }

        final HttpRoute route = new HttpRoute(host, port);

        SourceHandler srcHandler = (SourceHandler) httpCarbonMessage.getProperty(Constants.SRC_HANDLER);
        if (srcHandler == null) {
            if (log.isDebugEnabled()) {
                log.debug(Constants.SRC_HANDLER + " property not found in the message."
                        + " Message is not originated from the HTTP Server connector");
            }
        }

        try {
            TargetChannel targetChannel = connectionManager.borrowTargetChannel(route, srcHandler, sslConfig);
            targetChannel.getChannel().eventLoop().execute(() -> {
                Util.prepareBuiltMessageForTransfer(httpCarbonMessage);
                Util.setupTransferEncodingForRequest(httpCarbonMessage);
                if (targetChannel.getChannel() != null) {
                    targetChannel.setTargetHandler(targetChannel.getHTTPClientInitializer().getTargetHandler());
                    targetChannel.setCorrelatedSource(srcHandler);
                    targetChannel.setHttpRoute(route);
                    TargetHandler targetHandler = targetChannel.getTargetHandler();
                    targetHandler.setHttpClientConnectorFuture(httpClientConnectorFuture);
                    targetHandler.setListener(httpCarbonMessage.getResponseListener());
                    targetHandler.setIncomingMsg(httpCarbonMessage);
                    targetHandler.setConnectionManager(connectionManager);
                    targetHandler.setTargetChannel(targetChannel);
                    targetChannel.getChannel().pipeline().addBefore(Constants.TARGET_HANDLER,
                            Constants.IDLE_STATE_HANDLER,
                            new IdleStateHandler(socketIdleTimeout, socketIdleTimeout, 0,
                                    TimeUnit.MILLISECONDS));
                    targetChannel.setRequestWritten(true);
                    try {
                        HttpRequest httpRequest = Util.createHttpRequest(httpCarbonMessage);
                        ChannelUtils.writeContent(targetChannel.getChannel(), httpRequest, httpCarbonMessage);
                    } catch (Exception e) {
                        String msg = "Failed to send the request : " + e.getMessage().toLowerCase(Locale.ENGLISH);
                        log.error(msg, e);
                        MessagingException messagingException = new MessagingException(msg, e, 101500);
                        httpCarbonMessage.setMessagingException(messagingException);
                        httpClientConnectorFuture.notifyHTTPListener(httpCarbonMessage);
                    }
                }
            });
        } catch (Exception failedCause) {
            throw new ClientConnectorException(failedCause.getMessage(), failedCause);
        }

        return httpClientConnectorFuture;
    }

    @Override
    public boolean close() {
        return false;
    }
}
