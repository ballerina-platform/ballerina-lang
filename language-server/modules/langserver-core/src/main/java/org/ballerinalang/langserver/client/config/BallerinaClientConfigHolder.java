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
package org.ballerinalang.langserver.client.config;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is holding the latest client configuration.
 */
public class BallerinaClientConfigHolder {
    private static final BallerinaClientConfigHolder INSTANCE = new BallerinaClientConfigHolder();

    private List<ConfigChangeListener> listeners = new ArrayList<>();

    // Init ballerina client configuration with defaults
    private BallerinaClientConfig clientConfig = BallerinaClientConfig.getDefault();

    private BallerinaClientConfigHolder() {
    }

    public static BallerinaClientConfigHolder getInstance() {
        return INSTANCE;
    }

    /**
     * Returns current client configuration.
     *
     * @return {@link BallerinaClientConfig}
     */
    public BallerinaClientConfig getConfig() {
        return this.clientConfig;
    }

    /**
     * Register config listener.
     *
     * @param listener  Config change listener to register
     */
    public void register(ConfigChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Unregister config listener.
     *
     * @param listener  Config change listener to unregister
     */
    public void unregister(ConfigChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Update current client configuration.
     *
     * @param newConfig {@link BallerinaClientConfig} new configuration
     */
    public void updateConfig(BallerinaClientConfig newConfig) {
        // Update config
        BallerinaClientConfig oldConfig = clientConfig;
        clientConfig = newConfig;
        // Notify listeners
        listeners.stream().parallel().forEach(listener -> listener.didChange(oldConfig, newConfig));
    }
}
