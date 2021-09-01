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

import io.ballerina.compiler.api.symbols.Symbol;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;

/**
 * This class is used to build named arg completion item.
 */
public class NamedArgCompletionItemBuilder {

    private NamedArgCompletionItemBuilder() {

    }

    /**
     * Creates and returns a completion item.
     *
     * @param symbol symbol.
     * @param label           label.
     * @param insertText      text to be inserted.
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(Symbol symbol, String label,
                                       String insertText) {
        CompletionItem item = new CompletionItem();
        item.setLabel(label);
        item.setInsertText(insertText);
        item.setDetail(insertText);
        setMeta(item, symbol);
        return item;
    }

    private static void setMeta(CompletionItem item, Symbol symbol) {
        item.setKind(CompletionItemKind.Variable);
//        if (varSymbol == null) {
//            return;
//        }
//        if (varSymbol.documentation().isPresent() && varSymbol.documentation().get().description().isPresent()) {
//            item.setDocumentation(parameterSymbol.documentation().get().description().get());
//        }
    }
}
