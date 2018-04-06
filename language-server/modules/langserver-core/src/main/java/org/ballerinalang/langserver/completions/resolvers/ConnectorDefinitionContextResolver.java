/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.resolvers;

import org.antlr.v4.runtime.ParserRuleContext;
import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.util.CompletionItemResolver;
import org.ballerinalang.langserver.completions.util.sorters.CompletionItemSorter;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;

/**
 * Resolver for Connector definition context.
 */
public class ConnectorDefinitionContextResolver extends AbstractItemResolver {
    @Override
    public ArrayList<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {

        ArrayList<CompletionItem> completionItems = new ArrayList<>();

        if (!this.isAnnotationContext(completionContext)) {
            ParserRuleContext parserRuleContext = completionContext.get(DocumentServiceKeys.PARSER_RULE_CONTEXT_KEY);
            if (parserRuleContext != null) {
                AbstractItemResolver resolver = CompletionItemResolver.getResolverByClass(parserRuleContext.getClass());
                if (resolver != null) {
                    completionItems.addAll(resolver.resolveItems(completionContext));
                }
            } else {
                CompletionItemSorter itemSorter = ItemSorters.getSorterByClass(completionContext.get(CompletionKeys
                        .SYMBOL_ENV_NODE_KEY).getClass());
                this.populateBasicTypes(completionItems, completionContext.get(CompletionKeys.VISIBLE_SYMBOLS_KEY));
                itemSorter.sortItems(completionContext, completionItems);
            }
        }

        return completionItems;
    }
}
