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
import org.ballerinalang.langserver.common.utils.CommonUtil;
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

/**
 * Resolves all items that can appear as a top level element in the file.
 */
public class TopLevelResolver extends AbstractItemResolver {

    @Override
    public List<CompletionItem> resolveItems(LSServiceOperationContext ctx) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        ParserRuleContext prContext = ctx.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY);
        boolean snippetSupport = ctx.get(CompletionKeys.CLIENT_CAPABILITIES_KEY)
                .getCompletionItem()
                .getSnippetSupport();

        AbstractItemResolver resolver = prContext == null ? null : CompletionItemResolver.get(prContext.getClass());

        List<String> poppedTokens = CommonUtil.getPoppedTokenStrings(ctx);
        if (this.isAnnotationStart(ctx)) {
            resolver = CompletionItemResolver.get(ParserRuleAnnotationAttachmentResolver.class);
            completionItems.addAll(resolver.resolveItems(ctx));
        } else if (poppedTokens.size() >= 1 && this.isAccessModifierToken(poppedTokens.get(0))) {
            // Provides completions after public keyword
            completionItems.addAll(this.addTopLevelItems(ctx));
            completionItems.addAll(this.getBasicTypes(ctx.get(CompletionKeys.VISIBLE_SYMBOLS_KEY)));
        } else if (poppedTokens.size() >= 1 && poppedTokens.get(0).equals(ItemResolverConstants.EXTERN_KEYWORD)) {
            // Completion after the extern keyword. Only the signature of function should suggest
            completionItems.add(Snippet.DEF_FUNCTION_SIGNATURE.get().build(snippetSupport));
        } else if (resolver == null || resolver instanceof ParserRuleGlobalVariableDefinitionContextResolver) {
            completionItems.addAll(getGlobalVarDefCompletions(ctx, poppedTokens, resolver));
        } else {
            completionItems.addAll(resolver.resolveItems(ctx));
        }

        ItemSorters.get(DefaultItemSorter.class).sortItems(ctx, completionItems);
        return completionItems;
    }

    private CompletionItem getStaticItem(Snippet snippet, boolean isSnippet) {
        return snippet.get().build(isSnippet);
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
        completionItems.add(getStaticItem(Snippet.KW_TYPE, snippetCapability));
        completionItems.add(getStaticItem(Snippet.KW_PUBLIC, snippetCapability));
        completionItems.add(getStaticItem(Snippet.KW_FINAL, snippetCapability));
        completionItems.add(getStaticItem(Snippet.KW_CONST, snippetCapability));
        completionItems.add(getStaticItem(Snippet.KW_EXTERN, snippetCapability));
        completionItems.add(getStaticItem(Snippet.DEF_ERROR, snippetCapability));
        completionItems.add(getStaticItem(Snippet.KW_LISTENER, snippetCapability));
        return completionItems;
    }

    private boolean isAccessModifierToken(String token) {
        return token.equals(ItemResolverConstants.PUBLIC_KEYWORD)
                || token.equals(ItemResolverConstants.CONST_KEYWORD)
                || token.equals(ItemResolverConstants.FINAL_KEYWORD);
    }

    private ArrayList<CompletionItem> getGlobalVarDefCompletions(LSServiceOperationContext context,
                                                                 List<String> poppedTokens,
                                                                 AbstractItemResolver itemResolver) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        if (poppedTokens.size() < 2) {
            completionItems.addAll(this.addTopLevelItems(context));
            completionItems.addAll(this.getBasicTypes(context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY)));
        } else if (itemResolver instanceof ParserRuleGlobalVariableDefinitionContextResolver) {
            completionItems.addAll(itemResolver.resolveItems(context));
        }

        return completionItems;
    }
}
