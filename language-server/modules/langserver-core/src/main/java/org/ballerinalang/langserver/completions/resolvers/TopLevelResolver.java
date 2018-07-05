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
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Resolves all items that can appear as a top level element in the file.
 */
public class TopLevelResolver extends AbstractItemResolver {

    @Override
    public List<CompletionItem> resolveItems(LSServiceOperationContext ctx) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        ParserRuleContext parserRuleContext = ctx.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY);
        AbstractItemResolver errorContextResolver = parserRuleContext == null ? null :
                CompletionItemResolver.getResolverByClass(parserRuleContext.getClass());
        Stack<Token> poppedTokens = ctx.get(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY);

        if (this.isAnnotationStart(ctx)) {
            completionItems.addAll(CompletionItemResolver
                    .getResolverByClass(ParserRuleAnnotationAttachmentResolver.class).resolveItems(ctx));
        } else {
            if (errorContextResolver == null
                    || errorContextResolver == this
                    || (errorContextResolver instanceof ParserRuleGlobalVariableDefinitionContextResolver
                    && poppedTokens.size() < 2)) {
                addTopLevelItems(completionItems);
                this.populateBasicTypes(completionItems, ctx.get(CompletionKeys.VISIBLE_SYMBOLS_KEY));
            } else {
                completionItems.addAll(errorContextResolver.resolveItems(ctx));
            }
        }

        ItemSorters.getSorterByClass(DefaultItemSorter.class).sortItems(ctx, completionItems);
        return completionItems;
    }

    private void addStaticItem(List<CompletionItem> completionItems, String label, String insertText, String detail) {
        CompletionItem item = new CompletionItem();
        item.setLabel(label);
        item.setInsertText(insertText);
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setDetail(detail);
        completionItems.add(item);
    }

    /**
     * Add top level items to the given completionItems List.
     *
     * @param completionItems - completionItems List
     */
    private void addTopLevelItems(ArrayList<CompletionItem> completionItems) {
        addStaticItem(completionItems, ItemResolverConstants.IMPORT, ItemResolverConstants.IMPORT + " ",
                ItemResolverConstants.KEYWORD_TYPE);
        addStaticItem(completionItems, ItemResolverConstants.FUNCTION, Snippet.FUNCTION.toString(),
                ItemResolverConstants.SNIPPET_TYPE);
        addStaticItem(completionItems, ItemResolverConstants.MAIN_FUNCTION, Snippet.MAIN_FUNCTION.toString(),
                ItemResolverConstants.SNIPPET_TYPE);
        addStaticItem(completionItems, ItemResolverConstants.SERVICE, Snippet.SERVICE.toString(),
                ItemResolverConstants.SNIPPET_TYPE);
        addStaticItem(completionItems, ItemResolverConstants.ANNOTATION, Snippet.ANNOTATION_DEFINITION.toString(),
                ItemResolverConstants.SNIPPET_TYPE);
        addStaticItem(completionItems, ItemResolverConstants.XMLNS, Snippet.NAMESPACE_DECLARATION.toString(),
                ItemResolverConstants.SNIPPET_TYPE);
        addStaticItem(completionItems, ItemResolverConstants.OBJECT_TYPE, Snippet.OBJECT_SNIPPET.toString(),
                ItemResolverConstants.SNIPPET_TYPE);
        addStaticItem(completionItems, ItemResolverConstants.RECORD_TYPE, Snippet.RECORD_SNIPPET.toString(),
                ItemResolverConstants.SNIPPET_TYPE);
        addStaticItem(completionItems, ItemResolverConstants.ENDPOINT, Snippet.ENDPOINT.toString(),
                ItemResolverConstants.SNIPPET_TYPE);
        addStaticItem(completionItems, ItemResolverConstants.TYPE_TYPE, ItemResolverConstants.TYPE,
                ItemResolverConstants.KEYWORD_TYPE);
        addStaticItem(completionItems, ItemResolverConstants.PUBLIC_KEYWORD, Snippet.PUBLIC_KEYWORD_SNIPPET.toString(),
                ItemResolverConstants.KEYWORD_TYPE);
    }
}
