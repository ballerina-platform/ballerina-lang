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
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
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
public class ParserRuleEndpointTypeContext extends AbstractItemResolver {
    @Override
    public ArrayList<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        TokenStream tokenStream = completionContext.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        int currentIndex = completionContext.get(DocumentServiceKeys.TOKEN_INDEX_KEY);
        List<SymbolInfo> visibleSymbols = completionContext.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
        Token secondNextToken = CommonUtil.getNthDefaultTokensToRight(tokenStream, currentIndex, 2);
        Token pkgDelimiter = secondNextToken.getText().equals(UtilSymbolKeys.PKG_DELIMITER_KEYWORD) ?
                secondNextToken : tokenStream.get(currentIndex);

        if (pkgDelimiter != null && pkgDelimiter.getText().equals(UtilSymbolKeys.PKG_DELIMITER_KEYWORD)) {
            String pkgAlias = CommonUtil.getPreviousDefaultToken(tokenStream, pkgDelimiter.getTokenIndex()).getText();
            SymbolInfo pkgSymbolInfo = visibleSymbols.stream().filter(symbolInfo -> {
                BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
                PackageID packageID = bSymbol.pkgID;
                String nameAlias = packageID.getNameComps().get(packageID.getNameComps().size() - 1).getValue();
                return bSymbol instanceof BPackageSymbol && pkgAlias.equals(nameAlias);
            }).findAny().orElseGet(null);
            
            if (pkgSymbolInfo != null) {
                this.populateCompletionItemList(getEndpointEntries(pkgSymbolInfo.getScopeEntry().symbol.scope.entries),
                        completionItems);
            }
        } else {
            this.populateCompletionItemList(this.getEndpointEntries(visibleSymbols), completionItems);
        }
        
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
