/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.state.MetaStateEventAttribute;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.executor.condition.ConditionExpressionExecutor;
import org.wso2.siddhi.core.query.selector.GroupByKeyGenerator;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.core.query.selector.attribute.processor.AttributeProcessor;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.exception.DuplicateAttributeException;
import org.wso2.siddhi.query.api.execution.query.output.stream.OutputStream;
import org.wso2.siddhi.query.api.execution.query.selection.OutputAttribute;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;

import java.util.ArrayList;
import java.util.List;

public class SelectorParser {

    /**
     * Parse Selector portion of a query and return corresponding QuerySelector
     *
     * @param selector             selector to be parsed
     * @param outStream            output stream of the query
     * @param executionPlanContext query to be parsed
     * @param metaComplexEvent     Meta event used to collect execution info of stream associated with query
     * @param executors            List to hold VariableExpressionExecutors to update after query parsing
     * @return
     */
    public static QuerySelector parse(Selector selector, OutputStream outStream, ExecutionPlanContext executionPlanContext,
                                      MetaComplexEvent metaComplexEvent, List<VariableExpressionExecutor> executors) {
        boolean currentOn = false;
        boolean expiredOn = false;
        String id = null;

        if (outStream.getOutputEventType() == OutputStream.OutputEventType.CURRENT_EVENTS || outStream.getOutputEventType() == OutputStream.OutputEventType.ALL_EVENTS) {
            currentOn = true;
        }
        if (outStream.getOutputEventType() == OutputStream.OutputEventType.EXPIRED_EVENTS || outStream.getOutputEventType() == OutputStream.OutputEventType.ALL_EVENTS) {
            expiredOn = true;
        }

        id = outStream.getId();
        QuerySelector querySelector = new QuerySelector(id, selector, currentOn, expiredOn, executionPlanContext);
        querySelector.setAttributeProcessorList(getAttributeProcessors(selector, id, executionPlanContext, metaComplexEvent, executors));

        ConditionExpressionExecutor havingCondition = generateHavingExecutor(selector.getHavingExpression(),
                metaComplexEvent, executionPlanContext, executors);
        querySelector.setHavingConditionExecutor(havingCondition);
        if (!selector.getGroupByList().isEmpty()) {
            querySelector.setGroupByKeyGenerator(new GroupByKeyGenerator(selector.getGroupByList(), metaComplexEvent, executors, executionPlanContext));
        }


        return querySelector;
    }

    /**
     * Method to construct AttributeProcessor list for the selector
     *
     * @param selector
     * @param id
     * @param executionPlanContext
     * @param metaComplexEvent
     * @param executors
     * @return
     */
    private static List<AttributeProcessor> getAttributeProcessors(Selector selector, String id,
                                                                   ExecutionPlanContext executionPlanContext,
                                                                   MetaComplexEvent metaComplexEvent,
                                                                   List<VariableExpressionExecutor> executors) {

        List<AttributeProcessor> attributeProcessorList = new ArrayList<AttributeProcessor>();
        StreamDefinition outputDefinition = StreamDefinition.id(id);

        List<OutputAttribute> outputAttributes = selector.getSelectionList();
        if (selector.getSelectionList().size() == 0) {
            if (metaComplexEvent instanceof MetaStreamEvent) {

                List<Attribute> attributeList = ((MetaStreamEvent) metaComplexEvent).getInputDefinition().getAttributeList();
                for (Attribute attribute : attributeList) {
                    outputAttributes.add(new OutputAttribute(new Variable(attribute.getName())));
                }
            } else {
                for (MetaStreamEvent metaStreamEvent : ((MetaStateEvent) metaComplexEvent).getMetaStreamEvents()) {
                    List<Attribute> attributeList = metaStreamEvent.getInputDefinition().getAttributeList();
                    for (Attribute attribute : attributeList) {
                        OutputAttribute outputAttribute = new OutputAttribute(new Variable(attribute.getName()));
                        if (!outputAttributes.contains(outputAttribute)) {
                            outputAttributes.add(outputAttribute);
                        } else {
                            List<AbstractDefinition> definitions = new ArrayList<AbstractDefinition>();
                            for (MetaStreamEvent aMetaStreamEvent : ((MetaStateEvent) metaComplexEvent).getMetaStreamEvents()) {
                                definitions.add(aMetaStreamEvent.getInputDefinition());
                            }
                            throw new DuplicateAttributeException("Duplicate attribute exist in streams " + definitions);
                        }
                    }
                }

            }
        }

        int i = 0;
        for (OutputAttribute outputAttribute :outputAttributes) {

            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(outputAttribute.getExpression(),
                    metaComplexEvent, SiddhiConstants.UNKNOWN_STATE, executors, executionPlanContext,
                    !(selector.getGroupByList().isEmpty()), 0);
            if (expressionExecutor instanceof VariableExpressionExecutor) {   //for variables we will directly put value at conversion stage
                VariableExpressionExecutor executor = ((VariableExpressionExecutor) expressionExecutor);
                if (metaComplexEvent instanceof MetaStateEvent) {
                    ((MetaStateEvent) metaComplexEvent).addOutputData(new MetaStateEventAttribute(executor.getAttribute(), executor.getPosition()));
                } else {
                    ((MetaStreamEvent) metaComplexEvent).addOutputData(executor.getAttribute());
                }
                outputDefinition.attribute(outputAttribute.getRename(), ((VariableExpressionExecutor) expressionExecutor).getAttribute().getType());
            } else {
                //To maintain output variable positions
                if (metaComplexEvent instanceof MetaStateEvent) {
                    ((MetaStateEvent) metaComplexEvent).addOutputData(null);
                } else {
                    ((MetaStreamEvent) metaComplexEvent).addOutputData(null);
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
                                                                      ExecutionPlanContext executionPlanContext,
                                                                      List<VariableExpressionExecutor> executors) {
        ConditionExpressionExecutor havingConditionExecutor = null;
        if (expression != null) {
            havingConditionExecutor = (ConditionExpressionExecutor) ExpressionParser.parseExpression(expression,
                    metaComplexEvent, SiddhiConstants.HAVING_STATE, executors, executionPlanContext, false, 0);

        }
        return havingConditionExecutor;
    }
}
