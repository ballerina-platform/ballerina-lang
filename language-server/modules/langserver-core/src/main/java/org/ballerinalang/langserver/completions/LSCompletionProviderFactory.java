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
package org.ballerinalang.langserver.completions;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Loads and provides the Completion Providers.
 *
 * @since 0.990.4
 */
public class LSCompletionProviderFactory {

    private static final LSCompletionProviderFactory INSTANCE = new LSCompletionProviderFactory();

    private List<LSCompletionProvider> providersList = new ArrayList<>();

    private boolean isInitialized = false;

    private LSCompletionProviderFactory() {
        initiate();
    }

    public static LSCompletionProviderFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes the completions factory.
     */
    public void initiate() {
        if (isInitialized) {
            return;
        }
        ServiceLoader<LSCompletionProvider> providers = ServiceLoader.load(LSCompletionProvider.class);
        for (LSCompletionProvider provider : providers) {
            if (provider != null) {
                providersList.add(provider);
            }
        }
        isInitialized = true;
    }

    /**
     * Get the list of all providers.
     *
     * @return {@link List} Providers List
     */
    public List<LSCompletionProvider> getProviders() {
        return providersList;
    }

    /**
     * Add a completion provider.
     *
     * @param provider completion provider to register
     */
    public void register(LSCompletionProvider provider) {
        this.providersList.add(provider);
    }

    /**
     * Remove completion provider.
     *
     * @param provider completion provider to unregister
     */
    public void unregister(LSCompletionProvider provider) {
        this.providersList.remove(provider);
    }
}
