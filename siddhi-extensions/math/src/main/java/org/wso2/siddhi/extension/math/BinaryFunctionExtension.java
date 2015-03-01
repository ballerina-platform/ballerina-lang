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

/**
 * bin(a)
 * A Class which is used to do binary conversion.
 * Accept Type(s):INT
 * Return Type(s): STRING
 */
public class BinaryFunctionExtension extends FunctionExecutor {

    private static final Logger log = Logger.getLogger(BinaryFunctionExtension.class);
    private boolean isDebugMode = false;
    Attribute.Type returnType = Attribute.Type.STRING;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 1) {
            throw new ExecutionPlanCreationException("Invalid no of Arguments Passed. Required 1. Found " + attributeExpressionExecutors.length);
        } else {
            Attribute.Type attributeType = attributeExpressionExecutors[0].getReturnType();
            if (attributeType != Attribute.Type.INT) {
                throw new ExecutionPlanCreationException(
                        "math:bin() function can have only an int parameter");
            } else {
                if (log.isDebugEnabled()) {
                    isDebugMode = true;
                }
            }
        }
    }

    @Override
    protected Object execute(Object[] data) {
        return null;  //Since the bin function takes in only 1 parameter, this method does not get called. Hence, not implemented.
    }

    @Override
    protected Object execute(Object data) {
        String output;
        int input;
        if (data != null) {
            input = (Integer) data;
            output = Integer.toBinaryString(input);
            if (isDebugMode) {
                log.debug("Input value is " + input + ", Binary result is " + output);
            }
            return output;
        } else {
            throw new OperationNotSupportedException("Input to the math:bin() function cannot be null");
        }
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
