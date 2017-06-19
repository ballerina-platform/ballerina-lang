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
import org.wso2.carbon.transport.http.netty.config.ConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@code HTTPServerConnectorProvider} is responsible for providing and managing HTTP Listeners
 */
public class HTTPServerConnectorProvider extends ServerConnectorProvider {

    public static final String HOST = "Host";
    public static final String PORT = "Port";
    public static final String SCHEMA = "Schema";
    public static final String KEY_STORE_FILE = "keyStoreFile";
    public static final String KEY_STORE_PASS = "keyStorePass";
    public static final String CERT_PASS = "certPass";

    public HTTPServerConnectorProvider() {
        super(Constants.PROTOCOL_NAME);
    }

    public List<ServerConnector> initializeConnectors(TransportsConfiguration trpConfig) {

        List<ServerConnector> connectors = new ArrayList<>();

        ServerConnectorController serverConnectorController = new ServerConnectorController();

        //This logic assumes this will be called before any other code initialize the ServerConnectorController
        if (!serverConnectorController.isInitialized()) {
            serverConnectorController.initialize(trpConfig);
        }

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
    public List<ServerConnector> initializeConnectors() {
        return initializeConnectors(ConfigurationBuilder.getInstance().getConfiguration());
    }

    @Override
    public ServerConnector createConnector(String s, Map<String, String> properties) {
        TransportsConfiguration trpConfig = ConfigurationBuilder.getInstance().getConfiguration();

        Set<ListenerConfiguration> configSet = new HashSet<>();
        ListenerConfiguration config = buildListenerConfig(s, properties);
        configSet.add(config);
        trpConfig.setListenerConfigurations(configSet);

        ServerConnectorController serverConnectorController = new ServerConnectorController();
        if (!serverConnectorController.isInitialized()) {
            if (!serverConnectorController.initialize(trpConfig)) {
                trpConfig = serverConnectorController.getTransportsConfiguration();
                trpConfig.getListenerConfigurations().add(config);
            }
        } else {
            trpConfig = serverConnectorController.getTransportsConfiguration();
            trpConfig.getListenerConfigurations().add(config);
        }

        HTTPServerConnector connector = new HTTPServerConnector(config.getId());
        connector.setListenerConfiguration(config);
        connector.setServerConnectorController(serverConnectorController);
        if (config.isBindOnStartup()) {
            serverConnectorController.bindInterface(connector);
        }
        return connector;
    }

    private ListenerConfiguration buildListenerConfig(String id, Map<String, String> properties) {
        String host = properties.get(HOST) != null ? properties.get(HOST) : "0.0.0.0";
        int port = Integer.parseInt(properties.get(PORT));
        ListenerConfiguration config = new ListenerConfiguration(id, host, port);
        String schema = properties.get(SCHEMA);
        if (schema != null && schema.equals("https")) {
            config.setScheme(schema);
            config.setKeyStoreFile(properties.get(KEY_STORE_FILE));
            config.setKeyStorePass(properties.get(KEY_STORE_PASS));
            config.setCertPass(properties.get(CERT_PASS));
            //todo fill truststore stuff
        }
        return config;
    }
}
