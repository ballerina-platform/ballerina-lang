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

import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.LetExpressionNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.CompleteExpressionValidator;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.CompletionItem;

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
        List<LSCompletionItem> completionItems;
        SeparatedNodeList<LetVariableDeclarationNode> letVarDeclarations = node.letVarDeclarations();
        if (letVarDeclarations.isEmpty() || !letVarDeclarations.isEmpty()
                && letVarDeclarations.get(letVarDeclarations.size() - 1).textRange().length() == 0) {
            /*
            Covers the following context
            eg: let <cursor>
                let int a = b, <cursor>
                let int a = b, int c = d, <cursor>
             */
            completionItems = this.getTypeDescContextItems(context);
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_VAR.get()));
        } else if (onSuggestInKeyword(context, node)) {
            return Collections.singletonList(new SnippetCompletionItem(context, Snippet.KW_IN.get()));
        } else {
            return CompletionUtil.route(context, node.parent());
        }

        this.sort(context, node, completionItems);
        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, LetExpressionNode node) {
        int cursor = context.getCursorPositionInTree();

        SeparatedNodeList<LetVariableDeclarationNode> letVarDeclarations = node.letVarDeclarations();

        if (letVarDeclarations.isEmpty() || !letVarDeclarations.isEmpty()
                && letVarDeclarations.get(letVarDeclarations.size() - 1).textRange().length() == 0) {
            return true;
        }

        CompleteExpressionValidator validator = new CompleteExpressionValidator();
        return !letVarDeclarations.isEmpty() && node.inKeyword().isMissing()
                && letVarDeclarations.get(letVarDeclarations.size() - 1).expression().textRange().endOffset() <= cursor;
    }

    @Override
    public void sort(BallerinaCompletionContext context, LetExpressionNode node, List<LSCompletionItem> lsCItems) {
        for (LSCompletionItem lsCItem : lsCItems) {
            CompletionItem completionItem = lsCItem.getCompletionItem();
            completionItem.setSortText(SortingUtil.genSortTextForTypeDescContext(context, lsCItem));
        }
    }

    private boolean onSuggestInKeyword(BallerinaCompletionContext context, LetExpressionNode node) {
        SeparatedNodeList<LetVariableDeclarationNode> letVarDecls = node.letVarDeclarations();
        int cursor = context.getCursorPositionInTree();

        CompleteExpressionValidator validator = new CompleteExpressionValidator();
        if (letVarDecls.isEmpty()) {
            return false;
        }
        ExpressionNode expression = letVarDecls.get(letVarDecls.size() - 1).expression();
        boolean completed = expression.apply(validator);
        Node evalNode;
        /*
        Note:
        Added the following to special case the binary expression's resolving in the parser
        eg: let x = a i<cursor>
        Parser resolves the above as a binary expression where the + is missing. In order to make the incomplete
        expression capturing logic generic, we added the following special cased check.
         */
        if (expression.kind() == SyntaxKind.BINARY_EXPRESSION && completed
                && ((BinaryExpressionNode) expression).operator().isMissing()) {
            evalNode = ((BinaryExpressionNode) expression).lhsExpr();
        } else {
            evalNode = expression;
        }
        return node.inKeyword().isMissing() && completed && evalNode.textRange().endOffset() < cursor;
    }
}
