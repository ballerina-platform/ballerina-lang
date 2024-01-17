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
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.OnClauseNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.providers.context.util.QueryExpressionUtil;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion provider for {@link OnClauseNode} context.
 * Following rule is addressed,
 * <p>
 * {@code join typed-binding-pattern in expression on expression equals expression}
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class OnClauseNodeContext extends AbstractCompletionProvider<OnClauseNode> {

    public OnClauseNodeContext() {
        super(OnClauseNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, OnClauseNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();

        if (this.onSuggestEqualsKeyword(context, node)) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_EQUALS.get()));
        } else if (nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            /*
             * Covers the remaining rule content,
             * (1) on <cursor>
             * (2) on e<cursor>
             * (3) on expr equals <cursor>
             * (4) on expr equals e<cursor>
             * (5) on expr equals module:<cursor>
             */
            /*
            Covers the cases where the cursor is within the expression context
             */
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            List<Symbol> exprEntries = QNameRefCompletionUtil.getExpressionContextEntries(context, qNameRef);
            completionItems.addAll(this.getCompletionItemList(exprEntries, context));
        } else {
            completionItems.addAll(this.expressionCompletions(context));
            // If cursor at the end of clause, suggest other clauses
            if (cursorAtEndOfClause(context, node)) {
                completionItems.addAll(QueryExpressionUtil.getCommonKeywordCompletions(context));
            }
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, OnClauseNode node) {
        return !node.onKeyword().isMissing();
    }

    private boolean onSuggestEqualsKeyword(BallerinaCompletionContext context, OnClauseNode node) {
        int cursor = context.getCursorPositionInTree();
        ExpressionNode lhs = node.lhsExpression();
        ExpressionNode rhs = node.rhsExpression();
        Token equalsKeyword = node.equalsKeyword();

        if (lhs.isMissing() || !equalsKeyword.isMissing()) {
            return false;
        } else if (cursor > lhs.textRange().endOffset() &&
                (!rhs.isMissing() && cursor < rhs.textRange().startOffset())) {
            return true;
        } else {
            Node nodeAtCursor = context.getNodeAtCursor();

            /*
             * Captures:
             * (1) join var varName in expr e<cursor>
             * (2) join var varName in expr eq<cursor> expr
             */
            if (nodeAtCursor.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                SimpleNameReferenceNode nameReferenceNode = (SimpleNameReferenceNode) nodeAtCursor;
                return lhs.textRange().endOffset() == cursor &&
                        nameReferenceNode.textRange().endOffset() == cursor;
            }
        }

        return false;
    }

    private boolean cursorAtEndOfClause(BallerinaCompletionContext context, OnClauseNode node) {
        int cursor = context.getCursorPositionInTree();
        return !node.rhsExpression().isMissing() && node.rhsExpression().textRange().endOffset() < cursor;
    }
}
