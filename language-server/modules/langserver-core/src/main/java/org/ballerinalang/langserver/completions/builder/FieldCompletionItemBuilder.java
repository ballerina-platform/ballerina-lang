/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.compiler.api.symbols.ObjectFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;

/**
 * Completion item builder for the object fields and for record fields.
 *
 * @since 2.0.0
 */
public class FieldCompletionItemBuilder {
    private FieldCompletionItemBuilder() {
    }

    /**
     * Build the constant {@link CompletionItem}.
     *
     * @param symbol record field symbol or the object field symbol
     * @return {@link CompletionItem} generated completion item
     */
    private static CompletionItem getCompletionItem(Symbol symbol) {
        String recordFieldName = symbol.getName().orElseThrow();

        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(recordFieldName);
        completionItem.setInsertText(recordFieldName);
        completionItem.setKind(CompletionItemKind.Field);
        return completionItem;
    }

    /**
     * Build the constant {@link CompletionItem}.
     *
     * @param symbol {@link RecordFieldSymbol}
     * @return {@link CompletionItem} generated completion item
     */
    public static CompletionItem build(RecordFieldSymbol symbol) {
        return getCompletionItem(symbol);
    }

    /**
     * Build the constant {@link CompletionItem}.
     *
     * @param symbol {@link ObjectFieldSymbol}
     * @return {@link CompletionItem} generated completion item
     */
    public static CompletionItem build(ObjectFieldSymbol symbol) {
        return getCompletionItem(symbol);
    }

    /**
     * Build the constant {@link CompletionItem}.
     *
     * @param objectFieldSymbol {@link ObjectFieldSymbol}
     * @param withSelfPrefix {@link Boolean}
     * @return {@link CompletionItem} generated completion item
     */
    public static CompletionItem build(ObjectFieldSymbol objectFieldSymbol, boolean withSelfPrefix) {
        if (withSelfPrefix) {
            String label = "self." + objectFieldSymbol.getName().get();

            CompletionItem item = new CompletionItem();
            item.setLabel(label);
            item.setInsertText(label);
            item.setKind(CompletionItemKind.Field);
            return item;
        } else {
            return build(objectFieldSymbol);
        }
    }
}
