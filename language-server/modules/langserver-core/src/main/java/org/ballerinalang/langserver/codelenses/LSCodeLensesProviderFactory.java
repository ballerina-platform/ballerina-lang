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
package org.ballerinalang.langserver.codelenses;

import org.ballerinalang.langserver.client.config.BallerinaClientConfigHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Loads and provides the Code Lenses Providers.
 * 
 * @since 0.990.3
 */
public class LSCodeLensesProviderFactory {

    private static final LSCodeLensesProviderFactory INSTANCE = new LSCodeLensesProviderFactory();

    private List<LSCodeLensesProvider> providersList = new ArrayList<>();

    private boolean isEnabled = true;

    private boolean isInitialized = false;

    private LSCodeLensesProviderFactory() {
        initiate();
    }

    public static LSCodeLensesProviderFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Returns True if enabled, False otherwise.
     *
     * @return True if enabled
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Initializes the code lenses factory.
     */
    public void initiate() {
        if (isInitialized) {
            return;
        }
        ServiceLoader<LSCodeLensesProvider> providers = ServiceLoader.load(LSCodeLensesProvider.class);
        for (LSCodeLensesProvider executor : providers) {
            if (executor != null && executor.isEnabled()) {
                providersList.add(executor);
            }
        }
        BallerinaClientConfigHolder.getInstance().register((oldConfig, newConfig) -> {
            isEnabled = newConfig.getCodeLens().getAll().isEnabled();
        });
        isInitialized = true;
    }

    /**
     * Get the list of all active providers.
     *
     * @return {@link List} Providers List
     */
    public List<LSCodeLensesProvider> getProviders() {
        List<LSCodeLensesProvider> activeProviders = new ArrayList<>();
        for (LSCodeLensesProvider provider : providersList) {
            if (provider != null && provider.isEnabled()) {
                activeProviders.add(provider);
            }
        }
        return activeProviders;
    }

    /**
     * Add a code lens provider.
     *
     * @param provider  code lens provider to register
     */
    public void register(LSCodeLensesProvider provider) {
        this.providersList.add(provider);
    }

    /**
     * Remove code lens provider.
     *
     * @param provider  code lens provider to unregister
     */
    public void unregister(LSCodeLensesProvider provider) {
        this.providersList.remove(provider);
    }
}
