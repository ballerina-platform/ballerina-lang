/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.commons.codelenses.spi;

import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codelenses.LSCodeLensesProviderException;
import org.eclipse.lsp4j.CodeLens;

import java.util.List;

/**
 * Represents the SPI interface for the Language Server Code Lenses Provider.
 * 
 * @since 0.990.3
 */
public interface LSCodeLensesProvider {
    /**
     * Returns name of the code lenses provider.
     *
     * @return name
     */
    String getName();

    /**
     * Execute the Command.
     *
     * @param context           Language Server Context
     * @return {@link List}     List of code lenses
     * @throws LSCodeLensesProviderException exception while executing the code lenses provider
     */
    List<CodeLens> getLenses(LSContext context) throws LSCodeLensesProviderException;

    /**
     * Mark code lenses provider is enabled or not.
     *
     * @return True when enabled, false otherwise
     */
    boolean isEnabled();
}
