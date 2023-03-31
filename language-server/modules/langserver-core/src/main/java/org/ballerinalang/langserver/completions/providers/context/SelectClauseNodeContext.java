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
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.QueryExpressionNode;
import io.ballerina.compiler.syntax.tree.SelectClauseNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link SelectClauseNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class SelectClauseNodeContext extends AbstractCompletionProvider<SelectClauseNode> {

    public SelectClauseNodeContext() {
        super(SelectClauseNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, SelectClauseNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();

        if (nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            /*
            Covers the cases where the cursor is within the expression context
             */
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            List<Symbol> exprEntries = QNameRefCompletionUtil.getExpressionContextEntries(context, qNameRef);
            completionItems.addAll(this.getCompletionItemList(exprEntries, context));
        } else {
            completionItems.addAll(this.expressionCompletions(context));
            completionItems.add(new SnippetCompletionItem(context, Snippet.CLAUSE_ON_CONFLICT.get()));
        }
        this.sort(context, node, completionItems);
        
        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context, SelectClauseNode node, 
                     List<LSCompletionItem> completionItems) {
        Optional<QueryExpressionNode> queryExprNode =  SortingUtil.getTheOutermostQueryExpressionNode(node);
        if (queryExprNode.isEmpty()) {
            return;
        }        
        completionItems.forEach(lsCItem -> {
            int rank = 2;
            if (SortingUtil.isSymbolCItemWithinNodeAndCursor(context, lsCItem, queryExprNode.get())) {
                rank = 1;
            }
            lsCItem.getCompletionItem().setSortText(SortingUtil.genSortText(rank) +
                    SortingUtil.genSortText(SortingUtil.toRank(context, lsCItem)));
        });
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, SelectClauseNode node) {
        return !node.selectKeyword().isMissing();
    }
}
