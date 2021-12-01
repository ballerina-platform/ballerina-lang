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
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.OrderByClauseNode;
import io.ballerina.compiler.syntax.tree.OrderKeyNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link OrderByClauseNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class OrderByClauseNodeContext extends IntermediateClauseNodeContext<OrderByClauseNode> {

    public OrderByClauseNodeContext() {
        super(OrderByClauseNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, OrderByClauseNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();

        if (onSuggestDirectionKeywords(context, node)) {
            // Direction is optional
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ASCENDING.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_DESCENDING.get()));
            // Add query expression keywords (where, let, join, etc)
            completionItems.addAll(this.getKeywordCompletions(context, node));
        } else if (cursorAtTheEndOfClause(context, node)) {
            completionItems.addAll(this.getKeywordCompletions(context, node));
        } else if (nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
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
    public boolean onPreValidation(BallerinaCompletionContext context, OrderByClauseNode node) {
        return !node.orderKeyword().isMissing();
    }

    private boolean onSuggestDirectionKeywords(BallerinaCompletionContext context, OrderByClauseNode node) {
        SeparatedNodeList<OrderKeyNode> orderKeyNodes = node.orderKey();
        if (orderKeyNodes.isEmpty()) {
            return false;
        }
        int cursor = context.getCursorPositionInTree();
        OrderKeyNode lastOrderKey = orderKeyNodes.get(orderKeyNodes.size() - 1);
        return cursor > lastOrderKey.textRange().endOffset();
    }

    protected boolean cursorAtTheEndOfClause(BallerinaCompletionContext context, OrderByClauseNode node) {
        if (node.orderKey().isEmpty()) {
            return false;
        }

        OrderKeyNode lastOrderKey = node.orderKey().get(node.orderKey().size() - 1);
        int cursor = context.getCursorPositionInTree();

        if (lastOrderKey.orderDirection().isPresent()) {
            return lastOrderKey.orderDirection().get().textRange().endOffset() < cursor;
        } else if (lastOrderKey.expression() != null && !lastOrderKey.expression().isMissing()) {
            return lastOrderKey.expression().textRange().endOffset() < cursor;
        }

        return false;
    }

    @Override
    protected Optional<Node> getLastNodeOfClause(OrderByClauseNode node) {
        if (node.orderKey().isEmpty()) {
            return Optional.empty();
        }

        OrderKeyNode lastOrderKey = node.orderKey().get(node.orderKey().size() - 1);

        if (lastOrderKey.orderDirection().isPresent()) {
            return lastOrderKey.orderDirection().flatMap(Optional::of);
        } else {
            return Optional.of(lastOrderKey.expression());
        }
    }
}
