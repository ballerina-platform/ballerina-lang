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

import com.google.common.collect.Lists;
import org.antlr.v4.runtime.Token;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.filters.DelimiterBasedContentFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.ballerinalang.langserver.completions.util.sorters.ActionAndFieldAccessContextItemSorter;
import org.ballerinalang.langserver.completions.util.sorters.CompletionItemSorter;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parser rule based variable definition statement context resolver.
 */
public class ParserRuleVariableDefinitionStatementContextResolver extends AbstractItemResolver {
    @Override
    @SuppressWarnings("unchecked")
    public List<CompletionItem> resolveItems(LSServiceOperationContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<String> poppedTokens = context.get(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY).stream()
                .map(Token::getText)
                .collect(Collectors.toList());
        
        String checkOrTrapKW = this.getCheckOrTrapKeyword(poppedTokens);

        Class sorterKey;
        if (isInvocationOrInteractionOrFieldAccess(context)) {
            sorterKey = ActionAndFieldAccessContextItemSorter.class;
            Either<List<CompletionItem>, List<SymbolInfo>> filteredList =
                    SymbolFilters.get(DelimiterBasedContentFilter.class).filterItems(context);
            completionItems.addAll(this.getCompletionItemList(filteredList));
        } else if (checkOrTrapKW.equalsIgnoreCase(ItemResolverConstants.TRAP)) {
            List<SymbolInfo> filteredList = context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
            // Remove the functions without a receiver symbol, bTypes not being packages and attached functions
            filteredList.removeIf(symbolInfo -> {
                BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
                return (bSymbol instanceof BInvokableSymbol
                        && ((BInvokableSymbol) bSymbol).receiverSymbol != null
                        && CommonUtil.isValidInvokableSymbol(bSymbol))
                        || ((bSymbol instanceof BTypeSymbol)
                        && !(bSymbol instanceof BPackageSymbol))
                        || (bSymbol instanceof BInvokableSymbol
                        && ((bSymbol.flags & Flags.ATTACHED) == Flags.ATTACHED));
            });
            completionItems.addAll(this.getCompletionItemList(filteredList));
            sorterKey = context.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY).getClass();
        } else {
            sorterKey = context.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY).getClass();
            completionItems.addAll(this.getVarDefCompletionItems(context));
        }

        CompletionItemSorter itemSorter = ItemSorters.get(sorterKey);
        itemSorter.sortItems(context, completionItems);
        
        return completionItems;
    }
    
    private String getCheckOrTrapKeyword(List<String> poppedTokens) {
        String retrievedToken = "";
        for (String token : Lists.reverse(poppedTokens)) {
            if (token.equals(UtilSymbolKeys.EQUAL_SYMBOL_KEY)) {
                break;
            } else if (token.equals(ItemResolverConstants.CHECK_KEYWORD)
                    || token.equals(ItemResolverConstants.TRAP)) {
                retrievedToken = token;
                break;
            }
        }
        
        return retrievedToken;
    }
}
