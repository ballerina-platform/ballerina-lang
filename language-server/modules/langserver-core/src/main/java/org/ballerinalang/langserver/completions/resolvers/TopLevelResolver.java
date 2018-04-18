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
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext
        .ParserRuleGlobalVariableDefinitionContextResolver;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleTypeNameContextResolver;
import org.ballerinalang.langserver.completions.util.CompletionItemResolver;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.sorters.DefaultItemSorter;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves all items that can appear as a top level element in the file.
 */
public class TopLevelResolver extends AbstractItemResolver {

    @Override
    public ArrayList<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        ParserRuleContext parserRuleContext = completionContext.get(DocumentServiceKeys.PARSER_RULE_CONTEXT_KEY);
        AbstractItemResolver errorContextResolver = parserRuleContext == null ? null :
                CompletionItemResolver.getResolverByClass(parserRuleContext.getClass());

        if (parserRuleContext instanceof BallerinaParser.ServiceBodyContext) {
            // NOTE: This is a special case for annotations in resource only
            completionItems.addAll(errorContextResolver.resolveItems(completionContext));
            return completionItems;
        }

        boolean isAnnotation = this.isAnnotationContext(completionContext);
        if (isAnnotation) {
            completionItems.addAll(CompletionItemResolver
                    .getResolverByClass(AnnotationAttachmentResolver.class).resolveItems(completionContext));
        } else {
            if (errorContextResolver == null || errorContextResolver == this) {
                addTopLevelItems(completionItems);
            }
            if (errorContextResolver instanceof PackageNameContextResolver) {
                completionItems.addAll(errorContextResolver.resolveItems(completionContext));
            } else if (errorContextResolver instanceof ParserRuleGlobalVariableDefinitionContextResolver) {
                addTopLevelItems(completionItems);
                completionItems.addAll(errorContextResolver.resolveItems(completionContext));
            } else if (errorContextResolver instanceof ParserRuleTypeNameContextResolver) {
                addTopLevelItems(completionItems);
                completionItems.addAll(errorContextResolver.resolveItems(completionContext));
            }
        }

        ItemSorters.getSorterByClass(DefaultItemSorter.class).sortItems(completionContext, completionItems);
        return completionItems;
    }

    void addStaticItem(List<CompletionItem> completionItems, String label, String insertText, String detail) {
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
    }
}
