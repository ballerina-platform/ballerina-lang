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
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Provides the statement templates.
 */
public class StatementTemplateFilter extends AbstractSymbolFilter {
    @Override
    public List filterItems(LSServiceOperationContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        // Populate the statement templates

        // Populate If Statement template
        CompletionItem ifItem = new CompletionItem();
        ifItem.setLabel(ItemResolverConstants.IF);
        ifItem.setInsertText(Snippet.IF.toString());
        ifItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        completionItems.add(ifItem);

        // Populate While Statement template
        CompletionItem whileItem = new CompletionItem();
        whileItem.setLabel(ItemResolverConstants.WHILE);
        whileItem.setInsertText(Snippet.WHILE.toString());
        whileItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        completionItems.add(whileItem);

        // Populate Lock Statement template
        CompletionItem lockItem = new CompletionItem();
        lockItem.setLabel(ItemResolverConstants.LOCK);
        lockItem.setInsertText(Snippet.LOCK.toString());
        lockItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        completionItems.add(lockItem);

        // Populate Foreach Statement template
        CompletionItem forEachItem = new CompletionItem();
        forEachItem.setLabel(ItemResolverConstants.FOREACH);
        forEachItem.setInsertText(Snippet.FOREACH.toString());
        forEachItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        completionItems.add(forEachItem);

        // Populate Bind Statement template
        CompletionItem bindItem = new CompletionItem();
        bindItem.setLabel(ItemResolverConstants.BIND);
        bindItem.setInsertText(Snippet.BIND.toString());
        bindItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        completionItems.add(bindItem);

        // Populate Fork Statement template
        // TODO: Should make this a snippet type and move to specific sorters
        CompletionItem forkItem = new CompletionItem();
        forkItem.setLabel(ItemResolverConstants.FORK);
        forkItem.setInsertText(Snippet.FORK.toString());
        forkItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        completionItems.add(forkItem);

        // Populate Try Catch Statement template
        CompletionItem tryCatchItem = new CompletionItem();
        tryCatchItem.setLabel(ItemResolverConstants.TRY);
        tryCatchItem.setInsertText(Snippet.TRY_CATCH.toString());
        tryCatchItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        completionItems.add(tryCatchItem);

        // Populate Transaction Statement template
        CompletionItem transactionItem = new CompletionItem();
        transactionItem.setLabel(ItemResolverConstants.TRANSACTION);
        transactionItem.setInsertText(Snippet.TRANSACTION.toString());
        transactionItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        completionItems.add(transactionItem);

        // Populate Trigger Worker Statement template
        CompletionItem workerInvokeItem = new CompletionItem();
        workerInvokeItem.setLabel(ItemResolverConstants.TRIGGER_WORKER);
        workerInvokeItem.setInsertText(Snippet.TRIGGER_WORKER.toString());
        workerInvokeItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        completionItems.add(workerInvokeItem);

        // Populate Worker Reply Statement template
        CompletionItem workerReplyItem = new CompletionItem();
        workerReplyItem.setLabel(ItemResolverConstants.WORKER_REPLY);
        workerReplyItem.setInsertText(Snippet.WORKER_REPLY.toString());
        workerReplyItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        completionItems.add(workerReplyItem);
        
        if (completionContext.get(CompletionKeys.LOOP_COUNT_KEY) > 0
                && !completionContext.get(CompletionKeys.CURRENT_NODE_TRANSACTION_KEY)) {
            /*
            Populate Next Statement template only if enclosed within a looping construct
            and not in immediate transaction construct
             */
            CompletionItem nextItem = new CompletionItem();
            nextItem.setLabel(ItemResolverConstants.NEXT);
            nextItem.setInsertText(Snippet.NEXT.toString());
            nextItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
            completionItems.add(nextItem);
        }
        
        if (completionContext.get(CompletionKeys.LOOP_COUNT_KEY) > 0) {
            // Populate Break Statement template only if there is an enclosing looping construct such as while/ foreach
            CompletionItem breakItem = new CompletionItem();
            breakItem.setLabel(ItemResolverConstants.BREAK);
            breakItem.setInsertText(Snippet.BREAK.toString());
            breakItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
            completionItems.add(breakItem);
        }

        // Populate Return Statement template
        CompletionItem returnItem = new CompletionItem();
        returnItem.setLabel(ItemResolverConstants.RETURN);
        returnItem.setInsertText(Snippet.RETURN.toString());
        returnItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        completionItems.add(returnItem);
        
        if (completionContext.get(CompletionKeys.TRANSACTION_COUNT_KEY) > 0) {
            // Populate Worker Reply Statement template on if there is at least one enclosing transaction construct 
            CompletionItem abortItem = new CompletionItem();
            abortItem.setLabel(ItemResolverConstants.ABORT);
            abortItem.setInsertText(Snippet.ABORT.toString());
            abortItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
            completionItems.add(abortItem);
        }

        completionItems.sort(Comparator.comparing(CompletionItem::getLabel));

        // Set the insert text format to be snippet supported format
        completionItems.forEach(completionItem -> completionItem.setInsertTextFormat(InsertTextFormat.Snippet));

        return completionItems;
    }
}
