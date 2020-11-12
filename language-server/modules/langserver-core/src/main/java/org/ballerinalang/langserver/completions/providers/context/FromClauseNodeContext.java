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
import io.ballerina.compiler.syntax.tree.FromClauseNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Completion provider for {@link FromClauseNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class FromClauseNodeContext extends AbstractCompletionProvider<FromClauseNode> {

    public FromClauseNodeContext() {
        super(FromClauseNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, FromClauseNode node) {

        if (this.onBindingPatternContext(context, node)) {
            /*
            Covers the case where the cursor is within the binding pattern context.
            Eg:
            (1) var tesVar = stream from var cu<cursor>
            (2) var tesVar = stream from var <cursor>
            In these cases no suggestions are provided
             */
            return new ArrayList<>();
        }

        NonTerminalNode nodeAtCursor = context.get(CompletionKeys.NODE_AT_CURSOR_KEY);

        if (this.onTypedBindingPatternContext(context, node)) {
            /*
            Covers the case where the cursor is within the typed binding pattern context.
            Eg:
            (1) var tesVar = stream from v<cursor>
            (2) var tesVar = stream from <cursor> - this is blocked
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
        if (node.inKeyword().isMissing()) {
            /*
            Covers the following cases
            Eg:
            (1) var tesVar = stream from var item <cursor>
            (2) var tesVar = stream from var item <cursor>i
             */
            return Collections.singletonList(new SnippetCompletionItem(context, Snippet.KW_IN.get()));
        }
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
    public boolean onPreValidation(LSContext context, FromClauseNode node) {
        return !node.fromKeyword().isMissing();
    }

    private boolean onTypedBindingPatternContext(LSContext context, FromClauseNode node) {
        int cursor = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);
        Token fromKeyword = node.fromKeyword();
        Token inKeyword = node.inKeyword();
        TypedBindingPatternNode typedBindingPattern = node.typedBindingPattern();

        return ((cursor > fromKeyword.textRange().endOffset() && inKeyword.isMissing()) ||
                (cursor > fromKeyword.textRange().endOffset() && cursor < inKeyword.textRange().startOffset()))
                && (typedBindingPattern.isMissing() || typedBindingPattern.textRange().endOffset() >= cursor);
    }

    private boolean onBindingPatternContext(LSContext context, FromClauseNode node) {
        TypedBindingPatternNode typedBindingPattern = node.typedBindingPattern();

        if (typedBindingPattern == null || typedBindingPattern.isMissing()) {
            return false;
        }

        int cursor = context.get(CompletionKeys.TEXT_POSITION_IN_TREE);
        Token inKeyword = node.inKeyword();
        TypeDescriptorNode typeDescriptor = typedBindingPattern.typeDescriptor();
        BindingPatternNode bindingPattern = typedBindingPattern.bindingPattern();

        return ((cursor > typeDescriptor.textRange().endOffset() && inKeyword.isMissing()) ||
                (cursor > typeDescriptor.textRange().endOffset() && cursor < inKeyword.textRange().startOffset()))
                && (bindingPattern.isMissing() || bindingPattern.textRange().endOffset() >= cursor);
    }
}
