/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.filters.PackageActionFunctionAndTypesFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.ballerinalang.langserver.completions.util.sorters.ConditionalStatementItemSorter;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Item resolver for the if clause.
 * @since 0.965.0
 */
public class ParserRuleConditionalClauseContextResolver extends AbstractItemResolver {
    
    @Override
    public List<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        Either<List<CompletionItem>, List<SymbolInfo>> itemList;
        if (isInvocationOrFieldAccess(completionContext)) {
            itemList = SymbolFilters.getFilterByClass(PackageActionFunctionAndTypesFilter.class)
                    .filterItems(completionContext);
        } else {
            List<SymbolInfo> symbolInfoList =
                    this.filterConditionalSymbols(completionContext.get(CompletionKeys.VISIBLE_SYMBOLS_KEY));
            itemList = Either.forRight(symbolInfoList);
            this.populateTrueFalseKeywords(completionItems);
        }
        
        if (itemList.isLeft()) {
            completionItems.addAll(itemList.getLeft());
        } else {
            this.populateCompletionItemList(itemList.getRight(), completionItems);
        }

        ItemSorters.getSorterByClass(ConditionalStatementItemSorter.class)
                .sortItems(completionContext, completionItems);

        return completionItems;
    }

    private List<SymbolInfo> filterConditionalSymbols(List<SymbolInfo> symbolInfoList) {
        return symbolInfoList.stream().filter(symbolInfo -> {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            return (bSymbol instanceof BTypeSymbol && bSymbol instanceof BPackageSymbol)
                    || (bSymbol instanceof BVarSymbol && !(bSymbol instanceof BInvokableSymbol))
                    || (bSymbol instanceof BInvokableSymbol && !(bSymbol instanceof BOperatorSymbol)
                    && (((BInvokableSymbol) bSymbol).receiverSymbol == null));
        }).collect(Collectors.toList());
    }
    
    private void populateTrueFalseKeywords(List<CompletionItem> completionItems) {
        CompletionItem trueItem = new CompletionItem();
        CompletionItem falseItem = new CompletionItem();

        trueItem.setLabel(ItemResolverConstants.TRUE_KEYWORD);
        trueItem.setInsertText(ItemResolverConstants.TRUE_KEYWORD);
        trueItem.setDetail(ItemResolverConstants.KEYWORD_TYPE);

        falseItem.setLabel(ItemResolverConstants.FALSE_KEYWORD);
        falseItem.setInsertText(ItemResolverConstants.FALSE_KEYWORD);
        falseItem.setDetail(ItemResolverConstants.KEYWORD_TYPE);

        completionItems.add(trueItem);
        completionItems.add(falseItem);
    }
}
