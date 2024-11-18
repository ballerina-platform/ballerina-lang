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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.Document;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.NameUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SpreadCompletionItem;
import org.ballerinalang.langserver.completions.builder.SpreadCompletionItemBuilder;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link ListConstructorExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ListConstructorExpressionNodeContext extends AbstractCompletionProvider<ListConstructorExpressionNode> {
    public ListConstructorExpressionNodeContext() {
        super(ListConstructorExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context,
                                                 ListConstructorExpressionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            List<Symbol> entries = QNameRefCompletionUtil.getExpressionContextEntries(context,
                    (QualifiedNameReferenceNode) nodeAtCursor);

            completionItems.addAll(this.getCompletionItemList(entries, context));
        } else {
            completionItems.addAll(this.expressionCompletions(context));
            if (context.getNodeAtCursor().kind() != SyntaxKind.SPREAD_MEMBER) {
                completionItems.addAll(this.spreadOperatorCompletions(context));
            }
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private List<SpreadCompletionItem> spreadOperatorCompletions(BallerinaCompletionContext context) {
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();
        Optional<Document> document = context.currentDocument();
        if (semanticModel.isEmpty() || document.isEmpty()) {
            return Collections.emptyList();
        }
        Optional<TypeSymbol> expectedType = semanticModel.get().expectedType(document.get(),
                PositionUtil.getLinePosition(context.getCursorPosition()));
        if (expectedType.isEmpty()) {
            return Collections.emptyList();
        }
        return context.visibleSymbols(context.getCursorPosition()).stream()
                .filter(symbol -> {
                    if (symbol.getName().isEmpty()) {
                        return false;
                    }
                    TypeSymbol typeDescriptor;
                    if (symbol.kind() == SymbolKind.VARIABLE) {
                        typeDescriptor = ((VariableSymbol) symbol).typeDescriptor();
                        if (typeDescriptor.typeKind() != TypeDescKind.ARRAY) {
                            return false;
                        }
                    } else if (symbol.kind() == SymbolKind.FUNCTION) {
                        Optional<TypeSymbol> typeSymbol = ((FunctionSymbol) symbol).typeDescriptor()
                                .returnTypeDescriptor();
                        if (typeSymbol.isEmpty()) {
                            return false;
                        }
                        typeDescriptor = typeSymbol.get();
                    } else {
                        return false;
                    }
                    return typeDescriptor.typeKind() == TypeDescKind.ARRAY &&
                            ((ArrayTypeSymbol) typeDescriptor).memberTypeDescriptor().subtypeOf(expectedType.get());
                }).map(symbol -> {
                    TypeSymbol typeDescriptor;
                    if (symbol.kind() == SymbolKind.VARIABLE) {
                        typeDescriptor = ((VariableSymbol) symbol).typeDescriptor();
                    } else {
                        typeDescriptor = ((FunctionSymbol) symbol).typeDescriptor().returnTypeDescriptor().get();
                    }
                    String typeName = NameUtil.getModifiedTypeName(context, typeDescriptor);
                    CompletionItem completionItem =
                            SpreadCompletionItemBuilder.build(symbol, typeName, context);
                    return new SpreadCompletionItem(context, completionItem, symbol);
                }).toList();
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, ListConstructorExpressionNode node) {
        return node.textRange().startOffset() <= context.getCursorPositionInTree()
                && context.getCursorPositionInTree() <= node.textRange().endOffset();
    }

    @Override
    public void sort(BallerinaCompletionContext context, ListConstructorExpressionNode node,
                     List<LSCompletionItem> completionItems) {
        for (LSCompletionItem lsCItem : completionItems) {
            Optional<TypeSymbol> contextType = context.getContextType();
            String sortText;
            if (contextType.isEmpty()) {
                // Added for safety.
                sortText = SortingUtil.genSortText(SortingUtil.toRank(context, lsCItem, 2));
            } else if (lsCItem.getType() == LSCompletionItem.CompletionItemType.SPREAD) {
                Optional<Symbol> expression = ((SpreadCompletionItem) lsCItem).getExpression();
                //Default sort text of variable or function symbols
                int lastRank = expression.map(expr -> expr.kind() == SymbolKind.FUNCTION ? 4 : 3)
                        .orElse(3);
                //Set spread completion item sort text as the same of variable or function symbols
                sortText = SortingUtil.genSortText(1)
                        + SortingUtil.genSortText(1) //Assignable
                        + SortingUtil.genSortText(lastRank); 
            } else if (!SortingUtil.isTypeCompletionItem(lsCItem)) {
                /*
                Here the sort text is three-fold.
                First we will assign the highest priority (Symbols over the others such as keywords),
                then we sort with the resolved type,
                Then we again append the sorting among the symbols (ex: functions over variable).
                 */
                sortText = SortingUtil.genSortText(1)
                        + SortingUtil.genSortTextByAssignability(context, lsCItem, contextType.get())
                        + SortingUtil.genSortText(SortingUtil.toRank(context, lsCItem));
            } else {
                sortText = SortingUtil.genSortText(2) + SortingUtil.genSortText(SortingUtil.toRank(context, lsCItem));
            }
            lsCItem.getCompletionItem().setSortText(sortText);
        }
    }
}
