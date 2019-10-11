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

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.Locale;

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
    public static CompletionItem build(BSymbol bSymbol, String label) {
        CompletionItem item = new CompletionItem();
        item.setLabel(label);
        String[] delimiterSeparatedTokens = (label).split("\\.");
        item.setInsertText(delimiterSeparatedTokens[delimiterSeparatedTokens.length - 1]);
        setMeta(item, bSymbol);
        return item;
    }

    private static void setMeta(CompletionItem item, BSymbol bSymbol) {
        if (bSymbol == null) {
            item.setKind(CompletionItemKind.Class);
            return;
        }
        //Or, else
        if (bSymbol instanceof BPackageSymbol) {
            // package
            item.setKind(CompletionItemKind.Module);
        } else if (bSymbol.type instanceof BFiniteType) {
            // Finite types
            item.setKind(CompletionItemKind.TypeParameter);
        } else if (bSymbol.type instanceof BUnionType) {
            // Union types
            ArrayList<BType> memberTypes = new ArrayList(((BUnionType) bSymbol.type).getMemberTypes());
            boolean allMatch = memberTypes.stream().allMatch(bType -> bType.tag == memberTypes.get(0).tag);
            if (allMatch) {
                switch (memberTypes.get(0).tag) {
                    case TypeTags.ERROR:
                        item.setKind(CompletionItemKind.Event);
                        break;
                    case TypeTags.RECORD:
                        item.setKind(CompletionItemKind.Struct);
                        break;
                    case TypeTags.OBJECT:
                        item.setKind(CompletionItemKind.Interface);
                        break;
                    default:
                        break;
                }
            } else {
                item.setKind(CompletionItemKind.Enum);
            }
        } else if (bSymbol instanceof BRecordTypeSymbol) {
            item.setKind(CompletionItemKind.Struct);
        } else if (bSymbol instanceof BObjectTypeSymbol) {
            item.setKind(CompletionItemKind.Interface);
        }  else if (bSymbol instanceof BErrorTypeSymbol) {
            item.setKind(CompletionItemKind.Event);
        } else if (bSymbol.kind != null) {
            // class / objects
            item.setKind(CompletionItemKind.Class);
        } else {
            // default
            item.setKind(CompletionItemKind.Unit);
        }
        if (bSymbol.markdownDocumentation != null) {
            item.setDocumentation(bSymbol.markdownDocumentation.description);
        }
        // set sub bType
        String name = bSymbol.type.getKind().name();
        String detail = name.substring(0, 1).toUpperCase(Locale.ENGLISH)
                + name.substring(1).toLowerCase(Locale.ENGLISH);
        item.setDetail(detail);
    }
}
