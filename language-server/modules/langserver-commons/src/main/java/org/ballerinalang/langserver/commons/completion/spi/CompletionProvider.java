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

import io.ballerinalang.compiler.syntax.tree.Node;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Interface for completion item providers.
 *
 * @param <T> generic syntax tree node
 * @since 1.2.0
 */
public interface CompletionProvider<T extends Node> {

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
    @Deprecated
    List<LSCompletionItem> getCompletions(LSContext context) throws LSCompletionException;

    /**
     * Get Completion items for the scope/ context.
     *
     * @param context Language Server Context
     * @param node    Node instance for the parser context
     * @return {@link List}     List of calculated Completion Items
     * @throws LSCompletionException when completion fails
     */
    default List<LSCompletionItem> getCompletions(LSContext context, T node) throws LSCompletionException {
        return new ArrayList<>();
    }

    /**
     * Get the attachment points where the current provider attached to.
     *
     * @return {@link List}    List of attachment points
     */
    List<Class<T>> getAttachmentPoints();

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
    Optional<CompletionProvider<T>> getContextProvider(LSContext ctx);

    /**
     * Pre-validation is used during the completion provider selection phase.
     * Based on the node type, specific providers can add validation checks to fine tune the selection. With the parser
     * recovery phase there are predefined heuristics which might not optimal for the auto completion phase. Therefore
     * further validations (if needed) for the node can be added here. If this validation has not met, corresponding
     * provider will be skipped
     *
     * @param node    Node to evaluate
     * @param context Language server completion operation context
     * @return {@link Boolean} pre-validation status
     */
    boolean onPreValidation(LSContext context, T node);
}
