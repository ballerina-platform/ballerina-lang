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
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpreadFieldNode;
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
 * Completion provider for {@link SpreadFieldNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class SpreadFieldNodeContext extends AbstractCompletionProvider<SpreadFieldNode> {

    public SpreadFieldNodeContext() {
        super(SpreadFieldNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context,
                                                 SpreadFieldNode node) throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, context.getNodeAtCursor())) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) context.getNodeAtCursor();
            List<Symbol> moduleEntries = QNameReferenceUtil.getExpressionContextEntries(context, qNameRef);
            completionItems.addAll(this.getCompletionItemList(moduleEntries, context));
        } else {
            completionItems.addAll(this.expressionCompletions(context, node));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context, SpreadFieldNode node, List<LSCompletionItem> completionItems) {
        completionItems.forEach(lsCItem -> {
            int rank;
            if (lsCItem.getType() == LSCompletionItem.CompletionItemType.SYMBOL
                    && ((SymbolCompletionItem) lsCItem).getSymbol().isPresent()) {
                Optional<Symbol> symbol = ((SymbolCompletionItem) lsCItem).getSymbol();

                Optional<TypeSymbol> tDesc;
                switch (symbol.get().kind()) {
                    case RECORD_FIELD:
                    case OBJECT_FIELD:
                    case CLASS_FIELD:
                    case PARAMETER:
                    case PATH_PARAMETER:
                    case VARIABLE:
                        Optional<TypeSymbol> symbolType = SymbolUtil.getTypeDescriptor(symbol.get());
                        if (symbolType.isEmpty()) {
                            tDesc = Optional.empty();
                        } else {
                            tDesc = Optional.ofNullable(CommonUtil.getRawType(symbolType.get()));
                        }
                        break;
                    case FUNCTION:
                    case METHOD:
                    case RESOURCE_METHOD:
                        FunctionSymbol functionSymbol = (FunctionSymbol) symbol.get();
                        Optional<TypeSymbol> returnType = functionSymbol.typeDescriptor().returnTypeDescriptor();
                        if (returnType.isEmpty()) {
                            tDesc = Optional.empty();
                        } else {
                            tDesc = Optional.ofNullable(CommonUtil.getRawType(returnType.get()));
                        }
                        break;
                    default:
                        tDesc = Optional.empty();
                }

                if (tDesc.isPresent()
                        && (tDesc.get().typeKind() == TypeDescKind.RECORD
                        || tDesc.get().typeKind() == TypeDescKind.MAP)) {
                    rank = 1;
                } else {
                    rank = 2;
                }
            } else {
                rank = 3;
            }

            String sortText = SortingUtil.genSortText(rank)
                    + SortingUtil.genSortText(SortingUtil.toRank(context, lsCItem));
            lsCItem.getCompletionItem().setSortText(sortText);
        });
    }
}
