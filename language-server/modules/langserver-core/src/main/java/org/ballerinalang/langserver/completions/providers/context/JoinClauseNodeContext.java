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
import io.ballerina.compiler.syntax.tree.BindingPatternNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.JoinClauseNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.CompleteExpressionValidator;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link JoinClauseNode} context.
 * Following rule is addressed,
 * <p>
 * {@code join typed-binding-pattern in expression on expression equals expression}
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class JoinClauseNodeContext extends IntermediateClauseNodeContext<JoinClauseNode> {

    public JoinClauseNodeContext() {
        super(JoinClauseNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, JoinClauseNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();

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
            completionItems.addAll(this.getTypeDescContextItems(context));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_VAR.get()));
        } else if (this.onSuggestInKeyword(context, node)) {
            /*
             * Covers the following cases
             * (1) join var test <cursor>
             * (2) join var test i<cursor>
             * (3) join var test i<cursor> expression
             */
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_IN.get()));
        } else if (this.onSuggestOnKeyword(context, node)) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ON.get()));
        } else if (cursorAtTheEndOfClause(context, node)) {
            completionItems.addAll(this.getKeywordCompletions(context, node));
        } else if (nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            /*
             * Covers the remaining rule content,
             * (1) join var test in <cursor>
             * (2) join var test in e<cursor>
             * (3) join var test in module:<cursor>
             */
            /*
            Covers the cases where the cursor is within the expression context
             */
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            List<Symbol> exprEntries = QNameReferenceUtil.getExpressionContextEntries(context, qNameRef);
            completionItems.addAll(this.getCompletionItemList(exprEntries, context));
        } else if (!isMissingVarName(node.typedBindingPattern())
                && context.getCursorPositionInTree() >
                node.typedBindingPattern().bindingPattern().textRange().endOffset()) {
            completionItems.addAll(this.expressionCompletions(context));
        } else {
            return Collections.emptyList();
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, JoinClauseNode node) {
        return !node.joinKeyword().isMissing();
    }

    @Override
    protected Optional<Node> getLastNodeOfClause(JoinClauseNode node) {
        return Optional.of(node.joinOnCondition());
    }

    private boolean onSuggestBindingPattern(BallerinaCompletionContext context, JoinClauseNode node) {
        int cursor = context.getCursorPositionInTree();
        TypedBindingPatternNode typedBindingPattern = node.typedBindingPattern();
        CompleteExpressionValidator validator = new CompleteExpressionValidator();

        return !typedBindingPattern.typeDescriptor().apply(validator)
                || (cursor <= typedBindingPattern.typeDescriptor().textRange().endOffset()
                && cursor > node.joinKeyword().textRange().endOffset()
                && cursor < typedBindingPattern.bindingPattern().textRange().startOffset());
    }

    private boolean onSuggestInKeyword(BallerinaCompletionContext context, JoinClauseNode node) {
        int cursor = context.getCursorPositionInTree();
        TypedBindingPatternNode typedBindingPattern = node.typedBindingPattern();

        if (!node.inKeyword().isMissing()) {
            return false;
        } else if (cursor > typedBindingPattern.textRange().endOffset() &&
                cursor <= node.expression().textRange().startOffset()
                && !isMissingVarName(typedBindingPattern)) {
            /*
             * Captures:
             * (1) join var varName <cursor>
             */
            return true;
        } else {
            Node nodeAtCursor = context.getNodeAtCursor();

            /*
             * Captures:
             * (1) join var varName i<cursor>
             * (2) join var varName i<cursor> expression
             */
            if (nodeAtCursor.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                SimpleNameReferenceNode nameReferenceNode = (SimpleNameReferenceNode) nodeAtCursor;
                return node.expression().textRange().startOffset() + 1 == cursor &&
                        nameReferenceNode.textRange().startOffset() + 1 == cursor;
            }
        }

        return false;
    }

    private boolean onSuggestOnKeyword(BallerinaCompletionContext context, JoinClauseNode node) {
        int cursor = context.getCursorPositionInTree();

        if (node.typedBindingPattern().isMissing() ||
                node.typedBindingPattern().isMissing() ||
                node.inKeyword().isMissing() ||
                node.expression().isMissing() ||
                !node.joinOnCondition().onKeyword().isMissing()) {
            return false;
        } else if (node.expression().textRange().endOffset() < cursor &&
                cursor <= node.joinOnCondition().textRange().startOffset()) {
            // join var varName in expr <cursor>
            return true;
        } else {
            Node nodeAtCursor = context.getNodeAtCursor();

            /*
             * Captures:
             * (1) join var varName in expr o<cursor>
             * (2) join var varName in expr o<cursor> expr
             */
            if (nodeAtCursor.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE && !nodeAtCursor.equals(node.expression())) {
                SimpleNameReferenceNode nameReferenceNode = (SimpleNameReferenceNode) nodeAtCursor;
                return node.expression().textRange().endOffset() <= cursor &&
                        nameReferenceNode.textRange().endOffset() == cursor;
            }
        }

        return false;
    }

    private boolean isMissingVarName(TypedBindingPatternNode node) {
        BindingPatternNode bindingPattern = node.bindingPattern();
        return bindingPattern.kind() == SyntaxKind.CAPTURE_BINDING_PATTERN
                && ((CaptureBindingPatternNode) bindingPattern).variableName().isMissing();
    }
}
