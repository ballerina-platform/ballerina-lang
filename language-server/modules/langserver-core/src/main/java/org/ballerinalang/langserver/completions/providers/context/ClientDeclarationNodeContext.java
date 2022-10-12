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
import io.ballerina.compiler.syntax.tree.ClientDeclarationNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion provider for {@link ClientDeclarationNode} context.
 *
 * @since 2201.3.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ClientDeclarationNodeContext extends AbstractCompletionProvider<ClientDeclarationNode> {

    public ClientDeclarationNodeContext() {
        super(ClientDeclarationNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context,
                                                 ClientDeclarationNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        if (this.onSuggestClientUri(context, node)) {
            return completionItems;
        } else if (this.onSuggestAsKeyword(context, node)) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_AS.get()));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private boolean onSuggestClientUri(BallerinaCompletionContext context, ClientDeclarationNode node) {
        int cursor = context.getCursorPositionInTree();
        BasicLiteralNode clientUri = node.clientUri();

        return node.clientKeyword().textRange().endOffset() < cursor 
                && node.asKeyword().textRange().startOffset() > cursor
                && (clientUri.isMissing() || cursor <= clientUri.textRange().endOffset());
    }

    private boolean onSuggestAsKeyword(BallerinaCompletionContext context, ClientDeclarationNode node) {
        int cursor = context.getCursorPositionInTree();
        BasicLiteralNode clientUri = node.clientUri();

        return !clientUri.isMissing() && cursor > clientUri.textRange().endOffset() && node.asKeyword().isMissing();
    }
}
