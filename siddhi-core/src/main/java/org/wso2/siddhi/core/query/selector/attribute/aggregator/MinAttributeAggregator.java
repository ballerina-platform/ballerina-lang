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

public class MinAttributeAggregator extends AttributeAggregator {

    private MinAttributeAggregator minOutputAttributeAggregator;

    public void init(Attribute.Type type) {


    }

    /**
     * The initialization method for FunctionExecutor
     *
     * @param attributeExpressionExecutors are the executors of each attributes in the function
     * @param executionPlanContext         Execution plan runtime context
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 1) {
            throw new OperationNotSupportedException("Min aggregator has to have exactly 1 parameter, currently " +
                    attributeExpressionExecutors.length + " parameters provided");
        }
        Attribute.Type type = attributeExpressionExecutors[0].getReturnType();
        switch (type) {
            case FLOAT:
                minOutputAttributeAggregator = new MinAttributeAggregatorFloat();
                break;
            case INT:
                minOutputAttributeAggregator = new MinAttributeAggregatorInt();
                break;
            case LONG:
                minOutputAttributeAggregator = new MinAttributeAggregatorLong();
                break;
            case DOUBLE:
                minOutputAttributeAggregator = new MinAttributeAggregatorDouble();
                break;
            default:
                throw new OperationNotSupportedException("Min not supported for " + type);
        }
    }

    public Attribute.Type getReturnType() {
        return minOutputAttributeAggregator.getReturnType();
    }

    @Override
    public Object processAdd(Object data) {
        return minOutputAttributeAggregator.processAdd(data);
    }

    @Override
    public Object processAdd(Object[] data) {
        // will not occur
        return new IllegalStateException("Min cannot process data array, but found " + Arrays.deepToString(data));
    }

    @Override
    public Object processRemove(Object data) {
        return minOutputAttributeAggregator.processRemove(data);
    }

    @Override
    public Object processRemove(Object[] data) {
        // will not occur
        return new IllegalStateException("Min cannot process data array, but found " + Arrays.deepToString(data));
    }

    @Override
    public Object reset() {
        return minOutputAttributeAggregator.reset();
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
        return minOutputAttributeAggregator.currentState();
    }

    @Override
    public void restoreState(Object[] state) {
        minOutputAttributeAggregator.restoreState(state);
    }

    class MinAttributeAggregatorDouble extends MinAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private Deque<Double> minDeque = new LinkedList<Double>();
        private volatile Double minValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object data) {
            Double value = (Double) data;
            for (Iterator<Double> iterator = minDeque.descendingIterator(); iterator.hasNext(); ) {
                if (iterator.next() > value) {
                    iterator.remove();
                } else {
                    break;
                }
            }
            minDeque.addLast(value);
            if (minValue == null || minValue > value) {
                minValue = value;
            }
            return minValue;
        }

        @Override
        public synchronized Object processRemove(Object data) {
            minDeque.removeFirstOccurrence(data);
            minValue = minDeque.peekFirst();
            return minValue;
        }

        @Override
        public Object reset() {
            minDeque.clear();
            minValue = null;
            return null;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{minDeque, minValue};
        }

        @Override
        public void restoreState(Object[] state) {
            minDeque = (Deque<Double>) state[0];
            minValue = (Double) state[1];
        }

    }

    class MinAttributeAggregatorFloat extends MinAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.FLOAT;
        private Deque<Float> minDeque = new LinkedList<Float>();
        private volatile Float minValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object data) {
            Float value = (Float) data;
            for (Iterator<Float> iterator = minDeque.descendingIterator(); iterator.hasNext(); ) {
                if (iterator.next() > value) {
                    iterator.remove();
                } else {
                    break;
                }
            }
            minDeque.addLast(value);
            if (minValue == null || minValue > value) {
                minValue = value;
            }
            return minValue;
        }

        @Override
        public synchronized Object processRemove(Object data) {
            minDeque.removeFirstOccurrence(data);
            minValue = minDeque.peekFirst();
            return minValue;
        }

        @Override
        public Object reset() {
            minDeque.clear();
            minValue = null;
            return null;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{minDeque, minValue};
        }

        @Override
        public void restoreState(Object[] state) {
            minDeque = (Deque<Float>) state[0];
            minValue = (Float) state[1];
        }

    }

    class MinAttributeAggregatorInt extends MinAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.INT;
        private Deque<Integer> minDeque = new LinkedList<Integer>();
        private volatile Integer minValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object data) {
            Integer value = (Integer) data;
            for (Iterator<Integer> iterator = minDeque.descendingIterator(); iterator.hasNext(); ) {

                if (iterator.next() > value) {
                    iterator.remove();
                } else {
                    break;
                }
            }
            minDeque.addLast(value);
            if (minValue == null || minValue > value) {
                minValue = value;
            }
            return minValue;
        }

        @Override
        public Object reset() {
            minDeque.clear();
            minValue = null;
            return null;
        }

        @Override
        public synchronized Object processRemove(Object data) {
            minDeque.removeFirstOccurrence(data);
            minValue = minDeque.peekFirst();
            return minValue;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{minDeque, minValue};
        }

        @Override
        public void restoreState(Object[] state) {
            minDeque = (Deque<Integer>) state[0];
            minValue = (Integer) state[1];
        }

    }

    class MinAttributeAggregatorLong extends MinAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.LONG;
        private Deque<Long> minDeque = new LinkedList<Long>();
        private volatile Long minValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object data) {
            Long value = (Long) data;
            for (Iterator<Long> iterator = minDeque.descendingIterator(); iterator.hasNext(); ) {
                if (iterator.next() > value) {
                    iterator.remove();
                } else {
                    break;
                }
            }
            minDeque.addLast(value);
            if (minValue == null || minValue > value) {
                minValue = value;
            }
            return minValue;
        }

        @Override
        public Object reset() {
            minDeque.clear();
            minValue = null;
            return null;
        }

        @Override
        public synchronized Object processRemove(Object data) {
            minDeque.removeFirstOccurrence(data);
            minValue = minDeque.peekFirst();
            return minValue;
        }

        @Override
        public Object[] currentState() {
            return new Object[]{minDeque, minValue};
        }

        @Override
        public void restoreState(Object[] state) {
            minDeque = (Deque<Long>) state[0];
            minValue = (Long) state[1];
        }

    }

}
