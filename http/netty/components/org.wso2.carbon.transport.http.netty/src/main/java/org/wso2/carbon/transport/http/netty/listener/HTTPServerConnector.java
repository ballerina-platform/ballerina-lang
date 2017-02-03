/**
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.transport.http.netty.listener;

import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ListeningServerConnector;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;

/**
 * HTTP ServerConnector implementation
 */
public class HTTPServerConnector extends ListeningServerConnector {

    private static final Logger log = LoggerFactory.getLogger(HTTPServerConnector.class);

    private ChannelFuture channelFuture;

    private ListenerConfiguration listenerConfiguration;

    private ServerConnectorController serverConnectorController;

    public HTTPServerConnector(String id) {
        super(id);
    }

    @Override
    public boolean bind() {
        if (listenerConfiguration.isBindOnStartup()) { // Already bind at the startup, hence skipping
            return false;
        }

        return serverConnectorController.bindInterface(this);
    }

    @Override
    public boolean unbind() {
        return serverConnectorController.unBindInterface(this);
    }

    @Override
    public void start() {
        log.info("Starting  HTTP Transport Listener");
    }

    @Override
    public void stop() {
    }

    @Override
    public void beginMaintenance() {
    }

    @Override
    public void endMaintenance() {
    }

    @Override
    public void setMessageProcessor(CarbonMessageProcessor carbonMessageProcessor) {
        HTTPTransportContextHolder.getInstance().setMessageProcessor(carbonMessageProcessor);
    }

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public void setChannelFuture(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
    }

    public ListenerConfiguration getListenerConfiguration() {
        return listenerConfiguration;
    }

    public void setListenerConfiguration(ListenerConfiguration listenerConfiguration) {
        this.listenerConfiguration = listenerConfiguration;
    }

    public void setServerConnectorController(
            ServerConnectorController serverConnectorController) {
        this.serverConnectorController = serverConnectorController;
    }
}
