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
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link AttributeAggregator} to calculate standard deviation based on an event attribute.
 */
@Extension(
        name = "stdDev",
        namespace = "",
        description = "Returns the calculated standard deviation for all the events.",
        parameters = {
                @Parameter(name = "arg",
                        description = "The value that should be used to calculate the standard deviation.",
                        type = {DataType.INT, DataType.LONG, DataType.DOUBLE, DataType.FLOAT})
        },
        returnAttributes = @ReturnAttribute(
                description = "Returns the calculated standard deviation value as a double.",
                type = {DataType.DOUBLE}),
        examples = @Example(
                syntax = "from inputStream\n" +
                        "select stddev(temp) as stdTemp\n" +
                        "insert into outputStream;",
                description = "stddev(temp) returns the calculated standard deviation of temp for all the events " +
                        "based on their arrival and expiry."
        )
)
public class StdDevAttributeAggregator extends AttributeAggregator {
    private StdDevAttributeAggregator stdDevOutputAttributeAggregator;

    /**
     * The initialization method for FunctionExecutor
     *  @param attributeExpressionExecutors are the executors of each attributes in the function
     * @param configReader
     * @param executionPlanContext         Execution plan runtime context
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader,
                        ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 1) {
            throw new OperationNotSupportedException("stdDev aggregator has to have exactly 1 parameter, currently " +
                    attributeExpressionExecutors.length + " parameters provided");
        }

        Attribute.Type type = attributeExpressionExecutors[0].getReturnType();

        switch (type) {
            case INT:
                stdDevOutputAttributeAggregator = new StdDevAttributeAggregatorInt();
                break;
            case LONG:
                stdDevOutputAttributeAggregator = new StdDevAttributeAggregatorLong();
                break;
            case FLOAT:
                stdDevOutputAttributeAggregator = new StdDevAttributeAggregatorFloat();
                break;
            case DOUBLE:
                stdDevOutputAttributeAggregator = new StdDevAttributeAggregatorDouble();
                break;
            default:
                throw new OperationNotSupportedException("stdDev not supported for " + type);
        }
    }

    @Override
    public Attribute.Type getReturnType() {
        return stdDevOutputAttributeAggregator.getReturnType();
    }

    @Override
    public Object processAdd(Object data) {
        return stdDevOutputAttributeAggregator.processAdd(data);
    }

    @Override
    public Object processAdd(Object[] data) {
        return new IllegalStateException("stdDev cannot process data array, but found " + Arrays.deepToString(data));
    }

    @Override
    public Object processRemove(Object data) {
        return stdDevOutputAttributeAggregator.processRemove(data);
    }

    @Override
    public Object processRemove(Object[] data) {
        return new IllegalStateException("stdDev cannot process data array, but found " + Arrays.deepToString(data));
    }

    @Override
    public Object reset() {
        return stdDevOutputAttributeAggregator.reset();
    }

    @Override
    public Map<String, Object> currentState() {
        return stdDevOutputAttributeAggregator.currentState();
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        stdDevOutputAttributeAggregator.restoreState(state);
    }

    /**
     * Standard deviation aggregator for Double values
     */
    private class StdDevAttributeAggregatorDouble extends StdDevAttributeAggregator {
        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private double mean, oldMean, stdDeviation, sum;
        private int count = 0;

        @Override
        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object data) {
            // See here for the algorithm: http://www.johndcook.com/blog/standard_deviation/
            count++;
            double value = (Double) data;

            if (count == 1) {
                sum = mean = oldMean = value;
                stdDeviation = 0.0;
            } else {
                oldMean = mean;
                sum += value;
                mean = sum / count;
                stdDeviation += (value - oldMean) * (value - mean);
            }

            if (count < 2) {
                return 0.0;
            }
            return Math.sqrt(stdDeviation / count);
        }

        @Override
        public Object processRemove(Object data) {
            count--;
            double value = (Double) data;

            if (count == 0) {
                sum = mean = 0.0;
                stdDeviation = 0.0;
            } else {
                oldMean = mean;
                sum -= value;
                mean = sum / count;
                stdDeviation -= (value - oldMean) * (value - mean);
            }

            if (count < 2) {
                return 0.0;
            }
            return Math.sqrt(stdDeviation / count);
        }

        @Override
        public Object reset() {
            sum = mean = oldMean = 0.0;
            stdDeviation = 0.0;
            count = 0;
            return 0.0;
        }

        @Override
        public Map<String, Object> currentState() {
            Map<String, Object> state = new HashMap<>();
            state.put("Sum", sum);
            state.put("Mean", mean);
            state.put("OldMean", oldMean);
            state.put("stdDeviation", stdDeviation);
            state.put("Count", count);
            return state;
        }

