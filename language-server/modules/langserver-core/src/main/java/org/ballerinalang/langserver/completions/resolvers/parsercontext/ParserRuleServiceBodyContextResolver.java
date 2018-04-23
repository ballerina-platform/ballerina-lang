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
package org.ballerinalang.langserver.completions.resolvers.parsercontext;

import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.ballerinalang.langserver.completions.resolvers.AnnotationAttachmentResolver;
import org.ballerinalang.langserver.completions.util.CompletionItemResolver;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.sorters.DefaultItemSorter;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Completion Item Resolver for Parser Rule Context of Service Body.
 */
public class ParserRuleServiceBodyContextResolver extends AbstractItemResolver {

    @Override
    public ArrayList<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        if (this.isAnnotationContext(completionContext)) {
            completionContext.put(CompletionKeys.ATTACHMENT_POINT_NODE_TYPE_KEY, "resource");
            completionItems.addAll(CompletionItemResolver.getResolverByClass(AnnotationAttachmentResolver.class)
                    .resolveItems(completionContext));
        } else {
            fillBindKeyword(completionItems);
        }
        ItemSorters.getSorterByClass(DefaultItemSorter.class).sortItems(completionContext, completionItems);

        return completionItems;
    }
    
    private void fillBindKeyword(List<CompletionItem> completionItems) {
        // Populate Bind Statement template
        CompletionItem bindItem = new CompletionItem();
        bindItem.setLabel(ItemResolverConstants.BIND);
        bindItem.setInsertText(Snippet.BIND.toString());
        bindItem.setDetail(ItemResolverConstants.KEYWORD_TYPE);
        completionItems.add(bindItem);
    }
}
