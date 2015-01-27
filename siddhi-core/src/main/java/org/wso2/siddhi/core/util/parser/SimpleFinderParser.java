/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.util.finder.Finder;
import org.wso2.siddhi.core.util.finder.SimpleFinder;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.List;
import java.util.Map;

/**
 * Created on 1/19/15.
 */
public class SimpleFinderParser {

    public static Finder parse(Expression expression, MetaComplexEvent metaComplexEvent, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors,
                               Map<String, EventTable> eventTableMap, int matchingStreamIndex, AbstractDefinition candidateDefinition) {

        int candidateEventPosition = 0;
        int size = 0;

        MetaStreamEvent eventTableStreamEvent = new MetaStreamEvent();
        eventTableStreamEvent.setTableEvent(true);
        eventTableStreamEvent.addInputDefinition(candidateDefinition);
        for (Attribute attribute : candidateDefinition.getAttributeList()) {
            eventTableStreamEvent.addOutputData(attribute);
        }

        MetaStateEvent metaStateEvent = null;
        if (metaComplexEvent instanceof MetaStreamEvent) {
            metaStateEvent = new MetaStateEvent(2);
            metaStateEvent.addEvent(((MetaStreamEvent) metaComplexEvent));
            metaStateEvent.addEvent(eventTableStreamEvent);
            candidateEventPosition = 1;
            matchingStreamIndex = 0;
            size = 2;
        } else {

            MetaStreamEvent[] metaStreamEvents = ((MetaStateEvent) metaComplexEvent).getMetaStreamEvents();

            //for join
            for (; candidateEventPosition < metaStreamEvents.length; candidateEventPosition++) {
                MetaStreamEvent metaStreamEvent = metaStreamEvents[candidateEventPosition];
                if (metaStreamEvent.getLastInputDefinition().equalsIgnoreAnnotations(candidateDefinition)) {
                    metaStateEvent = ((MetaStateEvent) metaComplexEvent);
                    size = metaStreamEvents.length;
                    break;
                }
            }

            if (metaStateEvent == null) {
                metaStateEvent = new MetaStateEvent(metaStreamEvents.length + 1);
                for (MetaStreamEvent metaStreamEvent : metaStreamEvents) {
                    metaStateEvent.addEvent(metaStreamEvent);
                }
                metaStateEvent.addEvent(eventTableStreamEvent);
                candidateEventPosition = metaStreamEvents.length;
                size = metaStreamEvents.length + 1;
            }
        }

        ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                metaStateEvent, matchingStreamIndex, eventTableMap, variableExpressionExecutors, executionPlanContext, false, 0);
        return new SimpleFinder(expressionExecutor, candidateEventPosition, matchingStreamIndex, size);
    }
}
