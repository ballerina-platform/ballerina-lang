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
import io.ballerina.compiler.syntax.tree.MatchClauseNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion provider for {@link MatchClauseNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class MatchClauseNodeContext extends MatchStatementContext<MatchClauseNode> {

    public MatchClauseNodeContext() {
        super(MatchClauseNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, MatchClauseNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        
        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, context.getNodeAtCursor())) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) context.getNodeAtCursor();
            List<Symbol> moduleContent = QNameReferenceUtil.getModuleContent(context, qNameRef, this.constantFilter());
            completionItems.addAll(this.getCompletionItemList(moduleContent, context));
        } else if (onSuggestIfClause(context, node)) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_IF.get()));
        } else {
            completionItems.addAll(this.getPatternClauseCompletions(context));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }
    
    private boolean onSuggestIfClause(BallerinaCompletionContext context, MatchClauseNode node) {
        int cursor = context.getCursorPositionInTree();
        SeparatedNodeList<Node> matchPatterns = node.matchPatterns();
        
        return !matchPatterns.isEmpty() && cursor > matchPatterns.get(matchPatterns.size() - 1).textRange().endOffset();
    }
}
