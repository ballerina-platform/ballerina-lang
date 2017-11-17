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
package org.ballerinalang.composer.service.workspace.langserver.util.completion.resolvers;

import org.ballerinalang.composer.service.workspace.langserver.SymbolInfo;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ServiceContextResolver.
 */
public class ServiceContextResolver extends AbstractItemResolver {

    @Override
    public ArrayList<CompletionItem> resolveItems(SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols,
                                           HashMap<Class, AbstractItemResolver> resolvers) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        // TODO: Add annotations
        this.addResourceCompletionItem(completionItems);
        this.addTypes(completionItems, symbols);
        return completionItems;
    }

    private void addResourceCompletionItem(List<CompletionItem> completionItems) {
        CompletionItem resource = new CompletionItem();
        resource.setLabel(ItemResolverConstants.RESOURCE_TYPE);
        resource.setInsertText(ItemResolverConstants.RESOURCE_TEMPLATE);
        resource.setDetail(ItemResolverConstants.SNIPPET_TYPE);
        resource.setSortText(ItemResolverConstants.PRIORITY_7);
        completionItems.add(resource);
    }

    private void addTypes(List<CompletionItem> completionItems, ArrayList<SymbolInfo> symbols) {
        List<SymbolInfo> filteredSymbols = symbols.stream()
                .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol instanceof BTypeSymbol)
                .collect(Collectors.toList());
        this.populateCompletionItemList(filteredSymbols, completionItems);
    }
}
