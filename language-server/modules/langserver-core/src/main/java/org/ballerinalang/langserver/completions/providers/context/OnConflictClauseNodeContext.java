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
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.OnConflictClauseNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Completion provider for {@link OnConflictClauseNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class OnConflictClauseNodeContext extends AbstractCompletionProvider<OnConflictClauseNode> {

    public OnConflictClauseNodeContext() {
        super(OnConflictClauseNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, OnConflictClauseNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
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
    public boolean onPreValidation(BallerinaCompletionContext context, OnConflictClauseNode node) {
        return !node.onKeyword().isMissing() && !node.conflictKeyword().isMissing();
    }

    @Override
    public void sort(BallerinaCompletionContext context, OnConflictClauseNode node, List<LSCompletionItem> lsCItems) {
        for (LSCompletionItem lsCItem : lsCItems) {
            int rank = 2;
            if (this.isVariableOfErrorType(lsCItem)) {
                Optional<Symbol> symbol = ((SymbolCompletionItem) lsCItem).getSymbol();
                Optional<TypeSymbol> typeDescriptor = SymbolUtil.getTypeDescriptor(symbol.get());
                if (typeDescriptor.isPresent()
                        && CommonUtil.getRawType(typeDescriptor.get()).typeKind() == TypeDescKind.ERROR) {
                    rank = 1;
                }
            }
            String sortText = SortingUtil.genSortText(rank)
                    + SortingUtil.genSortText(SortingUtil.toRank(context, lsCItem));
            lsCItem.getCompletionItem().setSortText(sortText);
        }
    }

    private boolean isVariableOfErrorType(LSCompletionItem lsCItem) {
        Predicate<Symbol> symbolPredicate = CommonUtil.getVariableFilterPredicate();
        return lsCItem.getType() == LSCompletionItem.CompletionItemType.SYMBOL
                && ((SymbolCompletionItem) lsCItem).getSymbol().isPresent()
                && symbolPredicate.test(((SymbolCompletionItem) lsCItem).getSymbol().get());
    }
}
