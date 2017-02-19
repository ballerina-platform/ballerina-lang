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

package org.wso2.carbon.transport.http.netty.sender;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.MessageProcessorException;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.HttpRoute;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.common.ssl.SSLConfig;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;
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
 * A class creates connections with BE and send messages.
 * @deprecated use {@link HTTPClientConnector} instead.
 */
@Deprecated
public class HTTPSender implements TransportSender {

    private static final Logger log = LoggerFactory.getLogger(HTTPSender.class);
    private ConnectionManager connectionManager;
    private Map<String, SenderConfiguration> senderConfigurationMap;

    public HTTPSender(Set<SenderConfiguration> senderConfiguration, Set<TransportProperty> transportPropertiesSet) {
        if (senderConfiguration.isEmpty()) {
            log.error("Please specify at least one sender configuration");
            return;
        }
        senderConfigurationMap = senderConfiguration.stream().collect(Collectors
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
    public boolean send(CarbonMessage msg, CarbonCallback callback) throws MessageProcessorException {
        String protocol = (String) msg.getProperty(Constants.PROTOCOL);
        SenderConfiguration defaultSenderConfiguration = senderConfigurationMap
                .get(protocol.toLowerCase(Locale.getDefault()));

        final HttpRequest httpRequest = Util.createHttpRequest(msg);

        if (msg.getProperty(Constants.HOST) == null) {
            log.debug("Cannot find property HOST hence using default as " + "localhost"
                    + " Please specify remote host as 'HOST' in carbon message property ");
            msg.setProperty(Constants.HOST, "localhost");
        }
        if (msg.getProperty(Constants.PORT) == null) {
            SSLConfig sslConfig = defaultSenderConfiguration.getSslConfig();
            int port = 80;
            if (sslConfig != null) {
                port = 443;
            }
            log.debug("Cannot find property PORT hence using default as " + port
                    + " Please specify remote host as 'PORT' in carbon message property ");
            msg.setProperty(Constants.PORT, port);
        }

        final HttpRoute route = new HttpRoute((String) msg.getProperty(Constants.HOST),
                (Integer) msg.getProperty(Constants.PORT));

        SourceHandler srcHandler = (SourceHandler) msg.getProperty(Constants.SRC_HANDLER);
        if (srcHandler == null) {
            log.debug("Cannot find property SRC_HANDLER hence Sender uses as standalone.If you need to use sender with"
                    + "listener side please copy property SRC_HANDLER from incoming message");
        }

        Channel outboundChannel = null;
        try {
            TargetChannel targetChannel = connectionManager
                    .getTargetChannel(route, srcHandler, defaultSenderConfiguration, httpRequest, msg, callback);
            if (targetChannel != null) {
                outboundChannel = targetChannel.getChannel();
                targetChannel.getTargetHandler().setCallback(callback);
                targetChannel.getTargetHandler().setIncomingMsg(msg);
                targetChannel.getTargetHandler().setTargetChannel(targetChannel);
                targetChannel.getTargetHandler().setConnectionManager(connectionManager);
                boolean written = ChannelUtils.writeContent(outboundChannel, httpRequest, msg);
                if (written) {
                    targetChannel.setRequestWritten(true);
                }
            }
        } catch (Exception failedCause) {
            throw new MessageProcessorException(failedCause.getMessage(), failedCause);
        }

        return false;
    }

    @Override
    public String getId() {
        //hardcoded because there is always one sender with set of configurations
        return "http/s";
    }

}
