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
package org.ballerinalang.siddhi.core.query.selector.attribute.aggregator;

import org.ballerinalang.siddhi.annotation.Example;
import org.ballerinalang.siddhi.annotation.Extension;
import org.ballerinalang.siddhi.annotation.Parameter;
import org.ballerinalang.siddhi.annotation.ReturnAttribute;
import org.ballerinalang.siddhi.annotation.util.DataType;
import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.exception.OperationNotSupportedException;
import org.ballerinalang.siddhi.core.executor.ExpressionExecutor;
import org.ballerinalang.siddhi.core.util.config.ConfigReader;
import org.ballerinalang.siddhi.query.api.definition.Attribute;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link AttributeAggregator} to calculate average based on an event attribute.
 */
@Extension(
        name = "avg",
        namespace = "",
        description = "Calculates the average for all the events.",
        parameters = {
                @Parameter(name = "arg",
                        description = "The value that need to be averaged.",
                        type = {DataType.INT, DataType.LONG, DataType.DOUBLE, DataType.FLOAT})
        },
        returnAttributes = @ReturnAttribute(
                description = "Returns the calculated average value as a double.",
                type = {DataType.DOUBLE}),
        examples = @Example(
                syntax = "from fooStream#window.timeBatch\n select avg(temp) as avgTemp\n insert into barStream;",
                description = "avg(temp) returns the average temp value for all the events based on their " +
                        "arrival and expiry."
        )
)
public class AvgAttributeAggregator extends AttributeAggregator {

    private AvgAttributeAggregator avgOutputAttributeAggregator;

