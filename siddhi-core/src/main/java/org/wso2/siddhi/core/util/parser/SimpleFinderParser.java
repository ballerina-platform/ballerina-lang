/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.finder.SimpleFinder;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.List;

/**
 * Created on 1/19/15.
 */
public class SimpleFinderParser {
    public static SimpleFinder parse(Expression expression, MetaStateEvent metaStateEvent, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> executorList, AbstractDefinition candidateDefinition) {
        MetaStreamEvent[] metaStreamEvents = metaStateEvent.getMetaStreamEvents();
        int candidateEventPosition = 0;
        for (; candidateEventPosition < metaStreamEvents.length; candidateEventPosition++) {
            MetaStreamEvent metaStreamEvent = metaStreamEvents[candidateEventPosition];
            if (metaStreamEvent.getInputDefinition().equalsIgnoreAnnotations(candidateDefinition)) {
                break;
            }
        }
        int matchingEventPosition = 0;
        if (candidateEventPosition == 0) {
            matchingEventPosition = 1;
        }
        ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                metaStateEvent, SiddhiConstants.UNKNOWN_STATE, executorList, executionPlanContext, false, 0);
        return new SimpleFinder(expressionExecutor, candidateEventPosition, matchingEventPosition);
    }
}
