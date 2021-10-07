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
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link MethodCallExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class MethodCallExpressionNodeContext extends FieldAccessContext<MethodCallExpressionNode> {

    public MethodCallExpressionNodeContext() {
        super(MethodCallExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, MethodCallExpressionNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (this.withinParameterContext(context, node)) {
            if (QNameReferenceUtil.onQualifiedNameIdentifier(context, context.getNodeAtCursor())) {
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) context.getNodeAtCursor();
                List<Symbol> exprEntries = QNameReferenceUtil.getExpressionContextEntries(context, qNameRef);
                List<LSCompletionItem> items = this.getCompletionItemList(exprEntries, context);
                completionItems.addAll(items);
            } else {
                completionItems.addAll(this.actionKWCompletions(context));
                completionItems.addAll(this.expressionCompletions(context));
            }
        } else {
            ExpressionNode expression = node.expression();
            completionItems = getEntries(context, expression);
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, MethodCallExpressionNode node) {
        /*
        Supports the following only
        eg:
        (1) abc.def.test<cursor>Method()
        (2) abc.def.testMethod(<cursor>)
        With this check, the following example also will come to the methodCall navigating through the parent 
        hierarchy and skip properly
        eg:
        (3) s<cursor>abc.def.testMethod()
        (4) self.<cursor>Method()
         */
        int cursor = context.getCursorPositionInTree();
        NameReferenceNode nameRef = node.methodName();
        Token dotToken = node.dotToken();

        return ((cursor >= nameRef.textRange().startOffset() && cursor <= nameRef.textRange().endOffset())
                || (!dotToken.isMissing() && cursor > dotToken.textRange().startOffset()));
    }

    private boolean withinParameterContext(BallerinaCompletionContext context, MethodCallExpressionNode node) {
        int cursor = context.getCursorPositionInTree();
        Token openParen = node.openParenToken();
        Token closeParen = node.closeParenToken();

        return cursor > openParen.textRange().startOffset() && cursor < closeParen.textRange().endOffset();
    }

    @Override
    public void sort(BallerinaCompletionContext context,
                     MethodCallExpressionNode node,
                     List<LSCompletionItem> completionItems) {
        boolean withinParameterCtx = withinParameterContext(context, node);
        for (LSCompletionItem completionItem : completionItems) {
            int rank;
            // Here, we want to rank methods/functions first
            switch (completionItem.getType()) {
                case OBJECT_FIELD:
                case RECORD_FIELD:
                    rank = withinParameterCtx ? 1 : 2;
                    break;
                case SYMBOL:
                    Optional<Symbol> symbol = ((SymbolCompletionItem) completionItem).getSymbol();
                    if (symbol.stream().anyMatch(sym -> sym.kind() == SymbolKind.METHOD)) {
                        rank = withinParameterCtx ? 3 : 1;
                        break;
                    } else if (symbol.stream().anyMatch(sym -> sym.kind() == SymbolKind.FUNCTION)) {
                        rank = withinParameterCtx ? 3 : 1;
                        break;
                    } else if (symbol.stream().anyMatch(sym -> sym.kind() == SymbolKind.XMLNS)) {
                        rank = withinParameterCtx ? 2 : 3;
                        break;
                    } else {
                        rank = SortingUtil.toRank(context, completionItem, 3);
                        break;
                    }
                default:
                    rank = SortingUtil.toRank(context, completionItem, 3);
            }

            sortByAssignability(context, completionItem, rank);
        }
    }
}
