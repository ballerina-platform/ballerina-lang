/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.executor.function;


import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

public class MinimumFunctionExecutor extends FunctionExecutor {

    private Attribute.Type returnType;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        Attribute.Type attributeTypeOne = attributeExpressionExecutors[0].getReturnType();
        if (!((attributeTypeOne == Attribute.Type.DOUBLE) || (attributeTypeOne == Attribute.Type.INT) || (attributeTypeOne == Attribute.Type.FLOAT)
                || (attributeTypeOne == Attribute.Type.LONG))) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the argument" + 1 + " of minimum() function, " +
                    "required " + Attribute.Type.INT + " or " + Attribute.Type.LONG +
                    " or " + Attribute.Type.FLOAT + " or " + Attribute.Type.DOUBLE +
                    ", but found " + attributeTypeOne.toString());
        }
        for (int i = 1; i < attributeExpressionExecutors.length; i++) {
            Attribute.Type attributeType = attributeExpressionExecutors[i].getReturnType();
            if (!((attributeType == Attribute.Type.DOUBLE) || (attributeType == Attribute.Type.INT) || (attributeType == Attribute.Type.FLOAT)
                    || (attributeType == Attribute.Type.LONG))) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for the argument" + i + " of minimum() function, " +
                        "required " + Attribute.Type.INT + " or " + Attribute.Type.LONG +
                        " or " + Attribute.Type.FLOAT + " or " + Attribute.Type.DOUBLE +
                        ", but found " + attributeType.toString());
            }
            if (attributeTypeOne != attributeType) {
                throw new ExecutionPlanValidationException("Invalid parameter type found for arguments  of minimum() function, " +
                        "all parameters should be of same type, but found " + attributeTypeOne + " and " + attributeExpressionExecutors[i].getReturnType());
            }

        }
        returnType = attributeTypeOne;
    }

    /**
     * return maximum of arbitrary long set of Double values
     *
     * @param data array of Double values
     * @return max
     */
    @Override
    protected Object execute(Object[] data) {
        double min = Double.MAX_VALUE;
        for (Object aObj : data) {
            Double value = Double.MAX_VALUE;
            if (aObj instanceof Integer) {
                int inputInt = (Integer) aObj;
                value = (double) inputInt;
            } else if (aObj instanceof Long) {
                long inputLong = (Long) aObj;
                value = (double) inputLong;
            } else if (aObj instanceof Float) {
                float inputLong = (Float) aObj;
                value = (double) inputLong;
            } else if (aObj instanceof Double) {
                value = (Double) aObj;
            }
            if (value < min) {
                min = value;
            }
        }
        switch (returnType) {
            case INT:
                return (int) min;
            case LONG:
                return (long) min;
            case FLOAT:
                return (float) min;
            case DOUBLE:
                return min;
        }
        return min;
    }

    @Override
    protected Object execute(Object data) {
        return data;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

    @Override
    public Object[] currentState() {
        return new Object[0];
    }

    @Override
    public void restoreState(Object[] state) {

    }
}
