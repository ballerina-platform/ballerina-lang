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
package org.ballerinalang.langserver.completions.providers.contextproviders;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.sourceprune.SourcePruneKeys;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Completion Item Resolver for the Definition Context.
 *
 * @since v0.982.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.LSCompletionProvider")
public class ServiceDefinitionContextProvider extends AbstractCompletionProvider {

    public ServiceDefinitionContextProvider() {
        this.attachmentPoints.add(BallerinaParser.ServiceDefinitionContext.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        List<CommonToken> lhsTokens = context.get(SourcePruneKeys.LHS_TOKENS_KEY);
        List<CommonToken> lhsDefaultTokens = lhsTokens.stream()
                .filter(commonToken -> commonToken.getChannel() == Token.DEFAULT_CHANNEL)
                .collect(Collectors.toList());
        int startIndex = lhsDefaultTokens.size() - 1;
        // Backtrack the tokens from the head of the popped tokens in order determine the cursor position
        tokenScanner:
        while (true) {
            if (startIndex < 0) {
                return completionItems;
            }
            CommonToken token = lhsDefaultTokens.get(startIndex);
            int tokenType = token.getType();
            switch (tokenType) {
                case BallerinaParser.SERVICE:
                case BallerinaParser.ON:
                case BallerinaParser.NEW:
                case BallerinaParser.COLON:
                    break tokenScanner;
                default:
                    break;
            }

            startIndex--;
        }

        switch (lhsDefaultTokens.get(startIndex).getType()) {
            case BallerinaParser.ON : {
                // suggest all the visible, defined listeners
                completionItems.addAll(this.getCompletionItemsAfterOnKeyword(context));
                break;
            }
            case BallerinaParser.NEW: {
                /*
                    service helloService on new <cursor> {
                    }
                    Ideally this should be a syntax error and current grammar do not support it
                    Also Issue #18729 is also broken
                 */
                List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
                List<Scope.ScopeEntry> filteredSymbols = this.filterListenerTypes(visibleSymbols);
                completionItems.addAll(this.getCompletionItemList(new ArrayList<>(filteredSymbols), context));
                completionItems.addAll(this.getPackagesCompletionItems(context));
                break;
            }
            case BallerinaParser.COLON: {
                List<Scope.ScopeEntry> listeners = this.filterListenersFromPackage(context);
                completionItems.addAll(this.getCompletionItemList(listeners, context));
                break;
            }
            default: {
                // Fill the on keyword completion item
                completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ON.get()));
                break;
            }
        }
        return completionItems;
    }

    private List<Scope.ScopeEntry> filterListenerTypes(List<Scope.ScopeEntry> scopeEntries) {
        return scopeEntries.stream()
                .filter(scopeEntry -> CommonUtil.isListenerObject(scopeEntry.symbol))
                .collect(Collectors.toList());
    }
    
    private List<Scope.ScopeEntry> filterListenersFromPackage(LSContext context) {
        List<CommonToken> defaultTokens = context.get(SourcePruneKeys.LHS_DEFAULT_TOKENS_KEY);
        List<Integer> tokenTypes = context.get(SourcePruneKeys.LHS_DEFAULT_TOKEN_TYPES_KEY);
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        if (tokenTypes == null) {
            return new ArrayList<>();
        }

        int colonIndex = tokenTypes.indexOf(BallerinaParser.COLON);
        if (colonIndex < 0) {
            return new ArrayList<>();
        }
        String pkgName = defaultTokens.get(colonIndex - 1).getText();

        Optional<Scope.ScopeEntry> symbolWithName = visibleSymbols.stream()
                .filter(scopeEntry -> scopeEntry.symbol.name.getValue().equals(pkgName))
                .findAny();
        if (!symbolWithName.isPresent() || !(symbolWithName.get().symbol instanceof BPackageSymbol)) {
            return new ArrayList<>();
        }
        BPackageSymbol pkgSymbol = ((BPackageSymbol) symbolWithName.get().symbol);

        return pkgSymbol.scope.entries.values().stream()
                .filter(scopeEntry -> CommonUtil.isListenerObject(scopeEntry.symbol))
                .collect(Collectors.toList());
    }
}
