/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.event.MetaComplexEvent;
import org.ballerinalang.siddhi.core.event.state.MetaStateEvent;
import org.ballerinalang.siddhi.core.event.state.MetaStateEventAttribute;
import org.ballerinalang.siddhi.core.event.stream.MetaStreamEvent;
import org.ballerinalang.siddhi.core.executor.ConstantExpressionExecutor;
import org.ballerinalang.siddhi.core.executor.ExpressionExecutor;
import org.ballerinalang.siddhi.core.executor.VariableExpressionExecutor;
import org.ballerinalang.siddhi.core.executor.condition.ConditionExpressionExecutor;
import org.ballerinalang.siddhi.core.query.selector.GroupByKeyGenerator;
import org.ballerinalang.siddhi.core.query.selector.OrderByEventComparator;
import org.ballerinalang.siddhi.core.query.selector.QuerySelector;
import org.ballerinalang.siddhi.core.query.selector.attribute.processor.AttributeProcessor;
import org.ballerinalang.siddhi.core.table.Table;
import org.ballerinalang.siddhi.core.util.SiddhiConstants;
import org.ballerinalang.siddhi.query.api.definition.AbstractDefinition;
import org.ballerinalang.siddhi.query.api.definition.Attribute;
import org.ballerinalang.siddhi.query.api.definition.StreamDefinition;
import org.ballerinalang.siddhi.query.api.exception.DuplicateAttributeException;
import org.ballerinalang.siddhi.query.api.execution.query.output.stream.OutputStream;
import org.ballerinalang.siddhi.query.api.execution.query.selection.OutputAttribute;
import org.ballerinalang.siddhi.query.api.execution.query.selection.Selector;
import org.ballerinalang.siddhi.query.api.expression.Expression;
import org.ballerinalang.siddhi.query.api.expression.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class to parse {@link QuerySelector}.
 */
public class SelectorParser {
    private static final ThreadLocal<String> containsAggregatorThreadLocal = new ThreadLocal<String>();

    /**
     * Parse Selector portion of a query and return corresponding QuerySelector.
     *
     * @param selector                    selector to be parsed
     * @param outputStream                output stream
     * @param siddhiAppContext            query to be parsed
     * @param metaComplexEvent            Meta event used to collect execution info of stream associated with query
     * @param tableMap                    Table Map
     * @param variableExpressionExecutors variable expression executors
     * @param queryName                   query name of selector belongs to.
     * @param metaPosition                helps to identify the meta position of aggregates
     * @return QuerySelector
     */
    public static QuerySelector parse(Selector selector, OutputStream outputStream, SiddhiAppContext siddhiAppContext,
                                      MetaComplexEvent metaComplexEvent, Map<String, Table> tableMap,
                                      List<VariableExpressionExecutor> variableExpressionExecutors, String queryName,
                                      int metaPosition) {
        boolean currentOn = false;
        boolean expiredOn = false;
        String id = null;

        if (outputStream.getOutputEventType() == OutputStream.OutputEventType.CURRENT_EVENTS || outputStream
                .getOutputEventType() == OutputStream.OutputEventType.ALL_EVENTS) {
            currentOn = true;
        }
        if (outputStream.getOutputEventType() == OutputStream.OutputEventType.EXPIRED_EVENTS || outputStream
                .getOutputEventType() == OutputStream.OutputEventType.ALL_EVENTS) {
            expiredOn = true;
        }

        id = outputStream.getId();
        containsAggregatorThreadLocal.remove();
        QuerySelector querySelector = new QuerySelector(id, selector, currentOn, expiredOn, siddhiAppContext);
        List<AttributeProcessor> attributeProcessors = getAttributeProcessors(selector, id, siddhiAppContext,
                metaComplexEvent, tableMap, variableExpressionExecutors, outputStream, queryName, metaPosition);
        querySelector.setAttributeProcessorList(attributeProcessors, "true".equals(containsAggregatorThreadLocal.
                get()));
        containsAggregatorThreadLocal.remove();
        ConditionExpressionExecutor havingCondition = generateHavingExecutor(selector.getHavingExpression(),
                metaComplexEvent, siddhiAppContext, tableMap, variableExpressionExecutors, queryName);
        querySelector.setHavingConditionExecutor(havingCondition, "true".equals(containsAggregatorThreadLocal.get()));
        containsAggregatorThreadLocal.remove();
        if (!selector.getGroupByList().isEmpty()) {
            querySelector.setGroupByKeyGenerator(new GroupByKeyGenerator(selector.getGroupByList(), metaComplexEvent,
                    SiddhiConstants.UNKNOWN_STATE, null, variableExpressionExecutors, siddhiAppContext,
                    queryName));
        }
        if (!selector.getOrderByList().isEmpty()) {
            querySelector.setOrderByEventComparator(new OrderByEventComparator(selector.getOrderByList(),
                    metaComplexEvent, SiddhiConstants.HAVING_STATE, null, variableExpressionExecutors,
                    siddhiAppContext,
                    queryName));
        }
        if (selector.getLimit() != null) {
            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression((Expression) selector.getLimit(),
                    metaComplexEvent, SiddhiConstants.HAVING_STATE, tableMap, variableExpressionExecutors,
                    siddhiAppContext, false, 0, queryName);
            containsAggregatorThreadLocal.remove();
            querySelector.setLimit(((Number)
                    (((ConstantExpressionExecutor) expressionExecutor).getValue())).longValue());
        }
        return querySelector;
    }

