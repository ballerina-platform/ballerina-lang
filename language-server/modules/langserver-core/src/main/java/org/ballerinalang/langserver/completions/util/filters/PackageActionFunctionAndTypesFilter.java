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
package org.ballerinalang.langserver.completions.util.filters;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter the actions and the functions in a package.
 */
public class PackageActionFunctionAndTypesFilter extends AbstractSymbolFilter {

    @Override
    public List<SymbolInfo> filterItems(LSServiceOperationContext completionContext) {

        TokenStream tokenStream = completionContext.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        int delimiterIndex = this.getPackageDelimiterTokenIndex(completionContext);
        String delimiter = tokenStream.get(delimiterIndex).getText();
        ArrayList<SymbolInfo> returnSymbolsInfoList = new ArrayList<>();

        if (UtilSymbolKeys.DOT_SYMBOL_KEY.equals(delimiter)
                || UtilSymbolKeys.ACTION_INVOCATION_SYMBOL_KEY.equals(delimiter)) {
            // If the delimiter is "." then we are filtering the bound functions for the structs
            returnSymbolsInfoList
                    .addAll(CommonUtil.invocationsAndFieldsOnIdentifier(completionContext, delimiterIndex));
        } else if (UtilSymbolKeys.PKG_DELIMITER_KEYWORD.equals(delimiter)) {
            // We are filtering the package functions, actions and the types
            ArrayList<SymbolInfo> filteredList = this.getActionsFunctionsAndTypes(completionContext, delimiterIndex);

            // If this is a connector init
            if (isConnectorInit(delimiterIndex, completionContext)) {
                List<SymbolInfo> connectorKindList = filteredList
                        .stream()
                        .filter(item ->
                                item.getScopeEntry().symbol.kind.toString().equals(SymbolKind.CONNECTOR.toString())
                        ).collect(Collectors.toList());
                returnSymbolsInfoList.addAll(connectorKindList);
            } else {
                returnSymbolsInfoList.addAll(filteredList);
            }
        }

        return returnSymbolsInfoList;
    }

    /**
     * Check whether the statement being writing is a connector init by analyzing the tokens.
     * @param startIndex    Search start index
     * @param context       Document service context
     * @return {@link Boolean} connector init or not
     */
    private boolean isConnectorInit(int startIndex, LSServiceOperationContext context) {
        int nonHiddenTokenCount = 0;
        int counter = startIndex - 1;
        TokenStream tokenStream = context.get(DocumentServiceKeys.TOKEN_STREAM_KEY);

        while (true) {
            Token token = tokenStream.get(counter);
            if (nonHiddenTokenCount == 2
                    && tokenStream.get(counter + 1).getText().equals(UtilSymbolKeys.CREATE_KEYWORD_KEY)) {
                return true;
            } else if (nonHiddenTokenCount == 2) {
                break;
            }

            if (token.getChannel() == Token.DEFAULT_CHANNEL) {
                nonHiddenTokenCount++;
            }
            counter--;
        }

        return false;
    }

    /**
     * Get the actions, functions and types.
     * @param completionContext     Text Document Service context (Completion Context)
     * @param delimiterIndex        delimiter index (index of either . or :)
     * @return {@link ArrayList}    List of filtered symbol info
     */
    private ArrayList<SymbolInfo> getActionsFunctionsAndTypes(LSServiceOperationContext completionContext,
                                                              int delimiterIndex) {
        ArrayList<SymbolInfo> actionFunctionList = new ArrayList<>();
        TokenStream tokenStream = completionContext.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        List<SymbolInfo> symbols = completionContext.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
        String packageName = tokenStream.get(delimiterIndex - 1).getText();

        // Extract the package symbol
        SymbolInfo packageSymbolInfo = symbols.stream().filter(item -> {
            Scope.ScopeEntry scopeEntry = item.getScopeEntry();
            return item.getSymbolName().equals(packageName) && scopeEntry.symbol instanceof BPackageSymbol;
        }).findFirst().orElse(null);

        if (packageSymbolInfo != null) {
            Scope.ScopeEntry packageEntry = packageSymbolInfo.getScopeEntry();
            SymbolInfo symbolInfo = new SymbolInfo(packageSymbolInfo.getSymbolName(), packageEntry);

            symbolInfo.getScopeEntry().symbol.scope.entries.forEach((name, value) -> {
                if ((value.symbol instanceof BInvokableSymbol
                        && ((BInvokableSymbol) value.symbol).receiverSymbol == null)
                        || (value.symbol instanceof BTypeSymbol && !(value.symbol instanceof BPackageSymbol))) {
                    actionFunctionList.add(new SymbolInfo(name.toString(), value));
                }
            });
        }

        return actionFunctionList;
    }

    /**
     * Get the package delimiter token index, which is the index of . or :.
     * @param completionContext     Text Document Service context (Completion Context)
     * @return {@link Integer}      Index of the delimiter
     */
    private int getPackageDelimiterTokenIndex(LSServiceOperationContext completionContext) {
        ArrayList<String> terminalTokens = new ArrayList<>(Arrays.asList(new String[]{";", "}", "{", "(", ")"}));
        int delimiterIndex = -1;
        int searchTokenIndex = completionContext.get(DocumentServiceKeys.TOKEN_INDEX_KEY);
        TokenStream tokenStream = completionContext.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        /*
        In order to avoid the token index inconsistencies, current token index offsets from two default tokens
         */
        Token offsetToken = CommonUtil.getNthDefaultTokensToLeft(tokenStream, searchTokenIndex, 2);
        if (!terminalTokens.contains(offsetToken.getText())) {
            searchTokenIndex = offsetToken.getTokenIndex();
        }

        while (true) {
            if (searchTokenIndex >= tokenStream.size()) {
                break;
            }
            String tokenString = tokenStream.get(searchTokenIndex).getText();
            if (UtilSymbolKeys.DOT_SYMBOL_KEY.equals(tokenString)
                    || UtilSymbolKeys.PKG_DELIMITER_KEYWORD.equals(tokenString)
                    || UtilSymbolKeys.ACTION_INVOCATION_SYMBOL_KEY.equals(tokenString)) {
                delimiterIndex = searchTokenIndex;
                break;
            } else if (terminalTokens.contains(tokenString)
                    && completionContext.get(DocumentServiceKeys.TOKEN_INDEX_KEY) <= searchTokenIndex) {
                break;
            } else {
                searchTokenIndex++;
            }
        }

        return delimiterIndex;
    }
}
