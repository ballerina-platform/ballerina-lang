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
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.filters.DelimiterBasedContentFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.ballerinalang.langserver.sourceprune.SourcePruneKeys;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
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
    public List<LSCompletionItem> getCompletions(LSContext ctx) {
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
        List<CommonToken> lhsDefaultTokens = ctx.get(SourcePruneKeys.LHS_TOKENS_KEY).stream()
                .filter(commonToken -> commonToken.getChannel() == Token.DEFAULT_CHANNEL)
                .collect(Collectors.toList());
        Optional<CommonToken> assignToken = lhsDefaultTokens.stream()
                .filter(commonToken -> commonToken.getType() == BallerinaParser.ASSIGN)
                .findAny();
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY));

        if (lhsDefaultTokens.isEmpty()) {
            return completionItems;
        }

        int firstToken = lhsDefaultTokens.get(0).getType();
        int invocationOrDelimiterTokenType = ctx.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);

        // Consider the Listener keyword and suggest the listener type completions
        if (this.suggestListeners(lhsDefaultTokens)) {
            if (invocationOrDelimiterTokenType > -1) {
                int pkgDelimiterIndex = lhsDefaultTokens.stream()
                        .map(CommonToken::getType)
                        .collect(Collectors.toList())
                        .indexOf(BallerinaParser.COLON);
                String pkgAlias = lhsDefaultTokens.get(pkgDelimiterIndex - 1).getText();
                completionItems.addAll(this.getListenersFromPackage(ctx, pkgAlias));

                return completionItems;
            }
            completionItems.addAll(this.getListenersAndPackagesItems(ctx));

            return completionItems;
        }

        if (lhsDefaultTokens.size() <= 2) {
            if (firstToken == BallerinaParser.FINAL) {
                completionItems.addAll(this.getBasicTypesItems(ctx, visibleSymbols));
                completionItems.addAll(this.getPackagesCompletionItems(ctx));
            } else if (BallerinaParser.COLON == invocationOrDelimiterTokenType) {
                Either<List<LSCompletionItem>, List<Scope.ScopeEntry>> pkgContent = SymbolFilters
                        .get(DelimiterBasedContentFilter.class).filterItems(ctx);
                completionItems.addAll(this.getCompletionItemList(pkgContent, ctx));
            } else {
                completionItems.addAll(this.getAllTopLevelItems(ctx));
                completionItems.addAll(this.getPackagesCompletionItems(ctx));
            }
        } else if (invocationOrDelimiterTokenType > -1) {
            Either<List<LSCompletionItem>, List<Scope.ScopeEntry>> filteredList =
                    SymbolFilters.get(DelimiterBasedContentFilter.class).filterItems(ctx);
            completionItems.addAll(this.getCompletionItemList(filteredList, ctx));
        } else if (assignToken.isPresent()) {
            completionItems.addAll(this.getVarDefExpressionCompletions(ctx, true));
        } else {
            completionItems.addAll(this.getAllTopLevelItems(ctx));
            completionItems.addAll(this.getPackagesCompletionItems(ctx));
        }
        return completionItems;
    }

    private List<LSCompletionItem> getListenersAndPackagesItems(LSContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>(this.getPackagesCompletionItems(context));
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        List<Scope.ScopeEntry> listeners = visibleSymbols.stream()
                .filter(scopeEntry -> CommonUtil.isListenerObject(scopeEntry.symbol))
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(new ArrayList<>(listeners), context));
        return completionItems;
    }
    
    private List<LSCompletionItem> getListenersFromPackage(LSContext context, String alias) {
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        Optional<Scope.ScopeEntry> packageSymbolInfo = visibleSymbols.stream()
                .filter(scopeEntry -> scopeEntry.symbol instanceof BPackageSymbol
                        && scopeEntry.symbol.name.getValue().equals(alias))
                .findAny();
        
        if (!packageSymbolInfo.isPresent() || !(packageSymbolInfo.get().symbol instanceof BPackageSymbol)) {
            return completionItems;
        }
        List<Scope.ScopeEntry> listeners = ((BPackageSymbol) packageSymbolInfo.get().symbol).scope.entries.values()
                .stream()
                .filter(scopeEntry -> CommonUtil.isListenerObject(scopeEntry.symbol))
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(listeners, context));
        return completionItems;
    }
    
    private List<LSCompletionItem> getAllTopLevelItems(LSContext ctx) {
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        completionItems.addAll(this.addTopLevelItems(ctx));
        completionItems.addAll(this.getBasicTypesItems(ctx, visibleSymbols));
        
        return completionItems;
    }

    private boolean suggestListeners(List<CommonToken> lhsDefaultTokens) {
        List<Integer> tokenTypes = lhsDefaultTokens.stream()
                .map(CommonToken::getType)
                .collect(Collectors.toList());
        int assignToken = tokenTypes.indexOf(BallerinaParser.ASSIGN);
        int listenerToken = tokenTypes.indexOf(BallerinaParser.LISTENER);

        return assignToken == -1 && listenerToken > -1;
    }
}
