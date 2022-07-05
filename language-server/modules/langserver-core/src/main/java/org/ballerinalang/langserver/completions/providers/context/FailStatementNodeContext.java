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

import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.FailStatementNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
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
            List<Symbol> expressionContextEntries = QNameRefCompletionUtil.getExpressionContextEntries(context, qRef);
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
            if (lsCItem.getType() == LSCompletionItem.CompletionItemType.SYMBOL 
                    && isCompletionItemSubTypeOfError((SymbolCompletionItem) lsCItem)) {
                lsCItem.getCompletionItem().setSortText(SortingUtil.genSortText(1));
            } else if (SortingUtil.isModuleCompletionItem(lsCItem)) {
                lsCItem.getCompletionItem().setSortText(SortingUtil.genSortText(2));
            } else {
                lsCItem.getCompletionItem().setSortText(SortingUtil.genSortText(3));
            }
        }
    }

    private boolean isUnionOfErrors(UnionTypeSymbol unionTypeSymbol) {
        List<TypeSymbol> typeSymbols = unionTypeSymbol.memberTypeDescriptors();
        return typeSymbols.stream()
                .allMatch(typeSymbol -> CommonUtil.getRawType(typeSymbol).typeKind() == TypeDescKind.ERROR);
    }

    private boolean isFunctionReturnsUnionOfErrors(FunctionTypeSymbol functionTypeSymbol) {
        Optional<TypeSymbol> retTypeDec = functionTypeSymbol.returnTypeDescriptor();
        if (retTypeDec.isEmpty()) {
            return false;
        }
        TypeSymbol rawType = CommonUtil.getRawType(retTypeDec.get());
        if (rawType.typeKind() == TypeDescKind.UNION) {
            UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) rawType;
            return isUnionOfErrors(unionTypeSymbol);
        }
        return rawType.typeKind() == TypeDescKind.ERROR;
    }
    
    private boolean isCompletionItemSubTypeOfError(SymbolCompletionItem symbolCompletionItem) {
        Optional<Symbol> symbol = symbolCompletionItem.getSymbol();
        if (symbol.isEmpty() || symbol.get().kind() == SymbolKind.TYPE_DEFINITION) {
            return false;
        }
        
        Optional<TypeSymbol> tSymbol = SymbolUtil.getTypeDescriptor(symbol.get());
        if (tSymbol.isEmpty()) {
            return false;
        }
        
        TypeSymbol typeSymbol = CommonUtil.getRawType(tSymbol.get());
        if (typeSymbol.typeKind() == TypeDescKind.UNION) {
            UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) typeSymbol;
            return isUnionOfErrors(unionTypeSymbol);
        } else if (typeSymbol.typeKind() == TypeDescKind.FUNCTION) {
            FunctionTypeSymbol functionTypeSymbol = (FunctionTypeSymbol) typeSymbol;
            return isFunctionReturnsUnionOfErrors(functionTypeSymbol);
        } else {
            return typeSymbol.typeKind() == TypeDescKind.ERROR;
        }
    }
}
