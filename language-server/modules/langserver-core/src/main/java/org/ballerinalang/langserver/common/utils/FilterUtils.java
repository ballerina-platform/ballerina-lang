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
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
     * @return {@link ArrayList}    List of filtered symbol info
     */
    public static List<SymbolInfo> filterVariableEntriesOnDelimiter(LSContext context, String varName, int delimiter,
                                                                    List<CommonToken> defaultTokens, int delimIndex) {
        List<SymbolInfo> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
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
        if (BallerinaParser.DOT == delimiter || BallerinaParser.OPTIONAL_FIELD_ACCESS == delimiter
                || BallerinaParser.NOT == delimiter) {
            return getInvocationsAndFields(context, defaultTokens, delimIndex);
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
     * @param defaultTokens List of default tokens removed (LHS Tokens)
     * @param delimIndex Delimiter index
     * @return {@link List} List of extracted symbols
     */
    private static List<SymbolInfo> getInvocationsAndFields(LSContext context, List<CommonToken> defaultTokens,
                                                            int delimIndex) {
        List<SymbolInfo> resultList = new ArrayList<>();
        List<ChainedFieldModel> invocationFieldList = getInvocationFieldList(defaultTokens, delimIndex);

        ChainedFieldModel startField = invocationFieldList.get(0);
        List<SymbolInfo> symbolList = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        BSymbol bSymbol = getVariableByName(startField.name.getText(), symbolList).getScopeEntry().symbol;
        BType symbolType = bSymbol instanceof BInvokableSymbol ? ((BInvokableSymbol) bSymbol).retType : bSymbol.type;
        BType modifiedSymbolBType = getModifiedBType(symbolType);
        Map<Name, Scope.ScopeEntry> scopeEntries = getInvocationsAndFieldsForSymbol(modifiedSymbolBType, context);

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
            scopeEntries = getInvocationsAndFieldsForSymbol(modifiedSymbolBType, context);
        }
        if (scopeEntries == null) {
            return new ArrayList<>();
        }
        scopeEntries.forEach((entryName, fieldEntry) ->
                resultList.add(new SymbolInfo(fieldEntry.symbol.getName().value, fieldEntry)));

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
    
    private static BType getBTypeForUnionType(BUnionType bType) {
        List<BType> memberTypeList = new ArrayList<>(bType.getMemberTypes());
        memberTypeList.removeIf(type -> type.tsymbol instanceof BErrorTypeSymbol || type instanceof BNilType);

        if (memberTypeList.size() == 1) {
            return memberTypeList.get(0);
        }
        
        return bType;
    }

    private static List<SymbolInfo> loadActionsFunctionsAndTypesFromScope(Map<Name, Scope.ScopeEntry> entryMap) {
        List<SymbolInfo> actionFunctionList = new ArrayList<>();
        entryMap.forEach((name, scopeEntry) -> {
            BSymbol symbol = scopeEntry.symbol;
            if (((symbol instanceof BInvokableSymbol && ((BInvokableSymbol) symbol).receiverSymbol == null)
                    || isBTypeEntry(scopeEntry)
                    || symbol instanceof BVarSymbol)
                    && (symbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
                SymbolInfo entry = new SymbolInfo(name.toString(), scopeEntry);
                actionFunctionList.add(entry);
            }
        });

        return actionFunctionList;
    }
    
    private static BType getModifiedBType(BType bType) {
        return bType instanceof BUnionType ? getBTypeForUnionType((BUnionType) bType) : bType;
    }

    private static List<ChainedFieldModel> getInvocationFieldList(List<CommonToken> defaultTokens, int startIndex) {
        int traverser = startIndex;
        int rightParenthesisCount = 0;
        boolean invocation = false;
        List<ChainedFieldModel> fieldList = new ArrayList<>();
        Pattern pattern = Pattern.compile("^\\w+$");
        boolean captureNextValidField = false;
        while (traverser >= 0) {
            CommonToken token = defaultTokens.get(traverser);
            int type = token.getType();
            Matcher matcher = pattern.matcher(token.getText());
            if (type == BallerinaParser.RIGHT_PARENTHESIS) {
                if (!invocation) {
                    invocation = true;
                }
                rightParenthesisCount++;
                traverser--;
            } else if (type == BallerinaParser.LEFT_PARENTHESIS && rightParenthesisCount > 0) {
                rightParenthesisCount--;
                traverser--;
            } else if (type == BallerinaParser.DOT || type == BallerinaParser.NOT
                    || type == BallerinaParser.OPTIONAL_FIELD_ACCESS || rightParenthesisCount > 0) {
                captureNextValidField = true;
                traverser--;
            } else if (matcher.find() && rightParenthesisCount == 0 && captureNextValidField) {
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
                captureNextValidField = false;
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
    private static Map<Name, Scope.ScopeEntry> getInvocationsAndFieldsForSymbol(BType symbolType, LSContext context) {
        Integer invocationToken = context.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        SymbolTable symbolTable = SymbolTable.getInstance(compilerContext);
        Types types = Types.getInstance(compilerContext);
        Map<Name, Scope.ScopeEntry> entries = new HashMap<>();

        /*
        LangLib checks also contains a check for the object type tag. But we skip and instead extract the entries
        from the object symbol itself
         */
        if (symbolType.tsymbol instanceof BObjectTypeSymbol) {
            BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) symbolType.tsymbol;
            entries.putAll(objectTypeSymbol.methodScope.entries);
            entries.putAll(objectTypeSymbol.scope.entries);
            return entries.entrySet().stream().filter(entry -> (!(entry.getValue().symbol instanceof BInvokableSymbol))
                    || ((entry.getValue().symbol.flags & Flags.REMOTE) != Flags.REMOTE))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            entries.putAll(getLangLibScopeEntries(symbolType, symbolTable, types));
            if (symbolType instanceof BUnionType) {
                entries.putAll(getInvocationsAndFieldsForUnionType((BUnionType) symbolType));
            } else if (symbolType.tsymbol != null && symbolType.tsymbol.scope != null) {
                Map<Name, Scope.ScopeEntry> filteredEntries = symbolType.tsymbol.scope.entries.entrySet().stream()
                        .filter(entry -> {
                            if (symbolType.tag == TypeTags.RECORD && (invocationToken == BallerinaParser.DOT
                                    || invocationToken == BallerinaParser.NOT)) {
                                return !org.ballerinalang.model.util.Flags.isFlagOn(entry.getValue().symbol.flags,
                                        Flags.OPTIONAL);
                            }
                            return true;
                        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                entries.putAll(filteredEntries);
            }
        }
        return entries.entrySet().stream().filter(entry -> (!(entry.getValue().symbol instanceof BInvokableSymbol))
                || ((entry.getValue().symbol.flags & Flags.REMOTE) != Flags.REMOTE))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * When given a union of record types, iterate over the member types to extract the fields with the same name.
     * If a certain field with the same name contains in all records we extract the field entries
     * 
     * @param unionType union type to evaluate
     * @return {@link Map} map of scope entries
     */
    private static Map<Name, Scope.ScopeEntry> getInvocationsAndFieldsForUnionType(BUnionType unionType) {
        ArrayList<BType> memberTypes = new ArrayList<>(unionType.getMemberTypes());
        Map<Name, Scope.ScopeEntry> resultEntries = new HashMap<>();
        boolean allMatch = memberTypes.stream().allMatch(bType -> bType.tag == TypeTags.RECORD);
        // If all the members are not record types we stop proceeding
        if (!allMatch) {
            return resultEntries;
        }
        // Keep track of the occurrences of each of the field names
        LinkedHashMap<String, Integer> memberOccurrenceCounts = new LinkedHashMap<>();
        List<Name> firstMemberFieldKeys = new ArrayList<>();
        /*
        We keep only the name fields of the first member type since a field has to appear in all the member types
         */
        for (int memberCounter = 0; memberCounter < memberTypes.size(); memberCounter++) {
            int finalMemberCounter = memberCounter;
            ((BRecordType) memberTypes.get(memberCounter)).tsymbol.scope.entries.keySet()
                    .forEach(name -> {
                        if (memberOccurrenceCounts.containsKey(name.value)) {
                            memberOccurrenceCounts.put(name.value, memberOccurrenceCounts.get(name.value) + 1);
                        } else if (finalMemberCounter == 0) {
                            // Fields are only captured for the first member type, otherwise the count is increased
                            firstMemberFieldKeys.add(name);
                            memberOccurrenceCounts.put(name.value, 1);
                        }
                    });
        }
        if (memberOccurrenceCounts.size() == 0) {
            return resultEntries;
        }
        List<Integer> counts = new ArrayList<>(memberOccurrenceCounts.values());
        Map<Name, Scope.ScopeEntry> firstMemberEntries = ((BRecordType) memberTypes.get(0)).tsymbol.scope.entries;
        for (int i = 0; i < counts.size(); i++) {
            if (counts.get(i) != memberTypes.size()) {
                continue;
            }
            Name name = firstMemberFieldKeys.get(i);
            resultEntries.put(name, firstMemberEntries.get(name));
        }
        
        return resultEntries;
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
     * Retrieve lang-lib scope entries for this typeTag.
     *
     * @param bType   {@link BType}
     * @param symTable  {@link SymbolTable}
     * @param types {@link Types} analyzer
     * @return  map of scope entries
     */
    public static Map<Name, Scope.ScopeEntry> getLangLibScopeEntries(BType bType, SymbolTable symTable, Types types) {
        Map<Name, Scope.ScopeEntry> entries = new HashMap<>(symTable.langValueModuleSymbol.scope.entries);
        switch (bType.tag) {
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
                entries.putAll(symTable.langArrayModuleSymbol.scope.entries);
                break;
            case TypeTags.DECIMAL:
                entries.putAll(symTable.langDecimalModuleSymbol.scope.entries);
                break;
            case TypeTags.ERROR:
                entries.putAll(symTable.langErrorModuleSymbol.scope.entries);
                break;
            case TypeTags.FLOAT:
                entries.putAll(symTable.langFloatModuleSymbol.scope.entries);
                break;
            case TypeTags.FUTURE:
                entries.putAll(symTable.langFutureModuleSymbol.scope.entries);
                break;
            case TypeTags.INT:
                entries.putAll(symTable.langIntModuleSymbol.scope.entries);
                break;
            case TypeTags.MAP:
            case TypeTags.RECORD:
                entries.putAll(symTable.langMapModuleSymbol.scope.entries);
                return entries;
            case TypeTags.OBJECT:
                entries.putAll(symTable.langObjectModuleSymbol.scope.entries);
                break;
            case TypeTags.STREAM:
                entries.putAll(symTable.langStreamModuleSymbol.scope.entries);
                break;
            case TypeTags.STRING:
                entries.putAll(symTable.langStringModuleSymbol.scope.entries);
                break;
            case TypeTags.TABLE:
                entries.putAll(symTable.langTableModuleSymbol.scope.entries);
                break;
            case TypeTags.TYPEDESC:
                entries.putAll(symTable.langTypedescModuleSymbol.scope.entries);
                break;
            case TypeTags.XML:
                entries.putAll(symTable.langXmlModuleSymbol.scope.entries);
                break;
            default:
                break;
        }

        return entries.entrySet().stream().filter(entry -> {
            BSymbol symbol = entry.getValue().symbol;
            if (symbol instanceof BInvokableSymbol) {
                List<BVarSymbol> params = ((BInvokableSymbol) symbol).params;
                return params.isEmpty() || params.get(0).type.tag == bType.tag ||
                        (types.isAssignable(params.get(0).type, bType));
            }
            return symbol.kind != null;
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
