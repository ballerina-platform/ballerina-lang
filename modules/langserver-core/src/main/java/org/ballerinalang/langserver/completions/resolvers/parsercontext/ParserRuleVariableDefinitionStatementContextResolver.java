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
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.ballerinalang.langserver.completions.util.filters.PackageActionAndFunctionFilter;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
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
    public ArrayList<CompletionItem> resolveItems(TextDocumentServiceContext completionContext) {

        // Here we specifically need to check whether the statement is function invocation,
        // action invocation or worker invocation
        if (isActionOrFunctionInvocationStatement(completionContext)) {
            PackageActionAndFunctionFilter actionAndFunctionFilter = new PackageActionAndFunctionFilter();
            ArrayList<SymbolInfo> actionAndFunctions = new ArrayList<>();
            actionAndFunctions.addAll(actionAndFunctionFilter.filterItems(completionContext));
            ArrayList<CompletionItem> completionItems = new ArrayList<>();
            this.populateCompletionItemList(actionAndFunctions, completionItems);
            return completionItems;
        } else {
            // Add the create keyword
            CompletionItem createKeyword = new CompletionItem();
            createKeyword.setInsertText("create ");
            createKeyword.setLabel("create");
            createKeyword.setDetail(ItemResolverConstants.KEYWORD_TYPE);
            createKeyword.setSortText(Priority.PRIORITY7.name());

            ArrayList<CompletionItem> completionItems = new ArrayList<>();
            List<SymbolInfo> filteredList = completionContext.get(CompletionKeys.VISIBLE_SYMBOLS_KEY)
                    .stream()
                    .filter(symbolInfo -> !((symbolInfo.getScopeEntry().symbol instanceof BTypeSymbol)
                                    && !(symbolInfo.getScopeEntry().symbol instanceof BPackageSymbol)))
                    .collect(Collectors.toList());
            populateCompletionItemList(filteredList, completionItems);
            completionItems.add(createKeyword);
            return completionItems;
        }
    }
}
