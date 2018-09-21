/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.langserver.completions.util.filters;

import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Provides the statement templates.
 */
public class StatementTemplateFilter extends AbstractSymbolFilter {
    @Override
    public Either<List<CompletionItem>, List<SymbolInfo>> filterItems(LSServiceOperationContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        boolean isSnippet = context.get(CompletionKeys.CLIENT_CAPABILITIES_KEY).getCompletionItem().getSnippetSupport();

        // Populate If Statement template
        CompletionItem ifItem = new CompletionItem();
        Snippet.STMT_IF.getBlock().populateCompletionItem(ifItem, isSnippet);
        ifItem.setLabel(ItemResolverConstants.IF);
        ifItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        completionItems.add(ifItem);

        // Populate While Statement template
        CompletionItem whileItem = new CompletionItem();
        Snippet.STMT_WHILE.getBlock().populateCompletionItem(whileItem, isSnippet);
        whileItem.setLabel(ItemResolverConstants.WHILE);
        whileItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        completionItems.add(whileItem);

        // Populate Lock Statement template
        CompletionItem lockItem = new CompletionItem();
        Snippet.STMT_LOCK.getBlock().populateCompletionItem(lockItem, isSnippet);
        lockItem.setLabel(ItemResolverConstants.LOCK);
        lockItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        completionItems.add(lockItem);

        // Populate Foreach Statement template
        CompletionItem forEachItem = new CompletionItem();
        Snippet.STMT_FOREACH.getBlock().populateCompletionItem(forEachItem, isSnippet);
        forEachItem.setLabel(ItemResolverConstants.FOREACH);
        forEachItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        completionItems.add(forEachItem);

        // Populate Fork Statement template
        CompletionItem forkItem = new CompletionItem();
        Snippet.STMT_FORK_JOIN.getBlock().populateCompletionItem(forkItem, isSnippet);
        forkItem.setLabel(ItemResolverConstants.FORK);
        forkItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        completionItems.add(forkItem);

        // Populate Try Catch Statement template
        CompletionItem tryCatchItem = new CompletionItem();
        Snippet.STMT_TRY_CATCH.getBlock().populateCompletionItem(tryCatchItem, isSnippet);
        tryCatchItem.setLabel(ItemResolverConstants.TRY);
        tryCatchItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        completionItems.add(tryCatchItem);

        // Populate Transaction Statement template
        CompletionItem transactionItem = new CompletionItem();
        Snippet.STMT_TRANSACTION.getBlock().populateCompletionItem(transactionItem, isSnippet);
        transactionItem.setLabel(ItemResolverConstants.TRANSACTION);
        transactionItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        completionItems.add(transactionItem);

        // Populate Trigger Worker Statement template
        CompletionItem workerTriggerItem = new CompletionItem();
        Snippet.STMT_WORKER_TRIGGER.getBlock().populateCompletionItem(workerTriggerItem, isSnippet);
        workerTriggerItem.setLabel(ItemResolverConstants.TRIGGER_WORKER);
        workerTriggerItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        completionItems.add(workerTriggerItem);

        // Populate Worker Reply Statement template
        CompletionItem workerReplyItem = new CompletionItem();
        Snippet.STMT_WORKER_REPLY.getBlock().populateCompletionItem(workerReplyItem, isSnippet);
        workerReplyItem.setLabel(ItemResolverConstants.WORKER_REPLY);
        workerReplyItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        completionItems.add(workerReplyItem);
        
        // Populate Match statement template
        CompletionItem matchItem = new CompletionItem();
        Snippet.STMT_MATCH.getBlock().populateCompletionItem(matchItem, isSnippet);
        matchItem.setLabel(ItemResolverConstants.MATCH);
        matchItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        completionItems.add(matchItem);
        
        if (context.get(CompletionKeys.LOOP_COUNT_KEY) > 0 
                && !context.get(CompletionKeys.CURRENT_NODE_TRANSACTION_KEY)) {
            /*
            Populate Continue Statement template only if enclosed within a looping construct
            and not in immediate transaction construct
             */
            CompletionItem nextItem = new CompletionItem();
            Snippet.STMT_CONTINUE.getBlock().populateCompletionItem(nextItem, isSnippet);
            nextItem.setLabel(ItemResolverConstants.CONTINUE);
            nextItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
            completionItems.add(nextItem);
        }
        
        if (context.get(CompletionKeys.LOOP_COUNT_KEY) > 0) {
            // Populate Break Statement template only if there is an enclosing looping construct such as while/ foreach
            CompletionItem breakItem = new CompletionItem();
            Snippet.STMT_BREAK.getBlock().populateCompletionItem(breakItem, isSnippet);
            breakItem.setLabel(ItemResolverConstants.BREAK);
            breakItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
            completionItems.add(breakItem);
        }

        // Populate Return Statement template
        CompletionItem returnItem = new CompletionItem();
        Snippet.STMT_RETURN.getBlock().populateCompletionItem(returnItem, isSnippet);
        returnItem.setLabel(ItemResolverConstants.RETURN);
        returnItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        completionItems.add(returnItem);
        
        if (context.get(CompletionKeys.TRANSACTION_COUNT_KEY) > 0) {
            // Populate Worker Reply Statement template on if there is at least one enclosing transaction construct 
            CompletionItem abortItem = new CompletionItem();
            Snippet.STMT_ABORT.getBlock().populateCompletionItem(abortItem, isSnippet);
            abortItem.setLabel(ItemResolverConstants.ABORT);
            abortItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
            completionItems.add(abortItem);

            CompletionItem retryItem = new CompletionItem();
            Snippet.STMT_RETRY.getBlock().populateCompletionItem(retryItem, isSnippet);
            retryItem.setLabel(ItemResolverConstants.RETRY);
            retryItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
            completionItems.add(retryItem);
        }

        // Populate Throw Statement template
        CompletionItem throwItem = new CompletionItem();
        Snippet.STMT_THROW.getBlock().populateCompletionItem(throwItem, isSnippet);
        throwItem.setLabel(ItemResolverConstants.THROW);
        throwItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        completionItems.add(throwItem);

        completionItems.sort(Comparator.comparing(CompletionItem::getLabel));

        // Set the insert text format to be snippet supported format
        completionItems.forEach(completionItem -> completionItem.setInsertTextFormat(InsertTextFormat.Snippet));

        return Either.forLeft(completionItems);
    }
}
