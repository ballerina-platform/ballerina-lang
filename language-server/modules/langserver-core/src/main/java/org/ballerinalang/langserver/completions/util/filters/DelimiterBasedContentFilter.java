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
package org.ballerinalang.langserver.completions.util.filters;

import org.antlr.v4.runtime.CommonToken;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FilterUtils;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.sourceprune.SourcePruneKeys;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter the actions, functions and types in a package.
 */
public class DelimiterBasedContentFilter extends AbstractSymbolFilter {

    @Override
    public Either<List<CompletionItem>, List<SymbolInfo>> filterItems(LSContext ctx) {
        List<CommonToken> defaultTokens = ctx.get(SourcePruneKeys.LHS_DEFAULT_TOKENS_KEY);
        List<Integer> defaultTokenTypes = ctx.get(SourcePruneKeys.LHS_DEFAULT_TOKEN_TYPES_KEY);
        int delimiter = ctx.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
        String symbolToken = defaultTokens.get(defaultTokenTypes.lastIndexOf(delimiter) - 1).getText().replace("'", "");
        List<SymbolInfo> visibleSymbols = new ArrayList<>(ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        SymbolInfo symbol = FilterUtils.getVariableByName(symbolToken, visibleSymbols);
        boolean isActionInvocation = BallerinaParser.RARROW == delimiter
                && CommonUtil.isClientObject(symbol.getScopeEntry().symbol);
        boolean isWorkerSend = !isActionInvocation && BallerinaParser.RARROW == delimiter;

        if (BallerinaParser.DOT == delimiter || BallerinaParser.NOT == delimiter || BallerinaParser.COLON == delimiter
                || BallerinaParser.OPTIONAL_FIELD_ACCESS == delimiter || isActionInvocation) {
            List<SymbolInfo> symbolInfos = FilterUtils.filterVariableEntriesOnDelimiter(ctx, symbolToken, delimiter,
                    defaultTokens, defaultTokenTypes.lastIndexOf(delimiter));
            return Either.forRight(symbolInfos);
        }
        if (isWorkerSend) {
            List<SymbolInfo> workerSymbols = CommonUtil.getWorkerSymbols(ctx);
            return Either.forRight(workerSymbols);
        }

        return Either.forRight(new ArrayList<>());
    }
}
