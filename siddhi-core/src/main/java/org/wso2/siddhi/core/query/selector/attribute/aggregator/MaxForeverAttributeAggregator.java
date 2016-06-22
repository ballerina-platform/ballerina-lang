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
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

public class MaxForeverAttributeAggregator extends AttributeAggregator {

    private MaxForeverAttributeAggregator maxForeverAttributeAggregator;

    /**
     * The initialization method for FunctionExecutor
     *
     * @param attributeExpressionExecutors are the executors of each attributes in the function
     * @param executionPlanContext         Execution plan runtime context
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 1) {
            throw new OperationNotSupportedException("MaxForever aggregator has to have exactly 1 parameter, currently " +
                    attributeExpressionExecutors.length + " parameters provided");
        }
        Attribute.Type type = attributeExpressionExecutors[0].getReturnType();
        switch (type) {
            case FLOAT:
                maxForeverAttributeAggregator = new MaxForeverAttributeAggregatorFloat();
                break;
            case INT:
                maxForeverAttributeAggregator = new MaxForeverAttributeAggregatorInt();
                break;
            case LONG:
                maxForeverAttributeAggregator = new MaxForeverAttributeAggregatorLong();
                break;
            case DOUBLE:
                maxForeverAttributeAggregator = new MaxForeverAttributeAggregatorDouble();
                break;
            default:
                throw new OperationNotSupportedException("MaxForever not supported for " + type);
        }
    }

    public Attribute.Type getReturnType() {
        return maxForeverAttributeAggregator.getReturnType();
    }

    @Override
    public Object processAdd(Object data) {
        return maxForeverAttributeAggregator.processAdd(data);
    }

    @Override
    public Object processAdd(Object[] data) {
        // will not occur
        return new IllegalStateException("MaxForever cannot process data array, but found " + Arrays.deepToString(data));
    }

    @Override
    public Object processRemove(Object data) {
        return maxForeverAttributeAggregator.processRemove(data);
    }

    @Override
    public Object processRemove(Object[] data) {
        // will not occur
        return new IllegalStateException("MaxForever cannot process data array, but found " + Arrays.deepToString(data));
    }

    @Override
    public Object reset() {
        return maxForeverAttributeAggregator.reset();
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
        return maxForeverAttributeAggregator.currentState();
    }

    @Override
    public void restoreState(Object[] state) {
        maxForeverAttributeAggregator.restoreState(state);
    }

    class MaxForeverAttributeAggregatorDouble extends MaxForeverAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private volatile Double maxValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object data) {
            Double value = (Double) data;
            if (maxValue == null || maxValue < value) {
                maxValue = value;
            }
            return maxValue;
        }

        @Override
        public synchronized Object processRemove(Object data) {
            Double value = (Double) data;
            if (maxValue == null || maxValue < value) {
                maxValue = value;
            }
            return maxValue;
        }

        @Override
        public Object reset() {
            return maxValue;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{maxValue};
        }

        @Override
        public void restoreState(Object[] state) {
            maxValue = (Double) state[1];
        }

    }

    class MaxForeverAttributeAggregatorFloat extends MaxForeverAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.FLOAT;
        private volatile Float maxValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object data) {
            Float value = (Float) data;
            if (maxValue == null || maxValue < value) {
                maxValue = value;
            }
            return maxValue;
        }

        @Override
        public synchronized Object processRemove(Object data) {
            Float value = (Float) data;
            if (maxValue == null || maxValue < value) {
                maxValue = value;
            }
            return maxValue;
        }

        @Override
        public Object reset() {
            return maxValue;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{maxValue};
        }

        @Override
        public void restoreState(Object[] state) {
            maxValue = (Float) state[1];
        }

    }

    class MaxForeverAttributeAggregatorInt extends MaxForeverAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.INT;
        private volatile Integer maxValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object data) {
            Integer value = (Integer) data;
            if (maxValue == null || maxValue < value) {
                maxValue = value;
            }
            return maxValue;
        }

        @Override
        public synchronized Object processRemove(Object data) {
            Integer value = (Integer) data;
            if (maxValue == null || maxValue < value) {
                maxValue = value;
            }
            return maxValue;
        }

        @Override
        public Object reset() {
            return maxValue;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{ maxValue};
        }

        @Override
        public void restoreState(Object[] state) {
            maxValue = (Integer) state[1];
        }

    }

    class MaxForeverAttributeAggregatorLong extends MaxForeverAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.LONG;
        private volatile Long maxValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object data) {
            Long value = (Long) data;
            if (maxValue == null || maxValue < value) {
                maxValue = value;
            }
            return maxValue;
        }

        @Override
        public synchronized Object processRemove(Object data) {
            Long value = (Long) data;
            if (maxValue == null || maxValue < value) {
                maxValue = value;
            }
            return maxValue;
        }

        @Override
        public Object reset() {
            return maxValue;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{maxValue};
        }

        @Override
        public void restoreState(Object[] state) {
            maxValue = (Long) state[1];
        }

    }

}
