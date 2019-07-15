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

import com.google.common.collect.Lists;
import org.antlr.v4.runtime.CommonToken;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.wso2.ballerinalang.compiler.semantics.model.Scope.NOT_FOUND_ENTRY;

/**
 * Utilities for filtering the symbols from completion context and symbol information lists.
 */
public class FilterUtils {

    /**
     * Get the invocations and fields against an identifier (functions, actions and fields).
     *
     * @param context Text Document Service context (Completion Context)
     * @param varName Variable name to evaluate against (Can be package alias or defined variable)
     * @param delimiter    delimiter type either dot or action invocation symbol
     * @param defaultTokens         List of Default tokens
     * @param delimIndex            Delimiter index
     * @param addBuiltIn   Add built-in functions
     * @return {@link ArrayList}    List of filtered symbol info
     */
    public static List<SymbolInfo> filterVariableEntriesOnDelimiter(LSContext context, String varName, int delimiter,
                                                                    List<CommonToken> defaultTokens, int delimIndex,
                                                                    boolean addBuiltIn) {
        List<SymbolInfo> visibleSymbols = context.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
        visibleSymbols.removeIf(CommonUtil.invalidSymbolsPredicate());
        if (BallerinaParser.RARROW == delimiter) {
            SymbolInfo variable = getVariableByName(varName, visibleSymbols);
            if (variable != null && CommonUtil.isClientObject(variable.getScopeEntry().symbol)) {
                // Handling action invocations ep -> ...
                return getClientActions((BObjectTypeSymbol) variable.getScopeEntry().symbol.type.tsymbol);
            }
            // Handling worker-interactions eg. msg -> ...
            visibleSymbols.removeIf(symbolInfo -> symbolInfo.getScopeEntry().symbol instanceof BTypeSymbol);
            return visibleSymbols;
        }
        if (BallerinaParser.LARROW == delimiter) {
            // Handling worker-interaction msg <- ...
            visibleSymbols.removeIf(symbolInfo -> symbolInfo.getScopeEntry().symbol instanceof BTypeSymbol);
            return visibleSymbols;
        }
        if (BallerinaParser.DOT == delimiter || BallerinaParser.NOT == delimiter) {
            return getInvocationsAndFields(context, addBuiltIn, defaultTokens, delimIndex);
        }
        if (BallerinaParser.COLON == delimiter) {
            Optional<SymbolInfo> pkgSymbol = visibleSymbols.stream()
                    .filter(item -> item.getSymbolName().equals(varName)
                            && item.getScopeEntry().symbol instanceof BPackageSymbol)
                    .findFirst();

            if (!pkgSymbol.isPresent()) {
                return new ArrayList<>();
            }

            Map<Name, Scope.ScopeEntry> scopeEntryMap = pkgSymbol.get().getScopeEntry().symbol.scope.entries;
            return loadActionsFunctionsAndTypesFromScope(scopeEntryMap);
        }

        return new ArrayList<>();
    }

    /**
     * Get the invocations and fields against an identifier (functions, actions and fields).
     *
     * @param context               Text Document Service context (Completion Context)
     * @param variableName          Variable name to evaluate against (Can be package alias or defined variable)
     * @param delimiter             delimiter type either dot or action invocation symbol
     * @param defaultTokens         List of Default tokens
     * @param delimIndex            Delimiter index
     * @return {@link ArrayList}    List of filtered symbol info
     */
    public static List<SymbolInfo> filterVariableEntriesOnDelimiter(LSContext context, String variableName,
                                                                    int delimiter, List<CommonToken> defaultTokens,
                                                                    int delimIndex) {
        return filterVariableEntriesOnDelimiter(context, variableName, delimiter, defaultTokens, delimIndex, true);
    }

    /**
     * Get the actions defined over and endpoint.
     * @param bObjectTypeSymbol    Endpoint variable symbol to evaluate
     * @return {@link List}         List of extracted actions as Symbol Info
     */
    public static List<SymbolInfo> getClientActions(BObjectTypeSymbol bObjectTypeSymbol) {
        return bObjectTypeSymbol.methodScope.entries.entrySet().stream()
                .filter(entry -> (entry.getValue().symbol.flags & Flags.REMOTE) == Flags.REMOTE)
                .map(entry -> {
                    String actionName = entry.getKey().getValue();
                    String[] nameComponents = actionName.split("\\.");
                    return new SymbolInfo(nameComponents[nameComponents.length - 1], entry.getValue());
                })
                .collect(Collectors.toList());
    }

