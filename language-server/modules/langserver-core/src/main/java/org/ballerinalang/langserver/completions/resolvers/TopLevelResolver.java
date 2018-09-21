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
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

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

        if (this.isAnnotationStart(ctx)) {
            completionItems.addAll(CompletionItemResolver
                    .getResolverByClass(ParserRuleAnnotationAttachmentResolver.class).resolveItems(ctx));
        } else if (itemResolver == null 
                || itemResolver instanceof ParserRuleGlobalVariableDefinitionContextResolver) {
            completionItems.addAll(getGlobalVarDefCompletions(ctx));
        } else {
            completionItems.addAll(itemResolver.resolveItems(ctx));
        }

        ItemSorters.get(DefaultItemSorter.class).sortItems(ctx, completionItems);
        return completionItems;
    }

    private CompletionItem getStaticItem(String label, Snippet snippet, String detail, boolean isSnippet) {
        CompletionItem item = new CompletionItem();
        snippet.getBlock().populateCompletionItem(item, isSnippet);
        item.setLabel(label);
        item.setDetail(detail);
        return item;
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
        completionItems.add(getStaticItem(ItemResolverConstants.IMPORT, Snippet.KW_IMPORT,
                ItemResolverConstants.KEYWORD_TYPE, snippetCapability));
        completionItems.add(getStaticItem(ItemResolverConstants.FUNCTION, Snippet.DEF_FUNCTION,
                ItemResolverConstants.SNIPPET_TYPE, snippetCapability));
        completionItems.add(getStaticItem(ItemResolverConstants.MAIN_FUNCTION, Snippet.DEF_MAIN_FUNCTION,
                ItemResolverConstants.SNIPPET_TYPE, snippetCapability));
        completionItems.add(getStaticItem(ItemResolverConstants.SERVICE, Snippet.DEF_SERVICE,
                ItemResolverConstants.SNIPPET_TYPE, snippetCapability));
        completionItems.add(getStaticItem(ItemResolverConstants.SERVICE_WEBSOCKET, Snippet.DEF_SERVICE_WEBSOCKET,
                ItemResolverConstants.SNIPPET_TYPE, snippetCapability));
        completionItems.add(getStaticItem(ItemResolverConstants.SERVICE_WEBSUB, Snippet.DEF_SERVICE_WEBSUB,
                ItemResolverConstants.SNIPPET_TYPE, snippetCapability));
        completionItems.add(getStaticItem(ItemResolverConstants.ANNOTATION, Snippet.DEF_ANNOTATION,
                ItemResolverConstants.SNIPPET_TYPE, snippetCapability));
        completionItems.add(getStaticItem(ItemResolverConstants.XMLNS, Snippet.STMT_NAMESPACE_DECLARATION,
                ItemResolverConstants.SNIPPET_TYPE, snippetCapability));
        completionItems.add(getStaticItem(ItemResolverConstants.OBJECT_TYPE, Snippet.DEF_OBJECT_SNIPPET,
                ItemResolverConstants.SNIPPET_TYPE, snippetCapability));
        completionItems.add(getStaticItem(ItemResolverConstants.RECORD_TYPE, Snippet.DEF_RECORD,
                ItemResolverConstants.SNIPPET_TYPE, snippetCapability));
        completionItems.add(getStaticItem(ItemResolverConstants.ENDPOINT, Snippet.DEF_ENDPOINT,
                ItemResolverConstants.SNIPPET_TYPE, snippetCapability));
        completionItems.add(getStaticItem(ItemResolverConstants.TYPE_TYPE, Snippet.KW_TYPE,
                ItemResolverConstants.KEYWORD_TYPE, snippetCapability));
        completionItems.add(getStaticItem(ItemResolverConstants.PUBLIC_KEYWORD, Snippet.KW_PUBLIC,
                ItemResolverConstants.KEYWORD_TYPE, snippetCapability));
        
        return completionItems;
    }
    
    private ArrayList<CompletionItem> getGlobalVarDefCompletions(LSServiceOperationContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        List<String> poppedTokens = context.get(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY).stream()
                .map(Token::getText)
                .collect(Collectors.toList());
        if (poppedTokens.size() < 2) {
            completionItems.addAll(addTopLevelItems(context));
            completionItems.addAll(this.populateBasicTypes(context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY)));
        } else {
            completionItems
                    .addAll(CompletionItemResolver.getResolverByClass(BallerinaParser.DefinitionContext.class)
                            .resolveItems(context));
        }
        
        return completionItems;
    }
}
