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
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
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
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, ListConstructorExpressionNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            List<Symbol> entries = QNameReferenceUtil.getExpressionContextEntries(context,
                    (QualifiedNameReferenceNode) nodeAtCursor);

            completionItems.addAll(this.getCompletionItemList(entries, context));
        } else {
            completionItems.addAll(this.expressionCompletions(context));
        }
        this.sort(context, node, completionItems);

        return completionItems;
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
