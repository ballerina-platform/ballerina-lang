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

package org.ballerinalang.composer.service.workspace.langserver.util.completion.filters;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.composer.service.workspace.langserver.SymbolInfo;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Filter the actions and the functions in a package
 */
public class PackageActionAndFunctionFilter implements SymbolFilter {
    @Override
    public List<SymbolInfo> filterItems(SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols,
                                        HashMap<String, Object> properties) {

        TokenStream tokenStream = dataModel.getTokenStream();
        int delimiterIndex = this.getPackageDelimeterTokenIndex(dataModel);
        String delimiter = tokenStream.get(delimiterIndex).getText();
        ArrayList<SymbolInfo> returnSymbolsInfoList = new ArrayList<>();

        if (delimiter.equals(".")) {
            // If the delimiter is "." then we are filtering the bound functions for the structs
            returnSymbolsInfoList.addAll(this.getBoundActionAndFunctions(dataModel, symbols, delimiterIndex));
        } else if (delimiter.equals(":")) {
            // We are filtering the package functions
            returnSymbolsInfoList.addAll(this.getActionsAndFunctions(dataModel, symbols, delimiterIndex));
        }

        return returnSymbolsInfoList;
    }

    private ArrayList<SymbolInfo> getActionsAndFunctions(SuggestionsFilterDataModel dataModel,
                                                        ArrayList<SymbolInfo> symbols, int delimiterIndex) {

        ArrayList<SymbolInfo> actionFunctionList = new ArrayList<>();
        TokenStream tokenStream = dataModel.getTokenStream();
        String packageName = tokenStream.get(delimiterIndex - 1).getText();

        SymbolInfo packageSymbolInfo = symbols.stream().filter(item -> {
            Scope.ScopeEntry scopeEntry = item.getScopeEntry();
            return item.getSymbolName().equals(packageName) && scopeEntry.symbol instanceof BPackageSymbol;
        }).findFirst().orElse(null);

        if (packageSymbolInfo != null) {
            Scope.ScopeEntry packageEntry = packageSymbolInfo.getScopeEntry();
            SymbolInfo symbolInfo = new SymbolInfo(packageSymbolInfo.getSymbolName(), packageEntry);

            symbolInfo.getScopeEntry().symbol.scope.entries.forEach((name, value) -> {
                if (value.symbol instanceof BInvokableSymbol
                        && ((BInvokableSymbol) value.symbol).receiverSymbol == null) {
                    SymbolInfo actionFunctionSymbol = new SymbolInfo(name.toString(), value);
                    actionFunctionList.add(actionFunctionSymbol);
                }
            });
        }

        return actionFunctionList;
    }

    private ArrayList<SymbolInfo> getBoundActionAndFunctions(SuggestionsFilterDataModel dataModel,
                                                             ArrayList<SymbolInfo> symbols, int delimiterIndex) {

        ArrayList<SymbolInfo> actionFunctionList = new ArrayList<>();
        TokenStream tokenStream = dataModel.getTokenStream();
        String variableName = tokenStream.get(delimiterIndex - 1).getText();
        SymbolInfo variable = this.getVariableByName(variableName, symbols);

        if (variable == null) {
            return actionFunctionList;
        }

        String packageID = variable.getScopeEntry().symbol.getType().tsymbol.pkgID.toString();

        SymbolInfo packageSymbolInfo = symbols.stream().filter(item -> {
            Scope.ScopeEntry scopeEntry = item.getScopeEntry();
            return (scopeEntry.symbol instanceof BPackageSymbol)
                    && scopeEntry.symbol.pkgID.toString().equals(packageID);
        }).findFirst().orElse(null);

        if (packageSymbolInfo != null) {
            packageSymbolInfo.getScopeEntry().symbol.scope.entries.forEach((name, scopeEntry) -> {
                if (scopeEntry.symbol instanceof BInvokableSymbol
                        && ((BInvokableSymbol) scopeEntry.symbol).receiverSymbol != null) {
                    String symbolBoundedName = ((BInvokableSymbol) scopeEntry.symbol)
                            .receiverSymbol.getType().tsymbol.name.getValue();
                    String checkValue = variable.getScopeEntry().symbol.getType().tsymbol.name.getValue();
                    if (symbolBoundedName.equals(checkValue)) {
                        SymbolInfo actionFunctionSymbol = new SymbolInfo(name.toString(), scopeEntry);
                        actionFunctionList.add(actionFunctionSymbol);
                    }
                }
            });
        }

        return actionFunctionList;
    }

    /**
     * Get the index of a certain token
     * @param tokenString - token string
     * @param from - start searching from
     * @param dataModel - suggestions filter data model
     * @return {@link Integer}
     */
    public int getIndexOfTokenString(String tokenString, int from, SuggestionsFilterDataModel dataModel) {
        TokenStream tokenStream = dataModel.getTokenStream();
        int resultTokenIndex = -1;
        int searchIndex = from;

        while (true) {
            if (searchIndex < 0 || tokenStream.size() - 1 < searchIndex) {
                break;
            }
            Token token = tokenStream.get(searchIndex);
            if (token.getChannel() != Token.DEFAULT_CHANNEL || !token.getText().equals(tokenString)) {
                searchIndex++;
            } else {
                resultTokenIndex = searchIndex;
                break;
            }
        }

        return resultTokenIndex;
    }

    private int getPackageDelimeterTokenIndex(SuggestionsFilterDataModel dataModel) {
        ArrayList<String> terminalTokens = new ArrayList<>(Arrays.asList(new String[]{";", "}", "{"}));
        int currentTokenIndex = dataModel.getTokenIndex();
        int searchTokenIndex = currentTokenIndex;
        TokenStream tokenStream = dataModel.getTokenStream();
        int delimiterIndex = -1;
        String currentTokenStr = tokenStream.get(searchTokenIndex).getText();

        if (terminalTokens.contains(currentTokenStr)) {
            searchTokenIndex -= 1;
            while (true) {
                if (tokenStream.get(searchTokenIndex).getChannel() == Token.DEFAULT_CHANNEL) {
                    break;
                } else {
                    searchTokenIndex -= 1;
                }
            }
        }

        while (true) {
            if (searchTokenIndex >= tokenStream.size()) {
                break;
            }
            String tokenString = tokenStream.get(searchTokenIndex).getText();
            if (tokenString.equals(".") || tokenString.equals(":")) {
                delimiterIndex = searchTokenIndex;
                break;
            } else if (terminalTokens.contains(tokenString)) {
                break;
            } else {
                searchTokenIndex++;
            }
        }

        return delimiterIndex;
    }

    private SymbolInfo getVariableByName(String name, ArrayList<SymbolInfo> symbols) {
        return symbols.stream()
                .filter(symbolInfo -> symbolInfo.getSymbolName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
