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
import io.ballerina.compiler.syntax.tree.MatchStatementNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion provider for {@link MatchStatementNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class MatchStatementNodeContext extends MatchStatementContext<MatchStatementNode> {

    public MatchStatementNodeContext() {
        super(MatchStatementNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, MatchStatementNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (this.isWithinBraces(context, node)) {
            /*
            Handles the following
            eg: match v {
                    <cursor>
                }
             */
            completionItems.addAll(this.getPatternClauseCompletions(context));
        } else {
            /*
            Handles the following
            eg: 1) match <cursor>
                2) match v<cursor>
             */
            if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, context.getNodeAtCursor())) {
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) context.getNodeAtCursor();
                List<Symbol> exprEntries = QNameRefCompletionUtil.getExpressionContextEntries(context, qNameRef);
                completionItems.addAll(this.getCompletionItemList(exprEntries, context));
            } else {
                completionItems.addAll(this.actionKWCompletions(context));
                completionItems.addAll(this.expressionCompletions(context));
            }
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private boolean isWithinBraces(BallerinaCompletionContext context, MatchStatementNode node) {
        int cursor = context.getCursorPositionInTree();
        Token openBrace = node.openBrace();
        Token closeBrace = node.closeBrace();

        return cursor >= openBrace.textRange().endOffset() && cursor <= closeBrace.textRange().startOffset();
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, MatchStatementNode node) {
        int cursor = context.getCursorPositionInTree();
        Token matchKeyword = node.matchKeyword();
        Token openBrace = node.openBrace();
        Token closeBrace = node.closeBrace();
        
        /*
        Validates the following
        eg: 1) match ... {<cursor>}
            2) match <cursor>
         */
        return !matchKeyword.isMissing() && cursor >= matchKeyword.textRange().endOffset() + 1
                && (closeBrace.isMissing() || cursor < closeBrace.textRange().endOffset());
    }
}
