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
package org.ballerinalang.langserver.completions.resolvers;

import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.util.filters.StatementTemplateFilter;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;

import java.util.ArrayList;

/**
 * Statement context resolver for resolving the items of the statement context.
 */
public class StatementContextResolver extends AbstractItemResolver {
    @Override
    @SuppressWarnings("unchecked")
    public ArrayList<CompletionItem> resolveItems(TextDocumentServiceContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        StatementTemplateFilter statementTemplateFilter = new StatementTemplateFilter();
        // Add the statement templates
        completionItems.addAll(statementTemplateFilter.filterItems(completionContext));
        // We need to remove the functions having a receiver symbol
        completionContext.get(CompletionKeys.VISIBLE_SYMBOLS_KEY).removeIf(symbolInfo -> {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            return bSymbol instanceof BInvokableSymbol && ((BInvokableSymbol) bSymbol).receiverSymbol != null;
        });
        populateCompletionItemList(completionContext.get(CompletionKeys.VISIBLE_SYMBOLS_KEY), completionItems);

        return completionItems;
    }
}
