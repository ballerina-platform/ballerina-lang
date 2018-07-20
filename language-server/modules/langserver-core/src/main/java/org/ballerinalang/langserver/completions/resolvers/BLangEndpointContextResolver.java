/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE2.0
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
import org.ballerinalang.langserver.common.utils.completion.BLangRecordLiteralUtil;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.util.filters.PackageActionFunctionAndTypesFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.ballerinalang.langserver.completions.util.sorters.DefaultItemSorter;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BLangEndpoint context Item Resolver.
 */
public class BLangEndpointContextResolver extends AbstractItemResolver {
    
    @Override
    @SuppressWarnings("unchecked")
    public List<CompletionItem> resolveItems(LSServiceOperationContext ctx) {
        BLangNode bLangEndpoint = ctx.get(CompletionKeys.SYMBOL_ENV_NODE_KEY);
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        
        if (bLangEndpoint instanceof  BLangEndpoint
                && ((BLangEndpoint) bLangEndpoint).configurationExpr instanceof BLangRecordLiteral) {
            List<String> poppedTokens = ctx.get(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY)
                    .stream()
                    .map(Token::getText)
                    .collect(Collectors.toList());
            if (poppedTokens.indexOf(UtilSymbolKeys.PKG_DELIMITER_KEYWORD) == 1) {
                // Try to get completions for the field from the visible symbols
                // Eg: port: config:<cursor>
                if (isInvocationOrFieldAccess(ctx)) {
                    Either<List<CompletionItem>, List<SymbolInfo>> filteredList =
                            SymbolFilters.getFilterByClass(PackageActionFunctionAndTypesFilter.class)
                                    .filterItems(ctx);
                    this.populateCompletionItemList(filteredList, completionItems);
                } else {
                    completionItems.addAll(this.getVariableDefinitionCompletionItems(ctx));
                }
                ItemSorters.getSorterByClass(DefaultItemSorter.class).sortItems(ctx, completionItems);
            } else {
                BLangRecordLiteral recordLiteral
                        = (BLangRecordLiteral) ((BLangEndpoint) bLangEndpoint).configurationExpr;
                completionItems.addAll(BLangRecordLiteralUtil.getFieldsForMatchingRecord(recordLiteral, ctx));
            }
        }

        return completionItems;
    }

    /**
     * Check whether the token stream corresponds to a action invocation or a function invocation.
     *
     * @param context Completion operation context
     * @return {@link Boolean}      Whether invocation or Field Access
     */
    @Override
    protected boolean isInvocationOrFieldAccess(LSServiceOperationContext context) {
        List<String> poppedTokens = context.get(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY)
                .stream()
                .map(Token::getText)
                .collect(Collectors.toList());
        String topMostPoppedToken = poppedTokens.get(poppedTokens.size() - 1);
        return (poppedTokens.size() > 2)
                && (UtilSymbolKeys.PKG_DELIMITER_KEYWORD.equals(topMostPoppedToken)
                || UtilSymbolKeys.DOT_SYMBOL_KEY.equals(topMostPoppedToken));
    }
}
