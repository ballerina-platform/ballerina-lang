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
package org.ballerinalang.langserver.completions.util.sorters;

import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Priority;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Item sorter for the service context.
 */
public class ServiceContextItemSorter extends CompletionItemSorter {
    @Override
    public void sortItems(LSServiceOperationContext ctx, List<CompletionItem> completionItems) {
        BLangNode previousNode = ctx.get(CompletionKeys.PREVIOUS_NODE_KEY);
        boolean isSnippet = ctx.get(CompletionKeys.CLIENT_CAPABILITIES_KEY).getCompletionItem().getSnippetSupport();

        this.removeCompletionsByType(new ArrayList<>(Collections.singletonList(ItemResolverConstants.STATEMENT_TYPE)),
                completionItems);
        if (previousNode == null) {
            this.populateWhenCursorBeforeOrAfterEp(completionItems, isSnippet);
        } else if (previousNode instanceof BLangSimpleVariableDef) {
            this.setPriorities(completionItems);
            CompletionItem resItem = this.getResourceSnippet(isSnippet);
            resItem.setSortText(Priority.PRIORITY160.toString());
            completionItems.add(resItem);
        } else if (previousNode instanceof BLangResource) {
            completionItems.clear();
            completionItems.add(this.getResourceSnippet(isSnippet));
        }
    }
    
    private void populateWhenCursorBeforeOrAfterEp(List<CompletionItem> completionItems, boolean snippetCapability) {
        CompletionItem xmlnsSnippet = Snippet.STMT_NAMESPACE_DECLARATION.get().build(snippetCapability);
        CompletionItem resSnippet = this.getResourceSnippet(snippetCapability);
        this.setPriorities(completionItems);

        xmlnsSnippet.setSortText(Priority.PRIORITY150.toString());
        resSnippet.setSortText(Priority.PRIORITY160.toString());
        completionItems.add(xmlnsSnippet);
        completionItems.add(resSnippet);
    }
    
    private CompletionItem getResourceSnippet(boolean snippetCapability) {
        return Snippet.DEF_RESOURCE.get().build(snippetCapability);
    }
}
