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
package org.wso2.siddhi.core.query.selector.attribute.processor.executor;

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;
import org.wso2.siddhi.core.util.config.ConfigReader;

import java.util.HashMap;
import java.util.Map;

/**
 * Executor class for aggregations with group by configuration.
 */
public class GroupByAggregationAttributeExecutor extends AbstractAggregationAttributeExecutor {

    private static final ThreadLocal<String> keyThreadLocal = new ThreadLocal<String>();
    private final ConfigReader configReader;
    protected Map<String, AttributeAggregator> aggregatorMap = new HashMap<String, AttributeAggregator>();

    public GroupByAggregationAttributeExecutor(AttributeAggregator attributeAggregator,
                                               ExpressionExecutor[] attributeExpressionExecutors,
                                               ConfigReader configReader, SiddhiAppContext siddhiAppContext,
                                               String queryName) {
        super(attributeAggregator, attributeExpressionExecutors, siddhiAppContext, queryName);
        this.configReader = configReader;
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
        String key = keyThreadLocal.get();
        AttributeAggregator currentAttributeAggregator = aggregatorMap.get(key);
        if (currentAttributeAggregator == null) {
            currentAttributeAggregator = attributeAggregator.cloneAggregator(key);
            currentAttributeAggregator.initAggregator(attributeExpressionExecutors, siddhiAppContext, configReader);
            currentAttributeAggregator.start();
            aggregatorMap.put(key, currentAttributeAggregator);
        }
        return currentAttributeAggregator.process(event);
    }

    public ExpressionExecutor cloneExecutor(String key) {
        return new GroupByAggregationAttributeExecutor(attributeAggregator.cloneAggregator(key),

                                                       attributeExpressionExecutors, configReader, siddhiAppContext,
                                                       queryName);
    }

    @Override
    public Map<String, Object> currentState() {
        HashMap<String, Map<String, Object>> data = new HashMap<>();
        for (Map.Entry<String, AttributeAggregator> entry : aggregatorMap.entrySet()) {
            data.put(entry.getKey(), entry.getValue().currentState());
        }
        Map<String, Object> state = new HashMap<>();
        state.put("Data", data);
        return state;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        HashMap<String, Map<String, Object>> data = (HashMap<String, Map<String, Object>>) state.get("Data");

        for (Map.Entry<String, Map<String, Object>> entry : data.entrySet()) {
            String key = entry.getKey();
            AttributeAggregator aAttributeAggregator = attributeAggregator.cloneAggregator(key);
            aAttributeAggregator.initAggregator(attributeExpressionExecutors, siddhiAppContext, configReader);
            aAttributeAggregator.start();
            aAttributeAggregator.restoreState(entry.getValue());
            aggregatorMap.put(key, aAttributeAggregator);
        }
    }

    public static ThreadLocal<String> getKeyThreadLocal() {
        return keyThreadLocal;
    }
}
