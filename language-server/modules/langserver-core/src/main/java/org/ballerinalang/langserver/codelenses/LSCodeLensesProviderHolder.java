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

import org.ballerinalang.langserver.commons.codelenses.spi.LSCodeLensesProvider;
import org.ballerinalang.langserver.compiler.config.LSClientConfigHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Loads and provides the Code Lenses Providers.
 * 
 * @since 0.990.3
 */
public class LSCodeLensesProviderHolder {

    private static final List<LSCodeLensesProvider> providers = new ArrayList<>();

    private static final LSCodeLensesProviderHolder INSTANCE = new LSCodeLensesProviderHolder();

    private boolean isEnabled = true;

    private LSCodeLensesProviderHolder() {
        ServiceLoader<LSCodeLensesProvider> providers = ServiceLoader.load(LSCodeLensesProvider.class);
        for (LSCodeLensesProvider executor : providers) {
            if (executor != null && executor.isEnabled()) {
                LSCodeLensesProviderHolder.providers.add(executor);
            }
        }
        LSClientConfigHolder.getInstance().register((oldConfig, newConfig) -> {
            this.isEnabled = newConfig.getCodeLens().getAll().isEnabled();
        });
    }

    public static LSCodeLensesProviderHolder getInstance() {
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
     * Get the list of all active providers.
     *
     * @return {@link List} Providers List
     */
    public List<LSCodeLensesProvider> getProviders() {
        List<LSCodeLensesProvider> activeProviders = new ArrayList<>();
        for (LSCodeLensesProvider provider : providers) {
            if (provider != null && provider.isEnabled()) {
                activeProviders.add(provider);
            }
        }
        return activeProviders;
    }
}
