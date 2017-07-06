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

package org.ballerinalang.composer.service.workspace.langserver.util.resolvers;

import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.composer.service.workspace.langserver.SymbolInfo;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BallerinaFunction;
import org.ballerinalang.model.NativeUnit;
import org.ballerinalang.model.StructDef;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.natives.NativePackageProxy;
import org.ballerinalang.natives.NativeUnitProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interface for completion item resolvers
 */
public abstract class AbstractItemResolver {
    abstract ArrayList<CompletionItem> resolveItems(SuggestionsFilterDataModel dataModel,
                                                    ArrayList<SymbolInfo> symbols);

    List<SymbolInfo> filterPackageActionsAndFunctions(SuggestionsFilterDataModel dataModel,
                                                      ArrayList<SymbolInfo> symbols) {

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

    void populateCompletionItemList(List<SymbolInfo> symbolInfoList, List<CompletionItem> completionItems) {

        symbolInfoList.forEach(symbolInfo -> {
            CompletionItem completionItem = new CompletionItem();
            completionItem.setLabel(symbolInfo.getSymbolName());
            String[] delimiterSeparatedTokens = (symbolInfo.getSymbolName()).split("\\.");
            completionItem.setInsertText(delimiterSeparatedTokens[delimiterSeparatedTokens.length - 1]);
            if (symbolInfo.getSymbol() instanceof NativeUnitProxy
                    && symbolInfo.getSymbolName().contains("ClientConnector")) {
                this.populateClientConnectorCompletionItem(completionItem, symbolInfo);
            } else if (symbolInfo.getSymbol() instanceof NativeUnitProxy) {
                this.populateNativeUnitProxyCompletionItem(completionItem, symbolInfo);
            } else if (symbolInfo.getSymbol() instanceof BallerinaFunction) {
                this.populateBallerinaFunctionCompletionItem(completionItem, symbolInfo);
            } else if (symbolInfo.getSymbol() instanceof NativePackageProxy) {
                this.populateNativePackageProxyCompletionItem(completionItem, symbolInfo);
            } else if (symbolInfo.getSymbol() instanceof VariableDef) {
                this.populateVariableDefCompletionItem(completionItem, symbolInfo);
            } else if ((symbolInfo.getSymbol() instanceof StructDef)) {
                this.populateStructDefCompletionItem(completionItem, symbolInfo);
            }
            completionItems.add(completionItem);
        });
    }

    void populateClientConnectorCompletionItem(CompletionItem completionItem, SymbolInfo symbolInfo) {
        completionItem.setDetail(ItemResolverConstants.ACTION_TYPE);
        completionItem.setSortText(ItemResolverConstants.PRIORITY_2);
    }

    void populateNativeUnitProxyCompletionItem(CompletionItem completionItem, SymbolInfo symbolInfo) {
        completionItem.setDetail(ItemResolverConstants.FUNCTION_TYPE);
        completionItem.setSortText(ItemResolverConstants.PRIORITY_5);
    }

    void populateBallerinaFunctionCompletionItem(CompletionItem completionItem, SymbolInfo symbolInfo) {
        completionItem.setDetail(ItemResolverConstants.FUNCTION_TYPE);
        completionItem.setSortText(ItemResolverConstants.PRIORITY_6);
    }

    void populateNativePackageProxyCompletionItem(CompletionItem completionItem, SymbolInfo symbolInfo) {
        completionItem.setDetail(ItemResolverConstants.PACKAGE_TYPE);
        completionItem.setSortText(ItemResolverConstants.PRIORITY_4);
    }

    void populateVariableDefCompletionItem(CompletionItem completionItem, SymbolInfo symbolInfo) {
        String typeName = ((VariableDef) symbolInfo.getSymbol()).getTypeName().getName();
        completionItem.setDetail((typeName.equals("")) ? ItemResolverConstants.NONE : typeName);
        completionItem.setSortText(ItemResolverConstants.PRIORITY_7);
    }

    void populateStructDefCompletionItem(CompletionItem completionItem, SymbolInfo symbolInfo) {
        completionItem.setLabel(((StructDef) symbolInfo.getSymbol()).getName());
        completionItem.setDetail(ItemResolverConstants.STRUCT_TYPE);
        completionItem.setSortText(ItemResolverConstants.PRIORITY_6);
    }
}
