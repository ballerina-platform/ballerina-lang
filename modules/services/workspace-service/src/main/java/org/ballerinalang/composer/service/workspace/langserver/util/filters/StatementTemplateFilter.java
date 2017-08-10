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

package org.ballerinalang.composer.service.workspace.langserver.util.filters;

import org.ballerinalang.composer.service.workspace.langserver.SymbolInfo;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.langserver.util.resolvers.ItemResolverConstants;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Provides the statement templates
 */
public class StatementTemplateFilter implements SymbolFilter {
    @Override
    public List<CompletionItem> filterItems(SuggestionsFilterDataModel dataModel,
                                            ArrayList<SymbolInfo> symbols, HashMap<String, Object> properties) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        // Populate the statement templates

        // Populate If Statement template
        CompletionItem ifItem = new CompletionItem();
        ifItem.setLabel(ItemResolverConstants.IF);
        ifItem.setInsertText(ItemResolverConstants.IF_TEMPLATE);
        ifItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        ifItem.setSortText(ItemResolverConstants.PRIORITY_6);
        completionItems.add(ifItem);

        // Populate While Statement template
        CompletionItem whileItem = new CompletionItem();
        whileItem.setLabel(ItemResolverConstants.WHILE);
        whileItem.setInsertText(ItemResolverConstants.WHILE_TEMPLATE);
        whileItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        whileItem.setSortText(ItemResolverConstants.PRIORITY_6);
        completionItems.add(whileItem);

        // Populate Iterate Statement template
        CompletionItem iterateItem = new CompletionItem();
        iterateItem.setLabel(ItemResolverConstants.ITERATE);
        iterateItem.setInsertText(ItemResolverConstants.ITERATE_TEMPLATE);
        iterateItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        iterateItem.setSortText(ItemResolverConstants.PRIORITY_6);
        completionItems.add(iterateItem);

        // Populate Fork Statement template
        CompletionItem forkItem = new CompletionItem();
        forkItem.setLabel(ItemResolverConstants.FORK);
        forkItem.setInsertText(ItemResolverConstants.FORK_TEMPLATE);
        forkItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        forkItem.setSortText(ItemResolverConstants.PRIORITY_6);
        completionItems.add(forkItem);

        // Populate Try Catch Statement template
        CompletionItem tryCatchItem = new CompletionItem();
        tryCatchItem.setLabel(ItemResolverConstants.TRY);
        tryCatchItem.setInsertText(ItemResolverConstants.TRY_CATCH_TEMPLATE);
        tryCatchItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        tryCatchItem.setSortText(ItemResolverConstants.PRIORITY_6);
        completionItems.add(tryCatchItem);

        // Populate Transaction Statement template
        CompletionItem transactionItem = new CompletionItem();
        transactionItem.setLabel(ItemResolverConstants.TRANSACTION);
        transactionItem.setInsertText(ItemResolverConstants.TRANSACTION_TEMPLATE);
        transactionItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        transactionItem.setSortText(ItemResolverConstants.PRIORITY_6);
        completionItems.add(transactionItem);

        // Populate Transform Statement template
        CompletionItem transformItem = new CompletionItem();
        transformItem.setLabel(ItemResolverConstants.TRANSFORM);
        transformItem.setInsertText(ItemResolverConstants.TRANSFORM_TEMPLATE);
        transformItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        transformItem.setSortText(ItemResolverConstants.PRIORITY_6);
        completionItems.add(transformItem);

        // Populate Trigger Worker Statement template
        CompletionItem workerInvokeItem = new CompletionItem();
        workerInvokeItem.setLabel(ItemResolverConstants.TRIGGER_WORKER);
        workerInvokeItem.setInsertText(ItemResolverConstants.TRIGGER_WORKER_TEMPLATE);
        workerInvokeItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        workerInvokeItem.setSortText(ItemResolverConstants.PRIORITY_6);
        completionItems.add(workerInvokeItem);

        // Populate Worker Reply Statement template
        CompletionItem workerReplyItem = new CompletionItem();
        workerReplyItem.setLabel(ItemResolverConstants.WORKER_REPLY);
        workerReplyItem.setInsertText(ItemResolverConstants.WORKER_REPLY_TEMPLATE);
        workerReplyItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        workerReplyItem.setSortText(ItemResolverConstants.PRIORITY_6);
        completionItems.add(workerReplyItem);

        // Populate Continue Statement template
        CompletionItem continueItem = new CompletionItem();
        continueItem.setLabel(ItemResolverConstants.CONTINUE);
        continueItem.setInsertText(ItemResolverConstants.CONTINUE_TEMPLATE);
        continueItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        continueItem.setSortText(ItemResolverConstants.PRIORITY_6);
        completionItems.add(continueItem);

        // Populate Break Statement template
        CompletionItem breakItem = new CompletionItem();
        breakItem.setLabel(ItemResolverConstants.BREAK);
        breakItem.setInsertText(ItemResolverConstants.BREAK_TEMPLATE);
        breakItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        breakItem.setSortText(ItemResolverConstants.PRIORITY_6);
        completionItems.add(breakItem);

        // Populate Return Statement template
        CompletionItem returnItem = new CompletionItem();
        returnItem.setLabel(ItemResolverConstants.RETURN);
        returnItem.setInsertText(ItemResolverConstants.RETURN_TEMPLATE);
        returnItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        returnItem.setSortText(ItemResolverConstants.PRIORITY_6);
        completionItems.add(returnItem);

        // Populate Reply Statement template
        CompletionItem replyItem = new CompletionItem();
        replyItem.setLabel(ItemResolverConstants.REPLY);
        replyItem.setInsertText(ItemResolverConstants.REPLY_TEMPLATE);
        replyItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        replyItem.setSortText(ItemResolverConstants.PRIORITY_6);
        completionItems.add(replyItem);

        // Populate Worker Reply Statement template
        CompletionItem abortItem = new CompletionItem();
        abortItem.setLabel(ItemResolverConstants.ABORT);
        abortItem.setInsertText(ItemResolverConstants.ABORT_TEMPLATE);
        abortItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        abortItem.setSortText(ItemResolverConstants.PRIORITY_6);
        completionItems.add(abortItem);

        CompletionItem retryItem = new CompletionItem();
        retryItem.setLabel(ItemResolverConstants.RETRY);
        retryItem.setInsertText(ItemResolverConstants.RETRY_TEMPLATE);
        retryItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        retryItem.setSortText(ItemResolverConstants.PRIORITY_6);
        completionItems.add(retryItem);

        completionItems.sort(Comparator.comparing(CompletionItem::getLabel));

        return completionItems;
    }
}
