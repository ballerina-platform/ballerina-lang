/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.XMLNamePatternChainingNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link XMLNamePatternChainingNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class XMLNamePatternChainingNodeContext extends AbstractCompletionProvider<XMLNamePatternChainingNode> {

    public XMLNamePatternChainingNodeContext() {
        super(XMLNamePatternChainingNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, XMLNamePatternChainingNode node) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        List<Symbol> xmlNs = visibleSymbols.stream().filter(symbol -> symbol.kind() == SymbolKind.XMLNS)
                .collect(Collectors.toList());
        List<LSCompletionItem> completionItems = this.getCompletionItemList(xmlNs, context);
        this.sort(context, node, completionItems);

        return completionItems;
    }
}
