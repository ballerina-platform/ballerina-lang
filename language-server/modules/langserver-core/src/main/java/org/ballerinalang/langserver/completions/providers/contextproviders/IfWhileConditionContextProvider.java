/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.langserver.LSCompletionItem;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.ballerinalang.langserver.completions.util.filters.DelimiterBasedContentFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parser rule based statement context provider for the condition context in if/ while.
 *
 * @since 0.992.0
 */
@JavaSPIService("org.ballerinalang.langserver.completions.spi.LSCompletionProvider")
public class IfWhileConditionContextProvider extends LSCompletionProvider {

    public IfWhileConditionContextProvider() {
        this.attachmentPoints.add(IfWhileConditionContextProvider.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context) {
        List<CommonToken> lhsTokens = context.get(CompletionKeys.LHS_TOKENS_KEY);
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));

        if (lhsTokens == null) {
            return this.getExpressionCompletions(context, visibleSymbols);
        }
        List<CommonToken> lhsDefaultTokens = lhsTokens.stream()
                .filter(commonToken -> commonToken.getChannel() == Token.DEFAULT_CHANNEL)
                .collect(Collectors.toList());
        List<Integer> tokenTypes = lhsDefaultTokens.stream()
                .map(CommonToken::getType)
                .collect(Collectors.toList());
        visibleSymbols.removeIf(CommonUtil.invalidSymbolsPredicate());
        boolean isTypeGuardCondition = this.isTypeGuardCondition(tokenTypes);
        int invocationOrDelimiterTokenType = context.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
        
        if (invocationOrDelimiterTokenType > -1) {
            if (invocationOrDelimiterTokenType == BallerinaParser.COLON && isTypeGuardCondition) {
                String pkgName = lhsDefaultTokens.get(tokenTypes.indexOf(invocationOrDelimiterTokenType) - 1).getText();
                return this.getTypeItemsInPackage(visibleSymbols, pkgName, context);
            }
            Either<List<LSCompletionItem>, List<Scope.ScopeEntry>> items =
                    SymbolFilters.get(DelimiterBasedContentFilter.class).filterItems(context);
            return this.getCompletionItemList(items, context);
        }
        if (isTypeGuardCondition) {
            return this.getBasicTypesItems(context, visibleSymbols);
        }
        
        return this.getExpressionCompletions(context, visibleSymbols);
    }
    
    private boolean isTypeGuardCondition(List<Integer> tokenTypes) {
        return tokenTypes.contains(BallerinaParser.IS);
    }
    
    private List<LSCompletionItem> getExpressionCompletions(LSContext context, List<Scope.ScopeEntry> visibleSymbols) {
        List<LSCompletionItem> completionItems = this.getCompletionItemList(new ArrayList<>(visibleSymbols), context);
        completionItems.addAll(this.getPackagesCompletionItems(context));
        
        return completionItems;
    }
}
