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
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.OrderByClauseNode;
import io.ballerina.compiler.syntax.tree.OrderKeyNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.Arrays;
import java.util.List;

/**
 * Completion provider for {@link OrderByClauseNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class OrderByClauseNodeContext extends AbstractCompletionProvider<OrderByClauseNode> {

    public OrderByClauseNodeContext() {
        super(OrderByClauseNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, OrderByClauseNode node) {
        NonTerminalNode nodeAtCursor = context.get(CompletionKeys.NODE_AT_CURSOR_KEY);

        if (onSuggestDirectionKeywords(context, node)) {
            return Arrays.asList(
                    new SnippetCompletionItem(context, Snippet.KW_ASCENDING.get()),
                    new SnippetCompletionItem(context, Snippet.KW_DESCENDING.get())
            );
        }

        if (nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            /*
            Covers the cases where the cursor is within the expression context
             */
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            List<Symbol> exprEntries = QNameReferenceUtil.getExpressionContextEntries(context, qNameRef);
            return this.getCompletionItemList(exprEntries, context);
        }

        return this.expressionCompletions(context);
    }

    @Override
    public boolean onPreValidation(LSContext context, OrderByClauseNode node) {
        return !node.orderKeyword().isMissing();
    }

    private boolean onSuggestDirectionKeywords(LSContext context, OrderByClauseNode node) {
        SeparatedNodeList<OrderKeyNode> orderKeyNodes = node.orderKey();
        int cursor = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);
        NonTerminalNode nodeAtCursor = context.get(CompletionKeys.NODE_AT_CURSOR_KEY);

        if (orderKeyNodes.isEmpty() || nodeAtCursor.kind() != SyntaxKind.SIMPLE_NAME_REFERENCE) {
            return false;
        }

        OrderKeyNode lastOrderKey = orderKeyNodes.get(orderKeyNodes.size() - 1);

        return cursor > lastOrderKey.textRange().endOffset();
    }
}
