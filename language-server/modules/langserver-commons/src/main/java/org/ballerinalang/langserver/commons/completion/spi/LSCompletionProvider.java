/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.commons.completion.spi;

import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.eclipse.lsp4j.CompletionItem;

import java.util.List;
import java.util.Optional;

/**
 * Interface for completion item providers.
 *
 * @since 1.2.0
 */
public interface LSCompletionProvider {

    /**
     * Precedence for a given provider.
     *
     * @since 1.0
     */
    enum Precedence {
        LOW,
        HIGH
    }

    /**
     * Get Completion items for the scope/ context.
     *
     * @param context Language Server Context
     * @return {@link List}     List of calculated Completion Items
     * @throws LSCompletionException when completion fails
     */
    List<CompletionItem> getCompletions(LSContext context) throws LSCompletionException;

    /**
     * Get the attachment points where the current provider attached to.
     *
     * @return {@link List}    List of attachment points
     */
    List<Class> getAttachmentPoints();

    /**
     * Get the precedence of the provider.
     * 
     * @return {@link Precedence} precedence of the provider
     */
    Precedence getPrecedence();

    /**
     * Get the Context Provider.
     * Ex: When a given scope is resolved then the context can be resolved by parsing a sub rule or token analyzing
     *
     * @param ctx Language Server Context
     * @return {@link Optional} Context Completion provider
     */
    Optional<LSCompletionProvider> getContextProvider(LSContext ctx);
}
