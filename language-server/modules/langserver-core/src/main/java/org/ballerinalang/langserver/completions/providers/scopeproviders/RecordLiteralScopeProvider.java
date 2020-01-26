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
package org.ballerinalang.langserver.completions.providers.scopeproviders;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.LSCompletionItem;
import org.ballerinalang.langserver.common.utils.completion.BLangRecordLiteralUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.ballerinalang.langserver.completions.util.filters.DelimiterBasedContentFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Completion Item Resolver for BLangRecordLiteral.
 */
@JavaSPIService("org.ballerinalang.langserver.completions.spi.LSCompletionProvider")
public class RecordLiteralScopeProvider extends LSCompletionProvider {

    public RecordLiteralScopeProvider() {
        this.attachmentPoints.add(BLangRecordLiteral.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext ctx) {
        BLangNode scopeNode = ctx.get(CompletionKeys.SCOPE_NODE_KEY);
        List<CommonToken> lhsDefaultTokens = ctx.get(CompletionKeys.LHS_TOKENS_KEY).stream()
                .filter(commonToken -> commonToken.getChannel() == Token.DEFAULT_CHANNEL)
                .collect(Collectors.toList());
        List<Integer> defaultTokenTypes = lhsDefaultTokens.stream()
                .map(CommonToken::getType)
                .collect(Collectors.toList());
        int firstColonIndex = defaultTokenTypes.indexOf(BallerinaParser.COLON);
        int invocationTokenTypeIndex = defaultTokenTypes.lastIndexOf(ctx.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY));
        if (firstColonIndex == -1 && scopeNode instanceof BLangRecordLiteral) {
            /*
            ex:- @anotationVal {
                    <cursor>
                 }
                 @anotationVal {
                    f<cursor>
                 }
             */
            BLangRecordLiteral recordLiteral = (BLangRecordLiteral) scopeNode;
            return BLangRecordLiteralUtil.getFieldsForMatchingRecord(ctx, recordLiteral);
        }
        if (firstColonIndex >= 0 && firstColonIndex == invocationTokenTypeIndex) {
            /*
            ex:- @anotationVal {
                    f1: <cursor>
                 }
                 @anotationVal {
                    f1: a<cursor>
                 }
             */
            return this.getVarDefCompletions(ctx);
        } else {
            /*
            ex:- @anotationVal {
                    f1: a:<cursor>
                 }
                 @anotationVal {
                    f1: a:b<cursor>
                 }
             */
            Either<List<LSCompletionItem>, List<Scope.ScopeEntry>> filteredItems =
                    SymbolFilters.get(DelimiterBasedContentFilter.class).filterItems(ctx);
            return this.getCompletionItemList(filteredItems, ctx);
        }
    }
}
