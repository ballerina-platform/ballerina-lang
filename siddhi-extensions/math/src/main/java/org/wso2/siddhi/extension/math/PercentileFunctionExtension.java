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

package org.wso2.siddhi.extension.math;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import java.util.ArrayList;
import java.util.List;

/**
 * percentile(values, p)
 * Returns an estimate for the pth percentile of the stored values
 * Accept Type(s): FLOAT,INT,LONG,DOUBLE
 * Return Type: DOUBLE
 */
public class PercentileFunctionExtension extends AttributeAggregator {

    private PercentileFunctionExtension percentileOutputFunctionExtension;
    private double percentileValue;

    /**
     * The initialization method for FunctionExecutor
     *
     * @param attributeExpressionExecutors are the executors of each attributes in the function
     * @param executionPlanContext Execution plan runtime context
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {

        if (attributeExpressionExecutors.length != 2) {
            throw new OperationNotSupportedException("Percentile function has to have exactly 2 parameter, currently "
                    + attributeExpressionExecutors.length + " parameters provided");
        }

        if (!(attributeExpressionExecutors[1] instanceof ConstantExpressionExecutor)) {
            throw new OperationNotSupportedException("Percentile value has to be a constant");
        }

        Object percentileValueObject = attributeExpressionExecutors[1].execute(null);
        if (percentileValueObject instanceof Double) {
            percentileValue = ((Double) percentileValueObject);
        } else {
            throw new OperationNotSupportedException("Percentile value should be of type double. But found "
                    + attributeExpressionExecutors[1].getReturnType());
        }

        if (percentileValue <= 0 || percentileValue > 100) {
            throw new OperationNotSupportedException(
                    "Percentile value should be in 0 < p ≤ 100 range. But found " + percentileValue);
        }

        Attribute.Type type = attributeExpressionExecutors[0].getReturnType();

        switch (type) {
        case FLOAT:
            percentileOutputFunctionExtension = new PercentileFunctionExtensionFloat();
            break;
        case INT:
            percentileOutputFunctionExtension = new PercentileFunctionExtensionInt();
            break;
        case LONG:
            percentileOutputFunctionExtension = new PercentileFunctionExtensionLong();
            break;
        case DOUBLE:
            percentileOutputFunctionExtension = new PercentileFunctionExtensionDouble();
            break;
        default:
            throw new OperationNotSupportedException("Percentile not supported for " + type);
        }
    }

    public Attribute.Type getReturnType() {
        return percentileOutputFunctionExtension.getReturnType();
    }

    @Override
    public Object processAdd(Object data) {
        // will not occur
        return new IllegalStateException("Percentile cannot process a single argument, but found " + data.toString());
    }

    @Override
    public Object processAdd(Object[] data) {
        return percentileOutputFunctionExtension.processAdd(data);
    }

    @Override
    public Object processRemove(Object data) {
        // will not occur
        return new IllegalStateException("Percentile cannot process a single argument, but found " + data.toString());

    }

    @Override
    public Object processRemove(Object[] data) {
        return percentileOutputFunctionExtension.processRemove(data);
    }

    @Override
    public Object reset() {
        return percentileOutputFunctionExtension.reset();
    }

    @Override
    public void start() {
        // Nothing to start
    }

    @Override
    public void stop() {
        // nothing to stop
    }

    @Override
    public Object[] currentState() {
        return percentileOutputFunctionExtension.currentState();
    }

    @Override
    public void restoreState(Object[] state) {
        percentileOutputFunctionExtension.restoreState(state);
    }

    class PercentileFunctionExtensionDouble extends PercentileFunctionExtension {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private long count = 0;
        private List<Double> values = new ArrayList<Double>();
        private DescriptiveStatistics stat = new DescriptiveStatistics();

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object[] data) {
            count++;
            values.add((Double) data[0]);
            if (count == 0) {
                return 0.0;
            }

            stat.clear();
            for (double value : values) {
                stat.addValue(value);
            }
            return stat.getPercentile(percentileValue);
        }

        @Override
        public Object processRemove(Object[] data) {
            count--;
            values.remove(data[0]);
            if (count == 0) {
                return 0.0;
            }

            stat.clear();
            for (double value : values) {
                stat.addValue(value);
            }
            return stat.getPercentile(percentileValue);
        }

        @Override
        public Object reset() {
            count = 0;
            values.clear();
            stat.clear();
            return 0.0;
        }

        @Override
        public Object[] currentState() {
            return new Object[] { values, count };
        }

        @Override
        public void restoreState(Object[] state) {
            values = (List) state[0];
            count = (Long) state[1];
        }
    }

    class PercentileFunctionExtensionFloat extends PercentileFunctionExtension {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private long count = 0;
        private List<Float> values = new ArrayList<Float>();
        private DescriptiveStatistics stat = new DescriptiveStatistics();

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object[] data) {
            count++;
            values.add((Float) data[0]);
            if (count == 0) {
                return 0.0;
            }

            stat.clear();
            for (float value : values) {
                stat.addValue(value);
            }
            return stat.getPercentile(percentileValue);
        }

        @Override
        public Object processRemove(Object[] data) {
            count--;
            values.remove(data[0]);
            if (count == 0) {
                return 0.0;
            }

            stat.clear();
            for (float value : values) {
                stat.addValue(value);
            }
            return stat.getPercentile(percentileValue);
        }

        @Override
        public Object reset() {
            count = 0;
            values.clear();
            stat.clear();
            return 0.0;
        }

        @Override
        public Object[] currentState() {
            return new Object[] { values, count };
        }

        @Override
        public void restoreState(Object[] state) {
            values = (List) state[0];
            count = (Long) state[1];
        }
    }

    class PercentileFunctionExtensionInt extends PercentileFunctionExtension {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private long count = 0;
        private List<Integer> values = new ArrayList<Integer>();
        private DescriptiveStatistics stat = new DescriptiveStatistics();

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object[] data) {
            count++;
            values.add((Integer) data[0]);
            if (count == 0) {
                return 0.0;
            }

            stat.clear();
            for (int value : values) {
                stat.addValue(value);
            }
            return stat.getPercentile(percentileValue);
        }

        @Override
        public Object processRemove(Object[] data) {
            count--;
            values.remove(data[0]);
            if (count == 0) {
                return 0.0;
            }

            stat.clear();
            for (int value : values) {
                stat.addValue(value);
            }
            return stat.getPercentile(percentileValue);
        }

        @Override
        public Object reset() {
            count = 0;
            values.clear();
            stat.clear();
            return 0.0;
        }

        @Override
        public Object[] currentState() {
            return new Object[] { values, count };
        }

        @Override
        public void restoreState(Object[] state) {
            values = (List) state[0];
            count = (Long) state[1];
        }

    }

    class PercentileFunctionExtensionLong extends PercentileFunctionExtension {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private long count = 0;
        private List<Long> values = new ArrayList<Long>();
        private DescriptiveStatistics stat = new DescriptiveStatistics();

        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object[] data) {
            count++;
            values.add((Long) data[0]);
            if (count == 0) {
                return 0.0;
            }

            stat.clear();
            for (long value : values) {
                stat.addValue(value);
            }
            return stat.getPercentile(percentileValue);
        }

        @Override
        public Object processRemove(Object[] data) {
            count--;
            values.remove(data[0]);
            if (count == 0) {
                return 0.0;
            }

            stat.clear();
            for (long value : values) {
                stat.addValue(value);
            }
            return stat.getPercentile(percentileValue);
        }

        @Override
        public Object reset() {
            count = 0;
            values.clear();
            stat.clear();
            return 0.0;
        }

        @Override
        public Object[] currentState() {
            return new Object[] { values, count };
        }

        @Override
        public void restoreState(Object[] state) {
            values = (List) state[0];
            count = (Long) state[1];
        }

    }

}
