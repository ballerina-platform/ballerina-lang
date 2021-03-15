/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.syntax.tree.IntermediateClauseNode;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.Arrays;
import java.util.List;

/**
 * An abstract completion provider for child nodes of {@link IntermediateClauseNode}.
 *
 * @param <T> child node of {@link IntermediateClauseNode} for which completions are being provided
 */
public abstract class IntermediateClauseNodeContext<T extends IntermediateClauseNode>
        extends AbstractCompletionProvider<T> {

    public IntermediateClauseNodeContext(Class<T> attachmentPoint) {
        super(attachmentPoint);
    }

    protected List<LSCompletionItem> getKeywordCompletions(BallerinaCompletionContext context, T node) {
        return Arrays.asList(
                new SnippetCompletionItem(context, Snippet.KW_WHERE.get()),
                new SnippetCompletionItem(context, Snippet.KW_LET.get()),
                new SnippetCompletionItem(context, Snippet.CLAUSE_LET.get()),
                new SnippetCompletionItem(context, Snippet.KW_JOIN.get()),
                new SnippetCompletionItem(context, Snippet.CLAUSE_JOIN.get()),
                new SnippetCompletionItem(context, Snippet.KW_ORDERBY.get()),
                new SnippetCompletionItem(context, Snippet.KW_LIMIT.get()),
                new SnippetCompletionItem(context, Snippet.STMT_DO.get()),
                new SnippetCompletionItem(context, Snippet.KW_SELECT.get())
        );
    }
}
