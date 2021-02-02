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
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import org.ballerinalang.langserver.SnippetBlock;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        if (onQualifiedNameIdentifier(context, nodeAtCursor)) {
            /*
            Covers the following
            Ex: function test() {
                    module:<cursor>
                    module:a<cursor>
                }
             */
            QualifiedNameReferenceNode nameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            Predicate<Symbol> filter = symbol ->
                    symbol.kind() == SymbolKind.TYPE_DEFINITION
                            || symbol.kind() == SymbolKind.VARIABLE
                            || symbol.kind() == SymbolKind.CONSTANT
                            || symbol.kind() == SymbolKind.FUNCTION
                            || symbol.kind() == SymbolKind.CLASS;
            List<Symbol> moduleContent = QNameReferenceUtil.getModuleContent(context, nameRef, filter);

            completionItems.addAll(this.getCompletionItemList(moduleContent, context));
        } else {
            /*
            Covers the following
            Ex: function test() {
                    <cursor>
                    i<cursor>
                }
             */
            completionItems.addAll(getStaticCompletionItems(context));
            completionItems.addAll(getStatementCompletionItems(context, node));
            completionItems.addAll(this.getModuleCompletionItems(context));
            completionItems.addAll(this.getTypeItems(context));
            completionItems.addAll(this.getSymbolCompletions(context));
        }
        this.sort(context, node, completionItems);
        
        return completionItems;
    }

    protected List<LSCompletionItem> getStaticCompletionItems(BallerinaCompletionContext context) {

        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();

        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_NAMESPACE_DECLARATION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_XMLNS.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_VAR.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_WAIT.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_START.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FLUSH.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FUNCTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ISOLATED.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TRANSACTIONAL.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CHECK_PANIC.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CHECK.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FINAL.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FAIL.get()));

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
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_FORK.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_TRANSACTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_RETRY_TRANSACTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_MATCH.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_RETURN.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_PANIC.get()));
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
        // Todo: Implement rollback statement suggestion

        return completionItems;
    }

    protected List<LSCompletionItem> getSymbolCompletions(BallerinaCompletionContext context) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        List<LSCompletionItem> completionItems = new ArrayList<>();
        // TODO: Can we get this filter to a common place
        List<Symbol> filteredList = visibleSymbols.stream()
                .filter(symbol -> symbol.kind() == SymbolKind.FUNCTION || symbol instanceof VariableSymbol)
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(filteredList, context));
        completionItems.addAll(this.getTypeguardDestructedItems(visibleSymbols, context));
        return completionItems;
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
            int endOffset = statementNode.lineRange().endLine().offset();
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

    private List<LSCompletionItem> getTypeguardDestructedItems(List<Symbol> scopeEntries,
                                                               BallerinaCompletionContext ctx) {
        List<String> capturedSymbols = new ArrayList<>();
        // In the case of type guarded variables multiple symbols with the same symbol name and we ignore those
        return scopeEntries.stream()
                .filter(symbol -> {
                    if (symbol.kind() != SymbolKind.VARIABLE) {
                        return false;
                    }
                    TypeSymbol typeDesc = ((VariableSymbol) symbol).typeDescriptor();
                    return typeDesc.typeKind() == TypeDescKind.UNION && !capturedSymbols.contains(symbol.name());
                })
                .map(symbol -> {
                    capturedSymbols.add(symbol.name());
                    List<TypeSymbol> errorTypes = new ArrayList<>();
                    List<TypeSymbol> resultTypes = new ArrayList<>();
                    List<TypeSymbol> members
                            = new ArrayList<>(((UnionTypeSymbol) ((VariableSymbol) symbol).typeDescriptor())
                            .memberTypeDescriptors());
                    members.forEach(bType -> {
                        if (bType.typeKind() == TypeDescKind.ERROR) {
                            errorTypes.add(bType);
                        } else {
                            resultTypes.add(bType);
                        }
                    });
                    if (errorTypes.size() == 1) {
                        resultTypes.addAll(errorTypes);
                    }
                    String symbolName = symbol.name();
                    String label = symbolName + " - typeguard " + symbolName;
                    String detail = "Destructure the variable " + symbolName + " with typeguard";
                    StringBuilder snippet = new StringBuilder();
                    int paramCounter = 1;
                    if (errorTypes.size() > 1) {
                        snippet.append("if (").append(symbolName).append(" is ").append("error) {")
                                .append(CommonUtil.LINE_SEPARATOR).append("\t${1}").append(CommonUtil.LINE_SEPARATOR)
                                .append("}");
                        paramCounter++;
                    } else if (errorTypes.size() == 1) {
                        snippet.append("if (").append(symbolName).append(" is ")
                                .append(errorTypes.get(0).signature()).append(") {")
                                .append(CommonUtil.LINE_SEPARATOR).append("\t${1}").append(CommonUtil.LINE_SEPARATOR)
                                .append("}");
                        paramCounter++;
                    }
                    int finalParamCounter = paramCounter;
                    String restSnippet = (!snippet.toString().isEmpty() && resultTypes.size() > 2) ? " else " : "";
                    restSnippet += IntStream.range(0, resultTypes.size() - paramCounter).mapToObj(value -> {
                        TypeSymbol bType = members.get(value);
                        String placeHolder = "\t${" + (value + finalParamCounter) + "}";
                        return "if (" + symbolName + " is " + bType.signature() + ") {"
                                + CommonUtil.LINE_SEPARATOR + placeHolder + CommonUtil.LINE_SEPARATOR + "}";
                    }).collect(Collectors.joining(" else ")) + " else {" + CommonUtil.LINE_SEPARATOR + "\t${"
                            + members.size() + "}" + CommonUtil.LINE_SEPARATOR + "}";

                    snippet.append(restSnippet);

                    SnippetBlock cItemSnippet = new SnippetBlock(label, snippet.toString(), detail,
                            SnippetBlock.Kind.SNIPPET);
                    return new SnippetCompletionItem(ctx, cItemSnippet);
                }).collect(Collectors.toList());
    }
}
