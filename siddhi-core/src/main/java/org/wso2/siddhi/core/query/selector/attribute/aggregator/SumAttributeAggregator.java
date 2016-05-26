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
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.Arrays;

public class SumAttributeAggregator extends AttributeAggregator {

    private SumAttributeAggregator sumOutputAttributeAggregator;

    /**
     * The initialization method for FunctionExecutor
     *
     * @param attributeExpressionExecutors are the executors of each attributes in the function
     * @param executionPlanContext         Execution plan runtime context
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 1) {
            throw new OperationNotSupportedException("Sum aggregator has to have exactly 1 parameter, currently " +
                    attributeExpressionExecutors.length + " parameters provided");
        }
        Attribute.Type type = attributeExpressionExecutors[0].getReturnType();
        switch (type) {
            case FLOAT:
                sumOutputAttributeAggregator = new SumAttributeAggregatorFloat();
                break;
            case INT:
                sumOutputAttributeAggregator = new SumAttributeAggregatorInt();
                break;
            case LONG:
                sumOutputAttributeAggregator = new SumAttributeAggregatorLong();
                break;
            case DOUBLE:
                sumOutputAttributeAggregator = new SumAttributeAggregatorDouble();
                break;
            default:
                throw new OperationNotSupportedException("Sum not supported for " + type);
        }

    }

    public Attribute.Type getReturnType() {
        return sumOutputAttributeAggregator.getReturnType();
    }

    @Override
    public Object processAdd(Object data) {
        return sumOutputAttributeAggregator.processAdd(data);
    }

    @Override
    public Object processAdd(Object[] data) {
        // will not occur
        return new IllegalStateException("Sin cannot process data array, but found " + Arrays.deepToString(data));
    }

    @Override
    public Object processRemove(Object data) {
        return sumOutputAttributeAggregator.processRemove(data);
    }

    @Override
    public Object processRemove(Object[] data) {
        // will not occur
        return new IllegalStateException("Sin cannot process data array, but found " + Arrays.deepToString(data));
    }

    @Override
    public Object reset() {
        return sumOutputAttributeAggregator.reset();
    }

    @Override
    public void start() {
        //Nothing to start
    }

    @Override
    public void stop() {
        //Nothing to stop
    }

    @Override
    public Object[] currentState() {
        return sumOutputAttributeAggregator.currentState();
    }

    @Override
    public void restoreState(Object[] state) {
        sumOutputAttributeAggregator.restoreState(state);
    }

    class SumAttributeAggregatorDouble extends SumAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private double value = 0.0;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object data) {
            value += (Double) data;
            return value;
        }

        @Override
        public Object processRemove(Object data) {
            value -= (Double) data;
            return value;
        }

        @Override
        public Object reset() {
            value = 0.0;
            return value;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{value};
        }

        @Override
        public void restoreState(Object[] state) {
            value = (Double) state[0];
        }

    }

    class SumAttributeAggregatorFloat extends SumAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private double value = 0.0;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object data) {
            value += ((Float) data).doubleValue();
            return value;
        }

        @Override
        public Object processRemove(Object data) {
            value -= ((Float) data).doubleValue();
            return value;
        }

        public Object reset() {
            value = 0.0;
            return value;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{value};
        }

        @Override
        public void restoreState(Object[] state) {
            value = (Double) state[0];
        }

    }

    class SumAttributeAggregatorInt extends SumAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.LONG;
        private long value = 0L;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object data) {
            value += ((Integer) data).longValue();
            return value;
        }

        @Override
        public Object processRemove(Object data) {
            value -= ((Integer) data).longValue();
            return value;
        }

        public Object reset() {
            value = 0L;
            return value;
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

    class SumAttributeAggregatorLong extends SumAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.LONG;
        private long value = 0L;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object data) {
            value += (Long) data;
            return value;
        }

        @Override
        public Object processRemove(Object data) {
            value -= (Long) data;
            return value;
        }

        public Object reset() {
            value = 0L;
            return value;
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

}
