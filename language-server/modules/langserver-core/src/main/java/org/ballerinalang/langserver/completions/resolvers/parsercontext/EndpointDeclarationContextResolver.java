/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.completions.resolvers.parsercontext;

import org.antlr.v4.runtime.Token;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Completion Item Resolver for the endpoint type context.
 */
public class EndpointDeclarationContextResolver extends AbstractItemResolver {
    @Override
    public List<CompletionItem> resolveItems(LSServiceOperationContext context) {
        List<String> poppedTokens = CommonUtil.popNFromStack(context.get(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY), 3)
                .stream()
                .map(Token::getText)
                .collect(Collectors.toList());
        List<SymbolInfo> visibleSymbols = context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
        if (poppedTokens.contains(UtilSymbolKeys.PKG_DELIMITER_KEYWORD)) {
            String pkgAlias = poppedTokens.get(poppedTokens.indexOf(UtilSymbolKeys.PKG_DELIMITER_KEYWORD) - 1);

            for (SymbolInfo info : visibleSymbols) {
                BSymbol bSymbol = info.getScopeEntry().symbol;
                PackageID packageID = bSymbol.pkgID;
                String nameAlias = CommonUtil.getLastItem(packageID.getNameComps()).getValue();
                if (bSymbol instanceof BPackageSymbol && pkgAlias.equals(nameAlias)) {
                    return this.getCompletionItemList(getEndpointEntries(info.getScopeEntry().symbol.scope.entries));
                }
            }

            return new ArrayList<>();
        }

        List<CompletionItem> completionItems = new ArrayList<>();
        completionItems.addAll(this.getCompletionItemList(this.getEndpointEntries(visibleSymbols)));
        completionItems.addAll(this.getPackagesCompletionItems(context));
        return completionItems;
    }

    private List<SymbolInfo> getEndpointEntries(Map<Name, Scope.ScopeEntry> scopeEntries) {
        List<SymbolInfo> symbolInfoList = new ArrayList<>();
        scopeEntries.entrySet().forEach(entry -> {
            BSymbol bSymbol = entry.getValue().symbol;
            if (CommonUtil.isEndpointObject(bSymbol)) {
                symbolInfoList.add(new SymbolInfo(entry.getKey().toString(), entry.getValue()));
            }
        });
        
        return symbolInfoList;
    }

    private List<SymbolInfo> getEndpointEntries(List<SymbolInfo> symbolInfoList) {
        return symbolInfoList.stream().filter(symbolInfo -> {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            return CommonUtil.isEndpointObject(bSymbol) || bSymbol instanceof BPackageSymbol;
        }).collect(Collectors.toList());
    }
}
