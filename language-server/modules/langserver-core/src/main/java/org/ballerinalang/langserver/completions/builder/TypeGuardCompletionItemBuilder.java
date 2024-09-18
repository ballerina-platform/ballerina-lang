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
 * This class is being used to build typeguard completion item.
 *
 * @since 2.0
 */
public final class TypeGuardCompletionItemBuilder {

    private TypeGuardCompletionItemBuilder() {
    }

    /**
     * Creates and return a snippet type completion item.
     *
     * @param snippet             text to be inserted
     * @param label               label of the completion item
     * @param detail              detail of the completion item
     * @param additionalTextEdits textedits consisting the range to be replaced by the completion item.
     * @return {@link CompletionItem} generated completion item
     */
    public static CompletionItem build(String snippet, String label, String detail,
                                       List<TextEdit> additionalTextEdits) {
        CompletionItem completionItem = new CompletionItem();
        String insertText = snippet;
        completionItem.setInsertText(insertText);
        completionItem.setLabel(label);
        completionItem.setDetail(detail);
        completionItem.setAdditionalTextEdits(additionalTextEdits);
        completionItem.setKind(CompletionItemKind.Snippet);
        return completionItem;
    }

}
