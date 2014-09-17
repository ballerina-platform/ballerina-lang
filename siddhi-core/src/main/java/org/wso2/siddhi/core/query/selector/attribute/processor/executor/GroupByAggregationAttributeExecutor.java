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

package org.wso2.siddhi.core.query.selector.attribute.processor.executor;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.core.query.selector.attribute.handler.AttributeAggregator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupByAggregationAttributeExecutor extends AbstractAggregationAttributeExecutor {

    protected Map<String, AttributeAggregator> aggregatorMap = new HashMap<String, AttributeAggregator>();

    public GroupByAggregationAttributeExecutor(AttributeAggregator aggregator,
                                               List<ExpressionExecutor> expressionExecutors, SiddhiContext siddhiContext) {
        this.siddhiContext = siddhiContext;
        this.expressionExecutors = expressionExecutors;
        this.attributeAggregator = aggregator;
        siddhiContext.addEternalReferencedHolder(attributeAggregator);
        size = expressionExecutors.size();
    }

    @Override
    public Object execute(StreamEvent event) {
        String key = QuerySelector.getThreadLocalGroupByKey();
        AttributeAggregator currentAttributeAggregator = aggregatorMap.get(key);
        if (currentAttributeAggregator == null) {
            currentAttributeAggregator = attributeAggregator.newInstance();
            siddhiContext.addEternalReferencedHolder(currentAttributeAggregator);
            aggregatorMap.put(key, currentAttributeAggregator);
        }
        return process(event, currentAttributeAggregator);
    }

    public ExpressionExecutor cloneExecutor() {
        return new GroupByAggregationAttributeExecutor(attributeAggregator.newInstance(),expressionExecutors,siddhiContext);
    }


}
