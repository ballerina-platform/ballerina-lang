/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions;

import io.ballerina.compiler.api.ModuleID;
import org.ballerinalang.langserver.common.Trie;
import org.ballerinalang.langserver.commons.LanguageServerContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Prefix based search provider.
 *
 * @since 2201.1.1
 * */
public class CompletionSearchProvider {

    private final Trie trie;
    private final Set<ModuleID> indexedModules = new HashSet<>();

    public static final LanguageServerContext.Key<CompletionSearchProvider> SEARCH_PROVIDER_KEY =
            new LanguageServerContext.Key<>();

    public static CompletionSearchProvider getInstance(LanguageServerContext context) {
        CompletionSearchProvider completionSearchProvider = context.get(SEARCH_PROVIDER_KEY);
        if (completionSearchProvider == null) {
            completionSearchProvider = new CompletionSearchProvider(context);
        }

        return completionSearchProvider;
    }

    private CompletionSearchProvider(LanguageServerContext context) {
        this.trie = new Trie(Collections.emptyList());
        context.put(SEARCH_PROVIDER_KEY, this);
    }

    /**
     * Get the list of words that match the prefix.
     *
     * @param prefix    type prefix.
     * @return {@link List} List of words.
     */
    public List<String> getSuggestions(String prefix) {
       return trie.suggest(prefix);
    }

    /**
     * Add words to the trie.
     *
     * @param moduleName    module name.
     * @param stringList    list of words.
     */
    public void indexModule(ModuleID moduleName, List<String> stringList) {
        indexedModules.add(moduleName);
        stringList.forEach(s -> trie.root.insert(s.toLowerCase(Locale.ENGLISH)));
    }

    /**
     * Check if the module is already indexed.
     *
     * @param moduleName    module name
     * @return {@link Boolean} whether the module is already indexed or not.
     */
    public boolean checkModuleIndexed(ModuleID moduleName) {
        return indexedModules.contains(moduleName);
    }
}
