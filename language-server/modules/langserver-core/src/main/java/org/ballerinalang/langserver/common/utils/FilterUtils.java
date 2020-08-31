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

import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
     * Get the actions defined over and endpoint.
     *
     * @param objectTypeSymbol Endpoint variable symbol to evaluate
     * @return {@link List} List of extracted actions as Symbol Info
     */
    public static List<Scope.ScopeEntry> getClientActions(BObjectTypeSymbol objectTypeSymbol) {
        return objectTypeSymbol.methodScope.entries.values().stream()
                .filter(scopeEntry -> (scopeEntry.symbol.flags & Flags.REMOTE) == Flags.REMOTE)
                .collect(Collectors.toList());
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

    ///////////////////////////
    ///// Private Methods /////
    ///////////////////////////
    public static List<Scope.ScopeEntry> getObjectMethodsAndFields(LSContext context,
                                                                   BObjectTypeSymbol objectSymbol,
                                                                   String symbolName) {
        String currentModule = context.get(DocumentServiceKeys.CURRENT_PKG_NAME_KEY);
        String objectOwnerModule = objectSymbol.pkgID.getName().getValue();
        boolean symbolInCurrentModule = currentModule.equals(objectOwnerModule);

        // Extract the method entries
        Map<Name, Scope.ScopeEntry> entries = objectSymbol.methodScope.entries.entrySet().stream()
                .filter(entry -> {
                    BSymbol entrySymbol = entry.getValue().symbol;
                    boolean isPrivate = (entrySymbol.flags & Flags.PRIVATE) == Flags.PRIVATE;
                    boolean isPublic = (entrySymbol.flags & Flags.PUBLIC) == Flags.PUBLIC;

                    return !((entrySymbol.getName().getValue().contains(".init") && !"self".equals(symbolName))
                            || (isPrivate && !"self".equals(symbolName))
                            || (!isPrivate && !isPublic && !symbolInCurrentModule));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // Extract Field Entries
        Map<Name, Scope.ScopeEntry> fieldEntries = objectSymbol.scope.entries.entrySet().stream()
                .filter(entry -> {
                    BSymbol entrySymbol = entry.getValue().symbol;
                    boolean isPrivate = (entrySymbol.flags & Flags.PRIVATE) == Flags.PRIVATE;
                    boolean isPublic = (entrySymbol.flags & Flags.PUBLIC) == Flags.PUBLIC;
                    return !((isPrivate && !"self".equals(symbolName))
                            || (!isPrivate && !isPublic && !symbolInCurrentModule));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        entries.putAll(fieldEntries);

        return new ArrayList<>(entries.values());
    }

    /**
     * When given a union of record types, iterate over the member types to extract the fields with the same name.
     * If a certain field with the same name contains in all records we extract the field entries
     *
     * @param unionType union type to evaluate
     * @param context   Language server operation context
     * @return {@link Map} map of scope entries
     */
    public static List<Scope.ScopeEntry> getInvocationsAndFieldsForUnionType(BUnionType unionType,
                                                                             LSContext context) {
        ArrayList<BType> memberTypes = new ArrayList<>(unionType.getMemberTypes());
        List<Scope.ScopeEntry> resultEntries = new ArrayList<>();
        CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
        SymbolTable symbolTable = SymbolTable.getInstance(compilerContext);
        Types types = Types.getInstance(compilerContext);
//        Integer invocationTokenType = context.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
        // check whether union consists of same type tag symbols
        BType firstMemberType = memberTypes.get(0);
        boolean allMatch = memberTypes.stream().allMatch(bType -> bType.tag == firstMemberType.tag);
        // If all the members are not same types we stop proceeding
        if (!allMatch) {
            resultEntries.addAll(getLangLibScopeEntries(unionType, symbolTable, types));
            return resultEntries;
        }

        resultEntries.addAll(getLangLibScopeEntries(firstMemberType, symbolTable, types));
        if (firstMemberType.tag == TypeTags.RECORD) {
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
            Map<Name, Scope.ScopeEntry> firstMemberEntries = ((BRecordType) firstMemberType).tsymbol.scope.entries;
            for (int i = 0; i < counts.size(); i++) {
                if (counts.get(i) != memberTypes.size()) {
                    continue;
                }
                Name name = firstMemberFieldKeys.get(i);
                BSymbol symbol = firstMemberEntries.get(name).symbol;
                if (firstMemberType.tag == TypeTags.RECORD /*&& (invocationTokenType == BallerinaParser.DOT
                        || invocationTokenType == BallerinaParser.NOT)*/
                        && (org.ballerinalang.jvm.util.Flags.isFlagOn(symbol.flags, Flags.OPTIONAL))) {
                    continue;
                }
                resultEntries.add(firstMemberEntries.get(name));
            }
        }

        return resultEntries;
    }

    /**
     * Retrieve lang-lib scope entries for this typeTag.
     *
     * @param bType    {@link BType}
     * @param symTable {@link SymbolTable}
     * @param types    {@link Types} analyzer
     * @return map of scope entries
     */
    public static List<Scope.ScopeEntry> getLangLibScopeEntries(BType bType, SymbolTable symTable, Types types) {
        List<Scope.ScopeEntry> entries = new ArrayList<>();
        entries.addAll(symTable.langValueModuleSymbol.scope.entries.values());
        entries.addAll(symTable.langQueryModuleSymbol.scope.entries.values());
        switch (bType.tag) {
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
                entries.addAll(symTable.langArrayModuleSymbol.scope.entries.values());
                break;
            case TypeTags.DECIMAL:
                entries.addAll(symTable.langDecimalModuleSymbol.scope.entries.values());
                break;
            case TypeTags.ERROR:
                entries.addAll(symTable.langErrorModuleSymbol.scope.entries.values());
                break;
            case TypeTags.FLOAT:
                entries.addAll(symTable.langFloatModuleSymbol.scope.entries.values());
                break;
            case TypeTags.FUTURE:
                entries.addAll(symTable.langFutureModuleSymbol.scope.entries.values());
                break;
            case TypeTags.INT:
                entries.addAll(symTable.langIntModuleSymbol.scope.entries.values());
                break;
            case TypeTags.MAP:
            case TypeTags.RECORD:
                entries.addAll(symTable.langMapModuleSymbol.scope.entries.values());
                return entries;
            case TypeTags.OBJECT:
                entries.addAll(symTable.langObjectModuleSymbol.scope.entries.values());
                break;
            case TypeTags.STREAM:
                entries.addAll(symTable.langStreamModuleSymbol.scope.entries.values());
                break;
            case TypeTags.TABLE:
                entries.addAll(symTable.langTableModuleSymbol.scope.entries.values());
                break;
            case TypeTags.STRING:
                entries.addAll(symTable.langStringModuleSymbol.scope.entries.values());
                break;
            case TypeTags.TYPEDESC:
                entries.addAll(symTable.langTypedescModuleSymbol.scope.entries.values());
                break;
            case TypeTags.XML:
                entries.addAll(symTable.langXmlModuleSymbol.scope.entries.values());
                break;
            case TypeTags.BOOLEAN:
                entries.addAll(symTable.langBooleanModuleSymbol.scope.entries.values());
                break;
            default:
                break;
        }

        return entries.stream().filter(entry -> {
            BSymbol symbol = entry.symbol;
            if ((symbol.flags & Flags.PUBLIC) != Flags.PUBLIC) {
                return false;
            }
            if (symbol instanceof BInvokableSymbol) {
                List<BVarSymbol> params = ((BInvokableSymbol) symbol).params;
                return params.isEmpty() || params.get(0).type.tag == bType.tag ||
                        (types.isAssignable(bType, params.get(0).type));
            }
            return symbol.kind != null && symbol.kind != SymbolKind.OBJECT;
        }).collect(Collectors.toList());
    }
}
