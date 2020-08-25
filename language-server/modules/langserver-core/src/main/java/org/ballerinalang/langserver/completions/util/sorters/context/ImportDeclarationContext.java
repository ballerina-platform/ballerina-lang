/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.util.sorters.context;

import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.util.Priority;
import org.ballerinalang.langserver.completions.util.sorters.CompletionItemSorter;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Item sorter for Import Declaration Context.
 *
 * @since 1.2.0
 */
public class ImportDeclarationContext extends CompletionItemSorter {
    @Override
    public List<CompletionItem> sortItems(LSContext ctx, List<LSCompletionItem> completionItems) {
        List<CompletionItem> cItems = new ArrayList<>();
        completionItems.forEach(lsCItem -> {
            CompletionItem completionItem = lsCItem.getCompletionItem();
            if (completionItem.getLabel().contains("/")) {
                // Ex: import a<cursor>
                completionItem.setSortText(Priority.PRIORITY120.toString());
            } else {
                /*
                Address both of the following usecases
                import o<cursor> - Priority is given to the org names
                import orgName/<cursor>
                 */
                completionItem.setSortText(Priority.PRIORITY110.toString());
            }
            cItems.add(completionItem);
        });
        return cItems;
    }

    @Nonnull
    @Override
    protected List<Class> getAttachedContexts() {
        return new ArrayList<>();
    }
}
