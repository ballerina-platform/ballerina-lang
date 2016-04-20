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
package org.wso2.siddhi.core.query.selector.attribute.aggregator;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DistinctcountAttributeAggregator extends AttributeAggregator {
    private Map<Object, Long> distinctValues = new HashMap<Object, Long>();

    /**
     * The initialization method for FunctionExecutor
     *
     * @param attributeExpressionExecutors are the executors of each attributes in the function
     * @param executionPlanContext         Execution plan runtime context
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 1) {
            throw new OperationNotSupportedException("Distinct count aggregator has to have exactly 1 parameter, " +
                    "currently " + attributeExpressionExecutors.length + " parameters provided");
        }
    }

    public Attribute.Type getReturnType() {
        return Attribute.Type.LONG;
    }

    @Override
    public Object processAdd(Object data) {
        Long preVal = distinctValues.get(data);
        if (preVal != null) {
            distinctValues.put(data, ++preVal);
        } else {
            distinctValues.put(data, 1L);
        }
        return getDistinctCount();
    }

    @Override
    public Object processAdd(Object[] data) {
        return new IllegalStateException(
                "Distinct count aggregator cannot process data array, but found " + Arrays.deepToString(data));
    }

    @Override
    public Object processRemove(Object data) {
        Long preVal = distinctValues.get(data);
        preVal--;
        if (preVal > 0) {
            distinctValues.put(data, preVal);
        } else {
            distinctValues.remove(data);
        }
        return getDistinctCount();
    }

    @Override
    public Object processRemove(Object[] data) {
        return new IllegalStateException(
                "Distinct count aggregator cannot process data array, but found " + Arrays.deepToString(data));
    }

    @Override
    public Object reset() {
        distinctValues.clear();
        return getDistinctCount();
    }

    @Override
    public void start() {
        // Nothing to start.
    }

    @Override
    public void stop() {
        // Nothing to stop.
    }

    @Override
    public Object[] currentState() {
        return new Object[]{distinctValues};
    }

    @Override
    public void restoreState(Object[] state) {
        distinctValues = (Map<Object, Long>) state[0];
    }

    private long getDistinctCount() {
        return distinctValues.size();
    }
}
