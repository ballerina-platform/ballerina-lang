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

import io.ballerina.compiler.syntax.tree.Node;
import org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Loads and provides the Completion Providers.
 *
 * @since 0.990.4
 */
public class ProviderFactory {

    private static final Map<Class<?>, BallerinaCompletionProvider<Node>> providers = new HashMap<>();

    private static final ProviderFactory INSTANCE = new ProviderFactory();

    private ProviderFactory() {
        @SuppressWarnings("rawtypes")
        ServiceLoader<BallerinaCompletionProvider> providerServices =
                ServiceLoader.load(BallerinaCompletionProvider.class);
        for (BallerinaCompletionProvider<Node> provider : providerServices) {
            if (provider == null) {
                continue;
            }
            for (Class<?> attachmentPoint : provider.getAttachmentPoints()) {
                if (!providers.containsKey(attachmentPoint) ||
                        (providers.get(attachmentPoint).getPrecedence() == BallerinaCompletionProvider.Precedence.LOW
                                && provider.getPrecedence() == BallerinaCompletionProvider.Precedence.HIGH)) {
                    providers.put(attachmentPoint, provider);
                }
            }
        }
    }

    public static ProviderFactory instance() {
        return INSTANCE;
    }

    /**
     * Add a completion provider.
     *
     * @param provider completion provider to register
     */
    public void register(BallerinaCompletionProvider<Node> provider) {
        for (Class<?> attachmentPoint : provider.getAttachmentPoints()) {
            providers.put(attachmentPoint, provider);
        }
    }

    /**
     * Remove completion provider.
     *
     * @param provider completion provider to unregister
     */
    public void unregister(BallerinaCompletionProvider<?> provider) {
        for (Class<?> attachmentPoint : provider.getAttachmentPoints()) {
            providers.remove(attachmentPoint, provider);
        }
    }

    public Map<Class<?>, BallerinaCompletionProvider<Node>> getProviders() {
        return providers;
    }

    /**
     * Get Provider by Class key.
     *
     * @param key Provider key
     * @return {@link BallerinaCompletionProvider} Completion provider
     */
    public BallerinaCompletionProvider<?> getProvider(Class<?> key) {
        return providers.get(key);
    }
}
