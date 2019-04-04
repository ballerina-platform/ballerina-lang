/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langserver.completions.providers.subproviders;

import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.util.sorters.DefaultItemSorter;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves all items that can appear as a top level element in the file.
 */
public class TopLevelCompletionProvider extends AbstractSubCompletionProvider {
    @Override
    public List<CompletionItem> resolveItems(LSContext ctx) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        completionItems.addAll(addTopLevelItems(ctx));
        completionItems.addAll(getBasicTypes(ctx.get(CompletionKeys.VISIBLE_SYMBOLS_KEY)));

        ItemSorters.get(DefaultItemSorter.class).sortItems(ctx, completionItems);
        return completionItems;
    }
}
