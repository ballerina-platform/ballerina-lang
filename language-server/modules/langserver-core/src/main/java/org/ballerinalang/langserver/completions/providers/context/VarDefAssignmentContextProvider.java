package org.ballerinalang.langserver.completions.providers.context;

import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.VariableDeclarationNode;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VarDefAssignmentContextProvider<T extends Node> extends AbstractCompletionProvider<T> {
    public VarDefAssignmentContextProvider(Kind kind) {
        super(kind);
    }

    protected List<LSCompletionItem> actionKWCompletions(LSContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        /*
        Add the start keywords of the following actions.
        start, wait, flush, check, check panic, trap, query action (query pipeline starts with from keyword)
         */
        return Arrays.asList(
                new SnippetCompletionItem(context, Snippet.KW_START.get()),
                new SnippetCompletionItem(context, Snippet.KW_WAIT.get()),
                new SnippetCompletionItem(context, Snippet.KW_FLUSH.get()),
                new SnippetCompletionItem(context, Snippet.KW_FROM.get()),
                new SnippetCompletionItem(context, Snippet.KW_CHECK.get()),
                new SnippetCompletionItem(context, Snippet.KW_CHECK_PANIC.get())
        );
    }

    protected List<LSCompletionItem> expressionCompletions(LSContext context) {
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        /*
        check and check panic expression starts with check and check panic keywords, Which has been added with actions.
        query pipeline starts with from keyword and also being added with the actions
         */
        List<LSCompletionItem> completionItems = new ArrayList<>(this.getPackagesCompletionItems(context));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TABLE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_SERVICE.get()));
        // to support start of string template expression
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_STRING.get()));
        // to support start of string template expression
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_XML.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_NEW.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FUNCTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_LET.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TYPEOF.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TRAP.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ERROR.get()));

        completionItems.add(new SnippetCompletionItem(context, Snippet.EXPR_ERROR.get()));

        List<Scope.ScopeEntry> filteredList = visibleSymbols.stream()
                .filter(scopeEntry -> scopeEntry.symbol instanceof BVarSymbol
                        && !(scopeEntry.symbol instanceof BOperatorSymbol))
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(filteredList, context));
        // TODO: anon function expressions, 
        return completionItems;
    }
}
