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
package org.ballerinalang.langserver.completions.util.filters;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Symbol filters collection.
 */
public enum SymbolFilters {

    STMT_TEMPLATE_FILTER(StatementTemplateFilter.class, new StatementTemplateFilter());

    private final Class context;
    private final AbstractSymbolFilter symbolFilter;
    private static final Map<Class, AbstractSymbolFilter> resolverMap =
            Collections.unmodifiableMap(initializeMapping());

    SymbolFilters(Class context, AbstractSymbolFilter symbolFilter) {
        this.context = context;
        this.symbolFilter = symbolFilter;
    }

    private Class getContext() {
        return context;
    }

    private AbstractSymbolFilter getSymbolFilter() {
        return symbolFilter;
    }

    /**
     * Get the item filter by the class.
     * @param context                           context class to extract the relevant filter
     * @return {@link AbstractSymbolFilter}     Symbol filter for the given context
     */
    public static AbstractSymbolFilter get(Class context) {
        return resolverMap.get(context);
    }

    private static Map<Class, AbstractSymbolFilter> initializeMapping() {
        Map<Class, AbstractSymbolFilter> map = new HashMap<>();
        for (SymbolFilters resolver : SymbolFilters.values()) {
            map.put(resolver.getContext(), resolver.getSymbolFilter());
        }
        return map;
    }
}
