/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.aggregation.AggregationRuntime;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.state.StateEventPool;
import org.wso2.siddhi.core.event.state.populater.StateEventPopulatorFactory;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent.EventType;
import org.wso2.siddhi.core.exception.StoreQueryCreationException;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.StoreQueryRuntime;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.wso2.siddhi.core.util.parser.helper.QueryParserHelper;
import org.wso2.siddhi.core.window.Window;
import org.wso2.siddhi.query.api.aggregation.Within;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.execution.query.StoreQuery;
import org.wso2.siddhi.query.api.execution.query.input.store.AggregationInputStore;
import org.wso2.siddhi.query.api.execution.query.input.store.ConditionInputStore;
import org.wso2.siddhi.query.api.execution.query.input.store.InputStore;
import org.wso2.siddhi.query.api.execution.query.output.stream.OutputStream;
import org.wso2.siddhi.query.api.execution.query.output.stream.ReturnStream;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class to parse {@link StoreQueryRuntime}
 */
public class StoreQueryParser {

    /**
     * Parse a storeQuery and return corresponding StoreQueryRuntime.
     *
     * @param storeQuery       storeQuery to be parsed.
     * @param siddhiAppContext associated Siddhi app context.
     * @param tableMap         keyvalue containing tables.
     * @param windowMap        keyvalue containing windows.
     * @param aggregationMap   keyvalue containing aggregation runtimes.
     * @return StoreQueryRuntime
     */
    public static StoreQueryRuntime parse(StoreQuery storeQuery, SiddhiAppContext siddhiAppContext,
                                          Map<String, Table> tableMap,
                                          Map<String, Window> windowMap,
                                          Map<String, AggregationRuntime> aggregationMap) {
        String queryName = "store_query_" + storeQuery.getInputStore().getStoreId();
        InputStore inputStore = storeQuery.getInputStore();
        Within within = null;
        Expression per = null;
        Expression onCondition = Expression.value(true);
        MetaStreamEvent metaStreamEvent = new MetaStreamEvent();
        metaStreamEvent.setInputReferenceId(inputStore.getStoreReferenceId());

        if (inputStore instanceof AggregationInputStore) {
            AggregationInputStore aggregationInputStore = (AggregationInputStore) inputStore;
            if (aggregationInputStore.getPer() != null && aggregationInputStore.getWithin() != null) {
                if (metaStreamEvent.getEventType() != EventType.AGGREGATE) {
                    throw new StoreQueryCreationException(inputStore.getStoreId() +
                            " is not an aggregation hence it cannot be processed with 'within' and 'per'.");
                }
                within = aggregationInputStore.getWithin();
                per = aggregationInputStore.getPer();
            } else if (aggregationInputStore.getPer() != null || aggregationInputStore.getWithin() != null) {
                throw new StoreQueryCreationException(inputStore.getStoreId() +
                        " should either have both 'within' and 'per' defined or none.");
            }
            onCondition = ((AggregationInputStore) inputStore).getOnCondition();
        } else if (inputStore instanceof ConditionInputStore) {
            onCondition = ((ConditionInputStore) inputStore).getOnCondition();
        }
        List<VariableExpressionExecutor> variableExpressionExecutors = new ArrayList<>();
        Table table = tableMap.get(inputStore.getStoreId());
        if (table != null) {
            metaStreamEvent.setEventType(EventType.TABLE);
            initMetaStreamEvent(metaStreamEvent, table.getTableDefinition());
            MatchingMetaInfoHolder metaStreamInfoHolder = generateMatchingMetaInfoHolder(metaStreamEvent,
                    table.getTableDefinition());
            CompiledCondition compiledCondition = table.compileCondition(onCondition, metaStreamInfoHolder,
                    siddhiAppContext, variableExpressionExecutors, tableMap, queryName);
            StoreQueryRuntime storeQueryRuntime = new StoreQueryRuntime(table, compiledCondition, queryName,
                    metaStreamEvent.getEventType());
            pupulateStoreQueryRuntime(storeQueryRuntime, metaStreamInfoHolder, storeQuery.getSelector(),
                    variableExpressionExecutors, siddhiAppContext, tableMap, queryName);
            return storeQueryRuntime;
        } else {
            AggregationRuntime aggregation = aggregationMap.get(inputStore.getStoreId());
            if (aggregation != null) {
                metaStreamEvent.setEventType(EventType.AGGREGATE);
                initMetaStreamEvent(metaStreamEvent, aggregation.getAggregationDefinition());
                MatchingMetaInfoHolder metaStreamInfoHolder = generateMatchingMetaInfoHolder(metaStreamEvent,
                        aggregation.getAggregationDefinition());
                CompiledCondition compiledCondition = aggregation.compileCondition(onCondition, within, per,
                        generateMatchingMetaInfoHolder(metaStreamEvent, aggregation.getAggregationDefinition()),
                        variableExpressionExecutors, tableMap, queryName, siddhiAppContext);
                StoreQueryRuntime storeQueryRuntime = new StoreQueryRuntime(aggregation, compiledCondition, queryName,
                        metaStreamEvent.getEventType());
                pupulateStoreQueryRuntime(storeQueryRuntime, metaStreamInfoHolder, storeQuery.getSelector(),
                        variableExpressionExecutors, siddhiAppContext, tableMap, queryName);
                return storeQueryRuntime;
            } else {
                Window window = windowMap.get(inputStore.getStoreId());
                if (window != null) {
                    metaStreamEvent.setEventType(EventType.WINDOW);
                    initMetaStreamEvent(metaStreamEvent, window.getWindowDefinition());
                    MatchingMetaInfoHolder metaStreamInfoHolder = generateMatchingMetaInfoHolder(metaStreamEvent,
                            window.getWindowDefinition());
                    CompiledCondition compiledCondition = window.compileCondition(onCondition,
                            generateMatchingMetaInfoHolder(metaStreamEvent, window.getWindowDefinition()),
                            siddhiAppContext, variableExpressionExecutors, tableMap, queryName);
                    StoreQueryRuntime storeQueryRuntime = new StoreQueryRuntime(window, compiledCondition, queryName,
                            metaStreamEvent.getEventType());
                    pupulateStoreQueryRuntime(storeQueryRuntime, metaStreamInfoHolder, storeQuery.getSelector(),
                            variableExpressionExecutors, siddhiAppContext, tableMap, queryName);
                    return storeQueryRuntime;
                } else {
                    throw new StoreQueryCreationException(inputStore.getStoreId() +
                            " is neither a table, aggregation or window");
                }
            }
        }
    }

