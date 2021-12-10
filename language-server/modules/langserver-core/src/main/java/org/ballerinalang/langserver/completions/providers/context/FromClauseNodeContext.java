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
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.BindingPatternNode;
import io.ballerina.compiler.syntax.tree.FromClauseNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.CompletionItemKind;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link FromClauseNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class FromClauseNodeContext extends IntermediateClauseNodeContext<FromClauseNode> {

    public FromClauseNodeContext() {
        super(FromClauseNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, FromClauseNode node) {

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

        List<LSCompletionItem> completionItems = new ArrayList<>();

        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();

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
            completionItems.addAll(this.getTypeDescContextItems(context));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_VAR.get()));
        } else if (node.inKeyword().isMissing()) {
            /*
            Covers the following cases
            Eg:
            (1) var tesVar = stream from var item <cursor>
            (2) var tesVar = stream from var item <cursor>i
             */
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_IN.get()));
        } else if (cursorAtTheEndOfClause(context, node)) {
            completionItems.addAll(this.getKeywordCompletions(context, node));
        } else if (nodeAtCursor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            /*
            Covers the cases where the cursor is within the expression context
             */
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            List<Symbol> exprEntries = QNameReferenceUtil.getExpressionContextEntries(context, qNameRef);
            completionItems.addAll(this.getCompletionItemList(exprEntries, context));
        } else {
            completionItems.addAll(this.expressionCompletions(context));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, FromClauseNode node) {
        return !node.fromKeyword().isMissing();
    }
    
    @Override
    public void sort(BallerinaCompletionContext context,
                     FromClauseNode node,
                     List<LSCompletionItem> completionItems) {

        completionItems.forEach(lsCItem -> {
            if (isIterableCompletionItem(lsCItem)) {
                lsCItem.getCompletionItem().setSortText(SortingUtil.genSortText(1)
                        + SortingUtil.genSortText(SortingUtil.toRank(context, lsCItem)));
                return;
            } else if (lsCItem.getType() == LSCompletionItem.CompletionItemType.SYMBOL &&
                    lsCItem.getCompletionItem().getKind() == CompletionItemKind.Function) {
                lsCItem.getCompletionItem().setSortText(SortingUtil.genSortText(2));
                return;
            }
            lsCItem.getCompletionItem().setSortText(SortingUtil.genSortText(3) +
                    SortingUtil.genSortText(SortingUtil.toRank(context, lsCItem)));
        });
    }

    private boolean isIterableCompletionItem(LSCompletionItem lsCItem) {
        
        if (lsCItem.getType() != LSCompletionItem.CompletionItemType.SYMBOL) {
            return false;
        }
        
        Symbol symbol = ((SymbolCompletionItem) lsCItem).getSymbol().orElse(null);
        Optional<TypeSymbol> typeDesc = SymbolUtil.getTypeDescriptor(symbol);

        List<TypeDescKind> ITERABLES = Arrays.asList(
                TypeDescKind.STRING, TypeDescKind.ARRAY,
                TypeDescKind.MAP, TypeDescKind.TABLE,
                TypeDescKind.STREAM, TypeDescKind.XML);

        if (typeDesc.isPresent()) {
            TypeSymbol rawType = CommonUtil.getRawType(typeDesc.get());
            return ITERABLES.contains(rawType.typeKind());
        }
        return false;
    }

    private boolean onTypedBindingPatternContext(BallerinaCompletionContext context, FromClauseNode node) {
        int cursor = context.getCursorPositionInTree();
        Token fromKeyword = node.fromKeyword();
        Token inKeyword = node.inKeyword();
        TypedBindingPatternNode typedBindingPattern = node.typedBindingPattern();

        return ((cursor > fromKeyword.textRange().endOffset() && inKeyword.isMissing()) ||
                (cursor > fromKeyword.textRange().endOffset() && cursor < inKeyword.textRange().startOffset()))
                && (typedBindingPattern.isMissing() || typedBindingPattern.textRange().endOffset() >= cursor);
    }

    private boolean onBindingPatternContext(BallerinaCompletionContext context, FromClauseNode node) {
        TypedBindingPatternNode typedBindingPattern = node.typedBindingPattern();

        if (typedBindingPattern.isMissing()) {
            return false;
        }

        int cursor = context.getCursorPositionInTree();
        Token inKeyword = node.inKeyword();
        TypeDescriptorNode typeDescriptor = typedBindingPattern.typeDescriptor();
        BindingPatternNode bindingPattern = typedBindingPattern.bindingPattern();

        return ((cursor > typeDescriptor.textRange().endOffset() && inKeyword.isMissing()) ||
                (cursor > typeDescriptor.textRange().endOffset() && cursor < inKeyword.textRange().startOffset()))
                && (bindingPattern.isMissing() || bindingPattern.textRange().endOffset() >= cursor);
    }

    @Override
    protected Optional<Node> getLastNodeOfClause(FromClauseNode node) {
        return Optional.of(node.expression());
    }
}
