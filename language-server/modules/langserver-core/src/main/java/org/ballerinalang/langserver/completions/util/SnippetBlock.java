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
package org.ballerinalang.langserver.completions.util;

import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.ModuleUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.completions.builder.CompletionItemBuilder;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represent an insert text block having both plain text and snippet format strings.
 *
 * @since 0.982.0
 */
public class SnippetBlock extends CompletionItemBuilder {

    private final String label;
    private final String detail;
    private final String snippet;
    private final Kind kind;
    private final String filterText;
    private final Pair<String, String>[] imports;
    private String id;

    public SnippetBlock(String label, String filterText, String snippet, String detail, Kind kind) {
        this.label = label;
        this.filterText = filterText;
        this.snippet = snippet;
        this.detail = detail;
        this.kind = kind;
        this.imports = null;
    }

    public SnippetBlock(String label, String filterText, String snippet, String detail, Kind kind,
                        Pair<String, String>... importsByOrgAndAlias) {
        this.label = label;
        this.filterText = filterText;
        this.snippet = snippet;
        this.detail = detail;
        this.kind = kind;
        this.imports = importsByOrgAndAlias;
    }

    /**
     * Create a given completionItem's insert text.
     *
     * @param ctx LS Context
     * @return modified Completion Item
     */
    public CompletionItem build(BallerinaCompletionContext ctx) {
        // Each time we build a new completion item to make the snippet aware of the latest sources (alias changes, etc)
        CompletionItem completionItem = new CompletionItem();
        String insertText = this.snippet;
        if (imports != null) {
            List<TextEdit> importTextEdits = new ArrayList<>();
            for (Pair<String, String> pair : imports) {
                Optional<ImportDeclarationNode> matchedImport = ModuleUtil.matchingImportedModule(ctx, 
                        pair.getLeft(), pair.getRight());
                if (matchedImport.isEmpty()) {
                    importTextEdits.addAll(CommonUtil.getAutoImportTextEdits(pair.getLeft(), pair.getRight(), ctx));
                } else if (matchedImport.get().prefix().isPresent()) {
                    insertText = this.snippet.replace(pair.getRight() + ":",
                            matchedImport.get().prefix().get().prefix().text() + ":");
                }
            }
            completionItem.setAdditionalTextEdits(importTextEdits);
        }
        completionItem.setInsertText(insertText);
        if (!label.isEmpty()) {
            completionItem.setLabel(label);
        }
        if (!detail.isEmpty()) {
            completionItem.setDetail(detail);
        }
        if (!filterText.isEmpty()) {
            completionItem.setFilterText(filterText);
        }
        completionItem.setKind(this.getKind(this));

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

    public String getLabel() {
        return label;
    }

    public Kind kind() {
        return kind;
    }

    public Optional<String> getId() {
        return Optional.ofNullable(this.id);
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Represents Snippet Types in B7a LS.
     */
    public enum Kind {
        KEYWORD, SNIPPET, STATEMENT, TYPE;
    }
}
