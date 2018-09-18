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
import org.eclipse.lsp4j.InsertTextFormat;

/**
 * Represent an insert text block having both plain text and snippet format strings.
 * 
 * @since 0.982.0
 */
public class SnippetBlock {

    public SnippetBlock(String snippet) {
        this.snippet = snippet;
    }

    private String snippet;
    
    /**
     * Populate a given completionItem's insert text.
     *
     * @param completionItem    CompletionItem to modify
     * @param isSnippet         Whether snippet is expected or plain text expected
     */
    public void populateCompletionItem(CompletionItem completionItem, boolean isSnippet) {
        if (isSnippet) {
            completionItem.setInsertText(this.snippet);
            completionItem.setInsertTextFormat(InsertTextFormat.Snippet);
        } else {
            completionItem.setInsertText(getPlainTextSnippet());
            completionItem.setInsertTextFormat(InsertTextFormat.PlainText);
        }
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
        return this.snippet.replaceAll("(\\$\\{\\d:)([a-zA-Z]*)(\\})", "$2").replaceAll("(\\$\\{\\d\\})", "");
    }   
}