    private static void pupulateStoreQueryRuntime(StoreQueryRuntime storeQueryRuntime,
                                                  MatchingMetaInfoHolder metaStreamInfoHolder,
                                                  Selector selector,
                                                  List<VariableExpressionExecutor> variableExpressionExecutors,
                                                  SiddhiAppContext siddhiAppContext, Map<String, Table> tableMap,
                                                  String queryName) {
        QuerySelector querySelector = SelectorParser.parse(selector,
                new ReturnStream(OutputStream.OutputEventType.CURRENT_EVENTS), siddhiAppContext,
                metaStreamInfoHolder.getMetaStateEvent(), tableMap, variableExpressionExecutors, queryName);
        QueryParserHelper.reduceMetaComplexEvent(metaStreamInfoHolder.getMetaStateEvent());
        QueryParserHelper.updateVariablePosition(metaStreamInfoHolder.getMetaStateEvent(),
                variableExpressionExecutors);
        querySelector.setEventPopulator(StateEventPopulatorFactory.constructEventPopulator(
                metaStreamInfoHolder.getMetaStateEvent()));
        storeQueryRuntime.setStateEventPool(new StateEventPool(metaStreamInfoHolder.getMetaStateEvent(), 5));
        storeQueryRuntime.setSelector(querySelector);
    }

    private static MatchingMetaInfoHolder generateMatchingMetaInfoHolder(MetaStreamEvent metaStreamEvent,
                                                                         AbstractDefinition definition) {
        MetaStateEvent metaStateEvent = new MetaStateEvent(1);
        metaStateEvent.addEvent(metaStreamEvent);
        return new MatchingMetaInfoHolder(metaStateEvent, 0, 0,
                definition, definition, 0);
    }

    private static void initMetaStreamEvent(MetaStreamEvent metaStreamEvent, AbstractDefinition inputDefinition) {
        metaStreamEvent.addInputDefinition(inputDefinition);
        metaStreamEvent.initializeAfterWindowData();
        for (Attribute attribute : inputDefinition.getAttributeList()) {
            metaStreamEvent.addData(attribute);
        }
    }

}
