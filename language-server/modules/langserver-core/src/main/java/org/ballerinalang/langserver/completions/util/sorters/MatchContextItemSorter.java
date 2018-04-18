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

import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Priority;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.List;

/**
 * Completion Item sorter for the match statement completions.
 */
public class MatchContextItemSorter extends CompletionItemSorter {
    /**
     * Sort Completion Items based on a particular criteria.
     *
     * @param ctx             Completion context
     * @param completionItems List of initial completion items
     */
    @Override
    public void sortItems(LSServiceOperationContext ctx, List<CompletionItem> completionItems) {
        completionItems.forEach(completionItem -> {
            if (completionItem.getDetail().equals(ItemResolverConstants.B_TYPE)) {
                completionItem.setSortText(Priority.PRIORITY150.toString());
            } else if (completionItem.getDetail().equals(ItemResolverConstants.FUNCTION_TYPE)
                    && InsertTextFormat.Snippet.equals(completionItem.getInsertTextFormat())) {
                completionItem.setDetail(ItemResolverConstants.SNIPPET_TYPE);
                completionItem.setSortText(Priority.PRIORITY120.toString());
            } else if (completionItem.getDetail().equals(ItemResolverConstants.FUNCTION_TYPE)) {
                completionItem.setSortText(Priority.PRIORITY140.toString());
            } else if (InsertTextFormat.Snippet.equals(completionItem.getInsertTextFormat())) {
                completionItem.setDetail(ItemResolverConstants.SNIPPET_TYPE);
                completionItem.setSortText(Priority.PRIORITY110.toString());
            } else {
                completionItem.setSortText(Priority.PRIORITY130.toString());
            }
        });
    }
}
