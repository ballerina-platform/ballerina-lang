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
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Completion Item Resolver for the Definition Context.
 *
 * @since v0.982.0
 */
@JavaSPIService("org.ballerinalang.langserver.completions.spi.LSCompletionProvider")
public class DefinitionContextProvider extends LSCompletionProvider {

    public DefinitionContextProvider() {
        this.attachmentPoints.add(BallerinaParser.DefinitionContext.class);
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext context) {
        List<CompletionItem> completionItems = new ArrayList<>();
        List<Integer> lhsDefaultTokens = context.get(CompletionKeys.LHS_TOKENS_KEY).stream()
                .filter(commonToken -> commonToken.getChannel() == Token.DEFAULT_CHANNEL)
                .map(CommonToken::getType)
                .collect(Collectors.toList());

        switch (lhsDefaultTokens.get(0)) {
            case BallerinaParser.PUBLIC:
                completionItems.addAll(this.getItemsAfterPublic(context));
                break;
            case BallerinaParser.FINAL:
            case BallerinaParser.CONST:
                completionItems.addAll(this.getTypesAndPackages(context));
                break;
            default:
                break;
        }
        return completionItems;
    }

    private List<CompletionItem> getItemsAfterPublic(LSContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        completionItems.add(getStaticItem(context, Snippet.DEF_FUNCTION));
        completionItems.add(getStaticItem(context, Snippet.DEF_ANNOTATION));
        completionItems.add(getStaticItem(context, Snippet.DEF_OBJECT_SNIPPET));
        completionItems.add(getStaticItem(context, Snippet.DEF_RECORD));
        completionItems.add(getStaticItem(context, Snippet.DEF_CLOSED_RECORD));
        completionItems.add(getStaticItem(context, Snippet.KW_LISTENER));
        completionItems.add(getStaticItem(context, Snippet.KW_TYPE));
        completionItems.add(getStaticItem(context, Snippet.KW_CONST));
        completionItems.add(getStaticItem(context, Snippet.KW_ANNOTATION));
        completionItems.add(getStaticItem(context, Snippet.KW_FUNCTION));
        return completionItems;
    }

    private List<CompletionItem> getTypesAndPackages(LSContext ctx) {
        List<SymbolInfo> visibleSymbols = new ArrayList<>(ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        List<CompletionItem> completionItems = new ArrayList<>(this.getBasicTypes(visibleSymbols));
        completionItems.addAll(this.getPackagesCompletionItems(ctx));

        return completionItems;
    }
}
