/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.langserver.common.utils.completion;

import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.builder.BVariableCompletionItemBuilder;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility operations on the BLangRecordLiterals.
 */
public class BLangRecordLiteralUtil {

    private BLangRecordLiteralUtil() {
    }

    /**
     * Get the record field completion items.
     * 
     * @param context Language server operation Context
     * @param recordLiteral             Record Literal
     * @return {@link LSCompletionItem}   List of Completion Items
     */
    public static List<LSCompletionItem> getFieldsForMatchingRecord(LSContext context,
                                                                    BLangRecordLiteral recordLiteral) {
        BType expectedType = recordLiteral.expectedType;
        List<Scope.ScopeEntry> visibleSymbols = context.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
        Optional<BType> evalType = expectedType instanceof BUnionType ? getRecordTypeFromUnionType(expectedType)
                : Optional.ofNullable(expectedType);
        if (!evalType.isPresent() || !(evalType.get() instanceof BRecordType)) {
            return new ArrayList<>();
        }
        List<BField> fields = ((BRecordType) evalType.get()).fields;
        List<LSCompletionItem> completionItems = CommonUtil.getRecordFieldCompletionItems(context, fields);
        completionItems.add(CommonUtil.getFillAllStructFieldsItem(context, fields));
        completionItems.addAll(getVariableCompletionsForFields(context, visibleSymbols, fields));

        return completionItems;
    }
    
    private static Optional<BType> getRecordTypeFromUnionType(BType bType) {
        if (!(bType instanceof BUnionType)) {
            return Optional.empty();
        }
        List<BType> filteredRecords = ((BUnionType) bType).getMemberTypes().stream()
                .filter(type -> type instanceof BRecordType)
                .collect(Collectors.toList());
        
        if (filteredRecords.size() == 1) {
            return Optional.ofNullable(filteredRecords.get(0));
        }
        return Optional.empty();
    }
    
    private static List<LSCompletionItem> getVariableCompletionsForFields(LSContext ctx,
                                                                          List<Scope.ScopeEntry> visibleSymbols,
                                                                          List<BField> recordFields) {
        Map<String, BType> filedTypeMap = new HashMap<>();
        recordFields.forEach(bField -> filedTypeMap.put(bField.name.value, bField.type));
        List<LSCompletionItem> completionItems = new ArrayList<>();
        visibleSymbols.forEach(scopeEntry -> {
            BSymbol symbol = scopeEntry.symbol;
            BType type = symbol instanceof BConstantSymbol ? ((BConstantSymbol) symbol).literalType : symbol.type;
            String symbolName = symbol.name.getValue();
            if (filedTypeMap.containsKey(symbolName) && filedTypeMap.get(symbolName) == type
                    && symbol instanceof BVarSymbol) {
                String bTypeName = CommonUtil.getBTypeName(type, ctx, false);
                CompletionItem cItem = BVariableCompletionItemBuilder.build((BVarSymbol) symbol, symbolName, bTypeName);
                completionItems.add(new SymbolCompletionItem(ctx, symbol, cItem));
            }
        });
        
        return completionItems;
    }
}
