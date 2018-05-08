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
package org.ballerinalang.langserver.completions.resolvers;

import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.filters.StatementTemplateFilter;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.ArrayList;

/**
 * Default resolver for the completion items.
 */
public class DefaultResolver extends AbstractItemResolver {
    @Override
    @SuppressWarnings("unchecked")
    public ArrayList<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        CompletionItem workerItem = new CompletionItem();
        workerItem.setLabel(ItemResolverConstants.WORKER);
        workerItem.setInsertText(Snippet.WORKER.toString());
        workerItem.setInsertTextFormat(InsertTextFormat.Snippet);
        workerItem.setDetail(ItemResolverConstants.WORKER_TYPE);
        completionItems.add(workerItem);

        populateCompletionItemList(completionContext.get(CompletionKeys.VISIBLE_SYMBOLS_KEY), completionItems);

        // Add the statement templates
        StatementTemplateFilter statementTemplateFilter = new StatementTemplateFilter();
        completionItems.addAll(statementTemplateFilter.filterItems(completionContext));

        return completionItems;
    }
}
