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

import io.ballerina.compiler.syntax.tree.QueryPipelineNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Completion provider for {@link QueryPipelineNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class QueryPipelineNodeContext extends AbstractCompletionProvider<QueryPipelineNode> {

    public QueryPipelineNodeContext() {
        super(QueryPipelineNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, QueryPipelineNode node) {
        if (node.fromClause().isMissing() || node.fromClause().fromKeyword().isMissing()) {
            /*
            Covers the following
            (1) var test = table key(id) f<cursor>
            (2) var test = stream f<cursor>
            This particular section hits when there is at least one statement below (1) and (2)
             */
            return Arrays.asList(new SnippetCompletionItem(context, Snippet.KW_FROM.get()),
                    new SnippetCompletionItem(context, Snippet.CLAUSE_FROM.get()));
        }

        return new ArrayList<>();
    }
}
