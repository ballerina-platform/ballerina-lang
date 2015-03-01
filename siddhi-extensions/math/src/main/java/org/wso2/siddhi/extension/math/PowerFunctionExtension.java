/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.math;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;

/*
* power(value,toPower);
* A Class which is used to calculate power of a value.
* value - Accept Type(s):DOUBLE/INT/FLOAT/LONG
* toPower - Accept Type(s):DOUBLE/INT/FLOAT/LONG
* Return Type(s): DOUBLE
*/
public class PowerFunctionExtension extends FunctionExecutor {

    private static final Logger log = Logger.getLogger(PowerFunctionExtension.class);
    private boolean isDebugMode = false;
    Attribute.Type returnType = Attribute.Type.DOUBLE;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (log.isDebugEnabled()) {
            isDebugMode = true;
        }
        if (attributeExpressionExecutors.length != 2) {
            String errMsg = "Invalid no of Arguments Passed. Required 2. Found " + attributeExpressionExecutors.length;
            throw new ExecutionPlanCreationException(errMsg);
        }
        for (ExpressionExecutor expressionExecutor : attributeExpressionExecutors) {
            Attribute.Type attributeType = expressionExecutor.getReturnType();
            if (isDebugMode) {
                log.debug("Attribute Type - " + attributeType.toString());
            }
            if (!((attributeType == Attribute.Type.DOUBLE)
                    || (attributeType == Attribute.Type.INT)
                    || (attributeType == Attribute.Type.FLOAT)
                    || (attributeType == Attribute.Type.LONG))) {
                String errMsg = "Invalid parameter type found - " + attributeType.toString();
                throw new ExecutionPlanCreationException(errMsg);
            }
        }
    }

    @Override
    protected Object execute(Object[] data) {
        double inputVal1 = 0d;
        double inputVal2 = 0d;
        double outputValue = 0d;

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
            throw new OperationNotSupportedException("Input to the math:power() function cannot be null");
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
            throw new OperationNotSupportedException("Input to the math:power() function cannot be null");
        }
        outputValue = Math.pow(inputVal1, inputVal2);
        if (isDebugMode) {
            log.debug("Value = " + inputVal1 + ", To Power =" + inputVal2 + ", Output is " + outputValue);
        }
        return outputValue;
    }

    @Override
    protected Object execute(Object data) {
        return null;        //Since the power function takes in 2 parameters, this method does not get called. Hence, not implemented.
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
        return returnType;
    }

    @Override
    public Object[] currentState() {
        return null;    //No need to maintain state.
    }

    @Override
    public void restoreState(Object[] state) {
        if (log.isDebugEnabled()) {
            isDebugMode = true;
        }
    }
}
