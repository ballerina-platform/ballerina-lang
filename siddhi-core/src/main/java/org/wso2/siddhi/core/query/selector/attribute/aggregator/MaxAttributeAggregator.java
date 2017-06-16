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

import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * {@link AttributeAggregator} to calculate max value based on an event attribute.
 */
@Extension(
        name = "max",
        namespace = "",
        description = "Returns the maximum value for all the events.",
        parameters = {
                @Parameter(name = "arg",
                        description = "The value that needs to be compared to find the maximum value.",
                        type = {DataType.INT, DataType.LONG, DataType.DOUBLE, DataType.FLOAT})
        },
        returnAttributes = @ReturnAttribute(
                description = "Returns the maximum value in the same data type as the input.",
                type = {DataType.INT, DataType.LONG, DataType.DOUBLE, DataType.FLOAT}),
        examples = @Example(
                syntax = "from fooStream#window.timeBatch(10 sec)\n" +
                        "select max(temp) as maxTemp\n" +
                        "insert into barStream;",
                description = "max(temp) returns the maximum temp value recorded for all the events based on their " +
                        "arrival and expiry."
        )
)
public class MaxAttributeAggregator extends AttributeAggregator {

    private MaxAttributeAggregator maxOutputAttributeAggregator;

    /**
     * The initialization method for FunctionExecutor
     *  @param attributeExpressionExecutors are the executors of each attributes in the function
     * @param configReader this hold the {@link MaxAttributeAggregator} configuration reader.
     * @param siddhiAppContext         Siddhi app runtime context
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader,
                        SiddhiAppContext siddhiAppContext) {
        if (attributeExpressionExecutors.length != 1) {
            throw new OperationNotSupportedException("Max aggregator has to have exactly 1 parameter, currently " +
                    attributeExpressionExecutors.length + " parameters provided");
        }
        Attribute.Type type = attributeExpressionExecutors[0].getReturnType();
        switch (type) {
            case FLOAT:
                maxOutputAttributeAggregator = new MaxAttributeAggregatorFloat();
                break;
            case INT:
                maxOutputAttributeAggregator = new MaxAttributeAggregatorInt();
                break;
            case LONG:
                maxOutputAttributeAggregator = new MaxAttributeAggregatorLong();
                break;
            case DOUBLE:
                maxOutputAttributeAggregator = new MaxAttributeAggregatorDouble();
                break;
            default:
                throw new OperationNotSupportedException("Max not supported for " + type);
        }
    }

    public Attribute.Type getReturnType() {
        return maxOutputAttributeAggregator.getReturnType();
    }

    @Override
    public Object processAdd(Object data) {
        return maxOutputAttributeAggregator.processAdd(data);
    }

    @Override
    public Object processAdd(Object[] data) {
        // will not occur
        return new IllegalStateException("Max cannot process data array, but found " + Arrays.deepToString(data));
    }

    @Override
    public Object processRemove(Object data) {
        return maxOutputAttributeAggregator.processRemove(data);
    }

    @Override
    public Object processRemove(Object[] data) {
        // will not occur
        return new IllegalStateException("Max cannot process data array, but found " + Arrays.deepToString(data));
    }

    @Override
    public Object reset() {
        return maxOutputAttributeAggregator.reset();
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
    public Map<String, Object> currentState() {
        return maxOutputAttributeAggregator.currentState();
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        maxOutputAttributeAggregator.restoreState(state);
    }

    class MaxAttributeAggregatorDouble extends MaxAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private Deque<Double> maxDeque = new LinkedList<Double>();
        private volatile Double maxValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object data) {
            Double value = (Double) data;
            for (Iterator<Double> iterator = maxDeque.descendingIterator(); iterator.hasNext(); ) {
                if (iterator.next() < value) {
                    iterator.remove();
                } else {
                    break;
                }
            }
            maxDeque.addLast(value);
            if (maxValue == null || maxValue < value) {
                maxValue = value;
            }
            return maxValue;
        }

        @Override
        public synchronized Object processRemove(Object data) {
            maxDeque.removeFirstOccurrence(data);
            maxValue = maxDeque.peekFirst();
            return maxValue;
        }

        @Override
        public synchronized Object reset() {
            maxDeque.clear();
            maxValue = null;
            return null;
        }

        @Override
        public Map<String, Object> currentState() {
            Map<String, Object> state = new HashMap<>();
            synchronized (this) {
                state.put("MaxValue", maxValue);
                state.put("MaxDeque", maxDeque);
            }
            return state;
        }

        @Override
        public synchronized void restoreState(Map<String, Object> state) {
            maxValue = (Double) state.get("MaxValue");
            maxDeque = (Deque<Double>) state.get("MaxDeque");
        }
    }

    class MaxAttributeAggregatorFloat extends MaxAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.FLOAT;
        private Deque<Float> maxDeque = new LinkedList<Float>();
        private volatile Float maxValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object data) {
            Float value = (Float) data;
            for (Iterator<Float> iterator = maxDeque.descendingIterator(); iterator.hasNext(); ) {
                if (iterator.next() < value) {
                    iterator.remove();
                } else {
                    break;
                }
            }
            maxDeque.addLast(value);
            if (maxValue == null || maxValue < value) {
                maxValue = value;
            }
            return maxValue;
        }

        @Override
        public synchronized Object processRemove(Object data) {
            maxDeque.removeFirstOccurrence(data);
            maxValue = maxDeque.peekFirst();
            return maxValue;
        }

        @Override
        public synchronized Object reset() {
            maxDeque.clear();
            maxValue = null;
            return null;
        }

        @Override
        public Map<String, Object> currentState() {
            Map<String, Object> state = new HashMap<>();
            synchronized (this) {
                state.put("MaxValue", maxValue);
                state.put("MaxDeque", maxDeque);
            }
            return state;
        }

        @Override
        public synchronized void restoreState(Map<String, Object> state) {
            maxValue = (Float) state.get("MaxValue");
            maxDeque = (Deque<Float>) state.get("MaxDeque");
        }

    }

    class MaxAttributeAggregatorInt extends MaxAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.INT;
        private Deque<Integer> maxDeque = new LinkedList<Integer>();
        private volatile Integer maxValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object data) {
            Integer value = (Integer) data;
            for (Iterator<Integer> iterator = maxDeque.descendingIterator(); iterator.hasNext(); ) {
                if (iterator.next() < value) {
                    iterator.remove();
                } else {
                    break;
                }
            }
            maxDeque.addLast(value);
            if (maxValue == null || maxValue < value) {
                maxValue = value;
            }
            return maxValue;
        }

        @Override
        public synchronized Object processRemove(Object data) {
            maxDeque.removeFirstOccurrence(data);
            maxValue = maxDeque.peekFirst();
            return maxValue;
        }

        @Override
        public synchronized Object reset() {
            maxDeque.clear();
            maxValue = null;
            return null;
        }

        @Override
        public Map<String, Object> currentState() {
            Map<String, Object> state = new HashMap<>();
            synchronized (this) {
                state.put("MaxValue", maxValue);
                state.put("MaxDeque", maxDeque);
            }
            return state;
        }

        @Override
        public synchronized void restoreState(Map<String, Object> state) {
            maxValue = (Integer) state.get("MaxValue");
            maxDeque = (Deque<Integer>) state.get("MaxDeque");
        }

    }

    class MaxAttributeAggregatorLong extends MaxAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.LONG;
        private Deque<Long> maxDeque = new LinkedList<Long>();
        private volatile Long maxValue = null;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public synchronized Object processAdd(Object data) {
            Long value = (Long) data;
            for (Iterator<Long> iterator = maxDeque.descendingIterator(); iterator.hasNext(); ) {
                if (iterator.next() < value) {
                    iterator.remove();
                } else {
                    break;
                }
            }
            maxDeque.addLast(value);
            if (maxValue == null || maxValue < value) {
                maxValue = value;
            }
            return maxValue;
        }

        @Override
        public synchronized Object processRemove(Object data) {
            maxDeque.removeFirstOccurrence(data);
            maxValue = maxDeque.peekFirst();
            return maxValue;
        }

        @Override
        public synchronized Object reset() {
            maxDeque.clear();
            maxValue = null;
            return null;
        }

        @Override
        public Map<String, Object> currentState() {
            Map<String, Object> state = new HashMap<>();
            synchronized (this) {
                state.put("MaxValue", maxValue);
                state.put("MaxDeque", maxDeque);
            }
            return state;
        }

        @Override
        public synchronized void restoreState(Map<String, Object> state) {
            maxValue = (Long) state.get("MaxValue");
            maxDeque = (Deque<Long>) state.get("MaxDeque");
        }

    }

}
