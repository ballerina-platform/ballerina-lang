package org.ballerinalang.langserver.completions.util.sorters;

import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Priority;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.List;

/**
 * Completion Item sorter for the match statement completions.
 */
public class MatchContextItemSorter extends CompletionItemSorter {
    /**
     * Sort Completion Items based on a particular criteria.
     *
     * @param ctx             Completion context
     * @param completionItems List of initial completion items
     */
    @Override
    public void sortItems(LSServiceOperationContext ctx, List<CompletionItem> completionItems) {
        completionItems.forEach(completionItem -> {
            if (completionItem.getDetail().equals(ItemResolverConstants.B_TYPE)) {
                completionItem.setSortText(Priority.PRIORITY150.toString());
            } else if (completionItem.getDetail().equals(ItemResolverConstants.FUNCTION_TYPE)
                    && InsertTextFormat.Snippet.equals(completionItem.getInsertTextFormat())) {
                completionItem.setDetail(ItemResolverConstants.SNIPPET_TYPE);
                completionItem.setSortText(Priority.PRIORITY120.toString());
            } else if (completionItem.getDetail().equals(ItemResolverConstants.FUNCTION_TYPE)) {
                completionItem.setSortText(Priority.PRIORITY140.toString());
            } else if (InsertTextFormat.Snippet.equals(completionItem.getInsertTextFormat())) {
                completionItem.setDetail(ItemResolverConstants.SNIPPET_TYPE);
                completionItem.setSortText(Priority.PRIORITY110.toString());
            } else {
                completionItem.setSortText(Priority.PRIORITY130.toString());
            }
        });
    }
}
