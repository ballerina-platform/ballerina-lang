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
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.builder.BFunctionCompletionItemBuilder;
import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.langserver.common.utils.CommonUtil.getFunctionInvocationSignature;

/**
 * Context provider for Assignment statement.
 * 
 * @since 1.0
 */
@JavaSPIService("org.ballerinalang.langserver.completions.spi.LSCompletionProvider")
public class AssignmentStatementContextProvider extends LSCompletionProvider {

    public AssignmentStatementContextProvider() {
        this.attachmentPoints.add(BallerinaParser.AssignmentStatementContext.class);
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext ctx) {
        List<CompletionItem> completionItems = new ArrayList<>();
        List<Integer> defaultTokenTypes = ctx.get(CompletionKeys.LHS_DEFAULT_TOKEN_TYPES_KEY);
        List<CommonToken> defaultTokens = ctx.get(CompletionKeys.LHS_DEFAULT_TOKENS_KEY);
        int assignTokenIndex = defaultTokenTypes.indexOf(BallerinaParser.ASSIGN);
        int newTokenIndex = defaultTokenTypes.indexOf(BallerinaParser.NEW);
        String lhsToken = defaultTokens.get(assignTokenIndex - 1).getText();
        Optional<BSymbol> lhsTokenSymbol = this.getSymbolByName(lhsToken, ctx).stream()
                .map(symbolInfo -> symbolInfo.getScopeEntry().symbol)
                .filter(symbol -> symbol instanceof BVarSymbol)
                .findFirst();
        
        if (lhsTokenSymbol.isPresent() && newTokenIndex >= 0) {
            return getCompletionsAfterNewKW(lhsTokenSymbol.get(), ctx);
        }

        Integer invocationTokenType = ctx.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
        if (invocationTokenType != -1) {
            /*
            Action invocation context
             */
            return this.getProvider(InvocationOrFieldAccessContextProvider.class).getCompletions(ctx);
        }

        if (lhsTokenSymbol.isPresent() && lhsTokenSymbol.get().type.tsymbol instanceof BObjectTypeSymbol) {
            BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) lhsTokenSymbol.get().type.tsymbol;
            BInvokableSymbol initFunction = objectTypeSymbol.initializerFunc.symbol;
            Pair<String, String> newSign = getFunctionInvocationSignature(initFunction, CommonKeys.NEW_KEYWORD_KEY,
                    ctx);
            CompletionItem cItem = BFunctionCompletionItemBuilder.build(initFunction, newSign.getRight(),
                    newSign.getLeft(), ctx);
            completionItems.add(cItem);
        }

        List<SymbolInfo> filteredList = new ArrayList<>(ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        filteredList.removeIf(this.attachedSymbolFilter());
        filteredList.removeIf(symbolInfo -> symbolInfo.getScopeEntry().symbol instanceof BTypeSymbol);
        completionItems.addAll(this.getCompletionItemList(filteredList, ctx));
        completionItems.addAll(this.getPackagesCompletionItems(ctx));
        fillStaticItems(completionItems, ctx);
        return completionItems;
    }
    
    private void fillStaticItems(List<CompletionItem> completionItems, LSContext context) {
        // Add the wait keyword
        CompletionItem waitKeyword = Snippet.KW_WAIT.get().build(context);
        completionItems.add(waitKeyword);
        // Add the start keyword
        CompletionItem startKeyword = Snippet.KW_START.get().build(context);
        completionItems.add(startKeyword);
        // Add the flush keyword
        CompletionItem flushKeyword = Snippet.KW_FLUSH.get().build(context);
        completionItems.add(flushKeyword);
    }
    
    private List<CompletionItem> getCompletionsAfterNewKW(BSymbol lhsSymbol, LSContext context) {
        List<CompletionItem> completionItems = new ArrayList<>();
        if (!(lhsSymbol.type.tsymbol instanceof BObjectTypeSymbol)) {
            return completionItems;
        }
        BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) lhsSymbol.type.tsymbol;
        Integer invocationTokenType = context.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
        if (invocationTokenType < 0) {
            completionItems.addAll(getPackagesCompletionItems(context));
        }
        BInvokableSymbol initFunction = objectTypeSymbol.initializerFunc.symbol;
        Pair<String, String> newSign = getFunctionInvocationSignature(initFunction, objectTypeSymbol.name.value,
                context);
        CompletionItem cItem = BFunctionCompletionItemBuilder.build(initFunction, newSign.getRight(),
                newSign.getLeft(), context);
        completionItems.add(cItem);
        
        return completionItems;
    }
}
