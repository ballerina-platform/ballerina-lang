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
import org.ballerinalang.langserver.completions.builder.BFunctionCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.BVariableCompletionItemBuilder;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstructorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility operations on the BLangRecordLiterals.
 */
public class BLangRecordLiteralUtil {

    private static final String ELLIPSIS = "...";

    private BLangRecordLiteralUtil() {
    }

    public static List<LSCompletionItem> getSpreadCompletionItems(LSContext context, BType evalType) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        List<BType> typeList = getTypeListForMapAndRecords(evalType);
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        visibleSymbols.removeIf(CommonUtil.invalidSymbolsPredicate());

        for (Scope.ScopeEntry visibleSymbol : visibleSymbols) {
            BSymbol symbol = visibleSymbol.symbol;
            getSpreadableCompletionItem(context, symbol, typeList).ifPresent(completionItems::add);
        }

        return completionItems;
    }

    private static Optional<LSCompletionItem> getSpreadableCompletionItem(LSContext context, BSymbol bSymbol,
                                                                          List<BType> refTypeList) {
        BType bType;
        if (bSymbol instanceof BInvokableSymbol && !(bSymbol instanceof BConstructorSymbol)) {
            bType = ((BInvokableSymbol) bSymbol).retType;
        } else if (bSymbol instanceof BVarSymbol && !(bSymbol instanceof BConstructorSymbol)) {
            bType = bSymbol.type;
        } else {
            return Optional.empty();
        }

        List<BType> symbolTypeList = getTypeListForMapAndRecords(bType);
        // if bType is not a map or record, then the symbol type list is empty 
        boolean canSpread = !symbolTypeList.isEmpty() && refTypeList.containsAll(symbolTypeList);

        CompletionItem cItem;
        if (canSpread && bSymbol instanceof BInvokableSymbol) {
            cItem = BFunctionCompletionItemBuilder.build((BInvokableSymbol) bSymbol, context);
        } else if (canSpread) {
            cItem = BVariableCompletionItemBuilder.build((BVarSymbol) bSymbol, bSymbol.name.getValue(),
                    CommonUtil.getBTypeName(bType, context, false));
        } else {
            return Optional.empty();
        }
        // Modify the spread completion item to automatically determine the prefixed number of dots to complete ellipsis
        // TODO: Fix
        modifySpreadCompletionItem(context, cItem);

        return Optional.of(new SymbolCompletionItem(context, bSymbol, cItem));
    }

    private static List<BType> getTypeListForMapAndRecords(BType bType) {
        if (bType instanceof BMapType) {
            BType constraint = ((BMapType) bType).constraint;
            if (constraint instanceof BUnionType) {
                return new ArrayList<>(((BUnionType) constraint).getMemberTypes());
            }
            return Collections.singletonList(constraint);
        } else if (bType instanceof BRecordType) {
            return ((BRecordType) bType).fields.values().stream()
                    .map(bField -> bField.type)
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    private static void modifySpreadCompletionItem(LSContext context, CompletionItem cItem) {
        // TODO: Fix 
//        List<CommonToken> commonTokens = context.get(SourcePruneKeys.LHS_DEFAULT_TOKENS_KEY);
//        String lastToken = (commonTokens.isEmpty()) ? "" : commonTokens.get(commonTokens.size() - 1).getText();
//        long dotCount = lastToken.codePoints().filter(charVal -> charVal == '.').count();
        String prefix = String.join("", Collections.nCopies(ELLIPSIS.length(), "."));

        cItem.setInsertText(prefix + cItem.getInsertText());
        cItem.setLabel(ELLIPSIS + cItem.getLabel());
    }
}