    /**
     * Get invocations and fields.
     * Get the invocations and fields for a dot separated chained statement
     * Eg: int x = var1.field1.testFunction()
     *
     * @param context Language server operation context
     * @param addBuiltIn whether add the builtin functions
     * @param defaultTokens List of default tokens removed (LHS Tokens)
     * @param delimIndex Delimiter index
     * @return {@link List} List of extracted symbols
     */
    private static List<SymbolInfo> getInvocationsAndFields(LSContext context, boolean addBuiltIn,
                                                            List<CommonToken> defaultTokens, int delimIndex) {
        List<SymbolInfo> resultList = new ArrayList<>();
        List<ChainedFieldModel> invocationFieldList = getInvocationFieldList(defaultTokens, delimIndex);

        ChainedFieldModel startField = invocationFieldList.get(0);
        List<SymbolInfo> symbolList = context.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
        BSymbol bSymbol = getVariableByName(startField.name.getText(), symbolList).getScopeEntry().symbol;
        BType symbolType = bSymbol instanceof BInvokableSymbol ? ((BInvokableSymbol) bSymbol).retType : bSymbol.type;
        BType modifiedSymbolBType = getModifiedBType(symbolType);
        Map<Name, Scope.ScopeEntry> scopeEntries = getScopeEntriesForSymbol(modifiedSymbolBType, context);

        for (int itr = 1; itr < invocationFieldList.size(); itr++) {
            ChainedFieldModel fieldModel = invocationFieldList.get(itr);
            if (scopeEntries == null) {
                break;
            }
            Optional<Scope.ScopeEntry> entry = getScopeEntryWithName(scopeEntries, fieldModel);
            if (!entry.isPresent()) {
                break;
            }
            bSymbol = entry.get().symbol;
            symbolType = bSymbol instanceof BInvokableSymbol ? ((BInvokableSymbol) bSymbol).retType : bSymbol.type;
            modifiedSymbolBType = getModifiedBType(symbolType);
            scopeEntries = getScopeEntriesForSymbol(modifiedSymbolBType, context);
        }
        if (scopeEntries == null) {
            return new ArrayList<>();
        }
        scopeEntries.forEach((entryName, fieldEntry) ->
                resultList.add(new SymbolInfo(fieldEntry.symbol.getName().value, fieldEntry)));

        if (addBuiltIn) {
            CommonUtil.populateIterableAndBuiltinFunctions(modifiedSymbolBType, resultList, context);
        }

        return resultList;
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

    public static Optional<BSymbol> getBTypeEntry(Scope.ScopeEntry entry) {
        while (entry != NOT_FOUND_ENTRY) {
            if ((entry.symbol.tag & SymTag.TYPE) == SymTag.TYPE) {
                if (!CommonUtil.symbolContainsInvalidChars(entry.symbol) && entry.symbol instanceof BTypeSymbol) {
                    return Optional.of(entry.symbol);
                }
            }
            entry = entry.next;
        }
        return Optional.empty();
    }

    public static boolean isBTypeEntry(Scope.ScopeEntry entry) {
        return getBTypeEntry(entry).isPresent();
    }
    
    ///////////////////////////
    ///// Private Methods /////
    ///////////////////////////
    
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
        entryMap.forEach((name, scopeEntry) -> {
            BSymbol symbol = scopeEntry.symbol;
            if (((symbol instanceof BInvokableSymbol && ((BInvokableSymbol) symbol).receiverSymbol == null)
                    || isBTypeEntry(scopeEntry)
                    || symbol instanceof BVarSymbol || symbol instanceof BConstantSymbol)
                    && (symbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
                SymbolInfo entry = new SymbolInfo(name.toString(), scopeEntry);
                actionFunctionList.add(entry);
            }
        });

        return actionFunctionList;
    }
    
    private static BType getModifiedBType(BType bType) {
        return bType instanceof BUnionType ? getBTypeForUnionType((BUnionType) bType) : bType.tsymbol.type;
    }

    private static List<ChainedFieldModel> getInvocationFieldList(List<CommonToken> defaultTokens, int startIndex) {
        int traverser = startIndex;
        int rightParenthesisCount = 0;
        boolean invocation = false;
        List<ChainedFieldModel> fieldList = new ArrayList<>();
        while (traverser >= 0) {
            CommonToken token = defaultTokens.get(traverser);
            int type = token.getType();

            if (type == BallerinaParser.RIGHT_PARENTHESIS) {
                if (!invocation) {
                    invocation = true;
                }
                rightParenthesisCount++;
                traverser--;
            } else if (type == BallerinaParser.LEFT_PARENTHESIS && rightParenthesisCount > 0) {
                rightParenthesisCount--;
                traverser--;
            } else if (type == BallerinaParser.DOT || type == BallerinaParser.NOT || rightParenthesisCount > 0) {
                traverser--;
            } else if (type == BallerinaParser.Identifier && rightParenthesisCount == 0) {
                InvocationFieldType fieldType;
                CommonToken packageAlias = null;
                traverser--;

                if (invocation) {
                    fieldType = InvocationFieldType.INVOCATION;
                    invocation = false;
                } else {
                    fieldType = InvocationFieldType.FIELD;
                }

                if (traverser > 0 && defaultTokens.get(traverser).getType() == BallerinaParser.COLON) {
                    packageAlias = defaultTokens.get(traverser - 1);
                }
                ChainedFieldModel model = new ChainedFieldModel(fieldType, token, packageAlias);
                fieldList.add(model);
            } else {
                break;
            }
        }

        return Lists.reverse(fieldList);
    }

    /**
     * Analyze the given symbol type and extracts the invocations and fields from the scope entries.
     * When extracting the invocations, extract the type attached functions
     *
     * @param symbolType BType to evaluate
     * @param context Language Server Operation Context
     * @return {@link Map} Scope Entry Map
     */
    private static Map<Name, Scope.ScopeEntry> getScopeEntriesForSymbol(BType symbolType, LSContext context) {
//        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
//        SymbolTable symbolTable = SymbolTable.getInstance(compilerContext);
//        PackageID pkgId = getPackageIDForBType(symbolType);
//        String packageIDString = pkgId == null ? "" : pkgId.getName().getValue();
//        String builtinPkgName = symbolTable.builtInPackageSymbol.pkgID.name.getValue();

//        if (packageIDString.equals(builtinPkgName)) {
//            // Extract the invokable entries only from the scope entries
//            return symbolTable.builtInPackageSymbol.scope.entries.entrySet().stream().filter(entry -> {
//                BSymbol scopeEntrySymbol = entry.getValue().symbol;
//                if (!(scopeEntrySymbol instanceof BInvokableSymbol) || scopeEntrySymbol instanceof BOperatorSymbol) {
//                    return false;
//                }
//                BType ownerType = getModifiedBType(((BInvokableSymbol) scopeEntrySymbol).owner.type);
//                return ownerType == symbolType;
//            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//        }
        if (symbolType.tsymbol instanceof BObjectTypeSymbol) {
            BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) symbolType.tsymbol;
            Map<Name, Scope.ScopeEntry> entries = objectTypeSymbol.methodScope.entries;
            entries.putAll(objectTypeSymbol.scope.entries);
            return entries;
        }
        return symbolType.tsymbol.scope.entries;
    }