        @Override
        public void restoreState(Map<String, Object> state) {
            sum = (Long) state.get("Sum");
            mean = (Long) state.get("Mean");
            oldMean = (Long) state.get("OldMean");
            stdDeviation = (Long) state.get("stdDeviation");
            count = (int) state.get("Count");
        }
    }

    /**
     * Standard deviation aggregator for Float values
     */
    private class StdDevAttributeAggregatorFloat extends StdDevAttributeAggregator {
        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private double mean, oldMean, stdDeviation, sum;
        private int count = 0;

        @Override
        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object data) {
            // See here for the algorithm: http://www.johndcook.com/blog/standard_deviation/
            count++;
            double value = (Float) data;

            if (count == 1) {
                sum = mean = oldMean = value;
                stdDeviation = 0.0;
            } else {
                oldMean = mean;
                sum += value;
                mean = sum / count;
                stdDeviation += (value - oldMean) * (value - mean);
            }

            if (count < 2) {
                return 0.0;
            }
            return Math.sqrt(stdDeviation / count);
        }

        @Override
        public Object processRemove(Object data) {
            count--;
            double value = (Float) data;

            if (count == 0) {
                sum = mean = 0.0;
                stdDeviation = 0.0;
            } else {
                oldMean = mean;
                sum -= value;
                mean = sum / count;
                stdDeviation -= (value - oldMean) * (value - mean);
            }

            if (count < 2) {
                return 0.0;
            }
            return Math.sqrt(stdDeviation / count);
        }

        @Override
        public Object reset() {
            sum = mean = oldMean = 0.0;
            stdDeviation = 0.0;
            count = 0;
            return 0.0;
        }

        @Override
        public Map<String, Object> currentState() {
            Map<String, Object> state = new HashMap<>();
            state.put("Sum", sum);
            state.put("Mean", mean);
            state.put("OldMean", oldMean);
            state.put("stdDeviation", stdDeviation);
            state.put("Count", count);
            return state;
        }

        @Override
        public void restoreState(Map<String, Object> state) {
            sum = (Long) state.get("Sum");
            mean = (Long) state.get("Mean");
            oldMean = (Long) state.get("OldMean");
            stdDeviation = (Long) state.get("stdDeviation");
            count = (int) state.get("Count");
        }
    }

    /**
     * Standard deviation aggregator for Integer values
     */
    private class StdDevAttributeAggregatorInt extends StdDevAttributeAggregator {
        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private double mean, oldMean, stdDeviation, sum;
        private int count = 0;

        @Override
        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object data) {
            // See here for the algorithm: http://www.johndcook.com/blog/standard_deviation/
            count++;
            double value = (Integer) data;

            if (count == 1) {
                sum = mean = oldMean = value;
                stdDeviation = 0.0;
            } else {
                oldMean = mean;
                sum += value;
                mean = sum / count;
                stdDeviation += (value - oldMean) * (value - mean);
            }

            if (count < 2) {
                return 0.0;
            }
            return Math.sqrt(stdDeviation / count);
        }

        @Override
        public Object processRemove(Object data) {
            count--;
            double value = (Integer) data;

            if (count == 0) {
                sum = mean = 0.0;
                stdDeviation = 0.0;
            } else {
                oldMean = mean;
                sum -= value;
                mean = sum / count;
                stdDeviation -= (value - oldMean) * (value - mean);
            }

            if (count < 2) {
                return 0.0;
            }
            return Math.sqrt(stdDeviation / count);
        }

        @Override
        public Object reset() {
            sum = mean = oldMean = 0.0;
            stdDeviation = 0.0;
            count = 0;
            return 0.0;
        }

        @Override
        public Map<String, Object> currentState() {
            Map<String, Object> state = new HashMap<>();
            state.put("Sum", sum);
            state.put("Mean", mean);
            state.put("OldMean", oldMean);
            state.put("stdDeviation", stdDeviation);
            state.put("Count", count);
            return state;
        }

        @Override
        public void restoreState(Map<String, Object> state) {
            sum = (Long) state.get("Sum");
            mean = (Long) state.get("Mean");
            oldMean = (Long) state.get("OldMean");
            stdDeviation = (Long) state.get("stdDeviation");
            count = (int) state.get("Count");
        }
    }

    /**
     * Standard deviation aggregator for Long values
     */
    private class StdDevAttributeAggregatorLong extends StdDevAttributeAggregator {
        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private double mean, oldMean, stdDeviation, sum;
        private int count = 0;

        @Override
        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object data) {
            // See here for the algorithm: http://www.johndcook.com/blog/standard_deviation/
            count++;
            double value = (Long) data;

            if (count == 1) {
                sum = mean = oldMean = value;
                stdDeviation = 0.0;
            } else {
                oldMean = mean;
                sum += value;
                mean = sum / count;
                stdDeviation += (value - oldMean) * (value - mean);
            }

            if (count < 2) {
                return 0.0;
            }
            return Math.sqrt(stdDeviation / count);
        }

        @Override
        public Object processRemove(Object data) {
            count--;
            double value = (Long) data;

            if (count == 0) {
                sum = mean = 0.0;
                stdDeviation = 0.0;
            } else {
                oldMean = mean;
                sum -= value;
                mean = sum / count;
                stdDeviation -= (value - oldMean) * (value - mean);
            }

            if (count < 2) {
                return 0.0;
            }
            return Math.sqrt(stdDeviation / count);
        }

        @Override
        public Object reset() {
            sum = mean = oldMean = 0.0;
            stdDeviation = 0.0;
            count = 0;
            return 0.0;
        }

        @Override
        public Map<String, Object> currentState() {
            Map<String, Object> state = new HashMap<>();
            state.put("Sum", sum);
            state.put("Mean", mean);
            state.put("OldMean", oldMean);
            state.put("stdDeviation", stdDeviation);
            state.put("Count", count);
            return state;
        }

        @Override
        public void restoreState(Map<String, Object> state) {
            sum = (Long) state.get("Sum");
            mean = (Long) state.get("Mean");
            oldMean = (Long) state.get("OldMean");
            stdDeviation = (Long) state.get("stdDeviation");
            count = (int) state.get("Count");
        }
    }
}
