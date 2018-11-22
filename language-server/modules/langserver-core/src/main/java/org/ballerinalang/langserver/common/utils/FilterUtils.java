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
package org.ballerinalang.langserver.common.utils;

import org.antlr.v4.runtime.ParserRuleContext;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEndpointVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities for filtering the symbols from completion context and symbol information lists.
 */
public class FilterUtils {

    /**
     * Get the invocations and fields against an identifier (functions, actions and fields).
     *
     * @param context      Text Document Service context (Completion Context)
     * @param variableName Variable name to evaluate against (Can be package alias or defined variable)
     * @param delimiter    delimiter String either dot or action invocation symbol
     * @param symbolInfos  List of visible symbol info
     * @param addBuiltIn   Add built-in functions
     * @return {@link ArrayList}    List of filtered symbol info
     */
    public static List<SymbolInfo> getInvocationAndFieldSymbolsOnVar(LSServiceOperationContext context,
                                                                     String variableName, String delimiter,
                                                                     List<SymbolInfo> symbolInfos, boolean addBuiltIn) {
        ArrayList<SymbolInfo> resultList = new ArrayList<>();
        SymbolTable symbolTable = context.get(DocumentServiceKeys.SYMBOL_TABLE_KEY);
        ParserRuleContext parserRuleContext = context.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY);
        SymbolInfo variable = getVariableByName(variableName, symbolInfos);

        if (variable == null) {
            return resultList;
        }

        BType bType = variable.getScopeEntry().symbol.getType();
        BSymbol bVarSymbol = variable.getScopeEntry().symbol;

