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
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
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
 * Completion provider for {@link ReturnStatementNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ReturnStatementNodeContext extends AbstractCompletionProvider<ReturnStatementNode> {

    public ReturnStatementNodeContext() {
        super(ReturnStatementNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, ReturnStatementNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        if (node.expression().isPresent()
                && QNameReferenceUtil.onQualifiedNameIdentifier(context, context.getNodeAtCursor())) {
            List<Symbol> entries = QNameReferenceUtil.getExpressionContextEntries(context,
                    (QualifiedNameReferenceNode) context.getNodeAtCursor());

            completionItems.addAll(this.getCompletionItemList(entries, context));
        } else {
            completionItems.addAll(this.actionKWCompletions(context));
            completionItems.addAll(this.expressionCompletions(context));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context, ReturnStatementNode node,
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

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, ReturnStatementNode node) {
        int cursor = context.getCursorPositionInTree();
        return !node.returnKeyword().isMissing() && node.returnKeyword().textRange().endOffset() <= cursor;
    }
}
