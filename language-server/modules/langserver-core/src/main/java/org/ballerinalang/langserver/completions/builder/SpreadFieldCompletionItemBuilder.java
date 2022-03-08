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

import io.ballerina.compiler.api.symbols.Symbol;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;

/**
 * This class is being used to build spread field completion item.
 *
 * @since 2.0.0
 */
public class SpreadFieldCompletionItemBuilder {

    /**
     * Build the constant {@link CompletionItem}.
     *
     * @param symbol  {@link Symbol}
     * @param context Completion context
     * @return {@link CompletionItem} generated completion item
     */
    public static CompletionItem build(Symbol symbol, String typeName, BallerinaCompletionContext context) {
        String symbolName = symbol.getName().orElseThrow();
        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel("..." + symbolName);
        completionItem.setKind(CompletionItemKind.Variable);
        completionItem.setDetail(typeName);
        completionItem.setFilterText(symbolName);
        String insertText = "..." + symbolName;
        completionItem.setInsertText(insertText);
        return completionItem;
    }
}
