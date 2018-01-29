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

import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.completions.consts.ItemResolverConstants;
import org.ballerinalang.langserver.completions.consts.Priority;
import org.ballerinalang.langserver.completions.consts.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Provides the statement templates.
 */
public class StatementTemplateFilter extends SymbolFilter {
    @Override
    public List filterItems(TextDocumentServiceContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        // Populate the statement templates

        // Populate If Statement template
        CompletionItem ifItem = new CompletionItem();
        ifItem.setLabel(ItemResolverConstants.IF);
        ifItem.setInsertText(Snippet.IF.toString());
        ifItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        ifItem.setSortText(Priority.PRIORITY6.name());
        completionItems.add(ifItem);

        // Populate While Statement template
        CompletionItem whileItem = new CompletionItem();
        whileItem.setLabel(ItemResolverConstants.WHILE);
        whileItem.setInsertText(Snippet.WHILE.toString());
        whileItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        whileItem.setSortText(Priority.PRIORITY6.name());
        completionItems.add(whileItem);

        // Populate Bind Statement template
        CompletionItem bindItem = new CompletionItem();
        bindItem.setLabel(ItemResolverConstants.BIND);
        bindItem.setInsertText(Snippet.BIND.toString());
        bindItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        bindItem.setSortText(Priority.PRIORITY6.name());
        completionItems.add(bindItem);

        // Populate Endpoint Statement template
        CompletionItem endpointItem = new CompletionItem();
        endpointItem.setLabel(ItemResolverConstants.ENDPOINT);
        endpointItem.setInsertText(Snippet.ENDPOINT.toString());
        endpointItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        endpointItem.setSortText(Priority.PRIORITY6.name());
        completionItems.add(endpointItem);

        // Populate Iterate Statement template
        CompletionItem iterateItem = new CompletionItem();
        iterateItem.setLabel(ItemResolverConstants.ITERATE);
        iterateItem.setInsertText(Snippet.ITERATE.toString());
        iterateItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        iterateItem.setSortText(Priority.PRIORITY6.name());
        completionItems.add(iterateItem);

        // Populate Fork Statement template
        CompletionItem forkItem = new CompletionItem();
        forkItem.setLabel(ItemResolverConstants.FORK);
        forkItem.setInsertText(Snippet.FORK.toString());
        forkItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        forkItem.setSortText(Priority.PRIORITY6.name());
        completionItems.add(forkItem);

        // Populate Try Catch Statement template
        CompletionItem tryCatchItem = new CompletionItem();
        tryCatchItem.setLabel(ItemResolverConstants.TRY);
        tryCatchItem.setInsertText(Snippet.TRY_CATCH.toString());
        tryCatchItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        tryCatchItem.setSortText(Priority.PRIORITY6.name());
        completionItems.add(tryCatchItem);

        // Populate Transaction Statement template
        CompletionItem transactionItem = new CompletionItem();
        transactionItem.setLabel(ItemResolverConstants.TRANSACTION);
        transactionItem.setInsertText(Snippet.TRANSACTION.toString());
        transactionItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        transactionItem.setSortText(Priority.PRIORITY6.name());
        completionItems.add(transactionItem);

        // Populate Trigger Worker Statement template
        CompletionItem workerInvokeItem = new CompletionItem();
        workerInvokeItem.setLabel(ItemResolverConstants.TRIGGER_WORKER);
        workerInvokeItem.setInsertText(Snippet.TRIGGER_WORKER.toString());
        workerInvokeItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        workerInvokeItem.setSortText(Priority.PRIORITY6.name());
        completionItems.add(workerInvokeItem);

        // Populate Worker Reply Statement template
        CompletionItem workerReplyItem = new CompletionItem();
        workerReplyItem.setLabel(ItemResolverConstants.WORKER_REPLY);
        workerReplyItem.setInsertText(Snippet.WORKER_REPLY.toString());
        workerReplyItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        workerReplyItem.setSortText(Priority.PRIORITY6.name());
        completionItems.add(workerReplyItem);

        // Populate Next Statement template
        CompletionItem nextItem = new CompletionItem();
        nextItem.setLabel(ItemResolverConstants.NEXT);
        nextItem.setInsertText(Snippet.NEXT.toString());
        nextItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        nextItem.setSortText(Priority.PRIORITY6.name());
        completionItems.add(nextItem);

        // Populate Break Statement template
        CompletionItem breakItem = new CompletionItem();
        breakItem.setLabel(ItemResolverConstants.BREAK);
        breakItem.setInsertText(Snippet.BREAK.toString());
        breakItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        breakItem.setSortText(Priority.PRIORITY6.name());
        completionItems.add(breakItem);

        // Populate Return Statement template
        CompletionItem returnItem = new CompletionItem();
        returnItem.setLabel(ItemResolverConstants.RETURN);
        returnItem.setInsertText(Snippet.RETURN.toString());
        returnItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        returnItem.setSortText(Priority.PRIORITY6.name());
        completionItems.add(returnItem);

        // Populate Reply Statement template
        CompletionItem replyItem = new CompletionItem();
        replyItem.setLabel(ItemResolverConstants.REPLY);
        replyItem.setInsertText(Snippet.REPLY.toString());
        replyItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        replyItem.setSortText(Priority.PRIORITY6.name());
        completionItems.add(replyItem);

        // Populate Worker Reply Statement template
        CompletionItem abortItem = new CompletionItem();
        abortItem.setLabel(ItemResolverConstants.ABORT);
        abortItem.setInsertText(Snippet.ABORT.toString());
        abortItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        abortItem.setSortText(Priority.PRIORITY6.name());
        completionItems.add(abortItem);

        completionItems.sort(Comparator.comparing(CompletionItem::getLabel));

        // Set the insert text format to be snippet supported format
        completionItems.forEach(completionItem -> completionItem.setInsertTextFormat(InsertTextFormat.Snippet));

        return completionItems;
    }
}
