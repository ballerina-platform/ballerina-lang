/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langserver.completions.resolvers;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleAnnotationAttachmentResolver;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleGlobalVariableDefinitionContextResolver;
import org.ballerinalang.langserver.completions.util.CompletionItemResolver;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.sorters.DefaultItemSorter;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resolves all items that can appear as a top level element in the file.
 */
public class TopLevelResolver extends AbstractItemResolver {

    @Override
    public List<CompletionItem> resolveItems(LSServiceOperationContext ctx) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        ParserRuleContext parserRuleContext = ctx.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY);
        AbstractItemResolver itemResolver = parserRuleContext == null ? null :
                CompletionItemResolver.getResolverByClass(parserRuleContext.getClass());
        List<String> poppedTokens = ctx.get(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY).stream()
                .map(Token::getText)
                .collect(Collectors.toList());
        if (this.isAnnotationStart(ctx)) {
            completionItems.addAll(CompletionItemResolver
                    .getResolverByClass(ParserRuleAnnotationAttachmentResolver.class).resolveItems(ctx));
        } else if (poppedTokens.size() >= 1 && poppedTokens.get(0).equals(ItemResolverConstants.PUBLIC_KEYWORD)) {
            completionItems.addAll(addTopLevelItems(ctx));
            completionItems.addAll(this.populateBasicTypes(ctx.get(CompletionKeys.VISIBLE_SYMBOLS_KEY)));
        } else if (itemResolver == null || itemResolver instanceof ParserRuleGlobalVariableDefinitionContextResolver) {
            completionItems.addAll(getGlobalVarDefCompletions(ctx, poppedTokens, itemResolver));
        } else {
            completionItems.addAll(itemResolver.resolveItems(ctx));
        }

        ItemSorters.get(DefaultItemSorter.class).sortItems(ctx, completionItems);
        return completionItems;
    }

    private CompletionItem getStaticItem(Snippet snippet, boolean isSnippet) {
        return snippet.get().build(new CompletionItem(), isSnippet);
    }

    /**
     * Add top level items to the given completionItems List.
     *
     * @return {@link List}     List of populated completion items
     */
    private List<CompletionItem> addTopLevelItems(LSContext context) {
        boolean snippetCapability = context.get(CompletionKeys.CLIENT_CAPABILITIES_KEY).getCompletionItem()
                .getSnippetSupport();
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        completionItems.add(getStaticItem(Snippet.KW_IMPORT, snippetCapability));
        completionItems.add(getStaticItem(Snippet.DEF_FUNCTION, snippetCapability));
        completionItems.add(getStaticItem(Snippet.DEF_MAIN_FUNCTION, snippetCapability));
        completionItems.add(getStaticItem(Snippet.DEF_SERVICE, snippetCapability));
        completionItems.add(getStaticItem(Snippet.DEF_SERVICE_WEBSOCKET, snippetCapability));
        completionItems.add(getStaticItem(Snippet.DEF_SERVICE_WEBSUB, snippetCapability));
        completionItems.add(getStaticItem(Snippet.DEF_ANNOTATION, snippetCapability));
        completionItems.add(getStaticItem(Snippet.STMT_NAMESPACE_DECLARATION, snippetCapability));
        completionItems.add(getStaticItem(Snippet.DEF_OBJECT_SNIPPET, snippetCapability));
        completionItems.add(getStaticItem(Snippet.DEF_RECORD, snippetCapability));
        completionItems.add(getStaticItem(Snippet.DEF_ENDPOINT, snippetCapability));
        completionItems.add(getStaticItem(Snippet.KW_TYPE, snippetCapability));
        completionItems.add(getStaticItem(Snippet.KW_PUBLIC, snippetCapability));
        completionItems.add(getStaticItem(Snippet.DEF_ERROR, snippetCapability));
        return completionItems;
    }

    private ArrayList<CompletionItem> getGlobalVarDefCompletions(LSServiceOperationContext context,
                                                                 List<String> poppedTokens,
                                                                 AbstractItemResolver itemResolver) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        if (poppedTokens.size() < 2) {
            completionItems.addAll(addTopLevelItems(context));
            completionItems.addAll(this.populateBasicTypes(context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY)));
        } else if (itemResolver instanceof ParserRuleGlobalVariableDefinitionContextResolver) {
            completionItems.addAll(itemResolver.resolveItems(context));
        }

        return completionItems;
    }
}
