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
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.Names;

/**
 * This class is being used to build BType completion item.
 *
 * @since 0.983.0
 */
public class BTypeCompletionItemBuilder {
    private BTypeCompletionItemBuilder() {
    }

    /**
     * Creates and returns a completion item.
     *
     * @param bSymbol BSymbol or null
     * @param label   label
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(BTypeSymbol bSymbol, String label) {
        CompletionItem item = new CompletionItem();
        item.setLabel(label);
        String[] delimiterSeparatedTokens = (label).split("\\.");
        item.setInsertText(delimiterSeparatedTokens[delimiterSeparatedTokens.length - 1]);
        item.setDetail(ItemResolverConstants.B_TYPE);
        setMeta(item, bSymbol);
        return item;
    }

    private static void setMeta(CompletionItem item, BTypeSymbol bSymbol) {
        if (bSymbol == null) {
            item.setKind(CompletionItemKind.Class);
            return;
        }
        //Or, else
        if (bSymbol instanceof BPackageSymbol) {
            // package
            item.setKind(CompletionItemKind.Module);
        } else if (bSymbol.kind != null) {
            // class / objects
            item.setKind(CompletionItemKind.Class);
        } else if (bSymbol.type instanceof BFiniteType || bSymbol.type instanceof BUnionType) {
            // enums
            item.setKind(CompletionItemKind.Enum);
        } else if (bSymbol.pkgID.orgName.equals(Names.BUILTIN_ORG) &&
                bSymbol.pkgID.name.equals(Names.BUILTIN_PACKAGE)) {
            // keyword
            item.setKind(CompletionItemKind.Keyword);
        } else {
            // default
            item.setKind(CompletionItemKind.Unit);
        }
        if (bSymbol.markdownDocumentation != null) {
            item.setDocumentation(bSymbol.markdownDocumentation.description);
        }
    }
}
