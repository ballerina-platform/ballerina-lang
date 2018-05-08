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

import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Item sorters to be used on sorting completion items, based on the scope.
 */
public enum ItemSorters {
    ASSIGNMENT_STMT_ITEM_SORTER(BallerinaParser.AssignmentStatementContext.class,
            new AssignmentStmtContextSorter()),
    FUNCTION_BODY_ITEM_SORTER(BLangFunction.class,
            new CallableUnitBodyItemSorter()),
    DEFAULT_ITEM_SORTER(DefaultItemSorter.class,
            new DefaultItemSorter()),
    ENDPOINT_DEF_ITEM_SORTER(EndpointDefContextItemSorter.class,
            new EndpointDefContextItemSorter()),
    RESOURCE_BODY_ITEM_SORTER(BLangResource.class,
            new CallableUnitBodyItemSorter()),
    SERVICE_BODY_ITEM_SORTER(BLangService.class,
            new ServiceContextItemSorter()),
    STATEMENT_CONTEXT_ITEM_SORTER(StatementContextItemSorter.class,
            new StatementContextItemSorter()),
    TRANSFORMER_CONTEXT_ITEM_SORTER(BLangTransformer.class,
            new TransformerContextItemSorter()),
    VAR_DEF_CONTEXT_ITEM_SORTER(BallerinaParser.VariableDefinitionStatementContext.class,
            new VariableDefContextItemSorter()),
    CONDITIONAL_STMT_CONTEXT_ITEM_SORTER(ConditionalStatementItemSorter.class,
            new ConditionalStatementItemSorter()),
    MATCH_STMT_CONTEXT_ITEM_SORTER(MatchContextItemSorter.class,
            new MatchContextItemSorter());
    
    private final Class context;
    private final CompletionItemSorter itemSorter;
    private static final Map<Class, CompletionItemSorter> resolverMap =
            Collections.unmodifiableMap(initializeMapping());

    ItemSorters(Class context, CompletionItemSorter itemSorter) {
        this.context = context;
        this.itemSorter = itemSorter;
    }

    private Class getContext() {
        return context;
    }

    private CompletionItemSorter getItemSorter() {
        return itemSorter;
    }

    /**
     * Get the item sorter by the class.
     * @param context - context class to extract the relevant item sorter
     * @return {@link CompletionItemSorter} - Item sorter for the given context
     */
    public static CompletionItemSorter getSorterByClass(Class context) {
        if (!resolverMap.containsKey(context)) {
            return resolverMap.get(DefaultItemSorter.class);
        }
        return resolverMap.get(context);
    }

    private static Map<Class, CompletionItemSorter> initializeMapping() {
        Map<Class, CompletionItemSorter> map = new HashMap<>();
        for (ItemSorters resolver : ItemSorters.values()) {
            map.put(resolver.getContext(), resolver.getItemSorter());
        }
        return map;
    }
}
