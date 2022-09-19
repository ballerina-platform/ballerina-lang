/*
 * Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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

import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ModuleClientDeclarationNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Completion provider for {@link ModuleClientDeclarationNode} context.
 *
 * @since 2201.3.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ModuleClientDeclarationNodeContext extends AbstractCompletionProvider<ModuleClientDeclarationNode> {

    public ModuleClientDeclarationNodeContext() {
        super(ModuleClientDeclarationNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context,
                                                 ModuleClientDeclarationNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        if (this.onSuggestClientUri(context, node)) {
            return Collections.emptyList();
        } else if (this.onSuggestAsKeyword(context, node)) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_AS.get()));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private boolean onSuggestClientUri(BallerinaCompletionContext context, ModuleClientDeclarationNode node) {
        int cursor = context.getCursorPositionInTree();
        Token clientKeyword = node.clientKeyword();
        Token asKeyword = node.asKeyword();
        BasicLiteralNode clientUri = node.clientUri();

        return clientKeyword.textRange().endOffset() < cursor && asKeyword.textRange().startOffset() > cursor
                && (clientUri.isMissing() || cursor < clientUri.textRange().endOffset() + 1);
    }

    private boolean onSuggestAsKeyword(BallerinaCompletionContext context, ModuleClientDeclarationNode node) {
        int cursor = context.getCursorPositionInTree();
        BasicLiteralNode clientUri = node.clientUri();
        Token asKeyword = node.asKeyword();

        return !clientUri.isMissing() && cursor >= clientUri.textRange().endOffset() + 1 && asKeyword.isMissing();
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, ModuleClientDeclarationNode node) {
        int cursor = context.getCursorPositionInTree();
        Token clientKeyword = node.clientKeyword();

        return !clientKeyword.isMissing() && cursor > clientKeyword.textRange().endOffset()
                && cursor <= node.semicolonToken().textRange().endOffset();
    }
}
