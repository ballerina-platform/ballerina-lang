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

package org.wso2.siddhi.extension.mathfunctions;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;

/*
* asin(a);
* Returns the arc sin(inverse sine) of a if -1<=a<=1 or NULL otherwise.
* Accept Type(s) :DOUBLE/FLOAT
* Return Type(s): DOUBLE
*/
public class AsinFunctionExtension extends FunctionExecutor {

    private static final Logger log = Logger.getLogger(AsinFunctionExtension.class);
    private boolean isDebugMode = false;
    Attribute.Type returnType = Attribute.Type.DOUBLE;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (log.isDebugEnabled()) {
            isDebugMode = true;
        }
        if (attributeExpressionExecutors.length != 1) {
            String errMsg = "Invalid no of Arguments Passed. Attribute Length is - "
                    + attributeExpressionExecutors.length;
            throw new ExecutionPlanCreationException(errMsg);
        }
        for (ExpressionExecutor expressionExecutor : attributeExpressionExecutors) {
            Attribute.Type attributeType = expressionExecutor.getReturnType();
            if (isDebugMode) {
                log.debug("Attribute Type - " + attributeType.toString());
            }
            if (!((attributeType == Attribute.Type.DOUBLE)
                    || (attributeType == Attribute.Type.FLOAT))) {
                String errMsg = "Invalid parameters type found - "
                        + expressionExecutor.getReturnType().toString();
                throw new ExecutionPlanCreationException(errMsg);
            }
        }
    }

    @Override
    protected Object execute(Object[] data) {
        return null;  //Since the asin function takes in only 1 parameter, this method does not get called. Hence, not implemented.
    }

    @Override
    protected Object execute(Object data) {
        double inputValue = 0d;
        double outputValue = 0d;

        if (data != null) {
            //type-conversion
            if (data instanceof Float) {
                float inputLong = (Float) data;
                inputValue = (double) inputLong;
            } else if (data instanceof Double) {
                inputValue = (Double) data;
            }

            outputValue = Math.asin(inputValue);
            if (isDebugMode) {
                log.debug("Input Value = " + inputValue + ", Output Value = " + outputValue);
            }
        } else {
            throw new OperationNotSupportedException("Input to the math:asin() function cannot be null");
        }
        return outputValue;
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
