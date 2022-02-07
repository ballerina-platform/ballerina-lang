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

import io.ballerina.compiler.api.symbols.*;
import io.ballerina.compiler.syntax.tree.FailStatementNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link FailStatementNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class FailStatementNodeContext extends AbstractCompletionProvider<FailStatementNode> {
    public FailStatementNodeContext() {
        super(FailStatementNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, FailStatementNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode symbolAtCursor = context.getNodeAtCursor();
        if (symbolAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            QualifiedNameReferenceNode qRef = (QualifiedNameReferenceNode) symbolAtCursor;
            List<Symbol> expressionContextEntries = QNameReferenceUtil.getExpressionContextEntries(context, qRef);
            completionItems.addAll(this.getCompletionItemList(expressionContextEntries, context));
        } else {
            completionItems.addAll(this.expressionCompletions(context));
        }
        this.sort(context, node, completionItems);
        
        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context, FailStatementNode node,
                     List<LSCompletionItem> completionItems) {
        for (LSCompletionItem lsCItem : completionItems) {
            if (lsCItem.getType() == LSCompletionItem.CompletionItemType.SYMBOL) {
                SymbolCompletionItem symbolCompletionItem = (SymbolCompletionItem) lsCItem;
                if (isCompletionItemSubTypeError(symbolCompletionItem)) {
                    lsCItem.getCompletionItem().setSortText(SortingUtil.genSortText(1));
                } else {
                    int rank = SortingUtil.toRank(context, lsCItem);
                    lsCItem.getCompletionItem().setSortText(SortingUtil.genSortText(rank));
                }
            } else if (SortingUtil.isModuleCompletionItem(lsCItem)) {
                lsCItem.getCompletionItem().setSortText(SortingUtil.genSortText(2));
            } else {
                int rank = SortingUtil.toRank(context, lsCItem);
                lsCItem.getCompletionItem().setSortText(SortingUtil.genSortText(rank));
            }

        }
    }

    private boolean isUnionSymbolSubtypeError(UnionTypeSymbol unionTypeSymbol) {
        List<TypeSymbol> typeSymbols = unionTypeSymbol.memberTypeDescriptors();
        for (TypeSymbol typeSymbol :typeSymbols) {
            if (typeSymbol.typeKind() != TypeDescKind.TYPE_REFERENCE) {
                return false;
            }
            TypeReferenceTypeSymbol typeReferenceTypeSymbol = (TypeReferenceTypeSymbol) typeSymbol;
            if (typeReferenceTypeSymbol.typeDescriptor().typeKind() != TypeDescKind.ERROR) {
                return false;
            }
        }
        return true;
    }

    private boolean isCompletionItemSubTypeError(SymbolCompletionItem symbolCompletionItem) {
        if (symbolCompletionItem.getSymbol().isPresent()) {
            if (symbolCompletionItem.getSymbol().get().kind() == SymbolKind.TYPE_DEFINITION) {
                return false;
            }
            Optional<TypeSymbol> tSymbol = SymbolUtil.getTypeDescriptor(symbolCompletionItem.getSymbol().get());
            if (tSymbol.isPresent()) {
                TypeSymbol typeSymbol = CommonUtil.getRawType(tSymbol.get());
                if (typeSymbol.typeKind() == TypeDescKind.ERROR) {
                    return true;
                } else if (typeSymbol.typeKind() == TypeDescKind.UNION) {
                    UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) typeSymbol;
                    return isUnionSymbolSubtypeError(unionTypeSymbol);
                } else if (typeSymbol.typeKind() == TypeDescKind.FUNCTION) {
                    FunctionTypeSymbol functionTypeSymbol = (FunctionTypeSymbol) typeSymbol;
                    Optional<TypeSymbol> retTypeDec = functionTypeSymbol.returnTypeDescriptor();
                    return retTypeDec.isPresent() && retTypeDec.get().typeKind() == TypeDescKind.ERROR;
                }
            }
        }
        return false;
    }
}
