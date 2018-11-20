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

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.filters.DelimiterBasedContentFilter;
import org.ballerinalang.langserver.completions.util.filters.StatementTemplateFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.ballerinalang.langserver.completions.util.sorters.ActionAndFieldAccessContextItemSorter;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser rule based statement context resolver.
 */
public class ParserRuleStatementContextResolver extends AbstractItemResolver {
    @Override
    @SuppressWarnings("unchecked")
    public List<CompletionItem> resolveItems(LSServiceOperationContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        Either<List<CompletionItem>, List<SymbolInfo>> itemList;

        Class itemSorterClass;
        if (isInvocationOrInteractionOrFieldAccess(context)) {
            itemSorterClass = ActionAndFieldAccessContextItemSorter.class;
            itemList = SymbolFilters.get(DelimiterBasedContentFilter.class)
                    .filterItems(context);
        } else {
            boolean isSnippet = context.get(CompletionKeys.CLIENT_CAPABILITIES_KEY).getCompletionItem()
                    .getSnippetSupport();
            List<SymbolInfo> filteredSymbols = context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
            itemSorterClass = context.get(CompletionKeys.BLOCK_OWNER_KEY).getClass();
            filteredSymbols.removeIf(CommonUtil.invalidSymbolsPredicate());
            
            filteredSymbols.removeIf(symbolInfo -> {
                BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
                return bSymbol instanceof BInvokableSymbol && ((bSymbol.flags & Flags.ATTACHED) == Flags.ATTACHED);
            });
            completionItems.addAll(this.getCompletionItemList(filteredSymbols, context));
            itemList = SymbolFilters.get(StatementTemplateFilter.class).filterItems(context);

            // Add the packages
            completionItems.addAll(this.getPackagesCompletionItems(context));

            CompletionItem xmlns = new CompletionItem();
            Snippet.STMT_NAMESPACE_DECLARATION.get().build(xmlns, isSnippet);
            completionItems.add(xmlns);

            CompletionItem varKeyword = new CompletionItem();
            Snippet.KW_VAR.get().build(varKeyword, isSnippet);
            completionItems.add(varKeyword);

            // Add the error snippet
            CompletionItem error = new CompletionItem();
            Snippet.DEF_ERROR.get().build(error, isSnippet);
            completionItems.add(error);
        }
        
        completionItems.addAll(this.getCompletionsFromEither(itemList, context));
        
        // Now we need to sort the completion items and populate the completion items specific to the scope owner
        // as an example, resource, action, function scopes are different from the if-else, while, and etc
        ItemSorters.get(itemSorterClass).sortItems(context, completionItems);

        return completionItems;
    }
}

