/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
package io.ballerina.projects.plugins.completion;

import io.ballerina.compiler.syntax.tree.Node;

import java.util.List;

/**
 * Interface for completion item providers.
 *
 * @param <T> generic syntax tree node.
 * @since 2201.7.0
 */
public interface CompletionProvider<T extends Node> {

    /**
     * Get the name of the completion provider.
     *
     * @return {@link String}   Name of the completion provider
     */
    String name();

    /**
     * Get Completion items for the scope/ context.
     *
     * @param context completion operation Context
     * @param node    Node instance for the parser context
     * @return {@link List}     List of calculated Completion Items
     * @throws CompletionException when completion fails
     */
    List<CompletionItem> getCompletions(CompletionContext context, T node) throws CompletionException;

    /**
     * Get the attachment points where the current provider attached to.
     *
     * @return {@link List}    List of attachment points
     */
    List<Class<T>> getSupportedNodes();
}
