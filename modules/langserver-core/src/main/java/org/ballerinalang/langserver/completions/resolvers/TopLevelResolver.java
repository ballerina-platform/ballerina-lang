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
import org.ballerinalang.langserver.completions.SuggestionsFilterDataModel;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleConstantDefinitionContextResolver;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleGlobalVariableDefinitionContextResolver;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleTypeNameContextResolver;
import org.ballerinalang.model.AnnotationAttachment;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Resolves all items that can appear as a top level element in the file.
 */
public class TopLevelResolver extends AbstractItemResolver {

    @Override
    public ArrayList<CompletionItem> resolveItems(SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols,
                                                  HashMap<Class, AbstractItemResolver> resolvers) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();


        ParserRuleContext parserRuleContext = dataModel.getParserRuleContext();
        AbstractItemResolver errorContextResolver = parserRuleContext == null ? null :
                resolvers.get(parserRuleContext.getClass());

        boolean noAt = findPreviousToken(dataModel, "@", 5) < 0;
        if (noAt && (errorContextResolver == null || errorContextResolver == this)) {
            addTopLevelItems(completionItems, resolvers, dataModel, symbols);
        }
        if (errorContextResolver instanceof PackageNameContextResolver) {
            completionItems.addAll(errorContextResolver.resolveItems(dataModel, symbols, resolvers));
        } else if (errorContextResolver instanceof ParserRuleConstantDefinitionContextResolver) {
            completionItems.addAll(errorContextResolver.resolveItems(dataModel, symbols, resolvers));
        } else if (errorContextResolver instanceof ParserRuleGlobalVariableDefinitionContextResolver) {
            addTopLevelItems(completionItems, resolvers, dataModel, symbols);
            completionItems.addAll(errorContextResolver.resolveItems(dataModel, symbols, resolvers));
        } else if (errorContextResolver instanceof ParserRuleTypeNameContextResolver) {
            addTopLevelItems(completionItems, resolvers, dataModel, symbols);
            completionItems.addAll(errorContextResolver.resolveItems(dataModel, symbols, resolvers));
        } else {
            completionItems.addAll(
                    resolvers.get(AnnotationAttachment.class).resolveItems(dataModel, symbols, resolvers));
        }
        return completionItems;
    }

    void addStaticItem(List<CompletionItem> completionItems, String label, String insertText) {
        CompletionItem item = new CompletionItem();
        item.setLabel(label);
        item.setInsertText(insertText);
        item.setDetail(ItemResolverConstants.SNIPPET_TYPE);
        item.setSortText(Priority.PRIORITY7.name());
        completionItems.add(item);
    }

    /**
     * Add top level items to the given completionItems List.
     *
     * @param completionItems - completionItems List
     * @param resolvers       - resolvers
     * @param dataModel       - datamodel
     * @param symbols         - all symbols upto this point
     */
    private void addTopLevelItems(ArrayList<CompletionItem> completionItems, HashMap<Class,
            AbstractItemResolver> resolvers, SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols) {
        addStaticItem(completionItems, ItemResolverConstants.IMPORT, ItemResolverConstants.IMPORT + " ");
        addStaticItem(completionItems, ItemResolverConstants.PACKAGE, ItemResolverConstants.PACKAGE + " ");
        addStaticItem(completionItems, ItemResolverConstants.CONST, ItemResolverConstants.CONST + " ");
        addStaticItem(completionItems, ItemResolverConstants.FUNCTION, ItemResolverConstants.FUNCTION_TEMPLATE);
        addStaticItem(completionItems, ItemResolverConstants.SERVICE, ItemResolverConstants.SERVICE_TEMPLATE);
        addStaticItem(completionItems, ItemResolverConstants.TRANSFORMER, ItemResolverConstants.TRANSFORMER_TEMPLATE);
        addStaticItem(completionItems, ItemResolverConstants.CONNECTOR,
                ItemResolverConstants.CONNECTOR_DEFINITION_TEMPLATE);
        addStaticItem(completionItems, ItemResolverConstants.STRUCT,
                ItemResolverConstants.STRUCT_DEFINITION_TEMPLATE);
        addStaticItem(completionItems, ItemResolverConstants.ANNOTATION,
                ItemResolverConstants.ANNOTATION_DEFINITION_TEMPLATE);
        addStaticItem(completionItems, ItemResolverConstants.XMLNS,
                ItemResolverConstants.NAMESPACE_DECLARATION_TEMPLATE);
    }
}
