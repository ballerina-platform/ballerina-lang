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

import org.ballerinalang.langserver.completions.SuggestionsFilterDataModel;
import org.ballerinalang.langserver.completions.consts.CompletionItemResolver;
import org.ballerinalang.langserver.completions.consts.ItemResolverConstants;
import org.ballerinalang.langserver.completions.consts.Priority;
import org.ballerinalang.langserver.completions.consts.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Block statement Context Resolver.
 */
public class BlockStatementContextResolver extends AbstractItemResolver {
    @Override
    public ArrayList<CompletionItem> resolveItems(SuggestionsFilterDataModel dataModel) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        AbstractItemResolver itemResolver;

        if (dataModel.getParserRuleContext() != null) {
            itemResolver = CompletionItemResolver.getResolverByClass(dataModel.getParserRuleContext().getClass());
            completionItems.addAll(itemResolver.resolveItems(dataModel));
        } else {
            CompletionItem workerItem = new CompletionItem();
            workerItem.setLabel(ItemResolverConstants.WORKER);
            workerItem.setInsertText(Snippet.WORKER.toString());
            workerItem.setInsertTextFormat(InsertTextFormat.Snippet);
            workerItem.setDetail(ItemResolverConstants.WORKER_TYPE);
            workerItem.setSortText(Priority.PRIORITY6.name());
            workerItem.setKind(CompletionItemKind.Snippet);
            completionItems.add(workerItem);

            itemResolver = CompletionItemResolver.getResolverByClass(StatementContextResolver.class);
            completionItems.addAll(itemResolver.resolveItems(dataModel));

            // Add the var keyword
            CompletionItem varKeyword = new CompletionItem();
            varKeyword.setInsertText("var ");
            varKeyword.setLabel("var");
            varKeyword.setDetail(ItemResolverConstants.KEYWORD_TYPE);
            varKeyword.setSortText(Priority.PRIORITY6.name());
            completionItems.add(varKeyword);
        }

        HashMap<String, String> prioritiesMap = new HashMap<>();
        prioritiesMap.put(ItemResolverConstants.PACKAGE_TYPE, Priority.PRIORITY6.name());
        prioritiesMap.put(ItemResolverConstants.B_TYPE, Priority.PRIORITY7.name());
        this.assignItemPriorities(prioritiesMap, completionItems);

        return completionItems;
    }
}
