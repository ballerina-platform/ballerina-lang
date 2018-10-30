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

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;

/**
 * Represent an insert text block having both plain text and snippet format strings.
 * 
 * @since 0.982.0
 */
public class SnippetBlock {

    private String label = "";
    private String detail = "";
    private String snippet;
    private SnippetType snippetType;

    public SnippetBlock(String label, String snippet, String detail, SnippetType snippetType) {
        this.label = label;
        this.snippet = snippet;
        this.detail = detail;
        this.snippetType = snippetType;
    }

    public SnippetBlock(String snippet,  SnippetType snippetType) {
        this.snippet = snippet;
        this.snippetType = snippetType;
    }

    /**
     * Create a given completionItem's insert text.
     *
     * @param completionItem     CompletionItem to modify
     * @param isSnippetSupported Whether snippet is expected or plain text expected
     * @return modified Completion Item
     */
    public CompletionItem build(CompletionItem completionItem, boolean isSnippetSupported) {
        if (isSnippetSupported) {
            completionItem.setInsertText(this.snippet);
            completionItem.setInsertTextFormat(InsertTextFormat.Snippet);
        } else {
            completionItem.setInsertText(getPlainTextSnippet());
            completionItem.setInsertTextFormat(InsertTextFormat.PlainText);
        }
        if (!label.isEmpty()) {
            completionItem.setLabel(label);
        }
        if (!detail.isEmpty()) {
            completionItem.setDetail(detail);
        }
        completionItem.setKind(getKind());
        return completionItem;
    }

    /**
     * Get the Snippet String.
     *
     * @param isSnippet         Whether the snippet or plain text expected
     * @return {@link String}
     */
    public String getString(boolean isSnippet) {
        return isSnippet ? this.snippet : getPlainTextSnippet();
    }
    
    // Private Methods
    
    private String getPlainTextSnippet() {
        return this.snippet
                .replaceAll("(\\$\\{\\d:)([a-zA-Z]*:*[a-zA-Z]*)(\\})", "$2")
                .replaceAll("(\\$\\{\\d\\})", "");
    }

    /**
     * Returns LSP Snippet Type.
     *
     * @return {@link CompletionItemKind} LSP Snippet Type
     */
    private CompletionItemKind getKind() {
        switch (snippetType) {
            case KEYWORD:
                return CompletionItemKind.Keyword;
            case SNIPPET:
                return CompletionItemKind.Snippet;
            case STATEMENT:
                return CompletionItemKind.Unit;
            default:
                return CompletionItemKind.Snippet;
        }
    }

    public String getLabel() {
        return label;
    }

    /**
     * Represents Snippet Types in B7a LS.
     */
    public enum SnippetType {
        KEYWORD, SNIPPET, STATEMENT;
    }
}
