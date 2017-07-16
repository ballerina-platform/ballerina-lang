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
import org.ballerinalang.composer.service.workspace.langserver.util.filters.PackageActionAndFunctionFilter;
import org.ballerinalang.composer.service.workspace.langserver.util.filters.StatementTemplateFilter;
import org.ballerinalang.composer.service.workspace.langserver.util.resolvers.AbstractItemResolver;
import org.ballerinalang.composer.service.workspace.langserver.util.resolvers.ItemResolverConstants;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Parser rule based statement context resolver
 */
public class ParserRuleStatementContextResolver extends AbstractItemResolver {
    @Override
    public ArrayList<CompletionItem> resolveItems(SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols,
                                                  HashMap<Class, AbstractItemResolver> resolvers) {

        HashMap<String, Integer> prioritiesMap = new HashMap<>();

        // Here we specifically need to check whether the statement is function invocation,
        // action invocation or worker invocation
        if (isActionOrFunctionInvocationStatement(dataModel)) {
            PackageActionAndFunctionFilter actionAndFunctionFilter = new PackageActionAndFunctionFilter();

            ArrayList<CompletionItem> completionItems = actionAndFunctionFilter
                    .getCompletionItems(actionAndFunctionFilter.filterItems(dataModel, symbols, null));

            prioritiesMap.put(ItemResolverConstants.FUNCTION_TYPE, ItemResolverConstants.PRIORITY_7);
            prioritiesMap.put(ItemResolverConstants.ACTION_TYPE, ItemResolverConstants.PRIORITY_6);
            this.assignItemPriorities(prioritiesMap, completionItems);

            return completionItems;
        } else {
            ArrayList<CompletionItem> completionItems = new ArrayList<>();
            populateCompletionItemList(symbols, completionItems);
            StatementTemplateFilter statementTemplateFilter = new StatementTemplateFilter();
            // Add the statement templates
            completionItems.addAll(statementTemplateFilter.filterItems(dataModel, symbols, null));

            prioritiesMap.put(ItemResolverConstants.PACKAGE_TYPE, ItemResolverConstants.PRIORITY_6);
            prioritiesMap.put(ItemResolverConstants.STATEMENT_TYPE, ItemResolverConstants.PRIORITY_5);
            this.assignItemPriorities(prioritiesMap, completionItems);

            return completionItems;
        }
    }
}
