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
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.ReturnTypeFinder;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generic completion resolver for the Block Nodes.
 *
 * @param <T> block node type
 * @since 2.0.0
 */
public class BlockNodeContextProvider<T extends Node> extends AbstractCompletionProvider<T> {

    public BlockNodeContextProvider(Class<T> attachmentPoint) {
        super(attachmentPoint);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, T node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();

        if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            /*
            Covers the following
            Ex: function test() {
                    module:<cursor>
                    module:a<cursor>
                }
             */
            QualifiedNameReferenceNode nameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            Predicate<Symbol> filter = symbol -> symbol.kind() != SymbolKind.SERVICE_DECLARATION;
            List<Symbol> moduleContent = QNameRefCompletionUtil.getModuleContent(context, nameRef, filter);

            completionItems.addAll(this.getCompletionItemList(moduleContent, context));
        } else if (onSuggestionsAfterQualifiers(context, nodeAtCursor)) {
            completionItems.addAll(getCompletionItemsOnQualifiers(nodeAtCursor, context));
        } else {
            /*
            Covers the following
            Ex: if (true) {
                    <cursor>
                    i<cursor>
                }
             */
            completionItems.addAll(getStaticCompletionItems(context));
            completionItems.addAll(getStatementCompletionItems(context, node));
            completionItems.addAll(this.getTypeDescContextItems(context));
            completionItems.addAll(this.getSymbolCompletions(context));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    protected List<LSCompletionItem> getStaticCompletionItems(BallerinaCompletionContext context) {

        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();

        // Remove the function keyword suggestion from here, since it is suggested by typeItems API
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_NAMESPACE_DECLARATION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_XMLNS.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_VAR.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_WAIT.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_START.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FLUSH.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_NEW.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ISOLATED.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TRANSACTIONAL.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_LET.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TYPEOF.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TRAP.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CLIENT.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CHECK_PANIC.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CHECK.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FINAL.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FAIL.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.EXPR_ERROR_CONSTRUCTOR.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.EXPR_OBJECT_CONSTRUCTOR.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.EXPR_BASE16_LITERAL.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.EXPR_BASE64_LITERAL.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FROM.get()));

