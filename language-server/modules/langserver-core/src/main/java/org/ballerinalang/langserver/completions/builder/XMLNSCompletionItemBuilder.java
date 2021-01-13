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

import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.XMLNamespaceSymbol;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.MarkupContent;

import java.util.Optional;

/**
 * Completion item builder for the {@link XMLNamespaceSymbol}.
 *
 * @since 1.0
 */
public class XMLNSCompletionItemBuilder {
    private XMLNSCompletionItemBuilder() {
    }

    /**
     * Build the constant {@link CompletionItem}.
     *
     * @param namespaceSymbol constant symbol
     * @return {@link CompletionItem} generated completion item
     */
    public static CompletionItem build(XMLNamespaceSymbol namespaceSymbol) {
        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(namespaceSymbol.name());
        completionItem.setInsertText(namespaceSymbol.name());
        completionItem.setDetail(namespaceSymbol.namespaceUri());
        completionItem.setDocumentation(getDocumentation(namespaceSymbol));
        completionItem.setKind(CompletionItemKind.Unit);

        return completionItem;
    }

    private static MarkupContent getDocumentation(XMLNamespaceSymbol namespaceSymbol) {
        MarkupContent docMarkupContent = new MarkupContent();
        Optional<Documentation> docAttachment = namespaceSymbol.docAttachment();
        String description = docAttachment.isEmpty() || docAttachment.get().description().isEmpty() ? ""
                : docAttachment.get().description().get();
        docMarkupContent.setValue(description);
        docMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);

        return docMarkupContent;
    }
}
