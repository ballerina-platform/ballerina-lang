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

import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.wso2.ballerinalang.compiler.semantics.model.Scope.NOT_FOUND_ENTRY;

/**
 * Utilities for filtering the symbols from completion context and symbol information lists.
 */
public class FilterUtils {

    

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
            return symbol.kind != null && symbol.kind != SymbolKind.OBJECT && symbol.kind != SymbolKind.CONSTANT;
        }).collect(Collectors.toList());
    }
}
