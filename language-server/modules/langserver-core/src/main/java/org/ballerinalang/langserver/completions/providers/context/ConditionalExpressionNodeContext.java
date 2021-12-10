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

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ConditionalExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link ConditionalExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ConditionalExpressionNodeContext extends AbstractCompletionProvider<ConditionalExpressionNode> {

    public ConditionalExpressionNodeContext() {
        super(ConditionalExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, ConditionalExpressionNode node)
            throws LSCompletionException {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        List<LSCompletionItem> completionItems = new ArrayList<>();

        if (isMiddleExpressionQNameRef(node)) {
            /*
            Covers the following context
            eg: int n = true ? module1:
             */
            String middleExprName = ((SimpleNameReferenceNode) node.middleExpression()).name().text();
            List<Symbol> expressionContextSymbols = 
                    QNameReferenceUtil.getExpressionContextEntries(context, middleExprName);
            if (expressionContextSymbols.isEmpty()) {
                completionItems.addAll(this.expressionCompletions(context));
            } else {
                completionItems.addAll(this.getCompletionItemList(expressionContextSymbols, context));
            }
        } else if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            List<Symbol> expressionContextSymbols =
                    QNameReferenceUtil.getExpressionContextEntries(context, (QualifiedNameReferenceNode) nodeAtCursor);
            completionItems.addAll(this.getCompletionItemList(expressionContextSymbols, context));
        } else {
            completionItems.addAll(this.expressionCompletions(context));
        }
        this.sort(context, node, completionItems);
        return completionItems;
    }
    
    private boolean isMiddleExpressionQNameRef(ConditionalExpressionNode node) {
        Node middleExpression = node.middleExpression();
        Node endExpression = node.endExpression();
        return !middleExpression.isMissing() && middleExpression.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE 
                && !endExpression.isMissing() && endExpression.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE;
    }

    @Override
    public void sort(BallerinaCompletionContext context, ConditionalExpressionNode node,
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
