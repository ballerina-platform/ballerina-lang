/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link ServiceDeclarationNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class ServiceDeclarationNodeContext extends AbstractCompletionProvider<ServiceDeclarationNode> {

    public ServiceDeclarationNodeContext() {
        super(ServiceDeclarationNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, ServiceDeclarationNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        Token onKeyword = node.onKeyword();
        Position cursor = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        if (!onKeyword.isMissing() && cursor.getCharacter() > onKeyword.lineRange().endLine().offset()) {
            /*
            Covers the following case
            (1) service testService on <cursor>
            (2) service testService on l<cursor>
             */
            List<Symbol> visibleSymbols = context.get(CommonKeys.VISIBLE_SYMBOLS_KEY);
            List<Symbol> listeners = visibleSymbols.stream()
                    .filter(symbol -> symbol instanceof VariableSymbol
                            && ((VariableSymbol) symbol).qualifiers().contains(Qualifier.LISTENER))
                    .collect(Collectors.toList());
            completionItems.addAll(this.getCompletionItemList(listeners, context));
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_NEW.get()));
        }

        return completionItems;
    }
}