        return completionItems;
    }

    protected List<LSCompletionItem> getStatementCompletionItems(BallerinaCompletionContext context, T node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        boolean withinLoop = this.withinLoopConstructs(node);

        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_IF.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_WHILE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_DO.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_LOCK.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_FOREACH.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_FOREACH_RANGE_EXP.get()));
        if (this.onSuggestFork(node)) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_FORK.get()));
        }
        if (this.withinTransactionStatement(node)) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_ROLLBACK.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_COMMIT.get()));
        }
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_TRANSACTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_RETRY.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_RETRY_TRANSACTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_MATCH.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_PANIC.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_STREAM.get()));

        Optional<TypeSymbol> returnType = context.currentSemanticModel()
                .flatMap(semanticModel -> {
                    ReturnTypeFinder finder = new ReturnTypeFinder(semanticModel);
                    return finder.getTypeSymbol(context.getNodeAtCursor());
                });

        if (returnType.isEmpty() || returnType.get().typeKind() == TypeDescKind.NIL) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_RETURN_SC.get()));
        } else {
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_RETURN.get()));
        }

        Optional<Node> nodeBeforeCursor = this.nodeBeforeCursor(context, node);
        if (nodeBeforeCursor.isPresent()) {
            switch (nodeBeforeCursor.get().kind()) {
                case IF_ELSE_STATEMENT:
                    completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_ELSE_IF.get()));
                    completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_ELSE.get()));
                    break;
                case DO_STATEMENT:
                case MATCH_STATEMENT:
                case FOREACH_STATEMENT:
                case WHILE_STATEMENT:
                case LOCK_STATEMENT:
                    completionItems.add(new SnippetCompletionItem(context, Snippet.CLAUSE_ON_FAIL.get()));
                    break;
                default:
                    break;
            }
        }
        if (withinLoop) {
            /*
            Populate Break and continue Statement template only if there is an enclosing looping construct such as
            while/ foreach
            */
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_CONTINUE.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_BREAK.get()));
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

    protected List<LSCompletionItem> getSymbolCompletions(BallerinaCompletionContext context) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        // TODO: Can we get this filter to a common place
        Predicate<Symbol> symbolFilter = CommonUtil.getVariableFilterPredicate();
        symbolFilter = symbolFilter.or(symbol -> symbol.kind() == SymbolKind.FUNCTION);
        List<Symbol> filteredList = visibleSymbols.stream()
                .filter(symbolFilter)
                .collect(Collectors.toList());
        
        return this.getCompletionItemList(filteredList, context);
    }

    private boolean withinLoopConstructs(T node) {
        Node evalNode = node;
        boolean withinLoops = false;

        while (!withinLoops && evalNode.kind() != SyntaxKind.MODULE_PART) {
            withinLoops = (evalNode.kind() == SyntaxKind.WHILE_STATEMENT)
                    || (evalNode.kind() == SyntaxKind.FOREACH_STATEMENT);
            evalNode = evalNode.parent();
        }

        return withinLoops;
    }

    private boolean withinTransactionStatement(T node) {
        Node evalNode = node;
        boolean withinTransaction = false;

        while (!withinTransaction && evalNode != null) {
            withinTransaction = evalNode.kind() == SyntaxKind.TRANSACTION_STATEMENT;
            evalNode = evalNode.parent();
        }

        return withinTransaction;
    }

    private Optional<Node> nodeBeforeCursor(BallerinaCompletionContext context, Node node) {
        NodeList<StatementNode> statements;
        if (node.kind() == SyntaxKind.FUNCTION_BODY_BLOCK) {
            statements = ((FunctionBodyBlockNode) node).statements();
        } else if (node.kind() == SyntaxKind.BLOCK_STATEMENT) {
            statements = ((BlockStatementNode) node).statements();
        } else {
            return Optional.empty();
        }
        int cursor = context.getCursorPositionInTree();

        for (int i = statements.size() - 1; i >= 0; i--) {
            StatementNode statementNode = statements.get(i);
            int endOffset = statementNode.textRange().endOffset();
            if (statementNode.kind() == SyntaxKind.LOCAL_VAR_DECL
                    && ((VariableDeclarationNode) statementNode).equalsToken().isEmpty()) {
                continue;
                /*
                This particular condition is added to satisfy the following scenario,
                eg: if () {
                    } e<cursor>
                    here, e token is identified as a variable declaration node. hence we opt it out considering the
                    equal token's availability
                 */
            }
            if (cursor > endOffset) {
                return Optional.of(statementNode);
            }
        }

        return Optional.empty();
    }

    private boolean onSuggestFork(Node node) {
        Node parent = node.parent();
        while (parent != null) {
            if (parent.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                return ((FunctionDefinitionNode) parent).qualifierList().stream()
                        .noneMatch(token -> token.kind() == SyntaxKind.ISOLATED_KEYWORD);
            }
            parent = parent.parent();
        }

        return false;
    }

    boolean withinFunctionOrWorkerWithReturn(Node node) {
        NonTerminalNode parent = node.parent();
        while (parent != null) {
            if (parent.kind() == SyntaxKind.FUNCTION_DEFINITION
                    || parent.kind() == SyntaxKind.NAMED_WORKER_DECLARATION) {
                break;
            }
            parent = parent.parent();
        }

        if (parent == null) {
            return false;
        }

        if (parent.kind() == SyntaxKind.FUNCTION_DEFINITION) {
            FunctionDefinitionNode functionDef = (FunctionDefinitionNode) parent;
            return functionDef.functionSignature().returnTypeDesc().isPresent();
        }

        if (parent.kind() == SyntaxKind.NAMED_WORKER_DECLARATION) {
            NamedWorkerDeclarationNode workerDeclarationNode = (NamedWorkerDeclarationNode) parent;
            return workerDeclarationNode.returnTypeDesc().isPresent();
        }

        return false;
    }

    @Override
    public void sort(BallerinaCompletionContext context, T node,
                     List<LSCompletionItem> completionItems) {
        if (!this.withinFunctionOrWorkerWithReturn(node)) {
            super.sort(context, node, completionItems);
            return;
        }

        for (LSCompletionItem lsCItem : completionItems) {
            if (Snippet.KW_RETURN.equals(lsCItem)) {
                lsCItem.getCompletionItem().setSortText(SortingUtil.genSortText(1));
                continue;
            }

            int rank = SortingUtil.toRank(context, lsCItem, 1);
            lsCItem.getCompletionItem().setSortText(SortingUtil.genSortText(rank));
        }
    }
}
