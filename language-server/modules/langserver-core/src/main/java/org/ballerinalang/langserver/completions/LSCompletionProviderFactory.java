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

import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Loads and provides the Completion Providers.
 *
 * @since 0.990.4
 */
public class LSCompletionProviderFactory {

    private static final LSCompletionProviderFactory INSTANCE = new LSCompletionProviderFactory();
    
    private Map<Class, LSCompletionProvider> providers;

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
        this.providers = new HashMap<>();
        ServiceLoader<LSCompletionProvider> providerServices = ServiceLoader.load(LSCompletionProvider.class);
        for (LSCompletionProvider provider : providerServices) {
            if (provider != null) {
                for (Class attachmentPoint : provider.getAttachmentPoints()) {
                    if (!this.providers.containsKey(attachmentPoint)
                            || (this.providers.get(attachmentPoint).getPrecedence() == 
                            LSCompletionProvider.Precedence.LOW
                            && provider.getPrecedence() == LSCompletionProvider.Precedence.HIGH)) {
                        this.providers.put(attachmentPoint, provider);
                    }
                }
            }
        }
        isInitialized = true;
    }

    /**
     * Add a completion provider.
     *
     * @param provider completion provider to register
     */
    public void register(LSCompletionProvider provider) {
        for (Class attachmentPoint : provider.getAttachmentPoints()) {
            this.providers.put(attachmentPoint, provider);
        }
    }

    /**
     * Remove completion provider.
     *
     * @param provider completion provider to unregister
     */
    public void unregister(LSCompletionProvider provider) {
        for (Class attachmentPoint : provider.getAttachmentPoints()) {
            this.providers.remove(attachmentPoint, provider);
        }
    }

    public Map<Class, LSCompletionProvider> getProviders() {
        return this.providers;
    }

    /**
     * Get Provider by Class key.
     *
     * @param key   Provider key
     * @return {@link LSCompletionProvider} Completion provider  
     */
    public LSCompletionProvider getProvider(Class key) {
        return this.providers.get(key);
    }
}
