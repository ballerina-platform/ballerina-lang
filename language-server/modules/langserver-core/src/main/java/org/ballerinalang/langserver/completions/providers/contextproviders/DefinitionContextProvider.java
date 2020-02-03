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
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.sourceprune.SourcePruneKeys;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Completion Item Resolver for the Definition Context.
 *
 * @since v0.982.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.LSCompletionProvider")
public class DefinitionContextProvider extends AbstractCompletionProvider {

    public DefinitionContextProvider() {
        this.attachmentPoints.add(BallerinaParser.DefinitionContext.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        List<Integer> lhsDefaultTokens = context.get(SourcePruneKeys.LHS_TOKENS_KEY).stream()
                .filter(commonToken -> commonToken.getChannel() == Token.DEFAULT_CHANNEL)
                .map(CommonToken::getType)
                .collect(Collectors.toList());

        switch (lhsDefaultTokens.get(0)) {
            case BallerinaParser.PUBLIC:
                completionItems.addAll(this.getItemsAfterPublic(context));
                break;
            case BallerinaParser.FINAL:
            case BallerinaParser.CONST:
                completionItems.addAll(this.getTypesAndPackagesItems(context));
                break;
            default:
                break;
        }
        return completionItems;
    }

    private List<LSCompletionItem> getItemsAfterPublic(LSContext context) {
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_FUNCTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_ANNOTATION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_OBJECT_SNIPPET.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_RECORD.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_CLOSED_RECORD.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_LISTENER.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TYPE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CONST.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ANNOTATION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FUNCTION.get()));
        return completionItems;
    }

    private List<LSCompletionItem> getTypesAndPackagesItems(LSContext ctx) {
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        List<LSCompletionItem> completionItems = new ArrayList<>(this.getBasicTypesItems(ctx, visibleSymbols));
        completionItems.addAll(this.getPackagesCompletionItems(ctx));

        return completionItems;
    }
}
