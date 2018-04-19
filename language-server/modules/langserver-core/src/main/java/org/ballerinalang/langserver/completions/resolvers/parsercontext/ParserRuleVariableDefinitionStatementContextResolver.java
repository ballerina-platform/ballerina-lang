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
package org.ballerinalang.langserver.completions.resolvers.parsercontext;

import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.filters.ConnectorInitExpressionItemFilter;
import org.ballerinalang.langserver.completions.util.filters.PackageActionFunctionAndTypesFilter;
import org.ballerinalang.langserver.completions.util.sorters.CompletionItemSorter;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.ballerinalang.model.symbols.SymbolKind;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parser rule based variable definition statement context resolver.
 */
public class ParserRuleVariableDefinitionStatementContextResolver extends AbstractItemResolver {
    @Override
    @SuppressWarnings("unchecked")
    public ArrayList<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        PackageActionFunctionAndTypesFilter actionFunctionTypeFilter = new PackageActionFunctionAndTypesFilter();
        ConnectorInitExpressionItemFilter connectorInitItemFilter = new ConnectorInitExpressionItemFilter();

        // Here we specifically need to check whether the statement is function invocation,
        // action invocation or worker invocation
        if (isInvocationOrFieldAccess(completionContext)) {
            ArrayList<SymbolInfo> actionAndFunctions = new ArrayList<>();
            actionAndFunctions.addAll(actionFunctionTypeFilter.filterItems(completionContext));
            this.populateCompletionItemList(actionAndFunctions, completionItems);
        } else {
            // Fill completions if user is writing a connector init
            List<SymbolInfo> filteredConnectorInitSuggestions = connectorInitItemFilter.filterItems(completionContext);
            if (!filteredConnectorInitSuggestions.isEmpty()) {
                populateCompletionItemList(filteredConnectorInitSuggestions, completionItems);
            }

            // Add the create keyword
            CompletionItem createKeyword = new CompletionItem();
            createKeyword.setInsertText(Snippet.CHECK_KEYWORD_SNIPPET.toString());
            createKeyword.setLabel(ItemResolverConstants.CHECK_KEYWORD);
            createKeyword.setDetail(ItemResolverConstants.KEYWORD_TYPE);

            List<SymbolInfo> filteredList = completionContext.get(CompletionKeys.VISIBLE_SYMBOLS_KEY)
                    .stream()
                    .filter(symbolInfo -> {
                        BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
                        SymbolKind symbolKind = bSymbol.kind;
                        
                        // Here we return false if the BType is not either a package symbol or ENUM
                        return !((bSymbol instanceof BTypeSymbol) && !(bSymbol instanceof BPackageSymbol
                                || SymbolKind.ENUM.equals(symbolKind)));
                    })
                    .collect(Collectors.toList());

            // Remove the functions without a receiver symbol
            filteredList.removeIf(symbolInfo -> {
                BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
                return bSymbol instanceof BInvokableSymbol && ((BInvokableSymbol) bSymbol).receiverSymbol != null;
            });
            populateCompletionItemList(filteredList, completionItems);
            completionItems.add(createKeyword);
        }
        
        Class sorterKey = completionContext.get(DocumentServiceKeys.PARSER_RULE_CONTEXT_KEY).getClass();
        CompletionItemSorter itemSorter = ItemSorters.getSorterByClass(sorterKey);
        itemSorter.sortItems(completionContext, completionItems);
        
        return completionItems;
    }
}
