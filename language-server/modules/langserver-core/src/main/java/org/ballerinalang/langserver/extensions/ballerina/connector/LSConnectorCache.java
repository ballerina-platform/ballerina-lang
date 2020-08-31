/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langserver.extensions.ballerina.connector;

import com.google.gson.JsonElement;
import org.ballerinalang.langserver.LSGlobalContext;
import org.ballerinalang.langserver.commons.LSContext;

import java.util.HashMap;

/**
 * Cache to contain connector information.
 */
public class LSConnectorCache {

    private static final LSContext.Key<LSConnectorCache> LS_CONNECTOR_CACHE_KEY =
            new LSContext.Key<>();

    private static final Object LOCK = new Object();

    HashMap<String, JsonElement> connectorConfigs;

    public static LSConnectorCache getInstance(LSGlobalContext context) {
        LSConnectorCache lsConnectorCache = context.get(LS_CONNECTOR_CACHE_KEY);
        if (lsConnectorCache == null) {
            synchronized (LOCK) {
                lsConnectorCache = context.get(LS_CONNECTOR_CACHE_KEY);
                if (lsConnectorCache == null) {
                    lsConnectorCache = new LSConnectorCache();
                    context.put(LS_CONNECTOR_CACHE_KEY, lsConnectorCache);
                }
            }
        }
        return lsConnectorCache;
    }

    private LSConnectorCache() {
        connectorConfigs = new HashMap<>();
    }

    public void addConnectorConfig(String org, String module, String version, String connector,
                                   JsonElement connectorConfig) {
        this.connectorConfigs.put(createKey(org, module, version, connector), connectorConfig);
    }

    public JsonElement getConnectorConfig(String org, String module, String version, String connector) {
        return this.connectorConfigs.get(createKey(org, module, version, connector));
    }

    private String createKey(String org, String module, String version, String connector) {
        return org + "-" + module + "-" + version + "-" + connector;
    }
}