        if (variable.getScopeEntry().symbol instanceof BEndpointVarSymbol
                && delimiter.equals(UtilSymbolKeys.RIGHT_ARROW_SYMBOL_KEY)
                && ((BEndpointVarSymbol) bVarSymbol).getClientFunction.type instanceof BInvokableType) {
            // Handling action invocations ep -> ...
            resultList.addAll(getEndpointActions((BEndpointVarSymbol) variable.getScopeEntry().symbol));
        } else if (delimiter.equals(UtilSymbolKeys.RIGHT_ARROW_SYMBOL_KEY)
                || delimiter.equals(UtilSymbolKeys.LEFT_ARROW_SYMBOL_KEY)
                || parserRuleContext instanceof BallerinaParser.WorkerInteractionStatementContext) {
            // Handling worker-interactions eg. msg -> ... || msg <- ...
            List<SymbolInfo> filteredList = context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
            filteredList.removeIf(symbolInfo -> symbolInfo.getScopeEntry().symbol instanceof BTypeSymbol);
            resultList.addAll(filteredList);
        } else if (delimiter.equals(UtilSymbolKeys.DOT_SYMBOL_KEY)
                || delimiter.equals(UtilSymbolKeys.BANG_SYMBOL_KEY)) {
            String builtinPkgName = symbolTable.builtInPackageSymbol.pkgID.name.getValue();
            String currentPkgName = context.get(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY);
            Map<Name, Scope.ScopeEntry> entries = new HashMap<>();
            PackageID pkgId = getPackageIDForBType(bType);
            String packageIDString = pkgId == null ? "" : pkgId.getName().getValue();
            BType modifiedBType = bType instanceof BUnionType ? getBTypeForUnionType((BUnionType) bType) : bType;

            // Extract the package symbol. This is used to extract the entries of the particular package
            SymbolInfo packageSymbolInfo = symbolInfos.stream().filter(item -> {
                Scope.ScopeEntry scopeEntry = item.getScopeEntry();
                return (scopeEntry.symbol instanceof BPackageSymbol)
                        && scopeEntry.symbol.pkgID.name.getValue().equals(packageIDString);
            }).findFirst().orElse(null);

            if (packageIDString.equals(builtinPkgName)) {
                // If the packageID is ballerina/builtin, we extract entries of builtin package
                entries = symbolTable.builtInPackageSymbol.scope.entries;
            } else if (packageSymbolInfo == null && packageIDString.equals(currentPkgName)) {
                entries = getScopeEntries(bType, symbolInfos);
            } else if (packageSymbolInfo != null) {
                // If the package exist, we extract particular entries from package
                entries = packageSymbolInfo.getScopeEntry().symbol.scope.entries;
            }

            entries.forEach((name, scopeEntry) -> {
                /*
                 Note: Here we specifically remove the object attached functions shown since when we try to get the
                 visible symbols within the functions inside the objects, both the object reference as well as the
                 attached function is shown. For a more generalized solution, object attached functions are retrieved
                 from the object's scope entries
                 */
                if (scopeEntry.symbol instanceof BInvokableSymbol && scopeEntry.symbol.owner != null
                        && !(scopeEntry.symbol.owner instanceof BObjectTypeSymbol)) {
                    String symbolBoundedName = scopeEntry.symbol.owner.toString();
                    if (modifiedBType != null && symbolBoundedName.equals(modifiedBType.toString())) {
                        // TODO: Need to handle the name in a proper manner
                        String[] nameComponents = name.toString().split("\\.");
                        SymbolInfo actionFunctionSymbol =
                                new SymbolInfo(nameComponents[nameComponents.length - 1], scopeEntry);
                        resultList.add(actionFunctionSymbol);
                    }
                } else if ((scopeEntry.symbol instanceof BTypeSymbol)
                        && (SymbolKind.OBJECT.equals(scopeEntry.symbol.kind)
                        || SymbolKind.RECORD.equals(scopeEntry.symbol.kind))
                        && modifiedBType != null
                        && scopeEntry.symbol.type.toString().equals(modifiedBType.toString())) {
                    // Get the object/records' attached functions and the fields
                    Map<Name, Scope.ScopeEntry> attachedEntries = scopeEntry.symbol.scope.entries;
                    attachedEntries.forEach((entryName, attachedScopeEntry) -> {
                        if (!CommonUtil.isInvalidSymbol(attachedScopeEntry.symbol)) {
                            resultList.add(new SymbolInfo(entryName.getValue(), attachedScopeEntry));
                        }
                    });
                }
            });
            if (addBuiltIn) {
                CommonUtil.populateIterableAndBuiltinFunctions(variable, resultList, context);
            }
        } else if (delimiter.equals(UtilSymbolKeys.PKG_DELIMITER_KEYWORD)) {
            SymbolInfo packageSymbol = symbolInfos.stream().filter(item -> {
                Scope.ScopeEntry scopeEntry = item.getScopeEntry();
                return item.getSymbolName().equals(variableName) && scopeEntry.symbol instanceof BPackageSymbol;
            }).findFirst().orElse(null);

            Map<Name, Scope.ScopeEntry> scopeEntryMap = packageSymbol.getScopeEntry().symbol.scope.entries;
            resultList.addAll(loadActionsFunctionsAndTypesFromScope(scopeEntryMap));
        }

