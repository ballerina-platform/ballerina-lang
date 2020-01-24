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

import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Priority;
import org.eclipse.lsp4j.CompletionItem;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Item Sorter for the actions and field access items.
 */
public class ActionAndFieldAccessContextItemSorter extends CompletionItemSorter {
    /**
     * Sort Completion Items based on a particular criteria.
     *
     * @param ctx             Completion context
     * @param completionItems List of initial completion items
     */
    @Override
    public void sortItems(LSContext ctx, List<CompletionItem> completionItems) {
        this.setPriorities(completionItems);
        completionItems.forEach(this::decreasePriority);
    }

    @Override
    void setPriorities(List<CompletionItem> completionItems) {
        completionItems.forEach(completionItem -> {
            switch (completionItem.getDetail()) {
                case ItemResolverConstants.NONE:
                case ItemResolverConstants.KEYWORD_TYPE:
                case ItemResolverConstants.STATEMENT_TYPE:
                case ItemResolverConstants.SNIPPET_TYPE:
                case ItemResolverConstants.FIELD_TYPE:
                case ItemResolverConstants.B_TYPE:
                case ItemResolverConstants.PACKAGE_TYPE:
                case ItemResolverConstants.FUNCTION_TYPE:
                    super.setPriority(completionItem);
                    break;
                default:
                    completionItem.setSortText(Priority.PRIORITY220.toString());
                    break;
            }
        });
    }

    @Override
    @Nonnull
    List<Class> getAttachedContexts() {
        return null;
    }

    private void decreasePriority(CompletionItem completionItem) {
        int sortText = Integer.parseInt(completionItem.getSortText());
        completionItem.setSortText(Integer.toString(sortText + 1));
    }
}
