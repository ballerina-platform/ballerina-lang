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
import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.model.types.Type;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BConnectorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BEndpointType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BEnumType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interface for filtering the symbols.
 */
abstract class SymbolFilter {
    /**
     * Filters the symbolInfo from the list based on a particular filter criteria.
     * @param completionContext - Completion operation context
     * @return {@link ArrayList}
     */
    abstract List filterItems(TextDocumentServiceContext completionContext);

    /**
     * Get the actions, functions and types.
     * @param completionContext     Text Document Service context (Completion Context)
     * @param delimiterIndex        delimiter index (index of either . or :)
     * @return {@link ArrayList}    List of filtered symbol info
     */
    ArrayList<SymbolInfo> getActionsFunctionsAndTypes(TextDocumentServiceContext completionContext,
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
                    SymbolInfo actionFunctionSymbol = new SymbolInfo(name.toString(), value);
                    actionFunctionList.add(actionFunctionSymbol);
                }
            });
        }

        return actionFunctionList;
    }

    /**
     * Get the invocations and fields against an identifier (functions, struct fields and types including the enums).
     * @param context     Text Document Service context (Completion Context)
     * @param delimiterIndex        delimiter index (index of either . or :)
     * @return {@link ArrayList}    List of filtered symbol info
     */
    ArrayList<SymbolInfo> invocationsAndFieldsOnIdentifier(TextDocumentServiceContext context, int delimiterIndex) {
        ArrayList<SymbolInfo> actionFunctionList = new ArrayList<>();
        TokenStream tokenStream = context.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        List<SymbolInfo> symbols = context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
        SymbolTable symbolTable = context.get(DocumentServiceKeys.SYMBOL_TABLE_KEY);
        String variableName = tokenStream.get(delimiterIndex - 1).getText();
        SymbolInfo variable = this.getVariableByName(variableName, symbols);
        Map<Name, Scope.ScopeEntry> entries = new HashMap<>();
        String constraintTypeName = null;

        if (variable == null) {
            return actionFunctionList;
        }

        String packageID;
        BType bType = variable.getScopeEntry().symbol.getType();
        String bTypeValue;
        if (bType instanceof BEndpointType) {
            // If the BType is a BEndPointType we get the package id of the constraint
            Type constraint = ((BEndpointType) bType).getConstraint();
            assert constraint instanceof BConnectorType : constraint.getClass();
            packageID = ((BConnectorType) constraint).tsymbol.pkgID.toString();
            constraintTypeName = constraint.toString();
            bTypeValue = constraint.toString();
        } else {
            packageID = variable.getScopeEntry().symbol.getType().tsymbol.pkgID.toString();
            bTypeValue = bType.tsymbol.name.getValue();
        }
        String builtinPkgName = symbolTable.builtInPackageSymbol.name.getValue();

        // Extract the package symbol. This is used to extract the entries of the particular package
        SymbolInfo packageSymbolInfo = symbols.stream().filter(item -> {
            Scope.ScopeEntry scopeEntry = item.getScopeEntry();
            return (scopeEntry.symbol instanceof BPackageSymbol)
                    && scopeEntry.symbol.pkgID.toString().equals(packageID);
        }).findFirst().orElse(null);

        if (packageSymbolInfo == null && packageID.equals(builtinPkgName)) {
            // If the packageID is ballerina.builtin, we extract entries of builtin package
            entries = symbolTable.builtInPackageSymbol.scope.entries;
        } else if (packageSymbolInfo == null && (bType instanceof BEnumType || bType instanceof BStructType)) {
            // If the bType is an enum/ struct, we extract the fields of the enum
            entries = this.getScopeEntries(bTypeValue, context);
        } else if (packageSymbolInfo != null) {
            // If the package exist, we extract particular entries from package
            entries = packageSymbolInfo.getScopeEntry().symbol.scope.entries;
            if (constraintTypeName != null) {
                // If there is a constraint type for the variable, which means we are filtering the actions for the
                // particular endpoint. Hence we get the particular ClientConnector entry and it's action entries
                String filterName = constraintTypeName;
                Map.Entry filteredEntry = entries.entrySet()
                        .stream()
                        .filter(nameScopeEntry -> nameScopeEntry.getKey().getValue().equals(filterName))
                        .findFirst()
                        .orElse(null);
                entries = ((Scope.ScopeEntry) filteredEntry.getValue()).symbol.scope.entries;
            }
        }

        if (bType instanceof BEnumType) {
            entries.forEach((name, scopeEntry) -> {
                SymbolInfo actionFunctionSymbol = new SymbolInfo(name.toString(), scopeEntry);
                actionFunctionList.add(actionFunctionSymbol);
            });
        } else {
            entries.forEach((name, scopeEntry) -> {
                if (scopeEntry.symbol instanceof BInvokableSymbol
                        && ((BInvokableSymbol) scopeEntry.symbol).receiverSymbol != null) {
                    String symbolBoundedName = ((BInvokableSymbol) scopeEntry.symbol)
                            .receiverSymbol.getType().tsymbol.name.getValue();

                    if (symbolBoundedName.equals(bTypeValue)) {
                        // TODO: Need to handle the name in a proper manner
                        String[] nameComponents = name.toString().split("\\."); 
                        SymbolInfo actionFunctionSymbol =
                                new SymbolInfo(nameComponents[nameComponents.length - 1], scopeEntry);
                        actionFunctionList.add(actionFunctionSymbol);
                    }
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
    int getPackageDelimiterTokenIndex(TextDocumentServiceContext completionContext) {
        ArrayList<String> terminalTokens = new ArrayList<>(Arrays.asList(new String[]{";", "}", "{"}));
        int searchTokenIndex = completionContext.get(DocumentServiceKeys.TOKEN_INDEX_KEY);
        TokenStream tokenStream = completionContext.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
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
            if (".".equals(tokenString) || ":".equals(tokenString)) {
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

    /**
     * Get the variable symbol info by the name.
     * @param name      name of the variable
     * @param symbols   list of symbol info
     * @return {@link SymbolInfo}   Symbol Info extracted
     */
    private SymbolInfo getVariableByName(String name, List<SymbolInfo> symbols) {
        return symbols.stream()
                .filter(symbolInfo -> symbolInfo.getSymbolName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the scope entries.
     * @param bTypeName         Name of the bType 
     * @param completionCtx     Completion context
     * @return                  {@link Map} Scope entries map
     */
    private Map<Name, Scope.ScopeEntry> getScopeEntries(String bTypeName, TextDocumentServiceContext completionCtx) {
        SymbolInfo enumSymbolInfo = completionCtx.get(CompletionKeys.VISIBLE_SYMBOLS_KEY)
                .stream()
                .filter(symbolInfo -> symbolInfo.getSymbolName().equals(bTypeName))
                .findFirst()
                .orElse(null);

        return enumSymbolInfo.getScopeEntry().symbol.scope.entries;
    }
}
