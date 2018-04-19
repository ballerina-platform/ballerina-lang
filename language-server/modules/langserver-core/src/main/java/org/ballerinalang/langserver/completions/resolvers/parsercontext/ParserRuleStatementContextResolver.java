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

import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.filters.ConnectorInitExpressionItemFilter;
import org.ballerinalang.langserver.completions.util.filters.PackageActionFunctionAndTypesFilter;
import org.ballerinalang.langserver.completions.util.filters.StatementTemplateFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.ArrayList;

/**
 * Parser rule based statement context resolver.
 */
public class ParserRuleStatementContextResolver extends AbstractItemResolver {
    @Override
    @SuppressWarnings("unchecked")
    public ArrayList<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        ArrayList<SymbolInfo> filteredSymbols = new ArrayList<>();

        if (isInvocationOrFieldAccess(completionContext)) {
            filteredSymbols.addAll(SymbolFilters.getFilterByClass(PackageActionFunctionAndTypesFilter.class)
                    .filterItems(completionContext));
        } else {
            filteredSymbols.addAll(SymbolFilters.getFilterByClass(ConnectorInitExpressionItemFilter.class)
                    .filterItems(completionContext));
            filteredSymbols.addAll(this.removeInvalidStatementScopeSymbols(completionContext
                    .get(CompletionKeys.VISIBLE_SYMBOLS_KEY)));
            completionItems.addAll(SymbolFilters.getFilterByClass(StatementTemplateFilter.class)
                    .filterItems(completionContext));

            CompletionItem xmlns = new CompletionItem();
            xmlns.setLabel(ItemResolverConstants.XMLNS);
            xmlns.setInsertText(Snippet.NAMESPACE_DECLARATION.toString());
            xmlns.setInsertTextFormat(InsertTextFormat.Snippet);
            xmlns.setDetail(ItemResolverConstants.SNIPPET_TYPE);
            completionItems.add(xmlns);

            CompletionItem varKeyword = new CompletionItem();
            varKeyword.setInsertText(Snippet.VAR_KEYWORD_SNIPPET.toString());
            varKeyword.setLabel(ItemResolverConstants.VAR_KEYWORD);
            varKeyword.setDetail(ItemResolverConstants.KEYWORD_TYPE);
            completionItems.add(varKeyword);
        }

        this.populateCompletionItemList(filteredSymbols, completionItems);
        
        // Now we need to sort the completion items and populate the completion items specific to the scope owner
        // as an example, resource, action, function scopes are different from the if-else, while, and etc
        Class itemSorter = completionContext.get(CompletionKeys.BLOCK_OWNER_KEY).getClass();
        ItemSorters.getSorterByClass(itemSorter).sortItems(completionContext, completionItems);

        return completionItems;
    }
}

