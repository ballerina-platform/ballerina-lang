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
import org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;

public class AggregationAttributeExecutor extends AbstractAggregationAttributeExecutor {

    public AggregationAttributeExecutor(AttributeAggregator attributeAggregator,
                                        ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        super(attributeAggregator, attributeExpressionExecutors, executionPlanContext);
    }

    @Override
    public Object execute(ComplexEvent event) {
        return attributeAggregator.process(event);
    }

    public ExpressionExecutor cloneExecutor(String key) {
        return new AggregationAttributeExecutor(attributeAggregator.cloneAggregator(key), attributeExpressionExecutors, executionPlanContext);
    }

    @Override
    public Object[] currentState() {
        return new Object[]{attributeAggregator.currentState()};
    }

    @Override
    public void restoreState(Object[] state) {
        attributeAggregator.restoreState((Object[]) state[0]);
    }
}
