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

import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.jvm.util.Flags;
import org.ballerinalang.langserver.SnippetBlock;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;
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
    public List<LSCompletionItem> getCompletions(LSContext context, T node) throws LSCompletionException {
        NonTerminalNode nodeAtCursor = context.get(CompletionKeys.NODE_AT_CURSOR_KEY);
        if (onQualifiedNameIdentifier(context, nodeAtCursor)) {
            /*
            Covers the following
            Ex: function test() {
                    module:<cursor>
                    module:a<cursor>
                }
             */
            QualifiedNameReferenceNode nameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            Predicate<Scope.ScopeEntry> filter = scopeEntry -> {
                BSymbol symbol = scopeEntry.symbol;
                return !CommonUtil.isInvalidSymbol(symbol)
                        && (symbol instanceof BTypeSymbol || symbol instanceof BInvokableSymbol)
                        && (symbol.flags & Flags.PUBLIC) == Flags.PUBLIC;
            };
            List<Scope.ScopeEntry> moduleContent = QNameReferenceUtil.getModuleContent(context, nameRef, filter);
            return this.getCompletionItemList(moduleContent, context);
        }
        
        /*
        Covers the following
        Ex: function test() {
                <cursor>
                i<cursor>
            }
         */
        List<LSCompletionItem> completionItems = new ArrayList<>();
        completionItems.addAll(getStaticCompletionItems(context));
        completionItems.addAll(getStatementCompletionItems(context, node));
        completionItems.addAll(this.getPackagesCompletionItems(context));
        completionItems.addAll(this.getTypeItems(context));
        completionItems.addAll(this.getSymbolCompletions(context));

        return completionItems;
    }

    protected List<LSCompletionItem> getStaticCompletionItems(LSContext context) {

        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();

        // Add the xmlns snippet
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_NAMESPACE_DECLARATION.get()));
        // Add the xmlns keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_XMLNS.get()));
        // Add the var keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_VAR.get()));
        // Add the wait keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_WAIT.get()));
        // Add the start keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_START.get()));
        // Add the flush keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FLUSH.get()));
        // Add the function keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FUNCTION.get()));
        // Add the checkpanic keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CHECK_PANIC.get()));
        // Add the check keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CHECK.get()));
        // Add the final keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FINAL.get()));

        return completionItems;
    }

    protected List<LSCompletionItem> getStatementCompletionItems(LSContext context, T node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        boolean withinLoop = this.withinLoopConstructs(node);

        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_IF.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_WHILE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_LOCK.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_FOREACH.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_FORK.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_TRANSACTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_RETRY_TRANSACTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_MATCH.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_RETURN.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_PANIC.get()));
        // TODO: Revamp the following logic
        if (context.get(CompletionKeys.PREVIOUS_NODE_KEY) instanceof BLangIf) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_ELSE_IF.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_ELSE.get()));
        }
        if (withinLoop) {
            /*
            Populate Break and continue Statement template only if there is an enclosing looping construct such as
            while/ foreach
            */
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_CONTINUE.get()));
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_BREAK.get()));
        }
        if (context.get(CompletionKeys.TRANSACTION_COUNT_KEY) > 0) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_ROLLBACK.get()));
        }

        return completionItems;
    }

    protected List<LSCompletionItem> getSymbolCompletions(LSContext context) {
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        List<LSCompletionItem> completionItems = new ArrayList<>();
        // TODO: Can we get this filter to a common place
        List<Scope.ScopeEntry> filteredList = visibleSymbols.stream()
                .filter(scopeEntry -> scopeEntry.symbol instanceof BVarSymbol
                        && !(scopeEntry.symbol instanceof BOperatorSymbol))
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

    private List<LSCompletionItem> getTypeguardDestructedItems(List<Scope.ScopeEntry> scopeEntries,
                                                               LSContext ctx) {
        List<String> capturedSymbols = new ArrayList<>();
        // In the case of type guarded variables multiple symbols with the same symbol name and we ignore those
        return scopeEntries.stream()
                .filter(scopeEntry -> (scopeEntry.symbol.type instanceof BUnionType)
                        && !capturedSymbols.contains(scopeEntry.symbol.name.value))
                .map(entry -> {
                    capturedSymbols.add(entry.symbol.name.getValue());
                    List<BType> errorTypes = new ArrayList<>();
                    List<BType> resultTypes = new ArrayList<>();
                    List<BType> members = new ArrayList<>(((BUnionType) entry.symbol.type).getMemberTypes());
                    members.forEach(bType -> {
                        if (bType.tag == TypeTags.ERROR) {
                            errorTypes.add(bType);
                        } else {
                            resultTypes.add(bType);
                        }
                    });
                    if (errorTypes.size() == 1) {
                        resultTypes.addAll(errorTypes);
                    }
                    String symbolName = entry.symbol.name.getValue();
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
                                .append(CommonUtil.getBTypeName(errorTypes.get(0), ctx, true)).append(") {")
                                .append(CommonUtil.LINE_SEPARATOR).append("\t${1}").append(CommonUtil.LINE_SEPARATOR)
                                .append("}");
                        paramCounter++;
                    }
                    int finalParamCounter = paramCounter;
                    String restSnippet = (!snippet.toString().isEmpty() && resultTypes.size() > 2) ? " else " : "";
                    restSnippet += IntStream.range(0, resultTypes.size() - paramCounter).mapToObj(value -> {
                        BType bType = members.get(value);
                        String placeHolder = "\t${" + (value + finalParamCounter) + "}";
                        return "if (" + symbolName + " is " + CommonUtil.getBTypeName(bType, ctx, true) + ") {"
                                + CommonUtil.LINE_SEPARATOR + placeHolder + CommonUtil.LINE_SEPARATOR + "}";
                    }).collect(Collectors.joining(" else ")) + " else {" + CommonUtil.LINE_SEPARATOR + "\t${"
                            + members.size() + "}" + CommonUtil.LINE_SEPARATOR + "}";

                    snippet.append(restSnippet);

                    SnippetBlock cItemSnippet = new SnippetBlock(label, snippet.toString(), detail,
                            SnippetBlock.SnippetType.SNIPPET);
                    return new SnippetCompletionItem(ctx, cItemSnippet);
                }).collect(Collectors.toList());
    }
}
