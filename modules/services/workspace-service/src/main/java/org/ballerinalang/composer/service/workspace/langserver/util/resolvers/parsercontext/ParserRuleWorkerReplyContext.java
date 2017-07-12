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

package org.ballerinalang.composer.service.workspace.langserver.util.resolvers.parsercontext;

import org.ballerinalang.composer.service.workspace.langserver.SymbolInfo;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.langserver.util.resolvers.AbstractItemResolver;
import org.ballerinalang.composer.service.workspace.langserver.util.resolvers.ItemResolverConstants;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;
import org.ballerinalang.model.Worker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parser rule based Item resolver for the Worker reply statement
 */
public class ParserRuleWorkerReplyContext extends AbstractItemResolver {
    @Override
    public ArrayList<CompletionItem> resolveItems(SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols,
                                                  HashMap<Class, AbstractItemResolver> resolvers) {

        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<SymbolInfo> workers = symbols.stream()
                .filter(symbolInfo -> symbolInfo.getSymbol() instanceof Worker)
                .collect(Collectors.toList());

        workers.forEach(symbolInfo -> {
            CompletionItem workerCompletionItem = new CompletionItem();
            workerCompletionItem.setInsertText(symbolInfo.getSymbolName());
            workerCompletionItem.setDetail(ItemResolverConstants.WORKER_TYPE);
            workerCompletionItem.setSortText(ItemResolverConstants.PRIORITY_7);
            workerCompletionItem.setLabel(symbolInfo.getSymbolName());
            completionItems.add(workerCompletionItem);
        });

        return completionItems;
    }
}
