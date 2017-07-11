/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.runtime.model;

import org.ballerinalang.runtime.config.BLangConfigurationManager;
import org.ballerinalang.runtime.config.BallerinaConfiguration;
import org.ballerinalang.runtime.config.ServerConnectorConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds Ballerina runtime model.
 */
public class BLangRuntimeRegistry {

    private static final BLangRuntimeRegistry instance = new BLangRuntimeRegistry();

    private Map<String, ServerConnector> serverConnectorMap = new HashMap<>();

    // Caching values to avoid null check.
    private Map<String, Boolean> interceptionEnabledServerConnectorMap = new HashMap<>();

    private BLangRuntimeRegistry() {
    }

    public static BLangRuntimeRegistry getInstance() {
        return instance;
    }

    public ServerConnector getServerConnector(String protocol) {
        return serverConnectorMap.get(protocol);
    }

    public void addServerConnector(ServerConnector serverConnector) {
        this.serverConnectorMap.put(serverConnector.getProtocol(), serverConnector);
        this.interceptionEnabledServerConnectorMap.put(serverConnector.getProtocol(),
                serverConnector.isEnableInterceptors());
    }

    public boolean isInterceptionEnabled(String protocol) {
        return Boolean.TRUE.equals(interceptionEnabledServerConnectorMap.get(protocol));
    }

    public void initServerConnectors() {
        for (ServerConnector serverConnector : serverConnectorMap.values()) {
            serverConnector.init();
        }
    }

    public void initialize() {
        BallerinaConfiguration configuration = BLangConfigurationManager.getInstance().getConfiguration();
        for (ServerConnectorConfig serverConnectorConfig : configuration.getServerConnectors()) {
            ServerConnector serverConnector = new ServerConnector.ServerConnectorBuilder(serverConnectorConfig).build();
            this.addServerConnector(serverConnector);
        }
        initServerConnectors();
    }

    public void clear() {
        this.interceptionEnabledServerConnectorMap.clear();
        this.serverConnectorMap.clear();
    }
}
