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

import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.FunctionPointerCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link VariableDeclarationNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class VariableDeclarationNodeContext extends VariableDeclarationProvider<VariableDeclarationNode> {

    public VariableDeclarationNodeContext() {
        super(VariableDeclarationNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, VariableDeclarationNode node)
            throws LSCompletionException {
        if (node.initializer().isEmpty()) {
            return Collections.emptyList();
        }

        List<LSCompletionItem> completionItems
                = this.initializerContextCompletions(context, node.typedBindingPattern().typeDescriptor());
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, VariableDeclarationNode node) {
        if (node.equalsToken().isEmpty()) {
            return false;
        }
        int textPosition = context.getCursorPositionInTree();
        TextRange equalTokenRange = node.equalsToken().get().textRange();
        return equalTokenRange.endOffset() <= textPosition;
    }

    @Override
    public void sort(BallerinaCompletionContext context, VariableDeclarationNode node,
                     List<LSCompletionItem> completionItems) {
        Optional<TypeSymbol> typeSymbolAtCursor = context.getContextType();
        if (!typeSymbolAtCursor.isEmpty()) {
            super.sort(context, node, completionItems);
        }

        TypeSymbol symbol = typeSymbolAtCursor.get();
        completionItems.forEach(completionItem -> {
            int rank = SortingUtil.toRank(completionItem, 2);
            if (completionItem.getType() == LSCompletionItem.CompletionItemType.SYMBOL) {

                Optional<Symbol> completionSymbol = ((SymbolCompletionItem) completionItem).getSymbol();
                if (completionSymbol.isEmpty()) {
                    completionItem.getCompletionItem().setSortText(SortingUtil.genSortText(rank));
                    return;
                }
                Optional<TypeSymbol> evalTypeSymbol = SymbolUtil.getTypeDescriptor(completionSymbol.get());
                if (evalTypeSymbol.isPresent()) {
                    switch (completionItem.getCompletionItem().getKind()) {
                        case Variable:
                            if (evalTypeSymbol.get().assignableTo(symbol)) {
                                rank = 1;
                            }
                            break;
                        case Method:
                        case Function:
                            Optional<TypeSymbol> returnType = ((FunctionSymbol) completionSymbol.get())
                                    .typeDescriptor().returnTypeDescriptor();
                            if (returnType.isPresent() && returnType.get().assignableTo(symbol)) {
                                rank = 2;
                            }
                            break;
                        default:
                            rank = SortingUtil.toRank(completionItem, 2);
                    }
                }
            } else if (completionItem.getType() == LSCompletionItem.CompletionItemType.FUNCTION_POINTER) {
                Optional<Symbol> cSymbol = ((FunctionPointerCompletionItem) completionItem).getSymbol();
                if (cSymbol.isPresent()) {
                    Optional<TypeSymbol> evalTypeSymbol = SymbolUtil.getTypeDescriptor(cSymbol.get());
                    if (evalTypeSymbol.isPresent() && evalTypeSymbol.get().assignableTo(symbol)) {
                        rank = 1;
                    }
                }
            }
            completionItem.getCompletionItem().setSortText(SortingUtil.genSortText(rank));
        });
    }
}
