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

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.filters.DelimiterBasedContentFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.ballerinalang.langserver.completions.util.sorters.ConditionalStatementItemSorter;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Item resolver for the if clause.
 * @since 0.965.0
 */
public class ParserRuleConditionalClauseContextResolver extends AbstractItemResolver {
    
    @Override
    public List<CompletionItem> resolveItems(LSServiceOperationContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        Either<List<CompletionItem>, List<SymbolInfo>> itemList;
        if (isInvocationOrInteractionOrFieldAccess(context)) {
            itemList = SymbolFilters.get(DelimiterBasedContentFilter.class).filterItems(context);
        } else {
            List<SymbolInfo> symbolInfoList = context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
            symbolInfoList.removeIf(CommonUtil.invalidSymbolsPredicate());
            itemList = Either.forRight(this.filterConditionalSymbols(symbolInfoList));
            this.populateTrueFalseKeywords(completionItems);
        }

        completionItems.addAll(this.getCompletionsFromEither(itemList, context));
        ItemSorters.get(ConditionalStatementItemSorter.class).sortItems(context, completionItems);

        return completionItems;
    }

    private List<SymbolInfo> filterConditionalSymbols(List<SymbolInfo> symbolInfoList) {
        return symbolInfoList.stream().filter(symbolInfo -> {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            return (bSymbol instanceof BTypeSymbol && bSymbol instanceof BPackageSymbol)
                    || (bSymbol instanceof BVarSymbol && !(bSymbol instanceof BInvokableSymbol))
                    || (CommonUtil.isValidInvokableSymbol(bSymbol)
                    && ((bSymbol.flags & Flags.ATTACHED) != Flags.ATTACHED));
        }).collect(Collectors.toList());
    }

    private void populateTrueFalseKeywords(List<CompletionItem> completionItems) {
        CompletionItem trueItem = new CompletionItem();
        trueItem.setLabel(ItemResolverConstants.TRUE_KEYWORD);
        trueItem.setInsertText(ItemResolverConstants.TRUE_KEYWORD);
        trueItem.setDetail(ItemResolverConstants.KEYWORD_TYPE);
        trueItem.setKind(CompletionItemKind.Value);
        completionItems.add(trueItem);

        CompletionItem falseItem = new CompletionItem();
        falseItem.setLabel(ItemResolverConstants.FALSE_KEYWORD);
        falseItem.setInsertText(ItemResolverConstants.FALSE_KEYWORD);
        falseItem.setDetail(ItemResolverConstants.KEYWORD_TYPE);
        trueItem.setKind(CompletionItemKind.Value);
        completionItems.add(falseItem);
    }
}
