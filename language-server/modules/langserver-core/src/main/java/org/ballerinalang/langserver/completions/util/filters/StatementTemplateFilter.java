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
        CompletionItem ifItem = Snippet.STMT_IF.get().build(new CompletionItem(), isSnippet);
        completionItems.add(ifItem);

        // Populate While Statement template
        CompletionItem whileItem = Snippet.STMT_WHILE.get().build(new CompletionItem(), isSnippet);
        completionItems.add(whileItem);

        // Populate Lock Statement template
        CompletionItem lockItem = Snippet.STMT_LOCK.get().build(new CompletionItem(), isSnippet);
        completionItems.add(lockItem);

        // Populate Foreach Statement template
        CompletionItem forEachItem = Snippet.STMT_FOREACH.get().build(new CompletionItem(), isSnippet);
        completionItems.add(forEachItem);

        // Populate Fork Statement template
        CompletionItem forkItem = Snippet.STMT_FORK_JOIN.get().build(new CompletionItem(), isSnippet);
        completionItems.add(forkItem);

        // Populate Transaction Statement template
        CompletionItem transactionItem = Snippet.STMT_TRANSACTION.get().build(new CompletionItem(), isSnippet);
        completionItems.add(transactionItem);

        // Populate Trigger Worker Statement template
        CompletionItem workerTriggerItem = new CompletionItem();
        Snippet.STMT_WORKER_TRIGGER.get().build(workerTriggerItem, isSnippet);
        completionItems.add(workerTriggerItem);

        // Populate Worker Reply Statement template
        CompletionItem workerReplyItem = Snippet.STMT_WORKER_REPLY.get().build(new CompletionItem(), isSnippet);
        completionItems.add(workerReplyItem);
        
        // Populate Match statement template
        CompletionItem matchItem = Snippet.STMT_MATCH.get().build(new CompletionItem(), isSnippet);
        completionItems.add(matchItem);
        
        if (context.get(CompletionKeys.LOOP_COUNT_KEY) > 0 
                && !context.get(CompletionKeys.CURRENT_NODE_TRANSACTION_KEY)) {
            /*
            Populate Continue Statement template only if enclosed within a looping construct
            and not in immediate transaction construct
             */
            CompletionItem nextItem = Snippet.STMT_CONTINUE.get().build(new CompletionItem(), isSnippet);
            completionItems.add(nextItem);
        }
        
        if (context.get(CompletionKeys.LOOP_COUNT_KEY) > 0) {
            // Populate Break Statement template only if there is an enclosing looping construct such as while/ foreach
            CompletionItem breakItem = new CompletionItem();
            Snippet.STMT_BREAK.get().build(breakItem, isSnippet);
            completionItems.add(breakItem);
        }

        // Populate Return Statement template
        CompletionItem returnItem = new CompletionItem();
        Snippet.STMT_RETURN.get().build(returnItem, isSnippet);
        completionItems.add(returnItem);
        
        if (context.get(CompletionKeys.TRANSACTION_COUNT_KEY) > 0) {
            // Populate Worker Reply Statement template on if there is at least one enclosing transaction construct 
            CompletionItem abortItem = new CompletionItem();
            Snippet.STMT_ABORT.get().build(abortItem, isSnippet);
            completionItems.add(abortItem);

            CompletionItem retryItem = new CompletionItem();
            Snippet.STMT_RETRY.get().build(retryItem, isSnippet);
            completionItems.add(retryItem);
        }

        // Populate Throw Statement template
        CompletionItem throwItem = new CompletionItem();
        Snippet.STMT_PANIC.get().build(throwItem, isSnippet);
        completionItems.add(throwItem);

        completionItems.sort(Comparator.comparing(CompletionItem::getLabel));

        // Set the insert text format to be snippet supported format
        completionItems.forEach(completionItem -> completionItem.setInsertTextFormat(InsertTextFormat.Snippet));

        return Either.forLeft(completionItems);
    }
}
