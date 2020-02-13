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
import org.ballerinalang.langserver.SnippetBlock;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Annotation Attachment Resolver to resolve the corresponding annotation attachments.
 */
@JavaSPIService("org.ballerinalang.langserver.completions.spi.LSCompletionProvider")
public class TypeDefinitionContextProvider extends LSCompletionProvider {

    public TypeDefinitionContextProvider() {
        this.attachmentPoints.add(BallerinaParser.TypeDefinitionContext.class);
        this.attachmentPoints.add(BallerinaParser.FiniteTypeUnitContext.class);
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext ctx) {
        List<CommonToken> lhsDefaultTokens = ctx.get(CompletionKeys.LHS_DEFAULT_TOKENS_KEY);
        List<Integer> lhsTokenTypes = lhsDefaultTokens.stream()
                .map(CommonToken::getType)
                .collect(Collectors.toList());
        if (this.isObjectTypeDefinition(ctx)) {
            /*
            Ex: public type <cursor> object {}
             */
            return Arrays.asList(Snippet.KW_ABSTRACT.get().build(ctx), Snippet.KW_CLIENT.get().build(ctx));
        } else if (lhsTokenTypes.contains(BallerinaParser.TYPE)) {
            List<CompletionItem> lsCItems = new ArrayList<>();
            Integer invocationType = ctx.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
            List<SymbolInfo> visibleSymbols = ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY);

            if (invocationType == BallerinaParser.COLON) {
                CommonToken pkgName = lhsDefaultTokens.get(lhsTokenTypes.indexOf(invocationType) - 1);
                lsCItems.addAll(this.getTypeItemsInPackage(visibleSymbols, pkgName.getText(), ctx));
            } else if (lhsTokenTypes.contains(BallerinaParser.CLIENT)
                    && lhsTokenTypes.contains(BallerinaParser.ABSTRACT)) {
                /*
                Ex: public type testType client abstract <cursor>
                 */
                lsCItems.add(Snippet.KW_OBJECT.get().build(ctx));
            } else if (lhsTokenTypes.contains(BallerinaParser.CLIENT)
                    || lhsTokenTypes.contains(BallerinaParser.ABSTRACT)) {
                /*
                Ex: public type testType client | abstract <cursor>
                 */
                SnippetBlock objectModifier = lhsTokenTypes.contains(BallerinaParser.CLIENT)
                        ? Snippet.KW_ABSTRACT.get() : Snippet.KW_CLIENT.get();
                lsCItems.add(objectModifier.build(ctx));
                lsCItems.add(Snippet.KW_OBJECT.get().build(ctx));
            } else {
                /*
                Ex: public type testType <cursor>
                Ex: public type testType r<cursor>
                 */
                lsCItems.addAll(this.getPackagesCompletionItems(ctx));
                lsCItems.addAll(this.getBasicTypesItems(visibleSymbols));
                lsCItems.add(Snippet.KW_ABSTRACT.get().build(ctx));
                lsCItems.add(Snippet.KW_CLIENT.get().build(ctx));
                lsCItems.add(Snippet.KW_RECORD.get().build(ctx));
                lsCItems.add(Snippet.KW_OBJECT.get().build(ctx));
            }

            return lsCItems;
        }

        return new ArrayList<>();
    }

    /**
     * Check whether the cursor is within the object type definition.
     * This is identified capturing the first open brace and the token before that. If the token before the first brace
     * is object, then cursor is within the object type
     *
     * @return {@link Boolean} whether the cursor is within the object context
     */
    private boolean isObjectTypeDefinition(LSContext ctx) {
        List<CommonToken> rhsTokens = ctx.get(CompletionKeys.RHS_TOKENS_KEY);
        List<CommonToken> defaultRHSTokens = rhsTokens.stream()
                .filter(commonToken -> commonToken.getChannel() == Token.DEFAULT_CHANNEL)
                .collect(Collectors.toList());

        Optional<CommonToken> leftBrace = defaultRHSTokens.stream()
                .filter(commonToken -> commonToken.getType() == BallerinaParser.LEFT_BRACE)
                .findFirst();
        if (!leftBrace.isPresent()) {
            return false;
        }
        int tokenBeforeBrace = defaultRHSTokens.get(defaultRHSTokens.indexOf(leftBrace.get()) - 1).getType();
        return tokenBeforeBrace == BallerinaParser.OBJECT;
    }
}
