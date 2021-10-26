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

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link LetVariableDeclarationNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class LetVariableDeclarationNodeContext extends AbstractCompletionProvider<LetVariableDeclarationNode> {

    public LetVariableDeclarationNodeContext() {
        super(LetVariableDeclarationNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, LetVariableDeclarationNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        int cursor = context.getCursorPositionInTree();
        if (node.typedBindingPattern().typeDescriptor().textRange().endOffset() >= cursor) {
            /*
            Covers the following context
            eg: let va<cursor>
                let int a = b, f<cursor>
             */
            completionItems.addAll(this.getTypeDescContextItems(context));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_VAR.get()));
            for (LSCompletionItem lsCItem : completionItems) {
                CompletionItem completionItem = lsCItem.getCompletionItem();
                completionItem.setSortText(SortingUtil.genSortTextForTypeDescContext(context, lsCItem));
            }            
            return completionItems;
        }
        
        /*
        Covers the following context
        eg: let var x = <cursor>
            let var x = h<cursor>
            let var x = mod1:<cursor>
         */
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();

        if (nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            /*
            Covers the cases where the cursor is within the expression context
             */
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            List<Symbol> exprEntries = QNameReferenceUtil.getExpressionContextEntries(context, qNameRef);

            completionItems.addAll(this.getCompletionItemList(exprEntries, context));
        } else {
            completionItems.addAll(this.expressionCompletions(context));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, LetVariableDeclarationNode node) {
        int cursor = context.getCursorPositionInTree();
        Token equalsToken = node.equalsToken();
        ExpressionNode expression = node.expression();
        /*
        Following is a special case check added for the example here
        eg: 
        1) from var person in personList
                let var test = 12 s<cursor>
        Here at the cursor, it is identified as the binary expression where the operator is missing
         */
        if (!expression.isMissing() && expression.kind() == SyntaxKind.BINARY_EXPRESSION
                && cursor > ((BinaryExpressionNode) expression).lhsExpr().textRange().endOffset()) {
            return false;
        } else if (node.typedBindingPattern().typeDescriptor().textRange().endOffset() >= cursor) {
            return true;
        }
        return !equalsToken.isMissing() && equalsToken.textRange().startOffset() < cursor
                && (expression.isMissing() || cursor <= expression.textRange().endOffset());
    }
    @Override
    public void sort(BallerinaCompletionContext context, LetVariableDeclarationNode node,
                     List<LSCompletionItem> completionItems) {
        Optional<TypeSymbol> typeSymbolAtCursor = context.getContextType();
        if (typeSymbolAtCursor.isEmpty()) {
            super.sort(context, node, completionItems);
            return;
        }
        TypeSymbol symbol = typeSymbolAtCursor.get();
        for (LSCompletionItem completionItem : completionItems) {
            completionItem.getCompletionItem()
                    .setSortText(SortingUtil.genSortTextByAssignability(context, completionItem, symbol));
        }
    }
}
