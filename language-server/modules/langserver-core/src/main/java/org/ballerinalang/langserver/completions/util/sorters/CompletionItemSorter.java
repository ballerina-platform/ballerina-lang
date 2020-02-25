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

import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.util.Priority;
import org.eclipse.lsp4j.CompletionItem;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Abstract implementation for the completion Item Sorter.
 */
public abstract class CompletionItemSorter {

    /**
     * Sort Completion Items based on a particular criteria.
     *  @param ctx             Completion context
     * @param completionItems List of initial completion items
     * @return
     */
    public abstract List<CompletionItem> sortItems(LSContext ctx, List<LSCompletionItem> completionItems);

    /**
     * Set the priorities in the default order.
     *
     * @param completionItems list of completion items
     */
    protected void setPriorities(List<CompletionItem> completionItems) {
        completionItems.forEach(this::setPriority);
    }

    /**
     * Remove the specific type of completion items from the completion items list.
     *
     * @param types           Completion Item types
     * @param completionItems List of completion Items
     */
    void removeCompletionsByType(List<String> types, List<CompletionItem> completionItems) {
        completionItems.removeIf(completionItem -> types.contains(completionItem.getDetail()));
    }

    protected final void setPriority(CompletionItem completionItem) {
        if (completionItem.getKind() == null) {
            completionItem.setSortText(Priority.PRIORITY110.toString());
            return;
        }
        switch (completionItem.getKind()) {
            case Snippet:
                completionItem.setSortText(Priority.PRIORITY240.toString());
                break;
            case Unit:
                completionItem.setSortText(Priority.PRIORITY230.toString());
                break;
            case Keyword:
                completionItem.setSortText(Priority.PRIORITY220.toString());
                break;
            case Field:
                completionItem.setSortText(Priority.PRIORITY210.toString());
                break;
            case Event:
                completionItem.setSortText(Priority.PRIORITY200.toString());
                break;
            case Interface:
                completionItem.setSortText(Priority.PRIORITY190.toString());
                break;
            case Struct:
                completionItem.setSortText(Priority.PRIORITY180.toString());
                break;
            case TypeParameter:
                completionItem.setSortText(Priority.PRIORITY170.toString());
                break;
            case Enum:
                completionItem.setSortText(Priority.PRIORITY160.toString());
                break;
            case Class:
                completionItem.setSortText(Priority.PRIORITY150.toString());
                break;
            case Module:
                completionItem.setSortText(Priority.PRIORITY140.toString());
                break;
            case Variable:
                completionItem.setSortText(Priority.PRIORITY130.toString());
                break;
            case Function:
                completionItem.setSortText(Priority.PRIORITY120.toString());
                break;
            default:
                completionItem.setSortText(Priority.PRIORITY110.toString());
                break;
        }
    }

    protected abstract @Nonnull List<Class> getAttachedContexts();
}
