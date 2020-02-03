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

import org.ballerinalang.langserver.completions.util.sorters.context.DefinitionContext;
import org.ballerinalang.langserver.completions.util.sorters.context.ImportDeclarationContext;
import org.ballerinalang.langserver.completions.util.sorters.scope.PackageScope;
import org.ballerinalang.langserver.completions.util.sorters.scope.ServiceScope;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Item sorters to be used on sorting completion items, based on the scope.
 */
public enum ItemSorters {
    DEFAULT_ITEM_SORTER(new DefaultItemSorter()),
    SERVICE_SCOPE_ITEM_SORTER(new ServiceScope()),
    DEFINITION_CTX_ITEM_SORTER(new DefinitionContext()),
    PACKAGE_SCOPE_ITEM_SORTER(new PackageScope()),
    IMPORT_DECL_CTX_ITEM_SORTER(new ImportDeclarationContext());

    private final CompletionItemSorter itemSorter;
    private static final Map<Class, CompletionItemSorter> resolverMap =
            Collections.unmodifiableMap(initializeMapping());

    ItemSorters(CompletionItemSorter itemSorter) {
        this.itemSorter = itemSorter;
    }

    private CompletionItemSorter getItemSorter() {
        return itemSorter;
    }

    /**
     * Get the item sorter by the class.
     * @param context - context class to extract the relevant item sorter
     * @return {@link CompletionItemSorter} - Item sorter for the given context
     */
    public static CompletionItemSorter get(Class context) {
        if (context == null || !resolverMap.containsKey(context)) {
            return resolverMap.get(DefaultItemSorter.class);
        }
        return resolverMap.get(context);
    }

    private static Map<Class, CompletionItemSorter> initializeMapping() {
        Map<Class, CompletionItemSorter> map = new HashMap<>();
        for (ItemSorters resolver : ItemSorters.values()) {
            for (Class attachedContext : resolver.itemSorter.getAttachedContexts()) {
                map.put(attachedContext, resolver.getItemSorter());
            }
        }
        return map;
    }
}
