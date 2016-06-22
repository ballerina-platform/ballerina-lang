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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.query.selector.attribute.processor.executor;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;

import java.util.HashMap;
import java.util.Map;

public class GroupByAggregationAttributeExecutor extends AbstractAggregationAttributeExecutor {

    protected Map<String, AttributeAggregator> aggregatorMap = new HashMap<String, AttributeAggregator>();

    public GroupByAggregationAttributeExecutor(AttributeAggregator attributeAggregator,
                                               ExpressionExecutor[] attributeExpressionExecutors,
                                               ExecutionPlanContext executionPlanContext) {
        super(attributeAggregator, attributeExpressionExecutors, executionPlanContext);
    }

    @Override
    public Object execute(ComplexEvent event) {
        if (event.getType() == ComplexEvent.Type.RESET) {
            Object aOutput = null;
            for (AttributeAggregator attributeAggregator : aggregatorMap.values()) {
                aOutput = attributeAggregator.process(event);
            }
            return aOutput;
        }
        String key = QuerySelector.getThreadLocalGroupByKey();
        AttributeAggregator currentAttributeAggregator = aggregatorMap.get(key);
        if (currentAttributeAggregator == null) {
            currentAttributeAggregator = attributeAggregator.cloneAggregator(key);
            currentAttributeAggregator.initAggregator(attributeExpressionExecutors, executionPlanContext);
            currentAttributeAggregator.start();
            aggregatorMap.put(key, currentAttributeAggregator);
        }
        return currentAttributeAggregator.process(event);
    }

    public ExpressionExecutor cloneExecutor(String key) {
        return new GroupByAggregationAttributeExecutor(attributeAggregator.cloneAggregator(key), attributeExpressionExecutors, executionPlanContext);
    }


    @Override
    public Object[] currentState() {
        HashMap<String, Object[]> data = new HashMap<String, Object[]>();
        for (Map.Entry<String, AttributeAggregator> entry : aggregatorMap.entrySet()) {
            data.put(entry.getKey(), entry.getValue().currentState());
        }
        return new Object[]{data};
    }

    @Override
    public void restoreState(Object[] state) {
        HashMap<String, Object[]> data = (HashMap<String, Object[]>) state[0];

        for (Map.Entry<String, Object[]> entry : data.entrySet()) {
            String key = entry.getKey();
            AttributeAggregator aAttributeAggregator = attributeAggregator.cloneAggregator(key);
            aAttributeAggregator.initAggregator(attributeExpressionExecutors, executionPlanContext);
            aAttributeAggregator.start();
            aAttributeAggregator.restoreState(entry.getValue());
            aggregatorMap.put(key, aAttributeAggregator);
        }
    }
}
