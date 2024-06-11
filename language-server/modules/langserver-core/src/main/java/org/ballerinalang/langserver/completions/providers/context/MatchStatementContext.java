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

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.Node;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Generic Completion provider for match statement related contexts such as match node and pattern clauses.
 *
 * @param <T> Match statement node type
 * @since 2.0.0
 */
public abstract class MatchStatementContext<T extends Node> extends AbstractCompletionProvider<T> {

    public MatchStatementContext(Class<T> attachmentPoint) {
        super(attachmentPoint);
    }

    protected List<LSCompletionItem> getPatternClauseCompletions(BallerinaCompletionContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        // Error keyword suggestion is covered by the module completion items
        List<Snippet> snippets = Arrays.asList(Snippet.KW_TRUE, Snippet.KW_FALSE, Snippet.KW_VAR);

        snippets.forEach(snippet -> completionItems.add(new SnippetCompletionItem(context, snippet.get())));
        List<Symbol> filteredConstants = visibleSymbols.stream()
                .filter(this.constantFilter())
                .toList();

        completionItems.addAll(this.getCompletionItemList(filteredConstants, context));
        completionItems.addAll(this.getModuleCompletionItems(context));

        return completionItems;
    }

    protected Predicate<Symbol> constantFilter() {
        // also, should include the enum members as well. Since currently both are same, this is fine
        return symbol -> symbol.kind() == SymbolKind.CONSTANT || symbol.kind() == SymbolKind.ENUM_MEMBER;
    }
}
