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

public class MinForeverAttributeAggregator extends AttributeAggregator {

    private MinForeverAttributeAggregator minForeverAttributeAggregator;

    /**
     * The initialization method for FunctionExecutor
     *
     * @param attributeExpressionExecutors are the executors of each attributes in the function
     * @param executionPlanContext         Execution plan runtime context
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 1) {
            throw new OperationNotSupportedException("MinForever aggregator has to have exactly 1 parameter, currently " +
                    attributeExpressionExecutors.length + " parameters provided");
        }
        Attribute.Type type = attributeExpressionExecutors[0].getReturnType();
        switch (type) {
            case FLOAT:
                minForeverAttributeAggregator = new MinForeverAttributeAggregatorFloat();
                break;
            case INT:
                minForeverAttributeAggregator = new MinForeverAttributeAggregatorInt();
                break;
            case LONG:
                minForeverAttributeAggregator = new MinForeverAttributeAggregatorLong();
                break;
            case DOUBLE:
                minForeverAttributeAggregator = new MinForeverAttributeAggregatorDouble();
                break;
            default:
                throw new OperationNotSupportedException("MinForever not supported for " + type);
        }
    }

    public Attribute.Type getReturnType() {
        return minForeverAttributeAggregator.getReturnType();
    }

    @Override
    public Object processAdd(Object data) {
        return minForeverAttributeAggregator.processAdd(data);
    }

    @Override
    public Object processAdd(Object[] data) {
        // will not occur
        return new IllegalStateException("MinForever cannot process data array, but found " + Arrays.deepToString(data));
    }

    @Override
    public Object processRemove(Object data) {
        return minForeverAttributeAggregator.processRemove(data);
    }

    @Override
    public Object processRemove(Object[] data) {
        // will not occur
        return new IllegalStateException("MinForever cannot process data array, but found " + Arrays.deepToString(data));
    }

    @Override
    public Object reset() {
        return minForeverAttributeAggregator.reset();
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
        return minForeverAttributeAggregator.currentState();
    }

    @Override
    public void restoreState(Object[] state) {
        minForeverAttributeAggregator.restoreState(state);
    }

    class MinForeverAttributeAggregatorDouble extends MinForeverAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private volatile Double minValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object data) {
            Double value = (Double) data;
            if (minValue == null || minValue > value) {
                minValue = value;
            }
            return minValue;
        }

        @Override
        public synchronized Object processRemove(Object data) {
            Double value = (Double) data;
            if (minValue == null || minValue > value) {
                minValue = value;
            }
            return minValue;
        }

        @Override
        public Object reset() {
            return minValue;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{minValue};
        }

        @Override
        public void restoreState(Object[] state) {
            minValue = (Double) state[1];
        }

    }

    class MinForeverAttributeAggregatorFloat extends MinForeverAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.FLOAT;
        private volatile Float minValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object data) {
            Float value = (Float) data;
            if (minValue == null || minValue > value) {
                minValue = value;
            }
            return minValue;
        }

        @Override
        public synchronized Object processRemove(Object data) {
            Float value = (Float) data;
            if (minValue == null || minValue > value) {
                minValue = value;
            }
            return minValue;
        }

        @Override
        public Object reset() {
            return minValue;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{ minValue};
        }

        @Override
        public void restoreState(Object[] state) {
            minValue = (Float) state[1];
        }

    }

    class MinForeverAttributeAggregatorInt extends MinForeverAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.INT;
        private volatile Integer minValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object data) {
            Integer value = (Integer) data;
            if (minValue == null || minValue > value) {
                minValue = value;
            }
            return minValue;
        }

        @Override
        public Object reset() {
            return minValue;
        }

        @Override
        public synchronized Object processRemove(Object data) {
            Integer value = (Integer) data;
            if (minValue == null || minValue > value) {
                minValue = value;
            }
            return minValue;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{minValue};
        }

        @Override
        public void restoreState(Object[] state) {
            minValue = (Integer) state[1];
        }

    }

    class MinForeverAttributeAggregatorLong extends MinForeverAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.LONG;
        private volatile Long minValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object data) {
            Long value = (Long) data;
            if (minValue == null || minValue > value) {
                minValue = value;
            }
            return minValue;
        }

        @Override
        public Object reset() {
            return minValue;
        }

        @Override
        public synchronized Object processRemove(Object data) {
            Long value = (Long) data;
            if (minValue == null || minValue > value) {
                minValue = value;
            }
            return minValue;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{minValue};
        }

        @Override
        public void restoreState(Object[] state) {
            minValue = (Long) state[1];
        }

    }

}
