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

import io.ballerina.compiler.syntax.tree.KeySpecifierNode;
import io.ballerina.compiler.syntax.tree.OnConflictClauseNode;
import io.ballerina.compiler.syntax.tree.QueryConstructTypeNode;
import io.ballerina.compiler.syntax.tree.QueryExpressionNode;
import io.ballerina.compiler.syntax.tree.QueryPipelineNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link QueryExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class QueryExpressionNodeContext extends AbstractCompletionProvider<QueryExpressionNode> {

    private static final String TABLE_KW = "table";

    public QueryExpressionNodeContext() {
        super(QueryExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, QueryExpressionNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        if (node.queryConstructType().isPresent() && 
                this.onQueryConstructType(context, node.queryConstructType().get())) {
            QueryConstructTypeNode queryConstructType = node.queryConstructType().get();
            int cursor = context.getCursorPositionInTree();
            if (TABLE_KW.equals(queryConstructType.keyword().text())) {
                if (queryConstructType.keySpecifier().isEmpty() ||
                        queryConstructType.keySpecifier().get().keyKeyword().isMissing()) {
                    // 1. table [MISSING key]
                    completionItems.add(new SnippetCompletionItem(context, Snippet.KW_KEY.get()));
                } else if (withinKeySpecifierParenthesis(context, queryConstructType.keySpecifier().get())) {
                    // 1. table key(<cursor>)
                    // We don't suggest anything here
                } else if (cursor > queryConstructType.keySpecifier().get().textRange().endOffset()) {
                    // 1. table key(x) <cursor>
                    completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FROM.get()));
                    completionItems.add(new SnippetCompletionItem(context, Snippet.CLAUSE_FROM.get()));
                }
            } else if (queryConstructType.keyword().textRange().endOffset() < cursor) {
                // 1. stream <cursor>
                completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FROM.get()));
                completionItems.add(new SnippetCompletionItem(context, Snippet.CLAUSE_FROM.get()));
            }
        } else if (this.onQueryPipeLine(context, node)) {
            /*
             * we suggest intermediate clause snippets, the keywords and the select keyword as well. This is to support
             * multiple intermediate clauses and the select clause.
             * query-expr := [query-construct-type] query-pipeline select-clause [on-conflict-clause]
             */
            completionItems.addAll(getKeywordCompletionItems(context, node));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private boolean withinKeySpecifierParenthesis(BallerinaCompletionContext context, 
                                                  KeySpecifierNode keySpecifierNode) {
        int cursor = context.getCursorPositionInTree();
        return keySpecifierNode.openParenToken().textRange().endOffset() <= cursor &&
                (keySpecifierNode.closeParenToken().isMissing() ||
                        cursor <= keySpecifierNode.closeParenToken().textRange().startOffset());
    }

    private boolean onQueryConstructType(BallerinaCompletionContext context, QueryConstructTypeNode node) {
        int cursor = context.getCursorPositionInTree();
        return !node.isMissing() &&
                !node.keyword().isMissing() &&
                node.textRangeWithMinutiae().endOffset() >= cursor;
    }

    private boolean onQueryPipeLine(BallerinaCompletionContext context, QueryExpressionNode node) {
        int cursor = context.getCursorPositionInTree();
        QueryPipelineNode queryPipeline = node.queryPipeline();
        Optional<OnConflictClauseNode> onConflictClause = node.onConflictClause();

        return !queryPipeline.isMissing() && queryPipeline.textRange().endOffset() < cursor
                && (onConflictClause.isEmpty() || onConflictClause.get().textRange().startOffset() > cursor);
    }

    private List<LSCompletionItem> getKeywordCompletionItems(BallerinaCompletionContext context,
                                                             QueryExpressionNode node) {
        List<LSCompletionItem> completionItems = List.of(
                new SnippetCompletionItem(context, Snippet.KW_FROM.get()),
                new SnippetCompletionItem(context, Snippet.CLAUSE_FROM.get()),
                new SnippetCompletionItem(context, Snippet.KW_WHERE.get()),
                new SnippetCompletionItem(context, Snippet.KW_LET.get()),
                new SnippetCompletionItem(context, Snippet.CLAUSE_LET.get()),
                new SnippetCompletionItem(context, Snippet.KW_OUTER.get()),
                new SnippetCompletionItem(context, Snippet.KW_JOIN.get()),
                new SnippetCompletionItem(context, Snippet.CLAUSE_JOIN.get()),
                new SnippetCompletionItem(context, Snippet.KW_ORDERBY.get()),
                new SnippetCompletionItem(context, Snippet.KW_LIMIT.get())
        );

        // Need to specifically check if from keyword is missing because from clause can be there in the syntax tree
        // without the from keyword
        if (!node.queryPipeline().fromClause().isMissing() &&
                !node.queryPipeline().fromClause().fromKeyword().isMissing()) {
            // Above list is immutable, create new one
            completionItems = new ArrayList<>(completionItems);
            /*
             * It is mandatory to have at least one pipeline clause.
             * Only if that is true we suggest the select clause
             */
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_SELECT.get()));
            // Similarly do clause requires at least one query pipeline clause
            completionItems.add(new SnippetCompletionItem(context, Snippet.CLAUSE_DO.get()));
        }

        return completionItems;
    }
}
