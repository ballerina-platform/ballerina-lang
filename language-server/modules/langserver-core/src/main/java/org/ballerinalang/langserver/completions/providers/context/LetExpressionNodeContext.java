/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import io.ballerina.compiler.syntax.tree.LetExpressionNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles the completions for {@link LetExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class LetExpressionNodeContext extends AbstractCompletionProvider<LetExpressionNode> {

    public LetExpressionNodeContext() {
        super(LetExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, LetExpressionNode node) 
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (node.letVarDeclarations().isEmpty()) {
            /*
            Covers the following context
            eg: let <cursor>
             */
            completionItems.addAll(this.getTypeDescContextItems(context));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_VAR.get()));
        }
        else if (node.inKeyword().isMissing()) {
            return Collections.singletonList(new SnippetCompletionItem(context, Snippet.KW_IN.get()));
        }

        this.sort(context, node, completionItems);
        return completionItems;
    }
    
    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, LetExpressionNode node) {
        int cursor = context.getCursorPositionInTree();
        return !node.letVarDeclarations().isEmpty() && node.inKeyword().isMissing()
                && node.letVarDeclarations().get(node.letVarDeclarations().size() - 1)
                .expression().textRange().endOffset() < cursor || node.letVarDeclarations().isEmpty();
    }

    @Override
    public void sort(BallerinaCompletionContext context, LetExpressionNode node, List<LSCompletionItem> lsCItems) {
        for (LSCompletionItem lsCItem : lsCItems) {
            CompletionItem completionItem = lsCItem.getCompletionItem();
            completionItem.setSortText(SortingUtil.genSortTextForTypeDescContext(context, lsCItem));
        }
    }
}
