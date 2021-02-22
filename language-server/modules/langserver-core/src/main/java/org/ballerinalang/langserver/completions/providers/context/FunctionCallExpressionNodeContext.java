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

import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion Provider for {@link FunctionCallExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class FunctionCallExpressionNodeContext extends BlockNodeContextProvider<FunctionCallExpressionNode> {
    public FunctionCallExpressionNodeContext() {
        super(FunctionCallExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext ctx, FunctionCallExpressionNode node)
            throws LSCompletionException {
        if (this.onQualifiedNameIdentifier(ctx, ctx.getNodeAtCursor())) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) ctx.getNodeAtCursor();
            return this.getCompletionItemList(QNameReferenceUtil.getExpressionContextEntries(ctx, qNameRef), ctx);
        }
        List<LSCompletionItem> completionItems = new ArrayList<>();
        completionItems.addAll(this.actionKWCompletions(ctx));
        completionItems.addAll(this.expressionCompletions(ctx));
        // TODO: implement the following
//        completionItems.addAll(this.getNewExprCompletionItems(ctx, node));
        completionItems.add(new SnippetCompletionItem(ctx, Snippet.KW_IS.get()));

        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, FunctionCallExpressionNode node) {
        int cursor = context.getCursorPositionInTree();
        Token openParen = node.openParenToken();
        Token closeParen = node.closeParenToken();

        return cursor > openParen.textRange().startOffset() && cursor < closeParen.textRange().endOffset();
    }
}
