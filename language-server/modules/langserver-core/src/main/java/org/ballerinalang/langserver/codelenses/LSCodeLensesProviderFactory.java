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

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Loads and provides the Code Lenses Providers.
 * 
 * @since 0.990.3
 */
public class LSCodeLensesProviderFactory {

    private static final LSCodeLensesProviderFactory provider = new LSCodeLensesProviderFactory();

    private List<LSCodeLensesProvider> providersList;

    private LSCodeLensesProviderFactory() {
        ServiceLoader<LSCodeLensesProvider> providers = ServiceLoader.load(LSCodeLensesProvider.class);
        providersList = new ArrayList<>();
        for (LSCodeLensesProvider executor : providers) {
            if (executor != null && executor.isEnabled()) {
                providersList.add(executor);
            }
        }
    }

    public static LSCodeLensesProviderFactory getInstance() {
        return provider;
    }

    /**
     * Get the list of all active providers.
     *
     * @return {@link List} Providers List
     */
    public List<LSCodeLensesProvider> getProviders() {
        return new ArrayList<>(this.providersList);
    }
}
