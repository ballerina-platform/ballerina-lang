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

import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Handles the completions for {@link AssignmentStatementNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class AssignmentStatementNodeContext extends AbstractCompletionProvider<AssignmentStatementNode> {

    public AssignmentStatementNodeContext() {
        super(AssignmentStatementNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, AssignmentStatementNode node)
            throws LSCompletionException {
        if (this.cursorWithinLHS(context, node)) {
            return CompletionUtil.route(context, node.parent());
        }

        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, node.expression())) {
            /*
            Captures the following cases
            (1) [module:]TypeName c = module:<cursor>
            (2) [module:]TypeName c = module:a<cursor>
             */
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) node.expression();
            Predicate<Symbol> filter = symbol -> symbol instanceof VariableSymbol
                    || symbol.kind() == SymbolKind.FUNCTION;
            List<Symbol> moduleContent = QNameRefCompletionUtil.getModuleContent(context, qNameRef, filter);
            completionItems.addAll(this.getCompletionItemList(moduleContent, context));
        } else if (onSuggestionsAfterQualifiers(context, node.expression())) {
            /*
            Captures the following case.
            (1) TypeName c = <qualifier(s)> <cursor>
            Currently qualifier can be isolated, transactional, client, service.
             */
            completionItems.addAll(getCompletionItemsOnQualifiers(node.expression(), context));
        } else {
            /*
            Captures the following cases
            (1) [module:]TypeName c = <cursor>
            (2) [module:]TypeName c = a<cursor>
             */
            completionItems.addAll(this.actionKWCompletions(context));
            completionItems.addAll(this.expressionCompletions(context));
            completionItems.addAll(this.getNewExprCompletionItems(context, node));
        }
        this.sort(context, node, completionItems);
        return completionItems;
    }

    @Override
    protected List<LSCompletionItem> getCompletionItemsOnQualifiers(Node node, BallerinaCompletionContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>(super.getCompletionItemsOnQualifiers(node, context));
        List<Token> qualifiers = CommonUtil.getQualifiersOfNode(context, node);
        if (qualifiers.isEmpty()) {
            return completionItems;
        }
        Token lastQualifier = qualifiers.get(qualifiers.size() - 1);
        if (lastQualifier.kind() == SyntaxKind.ISOLATED_KEYWORD) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FUNCTION.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_OBJECT_TYPE_DESC_SNIPPET.get()));
        }
        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, AssignmentStatementNode node) {
        return !node.equalsToken().isMissing();
    }

    @Override
    public void sort(BallerinaCompletionContext context, AssignmentStatementNode node,
                     List<LSCompletionItem> completionItems) {

        Optional<TypeSymbol> typeSymbolAtCursor = Optional.empty();
        if (context.currentSemanticModel().isPresent() && context.currentDocument().isPresent()) {
            LinePosition cursorPosition = LinePosition.from(context.getCursorPosition().getLine(),
                                                                context.getCursorPosition().getCharacter());
            typeSymbolAtCursor = context.currentSemanticModel().get()
                    .expectedType(context.currentDocument().get(), cursorPosition);
        }

        if (typeSymbolAtCursor.isEmpty()) {
            super.sort(context, node, completionItems);
            return;
        }
        TypeSymbol symbol = typeSymbolAtCursor.get();
        for (LSCompletionItem completionItem : completionItems) {
            completionItem.getCompletionItem()
                    .setSortText(SortingUtil.genSortTextByAssignability(context, completionItem, symbol));
        }
    }

    private List<LSCompletionItem> getNewExprCompletionItems(BallerinaCompletionContext context,
                                                             AssignmentStatementNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        Optional<TypeSymbol> type = Optional.empty();
        if (context.currentSemanticModel().isPresent() && context.currentDocument().isPresent()) {
            LinePosition linePosition = node.expression().location().lineRange().startLine();
            type = context.currentSemanticModel().get()
                    .expectedType(context.currentDocument().get(), linePosition);
        }

        if (type.isEmpty()) {
            return completionItems;
        }

        TypeSymbol rawType = CommonUtil.getRawType(type.get());
        if (rawType.kind() == SymbolKind.CLASS) {
            completionItems.add(this.getImplicitNewCItemForClass((ClassSymbol) rawType, context));
        }

        return completionItems;
    }

    private boolean cursorWithinLHS(BallerinaCompletionContext context, AssignmentStatementNode node) {
        int equalToken = node.equalsToken().textRange().endOffset();
        int cursor = context.getCursorPositionInTree();

        return cursor < equalToken;
    }
}
