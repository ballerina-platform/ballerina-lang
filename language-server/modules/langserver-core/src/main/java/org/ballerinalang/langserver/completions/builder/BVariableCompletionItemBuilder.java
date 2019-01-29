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
package org.ballerinalang.langserver.completions.builder;

import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.model.types.TypeKind;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.util.Flags;

/**
 * This class is being used to build variable type completion item.
 *
 * @since 0.983.0
 */
public final class BVariableCompletionItemBuilder {
    private BVariableCompletionItemBuilder() {
    }

    /**
     * Creates and returns a completion item.
     *
     * @param bSymbol BSymbol
     * @param label   label
     * @param type    variable type
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(BVarSymbol bSymbol, String label, String type) {
        CompletionItem item = new CompletionItem();
        item.setLabel(label);
        String[] delimiterSeparatedTokens = (label).split("\\.");
        item.setInsertText(delimiterSeparatedTokens[delimiterSeparatedTokens.length - 1]);
        item.setDetail((type.equals("")) ? ItemResolverConstants.NONE : type);
        setMeta(item, bSymbol);
        return item;
    }

    private static void setMeta(CompletionItem item, BVarSymbol bSymbol) {
        if (bSymbol == null) {
            item.setKind(CompletionItemKind.Variable);
            return;
        }
        //Or, else
        if ((bSymbol.flags & Flags.FINAL) == Flags.FINAL) {
            if (bSymbol.type.tsymbol != null && TypeKind.STRING.typeName().equals(bSymbol.type.tsymbol.name.value)) {
                // string final
                item.setKind(CompletionItemKind.Text);
            } else {
                // non-string final
                item.setKind(CompletionItemKind.Unit);
            }
        } else {
            // variables
            item.setKind(CompletionItemKind.Variable);
        }
        if (bSymbol.markdownDocumentation != null) {
            item.setDocumentation(bSymbol.markdownDocumentation.description);
        }
    }
}
