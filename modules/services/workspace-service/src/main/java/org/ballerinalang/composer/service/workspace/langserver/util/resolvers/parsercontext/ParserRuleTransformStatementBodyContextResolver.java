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
import org.ballerinalang.composer.service.workspace.langserver.util.filters.BTypeFilter;
import org.ballerinalang.composer.service.workspace.langserver.util.resolvers.AbstractItemResolver;
import org.ballerinalang.composer.service.workspace.langserver.util.resolvers.ItemResolverConstants;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;
import org.ballerinalang.model.SimpleVariableDef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parser rule based Item resolver for the transform statement
 */
public class ParserRuleTransformStatementBodyContextResolver extends AbstractItemResolver {
    @Override
    public ArrayList<CompletionItem> resolveItems(SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols,
                                                  HashMap<Class, AbstractItemResolver> resolvers) {

        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        BTypeFilter bTypeFilter = new BTypeFilter();
        populateCompletionItemList(bTypeFilter.filterItems(dataModel, symbols, null), completionItems);

        List<SymbolInfo> variableDefs =  symbols.stream()
                .filter(symbolInfo -> symbolInfo.getSymbol() instanceof SimpleVariableDef)
                .collect(Collectors.toList());
        populateCompletionItemList(variableDefs, completionItems);

        // Populate Transaction Statement template
        CompletionItem transactionItem = new CompletionItem();
        transactionItem.setLabel(ItemResolverConstants.TRANSACTION);
        transactionItem.setInsertText(ItemResolverConstants.TRANSACTION_TEMPLATE);
        transactionItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        transactionItem.setSortText(ItemResolverConstants.PRIORITY_6);
        completionItems.add(transactionItem);

        return completionItems;
    }
}