        return resultList;
    }

    /**
     * Get the invocations and fields against an identifier (functions, actions and fields).
     *
     * @param context               Text Document Service context (Completion Context)
     * @param variableName          Variable name to evaluate against (Can be package alias or defined variable)
     * @param delimiter             delimiter String either dot or action invocation symbol
     * @param symbolInfos           List of visible symbol info
     * @return {@link ArrayList}    List of filtered symbol info
     */
    public static List<SymbolInfo> getInvocationAndFieldSymbolsOnVar(LSServiceOperationContext context,
                                                                     String variableName, String delimiter,
                                                                     List<SymbolInfo> symbolInfos) {
        return getInvocationAndFieldSymbolsOnVar(context, variableName, delimiter, symbolInfos, true);
    }

    /**
     * Get the actions defined over and endpoint.
     * @param bEndpointVarSymbol    Endpoint variable symbol to evaluate
     * @return {@link List}         List of extracted actions as Symbol Info
     */
    public static List<SymbolInfo> getEndpointActions(BEndpointVarSymbol bEndpointVarSymbol) {
        List<SymbolInfo> endpointActions = new ArrayList<>();
        BType getClientFuncType = bEndpointVarSymbol.getClientFunction.type;
        BType boundType = ((BInvokableType) getClientFuncType).retType;
        boundType.tsymbol.scope.entries.forEach((name, scopeEntry) -> {
            String[] nameComponents = name.toString().split("\\.");
            if (scopeEntry.symbol instanceof BInvokableSymbol
                    && !nameComponents[nameComponents.length - 1].equals(UtilSymbolKeys.NEW_KEYWORD_KEY)) {
                SymbolInfo actionFunctionSymbol =
                        new SymbolInfo(nameComponents[nameComponents.length - 1], scopeEntry);
                endpointActions.add(actionFunctionSymbol);
            }
        });

        return endpointActions;
    }
    
    ///////////////////////////
    ///// Private Methods /////
    ///////////////////////////

    /**
     * Get the scope entries.
     *
     * @param bType         BType
     * @param symbolInfos   List of visible symbol info to find filter the items
     * @return {@link Map}  Scope entries map
     */
    private static Map<Name, Scope.ScopeEntry> getScopeEntries(BType bType, List<SymbolInfo> symbolInfos) {
        HashMap<Name, Scope.ScopeEntry> returnMap = new HashMap<>();
        BType modifiedBType = bType instanceof BUnionType ? getBTypeForUnionType((BUnionType) bType) : bType;
        if (modifiedBType == null) {
            return returnMap;
        }
        symbolInfos.forEach(symbolInfo -> {
            if (!CommonUtil.isInvalidSymbol(symbolInfo.getScopeEntry().symbol) 
                    && (symbolInfo.getScopeEntry().symbol instanceof BTypeSymbol 
                    && symbolInfo.getScopeEntry().symbol.getType() != null 
                    && symbolInfo.getScopeEntry().symbol.getType().toString().equals(modifiedBType.toString())) 
                    || (symbolInfo.getScopeEntry().symbol instanceof BInvokableSymbol
                    && CommonUtil.isValidInvokableSymbol(symbolInfo.getScopeEntry().symbol))) {
                returnMap.put(symbolInfo.getScopeEntry().symbol.getName(), symbolInfo.getScopeEntry());
            }
        });

        return returnMap;
    }

    /**
     * Get the variable symbol info by the name.
     *
     * @param name    name of the variable
     * @param symbols list of symbol info
     * @return {@link SymbolInfo}   Symbol Info extracted
     */
    public static SymbolInfo getVariableByName(String name, List<SymbolInfo> symbols) {
        return symbols.stream()
                .filter(symbolInfo -> symbolInfo.getSymbolName().equals(name))
                .findFirst()
                .orElse(null);
    }
    
    private static PackageID getPackageIDForBType(BType bType) {
        if (bType instanceof BArrayType) {
            return  ((BArrayType) bType).eType.tsymbol.pkgID;
        } else if (bType instanceof BUnionType) {
            List<BType> memberTypeList = new ArrayList<>(((BUnionType) bType).getMemberTypes());
            memberTypeList.removeIf(type -> type.tsymbol instanceof BErrorTypeSymbol || type instanceof BNilType);

            if (memberTypeList.size() == 1) {
                return memberTypeList.get(0).tsymbol.pkgID;
            }
            return null;
        }
        return bType.tsymbol.pkgID;
    }
    
    private static BType getBTypeForUnionType(BUnionType bType) {
        List<BType> memberTypeList = new ArrayList<>(bType.getMemberTypes());
        memberTypeList.removeIf(type -> type.tsymbol instanceof BErrorTypeSymbol || type instanceof BNilType);

        if (memberTypeList.size() == 1) {
            return memberTypeList.get(0);
        }
        
        return null;
    }

    private static List<SymbolInfo> loadActionsFunctionsAndTypesFromScope(Map<Name, Scope.ScopeEntry> entryMap) {
        List<SymbolInfo> actionFunctionList = new ArrayList<>();
        entryMap.forEach((name, value) -> {
            BSymbol symbol = value.symbol;
            if (((symbol instanceof BInvokableSymbol && ((BInvokableSymbol) symbol).receiverSymbol == null)
                    || (symbol instanceof BTypeSymbol && !(symbol instanceof BPackageSymbol))
                    || symbol instanceof BVarSymbol) && (symbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
                SymbolInfo entry = new SymbolInfo(name.toString(), value);
                actionFunctionList.add(entry);
            }
        });

        return actionFunctionList;
    }
}