    /**
     * Method to construct AttributeProcessor list for the selector.
     *
     * @param selector                    Selector
     * @param id                          stream id
     * @param siddhiAppContext            siddhi app context
     * @param metaComplexEvent            meta ComplexEvent
     * @param tableMap                    Table Map
     * @param variableExpressionExecutors list of VariableExpressionExecutors
     * @param outputStream
     * @return list of AttributeProcessors
     */
    private static List<AttributeProcessor> getAttributeProcessors(Selector selector, String id,
                                                                   SiddhiAppContext siddhiAppContext,
                                                                   MetaComplexEvent metaComplexEvent,
                                                                   Map<String, Table> tableMap,
                                                                   List<VariableExpressionExecutor>
                                                                           variableExpressionExecutors,
                                                                   OutputStream outputStream,
                                                                   String queryName, int metaPosition) {

        List<AttributeProcessor> attributeProcessorList = new ArrayList<>();
        StreamDefinition outputDefinition = StreamDefinition.id(id);
        outputDefinition.setQueryContextStartIndex(outputStream.getQueryContextStartIndex());
        outputDefinition.setQueryContextEndIndex(outputStream.getQueryContextEndIndex());
        List<OutputAttribute> outputAttributes = selector.getSelectionList();
        if (selector.getSelectionList().size() == 0) {
            if (metaComplexEvent instanceof MetaStreamEvent) {

                List<Attribute> attributeList = ((MetaStreamEvent) metaComplexEvent).getLastInputDefinition()
                        .getAttributeList();
                for (Attribute attribute : attributeList) {
                    outputAttributes.add(new OutputAttribute(new Variable(attribute.getName())));
                }
            } else {
                int position = 0;
                for (MetaStreamEvent metaStreamEvent : ((MetaStateEvent) metaComplexEvent).getMetaStreamEvents()) {
                    if (metaPosition == SiddhiConstants.UNKNOWN_STATE || metaPosition == position) {
                        List<Attribute> attributeList = metaStreamEvent.getLastInputDefinition().getAttributeList();
                        for (Attribute attribute : attributeList) {
                            OutputAttribute outputAttribute = new OutputAttribute(new Variable(attribute.getName()));
                            if (!outputAttributes.contains(outputAttribute)) {
                                outputAttributes.add(outputAttribute);
                            } else {
                                List<AbstractDefinition> definitions = new ArrayList<>();
                                for (MetaStreamEvent aMetaStreamEvent : ((MetaStateEvent) metaComplexEvent)
                                        .getMetaStreamEvents()) {
                                    definitions.add(aMetaStreamEvent.getLastInputDefinition());
                                }
                                throw new DuplicateAttributeException("Duplicate attribute exist in streams " +
                                        definitions, outputStream.getQueryContextStartIndex(),
                                        outputStream.getQueryContextEndIndex());
                            }
                        }
                    }
                    ++position;
                }
            }
        }

        int i = 0;
        for (OutputAttribute outputAttribute : outputAttributes) {

            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(outputAttribute.getExpression(),
                    metaComplexEvent, SiddhiConstants.UNKNOWN_STATE, tableMap, variableExpressionExecutors,
                    siddhiAppContext,
                    !(selector.getGroupByList().isEmpty()), 0, queryName);
            if (expressionExecutor instanceof VariableExpressionExecutor) {   //for variables we will directly put
                // value at conversion stage
                VariableExpressionExecutor executor = ((VariableExpressionExecutor) expressionExecutor);
                if (metaComplexEvent instanceof MetaStateEvent) {
                    ((MetaStateEvent) metaComplexEvent).
                            addOutputDataAllowingDuplicate(new MetaStateEventAttribute(executor.getAttribute(),
                                    executor.getPosition()));
                } else {
                    ((MetaStreamEvent) metaComplexEvent).addOutputDataAllowingDuplicate(executor.getAttribute());
                }
                outputDefinition.attribute(outputAttribute.getRename(), ((VariableExpressionExecutor)
                        expressionExecutor).getAttribute().getType());
            } else {
                //To maintain output variable positions
                if (metaComplexEvent instanceof MetaStateEvent) {
                    ((MetaStateEvent) metaComplexEvent).addOutputDataAllowingDuplicate(null);
                } else {
                    ((MetaStreamEvent) metaComplexEvent).addOutputDataAllowingDuplicate(null);
                }
                AttributeProcessor attributeProcessor = new AttributeProcessor(expressionExecutor);
                attributeProcessor.setOutputPosition(i);
                attributeProcessorList.add(attributeProcessor);
                outputDefinition.attribute(outputAttribute.getRename(), attributeProcessor.getOutputType());
            }
            i++;
        }
        metaComplexEvent.setOutputDefinition(outputDefinition);
        return attributeProcessorList;
    }

    private static ConditionExpressionExecutor generateHavingExecutor(Expression expression,
                                                                      MetaComplexEvent metaComplexEvent,
                                                                      SiddhiAppContext siddhiAppContext,
                                                                      Map<String, Table> tableMap,
                                                                      List<VariableExpressionExecutor>
                                                                              variableExpressionExecutors, String
                                                                              queryName) {
        ConditionExpressionExecutor havingConditionExecutor = null;
        if (expression != null) {
            havingConditionExecutor = (ConditionExpressionExecutor) ExpressionParser.parseExpression(expression,
                    metaComplexEvent, SiddhiConstants.HAVING_STATE, tableMap, variableExpressionExecutors,
                    siddhiAppContext, false, 0, queryName);

        }
        return havingConditionExecutor;
    }

    public static ThreadLocal<String> getContainsAggregatorThreadLocal() {
        return containsAggregatorThreadLocal;
    }
}
