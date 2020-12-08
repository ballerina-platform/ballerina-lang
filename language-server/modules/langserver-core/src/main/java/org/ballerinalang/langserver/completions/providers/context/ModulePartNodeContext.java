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
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.providers.context.util.ModulePartNodeContextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link ModulePartNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class ModulePartNodeContext extends AbstractCompletionProvider<ModulePartNode> {

    public ModulePartNodeContext() {
        super(ModulePartNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(CompletionContext context, ModulePartNode node) {
        String tokenValueAtCursor = this.getTokenValueAtCursor(context);
        if (tokenValueAtCursor.equals(SyntaxKind.SERVICE_KEYWORD.stringValue())) {
            return this.getObjectTypeCompletions(context);
        }

        List<LSCompletionItem> completionItems = new ArrayList<>();
        completionItems.addAll(ModulePartNodeContextUtil.getTopLevelItems(context));
        completionItems.addAll(this.getTypeItems(context));
        completionItems.addAll(this.getModuleCompletionItems(context));
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    public void sort(CompletionContext context, ModulePartNode node, List<LSCompletionItem> items, Object... metaData) {
        ModulePartNodeContextUtil.sort(items);
    }

    private String getTokenValueAtCursor(CompletionContext context) {
        Token tokenAtCursor = context.getTokenAtCursor();
        String tokenVal;
        if (tokenAtCursor.kind() == SyntaxKind.EOF_TOKEN) {
            List<String> tokensFromMinutiae = getTokensFromMinutiae(tokenAtCursor.leadingMinutiae());
            tokenVal = !tokensFromMinutiae.isEmpty() ? tokensFromMinutiae.get(tokensFromMinutiae.size() - 1) : "";
        } else {
            tokenVal = tokenAtCursor.text();
        }

        return tokenVal;
    }

    private List<String> getTokensFromMinutiae(MinutiaeList minutiaeList) {
        List<String> tokens = new ArrayList<>();
        minutiaeList.forEach(minutiae -> {
            if (minutiae.kind() != SyntaxKind.WHITESPACE_MINUTIAE
                    && minutiae.kind() != SyntaxKind.END_OF_LINE_MINUTIAE) {
                tokens.add(minutiae.text());
            }
        });

        return tokens;
    }

    private List<LSCompletionItem> getObjectTypeCompletions(CompletionContext context) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        List<Symbol> filtered = visibleSymbols.stream().filter(symbol -> {
            if (symbol.kind() != SymbolKind.TYPE) {
                return false;
            }
            Optional<? extends TypeSymbol> objSymbol = SymbolUtil.getTypeDescriptor(symbol);
            return objSymbol.isPresent() && CommonUtil.getRawType(objSymbol.get()).typeKind() == TypeDescKind.OBJECT;
        }).collect(Collectors.toList());

        return this.getCompletionItemList(filtered, context);
    }
}
