package org.ballerinalang.langserver.completions.providers.context;

import io.ballerinalang.compiler.syntax.tree.Node;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BlockNodeContextProvider<T extends Node> extends AbstractCompletionProvider<T> {
    public BlockNodeContextProvider(Kind kind) {
        super(kind);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, T node) throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        completionItems.addAll(getStaticCompletionItems(context));
        completionItems.addAll(getStatementCompletionItems(context));
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
        // Add the error snippet
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_ERROR.get()));
        // Add the error type CompletionItem
        completionItems.add(CommonUtil.getErrorTypeCompletionItem(context));
        // Add the checkpanic keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CHECK_PANIC.get()));
        // Add the check keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CHECK.get()));
        // Add the final keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FINAL.get()));

        return completionItems;
    }

    protected List<LSCompletionItem> getStatementCompletionItems(LSContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        // Populate If Statement template
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_IF.get()));
        if (context.get(CompletionKeys.PREVIOUS_NODE_KEY) instanceof BLangIf) {
            // Populate Else If Statement template
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_ELSE_IF.get()));
            // Populate Else Statement template
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_ELSE.get()));
        }

        // Populate While Statement template
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_WHILE.get()));
        // Populate Lock Statement template
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_LOCK.get()));
        // Populate Foreach Statement template
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_FOREACH.get()));
        // Populate Fork Statement template
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_FORK.get()));
        // Populate Transaction Statement template
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_TRANSACTION.get()));
        // Populate Retry Transaction Statement template
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_RETRY_TRANSACTION.get()));
        // Populate Match statement template
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_MATCH.get()));

        if (context.get(CompletionKeys.LOOP_COUNT_KEY) > 0
                && !context.get(CompletionKeys.CURRENT_NODE_TRANSACTION_KEY)) {
            /*
            Populate Continue Statement template only if enclosed within a looping construct
            and not in immediate transaction construct
             */
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_CONTINUE.get()));
        }

        if (context.get(CompletionKeys.LOOP_COUNT_KEY) > 0) {
            // Populate Break Statement template only if there is an enclosing looping construct such as while/ foreach
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_BREAK.get()));
        }
        // Populate Return Statement template
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_RETURN.get()));

        if (context.get(CompletionKeys.TRANSACTION_COUNT_KEY) > 0) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_ROLLBACK.get()));
        }
        // Populate Throw Statement template
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_PANIC.get()));

        return completionItems;
    }

    protected List<LSCompletionItem> getSymbolCompletions(LSContext context) {
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        // TODO: Can we get this filter to a common place
        List<Scope.ScopeEntry> filteredList = visibleSymbols.stream()
                .filter(scopeEntry -> scopeEntry.symbol instanceof BVarSymbol
                        && !(scopeEntry.symbol instanceof BOperatorSymbol))
                .collect(Collectors.toList());

        return this.getCompletionItemList(filteredList, context);
    }
}
