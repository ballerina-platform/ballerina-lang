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

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.TextEdit;

import java.util.List;

/**
 * Builder used to build foreach completion item.
 *
 * @since 2.0.0
 */
public final class ForeachCompletionItemBuilder {

    private ForeachCompletionItemBuilder() {
    }

    /**
     * Creates and returns a snippet type completion item.
     *
     * @param snippet             Text to be inserted
     * @param label               Label of the completion item
     * @param detail              Detail of the completion item
     * @param documentation       Documentation of the completion item
     * @param additionalTextEdits Textedits consisting the range to be replaced by the completion item.
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(String snippet, String label, String detail,
                                       String documentation, List<TextEdit> additionalTextEdits) {
        CompletionItem completionItem = new CompletionItem();
        String insertText = snippet;
        completionItem.setInsertText(insertText);
        completionItem.setLabel(label);
        completionItem.setDetail(detail);
        completionItem.setDocumentation(documentation);
        completionItem.setAdditionalTextEdits(additionalTextEdits);
        completionItem.setKind(CompletionItemKind.Snippet);
        return completionItem;
    }
}
