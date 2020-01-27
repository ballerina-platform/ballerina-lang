/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.providers.contextproviders;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.filters.DelimiterBasedContentFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Parser rule based variable definition statement context resolver.
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.LSCompletionProvider")
public class GlobalVarDefContextProvider extends AbstractCompletionProvider {
    public GlobalVarDefContextProvider() {
        this.attachmentPoints.add(BallerinaParser.GlobalVariableDefinitionContext.class);
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext ctx) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<CommonToken> lhsDefaultTokens = ctx.get(CompletionKeys.LHS_TOKENS_KEY).stream()
                .filter(commonToken -> commonToken.getChannel() == Token.DEFAULT_CHANNEL)
                .collect(Collectors.toList());
        Optional<CommonToken> assignToken = lhsDefaultTokens.stream()
                .filter(commonToken -> commonToken.getType() == BallerinaParser.ASSIGN)
                .findAny();
        List<SymbolInfo> visibleSymbols = new ArrayList<>(ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY));

        if (lhsDefaultTokens.isEmpty()) {
            return completionItems;
        }

        int firstToken = lhsDefaultTokens.get(0).getType();
        int invocationOrDelimiterTokenType = ctx.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
        Optional<CommonToken> listenerKWToken = lhsDefaultTokens.stream()
                .filter(commonToken -> commonToken.getType() == BallerinaParser.LISTENER)
                .findAny();

        if (lhsDefaultTokens.size() <= 2) {
            if (listenerKWToken.isPresent()) {
                completionItems.addAll(this.getListenersAndPackages(ctx));
            } else if (firstToken == BallerinaParser.FINAL) {
                completionItems.addAll(this.getBasicTypes(visibleSymbols));
                completionItems.addAll(this.getPackagesCompletionItems(ctx));
            } else if (BallerinaParser.COLON == invocationOrDelimiterTokenType) {
                Either<List<CompletionItem>, List<SymbolInfo>> pkgContent = SymbolFilters
                        .get(DelimiterBasedContentFilter.class).filterItems(ctx);
                completionItems.addAll(this.getCompletionItemList(pkgContent, ctx));
            } else {
                completionItems.addAll(this.getAllTopLevelItems(ctx));
                completionItems.addAll(this.getPackagesCompletionItems(ctx));
            }
        } else if (invocationOrDelimiterTokenType > -1) {
            if (listenerKWToken.isPresent()) {
                int pkgDelimiterIndex = lhsDefaultTokens.stream()
                        .map(CommonToken::getType)
                        .collect(Collectors.toList())
                        .indexOf(BallerinaParser.COLON);
                String pkgAlias = lhsDefaultTokens.get(pkgDelimiterIndex - 1).getText();
                completionItems.addAll(this.getListenersFromPackage(ctx, pkgAlias));
            } else {
                Either<List<CompletionItem>, List<SymbolInfo>> filteredList =
                        SymbolFilters.get(DelimiterBasedContentFilter.class).filterItems(ctx);
                completionItems.addAll(this.getCompletionItemList(filteredList, ctx));  
            }
            // TODO: usage of index
        } else if (assignToken.isPresent()) {
            completionItems.addAll(this.getVarDefExpressionCompletions(ctx, true));
        } else {
            completionItems.addAll(this.getAllTopLevelItems(ctx));
            completionItems.addAll(this.getPackagesCompletionItems(ctx));
        }
        return completionItems;
    }
    
    private List<CompletionItem> getListenersAndPackages(LSContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>(this.getPackagesCompletionItems(context));
        List<SymbolInfo> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        List<SymbolInfo> listeners = visibleSymbols.stream()
                .filter(symbolInfo -> CommonUtil.isListenerObject(symbolInfo.getScopeEntry().symbol))
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(listeners, context));
        return completionItems;
    }
    
    private List<CompletionItem> getListenersFromPackage(LSContext context, String alias) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<SymbolInfo> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        Optional<SymbolInfo> packageSymbolInfo = visibleSymbols.stream()
                .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol instanceof BPackageSymbol
                        && symbolInfo.getSymbolName().equals(alias))
                .findAny();
        
        if (!packageSymbolInfo.isPresent()
                || !(packageSymbolInfo.get().getScopeEntry().symbol instanceof BPackageSymbol)) {
            return completionItems;
        }
        List<SymbolInfo> listeners = new ArrayList<>();
        ((BPackageSymbol) packageSymbolInfo.get().getScopeEntry().symbol).scope.entries.forEach((name, scopeEntry) -> {
            if (CommonUtil.isListenerObject(scopeEntry.symbol)) {
                listeners.add(new SymbolInfo(scopeEntry.symbol.getName().getValue(), scopeEntry));
            }
        });
        completionItems.addAll(this.getCompletionItemList(listeners, context));
        return completionItems;
    }
    
    private List<CompletionItem> getAllTopLevelItems(LSContext ctx) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<SymbolInfo> visibleSymbols = new ArrayList<>(ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        completionItems.addAll(this.addTopLevelItems(ctx));
        completionItems.addAll(this.getBasicTypes(visibleSymbols));
        
        return completionItems;
    }
}
