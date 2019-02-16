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

package org.ballerinalang.langserver.completions.resolvers;

import org.antlr.v4.runtime.Token;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.util.CompletionItemResolver;
import org.ballerinalang.langserver.completions.util.filters.DelimiterBasedContentFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Item Resolver for the BLangRecord node context.
 */
public class BLangRecordContextResolver extends AbstractItemResolver {
    @Override
    public List<CompletionItem> resolveItems(LSServiceOperationContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<String> poppedTokens = context.get(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY)
                .stream()
                .map(Token::getText)
                .collect(Collectors.toList());

        if (this.isInvocationOrInteractionOrFieldAccess(context)) {
            Either<List<CompletionItem>, List<SymbolInfo>> eitherList = SymbolFilters
                    .get(DelimiterBasedContentFilter.class).filterItems(context);
            completionItems.addAll(this.getCompletionItemList(eitherList, context));
        } else if (poppedTokens.contains(UtilSymbolKeys.EQUAL_SYMBOL_KEY)) {
            // If the popped tokens contains the equal symbol, then the variable definition is being writing
            // This parser rule context is used to select the proper sorter.
            context.put(CompletionKeys.PARSER_RULE_CONTEXT_KEY,
                    new BallerinaParser.VariableDefinitionStatementContext(null, -1));
            return CompletionItemResolver.get(BallerinaParser.VariableDefinitionStatementContext.class)
                    .resolveItems(context);
        } else {
            List<SymbolInfo> filteredTypes = context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY).stream()
                    .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol instanceof BTypeSymbol)
                    .collect(Collectors.toList());
            completionItems.addAll(this.getCompletionItemList(filteredTypes, context));
            completionItems.addAll(this.getPackagesCompletionItems(context));
        }
        return completionItems;
    }
}