    private static Optional<Scope.ScopeEntry> getScopeEntryWithName(Map<Name, Scope.ScopeEntry> entries,
                                                                    ChainedFieldModel fieldModel) {
        return entries.values().stream().filter(scopeEntry -> {
            BSymbol symbol = scopeEntry.symbol;
            String[] symbolNameComponents = symbol.getName().getValue().split("\\.");
            String symbolName = symbolNameComponents[symbolNameComponents.length - 1];
            if (!symbolName.equals(fieldModel.name.getText())) {
                return false;
            }
            return (fieldModel.fieldType == InvocationFieldType.INVOCATION && symbol instanceof BInvokableSymbol)
                    || (fieldModel.fieldType == InvocationFieldType.FIELD && !(symbol instanceof BInvokableSymbol));

        }).findAny();
    }

    /**
     * Data model for the chained field.
     */
    private static class ChainedFieldModel {
        InvocationFieldType fieldType;
        CommonToken name;
        CommonToken pkgAlias;

        ChainedFieldModel(InvocationFieldType fieldType, CommonToken name, CommonToken pkgAlias) {
            this.fieldType = fieldType;
            this.name = name;
            this.pkgAlias = pkgAlias;
        }
    }

    /**
     * Enum for chained field type.
     */
    public enum InvocationFieldType {
        FIELD,
        INVOCATION
    }
}
