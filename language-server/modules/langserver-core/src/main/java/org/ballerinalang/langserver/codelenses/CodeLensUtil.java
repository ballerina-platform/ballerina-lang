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

import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codelenses.LSCodeLensesProviderException;
import org.ballerinalang.langserver.commons.codelenses.spi.LSCodeLensesProvider;
import org.eclipse.lsp4j.CodeLens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides code lenses related common functionalities.
 *
 * @since 0.984.0
 */
public class CodeLensUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeLensUtil.class);

    /**
     * Compile and get code lenses.
     *
     * @param codeLensContext LSContext
     * @return a list of code lenses
     */
    public static List<CodeLens> getCodeLenses(LSContext codeLensContext) {
        List<CodeLens> lenses = new ArrayList<>();
        List<LSCodeLensesProvider> providers = LSCodeLensesProviderHolder.getInstance().getProviders();
        for (LSCodeLensesProvider provider : providers) {
            try {
                lenses.addAll(provider.getLenses(codeLensContext));
            } catch (LSCodeLensesProviderException e) {
                LOGGER.error("Error while retrieving lenses from: " + provider.getName());
            }
        }
        return lenses;
    }
}
