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
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleAnnotationAttachmentResolver;
import org.ballerinalang.langserver.completions.util.CompletionItemResolver;
import org.ballerinalang.langserver.completions.util.sorters.CompletionItemSorter;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.ArrayList;
import java.util.List;

/**
 * ServiceContextResolver.
 */
public class ServiceContextResolver extends AbstractItemResolver {

    @Override
    public List<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        ParserRuleContext parserRuleContext = completionContext.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY);

        if (this.isAnnotationStart(completionContext)) {
            completionItems.addAll(CompletionItemResolver
                    .get(ParserRuleAnnotationAttachmentResolver.class).resolveItems(completionContext));
        } else if (parserRuleContext == null || parserRuleContext instanceof BallerinaParser.ServiceDefinitionContext) {
            completionItems.addAll(this.populateBasicTypes(completionContext.get(CompletionKeys.VISIBLE_SYMBOLS_KEY)));
            CompletionItemSorter itemSorter =
                    ItemSorters.get(completionContext.get(CompletionKeys.SYMBOL_ENV_NODE_KEY).getClass());
            itemSorter.sortItems(completionContext, completionItems);
        } else {
            AbstractItemResolver resolver = CompletionItemResolver.get(parserRuleContext.getClass());
            if (resolver != null) {
                completionItems.addAll(resolver.resolveItems(completionContext));
            }
        }
        return completionItems;
    }
}
