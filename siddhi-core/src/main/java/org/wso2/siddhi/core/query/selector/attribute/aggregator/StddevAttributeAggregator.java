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

public class StddevAttributeAggregator extends AttributeAggregator {
    private StddevAttributeAggregator stddevOutputAttributeAggregator;

    /**
     * The initialization method for FunctionExecutor
     *
     * @param attributeExpressionExecutors are the executors of each attributes in the function
     * @param executionPlanContext         Execution plan runtime context
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 1) {
            throw new OperationNotSupportedException("Stddev aggregator has to have exactly 1 parameter, currently " +
                    attributeExpressionExecutors.length + " parameters provided");
        }

        Attribute.Type type = attributeExpressionExecutors[0].getReturnType();

        switch (type) {
            case INT:
                stddevOutputAttributeAggregator = new StddevAttributeAggregatorInt();
                break;
            case LONG:
                stddevOutputAttributeAggregator = new StddevAttributeAggregatorLong();
                break;
            case FLOAT:
                stddevOutputAttributeAggregator = new StddevAttributeAggregatorFloat();
                break;
            case DOUBLE:
                stddevOutputAttributeAggregator = new StddevAttributeAggregatorDouble();
                break;
            default:
                throw new OperationNotSupportedException("Stddev not supported for " + type);
        }
    }

    @Override
    public Attribute.Type getReturnType() {
        return stddevOutputAttributeAggregator.getReturnType();
    }

    @Override
    public Object processAdd(Object data) {
        return stddevOutputAttributeAggregator.processAdd(data);
    }

    @Override
    public Object processAdd(Object[] data) {
        return new IllegalStateException("Stddev cannot process data array, but found " + Arrays.deepToString(data));
    }

    @Override
    public Object processRemove(Object data) {
        return stddevOutputAttributeAggregator.processRemove(data);
    }

    @Override
    public Object processRemove(Object[] data) {
        return new IllegalStateException("Stddev cannot process data array, but found " + Arrays.deepToString(data));
    }

    @Override
    public Object reset() {
        return stddevOutputAttributeAggregator.reset();
    }

    @Override
    public Object[] currentState() {
        return stddevOutputAttributeAggregator.currentState();
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void restoreState(Object[] state) {
        stddevOutputAttributeAggregator.restoreState(state);
    }

    private class StddevAttributeAggregatorDouble extends StddevAttributeAggregator {
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
        public Object[] currentState() {
            return new Object[]{sum, mean, oldMean, stdDeviation, count};
        }

        @Override
        public void restoreState(Object[] state) {
            sum = (Double) state[0];
            mean = (Double) state[1];
            oldMean = (Double) state[2];
            stdDeviation = (Double) state[3];
            count = (Integer) state[4];
        }
    }

    private class StddevAttributeAggregatorFloat extends StddevAttributeAggregator {
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
        public Object[] currentState() {
            return new Object[]{sum, mean, oldMean, stdDeviation, count};
        }

        @Override
        public void restoreState(Object[] state) {
            sum = (Double) state[0];
            mean = (Double) state[1];
            oldMean = (Double) state[2];
            stdDeviation = (Double) state[3];
            count = (Integer) state[4];
        }
    }

    private class StddevAttributeAggregatorInt extends StddevAttributeAggregator {
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
        public Object[] currentState() {
            return new Object[]{sum, mean, oldMean, stdDeviation, count};
        }

        @Override
        public void restoreState(Object[] state) {
            sum = (Double) state[0];
            mean = (Double) state[1];
            oldMean = (Double) state[2];
            stdDeviation = (Double) state[3];
            count = (Integer) state[4];
        }
    }

    private class StddevAttributeAggregatorLong extends StddevAttributeAggregator {
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
        public Object[] currentState() {
            return new Object[]{sum, mean, oldMean, stdDeviation, count};
        }

        @Override
        public void restoreState(Object[] state) {
            sum = (Double) state[0];
            mean = (Double) state[1];
            oldMean = (Double) state[2];
            stdDeviation = (Double) state[3];
            count = (Integer) state[4];
        }
    }
}
