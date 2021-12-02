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

import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Completion provider for {@link CheckExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class CheckExpressionNodeContext extends AbstractCompletionProvider<CheckExpressionNode> {

    public CheckExpressionNodeContext() {
        super(CheckExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext ctx, CheckExpressionNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (node.parent().kind() == SyntaxKind.ASSIGNMENT_STATEMENT
                || node.parent().kind() == SyntaxKind.LOCAL_VAR_DECL
                || node.parent().kind() == SyntaxKind.MODULE_VAR_DECL
                || node.parent().kind() == SyntaxKind.OBJECT_FIELD
                || node.parent().kind() == SyntaxKind.FROM_CLAUSE) {
                completionItems.addAll(CompletionUtil.route(ctx, node.parent()));
        } else {

            NonTerminalNode nodeAtCursor = ctx.getNodeAtCursor();
            if (QNameReferenceUtil.onQualifiedNameIdentifier(ctx, nodeAtCursor)) {
                List<Symbol> expressions =
                        QNameReferenceUtil.getExpressionContextEntries(ctx, (QualifiedNameReferenceNode) nodeAtCursor);
                completionItems.addAll(getCompletionItemList(expressions, ctx));
            } else {
                /*
                    We add the action keywords in order to support the check action context completions
                 */
                completionItems.addAll(this.actionKWCompletions(ctx));
                completionItems.addAll(this.expressionCompletions(ctx));
                completionItems.add(new SnippetCompletionItem(ctx, Snippet.STMT_COMMIT.get()));
            }
        }
        this.sort(ctx, node, completionItems);

        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context, CheckExpressionNode node,
                     List<LSCompletionItem> completionItems) {
        Optional<TypeSymbol> contextType = context.getContextType();
        for (LSCompletionItem completionItem : completionItems) {
            String sortText = null;
            LSCompletionItem.CompletionItemType type = completionItem.getType();
            if (type == LSCompletionItem.CompletionItemType.SYMBOL) {
                Optional<Symbol> symbol = ((SymbolCompletionItem) completionItem).getSymbol();
                if (symbol.isPresent() && symbol.get().kind() == SymbolKind.VARIABLE) {
                    if (SymbolUtil.isClient(symbol.get())) {
                        // First show the clients since they have remote methods which probably will return error unions
                        sortText = SortingUtil.genSortText(1);
                    } else if (SymbolUtil.isClassVariable(symbol.get())) {
                        // Sort class variables at 3rd since they can have methods
                        sortText = SortingUtil.genSortText(3);
                    }
                } else if (symbol.isPresent() && symbol.get().kind() == SymbolKind.METHOD) {
                    // Show init method (or the new expression) 1st as well
                    MethodSymbol methodSymbol = (MethodSymbol) symbol.get();
                    if (methodSymbol.nameEquals("init")) {
                        sortText = SortingUtil.genSortText(1);
                    }
                }
            } else if (type == LSCompletionItem.CompletionItemType.SNIPPET) {
                // Show the new keyword 2nd
                if (((SnippetCompletionItem) completionItem).id().equals(Snippet.KW_NEW.name())) {
                    sortText = SortingUtil.genSortText(2);
                }
            }

            if (sortText == null && contextType.isPresent() &&
                    SortingUtil.isCompletionItemAssignableWithCheck(completionItem, contextType.get())) {
                // Items which has a union containing an error member are sorted 3r
                sortText = SortingUtil.genSortText(3);
            }

            if (sortText == null) {
                // Everything else, 3rd
                sortText = SortingUtil.genSortText(4);
            }

            sortText += SortingUtil.genSortText(SortingUtil.toRank(context, completionItem));
            completionItem.getCompletionItem().setSortText(sortText);
        }
    }
}
