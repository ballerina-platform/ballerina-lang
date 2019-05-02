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
package org.ballerinalang.langserver.completions.providers.subproviders.parsercontext;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.filters.DelimiterBasedContentFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Completion item provider for service definition context.
 */
public class ParserRuleServiceDefinitionCompletionProvider extends LSCompletionProvider {
    @Override
    public List<CompletionItem> getCompletions(LSContext ctx) {
        List<CompletionItem> completionItems = new ArrayList<>();
        TokenStream tokenStream = ctx.get(CompletionKeys.TOKEN_STREAM_KEY);
        Stack<Token> poppedTokens = ctx.get(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY);
        int startIndex = poppedTokens.peek().getTokenIndex() + 1;
        String stopToken = "";

        // Backtrack the tokens from the head of the popped tokens in order determine the cursor position
        tokenScanner:
        while (true) {
            Optional<Token> token = CommonUtil.getPreviousDefaultToken(tokenStream, startIndex);
            if (!token.isPresent()) {
                break;
            }
            String tokenString = token.get().getText();
            switch (tokenString) {
                case ItemResolverConstants.SERVICE:
                case ItemResolverConstants.ON:
                case ItemResolverConstants.NEW:
                case UtilSymbolKeys.PKG_DELIMITER_KEYWORD:
                    stopToken = tokenString;
                    break tokenScanner;
                default:
                    break;
            }

            startIndex = token.get().getTokenIndex();
        }

        switch (stopToken) {
            case ItemResolverConstants.ON : {
                // suggest all the visible, defined listeners
                List<SymbolInfo> filtered = this.filterListenerVariables(ctx.get(CompletionKeys.VISIBLE_SYMBOLS_KEY));
                completionItems.addAll(this.getCompletionItemList(filtered, ctx));
                completionItems.add(Snippet.KW_NEW.get().build(ctx));
                break;
            }
            case ItemResolverConstants.NEW: {
                List<SymbolInfo> filteredSymbols =
                        this.filterListenerTypes(ctx.get(CompletionKeys.VISIBLE_SYMBOLS_KEY));
                completionItems.addAll(this.getCompletionItemList(filteredSymbols, ctx));
                completionItems.addAll(this.getPackagesCompletionItems(ctx));
                break;
            }
            case UtilSymbolKeys.PKG_DELIMITER_KEYWORD: {
                Either<List<CompletionItem>, List<SymbolInfo>> eitherList = SymbolFilters
                        .get(DelimiterBasedContentFilter.class)
                        .filterItems(ctx);
                completionItems.addAll(this.getCompletionItemList(eitherList, ctx));
                break;
            }
            default: {
                // Fill the on keyword completion item
                completionItems.add(Snippet.KW_ON.get().build(ctx));
                break;
            }
        }
        
        return completionItems;
    }

    private List<SymbolInfo> filterListenerVariables(List<SymbolInfo> symbolInfos) {
        return symbolInfos.stream()
                .filter(symbolInfo -> {
                    BSymbol symbol = symbolInfo.getScopeEntry().symbol;
                    return symbol instanceof BVarSymbol && CommonUtil.isListenerObject(symbol.type.tsymbol);
                })
                .collect(Collectors.toList());
    }
    
    private List<SymbolInfo> filterListenerTypes(List<SymbolInfo> symbolInfos) {
        return symbolInfos.stream()
                .filter(symbolInfo -> CommonUtil.isListenerObject(symbolInfo.getScopeEntry().symbol))
                .collect(Collectors.toList());
    }
}
