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

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.executor.condition.ConditionExpressionExecutor;
import org.wso2.siddhi.core.query.selector.GroupByKeyGenerator;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.core.query.selector.attribute.processor.AttributeProcessor;
import org.wso2.siddhi.core.util.parser.helper.QueryParserHelper;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.execution.query.output.stream.OutputStream;
import org.wso2.siddhi.query.api.execution.query.selection.OutputAttribute;
import org.wso2.siddhi.query.api.execution.query.selection.Selector;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.ArrayList;
import java.util.List;

public class SelectorParser {

    /**
     * Parse Selector portion of a query and return corresponding QuerySelector
     *
     * @param selector       selector to be parsed
     * @param outStream      output stream of the query
     * @param context        query to be parsed
     * @param metaStateEvent Meta event used to collect execution info of stream associated with query
     * @param executors      List to hold VariableExpressionExecutors to update after query parsing
     * @return
     */
    public static QuerySelector parse(Selector selector, OutputStream outStream, SiddhiContext context, MetaStateEvent metaStateEvent, List<VariableExpressionExecutor> executors) {
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
        QuerySelector querySelector = new QuerySelector(id, selector, currentOn, expiredOn, context);
        querySelector.setAttributeProcessorList(getAttributeProcessors(selector, id, context, metaStateEvent, executors));

        ConditionExpressionExecutor havingCondition = generateHavingExecutor(selector.getHavingExpression(), metaStateEvent, context);
        querySelector.setHavingConditionExecutor(havingCondition);
        if (!selector.getGroupByList().isEmpty()) {
            querySelector.setGroupByKeyGenerator(new GroupByKeyGenerator(selector.getGroupByList(), metaStateEvent, executors, context));
        }


        return querySelector;
    }

    /**
     * Method to construct AttributeProcessor list for the selector
     *
     * @param selector
     * @param id
     * @param siddhiContext
     * @param metaStateEvent
     * @param executors
     * @return
     */
    private static List<AttributeProcessor> getAttributeProcessors(Selector selector, String id, SiddhiContext siddhiContext, MetaStateEvent metaStateEvent, List<VariableExpressionExecutor> executors) {
        List<AttributeProcessor> attributeProcessorList = new ArrayList<AttributeProcessor>();
        StreamDefinition temp = new StreamDefinition(id);

        int i = 0;
        for (OutputAttribute outputAttribute : selector.getSelectionList()) {

            if (metaStateEvent.getEventCount() == 1) {  //meta stream event
                MetaStreamEvent metaStreamEvent = metaStateEvent.getMetaEvent(0);
                ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(outputAttribute.getExpression(),
                        siddhiContext, metaStreamEvent, executors, !(selector.getGroupByList().isEmpty()));
                if (expressionExecutor instanceof VariableExpressionExecutor) {   //for variables we will directly put value at conversion stage
                    metaStreamEvent.addOutputData(((VariableExpressionExecutor) expressionExecutor).getAttribute());
                    temp.attribute(outputAttribute.getRename(), ((VariableExpressionExecutor) expressionExecutor).getAttribute().getType());
                } else {
                    metaStreamEvent.addOutputData(null);            //To maintain variable positions
                    AttributeProcessor attributeProcessor = new AttributeProcessor(expressionExecutor);
                    attributeProcessor.setOutputPosition(i);
                    attributeProcessorList.add(attributeProcessor);
                    temp.attribute(outputAttribute.getRename(), attributeProcessor.getOutputType());
                }

            } else {
                //TODO implement support for MetaStateEvent
            }
            i++;
        }
        metaStateEvent.setOutputDefinition(temp);
        return attributeProcessorList;
    }

    private static ConditionExpressionExecutor generateHavingExecutor(Expression expression,
                                                                      MetaStateEvent metaStateEvent, SiddhiContext siddhiContext) {
        List<VariableExpressionExecutor> executors = new ArrayList<VariableExpressionExecutor>();
        MetaStreamEvent metaEvent = new MetaStreamEvent();
        metaEvent.setInputDefinition(metaStateEvent.getOutputStreamDefinition());
        for (Attribute attribute : metaStateEvent.getOutputStreamDefinition().getAttributeList()) {
            metaEvent.addOutputData(attribute);
        }
        ConditionExpressionExecutor havingConditionExecutor = null;
        if (expression != null) {
            havingConditionExecutor = (ConditionExpressionExecutor) ExpressionParser.parseExpression(expression, siddhiContext, metaEvent, executors, false);

        }
        QueryParserHelper.updateVariablePosition(new MetaStateEvent(new MetaStreamEvent[]{metaEvent}), executors);
        return havingConditionExecutor;

    }
}
