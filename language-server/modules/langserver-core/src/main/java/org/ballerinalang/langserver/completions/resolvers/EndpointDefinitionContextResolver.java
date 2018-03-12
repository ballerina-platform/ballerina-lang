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
package org.ballerinalang.langserver.completions.resolvers;

import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.sorters.EndpointDefContextItemSorter;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangEndpointTypeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Item resolver for the endpoint definition context.
 */
public class EndpointDefinitionContextResolver extends AbstractItemResolver {
    @Override
    public ArrayList<CompletionItem> resolveItems(TextDocumentServiceContext completionContext) {
        BLangVariable bLangVariable = (BLangVariable) completionContext.get(CompletionKeys.SYMBOL_ENV_NODE_KEY);
        if (!(bLangVariable.typeNode instanceof BLangEndpointTypeNode)) {
            return null;
        }

        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        completionItems.add(getCreateKeyWordCompletionItem());
        List<SymbolInfo> filteredSymbols =
                filterEndpointContextItems(completionContext.get(CompletionKeys.VISIBLE_SYMBOLS_KEY));
        this.populateCompletionItemList(filteredSymbols, completionItems);

        ItemSorters.getSorterByClass(EndpointDefContextItemSorter.class).sortItems(completionContext, completionItems);
                
        return completionItems;
    }
    
    private CompletionItem getCreateKeyWordCompletionItem() {
        CompletionItem createItem = new CompletionItem();
        createItem.setInsertText(Snippet.CREATE_KEYWORD_SNIPPET.toString());
        createItem.setLabel(ItemResolverConstants.CREATE_KEYWORD);
        createItem.setDetail(ItemResolverConstants.KEYWORD_TYPE);
        return createItem;
    }
    
    private List<SymbolInfo> filterEndpointContextItems(List<SymbolInfo> symbols) {
        return symbols.stream().filter(symbolInfo -> {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            return (bSymbol instanceof BInvokableSymbol && ((BInvokableSymbol) bSymbol).receiverSymbol == null)
                    || (!(bSymbol instanceof BInvokableSymbol)
                    && bSymbol instanceof BVarSymbol);
        }).collect(Collectors.toList());
    }
}
