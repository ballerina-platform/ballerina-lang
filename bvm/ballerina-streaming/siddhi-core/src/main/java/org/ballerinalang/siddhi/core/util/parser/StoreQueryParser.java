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

package org.ballerinalang.siddhi.core.util.parser;

import org.ballerinalang.siddhi.core.aggregation.AggregationRuntime;
import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.event.state.MetaStateEvent;
import org.ballerinalang.siddhi.core.event.state.StateEventPool;
import org.ballerinalang.siddhi.core.event.state.populater.StateEventPopulatorFactory;
import org.ballerinalang.siddhi.core.event.stream.MetaStreamEvent;
import org.ballerinalang.siddhi.core.event.stream.MetaStreamEvent.EventType;
import org.ballerinalang.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.ballerinalang.siddhi.core.event.stream.populater.StreamEventPopulaterFactory;
import org.ballerinalang.siddhi.core.exception.StoreQueryCreationException;
import org.ballerinalang.siddhi.core.executor.VariableExpressionExecutor;
import org.ballerinalang.siddhi.core.query.FindStoreQueryRuntime;
import org.ballerinalang.siddhi.core.query.SelectStoreQueryRuntime;
import org.ballerinalang.siddhi.core.query.StoreQueryRuntime;
import org.ballerinalang.siddhi.core.query.processor.stream.window.QueryableProcessor;
import org.ballerinalang.siddhi.core.query.selector.QuerySelector;
import org.ballerinalang.siddhi.core.table.Table;
import org.ballerinalang.siddhi.core.util.SiddhiConstants;
import org.ballerinalang.siddhi.core.util.collection.operator.CompiledCondition;
import org.ballerinalang.siddhi.core.util.collection.operator.CompiledSelection;
import org.ballerinalang.siddhi.core.util.collection.operator.IncrementalAggregateCompileCondition;
import org.ballerinalang.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.ballerinalang.siddhi.core.util.parser.helper.QueryParserHelper;
import org.ballerinalang.siddhi.core.util.snapshot.SnapshotService;
import org.ballerinalang.siddhi.core.window.Window;
import org.ballerinalang.siddhi.query.api.aggregation.Within;
import org.ballerinalang.siddhi.query.api.definition.AbstractDefinition;
import org.ballerinalang.siddhi.query.api.definition.Attribute;
import org.ballerinalang.siddhi.query.api.execution.query.StoreQuery;
import org.ballerinalang.siddhi.query.api.execution.query.input.store.AggregationInputStore;
import org.ballerinalang.siddhi.query.api.execution.query.input.store.ConditionInputStore;
import org.ballerinalang.siddhi.query.api.execution.query.input.store.InputStore;
import org.ballerinalang.siddhi.query.api.execution.query.output.stream.OutputStream;
import org.ballerinalang.siddhi.query.api.execution.query.output.stream.ReturnStream;
import org.ballerinalang.siddhi.query.api.execution.query.selection.Selector;
import org.ballerinalang.siddhi.query.api.expression.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class to parse {@link StoreQueryRuntime}.
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
                                          Map<String, Table> tableMap, Map<String, Window> windowMap,
                                          Map<String, AggregationRuntime> aggregationMap) {
        String queryName = "store_query_" + storeQuery.getInputStore().getStoreId();
        InputStore inputStore = storeQuery.getInputStore();
        int metaPosition = SiddhiConstants.UNKNOWN_STATE;
        Within within = null;
        Expression per = null;
        try {
            SnapshotService.getSkipSnapshotableThreadLocal().set(true);
            Expression onCondition = Expression.value(true);
            MetaStreamEvent metaStreamEvent = new MetaStreamEvent();
            metaStreamEvent.setInputReferenceId(inputStore.getStoreReferenceId());

            if (inputStore instanceof AggregationInputStore) {
                AggregationInputStore aggregationInputStore = (AggregationInputStore) inputStore;
                if (aggregationMap.get(inputStore.getStoreId()) == null) {
                    throw new StoreQueryCreationException("Aggregation \"" + inputStore.getStoreId() +
                            "\" has not been defined");
                }
                if (aggregationInputStore.getPer() != null && aggregationInputStore.getWithin() != null) {
                    within = aggregationInputStore.getWithin();
                    per = aggregationInputStore.getPer();
                } else if (aggregationInputStore.getPer() != null || aggregationInputStore.getWithin() != null) {
                    throw new StoreQueryCreationException(
                            inputStore.getStoreId() + " should either have both 'within' and 'per' defined or none.");
                }
                if (((AggregationInputStore) inputStore).getOnCondition() != null) {
                    onCondition = ((AggregationInputStore) inputStore).getOnCondition();
                }
            } else if (inputStore instanceof ConditionInputStore) {
                if (((ConditionInputStore) inputStore).getOnCondition() != null) {
                    onCondition = ((ConditionInputStore) inputStore).getOnCondition();
                }
            }
            List<VariableExpressionExecutor> variableExpressionExecutors = new ArrayList<>();
            Table table = tableMap.get(inputStore.getStoreId());
            if (table != null) {
                return constructStoreQueryRuntime(table, storeQuery, siddhiAppContext, tableMap, queryName,
                        metaPosition, onCondition, metaStreamEvent, variableExpressionExecutors);
            } else {
                AggregationRuntime aggregation = aggregationMap.get(inputStore.getStoreId());
                if (aggregation != null) {
                    return constructStoreQueryRuntime(aggregation, storeQuery, siddhiAppContext, tableMap, queryName,
                            within, per, onCondition, metaStreamEvent, variableExpressionExecutors);
                } else {
                    Window window = windowMap.get(inputStore.getStoreId());
                    if (window != null) {
                        return constructStoreQueryRuntime(window, storeQuery, siddhiAppContext, tableMap, queryName,
                                metaPosition, onCondition, metaStreamEvent, variableExpressionExecutors);
                    } else {
                        throw new StoreQueryCreationException(
                                inputStore.getStoreId() + " is neither a table, aggregation or window");
                    }
                }
            }
        } finally {
            SnapshotService.getSkipSnapshotableThreadLocal().set(null);
        }
    }

    private static StoreQueryRuntime constructStoreQueryRuntime(
            Window window, StoreQuery storeQuery,
            SiddhiAppContext siddhiAppContext, Map<String, Table> tableMap, String queryName, int metaPosition,
            Expression onCondition, MetaStreamEvent metaStreamEvent,
            List<VariableExpressionExecutor> variableExpressionExecutors) {
        metaStreamEvent.setEventType(EventType.WINDOW);
        initMetaStreamEvent(metaStreamEvent, window.getWindowDefinition());
        MatchingMetaInfoHolder metaStreamInfoHolder = generateMatchingMetaInfoHolder(metaStreamEvent,
                window.getWindowDefinition());
        CompiledCondition compiledCondition = window.compileCondition(onCondition,
                generateMatchingMetaInfoHolder(metaStreamEvent, window.getWindowDefinition()),
                siddhiAppContext, variableExpressionExecutors, tableMap, queryName);
        FindStoreQueryRuntime findStoreQueryRuntime = new FindStoreQueryRuntime(window, compiledCondition,
                queryName, metaStreamEvent);
        populateFindStoreQueryRuntime(findStoreQueryRuntime, metaStreamInfoHolder, storeQuery.getSelector(),
                variableExpressionExecutors, siddhiAppContext, tableMap, queryName, metaPosition);
        return findStoreQueryRuntime;
    }

    private static StoreQueryRuntime constructStoreQueryRuntime(AggregationRuntime aggregation, StoreQuery storeQuery,
                                                                SiddhiAppContext siddhiAppContext,
                                                                Map<String, Table> tableMap, String queryName,
                                                                Within within, Expression per, Expression onCondition,
                                                                MetaStreamEvent metaStreamEvent,
                                                                List<VariableExpressionExecutor>
                                                                        variableExpressionExecutors) {
        int metaPosition;
        metaStreamEvent.setEventType(EventType.AGGREGATE);
        initMetaStreamEvent(metaStreamEvent, aggregation.getAggregationDefinition());
        MatchingMetaInfoHolder metaStreamInfoHolder = generateMatchingMetaInfoHolder(metaStreamEvent,
                aggregation.getAggregationDefinition());
        CompiledCondition compiledCondition = aggregation.compileExpression(onCondition, within, per,
                metaStreamInfoHolder, variableExpressionExecutors, tableMap, queryName, siddhiAppContext);
        metaStreamInfoHolder = ((IncrementalAggregateCompileCondition) compiledCondition).
                getAlteredMatchingMetaInfoHolder();
        FindStoreQueryRuntime findStoreQueryRuntime = new FindStoreQueryRuntime(aggregation, compiledCondition,
                queryName, metaStreamEvent);
        metaPosition = 1;
        populateFindStoreQueryRuntime(findStoreQueryRuntime, metaStreamInfoHolder,
                storeQuery.getSelector(), variableExpressionExecutors, siddhiAppContext, tableMap,
                queryName, metaPosition);
        ComplexEventPopulater complexEventPopulater = StreamEventPopulaterFactory.constructEventPopulator(
                metaStreamInfoHolder.getMetaStateEvent().getMetaStreamEvent(0), 0,
                ((IncrementalAggregateCompileCondition) compiledCondition).getAdditionalAttributes());
        ((IncrementalAggregateCompileCondition) compiledCondition)
                .setComplexEventPopulater(complexEventPopulater);
        return findStoreQueryRuntime;
    }

    private static StoreQueryRuntime constructStoreQueryRuntime(Table table, StoreQuery storeQuery,
                                                                SiddhiAppContext siddhiAppContext,
                                                                Map<String, Table> tableMap, String queryName,
                                                                int metaPosition, Expression onCondition,
                                                                MetaStreamEvent metaStreamEvent,
                                                                List<VariableExpressionExecutor>
                                                                        variableExpressionExecutors) {
        metaStreamEvent.setEventType(EventType.TABLE);
        initMetaStreamEvent(metaStreamEvent, table.getTableDefinition());
        MatchingMetaInfoHolder metaStreamInfoHolder = generateMatchingMetaInfoHolder(metaStreamEvent,
                table.getTableDefinition());
        CompiledCondition compiledCondition = table.compileCondition(onCondition, metaStreamInfoHolder,
                siddhiAppContext, variableExpressionExecutors, tableMap, queryName);
        if (table instanceof QueryableProcessor) {
            List<Attribute> expectedOutputAttributes = buildExpectedOutputAttributes(storeQuery, siddhiAppContext,
                    tableMap, queryName, metaPosition, metaStreamInfoHolder);
            CompiledSelection compiledSelection = ((QueryableProcessor) table).compileSelection(
                    storeQuery.getSelector(), expectedOutputAttributes, metaStreamInfoHolder, siddhiAppContext,
                    variableExpressionExecutors, tableMap, queryName);
            SelectStoreQueryRuntime storeQueryRuntime = new SelectStoreQueryRuntime((QueryableProcessor) table,
                    compiledCondition, compiledSelection, expectedOutputAttributes, queryName);
            QueryParserHelper.reduceMetaComplexEvent(metaStreamInfoHolder.getMetaStateEvent());
            QueryParserHelper.updateVariablePosition(metaStreamInfoHolder.getMetaStateEvent(),
                    variableExpressionExecutors);
            return storeQueryRuntime;
        } else {
            FindStoreQueryRuntime storeQueryRuntime = new FindStoreQueryRuntime(table, compiledCondition, queryName,
                    metaStreamEvent);
            populateFindStoreQueryRuntime(storeQueryRuntime, metaStreamInfoHolder, storeQuery.getSelector(),
                    variableExpressionExecutors, siddhiAppContext, tableMap, queryName, metaPosition);
            return storeQueryRuntime;
        }
    }

    private static List<Attribute> buildExpectedOutputAttributes(
            StoreQuery storeQuery, SiddhiAppContext siddhiAppContext, Map<String, Table> tableMap,
            String queryName, int metaPosition, MatchingMetaInfoHolder metaStreamInfoHolder) {
        MetaStateEvent selectMetaStateEvent =
                new MetaStateEvent(metaStreamInfoHolder.getMetaStateEvent().getMetaStreamEvents());
        SelectorParser.parse(storeQuery.getSelector(),
                new ReturnStream(OutputStream.OutputEventType.CURRENT_EVENTS), siddhiAppContext,
                selectMetaStateEvent, tableMap, new ArrayList<>(), queryName,
                metaPosition);
        return selectMetaStateEvent.getOutputStreamDefinition().getAttributeList();
    }

    private static void populateFindStoreQueryRuntime(FindStoreQueryRuntime findStoreQueryRuntime,
                                                      MatchingMetaInfoHolder metaStreamInfoHolder, Selector selector,
                                                      List<VariableExpressionExecutor> variableExpressionExecutors,
                                                      SiddhiAppContext siddhiAppContext,
                                                      Map<String, Table> tableMap,
                                                      String queryName, int metaPosition) {
        QuerySelector querySelector = SelectorParser.parse(selector,
                new ReturnStream(OutputStream.OutputEventType.CURRENT_EVENTS), siddhiAppContext,
                metaStreamInfoHolder.getMetaStateEvent(), tableMap, variableExpressionExecutors, queryName,
                metaPosition);
        QueryParserHelper.reduceMetaComplexEvent(metaStreamInfoHolder.getMetaStateEvent());
        QueryParserHelper.updateVariablePosition(metaStreamInfoHolder.getMetaStateEvent(), variableExpressionExecutors);
        querySelector.setEventPopulator(
                StateEventPopulatorFactory.constructEventPopulator(metaStreamInfoHolder.getMetaStateEvent()));
        findStoreQueryRuntime.setStateEventPool(new StateEventPool(metaStreamInfoHolder.getMetaStateEvent(), 5));
        findStoreQueryRuntime.setSelector(querySelector);
        findStoreQueryRuntime.setOutputAttributes(metaStreamInfoHolder.getMetaStateEvent().
                getOutputStreamDefinition().getAttributeList());
    }

    private static MatchingMetaInfoHolder generateMatchingMetaInfoHolder(MetaStreamEvent metaStreamEvent,
                                                                         AbstractDefinition definition) {
        MetaStateEvent metaStateEvent = new MetaStateEvent(1);
        metaStateEvent.addEvent(metaStreamEvent);
        return new MatchingMetaInfoHolder(metaStateEvent, -1, 0, definition,
                definition, 0);
    }

    private static void initMetaStreamEvent(MetaStreamEvent metaStreamEvent, AbstractDefinition inputDefinition) {
        metaStreamEvent.addInputDefinition(inputDefinition);
        metaStreamEvent.initializeAfterWindowData();
        inputDefinition.getAttributeList().forEach(metaStreamEvent::addData);
    }

}
