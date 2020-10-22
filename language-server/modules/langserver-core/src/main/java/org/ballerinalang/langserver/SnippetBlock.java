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
package org.ballerinalang.langserver;

import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent an insert text block having both plain text and snippet format strings.
 * 
 * @since 0.982.0
 */
public class SnippetBlock {

    private String label = "";
    private String detail = "";
    private final String snippet;
    private final Kind kind;
    private final Pair<String, String>[] imports;

    public SnippetBlock(String snippet, Kind kind) {
        this.snippet = snippet;
        this.kind = kind;
        this.imports = null;
    }

    public SnippetBlock(String label, String snippet, String detail, Kind kind) {
        this.label = label;
        this.snippet = snippet;
        this.detail = detail;
        this.kind = kind;
        this.imports = null;
    }

    public SnippetBlock(String label, String snippet, String detail, Kind kind,
                        Pair<String, String>... importsByOrgAndAlias) {
        this.label = label;
        this.snippet = snippet;
        this.detail = detail;
        this.kind = kind;
        this.imports = importsByOrgAndAlias;
    }

    /**
     * Create a given completionItem's insert text.
     *
     * @param ctx   LS Context
     * @return modified Completion Item
     */
    public CompletionItem build(LSContext ctx) {
        CompletionItem completionItem = new CompletionItem();
        completionItem.setInsertText(this.snippet);
        List<BLangImportPackage> currentDocImports = ctx.get(DocumentServiceKeys.CURRENT_DOC_IMPORTS_KEY);
        if (imports != null) {
            List<TextEdit> importTextEdits = new ArrayList<>();
            for (Pair<String, String> pair : imports) {
                boolean pkgAlreadyImported = currentDocImports.stream()
                        .anyMatch(importPkg -> importPkg.orgName.value.equals(pair.getLeft())
                                && importPkg.alias.value.equals(pair.getRight()));
                if (!pkgAlreadyImported) {
                    importTextEdits.addAll(CommonUtil.getAutoImportTextEdits(pair.getLeft(), pair.getRight(), ctx));
                }
            }
            completionItem.setAdditionalTextEdits(importTextEdits);
        }
        if (!label.isEmpty()) {
            completionItem.setLabel(label);
        }
        if (!detail.isEmpty()) {
            completionItem.setDetail(detail);
        }
        completionItem.setKind(getCompletionItemKind());
        return completionItem;
    }

    /**
     * Get the Snippet String.
     *
     * @return {@link String}
     */
    public String getString() {
        return this.snippet;
    }

    /**
     * Returns LSP Snippet Type.
     *
     * @return {@link CompletionItemKind} LSP Snippet Type
     */
    private CompletionItemKind getCompletionItemKind() {
        switch (kind) {
            case KEYWORD:
                return CompletionItemKind.Keyword;
            case TYPE:
                return CompletionItemKind.Unit;
            case SNIPPET:
            case STATEMENT:
            default:
                return CompletionItemKind.Snippet;
        }
    }

    public String getLabel() {
        return label;
    }

    public Kind kind() {
        return kind;
    }

    /**
     * Represents Snippet Types in B7a LS.
     */
    public enum Kind {
        KEYWORD, SNIPPET, STATEMENT, TYPE;
    }
}
