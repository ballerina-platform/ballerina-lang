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

package org.ballerinalang.composer.service.workspace.langserver.util.filters;

import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.composer.service.workspace.langserver.SymbolInfo;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.langserver.util.resolvers.ItemResolverConstants;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.NativeUnit;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.natives.NativePackageProxy;
import org.ballerinalang.natives.NativeUnitProxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter the actions and the functions in a package
 */
public class PackageActionAndFunctionFilter implements SymbolFilter {
    @Override
    public List<SymbolInfo> filterItems(SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols,
                                        HashMap<String, Object> properties) {

        int currentTokenIndex = dataModel.getTokenIndex();
        int searchLevel = 0;
        ArrayList<String> searchTokens = new ArrayList<>();
        TokenStream tokenStream = dataModel.getTokenStream();
        boolean continueSearch = true;
        int searchTokenIndex = currentTokenIndex + 1;

        while (continueSearch) {
            if (tokenStream != null && searchTokenIndex < tokenStream.size()) {
                String tokenStr = tokenStream.get(searchTokenIndex).getText();
                if (tokenStr.equals(":") || tokenStr.equals(".")) {
                    searchTokens.add(tokenStream.get(searchTokenIndex - 1).getText());
                    searchLevel++;
                } else if (tokenStr.equals("\n")) {
                    continueSearch = false;
                }
                searchTokenIndex++;
            } else {
                continueSearch = false;
            }
        }

        List<SymbolInfo> searchList = symbols.stream()
                .filter(symbolInfo -> !(symbolInfo.getSymbol() instanceof BType)).collect(Collectors.toList());

        for (int itr = 0; itr < searchLevel; itr++) {
            String searchStr = searchTokens.get(itr);
            List<SymbolInfo> filteredSymbolInfoList = searchList.stream()
                    .filter(
                            symbolInfo -> symbolInfo.getSymbolName().contains(searchStr)
                                    && (symbolInfo.getSymbol() instanceof NativePackageProxy
                                    || symbolInfo.getSymbol() instanceof NativeUnitProxy)
                    ).collect(Collectors.toList());

            searchList.clear();
            for (SymbolInfo aFilteredSymbolInfoList : filteredSymbolInfoList) {
                if (aFilteredSymbolInfoList.getSymbol() instanceof NativePackageProxy) {
                    BLangPackage bLangPackage =
                            ((NativePackageProxy) aFilteredSymbolInfoList.getSymbol()).load();
                    bLangPackage.getSymbolMap().forEach((k, v) -> {
                        SymbolInfo symbolInfo = new SymbolInfo(k.getName(), v);
                        searchList.add(symbolInfo);
                    });
                } else if (aFilteredSymbolInfoList.getSymbol() instanceof NativeUnitProxy) {
                    NativeUnit nativeUnit = ((NativeUnitProxy) aFilteredSymbolInfoList.getSymbol()).load();
                    SymbolInfo symbolInfo = new SymbolInfo(((BLangSymbol) nativeUnit).getName(),
                            ((BLangSymbol) nativeUnit));
                    searchList.add(symbolInfo);
                }
            }
        }

        return searchList;
    }

    /**
     * Get the list of completion Items from the symbolInfo list
     * @return {@link ArrayList}
     */
    public ArrayList<CompletionItem> getCompletionItems(List<SymbolInfo> symbolInfoList) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        symbolInfoList.forEach(symbolInfo -> {
            CompletionItem completionItem = new CompletionItem();
            if (symbolInfo.getSymbolName().contains(".ClientConnector.")) {
                String[] tokens = symbolInfo.getSymbolName().split("\\.");
                completionItem.setInsertText("ClientConnector." + tokens[tokens.length - 1] + "(${1})");
                completionItem.setLabel("ClientConnector." + tokens[tokens.length - 1]);
                completionItem.setDetail(ItemResolverConstants.ACTION_TYPE);
                completionItem.setSortText(ItemResolverConstants.PRIORITY_6);
            } else {
                completionItem.setInsertText(symbolInfo.getSymbolName() + "(${1})");
                completionItem.setLabel(symbolInfo.getSymbolName());
                completionItem.setDetail(ItemResolverConstants.FUNCTION_TYPE);
                completionItem.setSortText(ItemResolverConstants.PRIORITY_7);
            }
            completionItems.add(completionItem);
        });
        return completionItems;
    }
}
