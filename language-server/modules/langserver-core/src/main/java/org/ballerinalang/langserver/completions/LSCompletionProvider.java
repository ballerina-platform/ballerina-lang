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

import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.resolvers.CompletionItemsContext;
import org.eclipse.lsp4j.CompletionItem;

import java.util.List;

/**
 * Represents the SPI interface for the Language Server Completions Provider.
 *
 * @since 0.990.4
 */
public interface LSCompletionProvider {
    /**
     * Returns name of the completions provider.
     *
     * @return name
     */
    String getName();

    /**
     * Retrieving completions.
     *
     * @param context                Language Server Context
     * @param completionItemsContext Completion Item Context
     * @return {@link List}     List of completions
     * @throws LSCompletionProviderException exception while executing the completions provider
     */
    List<CompletionItem> getCompletions(LSContext context, CompletionItemsContext completionItemsContext)
            throws LSCompletionProviderException;
}
