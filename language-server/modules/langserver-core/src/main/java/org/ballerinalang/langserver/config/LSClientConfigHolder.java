/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.ballerinalang.langserver.commons.LanguageServerContext;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is holding the latest client configuration.
 */
public class LSClientConfigHolder {
    private final Gson gson = new Gson();
    private static final LanguageServerContext.Key<LSClientConfigHolder> CLIENT_CONFIG_HOLDER_KEY =
            new LanguageServerContext.Key<>();

    private final List<ClientConfigListener> listeners = new ArrayList<>();

    // Init ballerina client configuration with defaults
    private LSClientConfig clientConfig = LSClientConfig.getDefault();
    private JsonElement clientConfigJson = gson.toJsonTree(this.clientConfig).getAsJsonObject();

    private LSClientConfigHolder(LanguageServerContext serverContext) {
        serverContext.put(CLIENT_CONFIG_HOLDER_KEY, this);
    }

    public static LSClientConfigHolder getInstance(LanguageServerContext serverContext) {
        LSClientConfigHolder lsClientConfigHolder = serverContext.get(CLIENT_CONFIG_HOLDER_KEY);
        if (lsClientConfigHolder == null) {
            lsClientConfigHolder = new LSClientConfigHolder(serverContext);
        }

        return lsClientConfigHolder;
    }

    /**
     * Returns current client configuration.
     *
     * @return {@link LSClientConfig}
     */
    public LSClientConfig getConfig() {
        return this.clientConfig;
    }

    /**
     * Returns current client configuration.
     *
     * @return {@link T}
     */
    public <T> T getConfigAs(Class<T> type) {
        return gson.fromJson(clientConfigJson, (Type) type);
    }

    /**
     * Register config listener.
     *
     * @param listener Config change listener to register
     */
    public void register(ClientConfigListener listener) {
        listeners.add(listener);
    }

    /**
     * Unregister config listener.
     *
     * @param listener Config change listener to unregister
     */
    public void unregister(ClientConfigListener listener) {
        listeners.remove(listener);
    }

    /**
     * Update current client configuration.
     *
     * @param newClientConfigJson {@link JsonElement} new configuration
     */
    public void updateConfig(JsonElement newClientConfigJson) {
        LSClientConfig newClientConfig = gson.fromJson(newClientConfigJson, LSClientConfig.class);
        // Update config
        LSClientConfig oldClientConfig = this.clientConfig;
        JsonElement oldClientConfigJson = this.clientConfigJson;
        this.clientConfig = newClientConfig;
        this.clientConfigJson = newClientConfigJson;
        // Notify listeners
        this.listeners.forEach(listener -> listener.didChangeConfig(oldClientConfig, newClientConfig));
        this.listeners.forEach(listener -> listener.didChangeJson(oldClientConfigJson, newClientConfigJson));
    }
}
