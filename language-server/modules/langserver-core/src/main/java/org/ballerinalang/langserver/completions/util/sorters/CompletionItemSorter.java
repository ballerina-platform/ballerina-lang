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

import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Priority;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;

import java.util.HashMap;
import java.util.List;

/**
 * Abstract implementation for the completion Item Sorter.
 */
public abstract class CompletionItemSorter {

    /**
     * Sort Completion Items based on a particular criteria.
     * @param ctx               Completion context
     * @param completionItems   List of initial completion items
     */
    public abstract void sortItems(TextDocumentServiceContext ctx, List<CompletionItem> completionItems);

    /**
     * Assign the Priorities to the completion items.
     * @param completionItems - list of completion items
     * @param itemPriorityMap - Map of item priorities against the Item type
     */
    void setPriorities(List<CompletionItem> completionItems, HashMap<String, String> itemPriorityMap) {
        completionItems.forEach(completionItem -> {
            if (itemPriorityMap.containsKey(completionItem.getDetail())) {
                completionItem.setSortText(itemPriorityMap.get(completionItem.getDetail()));
            }
        });
    }

    /**
     * Set the priorities in the default order.
     * @param completionItems   list of completion items
     */
    void setPriorities(List<CompletionItem> completionItems) {
        completionItems.forEach(completionItem -> {
            switch (completionItem.getDetail()) {
                case ItemResolverConstants.NONE:
                    completionItem.setSortText(Priority.PRIORITY120.toString());
                    break;
                case ItemResolverConstants.KEYWORD_TYPE:
                    completionItem.setSortText(Priority.PRIORITY110.toString());
                    break;
                case ItemResolverConstants.STATEMENT_TYPE:
                    completionItem.setSortText(Priority.PRIORITY100.toString());
                    break;
                case ItemResolverConstants.SNIPPET_TYPE:
                    completionItem.setSortText(Priority.PRIORITY90.toString());
                    break;
                case ItemResolverConstants.FIELD_TYPE:
                    completionItem.setSortText(Priority.PRIORITY80.toString());
                    break;
                case ItemResolverConstants.B_TYPE:
                    completionItem.setSortText(Priority.PRIORITY70.toString());
                    break;
                case ItemResolverConstants.ENUM_TYPE:
                    completionItem.setSortText(Priority.PRIORITY60.toString());
                    break;
                case ItemResolverConstants.STRUCT:
                    completionItem.setSortText(Priority.PRIORITY50.toString());
                    break;
                case ItemResolverConstants.PACKAGE_TYPE:
                    completionItem.setSortText(Priority.PRIORITY40.toString());
                    break;
                case ItemResolverConstants.ACTION_TYPE:
                    completionItem.setSortText(Priority.PRIORITY30.toString());
                    break;
                case ItemResolverConstants.FUNCTION_TYPE:
                    completionItem.setSortText(Priority.PRIORITY20.toString());
                    break;
                default:
                    completionItem.setSortText(Priority.PRIORITY10.toString());
                    break;
            }
        });
    }

    CompletionItem getEndpointSnippet() {
        CompletionItem endpointItem = new CompletionItem();
        endpointItem.setLabel(ItemResolverConstants.ENDPOINT);
        endpointItem.setInsertText(Snippet.ENDPOINT.toString());
        endpointItem.setInsertTextFormat(InsertTextFormat.Snippet);
        endpointItem.setDetail(ItemResolverConstants.SNIPPET_TYPE);
        return endpointItem;
    }

    /**
     * Remove the specific type of completion items from the completion items list.
     * @param type              Completion Item type
     * @param completionItems   List of completion Items
     */
    void removeCompletionsByType(String type, List<CompletionItem> completionItems) {
        completionItems.removeIf(completionItem -> completionItem.getDetail().equals(type));
    }
}
