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

package org.wso2.siddhi.extension.math;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

/*
* min(a,b);
* Returns the minimum of 'a' and 'b'.
* Accept Type(s): INT,LONG,FLOAT,DOUBLE
* Return Type(s): DOUBLE
*/
public class MinFunctionExtension extends FunctionExecutor{
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 2) {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to math:min() function, " +
                    "required 2, but found " + attributeExpressionExecutors.length);
        }
        Attribute.Type attributeType = attributeExpressionExecutors[0].getReturnType();
        if (!((attributeType == Attribute.Type.DOUBLE)
                || (attributeType == Attribute.Type.INT)
                || (attributeType == Attribute.Type.FLOAT)
                || (attributeType == Attribute.Type.LONG))) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the first argument of math:min() function, " +
                    "required " + Attribute.Type.INT + " or " + Attribute.Type.LONG +
                    " or " + Attribute.Type.FLOAT + " or " + Attribute.Type.DOUBLE +
                    ", but found " + attributeType.toString());
        }
        attributeType = attributeExpressionExecutors[1].getReturnType();
        if (!((attributeType == Attribute.Type.DOUBLE)
                || (attributeType == Attribute.Type.INT)
                || (attributeType == Attribute.Type.FLOAT)
                || (attributeType == Attribute.Type.LONG))) {
            throw new ExecutionPlanValidationException("Invalid parameter type found for the second argument of math:min() function, " +
                    "required " + Attribute.Type.INT + " or " + Attribute.Type.LONG +
                    " or " + Attribute.Type.FLOAT + " or " + Attribute.Type.DOUBLE +
                    ", but found " + attributeType.toString());
        }
    }

    @Override
    protected Object execute(Object[] data) {
        double inputVal1 = 0d;
        double inputVal2 = 0d;
        if (data[0] != null) {
            //type-conversion
            if (data[0] instanceof Integer) {
                int inputInt = (Integer) data[0];
                inputVal1 = (double) inputInt;
            } else if (data[0] instanceof Long) {
                long inputLong = (Long) data[0];
                inputVal1 = (double) inputLong;
            } else if (data[0] instanceof Float) {
                float inputLong = (Float) data[0];
                inputVal1 = (double) inputLong;
            } else if (data[0] instanceof Double) {
                inputVal1 = (Double) data[0];
            }
        } else {
            throw new ExecutionPlanRuntimeException("Input to the math:min() function cannot be null");
        }
        if (data[1] != null) {
            //type-conversion
            if (data[1] instanceof Integer) {
                int inputInt = (Integer) data[1];
                inputVal2 = (double) inputInt;
            } else if (data[1] instanceof Long) {
                long inputLong = (Long) data[1];
                inputVal2 = (double) inputLong;
            } else if (data[1] instanceof Float) {
                float inputLong = (Float) data[1];
                inputVal2 = (double) inputLong;
            } else if (data[1] instanceof Double) {
                inputVal2 = (Double) data[1];
            }
        } else {
            throw new ExecutionPlanRuntimeException("Input to the math:min() function cannot be null");
        }
        return Math.min(inputVal1, inputVal2);
    }

    @Override
    protected Object execute(Object data) {
        return null;  //Since the min function takes in 2 parameters, this method does not get called. Hence, not implemented.
    }

    @Override
    public void start() {
        //Nothing to start.
    }

    @Override
    public void stop() {
        //Nothing to stop.
    }

    @Override
    public Attribute.Type getReturnType() {
        return Attribute.Type.DOUBLE;
    }

    @Override
    public Object[] currentState() {
        return null;    //No need to maintain state.
    }

    @Override
    public void restoreState(Object[] state) {
        //Since there's no need to maintain a state, nothing needs to be done here.
    }
}
