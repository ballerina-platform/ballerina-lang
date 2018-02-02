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

import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.consts.ItemResolverConstants;
import org.ballerinalang.langserver.completions.consts.Priority;
import org.ballerinalang.langserver.completions.consts.Snippet;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.ballerinalang.langserver.completions.util.filters.BTypeFilter;
import org.ballerinalang.model.SimpleVariableDef;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parser rule based Item resolver for the transform statement.
 */
public class ParserRuleTransformStatementBodyContextResolver extends AbstractItemResolver {
    @Override
    public ArrayList<CompletionItem> resolveItems(TextDocumentServiceContext completionContext) {

        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        BTypeFilter bTypeFilter = new BTypeFilter();
        populateCompletionItemList(bTypeFilter.filterItems(completionContext), completionItems);

        List<SymbolInfo> variableDefs =  completionContext.get(CompletionKeys.VISIBLE_SYMBOLS_KEY).stream()
                .filter(symbolInfo -> symbolInfo.getSymbol() instanceof SimpleVariableDef)
                .collect(Collectors.toList());
        populateCompletionItemList(variableDefs, completionItems);

        // Populate Transaction Statement template
        CompletionItem transactionItem = new CompletionItem();
        transactionItem.setLabel(ItemResolverConstants.TRANSACTION);
        transactionItem.setInsertText(Snippet.TRANSACTION.toString());
        transactionItem.setInsertTextFormat(InsertTextFormat.Snippet);
        transactionItem.setDetail(ItemResolverConstants.STATEMENT_TYPE);
        transactionItem.setSortText(Priority.PRIORITY6.name());
        completionItems.add(transactionItem);

        return completionItems;
    }
}
