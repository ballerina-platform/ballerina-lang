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
 */
package org.wso2.carbon.transport.http.netty.sender;

import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.HttpRoute;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.config.ConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.listener.SourceHandler;
import org.wso2.carbon.transport.http.netty.sender.channel.BootstrapConfiguration;
import org.wso2.carbon.transport.http.netty.sender.channel.ChannelUtils;
import org.wso2.carbon.transport.http.netty.sender.channel.TargetChannel;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * HTTP client connector class which is used for sending message to a backend endpoint.
 *
 * @since 4.0.0
 */
public class HTTPClientConnector implements ClientConnector {

    private static final Logger log = LoggerFactory.getLogger(HTTPClientConnector.class);
    private ConnectionManager connectionManager;
    private Map<String, SenderConfiguration> senderConfigurations;

    public HTTPClientConnector() {
        TransportsConfiguration transportsConfiguration = ConfigurationBuilder.getInstance().getConfiguration();
        init(transportsConfiguration.getSenderConfigurations(), transportsConfiguration.getTransportProperties());
    }

    public HTTPClientConnector(Set<SenderConfiguration> senderConfiguration,
                               Set<TransportProperty> transportPropertiesSet) {
        init(senderConfiguration, transportPropertiesSet);
    }

    private void init(Set<SenderConfiguration> senderConfiguration, Set<TransportProperty> transportPropertiesSet) {
        if (senderConfiguration.isEmpty()) {
            log.error("Please specify at least one sender configuration");
            return;
        }
        senderConfigurations = senderConfiguration.stream().collect(Collectors
                .toMap(senderConf -> senderConf.getScheme().toLowerCase(Locale.getDefault()), config -> config));

        Map<String, Object> transportProperties = new HashMap<>();

        if (transportPropertiesSet != null && !transportPropertiesSet.isEmpty()) {
            transportProperties = transportPropertiesSet.stream().collect(
                    Collectors.toMap(TransportProperty::getName, TransportProperty::getValue));

        }

        BootstrapConfiguration.createBootStrapConfiguration(transportProperties);
        this.connectionManager = ConnectionManager.getInstance(transportProperties);
    }

    @Override
    public boolean send(CarbonMessage msg, CarbonCallback callback, Map<String, String> parameters)
            throws ClientConnectorException {
        return send(msg, callback);
    }

    @Override
    public boolean send(CarbonMessage msg, CarbonCallback callback) throws ClientConnectorException {

        String protocol = (String) msg.getProperty(Constants.PROTOCOL);
        SenderConfiguration senderConfiguration = senderConfigurations.get(protocol.toLowerCase(Locale.getDefault()));

        Util.prepareBuiltMessageForTransfer(msg);
        Util.setupTransferEncodingForRequest(msg);

        final HttpRequest httpRequest = Util.createHttpRequest(msg);

        // Fetch Host
        String host;
        Object hostProperty = msg.getProperty(Constants.HOST);
        if (hostProperty != null && hostProperty instanceof String) {
            host = (String) hostProperty;
        } else {
            host = Constants.LOCALHOST;
            msg.setProperty(Constants.HOST, Constants.LOCALHOST);
            log.debug("Cannot find property HOST of type string, hence using localhost as the host");
        }

        // Fetch Port
        int port;
        Object intProperty = msg.getProperty(Constants.PORT);
        if (intProperty != null && intProperty instanceof Integer) {
            port = (int) intProperty;
        } else {
            port = senderConfiguration.getSslConfig() != null ?
                   Constants.DEFAULT_HTTPS_PORT : Constants.DEFAULT_HTTP_PORT;
            msg.setProperty(Constants.PORT, port);
            log.debug("Cannot find property PORT of type integer, hence using " + port);
        }

        final HttpRoute route = new HttpRoute(host, port);

        SourceHandler srcHandler = (SourceHandler) msg.getProperty(Constants.SRC_HANDLER);
        if (srcHandler == null) {
            log.debug("SRC_HANDLER property not found in the message." +
                      " Message is not originated from the HTTP Server connector");
        }

        try {
            TargetChannel targetChannel = connectionManager.
                    getTargetChannel(route, srcHandler, senderConfiguration, httpRequest, msg, callback);
            if (targetChannel != null) {
                TargetHandler targetHandler = targetChannel.getTargetHandler();
                targetHandler.setCallback(callback);
                targetHandler.setIncomingMsg(msg);
                targetHandler.setConnectionManager(connectionManager);
                targetHandler.setTargetChannel(targetChannel);
                if (ChannelUtils.writeContent(targetChannel.getChannel(), httpRequest, msg)) {
                    targetChannel.setRequestWritten(true); // If request written
                }
            }
        } catch (Exception failedCause) {
            throw new ClientConnectorException(failedCause.getMessage(), failedCause);
        }

        return false;
    }

    @Override
    public String getProtocol() {
        //hardcoded because there is always one sender with set of configurations
        return Constants.PROTOCOL_NAME;
    }

    public void setMessageProcessor(CarbonMessageProcessor carbonMessageProcessor) {
        HTTPTransportContextHolder.getInstance().setMessageProcessor(carbonMessageProcessor);
    }
}
