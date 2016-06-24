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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * percentile(value, p)
 * Returns an estimate for the pth percentile of the stored values
 * Accept Type(s): value: FLOAT,INT,LONG,DOUBLE / p: DOUBLE
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

    @Override
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

    /**
     * Percentile calculation method
     *
     * To calculate the pth percentile (where p is any number greater than 0 or less than or equal to 100), do the
     * following steps:
     * 1. Order all the values in the data set from smallest to largest.
     * 2. Multiply p percent by the total number of values, n. This number is called the index.
     * 3. If the index obtained in Step 2 is not a whole number, round it up to the nearest whole number and go to Step
     * 4a. If the index obtained in Step 2 is a whole number, go to Step 4b.
     * 4a. Count the values in your data set from left to right (from the smallest to the largest value) until you reach
     * the number indicated by Step 3. The corresponding value in your data set is the pth percentile.
     * 4b. Count the values in your data set from left to right until you reach the number indicated by Step 2.
     * The pth percentile is the average of that corresponding value in your data set and the value that directly
     * follows it.
     *
     * @param valuesList values list
     * @param percentile percentile (p)
     * @return pth percentile value
     */
    public double getPercentileValue(List<Double> valuesList, double percentile) {

        double percentileIndexTemp;
        int percentileIndex;

        // calculating percentile index
        percentileIndexTemp = percentile * valuesList.size() / 100;

        if (percentileIndexTemp % 1 == 0) {
            percentileIndex = (int) percentileIndexTemp;
            if (percentileIndex == valuesList.size()) {
                return valuesList.get(percentileIndex - 1);
            } else {
                return (valuesList.get(percentileIndex - 1) + valuesList.get(percentileIndex)) / 2;
            }
        } else {
            percentileIndex = (int) Math.round(percentileIndexTemp);
            return valuesList.get(percentileIndex - 1);
        }
    }

    /**
     * Adding values to the sorted ArrayList
     *
     * @param arrayList sorted ArrayList
     * @param value new value
     */
    public void sortedArrayListAdd(List<Double> arrayList, double value) {

        int insertIndex = Collections.binarySearch(arrayList, value);

        if (insertIndex < 0) {
            arrayList.add(-insertIndex - 1, value);
        } else {
            arrayList.add(insertIndex + 1, value);
        }
    }

    /**
     * Removing values from the sorted ArrayList
     *
     * @param arrayList Sorted ArrayList
     * @param value expired value
     */
    public void sortedArrayListRemove(List<Double> arrayList, double value) {

        int removeIndex = Collections.binarySearch(arrayList, value);

        arrayList.remove(removeIndex);

    }

    class PercentileFunctionExtensionDouble extends PercentileFunctionExtension {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private long count = 0;
        private List<Double> valuesList = new ArrayList<Double>();

        @Override
        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object[] data) {
            count++;
            if (count == 0) {
                return 0.0;
            }

            double value = (Double) data[0];

            sortedArrayListAdd(valuesList, value);

            return getPercentileValue(valuesList, percentileValue);
        }

        @Override
        public Object processRemove(Object[] data) {
            count--;
            if (count == 0) {
                return 0.0;
            }

            double value = (Double) data[0];

            sortedArrayListRemove(valuesList, value);

            return getPercentileValue(valuesList, percentileValue);
        }

        @Override
        public Object reset() {
            count = 0;
            valuesList.clear();
            return 0.0;
        }

        @Override
        public Object[] currentState() {
            return new Object[] { valuesList, count };
        }

        @Override
        public void restoreState(Object[] state) {
            valuesList = (List) state[0];
            count = (Long) state[1];
        }
    }

    class PercentileFunctionExtensionFloat extends PercentileFunctionExtension {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private long count = 0;
        private List<Double> valuesList = new ArrayList<Double>();

        @Override
        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object[] data) {
            count++;
            if (count == 0) {
                return 0.0;
            }

            double value = (Float) data[0];

            sortedArrayListAdd(valuesList, value);

            return getPercentileValue(valuesList, percentileValue);
        }

        @Override
        public Object processRemove(Object[] data) {
            count--;
            if (count == 0) {
                return 0.0;
            }

            double value = (Float) data[0];

            sortedArrayListRemove(valuesList, value);

            return getPercentileValue(valuesList, percentileValue);
        }

        @Override
        public Object reset() {
            count = 0;
            valuesList.clear();
            return 0.0;
        }

        @Override
        public Object[] currentState() {
            return new Object[] { valuesList, count };
        }

        @Override
        public void restoreState(Object[] state) {
            valuesList = (List) state[0];
            count = (Long) state[1];
        }
    }

    class PercentileFunctionExtensionInt extends PercentileFunctionExtension {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private long count = 0;
        private List<Double> valuesList = new ArrayList<Double>();

        @Override
        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object[] data) {
            count++;
            if (count == 0) {
                return 0.0;
            }

            double value = (Integer) data[0];

            sortedArrayListAdd(valuesList, value);

            return getPercentileValue(valuesList, percentileValue);
        }

        @Override
        public Object processRemove(Object[] data) {
            count--;
            if (count == 0) {
                return 0.0;
            }

            double value = (Integer) data[0];

            sortedArrayListRemove(valuesList, value);

            return getPercentileValue(valuesList, percentileValue);
        }

        @Override
        public Object reset() {
            count = 0;
            valuesList.clear();
            return 0.0;
        }

        @Override
        public Object[] currentState() {
            return new Object[] { valuesList, count };
        }

        @Override
        public void restoreState(Object[] state) {
            valuesList = (List) state[0];
            count = (Long) state[1];
        }

    }

    class PercentileFunctionExtensionLong extends PercentileFunctionExtension {

        private final Attribute.Type type = Attribute.Type.DOUBLE;
        private long count = 0;
        private List<Double> valuesList = new ArrayList<Double>();

        @Override
        public Attribute.Type getReturnType() {
            return type;
        }

        @Override
        public Object processAdd(Object[] data) {
            count++;
            if (count == 0) {
                return 0.0;
            }

            double value = (Long) data[0];

            sortedArrayListAdd(valuesList, value);

            return getPercentileValue(valuesList, percentileValue);
        }

        @Override
        public Object processRemove(Object[] data) {
            count--;
            if (count == 0) {
                return 0.0;
            }

            double value = (Long) data[0];

            sortedArrayListRemove(valuesList, value);

            return getPercentileValue(valuesList, percentileValue);
        }

        @Override
        public Object reset() {
            count = 0;
            valuesList.clear();
            return 0.0;
        }

        @Override
        public Object[] currentState() {
            return new Object[] { valuesList, count };
        }

        @Override
        public void restoreState(Object[] state) {
            valuesList = (List) state[0];
            count = (Long) state[1];
        }

    }

}