    /**
     * The initialization method for FunctionExecutor
     *
     * @param attributeExpressionExecutors are the executors of each attributes in the function
     * @param configReader                 this hold the {@link AvgAttributeAggregator} configuration reader.
     * @param siddhiAppContext             Siddhi app runtime context
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader,
                        SiddhiAppContext siddhiAppContext) {
        if (attributeExpressionExecutors.length != 1) {
            throw new OperationNotSupportedException("Avg aggregator has to have exactly 1 parameter, currently " +
                    attributeExpressionExecutors.length + " parameters provided");
        }
        Attribute.Type type = attributeExpressionExecutors[0].getReturnType();
        switch (type) {
            case FLOAT:
                avgOutputAttributeAggregator = new AvgAttributeAggregatorFloat();
                break;
            case INT:
                avgOutputAttributeAggregator = new AvgAttributeAggregatorInt();
                break;
            case LONG:
                avgOutputAttributeAggregator = new AvgAttributeAggregatorLong();
                break;
            case DOUBLE:
                avgOutputAttributeAggregator = new AvgAttributeAggregatorDouble();
                break;
            default:
                throw new OperationNotSupportedException("Avg not supported for " + type);
        }
    }

    public Attribute.Type getReturnType() {
        return avgOutputAttributeAggregator.getReturnType();
    }

    @Override
    public Object processAdd(Object data) {
        if (data == null) {
            return avgOutputAttributeAggregator.currentValue();
        }
        return avgOutputAttributeAggregator.processAdd(data);
    }

    @Override
    public Object processAdd(Object[] data) {
        // will not occur
        return new IllegalStateException("Avg cannot process data array, but found " + Arrays.deepToString(data));
    }

    @Override
    public Object processRemove(Object data) {
        if (data == null) {
            return avgOutputAttributeAggregator.currentValue();
        }
        return avgOutputAttributeAggregator.processRemove(data);
    }

    @Override
    public Object processRemove(Object[] data) {
        // will not occur
        return new IllegalStateException("Avg cannot process data array, but found " + Arrays.deepToString(data));
    }

    protected Object currentValue() {
        return null;
    }

    @Override
    public boolean canDestroy() {
        return avgOutputAttributeAggregator.canDestroy();
    }

    @Override
    public Object reset() {
        return avgOutputAttributeAggregator.reset();
    }

    @Override
    public Map<String, Object> currentState() {
        return avgOutputAttributeAggregator.currentState();
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        avgOutputAttributeAggregator.restoreState(state);
    }

    class AvgAttributeAggregatorDouble extends AvgAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private double value = 0.0;
        private long count = 0;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object data) {
            count++;
            value += (Double) data;
            if (count == 0) {
                return null;
            }
            return value / count;
        }

        @Override
        public Object processRemove(Object obj) {
            count--;
            value -= (Double) obj;
            if (count == 0) {
                return null;
            }
            return value / count;
        }

        @Override
        public Object reset() {
            value = 0.0;
            count = 0;
            return null;
        }

        @Override
        public boolean canDestroy() {
            return value == 0.0 && count == 0;
        }

        @Override
        public Map<String, Object> currentState() {
            Map<String, Object> state = new HashMap<>();
            state.put("Value", value);
            state.put("Count", count);
            return state;
        }

        @Override
        public void restoreState(Map<String, Object> state) {
            value = (double) state.get("Value");
            count = (long) state.get("Count");
        }

        protected Object currentValue() {
            if (count == 0) {
                return null;
            }
            return value / count;
        }
    }

    class AvgAttributeAggregatorFloat extends AvgAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private double value = 0.0;
        private long count = 0;

        public Attribute.Type getReturnType() {
            return this.type;
        }

        @Override
        public Object processAdd(Object data) {
            count++;
            value += (Float) data;
            if (count == 0) {
                return null;
            }
            return value / count;
        }

        @Override
        public Object processRemove(Object obj) {
            count--;
            value -= (Float) obj;
            if (count == 0) {
                return null;
            }
            return value / count;
        }

        @Override
        public Object reset() {
            value = 0.0;
            count = 0;
            return null;
        }

        @Override
        public boolean canDestroy() {
            return value == 0.0 && count == 0;
        }

        @Override
        public Map<String, Object> currentState() {
            Map<String, Object> state = new HashMap<>();
            state.put("Value", value);
            state.put("Count", count);
            return state;
        }

        @Override
        public void restoreState(Map<String, Object> state) {
            value = (double) state.get("Value");
            count = (long) state.get("Count");
        }

        protected Object currentValue() {
            if (count == 0) {
                return null;
            }
            return value / count;
        }
    }

    class AvgAttributeAggregatorInt extends AvgAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private double value = 0.0;
        private long count = 0;

        public Attribute.Type getReturnType() {
            return this.type;
        }

        @Override
        public Object processAdd(Object data) {
            count++;
            value += (Integer) data;
            if (count == 0) {
                return null;
            }
            return value / count;
        }

        @Override
        public Object processRemove(Object obj) {
            count--;
            value -= (Integer) obj;
            if (count == 0) {
                return null;
            }
            return value / count;
        }

        @Override
        public Object reset() {
            value = 0.0;
            count = 0;
            return null;
        }

        @Override
        public boolean canDestroy() {
            return value == 0.0 && count == 0;
        }

        @Override
        public Map<String, Object> currentState() {
            Map<String, Object> state = new HashMap<>();
            state.put("Value", value);
            state.put("Count", count);
            return state;
        }

        @Override
        public void restoreState(Map<String, Object> state) {
            value = (double) state.get("Value");
            count = (long) state.get("Count");
        }

        protected Object currentValue() {
            if (count == 0) {
                return null;
            }
            return value / count;
        }

    }

    class AvgAttributeAggregatorLong extends AvgAttributeAggregator {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private double value = 0.0;
        private long count = 0;

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object data) {
            count++;
            value += (Long) data;
            if (count == 0) {
                return null;
            }
            return value / count;
        }

        @Override
        public Object processRemove(Object obj) {
            count--;
            value -= (Long) obj;
            if (count == 0) {
                return null;
            }
            return value / count;
        }

        @Override
        public Object reset() {
            value = 0.0;
            count = 0;
            return null;
        }

        @Override
        public boolean canDestroy() {
            return value == 0.0 && count == 0;
        }

        @Override
        public Map<String, Object> currentState() {
            Map<String, Object> state = new HashMap<>();
            state.put("Value", value);
            state.put("Count", count);
            return state;
        }

        @Override
        public void restoreState(Map<String, Object> state) {
            value = (double) state.get("Value");
            count = (long) state.get("Count");
        }

        protected Object currentValue() {
            if (count == 0) {
                return null;
            }
            return value / count;
        }

    }


}
