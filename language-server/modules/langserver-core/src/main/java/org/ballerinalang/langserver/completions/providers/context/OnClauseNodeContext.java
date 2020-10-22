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
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.OnClauseNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.Collections;
import java.util.List;

/**
 * Completion provider for {@link OnClauseNode} context.
 * Following rule is addressed,
 * <p>
 * {@code join typed-binding-pattern in expression on expression equals expression}
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class OnClauseNodeContext extends AbstractCompletionProvider<OnClauseNode> {

    public OnClauseNodeContext() {
        super(OnClauseNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, OnClauseNode node) {
        NonTerminalNode nodeAtCursor = context.get(CompletionKeys.NODE_AT_CURSOR_KEY);

        if (this.onSuggestEqualsKeyword(context, node)) {
            return Collections.singletonList(new SnippetCompletionItem(context, Snippet.KW_EQUALS.get()));
        }

        /*
         * Covers the remaining rule content,
         * (1) on <cursor>
         * (2) on e<cursor>
         * (3) on expr equals <cursor>
         * (4) on expr equals e<cursor>
         * (5) on expr equals module:<cursor>
         */
        if (nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            /*
            Covers the cases where the cursor is within the expression context
             */
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            List<Symbol> exprEntries = QNameReferenceUtil.getExpressionContextEntries(context, qNameRef);
            return this.getCompletionItemList(exprEntries, context);
        }

        return this.expressionCompletions(context);
    }

    @Override
    public boolean onPreValidation(LSContext context, OnClauseNode node) {
        return !node.onKeyword().isMissing();
    }

    private boolean onSuggestEqualsKeyword(LSContext context, OnClauseNode node) {
        int cursor = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);
        ExpressionNode lhs = node.lhsExpression();
        ExpressionNode rhs = node.rhsExpression();
        Token equalsKeyword = node.equalsKeyword();

        return !lhs.isMissing() && cursor > lhs.textRange().endOffset()
                && (rhs.isMissing() || cursor < rhs.textRange().startOffset())
                && (equalsKeyword.isMissing() || (!equalsKeyword.isMissing()
                && cursor < equalsKeyword.textRange().startOffset()));
    }
}
