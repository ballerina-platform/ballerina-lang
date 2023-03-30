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
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Generic completion resolver for the Block Nodes.
 *
 * @param <T> block node type
 * @since 2.0.0
 */
public abstract class NodeWithRHSInitializerProvider<T extends Node> extends AbstractCompletionProvider<T> {

    public NodeWithRHSInitializerProvider(Class<T> attachmentPoint) {
        super(attachmentPoint);
    }

    @Override
    public void sort(BallerinaCompletionContext context, T node, List<LSCompletionItem> completionItems) {
        Optional<TypeSymbol> contextType = Optional.empty();
        if (context.currentSemanticModel().isPresent() && context.currentDocument().isPresent()) {
            contextType = context.currentSemanticModel().get().expectedType(context.currentDocument().get(),
                    LinePosition.from(context.getCursorPosition().getLine(),
                            context.getCursorPosition().getCharacter()));
        }

        for (LSCompletionItem lsCItem : completionItems) {
            String sortText;
            if (contextType.isEmpty()) {
                // Safety check. In general, should not reach this point
                sortText = SortingUtil.genSortText(SortingUtil.toRank(context, lsCItem));
            } else {
                sortText = SortingUtil.genSortTextByAssignability(context, lsCItem, contextType.get());
            }
            lsCItem.getCompletionItem().setSortText(sortText);
        }
    }

    protected List<LSCompletionItem> initializerContextCompletions(BallerinaCompletionContext context,
                                                                   Node initializer) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            /*
            Captures the following cases
            (1) [module:]TypeName c = module:<cursor>
            (2) [module:]TypeName c = module:a<cursor>
             */
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            Predicate<Symbol> filter = symbol -> symbol instanceof VariableSymbol
                    || symbol.kind() == SymbolKind.FUNCTION
                    || symbol.kind() == SymbolKind.TYPE_DEFINITION
                    || symbol.kind() == SymbolKind.CLASS;
            List<Symbol> moduleContent = QNameRefCompletionUtil.getModuleContent(context, qNameRef, filter);
            return this.getCompletionItemList(moduleContent, context);
        }

        if (onSuggestionsAfterQualifiers(context, initializer)) {
            /*
                Covers the following
                type x = <qualifier(s)> <cursor>
                type x = <qualifier(s)>  x<cursor>
                currently the qualifier can be isolated/transactional.
             */
            return getCompletionItemsOnQualifiers(initializer, context);
        }
        
        /*
        Captures the following cases
        (1) [module:]TypeName c = <cursor>
        (2) [module:]TypeName c = a<cursor>
         */
        List<LSCompletionItem> completionItems = new ArrayList<>();
        completionItems.addAll(this.actionKWCompletions(context));
        completionItems.addAll(this.expressionCompletions(context));
        completionItems.addAll(getNewExprCompletionItems(context));
        if (withinTransactionStatementNode(context)) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_COMMIT.get()));
        }
        return completionItems;
    }

    @Override
    protected List<LSCompletionItem> getCompletionItemsOnQualifiers(Node node, BallerinaCompletionContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>(super.getCompletionItemsOnQualifiers(node, context));
        if (node.kind() == SyntaxKind.MODULE_VAR_DECL) {
            return completionItems;
        }
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

    protected List<LSCompletionItem> getNewExprCompletionItems(BallerinaCompletionContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        Optional<TypeSymbol> contextType = context.getContextType();
        if (contextType.isEmpty()) {
            return completionItems;
        }
        TypeSymbol typeSymbol = CommonUtil.getRawType(contextType.get());
        if (typeSymbol.typeKind() == TypeDescKind.UNION) {
            Optional<TypeSymbol> memberType = ((UnionTypeSymbol) typeSymbol).memberTypeDescriptors().stream()
                    .map(CommonUtil::getRawType)
                    .filter(type -> type.typeKind() == TypeDescKind.STREAM || type.kind() == SymbolKind.CLASS)
                    .findFirst();
            if (memberType.isEmpty()) {
                return completionItems;
            }
            typeSymbol = memberType.get();
        }
        if (typeSymbol.typeKind() == TypeDescKind.STREAM) {
            LSCompletionItem implicitNewCompletionItem = this.getImplicitNewCItemForStreamType(typeSymbol, context);
            completionItems.add(implicitNewCompletionItem);
        } else if (typeSymbol.kind() == SymbolKind.CLASS) {
            LSCompletionItem implicitNewCompletionItem =
                    this.getImplicitNewCItemForClass((ClassSymbol) typeSymbol, context);
            completionItems.add(implicitNewCompletionItem);
        }

        return completionItems;
    }

    private boolean withinTransactionStatementNode(BallerinaCompletionContext context) {
        NonTerminalNode evalNode = context.getNodeAtCursor().parent();
        boolean withinTransaction = false;

        while (evalNode != null) {
            if (evalNode.kind() == SyntaxKind.TRANSACTION_STATEMENT) {
                withinTransaction = true;
                break;
            }
            evalNode = evalNode.parent();
        }
        return withinTransaction;
    }
}
