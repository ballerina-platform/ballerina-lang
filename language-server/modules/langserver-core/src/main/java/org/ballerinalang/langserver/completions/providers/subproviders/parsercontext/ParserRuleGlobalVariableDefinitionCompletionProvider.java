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
package org.ballerinalang.langserver.completions.providers.subproviders.parsercontext;

import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.providers.subproviders.AbstractSubCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.TopLevelCompletionProvider;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.filters.DelimiterBasedContentFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Parser rule based variable definition statement context resolver.
 */
public class ParserRuleGlobalVariableDefinitionCompletionProvider extends AbstractSubCompletionProvider {
    @Override
    public List<CompletionItem> resolveItems(LSContext ctx) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<String> poppedTokens = CommonUtil.getPoppedTokenStrings(ctx);
        
        if (poppedTokens.isEmpty()) {
            return completionItems;
        }
        if (poppedTokens.size() >= 2
                && ItemResolverConstants.MAP_KEYWORD.equals(poppedTokens.get(0))
                && UtilSymbolKeys.LT_SYMBOL_KEY.equals(poppedTokens.get(1))) {
            if (!UtilSymbolKeys.GT_SYMBOL_KEY.equals(CommonUtil.getLastItem(poppedTokens))) {
                completionItems.addAll(getBasicTypes(ctx.get(CompletionKeys.VISIBLE_SYMBOLS_KEY)));
            }
        } else if (poppedTokens.size() <= 2) {
            String firstToken = poppedTokens.get(0);
            String lastToken = CommonUtil.getLastItem(poppedTokens);
            if (poppedTokens.contains(ItemResolverConstants.LISTENER_KEYWORD)) {
                completionItems.addAll(this.getListenersAndPackages(ctx));
            } else if (this.isAccessModifierToken(firstToken)) {
                switch (firstToken) {
                    case ItemResolverConstants.PUBLIC_KEYWORD:
                        completionItems.addAll(this.getItemsAfterPublic(ctx));
                        break;
                    case ItemResolverConstants.FINAL_KEYWORD:
                    case ItemResolverConstants.CONST_KEYWORD:
                        completionItems.addAll(this.getTypesAndPackages(ctx));
                        break;
                    default:
                        break;
                }
            } else if (this.isInvocationOrInteractionOrFieldAccess(ctx)
                    && UtilSymbolKeys.PKG_DELIMITER_KEYWORD.equals(lastToken)) {
                Either<List<CompletionItem>, List<SymbolInfo>> pkgContent = SymbolFilters
                        .get(DelimiterBasedContentFilter.class).filterItems(ctx);
                completionItems.addAll(this.getCompletionItemList(pkgContent, ctx));
            } else {
                completionItems.addAll(this.getAllTopLevelItems(ctx));
                completionItems.addAll(this.getPackagesCompletionItems(ctx));
            }
        } else if (this.isInvocationOrInteractionOrFieldAccess(ctx)
                && poppedTokens.contains(ItemResolverConstants.LISTENER_KEYWORD)) {
            completionItems.addAll(this.getListenersFromPackage(ctx, poppedTokens.get(poppedTokens.size() - 2)));
            // TODO: usage of index
        } else if (poppedTokens.contains(UtilSymbolKeys.EQUAL_SYMBOL_KEY)) {
            completionItems.addAll(new ParserRuleVariableDefinitionCompletionProvider().resolveItems(ctx));
        } else {
            completionItems.addAll(new TopLevelCompletionProvider().resolveItems(ctx));
        }
        return completionItems;
    }
    
    private List<CompletionItem> getItemsAfterPublic(LSContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        completionItems.add(getStaticItem(context, Snippet.DEF_FUNCTION));
        completionItems.add(getStaticItem(context, Snippet.DEF_ANNOTATION));
        completionItems.add(getStaticItem(context, Snippet.DEF_OBJECT_SNIPPET));
        completionItems.add(getStaticItem(context, Snippet.DEF_RECORD));
        completionItems.add(getStaticItem(context, Snippet.KW_LISTENER));
        return completionItems;
    }
    
    private List<CompletionItem> getListenersAndPackages(LSContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>(this.getPackagesCompletionItems(context));
        List<SymbolInfo> listeners = context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY).stream()
                .filter(symbolInfo -> CommonUtil.isListenerObject(symbolInfo.getScopeEntry().symbol))
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(listeners, context));
        return completionItems;
    }
    
    private List<CompletionItem> getListenersFromPackage(LSContext context, String alias) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        Optional<SymbolInfo> packageSymbolInfo = context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY).stream()
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
        completionItems.addAll(this.addTopLevelItems(ctx));
        completionItems.addAll(this.getBasicTypes(ctx.get(CompletionKeys.VISIBLE_SYMBOLS_KEY)));
        
        return completionItems;
    }
    
    private List<CompletionItem> getTypesAndPackages(LSContext ctx) {
        List<CompletionItem> completionItems =
                new ArrayList<>(this.getBasicTypes(ctx.get(CompletionKeys.VISIBLE_SYMBOLS_KEY)));
        completionItems.addAll(this.getPackagesCompletionItems(ctx));
        
        return completionItems;
    }
}
