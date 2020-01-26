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
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
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
    public List<LSCompletionItem> getCompletions(LSContext ctx) {
        if (this.isObjectTypeDefinition(ctx)) {
            return Arrays.asList(new SnippetCompletionItem(ctx, Snippet.KW_ABSTRACT.get()),
                    new SnippetCompletionItem(ctx, Snippet.KW_CLIENT.get()));
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
