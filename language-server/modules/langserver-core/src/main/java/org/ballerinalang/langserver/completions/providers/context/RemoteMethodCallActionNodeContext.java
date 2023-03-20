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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.TypeResolverUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Handles the completions for the {@link RemoteMethodCallActionNode}.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class RemoteMethodCallActionNodeContext extends RightArrowActionNodeContext<RemoteMethodCallActionNode> {

    public RemoteMethodCallActionNodeContext() {
        super(RemoteMethodCallActionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, RemoteMethodCallActionNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (onSuggestClients(node, context)) {
            // Covers following:
            // 1. cl<cursor>->func()
            completionItems.addAll(this.expressionCompletions(context));
            this.sort(context, node, completionItems);
            return completionItems;
        }

        Optional<TypeSymbol> expressionType = Optional.empty();
        if (context.currentSemanticModel().isPresent() && context.currentDocument().isPresent()) {
            LinePosition linePosition = node.expression().location().lineRange().endLine();
            expressionType = context.currentSemanticModel().get()
                    .expectedType(context.currentDocument().get(), linePosition);
        }
        if (expressionType.isEmpty() || !SymbolUtil.isClient(expressionType.get())) {
            return Collections.emptyList();
        }

        if (onSuggestClientActions(node, context)) {
            /*
            Covers the following case where a is a client object and we suggest the remote actions
            (1) a -> g<cursor>
             */
            List<Symbol> clientActions = this.getClientActions(expressionType.get());
            completionItems.addAll(this.getCompletionItemList(clientActions, context));
        } else if (TypeResolverUtil.isInMethodCallParameterContext(node, context.getCursorPositionInTree())) {
            /*
             * Covers the following cases:
             * 1. a->func(<cursor>)
             * 2. a->func(mod1:<cursor>)
             */
            if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, context.getNodeAtCursor())) {
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) context.getNodeAtCursor();
                List<Symbol> exprEntries = QNameRefCompletionUtil.getExpressionContextEntries(context, qNameRef);
                List<LSCompletionItem> items = this.getCompletionItemList(exprEntries, context);
                completionItems.addAll(items);
            } else {
                completionItems.addAll(this.actionKWCompletions(context));
                completionItems.addAll(this.expressionCompletions(context));
                completionItems.addAll(this.getNamedArgExpressionCompletionItems(context, node));
            }
        }

        this.sort(context, node, completionItems);
        return completionItems;
    }

    private List<LSCompletionItem> getNamedArgExpressionCompletionItems(BallerinaCompletionContext context,
                                                                        RemoteMethodCallActionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();
        if (semanticModel.isEmpty()) {
            return completionItems;
        }
        Optional<Symbol> symbol = semanticModel.get().symbol(node);
        if (symbol.isEmpty() || !(symbol.get().kind() == SymbolKind.METHOD)) {
            return completionItems;
        }
        FunctionSymbol functionSymbol = (FunctionSymbol) symbol.get();
        return getNamedArgCompletionItems(context, functionSymbol, node.arguments());
    }

    private boolean onSuggestClientActions(RemoteMethodCallActionNode node, BallerinaCompletionContext context) {
        int cursor = context.getCursorPositionInTree();
        return node.rightArrowToken().textRange().endOffset() <= cursor &&
                (node.openParenToken().isMissing() || cursor <= node.openParenToken().textRange().startOffset());
    }

    private boolean onSuggestClients(RemoteMethodCallActionNode node, BallerinaCompletionContext context) {
        int cursor = context.getCursorPositionInTree();
        return cursor <= node.rightArrowToken().textRange().startOffset();
    }

    @Override
    public void sort(BallerinaCompletionContext context,
                     RemoteMethodCallActionNode node,
                     List<LSCompletionItem> completionItems) {
        if (TypeResolverUtil.isInMethodCallParameterContext(node, context.getCursorPositionInTree())) {
            super.sort(context, node, completionItems);
            return;
        }

        // At expression of the remote method call action, suggest clients first
        if (onSuggestClients(node, context)) {
            completionItems.forEach(completionItem -> {
                Optional<TypeSymbol> typeSymbol = SortingUtil.getSymbolFromCompletionItem(completionItem);
                String sortText;
                if (typeSymbol.isPresent() && SymbolUtil.isClient(typeSymbol.get())) {
                    sortText = SortingUtil.genSortText(1);
                } else {
                    sortText = SortingUtil.genSortText(2);
                }
                sortText += SortingUtil.genSortText(SortingUtil.toRank(context, completionItem));
                completionItem.getCompletionItem().setSortText(sortText);
            });
            return;
        }

        for (LSCompletionItem item : completionItems) {
            sortByAssignability(context, item, SortingUtil.toRank(context, item));
        }
    }
}
