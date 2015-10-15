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
package org.wso2.siddhi.core.query.selector.attribute.aggregator;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;

public class CountAttributeAggregator extends AttributeAggregator {

    private static Attribute.Type type = Attribute.Type.LONG;
    private long value = 0l;

    /**
     * The initialization method for FunctionExecutor
     *
     * @param attributeExpressionExecutors are the executors of each attributes in the function
     * @param executionPlanContext         Execution plan runtime context
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {

    }

    public Attribute.Type getReturnType() {
        return type;
    }

    @Override
    public Object processAdd(Object data) {
        value++;
        return value;
    }

    @Override
    public Object processAdd(Object[] data) {
        value++;
        return value;
    }

    @Override
    public Object processRemove(Object data) {
        value--;
        return value;
    }

    @Override
    public Object processRemove(Object[] data) {
        value--;
        return value;
    }

    @Override
    public Object reset() {
        value = 0l;
        return value;
    }

    @Override
    public void start() {
        //Nothing to start
    }

    @Override
    public void stop() {
        //nothing to stop
    }

    @Override
    public Object[] currentState() {
        return new Object[]{value};
    }

    @Override
    public void restoreState(Object[] state) {
        value = (Long) state[0];
    }
}
