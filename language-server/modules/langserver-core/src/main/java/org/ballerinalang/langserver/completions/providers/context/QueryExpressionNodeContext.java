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

import io.ballerina.compiler.syntax.tree.OnConflictClauseNode;
import io.ballerina.compiler.syntax.tree.QueryExpressionNode;
import io.ballerina.compiler.syntax.tree.QueryPipelineNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link QueryExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class QueryExpressionNodeContext extends AbstractCompletionProvider<QueryExpressionNode> {

    public QueryExpressionNodeContext() {
        super(QueryExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, QueryExpressionNode node)
            throws LSCompletionException {

        if (this.onQueryPipeLine(context, node)) {
            /*
             * we suggest intermediate clause snippets, the keywords and the select keyword as well. This is to support
             * multiple intermediate clauses and the select clause.
             * query-expr := [query-construct-type] query-pipeline select-clause [on-conflict-clause]
             */
            List<LSCompletionItem> completionItems = new ArrayList<>(Arrays.asList(
                    new SnippetCompletionItem(context, Snippet.KW_FROM.get()),
                    new SnippetCompletionItem(context, Snippet.CLAUSE_FROM.get()),
                    new SnippetCompletionItem(context, Snippet.KW_WHERE.get()),
                    new SnippetCompletionItem(context, Snippet.KW_LET.get()),
                    new SnippetCompletionItem(context, Snippet.CLAUSE_LET.get()),
                    new SnippetCompletionItem(context, Snippet.KW_JOIN.get()),
                    new SnippetCompletionItem(context, Snippet.CLAUSE_JOIN.get()),
                    new SnippetCompletionItem(context, Snippet.KW_ORDERBY.get()),
                    new SnippetCompletionItem(context, Snippet.KW_LIMIT.get())));
            if (!node.queryPipeline().fromClause().isMissing()) {
                /*
                 * It is mandatory to have at least one pipeline clause.
                 * Only if that is true we suggest the select clause
                 */
                completionItems.add(new SnippetCompletionItem(context, Snippet.KW_SELECT.get()));
            }

            return completionItems;
        }

        return new ArrayList<>();
    }

    private boolean onQueryPipeLine(LSContext context, QueryExpressionNode node) {
        int cursor = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);
        QueryPipelineNode queryPipeline = node.queryPipeline();
        Optional<OnConflictClauseNode> onConflictClause = node.onConflictClause();

        return !queryPipeline.isMissing() && queryPipeline.textRange().endOffset() < cursor
                && (!onConflictClause.isPresent() || onConflictClause.get().textRange().startOffset() > cursor);
    }
}
