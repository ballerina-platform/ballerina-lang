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

import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.codelenses.spi.LSCodeLensesProvider;
import org.ballerinalang.langserver.config.ClientConfigListener;
import org.ballerinalang.langserver.config.LSClientConfig;
import org.ballerinalang.langserver.config.LSClientConfigHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Loads and provides the Code Lenses Providers.
 *
 * @since 0.990.3
 */
public class LSCodeLensesProviderHolder {
    private static final List<LSCodeLensesProvider> codeLenses = new ArrayList<>();
    private static final LanguageServerContext.Key<LSCodeLensesProviderHolder> CODE_LENSES_PROVIDER_HOLDER_KEY =
            new LanguageServerContext.Key<>();
    private final LanguageServerContext serverContext;

    private boolean isEnabled = true;

    private LSCodeLensesProviderHolder(LanguageServerContext serverContext) {
        serverContext.put(CODE_LENSES_PROVIDER_HOLDER_KEY, this);
        loadServices();
        LSClientConfigHolder.getInstance(serverContext).register(new ClientConfigListener() {
            @Override
            public void didChangeConfig(LSClientConfig oldConfig, LSClientConfig newConfig) {
                isEnabled = newConfig.getCodeLens().getAll().isEnabled();
            }
        });
        this.serverContext = serverContext;
    }

    private void loadServices() {
        if (!LSCodeLensesProviderHolder.codeLenses.isEmpty()) {
            return;
        }
        ServiceLoader<LSCodeLensesProvider> providers = ServiceLoader.load(LSCodeLensesProvider.class);
        for (LSCodeLensesProvider codeLens : providers) {
            if (codeLens == null) {
                continue;
            }
            LSCodeLensesProviderHolder.codeLenses.add(codeLens);
        }
    }

    public static LSCodeLensesProviderHolder getInstance(LanguageServerContext serverContext) {
        LSCodeLensesProviderHolder lsCodeLensesProviderHolder = serverContext.get(CODE_LENSES_PROVIDER_HOLDER_KEY);
        if (lsCodeLensesProviderHolder == null) {
            lsCodeLensesProviderHolder = new LSCodeLensesProviderHolder(serverContext);
        }

        return lsCodeLensesProviderHolder;
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
     * Get the list of all active providers.
     *
     * @return {@link List} Providers List
     */
    public List<LSCodeLensesProvider> getProviders() {
        List<LSCodeLensesProvider> activeProviders = new ArrayList<>();
        for (LSCodeLensesProvider provider : codeLenses) {
            if (provider != null && provider.isEnabled(this.serverContext)) {
                activeProviders.add(provider);
            }
        }
        return activeProviders;
    }
}
