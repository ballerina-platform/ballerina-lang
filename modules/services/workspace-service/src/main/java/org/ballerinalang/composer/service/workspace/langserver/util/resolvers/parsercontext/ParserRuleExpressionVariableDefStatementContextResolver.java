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

package org.ballerinalang.composer.service.workspace.langserver.util.resolvers.parsercontext;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.composer.service.workspace.langserver.SymbolInfo;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.langserver.util.filters.PackageActionAndFunctionFilter;
import org.ballerinalang.composer.service.workspace.langserver.util.resolvers.AbstractItemResolver;
import org.ballerinalang.composer.service.workspace.langserver.util.resolvers.ItemResolverConstants;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BallerinaFunction;
import org.ballerinalang.model.SimpleVariableDef;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.natives.NativePackageProxy;
import org.ballerinalang.natives.NativeUnitProxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Expression Variable Definition Context resolver for the completion items. This is mainly supposed to be used when
 * there is a variable definition inside a transaction statement
 */
public class ParserRuleExpressionVariableDefStatementContextResolver extends AbstractItemResolver {
    @Override
    public ArrayList<CompletionItem> resolveItems(SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols,
                                                  HashMap<Class, AbstractItemResolver> resolvers) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        int packageAccessingTokenIndex = this.getTokenIndexOf(dataModel, ":");
        int equalSymbolTokenIndex = this.getTokenIndexOf(dataModel, "=");
        if (packageAccessingTokenIndex > 0) {
            completionItems.addAll(this.getFunctionCompletionItems(symbols,
                    (dataModel.getTokenStream().get(packageAccessingTokenIndex - 1)).getText()));
        } else if (equalSymbolTokenIndex > 0) {
            // Add the variable definitions, packages and the functions
            List<SymbolInfo> filteredSymbols =  symbols.stream()
                    .filter(symbolInfo -> symbolInfo.getSymbol() instanceof SimpleVariableDef
                                    || symbolInfo.getSymbol() instanceof NativePackageProxy
                                    || symbolInfo.getSymbol() instanceof BallerinaFunction)
                    .collect(Collectors.toList());
            populateCompletionItemList(filteredSymbols, completionItems);
        }
        return completionItems;
    }

    /**
     * Get the index of the given token
     * @param dataModel suggestions filter data model
     * @param tokenString token string to be found
     * @return {@link Integer}
     */
    private int getTokenIndexOf(SuggestionsFilterDataModel dataModel, String tokenString) {
        TokenStream tokenStream = dataModel.getTokenStream();
        int currentTokenIndex = dataModel.getTokenIndex();
        int searchIndex = currentTokenIndex - 1;
        List<String> terminatingCharacters = Arrays.asList(";", "{", "}", "@");

        while (true) {
            if (tokenStream != null && searchIndex < tokenStream.size()) {
                Token token = tokenStream.get(searchIndex);
                String tokenStr = token.getText();

                if (token.getChannel() == Token.DEFAULT_CHANNEL && terminatingCharacters.contains(tokenStr)) {
                    return -1;
                } else if (tokenStr.equals(tokenString)) {
                    return searchIndex;
                } else {
                    searchIndex--;
                }
            } else {
                return -1;
            }
        }
    }

    /**
     * Get the function completion items
     * @param symbols list of symbol info
     * @param packageTokenStr package token string
     * @return {@link String}
     */
    private List<CompletionItem> getFunctionCompletionItems(ArrayList<SymbolInfo> symbols, String packageTokenStr) {
        List<SymbolInfo> searchList = symbols.stream()
                .filter(symbolInfo -> !(symbolInfo.getSymbol() instanceof BType)).collect(Collectors.toList());
        List<SymbolInfo> filteredSymbolInfoList = searchList.stream()
                .filter(
                        symbolInfo -> symbolInfo.getSymbolName().contains(packageTokenStr)
                                && (symbolInfo.getSymbol() instanceof NativePackageProxy
                                || symbolInfo.getSymbol() instanceof NativeUnitProxy)
                ).collect(Collectors.toList());

        List<SymbolInfo> packageSymbolInfos = new ArrayList<>();
        filteredSymbolInfoList.stream()
                .filter(aFilteredSymbolInfoList -> aFilteredSymbolInfoList.getSymbol() instanceof NativePackageProxy)
                .forEach(aFilteredSymbolInfoList -> {
            BLangPackage bLangPackage =
                    ((NativePackageProxy) aFilteredSymbolInfoList.getSymbol()).load();
            bLangPackage.getSymbolMap().forEach((k, v) -> {
                SymbolInfo symbolInfo = new SymbolInfo(k.getName(), v);
                packageSymbolInfos.add(symbolInfo);
            });
        });

        PackageActionAndFunctionFilter actionAndFunctionFilter = new PackageActionAndFunctionFilter();
        List<CompletionItem> functionsAndActions =  actionAndFunctionFilter.getCompletionItems(packageSymbolInfos);
        return functionsAndActions.stream()
                .filter(completionItem -> completionItem.getDetail().equals(ItemResolverConstants.FUNCTION_TYPE))
                .collect(Collectors.toList());
    }
}
