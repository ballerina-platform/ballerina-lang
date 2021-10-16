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
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.WhileStatementNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link WhileStatementNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class WhileStatementNodeContext extends AbstractCompletionProvider<WhileStatementNode> {

    public WhileStatementNodeContext() {
        super(WhileStatementNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, WhileStatementNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        List<Symbol> filteredList = visibleSymbols.stream()
                .filter(symbol -> symbol instanceof VariableSymbol || symbol.kind() == SymbolKind.FUNCTION)
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(filteredList, context));
        completionItems.addAll(this.getModuleCompletionItems(context));
        List<Snippet> snippets = Arrays.asList(Snippet.KW_TRUE, Snippet.KW_FALSE);
        snippets.forEach(snippet -> completionItems.add(new SnippetCompletionItem(context, snippet.get())));
        this.sort(context, node, completionItems);
        
        return completionItems;
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, WhileStatementNode node) {
        int cursor = context.getCursorPositionInTree();
        BlockStatementNode whileBody = node.whileBody();
        Token whileKeyword = node.whileKeyword();
        
        /*
        Validates the following
        eg: 1) while <cursor>
            2) while <cursor>{
         */
        return !whileKeyword.isMissing() && cursor >= whileKeyword.textRange().endOffset() + 1
                && ((whileBody.isMissing() || cursor < whileBody.openBraceToken().textRange().endOffset()));
    }
}
