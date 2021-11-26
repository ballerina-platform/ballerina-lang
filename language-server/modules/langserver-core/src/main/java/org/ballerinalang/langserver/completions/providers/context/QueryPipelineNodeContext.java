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

import io.ballerina.compiler.syntax.tree.JoinClauseNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QueryPipelineNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.WhereClauseNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.providers.context.util.QueryExpressionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion provider for {@link QueryPipelineNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class QueryPipelineNodeContext extends AbstractCompletionProvider<QueryPipelineNode> {

    public QueryPipelineNodeContext() {
        super(QueryPipelineNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, QueryPipelineNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (node.fromClause().isMissing() || node.fromClause().fromKeyword().isMissing()) {
            /*
            Covers the following
            (1) var test = table key(id) f<cursor>
            (2) var test = stream f<cursor>
            This particular section hits when there is at least one statement below (1) and (2)
             */
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FROM.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.CLAUSE_FROM.get()));
        } else if (onMissingWhereNode(context)) {
                /*
            Covers the following
            [intermediate-clause] w<cursor> or [intermediate-clause] j<cursor>

            Parser recovers as follows.
            [intermediate] MISSING[where]w
            Therefore, for each possible intermediate clause MISSING[where] is used as a flag.
             */
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FROM.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.CLAUSE_FROM.get()));
            completionItems.addAll(QueryExpressionUtil.getCommonKeywordCompletions(context));
        } else if(onMissingJoinKeyword(context)) {
            /* Covers the following
            [intermediate-clause] outer <cursor>
             */
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_JOIN.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.CLAUSE_JOIN.get()));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private boolean onMissingWhereNode(BallerinaCompletionContext context) {
        NonTerminalNode nextIntermediate = context.getNodeAtCursor().parent();
        return !nextIntermediate.isMissing() && nextIntermediate.kind() == SyntaxKind.WHERE_CLAUSE
                && ((WhereClauseNode) nextIntermediate).whereKeyword().isMissing();
    }
    
    private boolean onMissingJoinKeyword(BallerinaCompletionContext context) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        NonTerminalNode evalNode = (nodeAtCursor.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE)
                ? nodeAtCursor.parent().parent() : nodeAtCursor;
        
        return !evalNode.isMissing() && evalNode.kind() == SyntaxKind.JOIN_CLAUSE 
                && ((JoinClauseNode) evalNode).joinKeyword().isMissing();
    }
}
