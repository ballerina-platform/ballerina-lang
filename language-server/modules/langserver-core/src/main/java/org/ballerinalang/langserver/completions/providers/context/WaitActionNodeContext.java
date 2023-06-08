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

import io.ballerina.compiler.api.symbols.FutureTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.WaitActionNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.PARAMETER;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static io.ballerina.compiler.api.symbols.SymbolKind.WORKER;

/**
 * Completion provider for {@link WaitActionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class WaitActionNodeContext extends AbstractCompletionProvider<WaitActionNode> {

    public WaitActionNodeContext() {
        super(WaitActionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, WaitActionNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        // Covers both alternate and single wait actions
        if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            /*
            Covers the following
            eg:
            (1) wait module1:<cursor>
            (1) wait module1:f<cursor>
             */
            Predicate<Symbol> predicate = symbol -> symbol.kind() == SymbolKind.FUNCTION
                    || symbol instanceof VariableSymbol;
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            List<Symbol> filteredList = QNameRefCompletionUtil.getModuleContent(context, qNameRef, predicate);
            completionItems.addAll(this.getCompletionItemList(filteredList, context));
        } else {
            completionItems.addAll(this.expressionCompletions(context));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    protected List<LSCompletionItem> expressionCompletions(BallerinaCompletionContext context) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        /*
        check and check panic expression starts with check and check panic keywords, Which has been added with actions.
        query pipeline starts with from keyword and also being added with the actions
         */
        List<LSCompletionItem> completionItems = new ArrayList<>(this.getModuleCompletionItems(context));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_SERVICE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_NEW.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ISOLATED.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TRANSACTIONAL.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FUNCTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_LET.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TYPEOF.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TRAP.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ERROR.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CLIENT.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_OBJECT.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CHECK.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CHECK_PANIC.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_IS.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.EXPR_ERROR_CONSTRUCTOR.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.EXPR_OBJECT_CONSTRUCTOR.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.EXPR_BASE16_LITERAL.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.EXPR_BASE64_LITERAL.get()));

        // Avoid the error symbol suggestion since it is covered by the lang.error lang-lib 
        List<Symbol> filteredList = visibleSymbols.stream()
                .filter(symbol -> (symbol instanceof VariableSymbol || symbol.kind() == PARAMETER ||
                        symbol.kind() == FUNCTION || symbol.kind() == WORKER)
                        && !symbol.getName().orElse("").equals(Names.ERROR.getValue()))
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(filteredList, context));
        this.getAnonFunctionDefSnippet(context).ifPresent(completionItems::add);
        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context, WaitActionNode node, List<LSCompletionItem> completionItems) {
        Optional<TypeSymbol> contextType = context.getContextType();
        for (LSCompletionItem lsCompletionItem : completionItems) {
            CompletionItem completionItem = lsCompletionItem.getCompletionItem();
            int rank;
            if (this.isValidSymbolCompletionItem(lsCompletionItem)) {
                SymbolCompletionItem symbolCompletionItem = (SymbolCompletionItem) lsCompletionItem;
                /*
                 * Since the validity check considers the empty check of the symbol, we do not need to add the
                 * isPresent check here explicitly
                 */
                Symbol symbol = symbolCompletionItem.getSymbol().get();
                if (symbol.kind() == WORKER) {
                    rank = 1;
                } else if (symbol.kind() == VARIABLE
                        && ((VariableSymbol) symbol).typeDescriptor().typeKind() == TypeDescKind.FUTURE 
                        && contextType.isPresent() && contextType.get().typeKind() == TypeDescKind.FUTURE) {
                    Optional<TypeSymbol> completionItemTypeSymbol 
                            = ((FutureTypeSymbol) ((VariableSymbol) symbol).typeDescriptor()).typeParameter();
                    Optional<TypeSymbol> contextTypeSymbol = ((FutureTypeSymbol) contextType.get()).typeParameter();
                    if (completionItemTypeSymbol.isPresent() && contextTypeSymbol.isPresent() 
                            && completionItemTypeSymbol.get().subtypeOf(contextTypeSymbol.get())) {
                            rank = 1;
                    } else {
                        rank = 2;
                    }
                } else {
                    rank = SortingUtil.toRank(context, lsCompletionItem, 2);
                }
            } else {
                rank = SortingUtil.toRank(context, lsCompletionItem, 2);
            }
            completionItem.setSortText(SortingUtil.genSortText(rank));
        }
    }
    
    private boolean isValidSymbolCompletionItem(LSCompletionItem lsCompletionItem) {
        return lsCompletionItem instanceof SymbolCompletionItem
                && ((SymbolCompletionItem) lsCompletionItem).getSymbol().isPresent();
    }
}
