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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.tools.text.TextRange;
import io.ballerinalang.compiler.syntax.tree.TypeDefinitionNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion Provider for {@link TypeDefinitionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class TypeDefinitionNodeContext extends AbstractCompletionProvider<TypeDefinitionNode> {
    public TypeDefinitionNodeContext() {
        super(TypeDefinitionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, TypeDefinitionNode node)
            throws LSCompletionException {
        return new ArrayList<>(typeDescriptorCItems(context));
    }

    private List<LSCompletionItem> typeDescriptorCItems(LSContext context) {
        // Add the type names
        List<LSCompletionItem> typeItems = this.getTypeItems(context);
        // Add the special snippets
        typeItems.add(new SnippetCompletionItem(context, Snippet.DEF_ERROR_TYPE_DESC.get()));
        typeItems.add(new SnippetCompletionItem(context, Snippet.DEF_RECORD_TYPE_DESC.get()));
        typeItems.add(new SnippetCompletionItem(context, Snippet.DEF_CLOSED_RECORD_TYPE_DESC.get()));
        typeItems.add(new SnippetCompletionItem(context, Snippet.DEF_OBJECT_TYPE_DESC_SNIPPET.get()));

        return typeItems;
    }

    @Override
    public boolean onPreValidation(LSContext context, TypeDefinitionNode node) {
        TextRange typeKWRange = node.typeKeyword().textRange();
        int cursorPosition = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);

        return typeKWRange.endOffset() < cursorPosition;
    }
}
