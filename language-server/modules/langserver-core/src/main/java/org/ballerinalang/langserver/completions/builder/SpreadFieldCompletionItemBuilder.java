/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.builder;

import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;

import java.util.List;

import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;

/**
 * This class is being used to build spread field completion item.
 *
 * @since 2201.1.1
 */
public class SpreadFieldCompletionItemBuilder {
    
    private static final String  SPREAD_OPERATOR = "...";

    /**
     * Build the constant {@link CompletionItem}.
     *
     * @param symbol   {@link Symbol}
     * @param typeName type name of the {@link Symbol}
     * @return {@link CompletionItem} generated completion item
     */
    public static CompletionItem build(Symbol symbol, String typeName, BallerinaCompletionContext context) {
        if (symbol.kind() == FUNCTION) {
            return SpreadFieldCompletionItemBuilder.build((FunctionSymbol) symbol, typeName, context);
        }
        String symbolName = symbol.getName().orElseThrow();
        String insertText = SPREAD_OPERATOR + symbolName;
        return build(insertText, insertText, CompletionItemKind.Variable, symbolName, typeName);
    }

    /**
     * Build the constant {@link CompletionItem}.
     *
     * @param symbol   {@link FunctionSymbol}
     * @param typeName type name of the {@link Symbol}
     * @return {@link CompletionItem} generated completion item
     */
    private static CompletionItem build(FunctionSymbol symbol, String typeName, BallerinaCompletionContext context) {
        String symbolName = symbol.getName().orElseThrow();
        List<String> funcArguments = CommonUtil.getFuncArguments(symbol, context);
        String insertText = SPREAD_OPERATOR + symbolName + (funcArguments.isEmpty() ? "()" : "(${1})");
        String label = SPREAD_OPERATOR + symbolName + "(" + String.join(", ", funcArguments) + ")";
        return build(insertText, label, CompletionItemKind.Function, symbolName, typeName);
    }

    /**
     * Build the constant {@link CompletionItem}.
     *
     * @param insertText insert text
     * @param label      label
     * @param kind       completion item kind
     * @param symbolName symbol name
     * @param typeName   type name of the {@link Symbol}
     * @return {@link CompletionItem} generated completion item
     */
    private static CompletionItem build(String insertText, String label, CompletionItemKind kind, String symbolName,
                                        String typeName) {
        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(label);
        completionItem.setKind(kind);
        completionItem.setDetail(typeName);
        completionItem.setFilterText(symbolName);
        completionItem.setInsertText(insertText);
        return completionItem;
    }

}
