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

import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion Provider for {@link FunctionBodyBlockNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class FunctionBodyBlockNodeContext extends BlockNodeContextProvider<FunctionBodyBlockNode> {
    public FunctionBodyBlockNodeContext() {
        super(FunctionBodyBlockNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, FunctionBodyBlockNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>(super.getCompletions(context, node));
        NonTerminalNode nodeAtCursor = context.get(CompletionKeys.NODE_AT_CURSOR_KEY);
        if (nodeAtCursor.kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_WORKER.get()));
        }

        return completionItems;
    }

    @Override
    public boolean onPreValidation(LSContext context, FunctionBodyBlockNode node) {
        return !node.openBraceToken().isMissing() && !node.closeBraceToken().isMissing();
    }
}
