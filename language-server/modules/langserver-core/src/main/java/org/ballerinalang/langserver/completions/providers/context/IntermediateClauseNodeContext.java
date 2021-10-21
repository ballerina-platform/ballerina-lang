/*
 *  Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import io.ballerina.compiler.syntax.tree.Node;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.providers.context.util.QueryExpressionUtil;

import java.util.List;
import java.util.Optional;

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
        return QueryExpressionUtil.getCommonKeywordCompletions(context);
    }

    protected boolean cursorAtTheEndOfClause(BallerinaCompletionContext context, T node) {
        Optional<Node> lastNode = getLastNodeOfClause(node);
        if (lastNode.isEmpty() || lastNode.get().isMissing()) {
            return false;
        }

        int cursor = context.getCursorPositionInTree();
        return lastNode.get().textRange().endOffset() < cursor;
    }

    /**
     * This method is supposed to return the last node (can be an expression or another node) of the child clause. This
     * method is used to determine if the cursor is at the end of a clause in
     * {@link #cursorAtTheEndOfClause(BallerinaCompletionContext, IntermediateClauseNode)}
     *
     * @param node Node related to the context
     * @return Optional node which is the last node in the clause
     */
    protected Optional<Node> getLastNodeOfClause(T node) {
        return Optional.empty();
    }
}
