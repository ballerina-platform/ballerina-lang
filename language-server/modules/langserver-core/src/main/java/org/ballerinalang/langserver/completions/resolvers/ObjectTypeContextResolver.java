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

import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Completion item resolver for the object type.
 */
public class ObjectTypeContextResolver extends AbstractItemResolver {
    @Override
    public ArrayList<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<SymbolInfo> filteredTypes = completionContext.get(CompletionKeys.VISIBLE_SYMBOLS_KEY).stream()
                .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol instanceof BTypeSymbol)
                .collect(Collectors.toList());
        this.populateCompletionItemList(filteredTypes, completionItems);
        populateSnippetSignatures(completionItems);
        
        return completionItems;
    }
    
    private void populateSnippetSignatures(List<CompletionItem> completionItems) {
        CompletionItem functionItem = new CompletionItem();
        functionItem.setLabel(ItemResolverConstants.FUNCTION);
        functionItem.setInsertText(Snippet.FUNCTION_SIGNATURE.toString());
        functionItem.setInsertTextFormat(InsertTextFormat.Snippet);
        functionItem.setDetail(ItemResolverConstants.SNIPPET_TYPE);
        completionItems.add(functionItem);

        CompletionItem constructorItem = new CompletionItem();
        constructorItem.setLabel(ItemResolverConstants.NEW_OBJECT_CONSTRUCTOR_TYPE);
        constructorItem.setInsertText(Snippet.NEW_OBJECT_CONSTRUCTOR.toString());
        constructorItem.setInsertTextFormat(InsertTextFormat.Snippet);
        constructorItem.setDetail(ItemResolverConstants.SNIPPET_TYPE);
        completionItems.add(constructorItem);

        CompletionItem publicBlockItem = new CompletionItem();
        publicBlockItem.setLabel(ItemResolverConstants.PUBLIC);
        publicBlockItem.setInsertText(Snippet.PUBLIC_BLOCK.toString());
        publicBlockItem.setInsertTextFormat(InsertTextFormat.Snippet);
        publicBlockItem.setDetail(ItemResolverConstants.SNIPPET_TYPE);
        completionItems.add(publicBlockItem);

        CompletionItem privateBlockItem = new CompletionItem();
        privateBlockItem.setLabel(ItemResolverConstants.PRIVATE);
        privateBlockItem.setInsertText(Snippet.PRIVATE_BLOCK.toString());
        privateBlockItem.setInsertTextFormat(InsertTextFormat.Snippet);
        privateBlockItem.setDetail(ItemResolverConstants.SNIPPET_TYPE);
        completionItems.add(privateBlockItem);
    }
}
