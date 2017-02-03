/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.transport.http.netty.listener;

import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.ServerConnectorProvider;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * {@code HTTPServerConnectorProvider} is responsible for providing and managing HTTP Listeners
 */
public class HTTPServerConnectorProvider extends ServerConnectorProvider {

    public HTTPServerConnectorProvider() {
        super(Constants.PROTOCOL_NAME);
    }

    @Override
    public List<ServerConnector> initializeConnectors() {

        List<ServerConnector> connectors = new ArrayList<>();

        TransportsConfiguration trpConfig = YAMLTransportConfigurationBuilder.build();
        ServerConnectorController serverConnectorController = new ServerConnectorController(trpConfig);

        Set<ListenerConfiguration> listenerConfigurationSet = trpConfig.getListenerConfigurations();

        listenerConfigurationSet.forEach(config -> {
            HTTPServerConnector connector = new HTTPServerConnector(config.getId());
            connector.setListenerConfiguration(config);
            connector.setServerConnectorController(serverConnectorController);
            if (config.isBindOnStartup()) {
                serverConnectorController.bindInterface(connector);
            }
            connectors.add(connector);
        });

        return connectors;
    }

    @Override
    public ServerConnector createConnector(String s) {
        return null;
    }
}
