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
import io.ballerina.compiler.syntax.tree.JoinClauseNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
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
 * Completion provider for {@link JoinClauseNode} context.
 * Following rule is addressed,
 * <p>
 * {@code join typed-binding-pattern in expression on expression equals expression}
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class JoinClauseNodeContext extends AbstractCompletionProvider<JoinClauseNode> {

    public JoinClauseNodeContext() {
        super(JoinClauseNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, JoinClauseNode node) {
        NonTerminalNode nodeAtCursor = context.get(CompletionKeys.NODE_AT_CURSOR_KEY);

        if (this.onSuggestBindingPattern(context, node)) {
            /*
            Covers the following case where,
            (1) join <cursor>
            (2) join <cursor> in ...
            Here we suggest the types and var keyword
             */
            if (nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
                return this.getCompletionItemList(QNameReferenceUtil.getTypesInModule(context, qNameRef), context);
            }
            List<LSCompletionItem> completionItems = this.getModuleCompletionItems(context);
            completionItems.addAll(this.getTypeItems(context));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_VAR.get()));

            return completionItems;
        }
        if (this.onSuggestInKeyword(context, node)) {
            /*
             * Covers the following cases
             * (1) join var test <cursor>
             * (2) join var test i<cursor>
             * (3) join var test i<cursor> expression
             */
            return Collections.singletonList(new SnippetCompletionItem(context, Snippet.KW_IN.get()));
        }

        /*
         * Covers the remaining rule content,
         * (1) join var test in <cursor>
         * (2) join var test in e<cursor>
         * (3) join var test in module:<cursor>
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
    public boolean onPreValidation(LSContext context, JoinClauseNode node) {
        return !node.joinKeyword().isMissing();
    }

    private boolean onSuggestBindingPattern(LSContext context, JoinClauseNode node) {
        int cursor = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);
        TypedBindingPatternNode typedBindingPattern = node.typedBindingPattern();
        if (typedBindingPattern.isMissing()) {
            return true;
        }

        return cursor <= typedBindingPattern.textRange().endOffset()
                && cursor >= typedBindingPattern.textRange().startOffset();
    }

    private boolean onSuggestInKeyword(LSContext context, JoinClauseNode node) {
        int cursor = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);
        TypedBindingPatternNode typedBindingPattern = node.typedBindingPattern();
        ExpressionNode expression = node.expression();

        return node.inKeyword().isMissing() && !typedBindingPattern.isMissing()
                && cursor > typedBindingPattern.textRange().endOffset()
                && (expression.isMissing() || cursor < expression.textRange().startOffset());
    }
}
