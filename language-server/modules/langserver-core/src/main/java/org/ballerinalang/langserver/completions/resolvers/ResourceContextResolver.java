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
import org.ballerinalang.langserver.completions.util.CompletionItemResolver;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion item resolver for BLangResource context.
 */
public class ResourceContextResolver extends AbstractItemResolver {

    @Override
    public List<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        ParserRuleContext parserRuleContext = completionContext.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY);
        List<CompletionItem> completionItems = new ArrayList<>();

        if (parserRuleContext == null) {
            CompletionItem workerItem = new CompletionItem();
            workerItem.setLabel(ItemResolverConstants.WORKER);
            workerItem.setInsertText(Snippet.WORKER.toString());
            workerItem.setInsertTextFormat(InsertTextFormat.Snippet);
            workerItem.setDetail(ItemResolverConstants.SNIPPET_TYPE);
            
            CompletionItem endpointItem = new CompletionItem();
            endpointItem.setLabel(ItemResolverConstants.ENDPOINT);
            endpointItem.setInsertText(Snippet.ENDPOINT.toString());
            endpointItem.setInsertTextFormat(InsertTextFormat.Snippet);
            endpointItem.setDetail(ItemResolverConstants.SNIPPET_TYPE);
            
            completionItems.add(workerItem);
            completionItems.add(endpointItem);
            
            return completionItems;
        }
        AbstractItemResolver resolver = CompletionItemResolver.getResolverByClass(parserRuleContext.getClass());

        if (resolver != null) {
            completionItems.addAll(resolver.resolveItems(completionContext));
        }

        return completionItems;
    }
}
